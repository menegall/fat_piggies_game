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
    void testHorizontalMovementAppliesAccelerationAndFriction() {
        // Setup: MaxVel=100, MaxAcc=1000, Mass=10
        Entity entity = createValidEntity(100f, 1000f, 10f);
        PlayerInputComponent input = entity.getComponent(PlayerInputComponent.class);

        // Simulate joystick completely to the right
        input.joystickPercentageX = 1f;
        input.joystickPercentageY = 0f;

        // Execution: update engine for 0.1 seconds
        engine.update(0.1f);

        // Mathematical Breakdown for dt = 0.1s:
        // 1. Force = input (1.0) * MaxAcc (1000) = 1000
        // 2. ax = Force (1000) / Mass (10) = 100
        // 3. New Velocity = 0 + (100 * 0.1) = 10
        // 4. Ground Friction (5.0f) = 10 - (10 * 5.0 * 0.1) = 10 - 5 = 5
        // Resulting vx should be 5.
        // Transform X = 0 + (5 * 0.1) = 0.5

        TransformComponent transform = entity.getComponent(TransformComponent.class);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);

        assertEquals(5f, velocity.vx, 0.001f, "Velocity X must correctly apply acceleration and friction");
        assertEquals(0f, velocity.vy, 0.001f, "Velocity Y must be 0");
        assertEquals(0.5f, transform.x, 0.001f, "Position X must be updated correctly");
        assertEquals(0f, transform.y, 0.001f, "Position Y must not change");
    }

    @Test
    void testEnvironmentalFrictionSlowsDownEntityWithNoInput() {
        // Setup: A pig that was pushed and is currently sliding
        Entity entity = createValidEntity(100f, 1000f, 10f);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        velocity.vx = 100f; // Moving fast to the right

        // Execution: update engine for 0.1 seconds (No joystick input)
        engine.update(0.1f);

        // Mathematical Breakdown for dt = 0.1s:
        // 1. Initial vx = 100
        // 2. Ground Friction (5.0f) = 100 - (100 * 5.0 * 0.1) = 100 - 50 = 50
        // Resulting vx should be 50.

        assertEquals(50f, velocity.vx, 0.001f, "Friction should halve the velocity in 0.1 seconds");
    }

    @Test
    void testSoftSpeedLimitTriggersWhenPushedBeyondMaxVelocity() {
        // Setup: Pig has max speed 100, but gets bumped to 300 speed
        Entity entity = createValidEntity(100f, 1000f, 10f);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        velocity.vx = 300f;

        // Execution: update engine for 0.05 seconds to see partial drag
        engine.update(0.05f);

        // Mathematical Breakdown for dt = 0.05s:
        // 1. Initial vx = 300
        // 2. Ground Friction (5.0f) = 300 - (300 * 5.0 * 0.05) = 300 - 75 = 225
        // 3. Speed (225) is still > Max Velocity (100), so Soft Limit triggers!
        // 4. Soft Drag (10.0f) = 225 - (225 * 10.0 * 0.05) = 225 - 112.5 = 112.5

        assertEquals(112.5f, velocity.vx, 0.001f, "Soft speed limit should aggressively brake the entity");
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
        // Therefore, friction should NOT be applied, and velocity should remain exactly 100.
        assertEquals(100f, velocity.vx, 0.001f, "Remote pigs must be ignored by the MovementSystem");
    }
}
