package com.fatpiggies.game.model;

import static com.fatpiggies.game.model.utils.GameConstants.BASE_LIFE;
import static com.fatpiggies.game.model.utils.GameConstants.BOTTOM_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.LEFT_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.PIG_ANGLE_OFFSET;
import static com.fatpiggies.game.model.utils.GameConstants.PIG_HEIGHT;
import static com.fatpiggies.game.model.utils.GameConstants.PIG_WIDTH;
import static com.fatpiggies.game.model.utils.GameConstants.PLAYER_BASE_ACCELERATION;
import static com.fatpiggies.game.model.utils.GameConstants.PLAYER_BASE_MASS;
import static com.fatpiggies.game.model.utils.GameConstants.PLAYER_BASE_VELOCITY;
import static com.fatpiggies.game.model.utils.GameConstants.PLAYER_COLLISION_RADIUS;
import static com.fatpiggies.game.model.utils.GameConstants.POWERUP_COLLISION_RADIUS;
import static com.fatpiggies.game.model.utils.GameConstants.POWERUP_MAX_LIFETIME;
import static com.fatpiggies.game.model.utils.GameConstants.POWERUP_MIN_LIFETIME;
import static com.fatpiggies.game.model.utils.GameConstants.POWER_MASS_MODIFIER;
import static com.fatpiggies.game.model.utils.GameConstants.POWER_UP_ANGLE_OFFSET;
import static com.fatpiggies.game.model.utils.GameConstants.POWER_UP_HEIGHT;
import static com.fatpiggies.game.model.utils.GameConstants.POWER_UP_WIDTH;
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
import com.fatpiggies.game.model.ecs.components.collision.NeedsRespawnComponent;
import com.fatpiggies.game.model.ecs.components.item.CollectibleComponent;
import com.fatpiggies.game.model.ecs.components.item.LifetimeComponent;
import com.fatpiggies.game.model.ecs.components.modifier.HealthModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.InputModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.MassModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.VelocityModifierComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkIdentityComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkSyncComponent;
import com.fatpiggies.game.model.ecs.components.physics.AccelerationComponent;
import com.fatpiggies.game.model.ecs.components.physics.MassComponent;
import com.fatpiggies.game.model.ecs.components.physics.VelocityComponent;
import com.fatpiggies.game.model.utils.PowerUpType;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.view.TextureId;

import java.util.Map;



public class GameWorld implements IReadOnlyGameWorld{
    private Engine engine;
    private Entity localPlayer;
    private String lobbyId;
    private String lobbyCode;
    private Map<String, PlayerSetup> playersSetup;

    public GameWorld(Engine engine) {
        this.engine = engine;
    }

    /**
     * Calls update function of engine to update all systems in the order they were added.
     *
     * @param dt time passed since the last frame
     */
    public void update(float dt) {
        engine.update(dt);
    }


    public void updatePlayerInput(float x, float y) {
        if (localPlayer == null) return;

        PlayerInputComponent input = localPlayer.getComponent(PlayerInputComponent.class);
        if (input != null) {
            input.joystickPercentageX = x;
            input.joystickPercentageY = y;
        }
    }

    /**
     * Creates a pig for the Host. It contains ALL physics and collision logic.
     *
     * @param networkId Unique ID of the player
     * @param textureId Texture ID of the pig
     */
    public void createHostPig(String networkId, TextureId textureId) {
        Entity entity = engine.createEntity();

        // Identity and Base Data
        NetworkIdentityComponent netId = new NetworkIdentityComponent();
        netId.playerId = networkId;

        TransformComponent transform = new TransformComponent();
        NeedsRespawnComponent spawn = new NeedsRespawnComponent();

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
        render.width = PIG_WIDTH;
        render.height = PIG_HEIGHT;
        render.angleOffset = PIG_ANGLE_OFFSET;

        entity.add(netId).add(transform).add(health).add(render)
            .add(input).add(collider).add(collisions).add(spawn);

        localPlayer = entity;
        this.engine.addEntity(entity);
    }


