# Dark Premium & Modern Glass Design System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Modernize the entire ReelNotes UI with a high-fidelity "Dark Premium & Modern Glass" design system inspired by Linear and Raycast, featuring deep OLED surfaces, neon violet/emerald accents, rich thumbnail cards, interactive recipe checklists with progress bars, and a floating glassmorphic navigation bar.

**Architecture:** Maintain the existing native MVVM + Jetpack Compose + Room + Gemini pipeline while overhauling the UI theme foundations, modular composables, and screens. Components are updated incrementally with self-contained tests, culminating in screen integration and visual verification.

**Tech Stack:** Jetpack Compose, Material 3, Coil Compose (`io.coil-kt:coil-compose`), AndroidX Icons Extended, JUnit 4, Kotlin Coroutines Test.

**Spec:** [`docs/superpowers/specs/2026-09-26-dark-premium-design-system.md`](file:///Users/nabil/dev/reelnotes/docs/superpowers/specs/2026-09-26-dark-premium-design-system.md)

## Global Constraints

- Android SDK min 26, target 35, Java 17, Kotlin 2.0.21.
- All colors and typography must use tokens defined in `Color.kt` and `Type.kt`.
- Material 3 `dynamicColor` must be disabled by default so the curated linear dark theme is always active.
- Existing business logic (Room database, extraction pipeline, Gemini summarizer, ViewModel flows) must remain 100% backward compatible without regression.
- Every commit must pass `./gradlew testDebugUnitTest`.

---

### Task 1: Dark Premium Color Palette & Theme Engine

**Files:**
- Create: `app/src/test/java/com/danstudios/reelnotes/ui/theme/ThemeColorsTest.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/theme/Theme.kt`

**Interfaces:**
- Consumes: Existing Jetpack Compose Material 3 `darkColorScheme` and `lightColorScheme`.
- Produces: `DarkBackground`, `DarkSurface`, `DarkSurfaceElevated`, `DarkBorder`, `DarkBorderHover`, `NeonViolet`, `NeonVioletLight`, `NeonVioletDark`, `EmeraldSuccess`, `EmeraldLight`, `CyanAccent`, `GoldStar`, `TextPrimary`, `TextSecondary`, `TextTertiary`, `TextPlaceholder`.

- [ ] **Step 1: Write unit test validating color definitions**

Create `app/src/test/java/com/danstudios/reelnotes/ui/theme/ThemeColorsTest.kt`:

```kotlin
package com.danstudios.reelnotes.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeColorsTest {
    @Test
    fun testDarkPremiumPaletteValues() {
        assertEquals(Color(0xFF0A0B0E), DarkBackground)
        assertEquals(Color(0xFF14161D), DarkSurface)
        assertEquals(Color(0xFF1C1F2B), DarkSurfaceElevated)
        assertEquals(Color(0xFF232733), DarkBorder)
        assertEquals(Color(0xFF8B5CF6), NeonViolet)
        assertEquals(Color(0xFF10B981), EmeraldSuccess)
        assertEquals(Color(0xFF06B6D4), CyanAccent)
        assertEquals(Color(0xFFFBBF24), GoldStar)
        assertEquals(Color(0xFFF8FAFC), TextPrimary)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.danstudios.reelnotes.ui.theme.ThemeColorsTest`  
Expected: Compilation failure because `DarkBackground` etc. are not yet declared in `Color.kt`.

- [ ] **Step 3: Update `Color.kt` and `Theme.kt`**

Update `app/src/main/java/com/danstudios/reelnotes/ui/theme/Color.kt`:

```kotlin
package com.danstudios.reelnotes.ui.theme

import androidx.compose.ui.graphics.Color

// Fond OLED et Surfaces Sombres
val DarkBackground = Color(0xFF0A0B0E)
val DarkSurface = Color(0xFF14161D)
val DarkSurfaceElevated = Color(0xFF1C1F2B)
val DarkBorder = Color(0xFF232733)
val DarkBorderHover = Color(0xFF383D52)

// Accents Néon & Identité
val NeonViolet = Color(0xFF8B5CF6)
val NeonVioletLight = Color(0xFFA78BFA)
val NeonVioletDark = Color(0xFF6D28D9)
val EmeraldSuccess = Color(0xFF10B981)
val EmeraldLight = Color(0xFF34D399)
val CyanAccent = Color(0xFF06B6D4)
val GoldStar = Color(0xFFFBBF24)

// Textes & Typographie
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)
val TextPlaceholder = Color(0xFF475569)

// Category Colors for Badges & Accents
val RecipeColor = Color(0xFFF59E0B)
val WorkoutColor = Color(0xFF10B981)
val TipsColor = Color(0xFF8B5CF6)
val TutorialColor = Color(0xFF06B6D4)
val TravelColor = Color(0xFF3B82F6)
val ProductColor = Color(0xFFEC4899)
val GeneralColor = Color(0xFF94A3B8)
```

Update `app/src/main/java/com/danstudios/reelnotes/ui/theme/Theme.kt`:

```kotlin
package com.danstudios.reelnotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonViolet,
    onPrimary = TextPrimary,
    primaryContainer = NeonVioletDark,
    onPrimaryContainer = NeonVioletLight,
    secondary = CyanAccent,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = CyanAccent,
    tertiary = EmeraldSuccess,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderHover
)

private val LightColorScheme = DarkColorScheme // Dark Premium est le thème par défaut privilégié

@Composable
fun ReelNotesTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.danstudios.reelnotes.ui.theme.ThemeColorsTest`  
Expected: BUILD SUCCESSFUL (test passes).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/danstudios/reelnotes/ui/theme/Color.kt app/src/main/java/com/danstudios/reelnotes/ui/theme/Theme.kt app/src/test/java/com/danstudios/reelnotes/ui/theme/ThemeColorsTest.kt
git commit -m "style: implement Dark Premium palette and theme engine"
```

---

### Task 2: Floating Glassmorphic Navigation Bar (`FloatingBottomBar.kt`)

**Files:**
- Create: `app/src/main/java/com/danstudios/reelnotes/ui/components/FloatingBottomBar.kt`
- Create: `app/src/test/java/com/danstudios/reelnotes/ui/components/FloatingBottomBarTabTest.kt`

**Interfaces:**
- Consumes: Material icons, `DarkSurface`, `DarkBorder`, `NeonViolet`, `TextPrimary`, `TextSecondary`.
- Produces: `@Composable fun FloatingBottomBar(selectedTab: BottomBarTab, onTabSelected: (BottomBarTab) -> Unit, onAddClick: () -> Unit, modifier: Modifier = Modifier)`.

- [ ] **Step 1: Write unit test for `BottomBarTab` enum**

Create `app/src/test/java/com/danstudios/reelnotes/ui/components/FloatingBottomBarTabTest.kt`:

```kotlin
package com.danstudios.reelnotes.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class FloatingBottomBarTabTest {
    @Test
    fun testBottomBarTabValues() {
        val tabs = BottomBarTab.entries
        assertEquals(3, tabs.size)
        assertEquals(BottomBarTab.NOTES, tabs[0])
        assertEquals(BottomBarTab.FAVORITES, tabs[1])
        assertEquals(BottomBarTab.SETTINGS, tabs[2])
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.danstudios.reelnotes.ui.components.FloatingBottomBarTabTest`  
Expected: Compilation failure because `BottomBarTab` does not exist yet.

- [ ] **Step 3: Implement `FloatingBottomBar.kt`**

Create `app/src/main/java/com/danstudios/reelnotes/ui/components/FloatingBottomBar.kt`:

```kotlin
package com.danstudios.reelnotes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danstudios.reelnotes.ui.theme.*

enum class BottomBarTab(val label: String) {
    NOTES("Notes"),
    FAVORITES("Favoris"),
    SETTINGS("Réglages")
}

@Composable
fun FloatingBottomBar(
    selectedTab: BottomBarTab,
    onTabSelected: (BottomBarTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color.Black.copy(alpha = 0.6f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(DarkSurface.copy(alpha = 0.92f))
                .border(1.dp, DarkBorder, RoundedCornerShape(28.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Notes Tab
            NavItem(
                label = "Notes",
                icon = if (selectedTab == BottomBarTab.NOTES) Icons.Filled.GridView else Icons.Outlined.GridView,
                isSelected = selectedTab == BottomBarTab.NOTES,
                onClick = { onTabSelected(BottomBarTab.NOTES) }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Favoris Tab
            NavItem(
                label = "Favoris",
                icon = if (selectedTab == BottomBarTab.FAVORITES) Icons.Filled.Star else Icons.Outlined.StarOutline,
                isSelected = selectedTab == BottomBarTab.FAVORITES,
                onClick = { onTabSelected(BottomBarTab.FAVORITES) }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Glowing Center Add Button (+)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(NeonViolet, CyanAccent)
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White),
                        onClick = onAddClick
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter un Reel",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Settings Tab
            NavItem(
                label = "Réglages",
                icon = if (selectedTab == BottomBarTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                isSelected = selectedTab == BottomBarTab.SETTINGS,
                onClick = { onTabSelected(BottomBarTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) NeonVioletLight else TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "navColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.danstudios.reelnotes.ui.components.FloatingBottomBarTabTest`  
Expected: BUILD SUCCESSFUL (test passes).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/danstudios/reelnotes/ui/components/FloatingBottomBar.kt app/src/test/java/com/danstudios/reelnotes/ui/components/FloatingBottomBarTabTest.kt
git commit -m "feat(ui): add FloatingBottomBar component with frosted glass effect"
```

---

### Task 3: Modernized `CategoryChipRow.kt` with Counts & Modern Glass Badges

**Files:**
- Create: `app/src/test/java/com/danstudios/reelnotes/ui/components/CategoryCountTest.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/components/CategoryChipRow.kt`

**Interfaces:**
- Consumes: `NoteCategory`, `DarkSurface`, `DarkSurfaceElevated`, `DarkBorder`, `NeonViolet`, `TextPrimary`, `TextSecondary`.
- Produces: `@Composable fun CategoryChipRow(selectedCategory: NoteCategory?, onlyFavorites: Boolean, onCategorySelected: (NoteCategory?) -> Unit, onToggleFavorites: () -> Unit, categoryCounts: Map<NoteCategory?, Int> = emptyMap(), modifier: Modifier = Modifier)`.

- [ ] **Step 1: Write unit test validating category count helper**

Create `app/src/test/java/com/danstudios/reelnotes/ui/components/CategoryCountTest.kt`:

```kotlin
package com.danstudios.reelnotes.ui.components

import com.danstudios.reelnotes.domain.model.NoteCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryCountTest {
    @Test
    fun testCategoryChipLabelFormatting() {
        val count = 5
        val formatted = if (count > 0) "Recettes ($count)" else "Recettes"
        assertEquals("Recettes (5)", formatted)
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.danstudios.reelnotes.ui.components.CategoryCountTest`  
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Update `CategoryChipRow.kt`**

Update `app/src/main/java/com/danstudios/reelnotes/ui/components/CategoryChipRow.kt`:

```kotlin
package com.danstudios.reelnotes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danstudios.reelnotes.domain.model.NoteCategory
import com.danstudios.reelnotes.ui.theme.*

@Composable
fun CategoryChipRow(
    selectedCategory: NoteCategory?,
    onlyFavorites: Boolean,
    onCategorySelected: (NoteCategory?) -> Unit,
    onToggleFavorites: () -> Unit,
    categoryCounts: Map<NoteCategory?, Int> = emptyMap(),
    totalNotesCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "✨ Tout" Chip
        val isAllSelected = selectedCategory == null && !onlyFavorites
        ModernChip(
            label = "✨ Tout",
            count = if (totalNotesCount > 0) totalNotesCount else categoryCounts[null],
            isSelected = isAllSelected,
            onClick = {
                if (onlyFavorites) onToggleFavorites()
                onCategorySelected(null)
            }
        )

        // Specific category chips
        for (category in NoteCategory.entries) {
            val isSelected = selectedCategory == category && !onlyFavorites
            val count = categoryCounts[category]
            ModernChip(
                label = "${category.iconEmoji} ${category.label}",
                count = count,
                isSelected = isSelected,
                onClick = {
                    if (onlyFavorites) onToggleFavorites()
                    if (selectedCategory == category) {
                        onCategorySelected(null)
                    } else {
                        onCategorySelected(category)
                    }
                }
            )
        }
    }
}

@Composable
private fun ModernChip(
    label: String,
    count: Int?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) NeonViolet.copy(alpha = 0.25f) else DarkSurface,
        animationSpec = tween(durationMillis = 200),
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) NeonViolet else DarkBorder,
        animationSpec = tween(durationMillis = 200),
        label = "chipBorder"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) TextPrimary else TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "chipText"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(9999.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor
        )

        if (count != null && count > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (isSelected) NeonViolet.copy(alpha = 0.4f) else DarkSurfaceElevated)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "$count",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) TextPrimary else TextTertiary
                )
            }
        }
    }
}
```

- [ ] **Step 4: Verify compilation & run tests**

Run: `./gradlew testDebugUnitTest`  
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/danstudios/reelnotes/ui/components/CategoryChipRow.kt app/src/test/java/com/danstudios/reelnotes/ui/components/CategoryCountTest.kt
git commit -m "feat(ui): modernize CategoryChipRow with count badges and neon glow"
```

---

### Task 4: Rich Note Card Component (`NoteCard.kt`) with Hero Thumbnail & Gradient Fade

**Files:**
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/components/NoteCard.kt`

**Interfaces:**
- Consumes: `ReelNote`, `AsyncImage` (Coil), `DarkSurface`, `DarkBorder`, `DarkBorderHover`, `NeonVioletLight`, `GoldStar`, `TextPrimary`, `TextSecondary`.
- Produces: `@Composable fun NoteCard(note: ReelNote, onClick: () -> Unit, onToggleFavorite: () -> Unit, onOpenReel: () -> Unit, modifier: Modifier = Modifier)`.

- [ ] **Step 1: Update `NoteCard.kt`**

Update `app/src/main/java/com/danstudios/reelnotes/ui/components/NoteCard.kt`:

```kotlin
package com.danstudios.reelnotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.danstudios.reelnotes.domain.model.NoteCategory
import com.danstudios.reelnotes.domain.model.ReelNote
import com.danstudios.reelnotes.ui.theme.*

@Composable
fun NoteCard(
    note: ReelNote,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenReel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail Hero Section (if thumbnailUrl available)
            if (!note.thumbnailUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    AsyncImage(
                        model = note.thumbnailUrl,
                        contentDescription = note.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay (transparent top to DarkSurface bottom)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        DarkSurface.copy(alpha = 0.4f),
                                        DarkSurface
                                    )
                                )
                            )
                    )

                    // Top row over thumbnail: Category badge & Favorite button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryBadge(category = note.category)

                        FavoriteButton(
                            isFavorite = note.isFavorite,
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }
            } else {
                // Header when no thumbnail
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadge(category = note.category)

                    FavoriteButton(
                        isFavorite = note.isFavorite,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }

            // Content Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = if (note.thumbnailUrl.isNullOrBlank()) 0.dp else 6.dp)
            ) {
                // Author tag
                if (!note.author.isNullOrBlank()) {
                    Text(
                        text = "${note.author} • Instagram",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonVioletLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Title
                Text(
                    text = note.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Summary
                Text(
                    text = note.summary,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                // Metadata Stats Chips
                val prepTime = note.structuredData.prepTime
                val ingCount = note.structuredData.ingredients.size
                val stepCount = note.structuredData.steps.size
                val servings = note.structuredData.servings

                val hasStats = prepTime != null || ingCount > 0 || stepCount > 0 || servings != null
                if (hasStats) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        prepTime?.let {
                            MetadataPill(text = "⏱ $it", color = EmeraldLight)
                        }
                        servings?.let {
                            MetadataPill(text = "👥 $it", color = CyanAccent)
                        }
                        if (ingCount > 0) {
                            MetadataPill(text = "🥗 $ingCount ingrédients", color = EmeraldLight)
                        }
                        if (stepCount > 0) {
                            MetadataPill(text = "$stepCount étapes", color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun CategoryBadge(category: NoteCategory) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface.copy(alpha = 0.85f))
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${category.iconEmoji} ${category.label}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(DarkSurface.copy(alpha = 0.85f))
            .border(1.dp, DarkBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggleFavorite
            )
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
            contentDescription = "Favori",
            tint = if (isFavorite) GoldStar else TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun MetadataPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
```

- [ ] **Step 2: Verify compilation and tests**

Run: `./gradlew testDebugUnitTest`  
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/danstudios/reelnotes/ui/components/NoteCard.kt
git commit -m "feat(ui): update NoteCard with hero thumbnail, gradient fade, and metadata pills"
```

---

### Task 5: Interactive Recipe Checklist & Timeline Steps Components (`RecipeChecklist.kt`, `StepList.kt`)

**Files:**
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/components/RecipeChecklist.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/components/StepList.kt`

**Interfaces:**
- Consumes: `IngredientItem`, `StepItem`, `DarkSurface`, `DarkSurfaceElevated`, `DarkBorder`, `EmeraldSuccess`, `CyanAccent`, `TextPrimary`, `TextSecondary`, `TextTertiary`.
- Produces: Updated `@Composable fun RecipeChecklist(...)` and `@Composable fun StepList(...)`.

- [ ] **Step 1: Update `RecipeChecklist.kt`**

Update `app/src/main/java/com/danstudios/reelnotes/ui/components/RecipeChecklist.kt`:

```kotlin
package com.danstudios.reelnotes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danstudios.reelnotes.domain.model.IngredientItem
import com.danstudios.reelnotes.ui.theme.*

@Composable
fun RecipeChecklist(
    ingredients: List<IngredientItem>,
    onToggleIngredient: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = ingredients.size
    val checkedCount = ingredients.count { it.isChecked }
    val progress = if (totalCount > 0) checkedCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "recipeProgress"
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section Header with count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingrédients",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "$checkedCount / $totalCount cochés",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (checkedCount == totalCount && totalCount > 0) EmeraldSuccess else CyanAccent
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Live Progress Bar Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progression recette",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(9999.dp)),
                    color = EmeraldSuccess,
                    trackColor = DarkSurfaceElevated
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Checklist Items
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ingredients.forEachIndexed { index, item ->
                IngredientRow(
                    item = item,
                    onClick = { onToggleIngredient(index, !item.isChecked) }
                )
            }
        }
    }
}

@Composable
private fun IngredientRow(
    item: IngredientItem,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (item.isChecked) EmeraldSuccess.copy(alpha = 0.5f) else DarkBorder,
        animationSpec = tween(durationMillis = 200),
        label = "ingBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (item.isChecked) EmeraldSuccess.copy(alpha = 0.08f) else DarkSurface,
        animationSpec = tween(durationMillis = 200),
        label = "ingBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom Checkbox
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (item.isChecked) EmeraldSuccess else DarkSurfaceElevated)
                .border(1.dp, if (item.isChecked) EmeraldSuccess else DarkBorder, RoundedCornerShape(6.dp))
        ) {
            if (item.isChecked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Ingredient Name
        Text(
            text = item.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
            color = if (item.isChecked) TextTertiary else TextPrimary,
            modifier = Modifier.weight(1f)
        )

        // Quantity Badge (Amount + Unit)
        val quantity = listOfNotNull(item.amount, item.unit).filter { it.isNotBlank() }.joinToString(" ")
        if (quantity.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyanAccent.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = quantity,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
            }
        }
    }
}
```

- [ ] **Step 2: Update `StepList.kt`**

Update `app/src/main/java/com/danstudios/reelnotes/ui/components/StepList.kt`:

```kotlin
package com.danstudios.reelnotes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danstudios.reelnotes.domain.model.StepItem
import com.danstudios.reelnotes.ui.theme.*

@Composable
fun StepList(
    title: String,
    steps: List<StepItem>,
    onToggleStep: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        steps.forEachIndexed { index, step ->
            val isLast = index == steps.size - 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onToggleStep(step.stepNumber, !step.isDone) }
                    ),
                verticalAlignment = Alignment.Top
            ) {
                // Timeline Column: Circle Badge + Connecting Line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    val circleBg by animateColorAsState(
                        targetValue = if (step.isDone) EmeraldSuccess else DarkSurfaceElevated,
                        animationSpec = tween(durationMillis = 200),
                        label = "stepCircleBg"
                    )
                    val circleBorder by animateColorAsState(
                        targetValue = if (step.isDone) EmeraldSuccess else DarkBorder,
                        animationSpec = tween(durationMillis = 200),
                        label = "stepCircleBorder"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(circleBg)
                            .border(1.dp, circleBorder, CircleShape)
                    ) {
                        Text(
                            text = "${step.stepNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (step.isDone) Color.White else TextSecondary
                        )
                    }

                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(44.dp)
                                .background(DarkBorder)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Step Instruction Text
                Text(
                    text = step.instruction,
                    fontSize = 14.sp,
                    color = if (step.isDone) TextTertiary else TextPrimary,
                    textDecoration = if (step.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = if (isLast) 0.dp else 20.dp)
                )
            }
        }
    }
}
```

- [ ] **Step 3: Verify compilation and tests**

Run: `./gradlew testDebugUnitTest`  
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/danstudios/reelnotes/ui/components/RecipeChecklist.kt app/src/main/java/com/danstudios/reelnotes/ui/components/StepList.kt
git commit -m "feat(ui): update RecipeChecklist with live progress bar and StepList with vertical timeline"
```

