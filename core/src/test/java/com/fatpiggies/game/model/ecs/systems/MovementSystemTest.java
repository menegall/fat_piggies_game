package com.fatpiggies.game.model.ecs.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkSyncComponent;
import com.fatpiggies.game.model.ecs.components.physics.AccelerationComponent;
import com.fatpiggies.game.model.ecs.components.physics.MassComponent;
import com.fatpiggies.game.model.ecs.components.physics.VelocityComponent;
import com.fatpiggies.game.model.ecs.systems.move.MovementSystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MovementSystemTest {
    private Engine engine;

    @BeforeEach
    void setUp() {
        // Prepare the engine and system before each test
        engine = new Engine();
        engine.addSystem(new MovementSystem());
    }

    /**
     * Helper method to create a fully equipped local pig with physics components.
     */
    private Entity createValidEntity(float maxVelocity, float maxAcceleration, float massValue) {
        Entity entity = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.x = 0f;
        transform.y = 0f;

        VelocityComponent velocity = new VelocityComponent();
        velocity.vx = 0f;
        velocity.vy = 0f;
        velocity.currentMaxVelocity = maxVelocity;

        AccelerationComponent acceleration = new AccelerationComponent();
        acceleration.currentMaxAcceleration = maxAcceleration;

        MassComponent mass = new MassComponent();
        mass.currentMass = massValue;

        PlayerInputComponent input = new PlayerInputComponent();
        input.joystickPercentageX = 0f;
        input.joystickPercentageY = 0f;
        input.multiplier = 1f;

        entity.add(transform);
        entity.add(velocity);
        entity.add(acceleration);
        entity.add(mass);
        entity.add(input);

        engine.addEntity(entity);
        return entity;
    }

    @Test
    void testNoInputResultsInZeroVelocityAndNoMovement() {
        // Setup: MaxVel=100, MaxAcc=1000, Mass=10
        Entity entity = createValidEntity(100f, 1000f, 10f);

        // Execution
        engine.update(0.1f);

        // Check
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);

        assertEquals(0f, velocity.vx, "Velocity X must be 0");
        assertEquals(0f, velocity.vy, "Velocity Y must be 0");
        assertEquals(0f, transform.x, "Position X must not change");
        assertEquals(0f, transform.y, "Position Y must not change");
    }

    @Test
    void testHorizontalMovementAppliesAccelerationOnly() {
        // Arcade Logic: Friction is IGNORED while the player is giving input.
        // Setup: MaxVel=100, MaxAcc=1000, Mass=10
        Entity entity = createValidEntity(100f, 1000f, 10f);
        PlayerInputComponent input = entity.getComponent(PlayerInputComponent.class);

        // Simulate joystick completely to the right
        input.joystickPercentageX = 1f;
        input.joystickPercentageY = 0f;

        // Execution: update engine for 0.1 seconds
        engine.update(0.1f);

        TransformComponent transform = entity.getComponent(TransformComponent.class);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);

        // In Arcade logic, acceleration is applied until it hits the maxSpeed limit (100).
        assertEquals(100f, velocity.vx, 0.001f, "Velocity X must correctly apply acceleration up to max speed");
        assertEquals(0f, velocity.vy, 0.001f, "Velocity Y must be 0");

        // Transform moves by velocity * dt (100 * 0.1 = 10)
        assertEquals(10f, transform.x, 0.001f, "Position X must be updated correctly based on clamped velocity");
        assertEquals(0f, transform.y, 0.001f, "Position Y must not change");
    }

    @Test
    void testLinearFrictionSlowsDownEntityWithNoInput() {
        // Arcade Logic: Friction is a linear subtraction, not a proportional drag.
        // Setup: A pig that was pushed and is currently sliding
        Entity entity = createValidEntity(100f, 1000f, 10f);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        velocity.vx = 100f; // Moving fast to the right

        // Execution: update engine for 0.1 seconds (No joystick input)
        engine.update(0.1f);

        // Mathematical Breakdown based on System actual output:
        // 1. Initial vx = 100
        // 2. Linear Friction (GROUND_FRICTION * dt) drops the speed to 95.0 in 0.1s.
        assertEquals(95f, velocity.vx, 0.001f, "Friction should linearly reduce velocity when there is no input");
    }

    @Test
    void testHardSpeedLimitTriggersWhenPushedBeyondMaxVelocity() {
        // Arcade Logic: A hard clamp limits the speed instantly to maxVelocity.
        // Setup: Pig has max speed 100, but gets bumped to 300 speed
        Entity entity = createValidEntity(100f, 1000f, 10f);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        velocity.vx = 300f;

        // Execution: update engine for 0.05 seconds
        engine.update(0.05f);

        // Mathematical Breakdown:
        // 1. Initial vx = 300
        // 2. The system checks if len2() > maxSpeed^2
        // 3. It normalizes and scales exactly to maxSpeed (100)
        assertEquals(100f, velocity.vx, 0.001f, "Hard speed limit should instantly cap the entity's speed to Max Velocity");
    }

    @Test
    void testSystemIgnoresRemotePigs() {
        // Setup: Create a pig, but attach a NetworkSyncComponent to simulate a remote player
        Entity entity = createValidEntity(100f, 1000f, 10f);
        entity.add(new NetworkSyncComponent()); // THIS SHOULD MAKE THE SYSTEM IGNORE IT

        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        velocity.vx = 100f; // Give it initial velocity

        // Execution: update engine
        engine.update(0.1f);

        // Check: Because it's a remote pig, the MovementSystem should completely skip it.
        // Therefore, velocity should remain exactly 100.
        assertEquals(100f, velocity.vx, 0.001f, "Remote pigs must be ignored by the MovementSystem");
    }
}
