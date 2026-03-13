package com.fatpiggies.game.model.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.fatpiggies.game.model.ecs.components.LifetimeComponent;

/**
 * Manages the duration of temporary entities, such as power-up effects.
 * It decrements the timeLeft in the LifetimeComponent and removes the entity from
 * the PooledEngine once it reaches zero.
 * When a power-up is created it is attached a LifetimeComponent that say when it die,
 * no matter what happen to the power-up.
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
