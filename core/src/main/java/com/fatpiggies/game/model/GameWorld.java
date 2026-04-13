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
import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.network.dto.PlayerData;
import com.fatpiggies.game.network.dto.PlayerInput;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.network.dto.PowerupData;
import com.fatpiggies.game.view.TextureId;

import java.util.HashMap;
import java.util.Map;

public class GameWorld implements IReadOnlyGameWorld {
    // Reusable sets to track active entities per frame without memory allocation
    private final java.util.Set<String> activePlayersTracker = new java.util.HashSet<>();
    private final java.util.Set<String> activePowerupsTracker = new java.util.HashSet<>();
    // Fast lookup map for powerups spawned remotely by the Host
    private final Map<String, Entity> clientRemotePowerups = new HashMap<>();

    private Engine engine;
    private Entity localPlayer;
    private String lobbyId;
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

    public void updateLocalPlayerInput(float x, float y) {
        if (localPlayer == null) {
            return;
        }

        PlayerInputComponent input = localPlayer.getComponent(PlayerInputComponent.class);
        if (input != null) {
            input.joystickPercentageX = MathUtils.clamp(x, -1.0f, 1.0f);
            input.joystickPercentageY = MathUtils.clamp(y, -1.0f, 1.0f);
        }
    }

    /**
     * Creates a pig for the Host. It contains ALL physics and collision logic.
     * The host simulates all players.
     * IMPORTANT:
     * This method does NOT set localPlayer automatically.
     * The controller must call setLocalPlayer(...) explicitly.
     *
     * @param networkId Unique ID of the player
     * @param textureId Texture ID of the pig
     * @return the created entity
     */
    public Entity createHostPig(String networkId, TextureId textureId) {
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

        this.engine.addEntity(entity);
        return entity;
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

        NetworkSyncComponent sync = new NetworkSyncComponent();
        TransformComponent transform = new TransformComponent();

        HealthComponent health = new HealthComponent();
        health.currentLife = BASE_LIFE;

        attachPhysicComponents(entity);

        PlayerInputComponent input = new PlayerInputComponent();
        input.multiplier = 1;

        RenderComponent render = new RenderComponent();
        render.textureId = textureId;
        render.width = PIG_WIDTH;
        render.height = PIG_HEIGHT;
        render.angleOffset = PIG_ANGLE_OFFSET;

        // No network sync
        entity.add(netId).add(transform).add(health)
            .add(input).add(render).add(sync);

        localPlayer = entity;
        engine.addEntity(entity);
    }

    /**
     * Creates a remote pig for the Client.
     * This pig will be controlled by the Network Lerp System.
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
     * Creates a random power-up in the game world.
     */
    public void createRandomPowerUp() {
        // Get all possible power-up types
        PowerUpType[] types = PowerUpType.values();

        // Ensure there are types available to avoid crashes
        if (types.length == 0) return;

        // Pick a random index
        int randomIndex = MathUtils.random(0, types.length - 1);

        // Create the power-up using the existing method
        createPowerUp(types[randomIndex]);
    }

