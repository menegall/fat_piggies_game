package com.fatpiggies.game.model.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.VelocityComponent;

/**
 * Responsible for moving the local player’s entity by applying physical
 * laws (velocity and acceleration) based on the PlayerInputComponent. It
 * provides
 * immediate feedback to the player (Client-Side Prediction).
 */
public class MovementSystem extends IteratingSystem {
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<VelocityComponent> vsm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<PlayerInputComponent> pim = ComponentMapper.getFor(PlayerInputComponent.class);

    public MovementSystem() {
        // We take all with TransformComponent, VelocityComponent and PlayerInputComponent
        super(Family.all(TransformComponent.class, VelocityComponent.class, PlayerInputComponent.class).get());

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = tm.get(entity);
        VelocityComponent velocity = vsm.get(entity);
        PlayerInputComponent playerInput = pim.get(entity);

        Vector2 desiredVelocity = new Vector2(playerInput.joystickPourcentageX, playerInput.joystickPourcentageY).nor()
                .scl(velocity.currentMaxVelocity);
        velocity.vx = desiredVelocity.x;
        velocity.vy = desiredVelocity.y;

        transform.x += velocity.vx * deltaTime;
        transform.y += velocity.vy * deltaTime;
    }
}
