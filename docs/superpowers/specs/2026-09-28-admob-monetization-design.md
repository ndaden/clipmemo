# Spécification Technique : Monétisation Google AdMob (ClipMemo)

## 1. Contexte & Objectifs

- **Application :** ClipMemo (Package: `com.danstudios.clipmemo`)
- **Objectif Business :** Maximiser la monétisation publicitaire sans bloquer l'expérience utilisateur de manière destructrice, en appliquant un modèle à fort eCPM tout en respectant les exigences et règles strictes de Google AdMob et du Google Play Store.
- **Formats Publicitaires :**
  1. **Bannière Adaptative Ancrée (Anchored Adaptive Banner) :** Ancrée en bas de l'écran principal et de l'écran de détail, occupant 100% de la largeur avec auto-refresh.
  2. **Interstitiel Vidéo Plein Écran (Full-Screen Interstitial) :** Déclenché systématiquement à chaque fin de traitement d'un Reel Instagram (100% de fréquence de déclenchement), préchargé en arrière-plan pour un affichage instantané (0 seconde d'attente).

---

## 2. Identifiants AdMob

### Production (Compte AdMob officiel de l'utilisateur)
- **AdMob App ID :** `ca-app-pub-6318635608591557~6617736972`
- **ID Bloc Bannière :** `ca-app-pub-6318635608591557/4419124059` (`ClipMemo_Banner_Bottom`)
- **ID Bloc Interstitiel :** `ca-app-pub-6318635608591557/1703573212` (`ClipMemo_Interstitial_PostProcess`)

### Test Officiel Google (Utilisé automatiquement en mode `DEBUG`)
- **Sample App ID (Manifest) :** Déclaré avec l'App ID de production dans `AndroidManifest.xml`
- **ID Bloc Bannière Test :** `ca-app-pub-3940256099942544/6300978111`
- **ID Bloc Interstitiel Test :** `ca-app-pub-3940256099942544/1033173712`

> **Note de sécurité :** Google AdMob suspend immédiatement les comptes publiant du trafic de test sur des IDs de production lors du développement. La classe `AdConfig` sélectionne dynamiquement les IDs de test si `BuildConfig.DEBUG == true`, et les IDs de production en `RELEASE`.

---

## 3. Architecture Logicielle & Composants

Les composants publicitaires sont isolés dans le package `com.danstudios.reelnotes.ads`.

```
app/src/main/java/com/danstudios/reelnotes/ads/
├── AdConfig.kt                  // Gestion des IDs Test vs Production
├── AdManager.kt                 // Singleton d'initialisation SDK et de gestion globale
├── InterstitialAdController.kt  // Préchargement, affichage et callbacks d'interstitiels
└── AdaptiveBannerAd.kt          // Composable Jetpack Compose pour la bannière adaptative
```

### 3.1. `AdConfig.kt`
Objet utilitaire fournissant les IDs appropriés :
```kotlin
object AdConfig {
    val bannerAdUnitId: String
        get() = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/6300978111" // Google Sample Banner
        } else {
            "ca-app-pub-6318635608591557/4419124059" // Production
        }

    val interstitialAdUnitId: String
        get() = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712" // Google Sample Interstitial
        } else {
            "ca-app-pub-6318635608591557/1703573212" // Production
        }
}
```

### 3.2. Initialisation SDK (`ReelNotesApp.kt`)
Dans `ReelNotesApp.onCreate()` :
- Lancement asynchrone sur un thread d'arrière-plan via `CoroutineScope(Dispatchers.IO).launch` :
  ```kotlin
  MobileAds.initialize(this) {}
  ```
- Initialisation immédiate du préchargement du premier interstitiel via `InterstitialAdController.preload(this)`.

