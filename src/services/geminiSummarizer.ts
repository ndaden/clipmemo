import { NoteCategory, ReelNote, StructuredNoteData } from '../types/note';
import { OfflineHeuristicExtractor } from './offlineHeuristicExtractor';

const SYSTEM_PROMPT = `
You are an expert AI assistant specialized in analyzing Instagram Reels and transforming them into structured, actionable notes.
Extract the information into valid JSON with this exact schema:
{
  "category": "RECIPE" | "TUTORIAL" | "WORKOUT" | "TIPS_INFO" | "TRAVEL" | "PRODUCT" | "GENERAL",
  "title": "Clear, precise title describing the content",
  "summary": "1-2 sentence TL;DR of the reel",
  "prepTime": "e.g. 15 min or null",
  "cookTime": "e.g. 25 min or null",
  "servings": "e.g. 4 personnes or null",
  "ingredients": [
    {"name": "ingredient name", "amount": "number or fraction e.g. 250", "unit": "g, ml, c. à soupe, cup etc"}
  ],
  "steps": [
    {"stepNumber": 1, "instruction": "Clear, concise action step"}
  ],
  "keyTakeaways": ["Key takeaway 1", "Key takeaway 2"],
  "tips": ["Pro tip or advice mentioned in reel"],
  "tags": ["tag1", "tag2"]
}
Only output the JSON object, with no markdown backticks or commentary.
`;

export interface SummarizeParams {
  caption: string;
  reelUrl: string;
  shortcode: string;
  author?: string | null;
  thumbnailUrl?: string | null;
  preferredLanguage?: 'fr' | 'en';
  apiKey?: string;
}

export const GeminiSummarizer = {
  async summarize(params: SummarizeParams): Promise<ReelNote> {
    const {
      caption,
      reelUrl,
      shortcode,
      author,
      thumbnailUrl,
      preferredLanguage = 'fr',
      apiKey,
    } = params;

    const effectiveKey =
      apiKey?.trim() ||
      (typeof process !== 'undefined' && (process.env?.GEMINI_API_KEY || process.env?.VITE_GEMINI_API_KEY)) ||
      '';

    if (!effectiveKey) {
      console.info('No Gemini API key supplied. Using OfflineHeuristicExtractor.');
      return OfflineHeuristicExtractor.extract(
        caption,
        reelUrl,
        shortcode,
        author,
        thumbnailUrl,
        preferredLanguage
      );
    }

    try {
      const models = ['gemini-2.5-flash', 'gemini-1.5-flash', 'gemini-2.0-flash'];
      const langPrompt = preferredLanguage === 'fr' ? 'French (Français)' : 'English';
      const prompt = `Output language: ${langPrompt}.\n\nAnalyze this Instagram Reel caption and content:\n"${caption}"`;

      let jsonText = '';

      for (const model of models) {
        try {
          const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${effectiveKey}`;
          const res = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              contents: [{ role: 'user', parts: [{ text: prompt }] }],
              systemInstruction: { parts: [{ text: SYSTEM_PROMPT }] },
              generationConfig: {
                responseMimeType: 'application/json',
                temperature: 0.2,
              },
            }),
          });

          if (!res.ok) {
            console.warn(`Gemini model ${model} returned HTTP ${res.status}`);
            continue;
          }

          const data = await res.json();
          jsonText = data.candidates?.[0]?.content?.parts?.[0]?.text?.trim() || '';
          if (jsonText) break;
        } catch (e) {
          console.warn(`Model ${model} fetch failed:`, e);
        }
      }

      if (!jsonText) {
        throw new Error('No candidate returned from Gemini');
      }

      let parsed: any;
      try {
        const cleanJson = jsonText.replace(/```(?:json)?/gi, '').replace(/```/g, '').trim();
        parsed = JSON.parse(cleanJson);
      } catch (e) {
        const firstBrace = jsonText.indexOf('{');
        const lastBrace = jsonText.lastIndexOf('}');
        if (firstBrace !== -1 && lastBrace > firstBrace) {
          parsed = JSON.parse(jsonText.substring(firstBrace, lastBrace + 1));
        } else {
          throw new Error('Invalid JSON received from Gemini');
        }
      }

      const validCategories: NoteCategory[] = [
        'RECIPE',
        'TUTORIAL',
        'WORKOUT',
        'TIPS_INFO',
        'TRAVEL',
        'PRODUCT',
        'GENERAL',
      ];
      const category: NoteCategory = validCategories.includes(parsed.category)
        ? parsed.category
        : 'GENERAL';

      const structuredData: StructuredNoteData = {
        summary: parsed.summary || '',
        ingredients: Array.isArray(parsed.ingredients)
          ? parsed.ingredients.map((ing: any) => ({
              name: String(ing.name || ''),
              amount: ing.amount ? String(ing.amount) : '',
              unit: ing.unit ? String(ing.unit) : '',
              isChecked: false,
            }))
          : [],
        steps: Array.isArray(parsed.steps)
          ? parsed.steps.map((st: any, idx: number) => ({
              stepNumber: st.stepNumber || idx + 1,
              instruction: String(st.instruction || ''),
              isDone: false,
            }))
          : [],
        keyTakeaways: Array.isArray(parsed.keyTakeaways) ? parsed.keyTakeaways.map(String) : [],
        prepTime: parsed.prepTime ? String(parsed.prepTime) : null,
        cookTime: parsed.cookTime ? String(parsed.cookTime) : null,
        servings: parsed.servings ? String(parsed.servings) : null,
        tips: Array.isArray(parsed.tips) ? parsed.tips.map(String) : [],
      };

      const tags: string[] = Array.isArray(parsed.tags) ? parsed.tags.map(String) : [];
      const title: string = parsed.title || `Note - Reel ${shortcode}`;

      const markdownContent = OfflineHeuristicExtractor.buildMarkdown(
        title,
        author,
        reelUrl,
        category,
        structuredData,
        tags,
        preferredLanguage
      );

      return {
        id: `note-${Date.now()}-${Math.random().toString(36).substr(2, 6)}`,
        reelUrl,
        shortcode,
        author: author || null,
        title,
        category,
        summary: structuredData.summary || title,
        rawCaption: caption,
        markdownContent,
        structuredData,
        thumbnailUrl: thumbnailUrl || null,
        tags,
        isFavorite: false,
        createdAt: Date.now(),
        updatedAt: Date.now(),
      };
    } catch (err) {
      console.warn('Gemini summarization failed, using offline heuristics fallback:', err);
      return OfflineHeuristicExtractor.extract(
        caption,
        reelUrl,
        shortcode,
        author,
        thumbnailUrl,
        preferredLanguage
      );
    }
  },
};