    /**
     * Creates a collectible power-up for the Host.
     * Randomizes position and lifetime.
     *
     * @param type The type of power-up to create, follow the PowerUpType enum.
     */
    private void createPowerUp(PowerUpType type) {
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
     * Creates a "dumb" powerup entity used ONLY on Clients.
     * It has no collision or physics logic, just a transform and a graphic,
     * because the Host handles all the real collision logic.
     */
    private Entity createVisualPowerupForClient(PowerupData puData) {
        Entity entity = engine.createEntity();

        TransformComponent transform = new TransformComponent();
        transform.x = puData.x;
        transform.y = puData.y;

        RenderComponent render = new RenderComponent();
        render.textureId = puData.textureId;
        render.width = POWER_UP_WIDTH;
        render.height = POWER_UP_HEIGHT;
        render.angleOffset = POWER_UP_ANGLE_OFFSET;

        entity.add(transform).add(render);
        engine.addEntity(entity);

        return entity;
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

    public void setLocalPlayer(Entity localPlayer) {
        this.localPlayer = localPlayer;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
    }


    public void setPlayersSetup(Map<String, PlayerSetup> playersSetup) {
        this.playersSetup = playersSetup;
    }

    public void cleanUpWorld() {
        clientRemotePowerups.clear();
        localPlayer = null;
        playersSetup = null;

        if (engine != null) {
            engine.removeAllEntities();
            engine = null;
        }
    }

    public boolean isGameFinished() {
        int alivePlayers = 0;

        for (Entity entity : engine.getEntities()) {
            NetworkIdentityComponent netId = entity.getComponent(NetworkIdentityComponent.class);
            HealthComponent health = entity.getComponent(HealthComponent.class);

            // Players
            if (netId != null && netId.playerId != null && health != null && health.currentLife > 0) {
                alivePlayers++;

                // More than one players
                if (alivePlayers > 1) {
                    return false;
                }
            }
        }

        // Only one player
        return true;
    }

    public void populateGameState(GameState gameState) {
        // Remove players that are not in the lobby
        if (playersSetup != null) {
            gameState.players.keySet().removeIf(playerId -> !playersSetup.containsKey(playerId));
        }

        // Clear trackers instead of re-instantiating them
        activePlayersTracker.clear();
        activePowerupsTracker.clear();

        // Iterate over all entities currently managed by the Ashley Engine
        for (Entity entity : engine.getEntities()) {
            // --- PROCESS PLAYERS ---
            NetworkIdentityComponent netId = entity.getComponent(NetworkIdentityComponent.class);
            if (netId != null && netId.playerId != null) {
                activePlayersTracker.add(netId.playerId);

                TransformComponent transform = entity.getComponent(TransformComponent.class);
                VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
                HealthComponent health = entity.getComponent(HealthComponent.class);

                if (transform != null && velocity != null) {
                    // Try to get the pooled object
                    PlayerData pd = gameState.players.get(netId.playerId);

                    if (pd == null) {
                        // Allocate only if it's a completely new player
                        pd = new PlayerData();
                        gameState.players.put(netId.playerId, pd);
                    }

                    // Update values
                    pd.x = roundToOneDecimal(transform.x);
                    pd.y = roundToOneDecimal(transform.y);
                    pd.vx = roundToTwoDecimals(velocity.vx);
                    pd.vy = roundToTwoDecimals(velocity.vy);
                    pd.hp = (health != null) ? health.currentLife : 0;
                }
            }

            // --- PROCESS POWERUPS ---
            CollectibleComponent collectible = entity.getComponent(CollectibleComponent.class);
            if (collectible != null) {
                // Since PowerUps lack a NetworkIdentityComponent, we use their hashcode as a unique ID
                String powerupId = String.valueOf(entity.hashCode());
                activePowerupsTracker.add(powerupId);

                TransformComponent transform = entity.getComponent(TransformComponent.class);
                RenderComponent render = entity.getComponent(RenderComponent.class);

                if (transform != null && render != null) {
                    PowerupData puData = gameState.powerups.get(powerupId);

                    if (puData == null) {
                        puData = new PowerupData();
                        gameState.powerups.put(powerupId, puData);
                    }

                    puData.x = roundToOneDecimal(transform.x);
                    puData.y = roundToOneDecimal(transform.y);
                    puData.textureId = render.textureId;
                }
            }
        }

        // --- CLEANUP ---
        // Remove players and powerups that are no longer in the Ashley Engine
        // This prevents memory leaks and stale data on the clients
        gameState.players.keySet().removeIf(id -> !activePlayersTracker.contains(id));
        gameState.powerups.keySet().removeIf(id -> !activePowerupsTracker.contains(id));
    }

    public void populatePlayerInput(PlayerInput input) {
        if (localPlayer == null) {
            input.jx = 0f;
            input.jy = 0f;
            return;
        }

        PlayerInputComponent inputComponent = localPlayer.getComponent(PlayerInputComponent.class);

        if (inputComponent != null) {
            input.jx = roundToTwoDecimals(inputComponent.joystickPercentageX);
            input.jy = roundToTwoDecimals(inputComponent.joystickPercentageY);
        } else {
            input.jx = 0f;
            input.jy = 0f;
        }
    }

    /**
     * Rounds a float to 2 decimal places to save network bandwidth.
     * Example: 123.456789f becomes 123.46f
     */
    private float roundToTwoDecimals(float value) {
        return Math.round(value * 100f) / 100f;
    }

    /**
     * Rounds a float to 1 decimal place for maximum compression.
     * Example: 123.456789f becomes 123.5f
     */
    private float roundToOneDecimal(float value) {
        return Math.round(value * 10f) / 10f;
    }

    /**
     * Apply the given input to the remote player input in the host world.
     * Use this ONLY on the Host controllers!
     *
     * @param playerId Unique ID of the player to which put the input
     * @param input    The input dto to apply
     */
    public void applyRemoteInput(String playerId, PlayerInput input) {
        if (playerId == null || input == null) return;

        // Iterate through the engine's entities to find the target player
        for (Entity entity : engine.getEntities()) {
            NetworkIdentityComponent netId = entity.getComponent(NetworkIdentityComponent.class);

            // Check if this entity belongs to the specific remote player
            if (netId != null && playerId.equals(netId.playerId)) {
                PlayerInputComponent inputComp = entity.getComponent(PlayerInputComponent.class);

                if (inputComp != null) {
                    inputComp.joystickPercentageX = MathUtils.clamp(input.jx, -1.0f, 1.0f);
                    inputComp.joystickPercentageY = MathUtils.clamp(input.jy, -1.0f, 1.0f);
                }
                break;
            }
        }
    }

    /**
     * Applies the GameState received from the Host to the local ECS entities.
     * Use this ONLY on the Client controllers!
     *
     * @param state The game state snapshot containing all players and powerups
     */
    public void applyGameState(GameState state) {
        if (state == null) return;

        for (Entity entity : engine.getEntities()) {
            NetworkIdentityComponent netId = entity.getComponent(NetworkIdentityComponent.class);

            if (netId == null || netId.playerId == null) continue;

            PlayerData pd = state.players.get(netId.playerId);
            if (pd == null) continue;

            NetworkSyncComponent sync = entity.getComponent(NetworkSyncComponent.class);
            HealthComponent health = entity.getComponent(HealthComponent.class);

            if (health != null) {
                health.currentLife = pd.hp;
            }

            if (sync != null) {
                sync.targetX = pd.x;
                sync.targetY = pd.y;
                sync.targetVx = pd.vx;
                sync.targetVy = pd.vy;
            }
        }

        // --- CLEAN POWERUPS ---
        clientRemotePowerups.entrySet().removeIf(entry -> {
            if (!state.powerups.containsKey(entry.getKey())) {
                engine.removeEntity(entry.getValue());
                return true;
            }
            return false;
        });

        for (Map.Entry<String, PowerupData> entry : state.powerups.entrySet()) {
            String powerupId = entry.getKey();
            PowerupData puData = entry.getValue();

            Entity localPu = clientRemotePowerups.get(powerupId);

            if (localPu == null) {
                localPu = createVisualPowerupForClient(puData);
                clientRemotePowerups.put(powerupId, localPu);
            } else {
                TransformComponent transform = localPu.getComponent(TransformComponent.class);
                if (transform != null) {
                    transform.x = puData.x;
                    transform.y = puData.y;
                }
            }
        }
    }

    public void applyGameStateInstant(GameState state) {
        if (state == null) return;

        for (Entity entity : engine.getEntities()) {
            NetworkIdentityComponent netId = entity.getComponent(NetworkIdentityComponent.class);

            if (netId == null || netId.playerId == null) continue;

            PlayerData pd = state.players.get(netId.playerId);
            if (pd == null) continue;

            TransformComponent transform = entity.getComponent(TransformComponent.class);
            HealthComponent health = entity.getComponent(HealthComponent.class);

            if (transform != null) {
                transform.x = pd.x;
                transform.y = pd.y;
            }

            if (health != null) {
                health.currentLife = pd.hp;
            }
        }
    }

    @Override
    public Engine getEngine() {
        return engine;
    }

    @Override
    public Entity getLocalPlayer() {
        return localPlayer;
    }

    @Override
    public int getLocalPlayerLife() {
        if (localPlayer == null) return 0;

        HealthComponent hc = localPlayer.getComponent(HealthComponent.class);
        return hc != null ? hc.currentLife : 0;
    }

    @Override
    public TextureId getLocalPlayerTexture() {
        if (localPlayer == null) return null;

        RenderComponent rc = localPlayer.getComponent(RenderComponent.class);
        return rc != null ? rc.textureId : null;
    }

    @Override
    public boolean isLocalPlayerAlive() {
        if (localPlayer == null) return false;

        HealthComponent hc = localPlayer.getComponent(HealthComponent.class);
        return hc != null && hc.currentLife > 0;
    }
}
