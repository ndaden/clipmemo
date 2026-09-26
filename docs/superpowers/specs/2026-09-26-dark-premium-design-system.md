# Spécification de Conception : Design System "Dark Premium & Modern Glass"

**Date :** 2026-09-26  
**Auteur :** Antigravity & User  
**Statut :** Validé par l'utilisateur  
**Branche :** `main`  

---

## 1. Vision & Objectifs

ReelNotes possède un moteur d'extraction natif performant (Room, WebView Chromium avec interception de flux média, multimodale Gemini). L'objectif de cette refonte est d'élever l'expérience utilisateur et l'esthétique de l'application au niveau des standards visuels les plus modernes de 2026, inspirés d'outils comme **Linear** et **Raycast** :

* **Esthétique "Dark Premium"** : Fond noir OLED profond (`#0A0B0E`), surfaces contrastées subtiles avec bordures lumineuses fines (`#232733`), éliminant l'aspect grisâtre par défaut de Material 3.
* **Effet "Modern Glass"** : Surfaces semi-transparentes, barres suspendues avec effet de flou (*frosted glass*), rayons de courbure doux (`20.dp` et `28.dp`).
* **Cartes de notes riches** : Intégration de la miniature du Reel avec fondu dégradé, métadonnées visuelles (temps de prép, nombre d'ingrédients/étapes), badges d'auteurs et catégorie épurés.
* **Écran de détail immersif** : Couverture visuelle en grand format (*Hero Banner*), checklist d'ingrédients interactive avec barre de progression de complétion, timeline d'étapes verticale avec pastilles lumineuses.
* **Navigation flottante** : Remplacement de la TopBar/FloatingActionButton basiques par une barre de navigation inférieure suspendue (*Floating Bottom Navigation*) intégrant l'accès aux favoris, aux réglages et un bouton central "+" lumineux.

---

## 2. Fondations du Design System

### 2.1 Palette de Couleurs

```kotlin
// Fond OLED et Surfaces Sombres
val DarkBackground = Color(0xFF0A0B0E)      // Noir profond principal
val DarkSurface = Color(0xFF14161D)         // Cartes et conteneurs
val DarkSurfaceElevated = Color(0xFF1C1F2B) // Surfaces au second niveau
val DarkBorder = Color(0xFF232733)          // Bordures ultra-fines 1.dp
val DarkBorderHover = Color(0xFF383D52)     // Bordures d'état actif

// Accents Néon & Identité
val NeonViolet = Color(0xFF8B5CF6)          // Accent principal (Linear vibe)
val NeonVioletLight = Color(0xFFA78BFA)     // Teinte d'interaction / icônes
val NeonVioletDark = Color(0xFF6D28D9)      // Dégradés / ombres lumineuses
val EmeraldSuccess = Color(0xFF10B981)      // Ingrédients cochés, succès
val EmeraldLight = Color(0xFF34D399)        // Accent vert lumineux
val CyanAccent = Color(0xFF06B6D4)          // Tags, filtres informatifs
val GoldStar = Color(0xFFFBBF24)            // Étoiles de favoris

// Textes & Typographie
val TextPrimary = Color(0xFFF8FAFC)         // Blanc pur haute lisibilité
val TextSecondary = Color(0xFF94A3B8)       // Gris argenté corps de texte
val TextTertiary = Color(0xFF64748B)        // Métadonnées discrètes
val TextPlaceholder = Color(0xFF475569)     // Placeholders
```

### 2.2 Formes & Géométrie
* **Cartes de notes (`NoteCard`)** : Coins arrondis à `20.dp`, bordure de `1.dp` en `DarkBorder`.
* **Chips & Badges de catégorie** : Coins arrondis à `12.dp` en pilule, padding compact.
* **Barre de navigation flottante (`FloatingBottomBar`)** : Coins arrondis à `28.dp`, padding horizontal `16.dp`, suspendue à `16.dp` au-dessus de la barre système Android.

---

## 3. Spécifications des Composants & Écrans

### 3.1 Cartes de Notes (`NoteCard.kt`)
* **Miniature (Hero Thumbnail)** :
  * Si `thumbnailUrl` est non-nulle, chargement via `AsyncImage` (Coil) avec `ContentScale.Crop` sur une hauteur de `150.dp`.
  * Dégradé noir vertical en surimpression (`Brush.verticalGradient(transparent -> DarkSurface)`) assurant une transition fluide vers le texte.
* **Badges flottants** :
  * Badge de catégorie avec icône emoji et fond translucide `DarkSurface.copy(alpha = 0.85f)` avec bordure fine.
  * Bouton favori en verre circulaire avec micro-interaction de changement d'icône (`GoldStar`).
* **Section Contenu** :
  * Titre gras en `TextPrimary` (max 2 lignes, `TextOverflow.Ellipsis`).
  * Auteur du Reel (`@author`) mis en avant en `NeonVioletLight`.
  * Résumé court en `TextSecondary`.
* **Puces de métadonnées** :
  * Badges discrets : `⏱ 15 min`, `🍳 8 ingrédients`, `🔢 4 étapes`.

### 3.2 Écran de Liste (`NotesListScreen.kt`)
* **En-tête & Recherche** :
  * Titre d'application stylisé avec lueur subtile.
  * Champ de recherche en forme de pilule foncée intégrée avec icône loupe violette et bouton de suppression animé.
* **Barre de filtres par catégorie (`CategoryChipRow.kt`)** :
  * Puces de catégorie affichant le nombre de notes associées (ex : `Tout (12)`, `🍳 Recettes (6)`).
  * Lueur violette néon sur le chip sélectionné (`NeonViolet.copy(alpha = 0.2f)` et bordure `NeonViolet`).
* **Empty State moderne** :
  * Illustration / icône moderne avec lueur violette douce invitant à partager un Reel Instagram ou à appuyer sur le bouton "+".
* **Padding inférieur** :
  * `contentPadding` du `LazyColumn` configuré à `bottom = 100.dp` pour que le dernier élément de la liste ne soit jamais masqué par la barre de navigation flottante.

### 3.3 Barre de Navigation Flottante (`FloatingBottomBar.kt`)
* **Positionnement** : Suspendue au bas de l'écran, centrée horizontalement avec marge latérale de `16.dp` et marge basse de `16.dp`.
* **Apparence** : Verre dépoli sombre (`DarkSurface.copy(alpha = 0.92f)`), bordure `1.dp` en `DarkBorder`, coins `28.dp`.
* **Éléments** :
  * Onglet **Notes** : Icône grille/liste + label discret.
  * Onglet **Favoris** : Étoile avec pastille de comptage si des favoris existent.
  * Bouton central **"+" Flottant** : Cercle de `48.dp` avec dégradé `NeonViolet -> CyanAccent`, ombre portée lumineuse, déclenchant le dialogue d'ajout manuel.
  * Onglet **Paramètres** : Icône roue crantée.

### 3.4 Écran de Détail (`NoteDetailScreen.kt`)
* **Header Visuel Immersif** :
  * Image plein écran en tête de vue (hauteur `240.dp`) si disponible, s'estompant dans le fond `DarkBackground`.
  * Boutons d'action circulaires flottants en verre dépoli (`40.dp`) : Retour, Favori, Copie Markdown, Partage, Suppression.
* **Titre & Auteur** :
  * Titre `HeadlineMedium` en blanc pur `#F8FAFC`.
  * Badge auteur cliquable avec icône Instagram.
* **Bouton CTA Principal** :
  * Bouton plein format **"Regarder le Reel sur Instagram"** avec dégradé violet néon et icône de lecture.
* **Checklist d'Ingrédients Interactive (`RecipeChecklist.kt`)** :
  * Barre de progression animée avec pourcentage de complétion (`3 / 8 ingrédients prêts • 37%`).
  * Cases à cocher personnalisées avec coche émeraude `#10B981` et animation d'atténuation.
* **Timeline d'Étapes (`StepList.kt`)** :
  * Ligne verticale continue reliant chaque étape.
  * Pastille numérotée lumineuse avec possibilité de marquer une étape comme réalisée.
* **Callout Boxes Stylisées** :
  * Cartes pour "Astuces du chef" (icône ampoule avec liseré jaune) et "Points clés" (icône étoile avec liseré cyan).

### 3.5 Écran des Réglages (`SettingsScreen.kt`)
* **Cartes regroupées par thème** :
  * Carte Clé API Google Gemini avec bouton de test interactif avec statut vert/rouge immédiat.
  * Carte de sélection de langue (Français / Anglais) avec boutons radio modernes.
  * Carte de données de démonstration avec bouton "Recharger les exemples".

---

## 4. Architecture des Fichiers & Modifications

* `app/src/main/java/com/danstudios/reelnotes/ui/theme/Color.kt` : Nouvelles teintes Dark OLED, accents néon et bordures.
* `app/src/main/java/com/danstudios/reelnotes/ui/theme/Theme.kt` : Palette `DarkColorScheme` premium mise à jour.
* `app/src/main/java/com/danstudios/reelnotes/ui/components/FloatingBottomBar.kt` : **[NOUVEAU]** Barre de navigation flottante en verre dépoli.
* `app/src/main/java/com/danstudios/reelnotes/ui/components/NoteCard.kt` : Intégration de la miniature, dégradé, badges et métadonnées.
* `app/src/main/java/com/danstudios/reelnotes/ui/components/CategoryChipRow.kt` : Style pilule moderne avec compteurs de notes.
* `app/src/main/java/com/danstudios/reelnotes/ui/components/RecipeChecklist.kt` : Barre de progression animée et cases à cocher émeraude.
* `app/src/main/java/com/danstudios/reelnotes/ui/components/StepList.kt` : Timeline verticale avec pastilles numérotées.
* `app/src/main/java/com/danstudios/reelnotes/ui/screens/NotesListScreen.kt` : Intégration de la recherche pilule, du FloatingBottomBar, et layout moderne.
* `app/src/main/java/com/danstudios/reelnotes/ui/screens/NoteDetailScreen.kt` : Header Hero grand format, boutons circulaires en verre, CTA Instagram.
* `app/src/main/java/com/danstudios/reelnotes/ui/screens/SettingsScreen.kt` : Style de cartes Linear avec bordures fines et retour de test visuel.

---

## 5. Stratégie de Validation & Non-Régression

1. **Compilation & Tests Unitaires** : Exécution de `./gradlew testDebugUnitTest` pour s'assurer qu'aucune logique métier (Room, extracteur, pipeline IA) n'est altérée.
2. **Build APK** : Vérification du build avec `./gradlew assembleDebug`.
3. **Validation Visuelle sur Émulateur** : Lancement de l'application sur l'émulateur Pixel 10 et capture d'écran pour valider les cartes, la barre flottante et l'écran de détail.
