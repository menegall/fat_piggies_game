package com.fatpiggies.game.controller;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.controller.mainControllerInterfaces.IPlayActions;
import com.fatpiggies.game.model.GameWorld;
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

import java.util.HashMap;
import java.util.Map;

public class HostPlayController implements IPlayController {

    private final IPlayActions actions;

    private final Map<String, Float> lastInputTimestamps = new HashMap<>();
    private final GameState gameState = new GameState();

    private Engine engine;
    private GameWorld world;

    private DatabaseService db;
    private boolean gameRunning = false;

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
    public GameWorld getWorld() { return world;}

    @Override
    public void startGame(DatabaseService db) {
        this.db = db;
        gameRunning = true;

        String currentUser = actions.getCurrentUserId();

        for (Map.Entry<String, PlayerSetup> entry :
            actions.getLobbyModel().getPlayerSetups().entrySet()) {

            String playerId = entry.getKey();
            PlayerSetup setup = entry.getValue();

            TextureId texture = TextureManager.getPigTexture(setup.color);

            if (playerId.equals(currentUser)) {
                world.createHostPig(playerId, texture); // joueur local
            } else {
                world.createHostPig(playerId, texture); // remote (mais sans écraser local)
            }
        }

        attachPlayListener();
    }

    @Override
    public void endGame() {
        gameRunning = false;

        if (world != null) {
            world.cleanUpWorld();
        }

        engine = null;
        world = null;
        lastInputTimestamps.clear();
    }

    @Override
    public void updateWorld(float dt) {
        if (!gameRunning || world == null) return;

        world.update(dt);

        if (world.isThePlayFinish()) {
            actions.onGameFinishedByHost();
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

    private void attachPlayListener() {
        db.listenToInputs(world.getLobbyId(), new DatabaseService.InputsCallback() {

            @Override
            public void onInputsReceived(Map<String, PlayerInput> inputs) {
                Gdx.app.postRunnable(() -> {

                    // SAFETY
                    if (!gameRunning || world == null) return;

                    for (Map.Entry<String, PlayerInput> entry : inputs.entrySet()) {

                        String playerId = entry.getKey();
                        PlayerInput input = entry.getValue();

                        if (playerId == null || input == null) continue;

                        float lastKnownTs = lastInputTimestamps.getOrDefault(playerId, -1f);

                        if (input.ts > lastKnownTs) {
                            lastInputTimestamps.put(playerId, input.ts);
                            world.applyRemoteInput(playerId, input);
                        }
                    }
                });
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {
                actions.showError(errorMessage);
            }
        });
    }
}
