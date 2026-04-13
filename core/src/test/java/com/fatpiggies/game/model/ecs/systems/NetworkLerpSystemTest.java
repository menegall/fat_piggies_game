package com.fatpiggies.game.model.ecs.systems;

import static com.fatpiggies.game.model.utils.GameConstants.LERP_FACTOR;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkSyncComponent;
import com.fatpiggies.game.model.ecs.systems.move.NetworkLerpSystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NetworkLerpSystemTest {
    private Engine engine;
    private NetworkLerpSystem system;

    @BeforeEach
    void setUp() {
        engine = new Engine();
        system = new NetworkLerpSystem();
        engine.addSystem(system);
    }

    @Test
    void testInterpolation_MovesTowardsTarget() {
        Entity remotePig = new Entity();
        TransformComponent transform = new TransformComponent();
        transform.x = 0f;
        transform.y = 0f;

        NetworkSyncComponent sync = new NetworkSyncComponent();
        sync.targetX = 10f;
        sync.targetY = 0f;

        remotePig.add(transform);
        remotePig.add(sync);
        engine.addEntity(remotePig);

        float deltaTime = 0.016f;
        engine.update(deltaTime);

        // Calculate expected value based on LERP_FACTOR
        // Formula: current + (target - current) * (LERP_FACTOR * deltaTime)
        float alpha = MathUtils.clamp(LERP_FACTOR * deltaTime, 0f, 1f);
        float expectedX = 0f + (10f - 0f) * alpha;

        assertEquals(expectedX, transform.x, 0.001f, "X should depend on LERP_FACTOR");
    }

    @Test
    void testSystem_IgnoresLocalPlayer() {
        Entity localPig = new Entity();
        TransformComponent transform = new TransformComponent();
        transform.x = 0f;
        NetworkSyncComponent sync = new NetworkSyncComponent();
        sync.targetX = 10f;
        PlayerInputComponent input = new PlayerInputComponent();

        localPig.add(transform).add(sync).add(input);
        engine.addEntity(localPig);

        engine.update(0.016f);

        assertEquals(0f, transform.x, 0.001f, "System must ignore entities with PlayerInputComponent");
    }

    @Test
    void testSystem_AngleInterpolation() {
        Entity remotePig = new Entity();
        TransformComponent transform = new TransformComponent();
        transform.x = 0f;
        transform.y = 0f;
        transform.angle = -5f; // Initial angle

        NetworkSyncComponent sync = new NetworkSyncComponent();
        sync.targetX = 10f;
        sync.targetY = 10f; // This creates a movement vector towards 45 degrees

        remotePig.add(transform);
        remotePig.add(sync);
        engine.addEntity(remotePig);

        float deltaTime = 0.016f;
        engine.update(deltaTime);

        // 1. Determine the target angle (direction of movement)
        // atan2(10-0, 10-0) = 45 degrees
        float moveAngle = MathUtils.atan2(10f, 10f) * MathUtils.radiansToDegrees;

        // 2. Calculate interpolation using the same alpha as the system
        float alpha = MathUtils.clamp(LERP_FACTOR * deltaTime, 0f, 1f);
        float expectedAngle = MathUtils.lerpAngleDeg(-5f, moveAngle, alpha);

        assertEquals(expectedAngle, transform.angle, 0.001f, "Angle interpolation failed");
    }
}
