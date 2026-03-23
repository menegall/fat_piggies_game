package com.fatpiggies.game.model.ecs.systems.move;

import static com.fatpiggies.game.model.utils.GameConstants.BOTTOM_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.LEFT_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.MAX_SPAWN_ATTEMPTS;
import static com.fatpiggies.game.model.utils.GameConstants.RIGHT_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.SAFE_SPAWN_RADIUS;
import static com.fatpiggies.game.model.utils.GameConstants.TOP_BOUND;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.NeedsRespawnComponent;
import com.fatpiggies.game.model.ecs.components.item.AttachedComponent;
import com.fatpiggies.game.model.ecs.components.physics.AccelerationComponent;
import com.fatpiggies.game.model.ecs.components.physics.MassComponent;
import com.fatpiggies.game.model.ecs.components.physics.VelocityComponent;

/**
 * System responsible for safely relocating entities and resetting their physical state when they are flagged for respawn.
 * <p>
 * <b>Behavior:</b>
 * This system acts as a listener for entities marked with the {@link NeedsRespawnComponent} tag.
 * When it processes an entity, it performs the following lifecycle reset:
 * <ol>
 * <li><b>Physics Reset:</b> Zeros out all movement vectors in the {@link VelocityComponent} (and Acceleration if applicable) to prevent leftover inertia.</li>
 * <li><b>Stats Reset:</b> Restores {@code currentMaxVelocity} to the {@code baseMaxVelocity}.</li>
 * <li><b>Power-up Cleanup:</b> Removes any temporary buffs or attached entities.</li>
 * <li><b>Safe Teleportation:</b> Finds a random spot within the arena bounds that is at least a safe distance away from other entities, falling back to the center if no space is found.</li>
 * <li><b>Flag Removal:</b> Removes the {@link NeedsRespawnComponent} to return the entity to normal gameplay.</li>
 * </ol>
 * <p>
 * <b>Required Components:</b> {@link TransformComponent}, {@link VelocityComponent}, {@link NeedsRespawnComponent}
 * <p>
 * <b>How to use in the Controller:</b>
 * Add this system to your Ashley Engine. Any other system in the game can trigger this behavior simply by attaching a {@code NeedsRespawnComponent} to an entity.
 * <pre>
 * {@code
 * Engine engine = new Engine();
 * engine.addSystem(new RespawnSystem());
 * }
 * </pre>
 * <i>Design Note: This modular approach means you can reuse this system for lava pits, traps, or manual respawn requests without duplicating the safe-spawn logic.</i>
 */
public class RespawnSystem extends IteratingSystem {
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<AccelerationComponent> am = ComponentMapper.getFor(AccelerationComponent.class);
    private final ComponentMapper<MassComponent> mm = ComponentMapper.getFor(MassComponent.class);
    private ImmutableArray<Entity> allEntitiesWithTransform;

    public RespawnSystem() {
        super(Family.all(TransformComponent.class, NeedsRespawnComponent.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        // Take all entities with TransformComponent to check for collisions
        allEntitiesWithTransform = engine.getEntitiesFor(Family.all(TransformComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = tm.get(entity);
        VelocityComponent velocity = vm.get(entity);
        AccelerationComponent acceleration = am.get(entity);
        MassComponent mass = mm.get(entity);

        physicReset(velocity, acceleration, mass);

        clearPowerUps(entity);

        // Safe Respawn
        boolean spawnSuccessful = false;
        // Try to find a free space for the pig
        for (int i = 0; i < MAX_SPAWN_ATTEMPTS; i++) {
            float randomX = MathUtils.random(LEFT_BOUND, RIGHT_BOUND);
            float randomY = MathUtils.random(BOTTOM_BOUND, TOP_BOUND);

            if (isSpaceFree(randomX, randomY, entity)) {
                transform.x = randomX;
                transform.y = randomY;
                spawnSuccessful = true;
                break;
            }
        }

        // Fallback: If no free space is found, center the pig
        if (!spawnSuccessful) {
            transform.x = (LEFT_BOUND + RIGHT_BOUND) / 2f;
            transform.y = (BOTTOM_BOUND + TOP_BOUND) / 2f;
        }
        // Remove tag
        entity.remove(NeedsRespawnComponent.class);
    }

    /**
     * Check if there is a free space for the pig to spawn.
     *
     * @return true if there is a free space, false otherwise
     */
    private boolean isSpaceFree(float x, float y, Entity self) {
        for (Entity otherEntity : allEntitiesWithTransform) {
            // Don't check collision with himself
            if (otherEntity == self) continue;

            TransformComponent otherTransform = tm.get(otherEntity);

            float dx = otherTransform.x - x;
            float dy = otherTransform.y - y;
            float distanceSquared = (dx * dx) + (dy * dy);

            // No safe space found
            if (distanceSquared < (SAFE_SPAWN_RADIUS * SAFE_SPAWN_RADIUS)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clear all the power-ups attached to the pig.
     *
     * @param pigEntity The entity representing the pig.
     */
    private void clearPowerUps(Entity pigEntity) {
        ImmutableArray<Entity> powerUpEntities = getEngine().getEntitiesFor(Family.all(AttachedComponent.class).get());
        for (Entity powerUp : powerUpEntities) {
            AttachedComponent attached = powerUp.getComponent(AttachedComponent.class);
            if (attached.targetEntity == pigEntity) {
                getEngine().removeEntity(powerUp);
            }
        }
    }

    /**
     * Reset all the physic components to their default values.
     */
    private void physicReset(VelocityComponent velocity, AccelerationComponent acceleration, MassComponent mass) {
        velocity.vx = 0f;
        velocity.vy = 0f;
        velocity.currentMaxVelocity = velocity.baseMaxVelocity;
        acceleration.ax = 0f;
        acceleration.ay = 0f;
        acceleration.currentMaxAcceleration = acceleration.baseMaxAcceleration;
        mass.currentMass = mass.baseMass;
    }
}
