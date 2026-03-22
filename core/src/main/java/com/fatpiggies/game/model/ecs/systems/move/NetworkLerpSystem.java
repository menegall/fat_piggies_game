package com.fatpiggies.game.model.ecs.systems.move;

import static com.fatpiggies.game.model.utils.GameConstants.LERP_FACTOR;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkSyncComponent;

/**
 * An interpolation system designed exclusively for remote entities (other players)
 * in a multiplayer environment. It smooths out network stuttering by visually
 * gliding the entity towards the authoritative coordinates sent by the server.
 * <p><b>How it works internally:</b><br>
 * The system targets entities that have both a {@link TransformComponent} and a
 * {@link NetworkSyncComponent}. Crucially, it <b>excludes</b> any entity with a
 * {@link PlayerInputComponent} to ensure the local player's movement is never overridden
 * by network lag.
 * It calculates an interpolation factor (alpha) based on {@code deltaTime} and uses
 * linear interpolation for X/Y coordinates, and spherical linear interpolation (shortest path)
 * for the rotation angle.
 * <p><b>Usage and Initialization:</b><br>
 * This system should only be added to the {@code Engine} of the <b>Client</b>.
 * The authoritative Host does not need this system for its internal physics simulation.
 * <pre>
 * {@code
 * // 1. Add system to the client's engine
 * engine.addSystem(new NetworkLerpSystem());
 * // 2. When receiving data from Firebase/Server:
 * NetworkSyncComponent sync = remotePig.getComponent(NetworkSyncComponent.class);
 * sync.targetX = newServerX;
 * sync.targetY = newServerY;
 * // The system will automatically lerp the TransformComponent towards these targets!
 * }
 * </pre>
 * <p><b>Execution Order Note:</b><br>
 * Add this system <b>after</b> any systems that might handle network buffers, but
 * strictly <b>before</b> your {@code RenderSystem}. Because it doesn't process local
 * players, its order relative to your {@code MovementSystem} does not matter, as they
 * operate on completely separate families of entities.
 */
public class NetworkLerpSystem extends IteratingSystem {
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<NetworkSyncComponent> nsm = ComponentMapper.getFor(NetworkSyncComponent.class);
    // Variable for fluidity of interpolation. lower value = smoother interpolation


    public NetworkLerpSystem() {
        // We take all with TransformComponent and NetworkSyncComponent but
        // exclude PlayerInputComponent
        super(Family.all(TransformComponent.class, NetworkSyncComponent.class)
            .exclude(PlayerInputComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = tm.get(entity);
        NetworkSyncComponent sync = nsm.get(entity);
        // We limit the alpha value to be between 0 and 1 to avoid bigger movements
        float lerpAlpha = MathUtils.clamp(LERP_FACTOR * deltaTime, 0f, 1f);
        float deltaX = sync.targetX - transform.x;
        float deltaY = sync.targetY - transform.y;
        float targetAngle = transform.angle;

        // Linear interpolation for x and y
        transform.x += (deltaX) * lerpAlpha;
        transform.y += (deltaY) * lerpAlpha;

        // Spherical interpolation for angle
        if (Math.abs(deltaX) > 0.01f || Math.abs(deltaY) > 0.01f) {
            targetAngle = MathUtils.atan2((float) (deltaY), (float) (deltaX)) * MathUtils.radiansToDegrees;
        }
        transform.angle = MathUtils.lerpAngleDeg((float) transform.angle, targetAngle, lerpAlpha);
    }
}
