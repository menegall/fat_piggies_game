package com.fatpiggies.game.model.ecs.systems.collision;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.ColliderComponent;
import com.fatpiggies.game.model.ecs.components.collision.CollisionEventComponent;


public class CollisionDetectionSystem extends IteratingSystem {

    private final ComponentMapper<TransformComponent> tm =
            ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<ColliderComponent> cm =
            ComponentMapper.getFor(ColliderComponent.class);
    private final ComponentMapper<CollisionEventComponent> cem =
            ComponentMapper.getFor(CollisionEventComponent.class);

    private ImmutableArray<Entity> entities;

    public CollisionDetectionSystem() {
        super(Family.all(
                TransformComponent.class,
                ColliderComponent.class,
                CollisionEventComponent.class
        ).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(getFamily());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CollisionEventComponent events = cem.get(entity);
        events.collidedWith.clear(); // reset every frame

        TransformComponent ta = tm.get(entity);
        ColliderComponent ca = cm.get(entity);

        for (int i = 0; i < entities.size(); i++) {
            Entity other = entities.get(i);

            if (other == entity || entity.hashCode() >= other.hashCode()) continue;

            TransformComponent tb = tm.get(other);
            ColliderComponent cb = cm.get(other);

            if (circlesOverlap(ta, ca, tb, cb)) {
                events.collidedWith.add(other);

                // also add reverse (important!)
                CollisionEventComponent otherEvents = cem.get(other);
                otherEvents.collidedWith.add(entity);
            }
        }
    }

    private boolean circlesOverlap(TransformComponent ta, ColliderComponent ca,
                                   TransformComponent tb, ColliderComponent cb) {

        float dx = (float)(ta.x - tb.x);
        float dy = (float)(ta.y - tb.y);

        float distanceSquared = dx * dx + dy * dy;
        float radiusSum = (float)(ca.radius + cb.radius);

        return distanceSquared <= radiusSum * radiusSum;
    }
}
