package com.fatpiggies.game.model;

import static com.fatpiggies.game.model.utils.GameConstants.BASE_LIFE;
import static com.fatpiggies.game.model.utils.GameConstants.BOTTOM_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.LEFT_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.PLAYER_BASE_ACCELERATION;
import static com.fatpiggies.game.model.utils.GameConstants.PLAYER_BASE_MASS;
import static com.fatpiggies.game.model.utils.GameConstants.PLAYER_BASE_VELOCITY;
import static com.fatpiggies.game.model.utils.GameConstants.PLAYER_COLLISION_RADIUS;
import static com.fatpiggies.game.model.utils.GameConstants.POWERUP_COLLISION_RADIUS;
import static com.fatpiggies.game.model.utils.GameConstants.POWERUP_MAX_LIFETIME;
import static com.fatpiggies.game.model.utils.GameConstants.POWERUP_MIN_LIFETIME;
import static com.fatpiggies.game.model.utils.GameConstants.POWER_MASS_MODIFIER;
import static com.fatpiggies.game.model.utils.GameConstants.POWER_VELOCITY_MODIFIER;
import static com.fatpiggies.game.model.utils.GameConstants.RIGHT_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.TOP_BOUND;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
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
import com.fatpiggies.game.model.ecs.systems.LifetimeSystem;
import com.fatpiggies.game.model.ecs.systems.PowerUpSpawnerSystem;
import com.fatpiggies.game.model.ecs.systems.StatSystem;
import com.fatpiggies.game.model.ecs.systems.collision.ArenaBoundsSystem;
import com.fatpiggies.game.model.ecs.systems.collision.CollisionDetectionSystem;
import com.fatpiggies.game.model.ecs.systems.collision.CollisionResolutionSystem;
import com.fatpiggies.game.model.ecs.systems.move.MovementSystem;
import com.fatpiggies.game.model.ecs.systems.move.NetworkLerpSystem;
import com.fatpiggies.game.model.ecs.systems.move.NetworkReconciliationSystem;
import com.fatpiggies.game.model.ecs.systems.move.RespawnSystem;
import com.fatpiggies.game.model.utils.PowerUpType;


public class GameWorld {
    private final Engine engine;
    private Entity localPlayer;

    public GameWorld(Engine engine) {
        this.engine = engine;

        // add all systems to engine
        engine.addSystem(new ArenaBoundsSystem());
        engine.addSystem(new CollisionDetectionSystem());
        engine.addSystem(new CollisionResolutionSystem());
        engine.addSystem(new MovementSystem());
        engine.addSystem(new NetworkLerpSystem());
        engine.addSystem(new NetworkReconciliationSystem());
        engine.addSystem(new RespawnSystem());
        engine.addSystem(new LifetimeSystem());
        engine.addSystem(new PowerUpSpawnerSystem());
        engine.addSystem(new StatSystem());
    }

    /**
     * Calls update function of engine to update all systems in the order they were added.
     * @param dt time passed since the last frame
     */
    public void update(float dt) {
        engine.update(dt);
    }

    /**
     * Updates the local players pigs position.
     * @param x x direction
     * @param y y direction
     */
    public void movePlayerPig(int x, int y) {
        PlayerInputComponent input = localPlayer.getComponent(PlayerInputComponent.class);
        if(input != null) {
            input.joystickPercentageX = x;
            input.joystickPercentageY = y;
        }
    }

    /**
     * Creates a pig for the Host. It contains ALL physics and collision logic.
     *
     * @param networkId Unique ID of the player
     * @param textureId Texture ID of the pig
     * @param startX    Initial X position
     * @param startY    Initial Y position
     */
    public void createHostPig(String networkId, String textureId, float startX, float startY) {
        Entity entity = engine.createEntity();

        // Identity and Base Data
        NetworkIdentityComponent netId = new NetworkIdentityComponent();
        netId.playerId = networkId;

        TransformComponent transform = new TransformComponent();
        transform.x = startX;
        transform.y = startY;

        HealthComponent health = new HealthComponent();
        health.currentLife = BASE_LIFE;

        attachPhysicComponents(entity);

        PlayerInputComponent input = new PlayerInputComponent();
        input.multiplier = 1;

        ColliderComponent collider = new ColliderComponent();
        collider.radius = PLAYER_COLLISION_RADIUS;
        CollisionEventComponent collisions = new CollisionEventComponent();

        RenderComponent render = new RenderComponent();
        render.textureId = textureId;

        entity.add(netId).add(transform).add(health).add(render)
            .add(input).add(collider).add(collisions);

        localPlayer = entity;
        this.engine.addEntity(entity);
    }


