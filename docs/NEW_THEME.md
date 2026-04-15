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

### Step 2: Add Theme-Specific TextureIds

Edit `core/src/main/java/com/fatpiggies/game/view/TextureId.java`:

```Java
public enum TextureId {
// ... existing entries ...

    // Jungle (new theme)
    PLAY_BACKGROUND_JUNGLE,
    BLUE_PIG_JUNGLE,
    RED_PIG_JUNGLE,
    GREEN_PIG_JUNGLE,
    YELLOW_PIG_JUNGLE,
    
    // ... rest of entries ...
}
```

### Step 3: Create Theme Assets

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

### Step 4: Register Theme Textures in TextureManager

Edit `core/src/main/java/com/fatpiggies/game/view/TextureManager.java` in the `loadTextures()`
method.
Find the section with existing themes and add:

```Java
public static void loadTextures() {

    if (!textures.isEmpty()) return;

    // ... existing UI textures ...

    // ===== FARM =====
    textures.put(TextureId.PLAY_BACKGROUND_FARM, new Texture("backgrounds/arena_farm.png"));
    configs.put(TextureId.BLUE_PIG_FARM, new AnimationConfig("pig/farm/blue.png", 2, 1, 2, -1, 1f));
    configs.put(TextureId.RED_PIG_FARM, new AnimationConfig("pig/farm/red.png", 2, 1, 2, -1, 1f));
    configs.put(TextureId.GREEN_PIG_FARM, new AnimationConfig("pig/farm/green.png", 2, 1, 2, -1, 1f));
    configs.put(TextureId.YELLOW_PIG_FARM, new AnimationConfig("pig/farm/yellow.png", 2, 1, 2, -1, 1f));

    // ... VOLCANO, PIRATE, SPACE sections ...

    // ===== JUNGLE (NEW) =====
    textures.put(TextureId.PLAY_BACKGROUND_JUNGLE, new Texture("backgrounds/arena_jungle.png"));
    configs.put(TextureId.BLUE_PIG_JUNGLE, new AnimationConfig("pig/jungle/blue.png", 2, 1, 2, -1, 1f));
    configs.put(TextureId.RED_PIG_JUNGLE, new AnimationConfig("pig/jungle/red.png", 2, 1, 2, -1, 1f));
    configs.put(TextureId.GREEN_PIG_JUNGLE, new AnimationConfig("pig/jungle/green.png", 2, 1, 2, -1, 1f));
    configs.put(TextureId.YELLOW_PIG_JUNGLE, new AnimationConfig("pig/jungle/yellow.png", 2, 1, 2, -1, 1f));

    // ... rest of configuration ...
}
```

**AnimationConfig Parameters Explained:**

* First param (`"pig/jungle/blue.png"`): Path to sprite sheet (relative to assets/)
* Second param (`2`): Number of rows in sprite grid
* Third param (`1`): Number of columns in sprite grid
* Fourth param (`2`): Number of animated frames
* Fifth param (`-1`): Forced frame (-1 = animate all frames)
* Sixth param (`1f`): Animation cycle time in seconds

### Step 5: Create Theme-to-Pig Mapping

In the same loadTextures() method, locate the "THEME PIG MAP" section and add:

```Java
    // ===== THEME PIG MAP =====
    Map<TextureId, TextureId> farm = new HashMap<>();
    farm.put(TextureId.BLUE_PIG, TextureId.BLUE_PIG_FARM);
    farm.put(TextureId.RED_PIG, TextureId.RED_PIG_FARM);
    farm.put(TextureId.GREEN_PIG, TextureId.GREEN_PIG_FARM);
    farm.put(TextureId.YELLOW_PIG, TextureId.YELLOW_PIG_FARM);
    
    Map<TextureId, TextureId> volcano = new HashMap<>();
    volcano.put(TextureId.BLUE_PIG, TextureId.BLUE_PIG_VOLCANO);
    volcano.put(TextureId.RED_PIG, TextureId.RED_PIG_VOLCANO);
    volcano.put(TextureId.GREEN_PIG, TextureId.GREEN_PIG_VOLCANO);
    volcano.put(TextureId.YELLOW_PIG, TextureId.YELLOW_PIG_VOLCANO);
    
    Map<TextureId, TextureId> pirate = new HashMap<>();
    pirate.put(TextureId.BLUE_PIG, TextureId.BLUE_PIG_PIRATE);
    pirate.put(TextureId.RED_PIG, TextureId.RED_PIG_PIRATE);
    pirate.put(TextureId.GREEN_PIG, TextureId.GREEN_PIG_PIRATE);
    pirate.put(TextureId.YELLOW_PIG, TextureId.YELLOW_PIG_PIRATE);
    
    Map<TextureId, TextureId> space = new HashMap<>();
    space.put(TextureId.BLUE_PIG, TextureId.BLUE_PIG_SPACE);
    space.put(TextureId.RED_PIG, TextureId.RED_PIG_SPACE);
    space.put(TextureId.GREEN_PIG, TextureId.GREEN_PIG_SPACE);
    space.put(TextureId.YELLOW_PIG, TextureId.YELLOW_PIG_SPACE);
    
    Map<TextureId, TextureId> jungle = new HashMap<>();  // ← NEW
    jungle.put(TextureId.BLUE_PIG, TextureId.BLUE_PIG_JUNGLE);
    jungle.put(TextureId.RED_PIG, TextureId.RED_PIG_JUNGLE);
    jungle.put(TextureId.GREEN_PIG, TextureId.GREEN_PIG_JUNGLE);
    jungle.put(TextureId.YELLOW_PIG, TextureId.YELLOW_PIG_JUNGLE);
    
    themePigMap.put(Theme.FARM, farm);
    themePigMap.put(Theme.VOLCANO, volcano);
    themePigMap.put(Theme.SPACE, space);
    themePigMap.put(Theme.PIRATE, pirate);
    themePigMap.put(Theme.JUNGLE, jungle);  // ← NEW
```

### Step 6: Create Theme Background Mapping

In the same method, locate the "THEME BACKGROUND MAP" section and add:

```Java
    // ===== THEME BACKGROUND =====
    themeBackgroundMap.put(Theme.FARM, TextureId.PLAY_BACKGROUND_FARM);
    themeBackgroundMap.put(Theme.VOLCANO, TextureId.PLAY_BACKGROUND_VOLCANO);
    themeBackgroundMap.put(Theme.PIRATE, TextureId.PLAY_BACKGROUND_PIRATE);
    themeBackgroundMap.put(Theme.SPACE, TextureId.PLAY_BACKGROUND_SPACE);
    themeBackgroundMap.put(Theme.JUNGLE, TextureId.PLAY_BACKGROUND_JUNGLE);  // ← NEW
```

### Step 7: Add Theme Pricing (Optional)

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

### Step 8: Test Your Theme

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
* Add 5 `TextureId` entries (`PLAY_BACKGROUND_JUNGLE`, `BLUE_PIG_JUNGLE`, etc.)
* Create `assets/backgrounds/arena_jungle.png`
* Create `assets/pig/jungle/` folder with 4 pig color sprites
* Add texture loading in `TextureManager.loadTextures()` (background + pig configs)
* Add theme-to-pig mapping in `themePigMap`
* Add theme-to-background mapping in `themeBackgroundMap`
* Add theme price in `ShopController.getPrice()` (optional but recommended)
* Rebuild
* Test theme selection and visual appearance in-game

