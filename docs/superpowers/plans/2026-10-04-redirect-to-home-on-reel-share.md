# Plan d'implémentation - Redirection systématique vers l'accueil lors du partage d'un Reel

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rediriger systématiquement l'utilisateur vers l'écran d'accueil (`NotesListScreen`) dès qu'un traitement de Reel Instagram est lancé (via le menu de partage Android ou via saisie manuelle), afin que le dialogue de progression (`ProcessingOverlay`) soit visible même si l'utilisateur se trouve sur l'écran des paramètres (`SettingsScreen`) ou le détail d'une note (`NoteDetailScreen`).

**Architecture:** 
- Déclaration d'un flux d'événements de navigation `navigateToNotesList: SharedFlow<Unit>` dans `ReelNotesViewModel`.
- Émission de cet événement dès l'appel à `processSharedUrl` (avant le traitement asynchrone).
- Écoute dans `MainActivity.kt` via `LaunchedEffect` sur `navigateToNotesList` et sur `isProcessing` pour exécuter `navController.navigate(Screen.NotesList.route)` avec `popUpTo(Screen.NotesList.route) { inclusive = false }` et `launchSingleTop = true`.
- Maintien du comportement post-traitement (`newlyCreatedNoteId` ouvre les détails de la note après la fin du traitement).

**Tech Stack:** Kotlin, Jetpack Compose, Jetpack Navigation Compose, Kotlin Coroutines & StateFlow/SharedFlow, JUnit 4.

**Spec:** docs/superpowers/plans/2026-10-04-redirect-to-home-on-reel-share.md

## Global Constraints

