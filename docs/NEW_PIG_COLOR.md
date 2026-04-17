# 🎨 Adding a New Pig Color

This documentation explains how to add new visual colors to the **Fat Piggies** game.

## Overview

Colors in Fat Piggies control the visual appearance of the pigs. Specifically :

1. The **Pig skins** (for each of the themes)
2. The **Life Pig skin**
3. The **Over Pig skin**


Current built-in colors are: `BLUE`, `GREEN`, `RED`, `YELLOW`, `PURPLE`.
Current built-in themes are: `FARM`, `VOLCANO`, `PIRATE`, `SPACE`.

---

## Step-by-Step Guide

### Step 1: Add New Color to the Enums

Edit `core/src/main/java/com/fatpiggies/game/view/PlayerColor.java`:

```java
package com.fatpiggies.game.view;

public enum PlayerColor {
    BLUE,
    GREEN,
    RED,
    YELLOW,
    PURPLE,
    PINK // NEW color
}
```

Edit `core/src/main/java/com/fatpiggies/game/view/TextureId.java`:

```java
package com.fatpiggies.game.view;

public enum TextureId {
    LOGO,
    MENU_BACKGROUND,

    // Used in the rest of the code
    PLAY_BACKGROUND,
    BLUE_PIG,
    GREEN_PIG,
    RED_PIG,
    YELLOW_PIG,
    PURPLE_PIG,
    PINK_PIG // NEW pig
    
    // Other ids
}
```

### Step 2: Add the Assets

The assets folder structure follows this pattern:

```Code
assets/   
└── pig/
    ├── farm/
    │   ├── blue.png
    │   ├── red.png
    │   ├── green.png
    │   ├── yellow.png
    │   ├── purple.png
    │   └── pink.png      (New color added)
    ├── volcano/
    │   ├── blue.png
    │   ├── red.png
    │   ├── green.png
    │   ├── yellow.png
    │   ├── purple.png
    │   └── pink.png      (New color added)
    ├── pirate/
    │   ├── blue.png
    │   ├── red.png
    │   ├── green.png
    │   ├── yellow.png
    │   ├── purple.png
    │   └── pink.png      (New color added)
    ├── space/
    │   ├── blue.png
    │   ├── red.png
    │   ├── green.png
    │   ├── yellow.png
    │   ├── purple.png
    │   └── pink.png      (New color added)
    ├── life/
    │   ├── blue.png
    │   ├── red.png
    │   ├── green.png
    │   ├── yellow.png
    │   ├── purple.png
    │   └── pink.png      (New color added)
    └── over/
        ├── blue.png
        ├── red.png
        ├── green.png
        ├── yellow.png
        ├── purple.png
        └── pink.png      (New color added)
```

**Specifications:**

* **Pig sprites:** for each themes
    * Grid layout: 2 rows × 1 column (2x1)
    * Contains 2 animated frames
    * Format: 32x32 pixel PNG (transparent background recommended)
  
* **Life Pig sprites:**
    * Grid layout: 2 rows × 2 column (2x2)
    * Contains 4 animated frames with the last one representing a dead pig
    * Format: PNG (transparent background recommended)

* **Over Pig sprites:**
    * Grid layout: 2 rows × 2 column (2x2)
    * Contains 4 animated frames
    * Format: PNG (transparent background recommended)

### Step 3: Test Your Color

1. Rebuild the project
2. Run the game
3. Change your pig color on the menu screen
4. Check in the shop if all your pigs got added
5. Start lobby to see your life pig texture

--- 

## Asset File Structure Summary

For a new color called **PINK**, create:

| File     | Location                        | Type | Dimensions           |
|----------|---------------------------------|------|----------------------|
| Pink pig | 	`assets/pig/farm/pink.png`     | 	PNG | 	2x1 grid (2 frames) | 
| Pink pig | 	`assets/pig/volcano/pink.png`	 | PNG  | 	2x1 grid (2 frames) |
| Pink pig | 	`assets/pig/pirate/pink.png`	  | PNG  | 	2x1 grid (2 frames) |
| Pink pig | 	`assets/pig/space/pink.png`	   | PNG  | 	2x1 grid (2 frames) |
| Pink pig | 	`assets/pig/life/pink.png`	    | PNG  | 	2x2 grid (4 frames) |
| Pink pig | 	`assets/pig/over/pink.png`	    | PNG  | 	2x2 grid (4 frames) |

---

## Implementation Checklist

* Add `PINK` to `PlayerColor` enum and `PINK_PIG` to `TextureId` enum
* Create all assets for each themes
* Create assets for life and over pig
* Rebuild
* Test theme selection and visual appearance in-game

>⚠️ Important: If any texture is missing, the game will crash at startup.