    /**
     * Creates a local pig for the Client. It contains ALL physics.
     * The Client will control this pig with his joystick.
     *
     * @param playerId  Unique ID of the player
     * @param textureId Texture ID of the pig
     */
    public void createLocalPig(String playerId, TextureId textureId) {
        Entity entity = engine.createEntity();

        NetworkIdentityComponent netId = new NetworkIdentityComponent();
        netId.playerId = playerId;

        TransformComponent transform = new TransformComponent();

        HealthComponent health = new HealthComponent();

        attachPhysicComponents(entity);

        PlayerInputComponent input = new PlayerInputComponent();
        input.multiplier = 1;


        NetworkSyncComponent sync = new NetworkSyncComponent();

        RenderComponent render = new RenderComponent();
        render.textureId = textureId;
        render.width = PIG_WIDTH;
        render.height = PIG_HEIGHT;
        render.angleOffset = PIG_ANGLE_OFFSET;

        entity.add(netId).add(transform).add(health)
            .add(input).add(sync).add(render);

        localPlayer = entity;
        this.engine.addEntity(entity);
    }

    /**
     * Creates a remote pig for the Client.
     * This pig will be controlled by the Network Lerp System .
     *
     * @param playerId  Unique ID of the player
     * @param textureId Texture ID of the pig
     */
    public void createRemotePig(String playerId, TextureId textureId) {
        Entity entity = engine.createEntity();

        NetworkIdentityComponent netId = new NetworkIdentityComponent();
        netId.playerId = playerId;

        TransformComponent transform = new TransformComponent();

        NetworkSyncComponent sync = new NetworkSyncComponent();

        RenderComponent render = new RenderComponent();
        render.textureId = textureId;
        render.width = PIG_WIDTH;
        render.height = PIG_HEIGHT;
        render.angleOffset = PIG_ANGLE_OFFSET;


        entity.add(netId).add(transform).add(sync).add(render);

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

        // From same texture length
        render.width = POWER_UP_WIDTH;
        render.height = POWER_UP_HEIGHT;
        render.angleOffset = POWER_UP_ANGLE_OFFSET;

        switch (type) {
            case BEER: {
                InputModifierComponent inputMod = new InputModifierComponent();
                render.textureId = TextureId.BEER;
                entity.add(inputMod);
                break;
            }

            case DONUT: {
                MassModifierComponent massMod = new MassModifierComponent();
                massMod.power = POWER_MASS_MODIFIER;
                VelocityModifierComponent velocityMod = new VelocityModifierComponent();
                velocityMod.power = -POWER_VELOCITY_MODIFIER;
                render.textureId = TextureId.DONUT;
                entity.add(massMod).add(velocityMod);
                break;
            }

            case LIFE: {
                HealthModifierComponent healthMod = new HealthModifierComponent();
                entity.add(healthMod);
                render.textureId = TextureId.LIFE;
                break;
            }

            case APPLE: {
                VelocityModifierComponent velocityMod = new VelocityModifierComponent();
                velocityMod.power = POWER_VELOCITY_MODIFIER;
                entity.add(velocityMod);
                render.textureId = TextureId.APPLE;
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

    @Override
    public Engine getEngine() {
        return engine;
    }

    @Override
    public Entity getLocalPlayer() {
        return localPlayer;
    }

    public void setLocalPlayer(Entity localPlayer) {
        this.localPlayer = localPlayer;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public void setLobbyCode(String lobbyCode) {
        this.lobbyCode = lobbyCode;
    }

    public void setPlayersSetup(Map<String, PlayerSetup> playersSetup) {
        this.playersSetup = playersSetup;
    }

    public void cleanUpWorld() {
        if (engine != null) {
            engine.removeAllEntities();
            engine = null;
        }
    }

    private int i = 0; // For testing

    public boolean isThePlayFinish() {
        // TODO Finish implementation of this method
        i++;
        return false;
    }

}