    /**
     * Creates a local pig for the Client. It contains ALL physics.
     * The Client will control this pig with his joystick.
     *
     * @param playerId  Unique ID of the player
     * @param textureId Texture ID of the pig
     * @param startX    Initial X position
     * @param startY    Initial Y position
     */
    public void createLocalPig(String playerId, String textureId, float startX, float startY) {
        Entity entity = engine.createEntity();

        NetworkIdentityComponent netId = new NetworkIdentityComponent();
        netId.playerId = playerId;

        TransformComponent transform = new TransformComponent();
        transform.x = startX;
        transform.y = startY;

        HealthComponent health = new HealthComponent();

        attachPhysicComponents(entity);

        PlayerInputComponent input = new PlayerInputComponent();
        input.multiplier = 1;


        NetworkSyncComponent sync = new NetworkSyncComponent();

        RenderComponent graphic = new RenderComponent();
        graphic.textureId = textureId;

        entity.add(netId).add(transform).add(health)
            .add(input).add(sync).add(graphic);

        localPlayer = entity;
        this.engine.addEntity(entity);
    }

    /**
     * Creates a remote pig for the Client.
     * This pig will be controlled by the Network Lerp System .
     *
     * @param playerId  Unique ID of the player
     * @param textureId Texture ID of the pig
     * @param startX    Initial X position
     * @param startY    Initial Y position
     */
    public void createRemotePig(String playerId, String textureId, float startX, float startY) {
        Entity entity = engine.createEntity();

        NetworkIdentityComponent netId = new NetworkIdentityComponent();
        netId.playerId = playerId;

        TransformComponent transform = new TransformComponent();
        transform.x = startX;
        transform.y = startY;

        NetworkSyncComponent sync = new NetworkSyncComponent();

        RenderComponent graphic = new RenderComponent();
        graphic.textureId = textureId;

        entity.add(netId).add(transform).add(sync).add(graphic);

        this.engine.addEntity(entity);
    }

    /**
     * Creates a collectible power-up for the Host.
     * Randomizes position and lifetime.
     *
     * @param type The type of power-up to create, follow the PowerUpType enum.
     */
    public void createPowerUp(PowerUpType type) {
        Entity entity = engine.createEntity();

        TransformComponent transform = new TransformComponent();
        transform.x = MathUtils.random(LEFT_BOUND, RIGHT_BOUND);
        transform.y = MathUtils.random(BOTTOM_BOUND, TOP_BOUND);

        LifetimeComponent lifetime = new LifetimeComponent();
        lifetime.timeLeft = MathUtils.random(POWERUP_MIN_LIFETIME, POWERUP_MAX_LIFETIME);

        ColliderComponent collider = new ColliderComponent();
        collider.radius = POWERUP_COLLISION_RADIUS;
        CollisionEventComponent collisions = new CollisionEventComponent();

        CollectibleComponent collectible = new CollectibleComponent();

        // Attach base components
        entity.add(transform).add(lifetime).add(collider)
            .add(collisions).add(collectible);

        attachModifierAndRender(entity, type);
        this.engine.addEntity(entity);
    }

    /**
     * Attaches a specific modifier component and the corresponding RenderComponent
     * based on the type.
     *
     * @param entity The entity to attach the components to.
     * @param type   The type of modifier to attach (e.g., BEER, DONUT, LIFE).
     */
    private void attachModifierAndRender(Entity entity, PowerUpType type) {
        RenderComponent render = new RenderComponent();

        switch (type) {
            case BEER: {
                InputModifierComponent inputMod = new InputModifierComponent();
                // TODO: use enum from view: render.textureId = "201";
                entity.add(inputMod);
                break;
            }

            case DONUT: {
                MassModifierComponent massMod = new MassModifierComponent();
                massMod.power = POWER_MASS_MODIFIER;
                VelocityModifierComponent velocityMod = new VelocityModifierComponent();
                velocityMod.power = -POWER_VELOCITY_MODIFIER;
                // TODO: use enum from view for render: render.textureId = "201";
                entity.add(massMod).add(velocityMod);
                break;
            }

            case LIFE: {
                // TODO Implement modifier for life
//                MassModifierComponent massMod = new MassModifierComponent();
//                massMod.power = POWER_MASS_MODIFIER;
//                entity.add(massMod);
                // TODO: use enum from view for render: render.textureId = "201";
                break;
            }

            case APPLE: {
                VelocityModifierComponent velocityMod = new VelocityModifierComponent();
                velocityMod.power = POWER_VELOCITY_MODIFIER;
                entity.add(velocityMod);
                // TODO: use enum from view for render: render.textureId = "201";
                break;
            }

            default:
                break;
        }
        entity.add(render);
    }

    /**
     * Attaches the base physics components to the entity.
     *
     * @param entity The entity to attach the components to.
     */
    private void attachPhysicComponents(Entity entity) {
        VelocityComponent velocity = new VelocityComponent();
        velocity.baseMaxVelocity = PLAYER_BASE_VELOCITY;
        velocity.currentMaxVelocity = PLAYER_BASE_VELOCITY;

        AccelerationComponent accel = new AccelerationComponent();
        accel.baseMaxAcceleration = PLAYER_BASE_ACCELERATION;
        accel.currentMaxAcceleration = PLAYER_BASE_ACCELERATION;

        MassComponent mass = new MassComponent();
        mass.baseMass = PLAYER_BASE_MASS;
        mass.currentMass = PLAYER_BASE_MASS;

        entity.add(velocity).add(accel).add(mass);
    }
}
