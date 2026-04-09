package com.fatpiggies.game.model.ecs.systems;

import static com.fatpiggies.game.model.utils.GameConstants.POWERUP_COLLISION_RADIUS;
import static com.fatpiggies.game.model.utils.GameConstants.POWER_ACCELERATION_MODIFIER;
import static com.fatpiggies.game.model.utils.GameConstants.POWER_MASS_MODIFIER;
import static com.fatpiggies.game.model.utils.GameConstants.POWER_VELOCITY_MODIFIER;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IntervalSystem;
import com.badlogic.gdx.math.MathUtils;
import com.fatpiggies.game.model.ecs.components.RenderComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.ColliderComponent;
import com.fatpiggies.game.model.ecs.components.item.CollectibleComponent;
import com.fatpiggies.game.model.ecs.components.item.LifetimeComponent;
import com.fatpiggies.game.model.ecs.components.modifier.AccelerationModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.MassModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.VelocityModifierComponent;
import com.fatpiggies.game.model.utils.GameConstants;
import com.fatpiggies.game.view.TextureId;

/**
 *  !!! <b>DON'T USE THIS SYSTEM</b> !!!
 * <p>
 * System responsible for spawning collectible power-ups at regular intervals.
 * <p>
 * <b>Behavior:</b>
 * This system runs periodically based on {@link GameConstants#POWERUP_SPAWN_INTERVAL}.
 * When triggered, it creates a new entity representing a power-up with:
 * <ul>
 * <li>A random position within the arena bounds.</li>
 * <li>A random lifetime before it disappears.</li>
 * <li>A randomly selected modifier component (Velocity, Acceleration, or Mass) that defines its effect.</li>
 * </ul>
 * <p>
 * <b>How to use in the Controller:</b>
 * Instantiate and add this system to the Ashley Engine.
 * <pre>
 * {@code
 * Engine engine = new Engine();
 * engine.addSystem(new PowerUpSpawnerSystem());
 * }
 * </pre>
 */
public class PowerUpSpawnerSystem extends IntervalSystem {

    public PowerUpSpawnerSystem() {
        // The system will update automatically every POWERUP_SPAWN_INTERVAL seconds
        super(GameConstants.POWERUP_SPAWN_INTERVAL);
    }

    @Override
    protected void updateInterval() {
        Engine engine = getEngine();
        if (engine == null) return;

        Entity powerUpEntity = new Entity();

        // Position: Random coordinates within the defined bounds
        TransformComponent transform = new TransformComponent();
        transform.x = MathUtils.random(GameConstants.LEFT_BOUND, GameConstants.RIGHT_BOUND);
        transform.y = MathUtils.random(GameConstants.BOTTOM_BOUND, GameConstants.TOP_BOUND);

        // Lifetime: Random duration between MIN and MAX
        LifetimeComponent lifetime = new LifetimeComponent();
        lifetime.timeLeft = MathUtils.random(GameConstants.POWERUP_MIN_LIFETIME, GameConstants.POWERUP_MAX_LIFETIME);

        // Core Components for a collectible
        RenderComponent render = new RenderComponent();
        CollectibleComponent collectible = new CollectibleComponent();
        ColliderComponent collider = new ColliderComponent();
        collider.radius = POWERUP_COLLISION_RADIUS;

        // TODO : delete this system?
        render.textureId = TextureId. BLUE_PIG;

        // Attach base components
        powerUpEntity.add(transform);
        powerUpEntity.add(lifetime);
        powerUpEntity.add(render);
        powerUpEntity.add(collectible);
        powerUpEntity.add(collider);

        // Random Modifier Assignment
        // Randomly pick a number between 0 and 2 to decide the power-up type
        int modifierType = MathUtils.random(0, 2);

        switch (modifierType) {
            case 0:
                VelocityModifierComponent velocityMod = new VelocityModifierComponent();
                velocityMod.power = POWER_VELOCITY_MODIFIER;
                powerUpEntity.add(velocityMod);
                break;

            case 1:
                AccelerationModifierComponent accelerationMod = new AccelerationModifierComponent();
                // E.g., increases acceleration
                accelerationMod.power = POWER_ACCELERATION_MODIFIER;
                powerUpEntity.add(accelerationMod);
                break;

            case 2:
                MassModifierComponent massMod = new MassModifierComponent();
                massMod.power = POWER_MASS_MODIFIER;
                powerUpEntity.add(massMod);
                break;
        }

        // Finally, add the fully constructed power-up to the engine
        engine.addEntity(powerUpEntity);
    }
}
