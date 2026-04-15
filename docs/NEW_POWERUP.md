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

*Note: If your powerup uses an existing modifier (velocity, acceleration, mass, health, input), you
can skip this step.*

### Step 4: Add Powerup Textures to Assets

Add your powerup texture to `assets/events/`.

### Step 5: Register Texture Configuration

Edit `core/src/main/java/com/fatpiggies/game/view/TextureManager.java` in the `loadTextures()`
method:

```java
public static void loadTextures() {
    // ... existing code ...
    
    // Add animation configuration for your new powerup

    textures.put("SPEED_BOOST", new Texture("events/speedBoost.png"));
    
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
            SpeedBoostModifierComponent speedMod = new SpeedBoostModifierComponent();
            speedMod.power = 150;  // Amount to increase velocity
            entity.add(speedMod);
            render.textureId = TextureId.SPEED_BOOST;
            break;
        }
        
        default:
            break;
    }
    entity.add(render);
}
```

### Step 7: Implement copy of the new modifier (optional)

> *Do next step only if you added a new modifier.*

Edit `core/src/main/java/com/fatpiggies/game/model/ecs/systems/collision/CollisionResolutionSystem.java`
in the `collect()` method:
```java
    // Add the mapper in the attribute of the system
    private final ComponentMapper<SpeedBoostModifierComponent> sbmMod = ComponentMapper.getFor(SpeedBoostModifierComponent.class);

private void collect(Entity collector, Entity item) {
        // ... other copy ...

        // Copy input modifier
        if (imMod.has(item)) {
            InputModifierComponent modifier = imMod.get(item);
            InputModifierComponent newMod = getEngine().createComponent(InputModifierComponent.class);
            newMod.power = modifier.power;
            buff.add(newMod);
        }

        // Copy speed boost modifier NEW
        if (sbmMod.has(item)) {
            SpeedBoostModifierComponent modifier = sbmMod.get(item);
            SpeedBoostModifierComponent newMod = getEngine().createComponent(SpeedBoostModifierComponent.class);
            newMod.power = modifier.power;
            buff.add(newMod);
        }

        // ... Lifetime ...
    
}
```

### Step 8: Implement application of the new modifier (optional)

Edit `core/src/main/java/com/fatpiggies/game/model/ecs/systems/StatSystem.java`
in the `update()` method:

```java
private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
// Add the mapper in the attribute of the system
private final ComponentMapper<SpeedBoostModifierComponent> sbmMod = ComponentMapper.getFor(SpeedBoostModifierComponent.class);

@Override
public void update(float deltaTime) {
    // ... other code ...

    // Apply Health Modifier
    if (sbmMod.has(powerup)) {
        VelocityComponent targetVel = vm.get(targetPig);
        SpeedBoostModifierComponent sbMod = sbmMod.get(powerup);
        targetVel.currentMaxVelocity += sbMod.power;
    }
    
    // ...
}
```

---

## Architecture Overview: How Powerups Work

```
Host Creates Powerup
    ↓
attachModifierAndRender() adds:
  • RenderComponent (for visual)
  • Modifier Component (BEER/DONUT/LIFE/APPLE/SPEED_BOOST)
  • CollectibleComponent (mark as collectible)
  • LifetimeComponent (expiration timer)
  • TransformComponent (position)
  • ColliderComponent (collision radius)
    ↓
Pig collides with powerup (CollisionDetectionSystem)
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
* Add sprite sheet to `assets/events/`
* Add case `POWERUP_NAME` in `GameWorld.attachModifierAndRender()`
* Add copy of the new modifier in `CollisionResolutionSystem.collect(collector, item)`
* Add application of new modifier in `StatSystem`
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


>⚠️ Important: If any texture is missing, the game will crash at startup.
