package com.fatpiggies.game.controller;

import static com.fatpiggies.game.model.utils.GameConstants.POWERUP_SPAWN_INTERVAL;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.controller.mainControllerInterfaces.IPlayActions;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.ecs.components.HealthComponent;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkIdentityComponent;
import com.fatpiggies.game.model.ecs.systems.LifetimeSystem;
import com.fatpiggies.game.model.ecs.systems.StatSystem;
import com.fatpiggies.game.model.ecs.systems.collision.ArenaBoundsSystem;
import com.fatpiggies.game.model.ecs.systems.collision.CollisionDetectionSystem;
import com.fatpiggies.game.model.ecs.systems.collision.CollisionResolutionSystem;
import com.fatpiggies.game.model.ecs.systems.move.MovementSystem;
import com.fatpiggies.game.model.ecs.systems.move.RespawnSystem;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.network.dto.PlayerInput;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HostPlayController implements IPlayController {

    private final IPlayActions actions;
    private final GameState gameState = new GameState();
    private final Map<String, Float> remoteInputFreshness = new HashMap<>();
    private final Map<String, Float> lastProcessedInputTs = new HashMap<>();
    private static final float INPUT_TIMEOUT = 0.2f;
    private float powerupTimer = POWERUP_SPAWN_INTERVAL;
    private Engine engine;
    private GameWorld world;
    private boolean gameRunning = false;
    private final List<String> deathOrder = new ArrayList<>();

    public HostPlayController(IPlayActions actions, String lobbyId) {
        this.actions = actions;

        engine = new Engine();

        engine.addSystem(new LifetimeSystem());
        engine.addSystem(new StatSystem());
        engine.addSystem(new MovementSystem());
        engine.addSystem(new ArenaBoundsSystem());
        engine.addSystem(new CollisionDetectionSystem());
        engine.addSystem(new CollisionResolutionSystem());
        engine.addSystem(new RespawnSystem());

        world = new GameWorld(engine);
        world.setLobbyId(lobbyId);
    }

    @Override
    public GameWorld getWorld() {
        return world;
    }

    @Override
    public void startGame() {
        gameRunning = true;
        remoteInputFreshness.clear();
        lastProcessedInputTs.clear();
        deathOrder.clear();

        powerupTimer = POWERUP_SPAWN_INTERVAL;

        String currentUser = actions.getCurrentUserId();
        Map<String, PlayerSetup> playerSetups = actions.getLobbyModel().getPlayerSetups();

        world.setPlayersSetup(playerSetups);

        for (Map.Entry<String, PlayerSetup> entry : playerSetups.entrySet()) {
            String playerId = entry.getKey();
            PlayerSetup setup = entry.getValue();

            TextureId texture = TextureManager.getPigTexture(setup.color);
            Entity pig = world.createHostPig(playerId, texture);

            if (playerId.equals(currentUser)) {
                world.setLocalPlayer(pig);
            } else {
                remoteInputFreshness.put(playerId, 0f);
            }
        }
    }

    @Override
    public void endGame() {
        gameRunning = false;

        if (world != null) {
            world.cleanUpWorld();
        }

        engine = null;
        world = null;
        remoteInputFreshness.clear();
        lastProcessedInputTs.clear();
    }

    @Override
    public void updateWorld(float dt) {
        if (!gameRunning || world == null) return;

        String currentUser = actions.getCurrentUserId();

        // Creating PowerUp Logic
        powerupTimer -= dt;
        if (powerupTimer <= 0f) {
            world.createRandomPowerUp();
            powerupTimer = POWERUP_SPAWN_INTERVAL;
        }

        // Timeout logic
        for (Entity entity : engine.getEntities()) {
            NetworkIdentityComponent netId = entity.getComponent(NetworkIdentityComponent.class);
            PlayerInputComponent inputComp = entity.getComponent(PlayerInputComponent.class);

            if (netId == null || inputComp == null || netId.playerId == null) continue;
            if (netId.playerId.equals(currentUser)) continue;

            float remaining = remoteInputFreshness.getOrDefault(netId.playerId, 0f) - dt;

            if (remaining <= 0f) {
                remaining = 0f;
                inputComp.joystickPercentageX = 0f;
                inputComp.joystickPercentageY = 0f;
            }

            remoteInputFreshness.put(netId.playerId, remaining);
        }

        // Physics update
        world.update(dt);

        // Track death order
        for (Entity entity : engine.getEntities()) {
            NetworkIdentityComponent netId = entity.getComponent(NetworkIdentityComponent.class);
            HealthComponent health = entity.getComponent(HealthComponent.class);

            if (netId != null && netId.playerId != null && health != null) {
                // If dead and not yet recorded, add to death order
                if (health.currentLife <= 0 && !deathOrder.contains(netId.playerId)) {
                    deathOrder.add(netId.playerId);
                }
            }
        }

        // Check end game
        if (world.isGameFinished()) {
            for (Entity entity : engine.getEntities()) {
                NetworkIdentityComponent netId = entity.getComponent(NetworkIdentityComponent.class);
                if (netId != null && netId.playerId != null) {
                    if (!deathOrder.contains(netId.playerId)) {
                        deathOrder.add(netId.playerId);
                    }
                }
            }
            actions.getLobbyModel().setFinalRanking(deathOrder);
            actions.onGameFinishedByHost(deathOrder);
        }
    }

    @Override
    public void updatePlayerInput(float x, float y) {
        if (world != null) {
            world.updateLocalPlayerInput(x, y);
        }
    }

    @Override
    public void sendToServer(DatabaseService db, float timer) {
        if (!gameRunning || world == null) return;

        gameState.ts += timer;
        world.populateGameState(gameState);

        db.pushGameState(world.getLobbyId(), gameState);
    }

    @Override
    public void attachPlayListener(DatabaseService db) {
        db.listenToInputs(world.getLobbyId(), new DatabaseService.InputsCallback() {
            @Override
            public void onInputsReceived(Map<String, PlayerInput> inputs) {
                Gdx.app.postRunnable(() -> {
                    if (!gameRunning || world == null || inputs == null) return;

                    for (Map.Entry<String, PlayerInput> entry : inputs.entrySet()) {
                        String playerId = entry.getKey();
                        PlayerInput playerInput = entry.getValue();

                        if (playerId == null || playerInput == null) continue;

                        // 1. Check for out-of-order packets
                        float lastTs = lastProcessedInputTs.getOrDefault(playerId, -1f);
                        if (playerInput.ts > lastTs) {

                            // 2. Update the tracking timestamp
                            lastProcessedInputTs.put(playerId, playerInput.ts);

                            // 3. Apply the input IMMEDIATELY to the physics engine
                            world.applyRemoteInput(playerId, playerInput);

                            // 4. Reset the timeout freshness so the pig doesn't stop
                            remoteInputFreshness.put(playerId, INPUT_TIMEOUT);
                        }
                    }
                });
            }

            @Override
            public void onError(NetworkError error) {
                actions.showError(error);
            }
        });
    }
}
