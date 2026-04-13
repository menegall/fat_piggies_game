package com.fatpiggies.game.model.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.fatpiggies.game.model.ecs.components.item.LifetimeComponent;

/**
 * A utility system that manages the duration of temporary entities, such as
 * power-up effects, projectiles, or particle emitters.
 * <p><b>How it works internally:</b><br>
 * The system continuously iterates over all entities possessing a {@link LifetimeComponent}.
 * Every frame, it subtracts the {@code deltaTime} from the component's {@code timeLeft}.
 * Once the time reaches zero or drops below it, the entity is automatically
 * and safely removed from the Ashley {@code Engine}. If you are using a
 * {@code PooledEngine}, the entity's components will be automatically returned to their pools.
 * <p><b>Usage and Initialization:</b><br>
 * Simply instantiate the system and add it to your engine. When creating a temporary
 * entity (like a spawned power-up), attach a {@code LifetimeComponent} and set its duration.
 * <pre>
 * {@code
 * // 1. Add system to the engine
 * engine.addSystem(new LifetimeSystem());
 * // 2. Example: Spawning a temporary power-up
 * Entity powerup = engine.createEntity();
 * LifetimeComponent life = engine.createComponent(LifetimeComponent.class);
 * life.timeLeft = 5.0f; // Expires in 5 seconds
 * powerup.add(life);
 * engine.addEntity(powerup);
 * }
 * </pre>
 * <p><b>Execution Order Note:</b><br>
 * It is best practice to add this system <b>early</b> in your engine's system list
 * (before movement, physics, or rendering). This ensures that expired entities are
 * cleaned up immediately and aren't needlessly processed or drawn during their final frame.
 */
public class LifetimeSystem extends IteratingSystem {
    // ComponentMapper provide optimal access to components
    private final ComponentMapper<LifetimeComponent> lm = ComponentMapper.getFor(LifetimeComponent.class);

    public LifetimeSystem() {
        // Define the Family on which this system will be applied
        // In this case: entities must have LifetimeComponent
        super(Family.all(LifetimeComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Get the LifetimeComponent
        LifetimeComponent lifetime = lm.get(entity);
        // Update the entity lifetime
        lifetime.timeLeft -= deltaTime;
        if (lifetime.timeLeft <= 0) {
            // Remove the entity from the engine
            getEngine().removeEntity(entity);
        }
    }
}
