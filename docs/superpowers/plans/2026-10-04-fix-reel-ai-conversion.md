# Fix Instagram Reel to Note Conversion Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the Instagram Reel note generation failure (which caused empty ingredients/steps and fallback to basic offline notes for `https://www.instagram.com/reel/C_JGaVLuiU0`) by upgrading the backend proxy to active Gemini 3.x models, hardening OpenGraph metadata extraction, and verifying the end-to-end flow.

**Architecture:** 
1. The backend proxy (`backend/src/index.ts`) forwards AI summarization requests to Google Gemini via Google Generative Language API. It will be updated from retired `gemini-2.5-flash`/`gemini-2.0-flash`/`gemini-1.5-flash` models (which return HTTP 404) to active models: `gemini-3.5-flash` (primary), `gemini-3.8-flash` (secondary), and `gemini-3.5-flash-lite` (tertiary).
2. The Android client's `InstagramMetadataFetcher.kt` will use a crawler user-agent (`facebookexternalhit`) on HTTP fallback to reliably extract complete OpenGraph tags (`og:description`, `og:image`, `og:url` author) if WebView extraction fails or runs in headless mode.
3. Once deployed to k3s via GitHub Actions, the Android app on the emulator will re-process the Reel, and the note detail screen will show complete ingredients, steps, and tips.

**Tech Stack:** Node.js / TypeScript, Fastify, Google Gemini API (`gemini-3.5-flash`), Kotlin, Jetpack Compose, Kubernetes (k3s), GitHub Actions CI/CD.

**Spec:** Bug investigation on Reel `https://www.instagram.com/reel/C_JGaVLuiU0`. Root cause identified: Gemini API returns HTTP 404 on `gemini-2.5-flash` / `gemini-1.5-flash` ("model is no longer available to new users"), causing backend proxy to return HTTP 500, forcing app to fall back to basic offline note extraction without ingredients or steps.

## Global Constraints

- Never expose `GEMINI_API_KEY` in the Android APK or client code.
- Backend API authentication (`X-App-Key`) must be preserved.
- Keep backwards compatibility with existing note database entities.
- All unit tests must pass before deployment.

---

### Task 1: Upgrade Gemini Models in Backend Proxy and Kubernetes Manifests

**Files:**
- Modify: `backend/src/index.ts:13-16, 96-98`
- Modify: `backend/.env:13-15`
- Modify: `backend/.env.example:13-15`
- Modify: `backend/k8s/03-api-deployment.yaml:60`
- Modify: `.github/workflows/deploy.yml:74-75`

**Interfaces:**
- Consumes: Google Generative AI API v1beta (`generateContent`)
- Produces: POST `/api/summarize` and POST `/api/summarize-multimodal` returning structured JSON

- [ ] **Step 1: Update model constants and fallbacks in `backend/src/index.ts`**

Change default model and fallback chain from `gemini-2.5-flash` / `gemini-2.0-flash` / `gemini-1.5-flash` to `gemini-3.5-flash`, `gemini-3.8-flash`, and `gemini-3.5-flash-lite`:

```typescript
const DEFAULT_MODEL = process.env.GEMINI_MODEL || "gemini-3.5-flash";
const FALLBACK_MODEL = process.env.GEMINI_FALLBACK_MODEL || "gemini-3.8-flash";

// In callGeminiApi:
const models = [DEFAULT_MODEL, FALLBACK_MODEL, "gemini-3.5-flash-lite"];
```

- [ ] **Step 2: Update environment files and Kubernetes deployment memory limit**

Update `backend/.env` and `backend/.env.example`:
```env
GEMINI_MODEL=gemini-3.5-flash
GEMINI_FALLBACK_MODEL=gemini-3.8-flash
```

In `backend/k8s/03-api-deployment.yaml`, increase memory limit from `256Mi` to `512Mi` to provide headroom for base64 video buffers during multimodal analysis:
```yaml
            limits:
              cpu: "500m"
              memory: "512Mi"
```

In `.github/workflows/deploy.yml`, update default fallback values in secret creation:
```yaml
            --from-literal=GEMINI_MODEL="${{ secrets.GEMINI_MODEL || 'gemini-3.5-flash' }}" \
            --from-literal=GEMINI_FALLBACK_MODEL="${{ secrets.GEMINI_FALLBACK_MODEL || 'gemini-3.8-flash' }}" \
```

- [ ] **Step 3: Compile and test backend locally**

Run: `cd backend && npm run build`
Expected: Build succeeds with zero TypeScript errors.

- [ ] **Step 4: Verify local backend summarization test**

Run a script calling `/api/summarize` with `gemini-3.5-flash` to ensure structured JSON output parses cleanly.

- [ ] **Step 5: Commit backend changes**

```bash
git add backend/src/index.ts backend/.env backend/.env.example backend/k8s/03-api-deployment.yaml .github/workflows/deploy.yml
git commit -m "fix(backend): upgrade Gemini models to gemini-3.5-flash and increase memory limit"
```

---

### Task 2: Enhance Instagram Metadata & Fallback HTTP Scraper in Android App

**Files:**
- Modify: `app/src/main/java/com/danstudios/reelnotes/data/network/InstagramMetadataFetcher.kt:127-140, 177-223`
- Test: `app/src/test/java/com/danstudios/reelnotes/data/network/InstagramMetadataFetcherTest.kt`

**Interfaces:**
- Consumes: Instagram Reel HTML response
- Produces: `FetchedReelMetadata` with `caption`, `author`, `thumbnailUrl`, `title`

