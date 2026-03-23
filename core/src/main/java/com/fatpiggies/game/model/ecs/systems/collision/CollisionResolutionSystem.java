package com.fatpiggies.game.model.ecs.systems.collision;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.ColliderComponent;
import com.fatpiggies.game.model.ecs.components.collision.CollisionEventComponent;
import com.fatpiggies.game.model.ecs.components.item.AttachedComponent;
import com.fatpiggies.game.model.ecs.components.item.CollectibleComponent;
import com.fatpiggies.game.model.ecs.components.item.LifetimeComponent;
import com.fatpiggies.game.model.ecs.components.modifier.AccelerationModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.MassModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.VelocityModifierComponent;
import com.fatpiggies.game.model.ecs.components.physics.MassComponent;
import com.fatpiggies.game.model.ecs.components.physics.VelocityComponent;
import com.fatpiggies.game.model.utils.GameConstants;

/**
 * A system responsible for resolving collisions and handling collectibles
 * in the ECS architecture.
 * <p>
 * <b>How it works internally:</b><br>
 * The system targets entities that have a {@link CollisionEventComponent}. For
 * each
 * collision event, it checks the involved entities and applies one of the
 * following:
 * <ul>
 * <li><b>Physics collision:</b> If both entities have {@link VelocityComponent}
 * and
 * {@link MassComponent}, a proper knockback is applied based on their relative
 * velocity and mass using a simple impulse formula.</li>
 * <li><b>Collectibles:</b> If one entity has a {@link CollectibleComponent} and
 * the other can move, the system creates a temporary "buff" entity attached to
 * the collector. Any modifier components
 * ({@link AccelerationModifierComponent},
 * {@link VelocityModifierComponent}, {@link MassModifierComponent}) and
 * {@link LifetimeComponent} from the collected item are copied to the buff
 * entity,
 * and the original item is removed from the engine.</li>
 * </ul>
 * <p>
 * <b>Usage and Initialization:</b><br>
 * Add this system to the {@code Engine} for both local and networked gameplay:
 *
 * <pre>
 * {@code
 * engine.addSystem(new CollisionResolutionSystem());
 * }
 * </pre>
 * <p>
 * The system automatically processes collisions each frame for all entities
 * with
 * {@link CollisionEventComponent}.
 * <p>
 * <b>Execution Order Note:</b><br>
 * It should be added <b>after</b> any system that calculates collisions or
 * updates
 * entity positions (e.g., movement or physics systems), but <b>before</b>
 * rendering
 * to ensure knockbacks and collectible effects are applied before drawing.
 */