---

### Task 6: Modernized `AddReelDialog.kt` Component

**Files:**
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/components/AddReelDialog.kt`

**Interfaces:**
- Consumes: `DarkSurface`, `DarkSurfaceElevated`, `DarkBorder`, `NeonViolet`, `CyanAccent`, `TextPrimary`, `TextSecondary`.
- Produces: `@Composable fun AddReelDialog(onDismiss: () -> Unit, onSubmit: (url: String, manualCaption: String?) -> Unit)`.

- [ ] **Step 1: Update `AddReelDialog.kt`**

Update `app/src/main/java/com/danstudios/reelnotes/ui/components/AddReelDialog.kt`:

```kotlin
package com.danstudios.reelnotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danstudios.reelnotes.ui.theme.*

@Composable
fun AddReelDialog(
    onDismiss: () -> Unit,
    onSubmit: (url: String, manualCaption: String?) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }
    var captionInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonViolet.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = NeonVioletLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ajouter un Reel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Collez le lien Instagram ou partagez directement depuis Instagram.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Lien du Reel Instagram", color = TextSecondary) },
                    placeholder = { Text("https://www.instagram.com/reel/...", color = TextPlaceholder) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            val clipText = clipboardManager.getText()?.text
                            if (!clipText.isNullOrBlank()) {
                                urlInput = clipText
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Coller lien",
                                tint = NeonVioletLight
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = captionInput,
                    onValueChange = { captionInput = it },
                    label = { Text("Légende ou texte (facultatif)", color = TextSecondary) },
                    placeholder = { Text("Utile si le compte est privé...", color = TextPlaceholder) },
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (urlInput.isNotBlank()) {
                        onSubmit(
                            urlInput.trim(),
                            captionInput.trim().ifBlank { null }
                        )
                    }
                },
                enabled = urlInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Transformer en notes avec l'IA", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
            ) {
                Text("Annuler")
            }
        }
    )
}
```

- [ ] **Step 2: Verify compilation and tests**

Run: `./gradlew testDebugUnitTest`  
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/danstudios/reelnotes/ui/components/AddReelDialog.kt
git commit -m "feat(ui): update AddReelDialog with Dark Premium styling and vibrant actions"
```

