package com.fatpiggies.game.model.ecs.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.fatpiggies.game.model.ecs.components.HealthComponent;
import com.fatpiggies.game.model.ecs.components.RenderComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.NeedsRespawnComponent;
import com.fatpiggies.game.model.ecs.systems.collision.ArenaBoundsSystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the ArenaBoundsSystem.
 * Ensures that entities out of bounds are properly penalized and flagged for respawn or death.
 */
public class ArenaBoundsSystemTest {
    private Engine engine;

    @BeforeEach
    void setUp() {
        engine = new Engine();
        engine.addSystem(new ArenaBoundsSystem());
    }

    private Entity createPig(float x, float y, int life) {
        Entity entity = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.x = x;
        transform.y = y;

        HealthComponent health = new HealthComponent();
        health.currentLife = life;

        RenderComponent render = new RenderComponent();

        entity.add(transform);
        entity.add(health);
        entity.add(render);

        engine.addEntity(entity);
        return entity;
    }

    @Test
    void testEntityInsideBoundsRemainsUnaffected() {
        // Setup: Pig safely in the middle of the arena
        Entity pig = createPig(500f, 500f, 3);

        // Execution
        engine.update(0.1f);

        // Check
        HealthComponent health = pig.getComponent(HealthComponent.class);
        assertEquals(3, health.currentLife, "Health should remain unchanged");
        assertNull(pig.getComponent(NeedsRespawnComponent.class), "Should NOT be flagged for respawn");
    }

    @Test
    void testEntityOutOfBoundsLosesLifeAndGetsRespawnFlag() {
        // Setup: Pig completely out of bounds (x < 50)
        Entity pig = createPig(10f, 500f, 3);

        // Execution
        engine.update(0.1f);

        // Check
        HealthComponent health = pig.getComponent(HealthComponent.class);
        assertEquals(2, health.currentLife, "Health should be reduced by 1");
        assertNotNull(pig.getComponent(NeedsRespawnComponent.class), "Should be flagged for respawn");
    }

    @Test
    void testEntityWithOneLifeDiesOutOfBounds() {
        // Setup: Pig out of bounds with only 1 life left
        Entity pig = createPig(1500f, 500f, 1);

        // Execution
        engine.update(0.1f);

        // Check
        HealthComponent health = pig.getComponent(HealthComponent.class);
        assertEquals(0, health.currentLife, "Health should drop to 0");

        // Death check: Transform and Render should be removed
        assertNull(pig.getComponent(TransformComponent.class), "TransformComponent should be removed on death");
        assertNull(pig.getComponent(RenderComponent.class), "RenderComponent should be removed on death");
        assertNull(pig.getComponent(NeedsRespawnComponent.class), "Dead entity should NOT be flagged for respawn");
    }
}
