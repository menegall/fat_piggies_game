package com.fatpiggies.game.model.ecs.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.fatpiggies.game.model.ecs.components.item.LifetimeComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LifetimeSystemTest {
    private Engine engine;

    @BeforeEach
    void setUp() {
        // Prepare the engine and system
        engine = new Engine();
        engine.addSystem(new LifetimeSystem());
    }

    @Test
    void testLifetime_decreaseLifetime() {
        // Setup
        Entity entity = new Entity();
        LifetimeComponent lifetime = new LifetimeComponent();
        lifetime.timeLeft = 1f;
        entity.add(lifetime);
        engine.addEntity(entity);

        // Execute test
        engine.update(0.1f);

        // Check
        assertEquals(0.9f, lifetime.timeLeft, 0.001f, "Lifetime was not decreased");

    }

    @Test
    void testLifetime_removeEntity() {
        // Setup
        Entity entity = new Entity();
        LifetimeComponent lifetime = new LifetimeComponent();
        lifetime.timeLeft = 0.5f;
        entity.add(lifetime);
        engine.addEntity(entity);

        // Execute test
        engine.update(0.6f);

        // Check
        assertEquals(0, engine.getEntitiesFor(Family.all(LifetimeComponent.class).get()).size(),
            "Entity was not removed");
    }
}