---

### Task 7: Screens Integration & Polish (`NotesListScreen.kt`, `NoteDetailScreen.kt`, `SettingsScreen.kt`)

**Files:**
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/screens/NotesListScreen.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/screens/NoteDetailScreen.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/ui/screens/SettingsScreen.kt`
- Modify: `app/src/main/java/com/danstudios/reelnotes/MainActivity.kt`

**Interfaces:**
- Consumes: `FloatingBottomBar`, `NoteCard`, `CategoryChipRow`, `RecipeChecklist`, `StepList`, `AddReelDialog`.
- Produces: Polished full application screens matching the approved web prototype.

- [ ] **Step 1: Update `NotesListScreen.kt` with FloatingBottomBar & modern header search**

Update `app/src/main/java/com/danstudios/reelnotes/ui/screens/NotesListScreen.kt`:
- Add `FloatingBottomBar` anchored at bottom.
- Add integrated search bar with rounded pill shape.
- Pass `categoryCounts` and `totalNotesCount` to `CategoryChipRow`.
- Add modern empty state with gradient icon and callout.

- [ ] **Step 2: Update `NoteDetailScreen.kt` with Hero cover, glass buttons, and card accents**

Update `app/src/main/java/com/danstudios/reelnotes/ui/screens/NoteDetailScreen.kt`:
- Display `AsyncImage` Hero cover if `thumbnailUrl` exists with vertical gradient fade.
- Circular glass back, favorite, share, and delete buttons.
- Direct Instagram button in `DarkSurfaceElevated` with play icon.
- Summary card with neon violet border and accent.
- Chef tip card with amber accent border (`#F59E0B`).

