import {
  NoteCategory,
  ReelNote,
  StructuredNoteData,
  IngredientItem,
  StepItem,
  CATEGORIES_CONFIG,
} from '../types/note';
import { CaptionSanitizer } from './captionSanitizer';

export const OfflineHeuristicExtractor = {
  RECIPE_KEYWORDS: [
    'recette', 'recipe', 'ingrédient', 'ingredient', 'cuisson', 'cuire', 'bake', 'cook',
    'cuillère', 'tbsp', 'tsp', 'gramme', 'g de', 'ml', 'cup', 'tasse', 'farine', 'pasta',
    'pâtes', 'sauce', 'airfryer', 'poêle', 'casserole', 'four', 'four préchauffé', 'sel', 'poivre'
  ],

  WORKOUT_KEYWORDS: [
    'workout', 'fitness', 'exercice', 'exercise', 'sets', 'reps', 'séries', 'répétitions',
    'squat', 'pompe', 'bench', 'haltère', 'muscu', 'musculation', 'abdos', 'cardio', 'training',
    'repos', 'rest', 'hiit', 'warmup'
  ],

  TIPS_KEYWORDS: [
    'astuce', 'astuces', 'tips', 'hack', 'hacks', 'conseil', 'conseils', 'secret', 'secrets',
    'savais-tu', 'did you know', 'booster', 'optimiser', 'saviez-vous', 'productivité'
  ],

  TUTORIAL_KEYWORDS: [
    'tuto', 'tutorial', 'diy', 'how to', 'comment faire', 'guide', 'étapes', 'fabrication'
  ],

  TRAVEL_KEYWORDS: [
    'voyage', 'travel', 'hôtel', 'hotel', 'destination', 'visiter', 'explore', 'trip', 'vacances', 'roadtrip'
  ],

  PRODUCT_KEYWORDS: [
    'unboxing', 'review', 'revue', 'avis', 'test', 'produit', 'code promo', 'haul'
  ],

  // Matches bullet or number, amount, unit, ingredient name
  INGREDIENT_PATTERN: /^(?:[-*•\d.)]\s*)?(\d+(?:[.,/]\d+)?)\s*(g|kg|ml|cl|l|cuillères?|c\.?\s*à\s*(?:soupe|café)|tbsp|tsp|cups?|tasses?|tranches?|gousses?|pincées?|boîtes?)?\s*(?:de\s+|d'|of\s+)?(.+)$/i,

  // Matches numbered steps: 1. or 1) or Étape 1: or Step 1:
  STEP_PATTERN: /^(?:(?:Étape|Step)\s*\d+[\s:.-]*|\d+[\s.)-]+)\s*(.+)$/i,

  extract(
    caption: string,
    reelUrl: string,
    shortcode: string,
    author?: string | null,
    thumbnailUrl?: string | null,
    language: 'fr' | 'en' = 'fr'
  ): ReelNote {
    const lines = caption
      .split('\n')
      .map(l => l.trim())
      .filter(l => l.length > 0);
    const lowerCaption = caption.toLowerCase();

    // 1. Extract hashtags
    const tags = CaptionSanitizer.extractHashtags(caption);

    // 2. Detect Category
    const category = this.detectCategory(lowerCaption, tags);

    // 3. Extract Title
    const title = this.extractTitle(lines, shortcode, category, language);

    // 4. Extract Structured Data
    let structuredData: StructuredNoteData;
    switch (category) {
      case 'RECIPE':
        structuredData = this.parseRecipeData(lines, lowerCaption, language);
        break;
      case 'WORKOUT':
        structuredData = this.parseWorkoutData(lines, language);
        break;
      case 'TUTORIAL':
        structuredData = this.parseTutorialData(lines, language);
        break;
      case 'TIPS_INFO':
        structuredData = this.parseTipsData(lines, language);
        break;
      default:
        structuredData = this.parseGeneralData(lines, language);
        break;
    }

    // 5. Build Markdown
    const markdownContent = this.buildMarkdown(title, author, reelUrl, category, structuredData, tags, language);

    const summary = structuredData.summary ||
      lines.find(l => !l.startsWith('#') && l.length > 20) ||
      title;

    return {
      id: `note-${Date.now()}-${Math.random().toString(36).substr(2, 6)}`,
      reelUrl,
      shortcode,
      author: author || null,
      title,
      category,
      summary,
      rawCaption: caption,
      markdownContent,
      structuredData,
      thumbnailUrl: thumbnailUrl || null,
      tags,
      isFavorite: false,
      createdAt: Date.now(),
      updatedAt: Date.now(),
    };
  },

  detectCategory(lowerText: string, tags: string[]): NoteCategory {
    const score = (keywords: string[]) => keywords.filter(kw => lowerText.includes(kw)).length;

    const recipeScore = score(this.RECIPE_KEYWORDS);
    const workoutScore = score(this.WORKOUT_KEYWORDS);
    const tipsScore = score(this.TIPS_KEYWORDS);
    const tutorialScore = score(this.TUTORIAL_KEYWORDS);
    const travelScore = score(this.TRAVEL_KEYWORDS);
    const productScore = score(this.PRODUCT_KEYWORDS);

    if (recipeScore >= 2 || (recipeScore >= 1 && (lowerText.includes('ingrédient') || lowerText.includes('ingredient')))) {
      return 'RECIPE';
    }
    if (workoutScore >= 2 || (workoutScore >= 1 && (lowerText.includes('reps') || lowerText.includes('séries')))) {
      return 'WORKOUT';
    }
    if (tipsScore >= 2) return 'TIPS_INFO';
    if (tutorialScore >= 2) return 'TUTORIAL';
    if (travelScore >= 2) return 'TRAVEL';
    if (productScore >= 2) return 'PRODUCT';

    if (recipeScore === 1) return 'RECIPE';
    if (workoutScore === 1) return 'WORKOUT';
    if (tipsScore === 1) return 'TIPS_INFO';
    if (tutorialScore === 1) return 'TUTORIAL';

    return 'GENERAL';
  },

  extractTitle(lines: string[], shortcode: string, category: NoteCategory, language: 'fr' | 'en'): string {
    for (const line of lines) {
      const clean = line.replace(/^[^\p{L}\p{N}]+/gu, '').trim();
      if (clean.length > 3 && !clean.startsWith('#') && !clean.startsWith('http')) {
        return clean.slice(0, 90);
      }
    }
    const catLabel = language === 'fr' ? CATEGORIES_CONFIG[category].labelFr : CATEGORIES_CONFIG[category].labelEn;
    return `${catLabel} - Reel ${shortcode}`;
  },

  parseRecipeData(lines: string[], lowerCaption: string, language: 'fr' | 'en'): StructuredNoteData {
    const ingredients: IngredientItem[] = [];
    const steps: StepItem[] = [];
    const tips: string[] = [];

    let inIngredients = false;
    let inSteps = false;

    // Detect prep time, cook time, servings if mentioned
    let prepTime: string | null = null;
    let cookTime: string | null = null;
    let servings: string | null = null;

    const timeMatch = lowerCaption.match(/(?:prep|préparation)[\s:]*(\d+\s*min(?:utes)?)/i);
    if (timeMatch) prepTime = timeMatch[1];

    const cookMatch = lowerCaption.match(/(?:cuisson|cook)[\s:]*(\d+\s*min(?:utes)?)/i);
    if (cookMatch) cookTime = cookMatch[1];

    const servMatch = lowerCaption.match(/(?:pour|servings|portions?)[\s:]*(\d+(?:-\d+)?\s*(?:personnes|pers|portions)?)/i);
    if (servMatch) servings = servMatch[1];

    for (const line of lines) {
      const lower = line.toLowerCase();
      if (lower.includes('ingrédient') || lower.includes('ingredients')) {
        inIngredients = true;
        inSteps = false;
        continue;
      }
      if (lower.includes('étape') || lower.includes('instruction') || lower.includes('préparation') || lower.includes('preparation') || lower.includes('recette :')) {
        inSteps = true;
        inIngredients = false;
        continue;
      }
      if (line.startsWith('#')) {
        inIngredients = false;
        inSteps = false;
        continue;
      }

      if (inIngredients) {
        const match = line.match(this.INGREDIENT_PATTERN);
        if (match) {
          ingredients.push({
            name: match[3]?.trim() || '',
            amount: match[1]?.trim() || '',
            unit: match[2]?.trim() || '',
            isChecked: false,
          });
        } else if (line.startsWith('-') || line.startsWith('•') || line.startsWith('*')) {
          const clean = line.replace(/^[-•*]\s*/, '').trim();
          if (clean) ingredients.push({ name: clean, isChecked: false });
        }
      } else if (inSteps) {
        const stepMatch = line.match(this.STEP_PATTERN);
        if (stepMatch && stepMatch[1]) {
          steps.push({
            stepNumber: steps.length + 1,
            instruction: stepMatch[1].trim(),
            isDone: false,
          });
        } else if (line.startsWith('-') || line.startsWith('•')) {
          const clean = line.replace(/^[-•]\s*/, '').trim();
          if (clean) {
            steps.push({
              stepNumber: steps.length + 1,
              instruction: clean,
              isDone: false,
            });
          }
        }
      } else {
        const stepMatch = line.match(this.STEP_PATTERN);
        if (stepMatch && stepMatch[1]) {
          steps.push({
            stepNumber: steps.length + 1,
            instruction: stepMatch[1].trim(),
            isDone: false,
          });
        }
      }
    }

    // Fallback: If sections were not explicitly marked
    if (ingredients.length === 0) {
      for (const line of lines) {
        if (line.startsWith('#')) continue;
        const match = line.match(this.INGREDIENT_PATTERN);
        if (match && (match[2] || line.startsWith('-') || line.startsWith('•'))) {
          ingredients.push({
            name: match[3]?.trim() || '',
            amount: match[1]?.trim() || '',
            unit: match[2]?.trim() || '',
            isChecked: false,
          });
        }
      }
    }

    return {
      summary: language === 'fr' ? 'Recette extraite et structurée depuis le Reel' : 'Recipe extracted and structured from Reel',
      ingredients,
      steps,
      keyTakeaways: [],
      prepTime,
      cookTime,
      servings,
      tips,
    };
  },

  parseWorkoutData(lines: string[], language: 'fr' | 'en'): StructuredNoteData {
    const steps: StepItem[] = [];
    const takeaways: string[] = [];

    for (const line of lines) {
      if (line.startsWith('#')) continue;
      const stepMatch = line.match(this.STEP_PATTERN);
      if (stepMatch && stepMatch[1]) {
        steps.push({
          stepNumber: steps.length + 1,
          instruction: stepMatch[1].trim(),
          isDone: false,
        });
      } else if (line.includes('x') || line.includes('sets') || line.includes('reps') || line.includes('séries')) {
        takeaways.push(line.replace(/^[-•*]\s*/, '').trim());
      }
    }

    return {
      summary: language === 'fr' ? 'Séance d’entraînement et répétitions' : 'Workout routine and repetitions',
      ingredients: [],
      steps,
      keyTakeaways: takeaways,
      tips: [],
    };
  },

  parseTutorialData(lines: string[], language: 'fr' | 'en'): StructuredNoteData {
    const steps: StepItem[] = [];
    const takeaways: string[] = [];

    for (const line of lines) {
      if (line.startsWith('#')) continue;
      const stepMatch = line.match(this.STEP_PATTERN);
      if (stepMatch && stepMatch[1]) {
        steps.push({
          stepNumber: steps.length + 1,
          instruction: stepMatch[1].trim(),
          isDone: false,
        });
      } else if (line.startsWith('-') || line.startsWith('•')) {
        takeaways.push(line.replace(/^[-•]\s*/, '').trim());
      }
    }

    return {
      summary: language === 'fr' ? 'Guide étape par étape' : 'Step-by-step tutorial guide',
      ingredients: [],
      steps,
      keyTakeaways: takeaways,
      tips: [],
    };
  },

  parseTipsData(lines: string[], language: 'fr' | 'en'): StructuredNoteData {
    const takeaways: string[] = [];

    for (const line of lines) {
      if (line.startsWith('#')) continue;
      if (line.startsWith('•') || line.startsWith('-') || line.startsWith('*')) {
        const clean = line.replace(/^[•\-*]\s*/, '').trim();
        if (clean.length > 5) takeaways.push(clean);
      } else {
        const stepMatch = line.match(this.STEP_PATTERN);
        if (stepMatch && stepMatch[1]) {
          takeaways.push(stepMatch[1].trim());
        }
      }
    }

    return {
      summary: language === 'fr' ? 'Points clés et conseils pratiques' : 'Key takeaways and practical advice',
      ingredients: [],
      steps: [],
      keyTakeaways: takeaways,
      tips: [],
    };
  },

  parseGeneralData(lines: string[], language: 'fr' | 'en'): StructuredNoteData {
    const takeaways = lines
      .filter(l => !l.startsWith('#') && (l.startsWith('-') || l.startsWith('•') || l.startsWith('*')))
      .map(l => l.replace(/^[-•*]\s*/, '').trim());

    return {
      summary: lines.find(l => !l.startsWith('#')) || (language === 'fr' ? 'Résumé de la publication' : 'Summary of post'),
      ingredients: [],
      steps: [],
      keyTakeaways: takeaways,
      tips: [],
    };
  },

  buildMarkdown(
    title: string,
    author: string | null | undefined,
    reelUrl: string,
    category: NoteCategory,
    data: StructuredNoteData,
    tags: string[],
    language: 'fr' | 'en'
  ): string {
    const isFr = language === 'fr';
    const lines: string[] = [];

    lines.push(`# ${title}\n`);
    if (author) {
      lines.push(`**${isFr ? 'Auteur' : 'Author'} :** ${author}  `);
    }
    const catLabel = isFr ? CATEGORIES_CONFIG[category].labelFr : CATEGORIES_CONFIG[category].labelEn;
    lines.push(`**${isFr ? 'Catégorie' : 'Category'} :** ${CATEGORIES_CONFIG[category].iconEmoji} ${catLabel}  `);
    lines.push(`**${isFr ? 'Lien Reel' : 'Reel Link'} :** [Instagram](${reelUrl})\n`);

    if (data.prepTime || data.cookTime || data.servings) {
      const meta = [
        data.prepTime ? `⏱️ Prep: ${data.prepTime}` : null,
        data.cookTime ? `🔥 Cook: ${data.cookTime}` : null,
        data.servings ? `👥 Servings: ${data.servings}` : null,
      ].filter(Boolean).join(' | ');
      lines.push(`*${meta}*\n`);
    }

    if (data.summary) {
      lines.push(`### ${isFr ? 'Résumé' : 'Summary'}\n${data.summary}\n`);
    }

    if (data.ingredients.length > 0) {
      lines.push(`## ${isFr ? 'Ingrédients' : 'Ingredients'}`);
      for (const ing of data.ingredients) {
        const qty = [ing.amount, ing.unit].filter(Boolean).join(' ');
        const check = ing.isChecked ? '[x]' : '[ ]';
        lines.push(`- ${check} ${qty ? `**${qty}** ` : ''}${ing.name}`);
      }
      lines.push('');
    }

    if (data.steps.length > 0) {
      lines.push(`## ${isFr ? 'Étapes de préparation' : 'Steps'}`);
      for (const step of data.steps) {
        lines.push(`${step.stepNumber}. ${step.instruction}`);
      }
      lines.push('');
    }

    if (data.keyTakeaways.length > 0) {
      lines.push(`## ${isFr ? 'Points clés' : 'Key Takeaways'}`);
      for (const pt of data.keyTakeaways) {
        lines.push(`- ${pt}`);
      }
      lines.push('');
    }

    if (data.tips.length > 0) {
      lines.push(`## ${isFr ? 'Astuces & Conseils' : 'Tips & Tricks'}`);
      for (const tip of data.tips) {
        lines.push(`> 💡 ${tip}`);
      }
      lines.push('');
    }

    if (tags.length > 0) {
      lines.push(`### Tags\n${tags.map(t => `#${t}`).join(' ')}\n`);
    }

    return lines.join('\n').trim();
  },
};
