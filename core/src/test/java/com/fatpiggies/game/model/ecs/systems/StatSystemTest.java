package com.fatpiggies.game.model.ecs.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.fatpiggies.game.model.ecs.components.HealthComponent;
import com.fatpiggies.game.model.ecs.components.item.AttachedComponent;
import com.fatpiggies.game.model.ecs.components.modifier.HealthModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.VelocityModifierComponent;
import com.fatpiggies.game.model.ecs.components.physics.AccelerationComponent;
import com.fatpiggies.game.model.ecs.components.physics.MassComponent;
import com.fatpiggies.game.model.ecs.components.physics.VelocityComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatSystemTest {

    private Engine engine;
    private StatSystem statSystem;

    @BeforeEach
    void setUp() {
        // Initialize a fresh engine and system before every single test
        engine = new Engine();
        statSystem = new StatSystem();
        engine.addSystem(statSystem);
    }

    // --- HELPER METHODS ---

    /**
     * Creates a dummy pig with base stats (including health) and adds it to the engine.
     */
    private Entity createPig(float baseVel, float baseAcc, float baseMass) {
        Entity pig = new Entity();

        VelocityComponent vel = new VelocityComponent();
        vel.baseMaxVelocity = baseVel;
        vel.currentMaxVelocity = 0f; // Intentionally set to 0 to test the reset logic

        AccelerationComponent acc = new AccelerationComponent();
        acc.baseMaxAcceleration = baseAcc;
        acc.currentMaxAcceleration = 0f;

        MassComponent mass = new MassComponent();
        mass.baseMass = baseMass;
        mass.currentMass = 0f;

        HealthComponent health = new HealthComponent();
        health.currentLife = 3; // Default starting life for the tests

        pig.add(vel).add(acc).add(mass).add(health);
        engine.addEntity(pig);
        return pig;
    }

    /**
     * Creates a velocity power-up attached to a specific pig.
     */
    private Entity createVelocityPowerup(Entity targetPig, int power) {
        Entity powerup = new Entity();

        // 1. Attach it to the pig
        AttachedComponent attached = new AttachedComponent();
        attached.targetEntity = targetPig;

        // 2. Give it the modifier
        VelocityModifierComponent velMod = new VelocityModifierComponent();
        velMod.power = power;

        powerup.add(attached).add(velMod);
        engine.addEntity(powerup);
        return powerup;
    }

    /**
     * Creates a health power-up attached to a specific pig.
     */
    private Entity createHealthPowerup(Entity targetPig) {
        Entity powerup = new Entity();

        AttachedComponent attached = new AttachedComponent();
        attached.targetEntity = targetPig;

        HealthModifierComponent healthMod = new HealthModifierComponent();

        powerup.add(attached).add(healthMod);
        engine.addEntity(powerup);
        return powerup;
    }

    // --- TESTS ---

    @Test
    void testSystem_WithoutModifiers_ResetsToBaseStats() {
        // 1. Setup: Create a pig with NO power-ups attached
        Entity pig = createPig(100f, 50f, 10f);

        // 2. Execute: Simulate one frame
        engine.update(0.016f);

        // 3. Verify: The system should have copied the base stats to the current stats
        VelocityComponent vel = pig.getComponent(VelocityComponent.class);
        assertEquals(100f, vel.currentMaxVelocity, 0.001f, "Velocity was not reset to base.");
    }

    @Test
    void testSystem_WithSingleModifier_AppliesCorrectly() {
        // 1. Setup: Pig with 100 speed, and a power-up that adds 50 speed
        Entity pig = createPig(100f, 50f, 10f);
        createVelocityPowerup(pig, 50);

        // 2. Execute
        engine.update(0.016f);

        // 3. Verify: Current velocity should be Base (100) + Modifier (50) = 150
        VelocityComponent vel = pig.getComponent(VelocityComponent.class);
        assertEquals(150f, vel.currentMaxVelocity, 0.001f, "Velocity modifier was not applied correctly.");
    }

    @Test
    void testSystem_WithMultipleModifiers_StacksCorrectly() {
        // 1. Setup: Pig with 100 speed, and TWO power-ups (+50 and +25)
        Entity pig = createPig(100f, 50f, 10f);
        createVelocityPowerup(pig, 50);
        createVelocityPowerup(pig, 25);

        // 2. Execute
        engine.update(0.016f);

        // 3. Verify: Current velocity should be Base (100) + Mod1 (50) + Mod2 (25) = 175
        VelocityComponent vel = pig.getComponent(VelocityComponent.class);
        assertEquals(175f, vel.currentMaxVelocity, 0.001f, "Modifiers did not stack properly.");
    }

    @Test
    void testSystem_WhenModifierRemoved_StatsReturnToBase() {
        // 1. Setup
        Entity pig = createPig(100f, 50f, 10f);
        Entity powerup = createVelocityPowerup(pig, 50);

        // 2. Execute Frame 1 (Power-up is active)
        engine.update(0.016f);
        VelocityComponent vel = pig.getComponent(VelocityComponent.class);
        assertEquals(150f, vel.currentMaxVelocity, 0.001f, "Stats should be boosted.");

        // 3. Execute Frame 2 (Simulate LifetimeSystem removing the expired power-up)
        engine.removeEntity(powerup);
        engine.update(0.016f);

        // 4. Verify: Stats must snap back down to the base value
        assertEquals(100f, vel.currentMaxVelocity, 0.001f, "Stats did not reset after power-up removal.");
    }

    @Test
    void testSystem_WithHealthModifier_IncreasesLifeAndRemovesPowerup() {
        // 1. Setup: Create a pig (starts with 3 life) and attach a health powerup
        Entity pig = createPig(100f, 50f, 10f);
        Entity healthPowerup = createHealthPowerup(pig);

        // 2. Execute
        engine.update(0.016f);

        // 3. Verify: Health should be incremented from 3 to 4
        HealthComponent health = pig.getComponent(HealthComponent.class);
        assertEquals(4, health.currentLife, "Health should have incremented by 1.");

        // 4. Verify: The health powerup should be instantly consumed and removed from the engine
        assertFalse(engine.getEntities().contains(healthPowerup, true), "Health powerup must be removed after consumption.");
    }
}
