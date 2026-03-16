package com.fatpiggies.game.model.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.fatpiggies.game.model.ecs.components.NetworkSyncComponent;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;

/**
 * Used exclusively for remote entities (other players or synchronized
 * objects). It smoothly interpolates the current TransformComponent toward the target
 * coordinates stored in the NetworkSyncComponent received from the database.
 * It also interpolate the angle using Spherical Linear Interpolation.
 */
public class NetworkLerpSystem extends IteratingSystem {
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<NetworkSyncComponent> nsm = ComponentMapper.getFor(NetworkSyncComponent.class);
    // Variable for fluidity of interpolation. lower value = smoother interpolation
    private final float LERP_FACTOR = 10f;

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
        double deltaX = sync.targetX - transform.x;
        double deltaY = sync.targetY - transform.y;
        float targetAngle = (float) transform.angle;

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
