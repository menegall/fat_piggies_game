package com.fatpiggies.game.model.ecs.systems.move;

import static com.fatpiggies.game.model.utils.GameConstants.FORCE_FACTOR;
import static com.fatpiggies.game.model.utils.GameConstants.GROUND_FRICTION;
import static com.fatpiggies.game.model.utils.GameConstants.INPUT_DEADZONE;
import static com.fatpiggies.game.model.utils.GameConstants.STOP_THRESHOLD;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.physics.AccelerationComponent;
import com.fatpiggies.game.model.ecs.components.physics.MassComponent;
import com.fatpiggies.game.model.ecs.components.physics.VelocityComponent;

public class MovementSystem extends IteratingSystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<PlayerInputComponent> pim = ComponentMapper.getFor(PlayerInputComponent.class);
    private final ComponentMapper<AccelerationComponent> am = ComponentMapper.getFor(AccelerationComponent.class);
    private final ComponentMapper<MassComponent> mm = ComponentMapper.getFor(MassComponent.class);

    private final Vector2 inputDir = new Vector2();
    private final Vector2 currentVelocity = new Vector2();
    private final Vector2 frictionVec = new Vector2();

    public MovementSystem() {
        super(Family.all(TransformComponent.class, VelocityComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = tm.get(entity);
        VelocityComponent velocity = vm.get(entity);

        currentVelocity.set(velocity.vx, velocity.vy);

        boolean canMoveWithInput = pim.has(entity) && am.has(entity) && mm.has(entity);
        boolean hasMovementInput = false;

        if (canMoveWithInput) {
            PlayerInputComponent input = pim.get(entity);
            AccelerationComponent acc = am.get(entity);
            MassComponent mass = mm.get(entity);

            inputDir.set(
                input.joystickPercentageX * input.multiplier,
                input.joystickPercentageY * input.multiplier
            );

            if (inputDir.len2() < INPUT_DEADZONE * INPUT_DEADZONE) {
                inputDir.setZero();
            } else {
                hasMovementInput = true;
            }

            if (hasMovementInput) {
                transform.angle = inputDir.angleDeg();

                inputDir.nor().scl(acc.currentMaxAcceleration);

                acc.ax = (inputDir.x * FORCE_FACTOR) / mass.currentMass;
                acc.ay = (inputDir.y * FORCE_FACTOR) / mass.currentMass;

                currentVelocity.x += acc.ax * deltaTime;
                currentVelocity.y += acc.ay * deltaTime;
            } else {
                acc.ax = 0f;
                acc.ay = 0f;
            }
        }

        // Friction only if no input
        if (!hasMovementInput) {
            float frictionAmount = GROUND_FRICTION * deltaTime;
            float speed = currentVelocity.len();

            if (speed <= frictionAmount) {
                currentVelocity.setZero();
            } else {
                frictionVec.set(currentVelocity).nor().scl(frictionAmount);
                currentVelocity.sub(frictionVec);
            }
        }

        // Limit the speed
        float maxSpeed = velocity.currentMaxVelocity;
        if (currentVelocity.len2() > maxSpeed * maxSpeed) {
            currentVelocity.nor().scl(maxSpeed);
        }

        // Anti-jitter
        if (currentVelocity.len2() < STOP_THRESHOLD * STOP_THRESHOLD) {
            currentVelocity.setZero();
        }

        velocity.vx = currentVelocity.x;
        velocity.vy = currentVelocity.y;

        transform.x += velocity.vx * deltaTime;
        transform.y += velocity.vy * deltaTime;
    }
}
