package com.fatpiggies.game.model.ecs.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.fatpiggies.game.model.ecs.components.NetworkSyncComponent;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NetworkLerpSystemTest {
    private Engine engine;
    private NetworkLerpSystem system;

    @BeforeEach
    void setUp() {
        // Prepare the engine and system
        engine = new Engine();
        system = new NetworkLerpSystem();
        engine.addSystem(system);
    }

    @Test
    void testInterpolation_MovesTowardsTarget() {
        // Setup entity(Remote Pig)
        Entity remotePig = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.x = 0f; // Start from 0
        transform.y = 0f;
        transform.angle = 0f;

        NetworkSyncComponent sync = new NetworkSyncComponent();
        sync.targetX = 10f; // Server say that has to move to (10,0)
        sync.targetY = 0f;

        remotePig.add(transform);
        remotePig.add(sync);
        engine.addEntity(remotePig);

        // Simulate 60fps (0.016 seconds)
        engine.update(0.016f);

        // Check
        assertEquals(1.6f, transform.x, 0.001f, "X was not interpolated correctly");
        assertEquals(0f, transform.y, 0.001f, "Y must not change");
    }

    @Test
    void testSystem_IgnoresLocalPlayer() {
        // Setup
        Entity localPig = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.x = 0f;

        NetworkSyncComponent sync = new NetworkSyncComponent();
        sync.targetX = 10f;

        // Add component that must make the system ignore the entity
        PlayerInputComponent input = new PlayerInputComponent();

        localPig.add(transform);
        localPig.add(sync);
        localPig.add(input);
        engine.addEntity(localPig);

        // Execute test
        engine.update(0.016f);

        // Check
        assertEquals(0f, transform.x, 0.001f, "System is not ignoring local player!");
    }

    @Test
    void testSystem_AngleInterpolation() {
        // Setup
        Entity remotePig = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.x = 0f; // Start from 0
        transform.y = 0f;
        transform.angle = -5f;

        NetworkSyncComponent sync = new NetworkSyncComponent();
        sync.targetX = 10f; // Server say that has to move to (10,10)
        sync.targetY = 10f;

        remotePig.add(transform);
        remotePig.add(sync);
        engine.addEntity(remotePig);

        // Execute test
        engine.update(0.016f);

        // Check
        assertEquals(3f, transform.angle, 0.001f, "Angle was not interpolated correctly");
    }

}
