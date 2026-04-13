package com.fatpiggies.game.model.ecs.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.fatpiggies.game.model.ecs.components.RenderComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.ColliderComponent;
import com.fatpiggies.game.model.ecs.components.item.CollectibleComponent;
import com.fatpiggies.game.model.ecs.components.item.LifetimeComponent;
import com.fatpiggies.game.model.ecs.components.modifier.AccelerationModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.MassModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.VelocityModifierComponent;
import com.fatpiggies.game.model.utils.GameConstants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the PowerUpSpawnerSystem.
 * Verifies that entities are spawned only when the interval is reached,
 * and that they contain the correct base components and exactly one modifier.
 */
public class PowerUpSpawnerSystemTest {

    private Engine engine;
    private PowerUpSpawnerSystem spawnerSystem;

    @BeforeEach
    void setUp() {
        engine = new Engine();
        spawnerSystem = new PowerUpSpawnerSystem();
        engine.addSystem(spawnerSystem);
    }

    @Test
    void testNoPowerUpSpawnedBeforeInterval() {
        // Execute: Update engine for less time than the spawn interval
        engine.update(GameConstants.POWERUP_SPAWN_INTERVAL - 1.0f);

        // Check: Ensure no entities with CollectibleComponent exist
        ImmutableArray<Entity> powerUps = engine.getEntitiesFor(Family.all(CollectibleComponent.class).get());
        assertEquals(0, powerUps.size(), "No power-ups should spawn before the interval is reached");
    }

    @Test
    void testPowerUpSpawnedAfterInterval() {
        // Execute: Update engine for exactly the interval time (plus a tiny delta to be safe)
        engine.update(GameConstants.POWERUP_SPAWN_INTERVAL + 0.1f);

        // Check: Ensure exactly one power-up entity was spawned
        ImmutableArray<Entity> powerUps = engine.getEntitiesFor(Family.all(CollectibleComponent.class).get());
        assertEquals(1, powerUps.size(), "Exactly one power-up should be spawned after the interval");
    }

    @Test
    void testSpawnedPowerUpHasCorrectBaseComponentsAndPosition() {
        // Execute
        engine.update(GameConstants.POWERUP_SPAWN_INTERVAL + 0.1f);
        Entity powerUp = engine.getEntitiesFor(Family.all(CollectibleComponent.class).get()).first();

        // Check Base Components
        assertNotNull(powerUp.getComponent(TransformComponent.class), "Must have TransformComponent");
        assertNotNull(powerUp.getComponent(LifetimeComponent.class), "Must have LifetimeComponent");
        assertNotNull(powerUp.getComponent(RenderComponent.class), "Must have RenderComponent");
        assertNotNull(powerUp.getComponent(ColliderComponent.class), "Must have ColliderComponent");
        assertNotNull(powerUp.getComponent(CollectibleComponent.class), "Must have CollectibleComponent");

        // Check Position Bounds
        TransformComponent transform = powerUp.getComponent(TransformComponent.class);
        assertTrue(transform.x >= GameConstants.LEFT_BOUND && transform.x <= GameConstants.RIGHT_BOUND,
            "X position must be within arena bounds");
        assertTrue(transform.y >= GameConstants.BOTTOM_BOUND && transform.y <= GameConstants.TOP_BOUND,
            "Y position must be within arena bounds");

        // Check Lifetime Bounds
        LifetimeComponent lifetime = powerUp.getComponent(LifetimeComponent.class);
        assertTrue(lifetime.timeLeft >= GameConstants.POWERUP_MIN_LIFETIME && lifetime.timeLeft <= GameConstants.POWERUP_MAX_LIFETIME,
            "Lifetime must be within configured min and max bounds");
    }

    @Test
    void testSpawnedPowerUpHasExactlyOneModifier() {
        // Execute
        engine.update(GameConstants.POWERUP_SPAWN_INTERVAL + 0.1f);
        Entity powerUp = engine.getEntitiesFor(Family.all(CollectibleComponent.class).get()).first();

        // Check Modifiers
        boolean hasVelocityMod = powerUp.getComponent(VelocityModifierComponent.class) != null;
        boolean hasAccelerationMod = powerUp.getComponent(AccelerationModifierComponent.class) != null;
        boolean hasMassMod = powerUp.getComponent(MassModifierComponent.class) != null;

        // XOR equivalent for 3 booleans: count how many are true
        int modifierCount = 0;
        if (hasVelocityMod) modifierCount++;
        if (hasAccelerationMod) modifierCount++;
        if (hasMassMod) modifierCount++;

        assertEquals(1, modifierCount, "The power-up must have exactly ONE type of modifier component assigned");
    }
}
