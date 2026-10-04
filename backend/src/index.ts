import fastify from "fastify";
import cors from "@fastify/cors";
import rateLimit from "@fastify/rate-limit";
import multipart from "@fastify/multipart";
import dotenv from "dotenv";

dotenv.config();

const PORT = parseInt(process.env.PORT || "3000", 10);
const HOST = process.env.HOST || "0.0.0.0";
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || "";
const APP_SECRET_KEY = process.env.APP_SECRET_KEY || "";
const DEFAULT_MODEL = process.env.GEMINI_MODEL || "gemini-2.5-flash";
const FALLBACK_MODEL = process.env.GEMINI_FALLBACK_MODEL || "gemini-2.0-flash";

const server = fastify({
  logger: true,
  bodyLimit: 50 * 1024 * 1024, // 50MB max payload
});

// Setup plugins
await server.register(cors, {
  origin: true,
});

await server.register(rateLimit, {
  max: 60,
  timeWindow: "1 minute",
});

await server.register(multipart, {
  limits: {
    fileSize: 45 * 1024 * 1024, // 45MB max file upload
  },
});

// Auth hook: check X-App-Key if APP_SECRET_KEY is configured
server.addHook("preHandler", async (request, reply) => {
  if (request.url === "/health" || request.url === "/") {
    return;
  }

  if (APP_SECRET_KEY) {
    const providedKey = request.headers["x-app-key"];
    if (!providedKey || providedKey !== APP_SECRET_KEY) {
      reply.code(401).send({ error: "Unauthorized: Invalid or missing X-App-Key header" });
      return;
    }
  }
});

// Health check endpoint
server.get("/health", async () => {
  return { status: "ok", timestamp: new Date().toISOString() };
});

server.get("/", async () => {
  return { name: "ClipMemo Backend Proxy", status: "running" };
});

interface SummarizeTextBody {
  caption: string;
  preferredLanguage?: string;
}

interface SummarizeMultimodalBody {
  mediaBase64?: string;
  mimeType?: string;
  captionContext?: string;
  preferredLanguage?: string;
}

function cleanAiJsonResponse(rawText: string): any {
  let clean = rawText.trim();
  const codeBlockMatch = clean.match(/```(?:json)?\s*([\s\S]*?)\s*```/);
  if (codeBlockMatch && codeBlockMatch[1]) {
    clean = codeBlockMatch[1].trim();
  } else {
    const firstBrace = clean.indexOf("{");
    const lastBrace = clean.lastIndexOf("}");
    if (firstBrace !== -1 && lastBrace > firstBrace) {
      clean = clean.substring(firstBrace, lastBrace + 1).trim();
    }
  }
  return JSON.parse(clean);
}

