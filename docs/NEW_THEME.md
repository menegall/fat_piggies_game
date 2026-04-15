# 🎨 Adding a New Theme

This documentation explains how to add new visual themes to the **Fat Piggies** game.

## Overview

Themes in Fat Piggies control the visual appearance of the gameplay area. Specifically, a theme
changes:

1. The **Arena background**
2. The **Pig skins** (for each of the 4 colors: blue, red, green, yellow)

Current built-in themes are: `FARM`, `VOLCANO`, `PIRATE`, `SPACE`.

---

## Step-by-Step Guide

### Step 1: Add New Theme to Enum

Edit `core/src/main/java/com/fatpiggies/game/view/Theme.java`:

```java
package com.fatpiggies.game.view;

public enum Theme {
    FARM,
    VOLCANO,
    PIRATE,
    SPACE,
    JUNGLE,  // ← NEW: Add your theme here
}
```

### Step 2: Create Theme Assets

The assets folder structure follows this pattern:

```Code
assets/
├── backgrounds/
│   ├── arena_farm.png
│   ├── arena_volcano.png
│   ├── arena_pirate.png
│   ├── arena_space.png
│   └── arena_jungle.png          (NEW: Background for jungle theme)
└── pig/
    ├── farm/
    │   ├── blue.png
    │   ├── red.png
    │   ├── green.png
    │   └── yellow.png
    ├── volcano/
    │   ├── blue.png
    │   ├── red.png
    │   ├── green.png
    │   └── yellow.png
    ├── pirate/
    │   ├── blue.png
    │   ├── red.png
    │   ├── green.png
    │   └── yellow.png
    ├── space/
    │   ├── blue.png
    │   ├── red.png
    │   ├── green.png
    │   └── yellow.png
    └── jungle/                    (NEW: Create this folder)
        ├── blue.png              (2x1 sprite, 2 animated frames)
        ├── red.png
        ├── green.png
        └── yellow.png
```

**Specifications:**

* **Arena background:** Place in `assets/backgrounds/arena_jungle.png`
    * Resolution: 1920x1080 pixels (or your target resolution)
    * Format: PNG
* **Pig sprites:** Place in `assets/pig/jungle/` directory
    * Grid layout: 2 rows × 1 column (2x1)
    * Contains 2 animated frames
    * Format: PNG (transparent background recommended)

### Step 3: Load Theme Textures in TextureManager

Edit `core/src/main/java/com/fatpiggies/game/view/TextureManager.java` in the `loadTextures()`
method.
Find the section with existing themes and add:

```Java
public static void loadTextures() {

    if (!textures.isEmpty()) return;

    // ... existing UI textures ...

    // ===== THEMES =====
    loadTheme("farm");
    loadTheme("volcano");
    loadTheme("pirate");
    loadTheme("space");
    loadTheme("jungle");  // ← NEW: load your theme here
    
    // ... rest of configuration ...
}
```

### Step 4: Add Theme Pricing (Optional)

Edit `core/src/main/java/com/fatpiggies/game/controller/ShopController.java`:

```Java
public class ShopController {
private int coins;
private Set<Theme> unlocked;

    private static final int FARM_PRICE = 0;
    private static final int VOLCANO_PRICE = 100;
    private static final int PIRATE_PRICE = 200;
    private static final int SPACE_PRICE = 300;
    private static final int JUNGLE_PRICE = 250;  // ← NEW

    // ... other code ...

    public int getPrice(Theme theme) {
        switch(theme){
            case FARM: return FARM_PRICE;
            case VOLCANO: return VOLCANO_PRICE;
            case PIRATE: return PIRATE_PRICE;
            case SPACE: return SPACE_PRICE;
            case JUNGLE: return JUNGLE_PRICE;  // ← NEW
            default: return FARM_PRICE;
        }
    }
    // ... rest of class ...
}
```

### Step 5: Test Your Theme

1. Create the asset files in the correct locations:
    * `assets/backgrounds/arena_jungle.png`
    * `assets/pig/jungle/blue.png`
    * `assets/pig/jungle/red.png`
    * `assets/pig/jungle/green.png`
    * `assets/pig/jungle/yellow.png`
2. Rebuild the project
3. Run the game
4. Go to the Shop screen
5. Navigate through themes using the Previous/Next buttons
6. Your new JUNGLE theme should appear in the rotation
7. Select it to see the new arena background and pig skins

--- 

## Asset File Structure Summary

For a new theme called **JUNGLE**, create:

| File             | Location                              | Type | Dimensions           |
|------------------|---------------------------------------|------|----------------------|
| Arena background | `assets/backgrounds/arena_jungle.png` | PNG  | 1920x1080            |
| Blue pig         | 	`assets/pig/jungle/blue.png`         | 	PNG | 	2x1 grid (2 frames) | 
| Red pig          | 	`assets/pig/jungle/red.png`	         | PNG  | 	2x1 grid (2 frames) |
| Green pig        | 	`assets/pig/jungle/green.png`	       | PNG  | 	2x1 grid (2 frames) |
| Yellow pig       | 	`assets/pig/jungle/yellow.png`	      | PNG  | 	2x1 grid (2 frames) |

---

## Implementation Checklist

* Add `JUNGLE` to `Theme` enum
* Create `assets/backgrounds/arena_jungle.png`
* Create `assets/pig/jungle/` folder with 4 pig color sprites
* Load themes in `TextureManager.loadTextures()`
* Add theme price in `ShopController.getPrice()` (optional but recommended)
* Rebuild
* Test theme selection and visual appearance in-game

