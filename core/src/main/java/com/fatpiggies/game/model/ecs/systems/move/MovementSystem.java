package com.fatpiggies.game.model.ecs.systems.move;

import static com.fatpiggies.game.model.utils.GameConstants.GROUND_FRICTION;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkSyncComponent;
import com.fatpiggies.game.model.ecs.components.physics.AccelerationComponent;
import com.fatpiggies.game.model.ecs.components.physics.MassComponent;
import com.fatpiggies.game.model.ecs.components.physics.VelocityComponent;

/**
 * A physics-based movement system responsible for updating the positions of all local entities.
 * <p>
 * <b>How it works internally:</b><br>
 * Unlike traditional arcade movement that directly sets velocity, this system acts as a 2D
 * physics engine to allow for natural knockbacks and weight differences:
 * <ul>
 * <li><b>Player Input (Force):</b> It reads the {@link PlayerInputComponent} and treats the joystick
 * data as a directional driving force.</li>
 * <li><b>Inertia & Mass:</b> It applies Newton's Second Law (Acceleration = Force / Mass) using the
 * {@link MassComponent}. Heavier pigs will accelerate and change directions much slower than light ones.</li>
 * <li><b>Environmental Friction:</b> A constant friction is applied to <i>all</i> moving entities.
 * If a pig drops their joystick or gets knocked away, they won't slide forever; they will
 * skid to a halt naturally.</li>
 * <li><b>Soft Speed Limit:</b> Instead of rigidly capping speed (which would ruin high-speed collision knockbacks),
 * the system applies an aggressive "over-speed drag" if a player exceeds their {@code currentMaxVelocity}.
 * This lets pigs get launched across the screen but quickly regain control.</li>
 * </ul>
 * <p>
 * <b>Target Entities:</b><br>
 * This system processes ANY entity with a {@link TransformComponent} and {@link VelocityComponent}.
 * However, it explicitly <b>excludes</b> entities with a {@link NetworkSyncComponent}. Remote
 * pigs are handled entirely by the {@code NetworkLerpSystem}.
 * <p>
 * <b>Execution Order Note:</b><br>
 * It is crucial that this system is added to the engine <b>after</b> the {@code HostInputSystem}
 * (so it reads fresh inputs) and the {@code StatSystem} (so it uses the latest mass/velocity stats),
 * but <b>before</b> the {@code CollisionSystem} (which will apply the actual bounce forces).
 */
public class MovementSystem extends IteratingSystem {
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<PlayerInputComponent> pim = ComponentMapper.getFor(PlayerInputComponent.class);
    private final ComponentMapper<AccelerationComponent> am = ComponentMapper.getFor(AccelerationComponent.class);
    private final ComponentMapper<MassComponent> mm = ComponentMapper.getFor(MassComponent.class);

    // Reusable vectors to prevent Garbage Collection stutters
    private final Vector2 inputForce = new Vector2();
    private final Vector2 currentVelocity = new Vector2();

    // Ground friction value.
    // Higher = pigs stop faster. Lower = they slide around like on ice.


    public MovementSystem() {
        // Iterate over EVERYTHING that has Position and Velocity.
        // EXCLUDE network pigs (NetworkSyncComponent) because they are moved by the LerpSystem.
        super(Family.all(TransformComponent.class, VelocityComponent.class)
            .exclude(NetworkSyncComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = tm.get(entity);
        VelocityComponent velocity = vm.get(entity);

        currentVelocity.set(velocity.vx, velocity.vy);

        // 1. PLAYER INPUT (Acceleration and Mass)
        if (pim.has(entity) && am.has(entity) && mm.has(entity)) {
            PlayerInputComponent playerInput = pim.get(entity);
            AccelerationComponent acc = am.get(entity);
            MassComponent mass = mm.get(entity);

            // Calculate the directional thrust from the joystick
            // *multiplier => for inverse command
            inputForce.set(playerInput.joystickPourcentageX * playerInput.multiplier,
                playerInput.joystickPourcentageY * playerInput.multiplier);

            if (!inputForce.isZero()) {
                transform.angle = inputForce.angleDeg();
            }

            // Limit to 1f so diagonal movement isn't faster than straight movement
            inputForce.limit(1f).scl(acc.currentMaxAcceleration);

            // Newton's Second Law: Acceleration = Force / Mass
            acc.ax = inputForce.x / mass.currentMass;
            acc.ay = inputForce.y / mass.currentMass;

            // Add the acceleration to the current velocity
            currentVelocity.x += acc.ax * deltaTime;
            currentVelocity.y += acc.ay * deltaTime;
        }

        // 2. FRICTION
        // Constantly decelerates the entity, simulating dragging on the ground.
        // Because it's outside the if-statement, it applies to EVERYONE: players, pushed objects, bounced pigs!
        currentVelocity.x -= currentVelocity.x * GROUND_FRICTION * deltaTime;
        currentVelocity.y -= currentVelocity.y * GROUND_FRICTION * deltaTime;

        // 3. SOFT SPEED LIMIT
        // If the player pushes the joystick, they shouldn't exceed their currentMaxVelocity.
        // But beware! If they get pushed by an enemy at crazy speeds, we DO NOT clamp it instantly.
        // We apply extra drag so friction slows them down naturally.
        float speed = currentVelocity.len();
        if (pim.has(entity) && speed > velocity.currentMaxVelocity) {
            // An elegant trick: apply extra drag if exceeding their OWN max speed.
            // This allows strong collision bounces, but brakes you faster when you are out of control.
            float overSpeedDrag = 10.0f;
            currentVelocity.x -= currentVelocity.x * overSpeedDrag * deltaTime;
            currentVelocity.y -= currentVelocity.y * overSpeedDrag * deltaTime;
        }

        // Save the newly calculated velocity back to the component
        velocity.vx = currentVelocity.x;
        velocity.vy = currentVelocity.y;

        // 4. FINAL MOVEMENT
        transform.x += velocity.vx * deltaTime;
        transform.y += velocity.vy * deltaTime;
    }
}
