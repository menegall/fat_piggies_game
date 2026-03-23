package com.fatpiggies.game.model.ecs.systems.collision;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.ColliderComponent;
import com.fatpiggies.game.model.ecs.components.collision.CollisionEventComponent;

public class CollisionDetectionSystem extends EntitySystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<ColliderComponent> cm = ComponentMapper.getFor(ColliderComponent.class);
    private final ComponentMapper<CollisionEventComponent> cem = ComponentMapper.getFor(CollisionEventComponent.class);

    private ImmutableArray<Entity> entities;

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(
                Family.all(
                        TransformComponent.class,
                        ColliderComponent.class,
                        CollisionEventComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {

        // reset
        for (int i = 0; i < entities.size(); i++) {
            cem.get(entities.get(i)).collidedWith.clear();
        }

        // pairwise collision
        for (int i = 0; i < entities.size(); i++) {
            Entity a = entities.get(i);

            TransformComponent ta = tm.get(a);
            ColliderComponent ca = cm.get(a);
            CollisionEventComponent ea = cem.get(a);

            for (int j = i + 1; j < entities.size(); j++) {
                Entity b = entities.get(j);

                TransformComponent tb = tm.get(b);
                ColliderComponent cb = cm.get(b);
                CollisionEventComponent eb = cem.get(b);

                if (circlesOverlap(ta, ca, tb, cb)) {
                    ea.collidedWith.add(b);
                }
            }
        }
    }

    private boolean circlesOverlap(TransformComponent ta, ColliderComponent ca,
            TransformComponent tb, ColliderComponent cb) {

        float dx = ta.x - tb.x;
        float dy = ta.y - tb.y;

        float distanceSquared = dx * dx + dy * dy;
        float radiusSum = ca.radius + cb.radius;

        return distanceSquared <= radiusSum * radiusSum;
    }
}
