package com.fatpiggies.game.model.ecs.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.fatpiggies.game.model.ecs.components.AccelerationComponent;
import com.fatpiggies.game.model.ecs.components.MassComponent;
import com.fatpiggies.game.model.ecs.components.NeedsRespawnComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.VelocityComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the RespawnSystem.
 * Ensures physics are reset and the entity is moved to a safe location.
 */
public class RespawnSystemTest {
    private Engine engine;

    @BeforeEach
    void setUp() {
        engine = new Engine();
        engine.addSystem(new RespawnSystem());
    }

    private Entity createRespawningPig() {
        Entity entity = new Entity();

        // Simulating an out-of-bounds position
        TransformComponent transform = new TransformComponent();
        transform.x = -100f;
        transform.y = 500f;

        // Simulating physic
        VelocityComponent velocity = new VelocityComponent();
        velocity.vx = 50f;
        velocity.vy = -30f;
        velocity.baseMaxVelocity = 10f;
        velocity.currentMaxVelocity = 25f; // E.g., had a speed boost active
        AccelerationComponent acceleration = new AccelerationComponent();
        acceleration.ax = 15f;
        acceleration.ay = -5f;
        acceleration.baseMaxAcceleration = 10f;
        acceleration.currentMaxAcceleration = 20f;
        MassComponent mass = new MassComponent();
        mass.baseMass = 10f;
        mass.currentMass = 15f;


        // Adding the flag that triggers the system
        NeedsRespawnComponent respawnFlag = new NeedsRespawnComponent();

        entity.add(transform);
        entity.add(velocity);
        entity.add(acceleration);
        entity.add(mass);
        entity.add(respawnFlag);

        engine.addEntity(entity);
        return entity;
    }

    @Test
    void testRespawnResetsPhysicsAndPosition() {
        // Setup
        Entity pig = createRespawningPig();

        // Execution
        engine.update(0.1f);

        // Check Physics Reset
        VelocityComponent velocity = pig.getComponent(VelocityComponent.class);
        assertEquals(0f, velocity.vx, "Velocity X must be completely reset");
        assertEquals(0f, velocity.vy, "Velocity Y must be completely reset");
        assertEquals(10f, velocity.currentMaxVelocity, "Current max velocity must be reset to base max velocity");

        // Check Position (must be inside the bounds: 50 to 1000)
        TransformComponent transform = pig.getComponent(TransformComponent.class);
        assertTrue(transform.x >= 50f && transform.x <= 1000f, "X position must be within arena bounds");
        assertTrue(transform.y >= 50f && transform.y <= 1000f, "Y position must be within arena bounds");

        // Check Flag Removal
        assertNull(pig.getComponent(NeedsRespawnComponent.class), "NeedsRespawnComponent must be removed after successful respawn");
    }
}
