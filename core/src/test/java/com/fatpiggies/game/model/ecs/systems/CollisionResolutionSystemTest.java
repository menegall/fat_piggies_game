package com.fatpiggies.game.model.ecs.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.ColliderComponent;
import com.fatpiggies.game.model.ecs.components.collision.CollisionEventComponent;
import com.fatpiggies.game.model.ecs.components.item.AttachedComponent;
import com.fatpiggies.game.model.ecs.components.item.CollectibleComponent;
import com.fatpiggies.game.model.ecs.components.item.LifetimeComponent;
import com.fatpiggies.game.model.ecs.components.modifier.VelocityModifierComponent;
import com.fatpiggies.game.model.ecs.components.physics.MassComponent;
import com.fatpiggies.game.model.ecs.components.physics.VelocityComponent;
import com.fatpiggies.game.model.ecs.systems.collision.CollisionResolutionSystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CollisionResolutionSystemTest {

    private Engine engine;
    private CollisionResolutionSystem resolutionSystem;

    @BeforeEach
    void setUp() {
        engine = new Engine();
        resolutionSystem = new CollisionResolutionSystem();
        engine.addSystem(resolutionSystem);
    }

    private Entity createPig(float x, float vx, float massValue) {
        Entity pig = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.x = x;
        transform.y = 0;

        VelocityComponent velocity = new VelocityComponent();
        velocity.vx = vx;
        velocity.vy = 0;

        MassComponent mass = new MassComponent();
        mass.currentMass = massValue;

        ColliderComponent collider = new ColliderComponent();
        collider.radius = 1.0f;

        CollisionEventComponent events = new CollisionEventComponent();

        pig.add(transform).add(velocity).add(mass).add(collider).add(events);
        engine.addEntity(pig);
        return pig;
    }

    private Entity createPowerUp(float x) {
        Entity item = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.x = x;
        transform.y = 0;

        CollectibleComponent collectible = new CollectibleComponent();

        VelocityModifierComponent vMod = new VelocityModifierComponent();
        vMod.power = 200;

        LifetimeComponent lifetime = new LifetimeComponent();
        lifetime.timeLeft = 5.0f;

        ColliderComponent collider = new ColliderComponent();
        collider.radius = 0.5f;

        CollisionEventComponent events = new CollisionEventComponent();

        item.add(transform).add(collectible).add(vMod).add(lifetime).add(collider).add(events);
        engine.addEntity(item);
        return item;
    }

    @Test
    void testPigVsPig_AppliesKnockback() {
        // Pig A moving Right, Pig B moving Left. They are overlapping.
        Entity pigA = createPig(0f, 100f, 50f);
        Entity pigB = createPig(1f, -100f, 50f);

        // Manually trigger the collision event since we aren't running the DetectionSystem here
        pigA.getComponent(CollisionEventComponent.class).collidedWith.add(pigB);

        engine.update(0.1f);

        VelocityComponent vA = pigA.getComponent(VelocityComponent.class);
        VelocityComponent vB = pigB.getComponent(VelocityComponent.class);

        // Pig A should have bounced backwards (negative velocity)
        assertTrue(vA.vx < 0, "Pig A should be knocked backwards to the left");
        // Pig B should have bounced backwards (positive velocity)
        assertTrue(vB.vx > 0, "Pig B should be knocked backwards to the right");
    }

    @Test
    void testPigVsPig_SeparatesOverlappingEntities() {
        // Entities placed exactly at the same X coordinate
        Entity pigA = createPig(0f, 0f, 50f);
        Entity pigB = createPig(0f, 0f, 50f);

        pigA.getComponent(CollisionEventComponent.class).collidedWith.add(pigB);

        engine.update(0.1f);

        TransformComponent tA = pigA.getComponent(TransformComponent.class);
        TransformComponent tB = pigB.getComponent(TransformComponent.class);

        // The penetration resolution should push them apart
        assertFalse(tA.x == tB.x, "Entities should no longer be at the exact same position");
    }

    @Test
    void testPigVsCollectible_CreatesBuffAndDestroysItem() {
        Entity pig = createPig(0f, 100f, 50f);
        Entity item = createPowerUp(0f);

        pig.getComponent(CollisionEventComponent.class).collidedWith.add(item);

        int entitiesBeforeUpdate = engine.getEntities().size(); // Should be 2 (Pig + Item)

        engine.update(0.1f);

        // 1. The item should be destroyed. The pig remains. A new buff is created.
        assertEquals(entitiesBeforeUpdate, engine.getEntities().size());
        assertFalse(engine.getEntities().contains(item, true), "The original item MUST be removed from the engine");

        // 2. Find the new Buff entity
        Entity buff = engine.getEntitiesFor(Family.all(AttachedComponent.class).get()).first();
        assertNotNull(buff, "A buff entity should have been created");

        // 3. Verify Buff Data
        AttachedComponent attached = buff.getComponent(AttachedComponent.class);
        assertEquals(pig, attached.targetEntity, "Buff must be attached to the Pig that collected it");

        VelocityModifierComponent vMod = buff.getComponent(VelocityModifierComponent.class);
        assertNotNull(vMod, "Buff must have copied the VelocityModifier");
        assertEquals(200f, vMod.power, "Buff modifier power must match the original item");

        LifetimeComponent lifetime = buff.getComponent(LifetimeComponent.class);
        assertNotNull(lifetime, "Buff must have a lifetime component to expire later");
        assertEquals(5.0f, lifetime.timeLeft);
    }
}