public class CollisionResolutionSystem extends IteratingSystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<MassComponent> mm = ComponentMapper.getFor(MassComponent.class);
    private final ComponentMapper<CollectibleComponent> cmCollectible = ComponentMapper
        .getFor(CollectibleComponent.class);
    private final ComponentMapper<CollisionEventComponent> cem = ComponentMapper.getFor(CollisionEventComponent.class);
    private final ComponentMapper<AccelerationModifierComponent> am = ComponentMapper
        .getFor(AccelerationModifierComponent.class);
    private final ComponentMapper<VelocityModifierComponent> vmMod = ComponentMapper
        .getFor(VelocityModifierComponent.class);
    private final ComponentMapper<MassModifierComponent> mmMod = ComponentMapper.getFor(MassModifierComponent.class);
    private final ComponentMapper<ColliderComponent> cm = ComponentMapper.getFor(ColliderComponent.class);

    private final Vector2 normal = new Vector2();
    private final Vector2 relativeVelocity = new Vector2();
    private final Vector2 impulse = new Vector2();

    public CollisionResolutionSystem() {
        super(Family.all(CollisionEventComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CollisionEventComponent events = cem.get(entity);

        for (Entity other : events.collidedWith) {

            boolean aCanMove = vm.has(entity);
            boolean bCanMove = vm.has(other);

            boolean aCollectible = cmCollectible.has(entity);
            boolean bCollectible = cmCollectible.has(other);

            // Physics collision (bounce)
            if (aCanMove && bCanMove) {
                applyKnockback(entity, other);
            }

            // Collectible handling
            else if (aCanMove && bCollectible) {
                collect(entity, other);
            } else if (bCanMove && aCollectible) {
                collect(other, entity);
            }
        }
    }

    private void applyKnockback(Entity a, Entity b) {
        TransformComponent ta = tm.get(a);
        TransformComponent tb = tm.get(b);

        VelocityComponent va = vm.get(a);
        VelocityComponent vb = vm.get(b);

        MassComponent ma = mm.get(a);
        MassComponent mb = mm.get(b);

        ColliderComponent ca = cm.get(a);
        ColliderComponent cb = cm.get(b);

        float radiusA = ca != null ? ca.radius : 0.5f;
        float radiusB = cb != null ? cb.radius : 0.5f;

        normal.set(ta.x - tb.x, ta.y - tb.y);
        float distance = normal.len();

        // Avoid division by zero
        if (distance == 0f) {
            normal.set(1, 0); // Arbitrary direction
            distance = 1f;
        } else {
            normal.scl(1f / distance); // normalize
        }

        // Relative velocity
        relativeVelocity.set(va.vx - vb.vx, va.vy - vb.vy);
        float velocityAlongNormal = relativeVelocity.dot(normal);

        // Only resolve if moving towards each other
        if (velocityAlongNormal < 0) {
            // Impulse (velocity-based resolution)
            float impulseScalar = -(1 + GameConstants.PLAYER_BOUNCINESS) * velocityAlongNormal;
            impulseScalar /= (1 / ma.currentMass + 1 / mb.currentMass);
            impulse.set(normal).scl(impulseScalar);

            va.vx += impulse.x / ma.currentMass;
            va.vy += impulse.y / ma.currentMass;

            vb.vx -= impulse.x / mb.currentMass;
            vb.vy -= impulse.y / mb.currentMass;
        }

        // Position correction (to prevent overlap)
        float penetration = (radiusA + radiusB) - distance;

        if (penetration > 0) {
            float invMassA = 1f / ma.currentMass;
            float invMassB = 1f / mb.currentMass;

            final float percent = 0.5f; // push each entity 50% of penetration
            Vector2 correction = new Vector2(normal).scl(penetration / (invMassA + invMassB) * percent);

            ta.x += correction.x * invMassA;
            ta.y += correction.y * invMassA;

            tb.x -= correction.x * invMassB;
            tb.y -= correction.y * invMassB;
        }
    }

    private void collect(Entity collector, Entity item) {
        // Avoid double collection
        if (item.isScheduledForRemoval()) {
            return;
        }

        Entity buff = getEngine().createEntity();

        AttachedComponent attached = getEngine().createComponent(AttachedComponent.class);
        attached.targetEntityId = collector;
        buff.add(attached);

        // Copy acceleration modifier
        if (am.has(item)) {
            AccelerationModifierComponent modifier = am.get(item);
            AccelerationModifierComponent newMod = getEngine().createComponent(AccelerationModifierComponent.class);
            newMod.power = modifier.power;
            buff.add(newMod);
        }

        // Copy velocity modifier
        if (vmMod.has(item)) {
            VelocityModifierComponent modifier = vmMod.get(item);
            VelocityModifierComponent newMod = getEngine().createComponent(VelocityModifierComponent.class);
            newMod.power = modifier.power;
            buff.add(newMod);
        }

        // Copy mass modifier
        if (mmMod.has(item)) {
            MassModifierComponent modifier = mmMod.get(item);
            MassModifierComponent newMod = getEngine().createComponent(MassModifierComponent.class);
            newMod.power = modifier.power;
            buff.add(newMod);
        }

        // Copy lifetime (temporary effect)
        LifetimeComponent lifetime = getEngine().createComponent(LifetimeComponent.class);
        lifetime.timeLeft = item.getComponent(LifetimeComponent.class).timeLeft;
        buff.add(lifetime);

        getEngine().addEntity(buff);
        getEngine().removeEntity(item);
    }
}