### 3.3. `InterstitialAdController.kt`
Gère le cycle de vie des annonces interstitielles :
- **Préchargement proactif :** `preload(context: Context)` appelle `InterstitialAd.load()` dès le démarrage.
- **Méthode d'affichage sécurisée :**
  ```kotlin
  fun showInterstitial(activity: Activity, onDismissedOrFailed: () -> Unit)
  ```
  - Si `interstitialAd != null` : attache un `FullScreenContentCallback` avec :
    - `onAdDismissedFullScreenContent` : met à null la référence actuelle, relance immédiatement un `preload()`, puis exécute `onDismissedOrFailed()`.
    - `onAdFailedToShowFullScreenContent` : met à null, relance un `preload()`, et exécute immédiatement `onDismissedOrFailed()`.
    - Déclenche `interstitialAd.show(activity)`.
  - Si `interstitialAd == null` (pas de pub chargée, mode hors ligne) : exécute directement `onDismissedOrFailed()` sans latence et retente un `preload()`.

### 3.4. `AdaptiveBannerAd.kt`
Composant Composable affichant la bannière en bas d'écran :
- Calcul de la taille adaptative selon la largeur de l'écran en `dp` via `AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidth)`.
- Rendu via `AndroidView` avec configuration du `AdView`.
- Gestion des cycles de vie avec `DisposableEffect` :
  - Appel de `adView.destroy()` lors du déchargement du composable pour éviter toute fuite mémoire.
- Rendu harmonieux : encart conteneur `DarkBackground` avec séparateur supérieur discret `DarkBorder`.

---

## 4. Intégration dans les Écrans & Flux

### 4.1. Écran Principal (`NotesListScreen.kt`)
- La bannière `AdaptiveBannerAd` est ancrée tout en bas de l'écran.
- La barre de navigation flottante (`FloatingBottomBar`) est positionnée au-dessus de la bannière.
- Le `LazyColumn` des notes intègre un padding inférieur augmenté (~160.dp) afin que la dernière carte de la liste ne soit jamais occultée par la barre de navigation ou la bannière.

### 4.2. Écran de Détail (`NoteDetailScreen.kt`)
- La bannière `AdaptiveBannerAd` est également ancrée au bas de l'écran.
- Le conteneur scrollable de la note dispose d'un spacer inférieur adéquat pour préserver la lisibilité de la fiche.

### 4.3. Déclenchement de l'Interstitiel Post-Traitement (`MainActivity.kt`)
Lors de la fin d'analyse d'un Reel (soumis via le dialogue `+` ou via le partage `ACTION_SEND` depuis Instagram) :
- `viewModel.newlyCreatedNoteId` émet le `noteId`.
- `MainActivity` intercepte l'émission et déclenche l'affichage :
  ```kotlin
  viewModel.newlyCreatedNoteId.collectLatest { noteId ->
      InterstitialAdController.showInterstitial(this@MainActivity) {
          navController.navigate(Screen.NoteDetail.createRoute(noteId))
      }
  }
  ```
- **Garantie de non-blocage :** Que l'utilisateur regarde la vidéo, la ferme, ou qu'il soit hors-ligne, il atterrit obligatoirement sur sa fiche détaillée.

---

## 5. Modifications du Build & Manifeste

### 5.1. `gradle/libs.versions.toml`
```toml
[versions]
playServicesAds = "23.6.0"

[libraries]
play-services-ads = { group = "com.google.android.gms", name = "play-services-ads", version.ref = "playServicesAds" }
```

### 5.2. `app/build.gradle.kts`
```kotlin
dependencies {
    implementation(libs.play.services.ads)
    ...
}
```

### 5.3. `app/src/main/AndroidManifest.xml`
Dans `<application>` :
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-6318635608591557~6617736972" />
```

---

## 6. Stratégie de Vérification & Tests

1. **Tests Unitaires :**
   - `AdConfigTest` : Validation que le basculement debug/release sélectionne les bons IDs et que les formats d'ID AdMob sont conformes.
   - `InterstitialAdControllerTest` : Validation que le callback de redirection s'exécute systématiquement dans tous les scénarios (annonce nulle, annonce fermée, échec d'affichage).
2. **Tests de Compilation & Intégration :**
   - `./gradlew testDebugUnitTest` : 100% tests unitaires passants.
   - `./gradlew assembleDebug` : Compilation complète de l'application avec le SDK Google Mobile Ads.
