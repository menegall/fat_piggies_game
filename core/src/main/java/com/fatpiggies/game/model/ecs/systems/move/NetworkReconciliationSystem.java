package com.fatpiggies.game.model.ecs.systems.move;

import static com.fatpiggies.game.model.utils.GameConstants.CORRECTION_LERP;
import static com.fatpiggies.game.model.utils.GameConstants.IGNORE_THRESHOLD;
import static com.fatpiggies.game.model.utils.GameConstants.SNAP_THRESHOLD;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkSyncComponent;
import com.fatpiggies.game.model.ecs.components.physics.VelocityComponent;

/**
 * Handles server reconciliation for the local player's entity in a Client-Side Prediction architecture.
 * <p><b>How it works internally:</b><br>
 * The local player moves instantly based on their joystick input (Client-Side Prediction).
 * However, the authoritative Host periodically sends the "true" state of the game via the
 * {@link NetworkSyncComponent}. This system compares the predicted local {@link TransformComponent}
 * with the server's authoritative target position.
 * <ul>
 * <li><b>Small Error:</b> Ignored. We trust the local prediction to keep movement smooth.</li>
 * <li><b>Medium Error:</b> Rubber-banding. A smooth interpolation (lerp) pulls the player towards the server's position.</li>
 * <li><b>Large Error (Snap):</b> Usually caused by unpredicted collisions (e.g., being bumped by another player).
 * The system instantly teleports the player to the server's position AND overwrites the local
 * {@link VelocityComponent} so the physics engine can seamlessly continue the knockback momentum.</li>
 * </ul>
 * <p><b>Execution Order Note:</b><br>
 * Must be executed <b>after</b> the {@code MovementSystem} on the Client's engine,
 * but <b>before</b> the {@code RenderSystem}.
 */
public class NetworkReconciliationSystem extends IteratingSystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<NetworkSyncComponent> nsm = ComponentMapper.getFor(NetworkSyncComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);

    // Reusable vectors to prevent Garbage Collection memory allocation during gameplay
    private final Vector2 predictedPos = new Vector2();
    private final Vector2 serverPos = new Vector2();
    private final Vector2 correction = new Vector2();


    public NetworkReconciliationSystem() {
        // We only target the LOCAL player. That means the entity MUST have a PlayerInputComponent.
        super(Family.all(
            TransformComponent.class,
            NetworkSyncComponent.class,
            PlayerInputComponent.class,
            VelocityComponent.class
        ).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = tm.get(entity);
        NetworkSyncComponent sync = nsm.get(entity);
        VelocityComponent velocity = vm.get(entity);

        predictedPos.set(transform.x, transform.y);
        serverPos.set(sync.targetX, sync.targetY);

        // Calculate the distance between our predicted reality and the server's absolute reality
        float errorDistance = predictedPos.dst(serverPos);

        if (errorDistance > IGNORE_THRESHOLD) {

            if (errorDistance > SNAP_THRESHOLD) {
                // CRITICAL DESYNC: The server says we were knocked away!
                // Correct the position instantly
                transform.x = sync.targetX;
                transform.y = sync.targetY;

            } else {
                // MEDIUM DESYNC: We are drifting slightly out of sync.
                // Apply a soft correction (Lerp) to realign without a violent camera snap.
                correction.set(serverPos).sub(predictedPos);

                transform.x += correction.x * CORRECTION_LERP * deltaTime;
                transform.y += correction.y * CORRECTION_LERP * deltaTime;
            }
        }
    }
}
