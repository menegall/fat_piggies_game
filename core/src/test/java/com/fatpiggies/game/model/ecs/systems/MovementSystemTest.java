package com.fatpiggies.game.model.ecs.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.VelocityComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MovementSystemTest {
    private Engine engine;

    @BeforeEach
    void setUp() {
        // Prepare the engine and system
        engine = new Engine();
        engine.addSystem(new MovementSystem());
    }

    private Entity createValidEntity(float maxVelocity) {
        Entity entity = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.x = 0f;
        transform.y = 0f;

        VelocityComponent velocity = new VelocityComponent();
        velocity.vx = 0f;
        velocity.vy = 0f;
        velocity.currentMaxVelocity = maxVelocity;

        PlayerInputComponent input = new PlayerInputComponent();
        input.joystickPourcentageX = 0f;
        input.joystickPourcentageY = 0f;

        entity.add(transform);
        entity.add(velocity);
        entity.add(input);

        engine.addEntity(entity);
        return entity;
    }

    @Test
    void testNoInputResultsInZeroVelocityAndNoMovement() {
        // Setup
        Entity entity = createValidEntity(10f);

        // Execution
        engine.update(1.0f);

        // Check
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);

        assertEquals(0f, velocity.vx, "Velocity X must be 0");
        assertEquals(0f, velocity.vy, "Velocity Y must be 0");
        assertEquals(0f, transform.x, "Position X must not change");
        assertEquals(0f, transform.y, "Position X must not change");
    }

    @Test
    void testHorizontalMovementAppliesCorrectVelocityAndPosition() {
        // Setup
        Entity entity = createValidEntity(5f);
        PlayerInputComponent input = entity.getComponent(PlayerInputComponent.class);

        // Simulation joystick completely to the right
        input.joystickPourcentageX = 1f;
        input.joystickPourcentageY = 0f;

        // Execution: update engine for 0.5 seconds
        engine.update(0.5f);

        // Check
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);

        assertEquals(5f, velocity.vx, "Velocity X must be equals to maxVelocity");
        assertEquals(0f, velocity.vy, "Velocity Y must be 0");


        assertEquals(2.5f, transform.x, "Position X must be updated correctly");
        assertEquals(0f, transform.y, "Position Y must not change");
    }

    @Test
    void testDiagonalMovementIsNormalized() {
        // Setup
        Entity entity = createValidEntity(10f);
        PlayerInputComponent input = entity.getComponent(PlayerInputComponent.class);

        input.joystickPourcentageX = 1f;
        input.joystickPourcentageY = 1f;

        // Execution
        engine.update(1.0f);

        // Check
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);

        float expectedVelocityAxis = 10f * (float) Math.sqrt(0.5);

        assertEquals(expectedVelocityAxis, velocity.vx, 0.001f, "Velocity X must be normalized");
        assertEquals(expectedVelocityAxis, velocity.vy, 0.001f, "Velocity Y must be normalized");

        assertEquals(expectedVelocityAxis, transform.x, 0.001f, "Position X must be updated correctly");
        assertEquals(expectedVelocityAxis, transform.y, 0.001f, "Position Y must be updated correctly");
    }
}