async function callGeminiApi(
  contents: any[],
  systemInstruction?: string
): Promise<any> {
  if (!GEMINI_API_KEY) {
    throw new Error("GEMINI_API_KEY is not configured on the backend server.");
  }

  const models = [DEFAULT_MODEL, FALLBACK_MODEL, "gemini-1.5-flash"];
  let lastError: any = null;

  for (const model of models) {
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${GEMINI_API_KEY}`;
    
    const requestPayload: any = {
      contents,
      generationConfig: {
        responseMimeType: "application/json",
        temperature: 0.2,
      },
    };

    if (systemInstruction) {
      requestPayload.systemInstruction = {
        parts: [{ text: systemInstruction }],
      };
    }

    try {
      const resp = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestPayload),
      });

      if (resp.status === 429 || resp.status === 503) {
        server.log.warn(`Model ${model} returned ${resp.status}, trying fallback model...`);
        lastError = new Error(`HTTP ${resp.status} from Gemini (${model})`);
        continue;
      }

      if (!resp.ok) {
        const errorText = await resp.text();
        server.log.error(`Gemini API error (${model} - HTTP ${resp.status}): ${errorText}`);
        lastError = new Error(`Gemini error (${model}): HTTP ${resp.status}`);
        continue;
      }

      const jsonResponse: any = await resp.json();
      const candidateText = jsonResponse.candidates?.[0]?.content?.parts?.[0]?.text;
      if (!candidateText) {
        lastError = new Error("No text candidate returned from Gemini");
        continue;
      }

      return cleanAiJsonResponse(candidateText);
    } catch (err: any) {
      server.log.error(`Error calling Gemini model ${model}:`, err);
      lastError = err;
    }
  }

  throw lastError || new Error("Failed to call all Gemini models");
}

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
    {"name": "ingredient name", "amount": "number or fraction", "unit": "g, ml, c. à soupe, etc"}
  ],
  "steps": [
    {"stepNumber": 1, "instruction": "Clear, concise action step"}
  ],
  "keyTakeaways": ["Key takeaway 1", "Key takeaway 2"],
  "tips": ["Pro tip or advice mentioned in reel"],
  "tags": ["tag1", "tag2"]
}
`;

// Text-only summarization
server.post<{ Body: SummarizeTextBody }>("/api/summarize", async (request, reply) => {
  const { caption, preferredLanguage = "fr" } = request.body || {};

  if (!caption || !caption.trim()) {
    return reply.code(400).send({ error: "Caption is required" });
  }

  const langPrompt = preferredLanguage.startsWith("fr") ? "French (Français)" : "English";
  const userPrompt = `
Output language: ${langPrompt}.

Analyze the following Instagram Reel caption / text:
"${caption}"
`;

  try {
    const contents = [
      {
        role: "user",
        parts: [{ text: userPrompt }],
      },
    ];

    const structuredData = await callGeminiApi(contents, SYSTEM_PROMPT);
    return reply.send(structuredData);
  } catch (error: any) {
    server.log.error("Summarize error:", error);
    return reply.code(500).send({
      error: error.message || "Failed to summarize text with Gemini",
    });
  }
});

// Multimodal summarization (accepts JSON or Multipart file)
server.post("/api/summarize-multimodal", async (request, reply) => {
  let mediaBase64 = "";
  let mimeType = "video/mp4";
  let captionContext = "";
  let preferredLanguage = "fr";

  if (request.isMultipart()) {
    const parts = request.parts();
    for await (const part of parts) {
      if (part.type === "file") {
        const buffer = await part.toBuffer();
        mediaBase64 = buffer.toString("base64");
        mimeType = part.mimetype || "video/mp4";
      } else {
        const fieldName = part.fieldname;
        const value = part.value as string;
        if (fieldName === "captionContext") captionContext = value;
        if (fieldName === "preferredLanguage") preferredLanguage = value;
        if (fieldName === "mimeType") mimeType = value;
      }
    }
  } else {
    const body = (request.body as SummarizeMultimodalBody) || {};
    mediaBase64 = body.mediaBase64 || "";
    mimeType = body.mimeType || "video/mp4";
    captionContext = body.captionContext || "";
    preferredLanguage = body.preferredLanguage || "fr";
  }

  if (!mediaBase64) {
    return reply.code(400).send({ error: "Media file or mediaBase64 is required" });
  }

  const langPrompt = preferredLanguage.startsWith("fr") ? "French (Français)" : "English";
  const userPrompt = `
Output language: ${langPrompt}.

IMPORTANT INSTRUCTIONS:
1. Listen to the spoken audio and watch the video carefully. Transcribe all spoken instructions, ingredients, and exact measurements.
2. Take into account any text overlays, labels, or captions visible in the clip.
3. Context or caption provided: "${captionContext}"
`;

  try {
    const contents = [
      {
        role: "user",
        parts: [
          { text: userPrompt },
          {
            inlineData: {
              mimeType: mimeType,
              data: mediaBase64,
            },
          },
        ],
      },
    ];

    const structuredData = await callGeminiApi(contents, SYSTEM_PROMPT);
    return reply.send(structuredData);
  } catch (error: any) {
    server.log.error("Multimodal summarize error:", error);
    return reply.code(500).send({
      error: error.message || "Failed to summarize media with Gemini",
    });
  }
});

// Start server
async function start() {
  try {
    await server.listen({ port: PORT, host: HOST });
    console.log(`ClipMemo Backend Proxy listening on http://${HOST}:${PORT}`);
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
}

start();
