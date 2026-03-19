package com.fatpiggies.game.model.ecs.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.fatpiggies.game.model.ecs.components.*;
/**
 * A system responsible for resolving collisions and handling collectibles
 * in the ECS architecture.
 * <p><b>How it works internally:</b><br>
 * The system targets entities that have a {@link CollisionEventComponent}. For each
 * collision event, it checks the involved entities and applies one of the following:
 * <ul>
 *     <li><b>Physics collision:</b> If both entities have {@link VelocityComponent} and
 *     {@link MassComponent}, a proper knockback is applied based on their relative
 *     velocity and mass using a simple impulse formula.</li>
 *     <li><b>Collectibles:</b> If one entity has a {@link CollectibleComponent} and
 *     the other can move, the system creates a temporary "buff" entity attached to
 *     the collector. Any modifier components ({@link AccelerationModifierComponent},
 *     {@link VelocityModifierComponent}, {@link MassModifierComponent}) and
 *     {@link LifetimeComponent} from the collected item are copied to the buff entity,
 *     and the original item is removed from the engine.</li>
 * </ul>
 * <p><b>Usage and Initialization:</b><br>
 * Add this system to the {@code Engine} for both local and networked gameplay:
 * <pre>
 * {@code
 * engine.addSystem(new CollisionResolutionSystem());
 * }
 * </pre>
 * The system automatically processes collisions each frame for all entities with
 * {@link CollisionEventComponent}.
 * <p><b>Execution Order Note:</b><br>
 * It should be added <b>after</b> any system that calculates collisions or updates
 * entity positions (e.g., movement or physics systems), but <b>before</b> rendering
 * to ensure knockbacks and collectible effects are applied before drawing.
 */
public class CollisionResolutionSystem extends IteratingSystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<MassComponent> mm = ComponentMapper.getFor(MassComponent.class);
    private final ComponentMapper<CollectibleComponent> cmCollectible = ComponentMapper.getFor(CollectibleComponent.class);
    private final ComponentMapper<CollisionEventComponent> cem = ComponentMapper.getFor(CollisionEventComponent.class);
    private final ComponentMapper<AccelerationModifierComponent> am = ComponentMapper.getFor(AccelerationModifierComponent.class);
    private final ComponentMapper<VelocityModifierComponent> vmMod = ComponentMapper.getFor(VelocityModifierComponent.class);
    private final ComponentMapper<MassModifierComponent> mmMod = ComponentMapper.getFor(MassModifierComponent.class);

    public CollisionResolutionSystem() {
        super(Family.all(CollisionEventComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CollisionEventComponent events = cem.get(entity);

        for (Entity other : events.collidedWith) {

            boolean aCanMove = vm.has(entity);
            boolean bCanMove = vm.has(other);

            boolean aHasMass = mm.has(entity);
            boolean bHasMass = mm.has(other);

            boolean aCollectible = cmCollectible.has(entity);
            boolean bCollectible = cmCollectible.has(other);

            // Physics collision (bounce)
            if (aCanMove && bCanMove && aHasMass && bHasMass) {
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

        Vector2 normal = new Vector2((float)(ta.x - tb.x), (float)(ta.y - tb.y)).nor();

        Vector2 relativeVelocity = new Vector2(va.vx - vb.vx, va.vy - vb.vy);
        float velocityAlongNormal = relativeVelocity.dot(normal);

        if (velocityAlongNormal > 0)
            return;

        float restitution = 0.8f; // bounciness
        float impulseScalar = -(1 + restitution) * velocityAlongNormal;
        impulseScalar /= (1 / ma.currentMass + 1 / mb.currentMass);

        Vector2 impulse = normal.scl(impulseScalar);

        va.vx += impulse.x / ma.currentMass;
        va.vy += impulse.y / ma.currentMass;

        vb.vx -= impulse.x / mb.currentMass;
        vb.vy -= impulse.y / mb.currentMass;
    }

    private void collect(Entity collector, Entity item) {

    Entity buff = getEngine().createEntity();

    // Attach to player
    AttachedComponent attached = getEngine().createComponent(AttachedComponent.class);
    attached.targetEntityId = (int) collector.hashCode(); // or better: network id
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
