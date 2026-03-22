package com.fatpiggies.game.model.ecs.systems.collision;

import static com.fatpiggies.game.model.utils.GameConstants.BOTTOM_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.LEFT_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.RIGHT_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.TOP_BOUND;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.fatpiggies.game.model.ecs.components.HealthComponent;
import com.fatpiggies.game.model.ecs.components.RenderComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.NeedsRespawnComponent;
import com.fatpiggies.game.model.ecs.systems.move.RespawnSystem;

/**
 * System responsible for monitoring entity positions and enforcing the game's arena boundaries.
 * <p>
 * <b>Behavior:</b>
 * This system checks the {@link TransformComponent} of entities against the defined arena limits.
 * If an entity crosses these bounds, it decrements the {@link HealthComponent#currentLife}.
 * <ul>
 * <li>If the entity has lives remaining, it is flagged with a {@link NeedsRespawnComponent} to be relocated safely.</li>
 * <li>If the entity's life drops to 0 or below, its {@link TransformComponent} and {@link RenderComponent} are removed, effectively "killing" it and hiding it from the game world.</li>
 * </ul>
 * <p>
 * <b>Required Components:</b> {@link TransformComponent}, {@link HealthComponent}
 * <br>
 * <b>Excluded Components:</b> {@link NeedsRespawnComponent} (Prevents the system from deducting multiple lives while the entity is waiting to be processed by the RespawnSystem).
 * <p>
 * <b>How to use in the Controller:</b>
 * Instantiate and add this system to your Ashley Engine during the game screen or controller initialization.
 * <pre>
 * {@code
 * Engine engine = new Engine();
 * engine.addSystem(new ArenaBoundsSystem());
 * }
 * </pre>
 * <i>Note: Ensure this system is added to the engine alongside the {@link RespawnSystem} for the full out-of-bounds loop to work correctly.</i>
 */
public class ArenaBoundsSystem extends IteratingSystem {
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);
    private ImmutableArray<Entity> allEntitiesWithTransform;

    public ArenaBoundsSystem() {
        super(Family.all(TransformComponent.class, HealthComponent.class)
            .exclude(NeedsRespawnComponent.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        // Take all entities with TransformComponent to check for collisions respawn
        allEntitiesWithTransform = engine.getEntitiesFor(Family.all(TransformComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HealthComponent health = hm.get(entity);

        // If the entity is dead, skip it
        if (health.currentLife <= 0) return;

        TransformComponent transform = tm.get(entity);


        boolean isOutOfBounds = transform.x < LEFT_BOUND || transform.x > RIGHT_BOUND ||
            transform.y < BOTTOM_BOUND || transform.y > TOP_BOUND;

        if (isOutOfBounds) {
            // Remove one life if the entity is out of bounds
            health.currentLife--;

            if (health.currentLife <= 0) {
                // DEAD: Remove components to eliminate pig.
                entity.remove(TransformComponent.class);
                entity.remove(RenderComponent.class);
            } else {
                // RESPAWN: Mark the entity for respawning
                entity.add(new NeedsRespawnComponent());
            }
        }

    }
}
