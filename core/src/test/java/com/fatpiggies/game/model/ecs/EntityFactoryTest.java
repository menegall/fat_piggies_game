package com.fatpiggies.game.model.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.ashley.core.Entity;
import com.fatpiggies.game.model.ecs.components.HealthComponent;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.RenderComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.ColliderComponent;
import com.fatpiggies.game.model.ecs.components.collision.CollisionEventComponent;
import com.fatpiggies.game.model.ecs.components.item.CollectibleComponent;
import com.fatpiggies.game.model.ecs.components.item.LifetimeComponent;
import com.fatpiggies.game.model.ecs.components.modifier.InputModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.MassModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.VelocityModifierComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkIdentityComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkSyncComponent;
import com.fatpiggies.game.model.ecs.components.physics.AccelerationComponent;
import com.fatpiggies.game.model.ecs.components.physics.MassComponent;
import com.fatpiggies.game.model.ecs.components.physics.VelocityComponent;
import com.fatpiggies.game.model.utils.PowerUpType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EntityFactoryTest {

    private EntityFactory entityFactory;

    @BeforeEach
    void setUp() {
        entityFactory = new EntityFactory();
    }

    @Test
    void testCreateHostPig_ContainsAllPhysicsAndCollisions() {
        Entity hostPig = entityFactory.createHostPig("host_123", "PIG_RED", 100f, 200f);

        // Verify Identity & Data
        assertNotNull(hostPig.getComponent(NetworkIdentityComponent.class));
        assertEquals("host_123", hostPig.getComponent(NetworkIdentityComponent.class).playerId);

        assertNotNull(hostPig.getComponent(TransformComponent.class));
        assertEquals(100f, hostPig.getComponent(TransformComponent.class).x);
        assertEquals(200f, hostPig.getComponent(TransformComponent.class).y);

        assertNotNull(hostPig.getComponent(HealthComponent.class));
        assertNotNull(hostPig.getComponent(RenderComponent.class));
        assertEquals("PIG_RED", hostPig.getComponent(RenderComponent.class).textureId);

        // Verify Physics (Host MUST have them)
        assertNotNull(hostPig.getComponent(VelocityComponent.class));
        assertNotNull(hostPig.getComponent(AccelerationComponent.class));
        assertNotNull(hostPig.getComponent(MassComponent.class));

        // Verify Inputs (Host MUST have them to process Firebase buffers)
        assertNotNull(hostPig.getComponent(PlayerInputComponent.class));
        assertEquals(1.0f, hostPig.getComponent(PlayerInputComponent.class).multiplier);

        // Verify Collisions (Host MUST have them)
        assertNotNull(hostPig.getComponent(ColliderComponent.class));
        assertNotNull(hostPig.getComponent(CollisionEventComponent.class));

        // Verify it does NOT have client-only components
        assertNull(hostPig.getComponent(NetworkSyncComponent.class));
    }

    @Test
    void testCreateLocalPig_ContainsPhysicsButNoCollisions() {
        Entity localPig = entityFactory.createLocalPig("local_123", "PIG_BLUE", 50f, 50f);

        // Verify Base
        assertNotNull(localPig.getComponent(NetworkIdentityComponent.class));
        assertNotNull(localPig.getComponent(TransformComponent.class));
        assertNotNull(localPig.getComponent(RenderComponent.class));

        // Verify Physics for Client-Side Prediction
        assertNotNull(localPig.getComponent(VelocityComponent.class));
        assertNotNull(localPig.getComponent(MassComponent.class));
        assertNotNull(localPig.getComponent(PlayerInputComponent.class));

        // Verify Network Sync (Must exist to receive server corrections)
        assertNotNull(localPig.getComponent(NetworkSyncComponent.class));

        // CRITICAL: Local Pig must NEVER have collision components
        assertNull(localPig.getComponent(ColliderComponent.class), "Local Pig must not calculate collisions!");
        assertNull(localPig.getComponent(CollisionEventComponent.class));
    }

    @Test
    void testCreateRemotePig_IsAnEmptyPuppet() {
        Entity remotePig = entityFactory.createRemotePig("remote_123", "PIG_GREEN", 0f, 0f);

        // Verify Base
        assertNotNull(remotePig.getComponent(NetworkIdentityComponent.class));
        assertNotNull(remotePig.getComponent(TransformComponent.class));
        assertNotNull(remotePig.getComponent(RenderComponent.class));

        // Verify Network Sync (Used by LerpSystem)
        assertNotNull(remotePig.getComponent(NetworkSyncComponent.class));

        // CRITICAL: Remote Pig must be empty of physics and inputs
        assertNull(remotePig.getComponent(VelocityComponent.class), "Remote pigs should not have physics");
        assertNull(remotePig.getComponent(MassComponent.class));
        assertNull(remotePig.getComponent(PlayerInputComponent.class), "Remote pigs should not have inputs");
        assertNull(remotePig.getComponent(ColliderComponent.class));
    }

    @Test
    void testCreatePowerUp_Apple_GrantsVelocity() {
        Entity apple = entityFactory.createPowerUp(PowerUpType.APPLE);

        // Verify Base Power-up Components
        assertNotNull(apple.getComponent(TransformComponent.class));
        assertNotNull(apple.getComponent(LifetimeComponent.class));
        assertNotNull(apple.getComponent(CollectibleComponent.class));
        assertNotNull(apple.getComponent(ColliderComponent.class));
        assertNotNull(apple.getComponent(RenderComponent.class));

        // Verify Specific Buff
        VelocityModifierComponent vMod = apple.getComponent(VelocityModifierComponent.class);
        assertNotNull(vMod);
        assertTrue(vMod.power > 0, "Apple should grant positive velocity");

        // Ensure no other buffs are attached
        assertNull(apple.getComponent(MassModifierComponent.class));
        assertNull(apple.getComponent(InputModifierComponent.class));
    }

    @Test
    void testCreatePowerUp_Beer_InversesInput() {
        Entity beer = entityFactory.createPowerUp(PowerUpType.BEER);

        InputModifierComponent iMod = beer.getComponent(InputModifierComponent.class);
        assertNotNull(iMod);
        assertEquals(-1.0f, iMod.power, "Beer should inverse the input multiplier");

        assertNull(beer.getComponent(VelocityModifierComponent.class));
        assertNull(beer.getComponent(MassModifierComponent.class));
    }

    @Test
    void testCreatePowerUp_Donut_GrantsMassButReducesSpeed() {
        Entity donut = entityFactory.createPowerUp(PowerUpType.DONUT);

        MassModifierComponent mMod = donut.getComponent(MassModifierComponent.class);
        assertNotNull(mMod);
        assertTrue(mMod.power > 0, "Donut should increase mass");

        VelocityModifierComponent vMod = donut.getComponent(VelocityModifierComponent.class);
        assertNotNull(vMod);
        assertTrue(vMod.power < 0, "Donut should decrease velocity");

        assertNull(donut.getComponent(InputModifierComponent.class));
    }
}
