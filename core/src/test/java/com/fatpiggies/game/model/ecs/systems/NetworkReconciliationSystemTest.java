package com.fatpiggies.game.model.ecs.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkSyncComponent;
import com.fatpiggies.game.model.ecs.components.physics.VelocityComponent;
import com.fatpiggies.game.model.ecs.systems.move.NetworkReconciliationSystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NetworkReconciliationSystemTest {

    private Engine engine;

    @BeforeEach
    void setUp() {
        engine = new Engine();
        engine.addSystem(new NetworkReconciliationSystem());
    }

    /**
     * Helper method to create a local player entity with all required components.
     */
    private Entity createLocalPig(float x, float y, float vx, float targetX, float targetY, float targetVx, float angle, float targetAngle) {
        Entity entity = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.x = x;
        transform.y = y;
        transform.angle = angle;

        VelocityComponent velocity = new VelocityComponent();
        velocity.vx = vx;
        velocity.vy = 0f;

        NetworkSyncComponent sync = new NetworkSyncComponent();
        sync.targetX = targetX;
        sync.targetY = targetY;
        sync.targetVx = targetVx;
        sync.targetVy = 0f;
        sync.targetAngle = targetAngle;

        PlayerInputComponent input = new PlayerInputComponent(); // Crucial for Local Pig identification

        entity.add(transform).add(velocity).add(sync).add(input);
        engine.addEntity(entity);
        return entity;
    }

    @Test
    void testSmallError_BelowIgnoreThreshold_DoesNothing() {
        // Setup: Local X is 0, Server X is 20.
        // Distance is 20, which is < IGNORE_THRESHOLD (30).
        Entity player = createLocalPig(0f, 0f, 100f, 20f, 0f, 100f, 0f, 0f);

        // Execute frame
        engine.update(0.1f);

        TransformComponent transform = player.getComponent(TransformComponent.class);
        VelocityComponent velocity = player.getComponent(VelocityComponent.class);

        // Verify: The system trusts the client completely. No changes should occur.
        assertEquals(0f, transform.x, 0.001f, "Position should NOT be corrected for small errors.");
        assertEquals(100f, velocity.vx, 0.001f, "Velocity should NOT be touched.");
    }

    @Test
    void testMediumError_BetweenThresholds_AppliesRubberBanding() {
        // Setup: Local X is 0, Server X is 100.
        // Distance is 100, which is > 30 and < 150. (Medium Error)
        Entity player = createLocalPig(0f, 0f, 100f, 100f, 0f, 100f, 0f, 0f);

        // Execute frame with deltaTime = 0.1 seconds
        engine.update(0.1f);

        TransformComponent transform = player.getComponent(TransformComponent.class);
        VelocityComponent velocity = player.getComponent(VelocityComponent.class);

        // Math breakdown for Lerp:
        // Correction vector = ServerPos (100) - LocalPos (0) = 100
        // Lerp step = 100 * CORRECTION_LERP (5.0f) * deltaTime (0.1f) = 50
        // New X should be 0 + 50 = 50.

        assertEquals(50f, transform.x, 0.001f, "Position should be smoothly interpolated (Lerped) towards the server.");
        assertEquals(100f, velocity.vx, 0.001f, "Velocity should NOT be touched during a soft correction.");
    }

    @Test
    void testLargeError_AboveSnapThreshold_SnapsPositionAndVelocity() {
        // Setup: Local X is 0 (moving right at 100). Server X is 200 (knocked left at -500).
        // Distance is 200, which is > SNAP_THRESHOLD (150). (Critical Desync)
        Entity player = createLocalPig(0f, 0f, 100f, 200f, 0f, -500f, 0f, 0f);

        // Execute frame
        engine.update(0.1f);

        TransformComponent transform = player.getComponent(TransformComponent.class);
        VelocityComponent velocity = player.getComponent(VelocityComponent.class);

        // Verify: Instant teleportation AND velocity overwrite
        assertEquals(200f, transform.x, 0.001f, "Position should SNAP instantly to the server's position.");
        assertEquals(-500f, velocity.vx, 0.001f, "Local velocity MUST be overwritten to apply the server's knockback physics.");
    }

    @Test
    void testAngleDesync_Above10Degrees_SnapsAngle() {
        // Setup: Local angle is 0, Server angle is 45. Diff is 45 (> 10).
        // Position is perfectly synced to isolate the angle test.
        Entity player = createLocalPig(0f, 0f, 0f, 0f, 0f, 0f, 0f, 45f);

        engine.update(0.1f);

        TransformComponent transform = player.getComponent(TransformComponent.class);

        // Verify: Angle snaps to match the server.
        assertEquals(45f, transform.angle, 0.001f, "Angle should snap to server target if difference > 10 degrees.");
    }

    @Test
    void testAngleSync_Below10Degrees_IgnoresAngle() {
        // Setup: Local angle is 0, Server angle is 5. Diff is 5 (< 10).
        Entity player = createLocalPig(0f, 0f, 0f, 0f, 0f, 0f, 0f, 5f);

        engine.update(0.1f);

        TransformComponent transform = player.getComponent(TransformComponent.class);

        // Verify: Angle is ignored to prevent micro-stuttering while turning.
        assertEquals(0f, transform.angle, 0.001f, "Angle should NOT snap if the difference is tiny.");
    }

    @Test
    void testRemotePig_IsCompletelyIgnored() {
        // Setup: We create a pig with a MASSIVE error (Distance 500).
        // HOWEVER, we do NOT add a PlayerInputComponent. This simulates a Remote Enemy Pig.
        Entity remotePig = new Entity();
        TransformComponent transform = new TransformComponent();
        transform.x = 0;
        transform.y = 0;
        VelocityComponent velocity = new VelocityComponent();
        velocity.vx = 50;
        NetworkSyncComponent sync = new NetworkSyncComponent();
        sync.targetX = 500;
        sync.targetY = 0;

        remotePig.add(transform).add(velocity).add(sync);
        // NO PlayerInputComponent added!
        engine.addEntity(remotePig);

        engine.update(0.1f);

        // Verify: Because it lacks PlayerInputComponent, the Reconciliation System's Family must reject it.
        // It should remain exactly where it was.
        assertEquals(0f, transform.x, 0.001f, "Remote pigs must be ignored by the Reconciliation System.");
        assertEquals(50f, velocity.vx, 0.001f, "Remote pigs' velocities must not be touched here.");
    }
}