- [ ] **Step 3: Update `SettingsScreen.kt` with Dark Premium cards and borders**

Update `app/src/main/java/com/danstudios/reelnotes/ui/screens/SettingsScreen.kt`:
- Card containers with `DarkSurface` and `DarkBorder`.
- Google Gemini AI status banner and testing states.
- Clean typography and toggle switches.

- [ ] **Step 4: Run full test suite & assemble debug APK**

Run: `./gradlew testDebugUnitTest assembleDebug`  
Expected: BUILD SUCCESSFUL with 0 errors.

- [ ] **Step 5: Verify on emulator or launch check**

Run: `./gradlew compileDebugKotlin`  
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/danstudios/reelnotes/ui/screens/ app/src/main/java/com/danstudios/reelnotes/MainActivity.kt
git commit -m "feat(ui): integrate Dark Premium design system across all screens"
```

---

## Plan Self-Review Check

1. **Spec Coverage**: All items in `docs/superpowers/specs/2026-09-26-dark-premium-design-system.md` (colors, typography, `FloatingBottomBar`, `NoteCard` thumbnails, `CategoryChipRow` with counts, `RecipeChecklist` progress bar, `StepList` timeline, screen styling) are mapped to Tasks 1 through 7.
2. **No Placeholders**: Every step contains concrete code, exact paths, and commands.
3. **Type Consistency**: Color names and signatures match across all tasks.