- [ ] **Step 1: Write failing unit test for OpenGraph HTML parsing**

Add a test in `InstagramMetadataFetcherTest.kt` testing SSR OpenGraph HTML (like that of `C_JGaVLuiU0`):
```kotlin
@Test
fun `parseHtml extracts caption and author from OpenGraph tags with Instagram prefix`() {
    val sampleHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta property="og:url" content="https://www.instagram.com/seizemay/reel/C_JGaVLuiU0/" />
            <meta property="og:description" content="45K likes, 107 comments - seizemay on August 26, 2024: &quot;Un Burger Maison qui n'a rien à envier au Burger de Fastfood !! 🤤🍔 Ingrédients : 2 pains burger, 300g de viande...&quot;." />
            <meta property="og:image" content="https://scontent.cdn/burger.jpg" />
            <meta property="og:title" content="JORDAN M. on Instagram: &quot;Un Burger Maison...&quot;" />
        </head>
        <body></body>
        </html>
    """.trimIndent()

    val meta = InstagramMetadataFetcher.parseHtml(sampleHtml)
    assertEquals("@seizemay", meta.author)
    assertEquals("https://scontent.cdn/burger.jpg", meta.thumbnailUrl)
    assertFalse(meta.caption.contains("45K likes"))
    assertTrue(meta.caption.contains("Un Burger Maison"))
    assertTrue(meta.caption.contains("2 pains burger"))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "*.InstagramMetadataFetcherTest"`
Expected: FAIL (author null or prefix not stripped).

- [ ] **Step 3: Update `InstagramMetadataFetcher.kt`**

1. In HTTP fallback (around line 130), change User-Agent to:
```kotlin
.header("User-Agent", "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)")
```
2. In `parseHtml`:
- Extract author from `og:url`:
```kotlin
val ogUrlRegex = Regex("""property=["']og:url["']\s+content=["']https://www\.instagram\.com/([^/]+)/reel/""")
val ogAuthor = ogUrlRegex.find(html)?.groupValues?.get(1)?.let { "@$it" }
```
- Robust `og:description` matching attribute in any order:
```kotlin
val ogDescRegex = Regex("""<meta\s+(?:property=["']og:description["']\s+content=["'](.*?)["']|content=["'](.*?)["']\s+property=["']og:description["'])""", RegexOption.DOT_MATCHES_ALL)
val rawOgDesc = ogDescRegex.find(html)?.let { it.groupValues[1].ifEmpty { it.groupValues[2] } } ?: ""
```
- Strip boilerplate Instagram prefix:
```kotlin
val cleanCaption = caption
    .replace(Regex("""^[0-9.,]+[KkMm]?\s+likes,\s+[0-9.,]+[KkMm]?\s+comments\s+-\s+[A-Za-z0-9_.]+\s+on\s+[^:]+:\s*["“]"""), "")
    .replace(Regex("""["”]\.?\s*$"""), "")
```

- [ ] **Step 4: Run unit tests to verify they pass**

Run: `./gradlew testDebugUnitTest`
Expected: ALL PASS.

- [ ] **Step 5: Commit client changes**

```bash
git add app/src/main/java/com/danstudios/reelnotes/data/network/InstagramMetadataFetcher.kt app/src/test/java/com/danstudios/reelnotes/data/network/InstagramMetadataFetcherTest.kt
git commit -m "fix(app): improve Instagram OpenGraph metadata extraction and user-agent"
```

---

### Task 3: Deploy Backend to K3s and Verify Conversion on Emulator

**Files:**
- GitHub Actions: `.github/workflows/deploy.yml`
- Android App: `app/build.gradle.kts` / APK installation

- [ ] **Step 1: Push changes to main branch**

Push commit to trigger GitHub Actions `deploy.yml`.

- [ ] **Step 2: Monitor deployment and wait for rollout on k3s**

Check GitHub Actions run status using `gh run watch` or curl `https://clipmemo.dnabil.ovh/health`.
Verify backend summarization live with curl:
```bash
curl -s -X POST "https://clipmemo.dnabil.ovh/api/summarize" \
  -H "Content-Type: application/json" \
  -H "X-App-Key: 19b2f25fdd8360544863c8755b9d33ef9176699c5c092a38dad693519b760bba" \
  -d '{"caption": "Un Burger Maison qui n’a rien à envier au Burger de Fastfood !! 🤤🍔 Ingrédients : 2 pains burger, 300g viande, 4 tranches cheddar...", "preferredLanguage": "fr"}'
```
Expected: 200 OK with valid JSON structured note.

- [ ] **Step 3: Reinstall/Update debug app on emulator-5554**

Run: `./gradlew installDebug`

- [ ] **Step 4: Re-process Reel `https://www.instagram.com/reel/C_JGaVLuiU0`**

Trigger intent:
```bash
/Users/nabil/Library/Android/sdk/platform-tools/adb shell am start -a android.intent.action.SEND -t "text/plain" -e android.intent.extra.TEXT "https://www.instagram.com/reel/C_JGaVLuiU0" -n com.danstudios.clipmemo.debug/com.danstudios.reelnotes.MainActivity
```

- [ ] **Step 5: Capture screenshot and verify note content**

Dismiss ad and verify that the Note Detail screen now contains:
- Recipe category badge
- Full list of Ingredients with quantities (pains, viande hachée, cheddar, etc.)
- Step-by-step preparation instructions
- Tips & Key takeaways
