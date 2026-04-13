package com.fatpiggies.game.model.ecs.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.ColliderComponent;
import com.fatpiggies.game.model.ecs.components.collision.CollisionEventComponent;
import com.fatpiggies.game.model.ecs.systems.collision.CollisionDetectionSystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CollisionDetectionSystemTest {

    private Engine engine;
    private CollisionDetectionSystem detectionSystem;

    @BeforeEach
    void setUp() {
        engine = new Engine();
        detectionSystem = new CollisionDetectionSystem();
        engine.addSystem(detectionSystem);
    }

    private Entity createCollidableEntity(float x, float y, float radius) {
        Entity entity = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.x = x;
        transform.y = y;

        ColliderComponent collider = new ColliderComponent();
        collider.radius = radius;

        CollisionEventComponent events = new CollisionEventComponent();

        entity.add(transform).add(collider).add(events);
        engine.addEntity(entity);
        return entity;
    }

    @Test
    void testEntitiesOverlap_RecordsCollision() {
        // Setup: Two entities placed close together (Distance is 1.0, combined radius is 2.0)
        Entity a = createCollidableEntity(0, 0, 1.0f);
        Entity b = createCollidableEntity(1, 0, 1.0f);

        engine.update(0.1f);

        CollisionEventComponent eventsA = a.getComponent(CollisionEventComponent.class);
        CollisionEventComponent eventsB = b.getComponent(CollisionEventComponent.class);

        // System uses pairwise iteration, so A detects B.
        assertEquals(1, eventsA.collidedWith.size(), "Entity A should record 1 collision");
        assertTrue(eventsA.collidedWith.contains(b), "Entity A should have collided with B");

        // Note: Because of how your nested loop is written (j = i + 1), B will not have A in its list.
        // The ResolutionSystem still works perfectly because A will process the pair!
        assertEquals(0, eventsB.collidedWith.size(), "Entity B's list is empty due to one-way pairwise logging");
    }

    @Test
    void testEntitiesDoNotOverlap_NoCollisionRecorded() {
        // Setup: Two entities placed far apart (Distance is 5.0, combined radius is 2.0)
        Entity a = createCollidableEntity(0, 0, 1.0f);
        Entity b = createCollidableEntity(5, 0, 1.0f);

        engine.update(0.1f);

        CollisionEventComponent eventsA = a.getComponent(CollisionEventComponent.class);

        assertTrue(eventsA.collidedWith.isEmpty(), "Entities are too far apart, list should be empty");
    }

    @Test
    void testEntitiesJustTouching_RecordsCollision() {
        // Setup: Edges exactly touching (Distance is 2.0, combined radius is 2.0)
        Entity a = createCollidableEntity(0, 0, 1.0f);
        Entity b = createCollidableEntity(2, 0, 1.0f);

        engine.update(0.1f);

        CollisionEventComponent eventsA = a.getComponent(CollisionEventComponent.class);

        assertEquals(1, eventsA.collidedWith.size(), "Entities exactly touching should collide (<= distance)");
    }

    @Test
    void testPreviousFrameCollisionsAreCleared() {
        // Step 1: Force a collision
        Entity a = createCollidableEntity(0, 0, 1.0f);
        Entity b = createCollidableEntity(1, 0, 1.0f);
        engine.update(0.1f);

        CollisionEventComponent eventsA = a.getComponent(CollisionEventComponent.class);
        assertFalse(eventsA.collidedWith.isEmpty());

        // Step 2: Move them far apart
        a.getComponent(TransformComponent.class).x = 100f;

        // Step 3: Run the system again. The list should be cleared and stay empty.
        engine.update(0.1f);
        assertTrue(eventsA.collidedWith.isEmpty(), "System MUST clear the list at the start of the frame");
    }
}