- Backwards compatibility: Ne pas altérer la navigation existante vers `NoteDetailScreen` une fois le traitement terminé (`newlyCreatedNoteId`).
- Pas de régression d'UI : `launchSingleTop = true` pour éviter tout clignotement ou rechargement si l'utilisateur est déjà sur `NotesListScreen`.
- Isolation : Le ViewModel ne doit pas référencer directement `NavController` (respect de l'architecture MVVM / Compose unie-directionnelle).
- Tous les tests unitaires existants et nouveaux doivent passer (`./gradlew testDebugUnitTest`).

---

## User Review Required

> [!NOTE]
> Lorsqu'un Reel est partagé alors que l'utilisateur lit une note existante (`NoteDetailScreen`) ou consulte les paramètres (`SettingsScreen`), l'écran en cours sera dépilé pour afficher immédiatement l'écran d'accueil avec l'overlay de chargement. Dès que l'analyse IA du nouveau Reel est achevée, l'application ouvrira automatiquement la nouvelle note créée (avec l'interstitiel publicitaire comme prévu).

---

## Proposed Changes

### ViewModel Layer

#### [MODIFY] `app/src/main/java/com/danstudios/reelnotes/ui/viewmodel/ReelNotesViewModel.kt`

- Ajouter un `MutableSharedFlow<Unit>` :
  ```kotlin
  private val _navigateToNotesList = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val navigateToNotesList: SharedFlow<Unit> = _navigateToNotesList.asSharedFlow()
  ```
- Dans `processSharedUrl(sharedText, manualCaption, context, onFinished)` :
  - Émettre `_navigateToNotesList.tryEmit(Unit)` dès le début (si `sharedText.isNotBlank()`).
  - Mettre à jour `_isProcessing.value = true`.

---

### UI / Navigation Layer

#### [MODIFY] `app/src/main/java/com/danstudios/reelnotes/MainActivity.kt`

- Ajouter un `LaunchedEffect` écoutant à la fois `viewModel.navigateToNotesList` et `viewModel.isProcessing` pour rediriger vers `Screen.NotesList.route` :
  ```kotlin
  // Automatically navigate back to notes list when reel processing starts
  LaunchedEffect(Unit) {
      viewModel.navigateToNotesList.collectLatest {
          navController.navigate(Screen.NotesList.route) {
              popUpTo(Screen.NotesList.route) {
                  inclusive = false
              }
              launchSingleTop = true
          }
      }
  }

  LaunchedEffect(Unit) {
      viewModel.isProcessing.collectLatest { isProcessing ->
          if (isProcessing) {
              navController.navigate(Screen.NotesList.route) {
                  popUpTo(Screen.NotesList.route) {
                      inclusive = false
                  }
                  launchSingleTop = true
              }
          }
      }
  }
  ```

---

### Testing Layer

#### [NEW] `app/src/test/java/com/danstudios/reelnotes/ui/viewmodel/ReelNotesViewModelNavigationTest.kt`

- Test unitaire vérifiant que l'appel à `processSharedUrl` émet bien l'événement `navigateToNotesList` et passe `isProcessing` à `true`.
- Test unitaire vérifiant qu'une URL vide ou blanche n'émet pas l'événement de redirection.

---

## Plan Tasks

### Task 1: Navigation Event in ViewModel & Unit Tests

**Files:**
- Create: `app/src/test/java/com/danstudios/reelnotes/ui/viewmodel/ReelNotesViewModelNavigationTest.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/viewmodel/ReelNotesViewModel.kt`

- [ ] **Step 1: Write the failing unit tests for `navigateToNotesList` in `ReelNotesViewModelNavigationTest.kt`**
- [ ] **Step 2: Run test to verify failure (RED)**
  - Command: `./gradlew testDebugUnitTest --tests "*.ReelNotesViewModelNavigationTest"`
- [ ] **Step 3: Implement `_navigateToNotesList` in `ReelNotesViewModel.kt`**
- [ ] **Step 4: Run test to verify pass (GREEN)**
  - Command: `./gradlew testDebugUnitTest --tests "*.ReelNotesViewModelNavigationTest"`
- [ ] **Step 5: Commit changes**
  - Command: `git commit -m "feat(viewmodel): emit navigateToNotesList when processing shared reel"`

### Task 2: Connect Navigation in MainActivity & Verification on Emulator

**Files:**
- Modify: `app/src/main/java/com/danstudios/reelnotes/MainActivity.kt`

- [ ] **Step 1: Add `LaunchedEffect` redirection in `MainActivity.kt`**
- [ ] **Step 2: Run all unit tests to ensure no regressions**
  - Command: `./gradlew testDebugUnitTest`
- [ ] **Step 3: Build & Install debug APK on running emulator**
  - Command: `./gradlew installDebug`
- [ ] **Step 4: Manual verification on emulator**
  - Ouvrir l'application sur l'écran Settings (`Screen.Settings.route`).
  - Envoyer un intent de partage `ACTION_SEND` avec une URL Instagram Reel via `adb shell am start -a android.intent.action.SEND -t text/plain -e android.intent.extra.TEXT "https://www.instagram.com/reel/C_JGaVLuiU0" com.danstudios.clipmemo`.
  - Capturer l'écran de l'émulateur pour vérifier que l'application s'est immédiatement repositionnée sur l'écran principal avec le dialog de chargement `ProcessingOverlay` visible.
- [ ] **Step 5: Commit changes**
  - Command: `git commit -m "feat(ui): redirect to main screen automatically on reel share"`

---

## Verification Plan

### Automated Tests
```bash
./gradlew testDebugUnitTest
```

### Manual Verification
1. Lancer l'app ClipMemo sur l'émulateur Android.
2. Naviguer manuellement sur l'écran des Paramètres (Settings).
3. Simuler le partage d'un reel Instagram :
   ```bash
   adb shell am start -a android.intent.action.SEND -t text/plain -e android.intent.extra.TEXT "https://www.instagram.com/reel/C_JGaVLuiU0" com.danstudios.clipmemo
   ```
4. Constater que l'écran bascule immédiatement sur l'écran principal et que le loader `ProcessingOverlay` s'affiche avec le texte de progression.
5. Vérifier que la note s'ouvre à la fin du traitement.
