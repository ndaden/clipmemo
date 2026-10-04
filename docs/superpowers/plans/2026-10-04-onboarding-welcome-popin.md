# Plan d'implémentation - Popin d'accueil au premier lancement (FR / EN)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Afficher un message de bienvenue (popin / dialogue modal d'onboarding) au tout premier lancement de l'application afin d'expliquer clairement à l'utilisateur le fonctionnement de ClipMemo (partage de Reels et extraction automatique de fiches) ainsi que le caractère optionnel de la connexion à un compte Instagram (pour débloquer les Reels privés ou avec restriction d'âge). Ce message doit être disponible en français et en anglais et ne plus s'afficher une fois validé.

**Architecture:**
- **Persistance (`PreferencesManager`)** : Ajout d'une préférence booléenne `hasSeenOnboarding: Boolean` (par défaut `false`).
- **ViewModel (`ReelNotesViewModel`)** : Exposition de `showOnboarding: StateFlow<Boolean>` et de la méthode `dismissOnboarding()`.
- **Ressources bilingues (`res/values/strings.xml` & `res/values-en/strings.xml`)** : Ajout de l'ensemble des textes d'accueil, explications et boutons en français et en anglais.
- **Composant UI (`OnboardingDialog.kt`)** : Dialogue Compose élégant (thème sombre, bordures néon, icônes illustratives, carte explicative en 2 étapes clés + boutons d'action).
- **Intégration UI (`NotesListScreen.kt`)** : Affichage conditionnel de `OnboardingDialog` au lancement si `showOnboarding` est vrai. Possibilité de fermer la popin ("C'est parti !") ou d'ouvrir directement la connexion Instagram ("Connecter Instagram").

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, SharedPreferences, StateFlow, JUnit 4.

**Spec:** docs/superpowers/plans/2026-10-04-onboarding-welcome-popin.md

---

## Global Constraints

- Ne pas réafficher la popin lors des lancements ultérieurs si l'utilisateur l'a déjà validée ou fermée (`hasSeenOnboarding = true`).
- Les textes doivent être rigoureusement disponibles en français (`values/strings.xml`) et en anglais (`values-en/strings.xml`).
- Le style doit respecter la charte graphique de ClipMemo : `DarkSurface`, `DarkBorder`, `NeonViolet`, `TextPrimary`, `TextSecondary`, `RoundedCornerShape(24.dp)`.
- Aucun crash ou régression de navigation. Tous les tests unitaires existants et nouveaux doivent passer.

---

## User Review Required

> [!NOTE]
> La popin comportera 2 boutons d'action :
> 1. **Bouton principal ("C'est parti !" / "Get Started")** : Enregistre `hasSeenOnboarding = true` et ferme la boîte de dialogue pour accéder directement à la liste des notes.
> 2. **Bouton secondaire ("Connecter Instagram" / "Connect Instagram")** : Enregistre `hasSeenOnboarding = true`, ferme la boîte de dialogue et ouvre immédiatement le dialogue de connexion Instagram (WebView sécurisée).

---

## Proposed Changes

### Data & Preferences Layer

#### [MODIFY] `app/src/main/java/com/danstudios/reelnotes/data/local/PreferencesManager.kt`

- Ajouter la propriété `hasSeenOnboarding` :
  ```kotlin
  open var hasSeenOnboarding: Boolean
      get() = prefs?.getBoolean(KEY_HAS_SEEN_ONBOARDING, false) ?: false
      set(value) {
          prefs?.edit()?.putBoolean(KEY_HAS_SEEN_ONBOARDING, value)?.apply()
      }
  ```
- Ajouter dans le companion object :
  ```kotlin
  private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"
  ```

---

### ViewModel Layer

#### [MODIFY] `app/src/main/java/com/danstudios/reelnotes/ui/viewmodel/ReelNotesViewModel.kt`

- Ajouter l'état réactif et la fonction de fermeture :
  ```kotlin
  private val _showOnboarding = MutableStateFlow(!preferences.hasSeenOnboarding)
  val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

  fun dismissOnboarding() {
      preferences.hasSeenOnboarding = true
      _showOnboarding.value = false
  }
  ```

---

### Localization Layer

#### [MODIFY] `app/src/main/res/values/strings.xml` (Français)

```xml
    <!-- Onboarding / Welcome Dialog -->
    <string name="onboarding_title">Bienvenue sur ClipMemo</string>
    <string name="onboarding_subtitle">Transformez vos Reels Instagram en fiches claires et organisées en un instant.</string>
    <string name="onboarding_step_share_title">Partagez &amp; Capturez</string>
    <string name="onboarding_step_share_desc">Partagez un Reel Instagram directement vers ClipMemo ou collez son lien. L\'IA extrait automatiquement les étapes, ingrédients ou points clés.</string>
    <string name="onboarding_step_insta_title">Connexion Instagram (Optionnelle)</string>
    <string name="onboarding_step_insta_desc">L\'application fonctionne sans compte. Vous pouvez toutefois connecter votre compte Instagram dans les Paramètres pour analyser les Reels privés ou avec restriction d\'âge.</string>
    <string name="onboarding_btn_start">C\'est parti !</string>
    <string name="onboarding_btn_connect_insta">Connecter Instagram</string>
```

#### [MODIFY] `app/src/main/res/values-en/strings.xml` (English)

```xml
    <!-- Onboarding / Welcome Dialog -->
    <string name="onboarding_title">Welcome to ClipMemo</string>
    <string name="onboarding_subtitle">Turn Instagram Reels into clean, organized notes in seconds.</string>
    <string name="onboarding_step_share_title">Share &amp; Capture</string>
    <string name="onboarding_step_share_desc">Share an Instagram Reel straight to ClipMemo or paste its link. AI automatically extracts steps, ingredients, or key takeaways.</string>
    <string name="onboarding_step_insta_title">Instagram Login (Optional)</string>
    <string name="onboarding_step_insta_desc">The app works without an account. You can optionally link your Instagram account in Settings to analyze private or age-restricted Reels.</string>
    <string name="onboarding_btn_start">Get Started</string>
    <string name="onboarding_btn_connect_insta">Connect Instagram</string>
```

---

### UI Layer

#### [NEW] `app/src/main/java/com/danstudios/reelnotes/ui/components/OnboardingDialog.kt`

- Composant `OnboardingDialog(onDismiss: () -> Unit, onConnectInstagram: () -> Unit)` :
  - `AlertDialog` avec design ClipMemo (`DarkSurface`, bordure `DarkBorder`, coins arrondis `24.dp`).
  - En-tête avec icône `AutoAwesome` dans un cadre lumineux `NeonViolet`.
  - Deux blocs explicatifs visuels avec icônes distinctes (`AutoStories` ou `Share` pour le fonctionnement, `LockOpen` pour Instagram optionnel).
  - Bouton primaire "C'est parti !" avec fond `NeonViolet`.
  - Bouton secondaire "Connecter Instagram" avec texte discret.

#### [MODIFY] `app/src/main/java/com/danstudios/reelnotes/ui/screens/NotesListScreen.kt`

- Observer `showOnboarding by viewModel.showOnboarding.collectAsState()`.
- Gérer l'état `showInstagramLoginDialog` pour ouvrir le dialogue si l'utilisateur choisit "Connecter Instagram".
- Afficher `OnboardingDialog` si `showOnboarding` est vrai.

---

### Testing Layer

#### [MODIFY] `app/src/test/java/com/danstudios/reelnotes/ui/viewmodel/ReelNotesViewModelNavigationTest.kt`

- Ajouter les tests unitaires :
  1. `testOnboardingShownOnFirstLaunch()` : vérifie que `showOnboarding == true` quand `hasSeenOnboarding == false`.
  2. `testDismissOnboardingPersistsPreference()` : vérifie que `dismissOnboarding()` passe `hasSeenOnboarding` à `true` et `showOnboarding` à `false`.
  3. `testOnboardingNotShownWhenAlreadySeen()` : vérifie que `showOnboarding == false` quand la préférence est déjà à `true`.

---

## Plan Tasks

### Task 1: Preferences & ViewModel Logic with Unit Tests (TDD)

**Files:**
- Modify: `app/src/main/java/com/danstudios/reelnotes/data/local/PreferencesManager.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/viewmodel/ReelNotesViewModel.kt`
- Modify: `app/src/test/java/com/danstudios/reelnotes/ui/viewmodel/ReelNotesViewModelNavigationTest.kt`

- [x] **Step 1: Write failing unit tests for onboarding state in `ReelNotesViewModelNavigationTest.kt`**
- [x] **Step 2: Run tests to verify failure (RED)**
  - Command: `./gradlew testDebugUnitTest --tests "*.ReelNotesViewModelNavigationTest"`
- [x] **Step 3: Implement `hasSeenOnboarding` in `PreferencesManager.kt` & `showOnboarding` / `dismissOnboarding()` in `ReelNotesViewModel.kt`**
- [x] **Step 4: Run tests to verify pass (GREEN)**
  - Command: `./gradlew testDebugUnitTest --tests "*.ReelNotesViewModelNavigationTest"`
- [x] **Step 5: Commit changes**
  - Command: `git commit -m "feat(preferences): add hasSeenOnboarding preference and ViewModel state"`

### Task 2: Bilingual String Resources (FR & EN)

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

- [x] **Step 1: Add French onboarding string resources in `res/values/strings.xml`**
- [x] **Step 2: Add English onboarding string resources in `res/values-en/strings.xml`**
- [x] **Step 3: Verify resources build without errors**
  - Command: `./gradlew compileDebugKotlin`
- [x] **Step 4: Commit changes**
  - Command: `git commit -m "feat(i18n): add onboarding string resources in French and English"`

### Task 3: OnboardingDialog Component & Integration in NotesListScreen

**Files:**
- Create: `app/src/main/java/com/danstudios/reelnotes/ui/components/OnboardingDialog.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/screens/NotesListScreen.kt`

- [x] **Step 1: Create `OnboardingDialog.kt` composable with themed cards, icons, and action buttons**
- [x] **Step 2: Integrate `OnboardingDialog` in `NotesListScreen.kt` with Instagram login flow support**
- [x] **Step 3: Run all unit tests**
  - Command: `./gradlew testDebugUnitTest`
- [x] **Step 4: Commit changes**
  - Command: `git commit -m "feat(ui): create OnboardingDialog component and integrate in NotesListScreen"`

### Task 4: Verification on Android Emulator

- [x] **Step 1: Build & install debug APK on emulator**
  - Command: `./gradlew installDebug`
- [x] **Step 2: Clear app data on emulator to simulate fresh first launch**
  - Command: `/Users/nabil/Library/Android/sdk/platform-tools/adb shell pm clear com.danstudios.clipmemo.debug`
- [x] **Step 3: Launch app and take screenshot of the Onboarding popin**
  - Verify layout, title, explanation bullet points, and buttons.
- [x] **Step 4: Test dismissing the popin and relaunching app to ensure it does not reappear**
- [x] **Step 5: Test language switching (French & English) to verify localization**
- [x] **Step 6: Update walkthrough artifact and push changes to remote**

---

## Verification Plan

### Automated Tests
```bash
./gradlew testDebugUnitTest
```

### Manual Verification
1. Réinitialiser les données de l'application sur l'émulateur :
   ```bash
   adb shell pm clear com.danstudios.clipmemo.debug
   ```
2. Lancer l'application : la popin d'accueil s'affiche immédiatement.
3. Vérifier les deux sections explicatives (comment fonctionne l'extraction, et la connexion Instagram optionnelle).
4. Cliquer sur "C'est parti !" : la popin se ferme et laisse place à la liste des notes.
5. Fermer et rouvrir l'application : vérifier que la popin ne réapparaît pas.
6. Changer la langue dans les Paramètres (vers l'anglais) et réinitialiser pour vérifier la version anglaise ("Welcome to ClipMemo").
