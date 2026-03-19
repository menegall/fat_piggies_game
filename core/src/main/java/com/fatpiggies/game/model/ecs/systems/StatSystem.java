package com.fatpiggies.game.model.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.fatpiggies.game.model.ecs.components.AccelerationComponent;
import com.fatpiggies.game.model.ecs.components.AccelerationModifierComponent;
import com.fatpiggies.game.model.ecs.components.AttachedComponent;
import com.fatpiggies.game.model.ecs.components.MassComponent;
import com.fatpiggies.game.model.ecs.components.MassModifierComponent;
import com.fatpiggies.game.model.ecs.components.VelocityComponent;
import com.fatpiggies.game.model.ecs.components.VelocityModifierComponent;

/**
 * A pure ECS system responsible for dynamically calculating the current stats
 * (Velocity, Acceleration, Mass) of entities every frame based on active power-ups.
 * <p><b>How it works internally:</b><br>
 * Because power-ups can expire at any moment, this system uses a two-step process every frame:
 * <ol>
 * <li>It resets the {@code currentMaxVelocity}, {@code currentMaxAcceleration}, and {@code currentMass}
 * of all eligible entities back to their respective base values.</li>
 * <li>It iterates over all power-up entities that have an {@link AttachedComponent}.
 * It reads their modifier components (e.g., {@link VelocityModifierComponent})
 * and adds those values to the target entity's current stats.</li>
 * </ol>
 * <p><b>Usage and Initialization:</b><br>
 * You do not need to call the {@code update()} method manually. Simply instantiate
 * the system and add it to your Ashley {@code Engine}. The engine will automatically
 * call {@code update(deltaTime)} during the game loop.
 * <pre>
 * {@code
 * // 1. Create the engine
 * PooledEngine engine = new PooledEngine();
 * // 2. Add the StatSystem BEFORE any systems that use the stats (like MovementSystem)
 * engine.addSystem(new StatSystem());
 * // 3. Add systems that rely on the calculated stats
 * engine.addSystem(new MovementSystem());
 * // 4. In your main game loop:
 * engine.update(deltaTime);
 * }
 * </pre>
 * <p><b>Execution Order Note:</b><br>
 * It is highly recommended to add this system to the engine <b>before</b> your
 * {@code MovementSystem} or {@code PhysicsSystem}. This ensures that when the
 * movement logic runs, it uses the most up-to-date speeds and masses for that frame.
 */
public class StatSystem extends EntitySystem {
    // Mappers for Core Stats
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<AccelerationComponent> am = ComponentMapper.getFor(AccelerationComponent.class);
    private final ComponentMapper<MassComponent> mm = ComponentMapper.getFor(MassComponent.class);

    // Mappers for Modifiers & Attachments
    private final ComponentMapper<AttachedComponent> attachedMapper = ComponentMapper.getFor(AttachedComponent.class);
    private final ComponentMapper<VelocityModifierComponent> vModMapper = ComponentMapper.getFor(VelocityModifierComponent.class);
    private final ComponentMapper<AccelerationModifierComponent> aModMapper = ComponentMapper.getFor(AccelerationModifierComponent.class);
    private final ComponentMapper<MassModifierComponent> mModMapper = ComponentMapper.getFor(MassModifierComponent.class);

    // Arrays to hold the entities we care about
    private ImmutableArray<Entity> entitiesWithStats;
    private ImmutableArray<Entity> activePowerups;

    public StatSystem() {
        // Empty constructor: No external maps or controllers!
    }

    @Override
    public void addedToEngine(Engine engine) {
        // Grab all entities that HAVE stats (e.g., LocalPig, RemotePig)
        entitiesWithStats = engine.getEntitiesFor(Family.all(
            VelocityComponent.class,
            AccelerationComponent.class,
            MassComponent.class).get());

        // Grab all power-ups that are CURRENTLY ATTACHED to someone
        activePowerups = engine.getEntitiesFor(Family.all(AttachedComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        // Step 1: Reset all stats to their base values
        for (int i = 0; i < entitiesWithStats.size(); ++i) {
            Entity pig = entitiesWithStats.get(i);

            VelocityComponent vel = vm.get(pig);
            AccelerationComponent acc = am.get(pig);
            MassComponent mass = mm.get(pig);

            // Reset current values to base values
            vel.currentMaxVelocity = vel.baseMaxVelocity;
            acc.currentMaxAcceleration = acc.baseMaxAcceleration;
            mass.currentMass = mass.baseMass;
        }

        // Step 2: Apply the modifiers from active power-ups
        for (int i = 0; i < activePowerups.size(); ++i) {
            Entity powerup = activePowerups.get(i);
            AttachedComponent attached = attachedMapper.get(powerup);

            // Grab the target directly from the component
            Entity targetPig = attached.targetEntityId;

            // Make sure the pig hasn't been removed from the engine
            if (targetPig != null && !targetPig.isScheduledForRemoval()) {
                // Apply Velocity Modifier
                if (vModMapper.has(powerup)) {
                    VelocityComponent targetVel = vm.get(targetPig);
                    VelocityModifierComponent velMod = vModMapper.get(powerup);
                    targetVel.currentMaxVelocity += velMod.power;
                }
                // Apply Acceleration Modifier
                if (aModMapper.has(powerup)) {
                    AccelerationComponent targetAcc = am.get(targetPig);
                    AccelerationModifierComponent accMod = aModMapper.get(powerup);
                    targetAcc.currentMaxAcceleration += accMod.power;
                }
                // Apply Mass Modifier
                if (mModMapper.has(powerup)) {
                    MassComponent targetMass = mm.get(targetPig);
                    MassModifierComponent massMod = mModMapper.get(powerup);
                    targetMass.currentMass += massMod.power;
                }
            }
        }
    }
}
