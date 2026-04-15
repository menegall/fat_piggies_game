# 🍄 Adding a New Powerup

This documentation explains how to add new powerups to the **Fat Piggies** game.
The game uses an ECS (Entity Component System) architecture built with LibGDX's Ashley framework.

## Overview

Powerups are collectible items that modify pig stats (velocity, acceleration, mass, health, or
input).
When a pig collides with a powerup, the powerup's effects are applied through modifier components.

Powerups have:

* A **type** (enum value in `PowerUpType`)
* A **visual representation** (texture from `TextureId`)
* A **modifier component** (defines what stat it affects)
* A **lifetime** (how long it persists in the game)

---

## Step-by-Step Guide

### Step 1: Add a New PowerUpType Enum Value

Edit `core/src/main/java/com/fatpiggies/game/model/utils/PowerUpType.java`:

```java
package com.fatpiggies.game.model.utils;

public enum PowerUpType {
    BEER,
    DONUT,
    LIFE,
    APPLE,
    SPEED_BOOST,  // ← NEW: Add your powerup type here
}
```

### Step 2: Add Textures to TextureId Enum

Edit `core/src/main/java/com/fatpiggies/game/view/TextureId.java`:

```java
public enum TextureId {
    // ... existing entries ...
    
    // Powerups
    DONUT,
    BEER,
    LIFE,
    APPLE,
    SPEED_BOOST,  // ← NEW: Add texture ID for your powerup
    
    // ... rest of entries ...
}
```

### Step 3: Create the Modifier Component (if needed)

If your powerup affects a new stat type, create a new modifier component.
For example, if we're creating a speed boost:

```java
package com.fatpiggies.game.model.ecs.components.modifier;

import com.badlogic.ashley.core.Component;

public class SpeedBoostModifierComponent implements Component {
    public int power;  // The magnitude of the boost
}
```

_Note: If your powerup uses an existing modifier (velocity, acceleration, mass, health, input), you
can skip this step._

### Step 4: Add Powerup Textures to Assets

Add your powerup texture to `assets/events/items.png`. The game uses a sprite sheet:

* Grid size: 2x2 (2 rows, 2 columns)
* Total frames: 4 animated frames
* Animation time: 2.0 seconds

Add your texture to this sprite sheet and note its position (row, column).

### Step 5: Register Texture Configuration

Edit `core/src/main/java/com/fatpiggies/game/view/TextureManager.java` in the `loadTextures()`
method:

```java
public static void loadTextures() {
    // ... existing code ...
    
    // Add animation configuration for your new powerup
    configs.put(TextureId.SPEED_BOOST, 
        new AnimationConfig("events/items.png", 2, 2, 4, -1, 2f));
    
    // ... rest of code ...
}
```

### Step 6: Implement Powerup Logic in GameWorld

Edit `core/src/main/java/com/fatpiggies/game/model/GameWorld.java` in the
`attachModifierAndRender()` method:

```java
private void attachModifierAndRender(Entity entity, PowerUpType type) {
    RenderComponent render = new RenderComponent();
    render.width = POWER_UP_WIDTH;
    render.height = POWER_UP_HEIGHT;
    render.angleOffset = POWER_UP_ANGLE_OFFSET;

    switch (type) {
        case BEER: {
            InputModifierComponent inputMod = new InputModifierComponent();
            render.textureId = TextureId.BEER;
            entity.add(inputMod);
            break;
        }
        // ... other cases ...
        
        case SPEED_BOOST: {  // ← NEW: Add your case
            VelocityModifierComponent velocityMod = new VelocityModifierComponent();
            velocityMod.power = 150;  // Amount to increase velocity
            entity.add(velocityMod);
            render.textureId = TextureId.SPEED_BOOST;
            break;
        }
        
        default:
            break;
    }
    entity.add(render);
}
```

---

## Architecture Overview: How Powerups Work

```
Host Creates Powerup
    ↓
attachModifierAndRender() adds:
  • RenderComponent (for visual)
  • Modifier Component (BEER/DONUT/LIFE/APPLE)
  • CollectibleComponent (mark as collectible)
  • LifetimeComponent (expiration timer)
  • TransformComponent (position)
  • ColliderComponent (collision radius)
    ↓
Pig collides with powerup
    ↓
CollisionResolutionSystem.collect()
    ↓
Creates new buff entity:
  • AttachedComponent (links to pig)
  • Copies modifier component
  • Copies lifetime
    ↓
StatSystem reads modifier each frame:
  • Increases/decreases pig stats
  • Duration countdown
    ↓
LifetimeSystem removes expired buff
```

## Powerup Types Reference

| Type  | Effect Component                                    | Use Case               |
|-------|-----------------------------------------------------|------------------------|
| BEER  | `InputModifierComponent`                            | Inverted controls      |
| DONUT | `MassModifierComponent` `VelocityModifierComponent` | Makes pig slow & heavy |
| LIFE  | `HealthModifierComponent`                           | Healing (+1 HP)        |
| APPLE | `VelocityModifierComponent`                         | Speed boost            |

## Checklist

* Add `POWERUP_NAME` to `PowerUpType` enum
* Add `POWERUP_NAME` to `TextureId` enum
* Create modifier component (only if new stat type)
* Add sprite sheet to `assets/events/items.png`
* Add `AnimationConfig` in `TextureManager.loadTextures()`
* Add case `POWERUP_NAME` in `GameWorld.attachModifierAndRender()`
* Test powerup spawning and collection

## Troubleshooting

| Issue                      | Solution                                                                             |
|----------------------------|--------------------------------------------------------------------------------------|
| Powerup not appearing      | Check `PowerUpType` enum has the type, check `attachModifierAndRender()` handles it. |
| Texture missing error      | Verify file path in `assets/` matches the string in `TextureManager`.                |
| Powerup effect not working | Ensure modifier component is attached and `StatSystem` processes it.                 |

## Key Classes Reference

* `PowerUpType.java` - Enum defining powerup types.
* `GameWorld.java` - Creates powerups and applies modifiers.
* `StatSystem.java` - Applies powerup effects each frame.
* `LifetimeSystem.java` - Removes expired powerups.
* `CollisionResolutionSystem.java` - Handles powerup collection.
