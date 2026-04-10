package com.fatpiggies.game.controller;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.controller.mainControllerInterfaces.IPlayActions;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.ecs.systems.move.MovementSystem;
import com.fatpiggies.game.model.ecs.systems.move.NetworkLerpSystem;
import com.fatpiggies.game.model.ecs.systems.move.NetworkReconciliationSystem;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.network.dto.PlayerInput;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

import java.util.Map;

public class ClientPlayController implements IPlayController {

    private final IPlayActions actions;
    private Engine engine;
    private GameWorld world;
    private final PlayerInput input = new PlayerInput();
    private float lastStateTimestamp = 0f;
    private boolean gameRunning = false;

    private DatabaseService db;

    public ClientPlayController(IPlayActions actions, String lobbyId) {
        this.actions = actions;

        engine = new Engine();

        engine.addSystem(new NetworkReconciliationSystem());
        engine.addSystem(new NetworkLerpSystem());
        engine.addSystem(new MovementSystem());

        world = new GameWorld(engine);
        world.setLobbyId(lobbyId);
    }

    @Override
    public GameWorld getWorld() { return world;}

    @Override
    public void startGame(DatabaseService db) {
        this.db = db;

        gameRunning = true;
        lastStateTimestamp = 0f;

        String currentUser = actions.getCurrentUserId();

        for (Map.Entry<String, PlayerSetup> entry :
            actions.getLobbyModel().getPlayerSetups().entrySet()) {

            String playerId = entry.getKey();
            PlayerSetup setup = entry.getValue();

            TextureId texture = TextureManager.getPigTexture(setup.color);

            if (playerId.equals(currentUser)) {
                world.createLocalPig(playerId, texture);
            } else {
                world.createRemotePig(playerId, texture);
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
        lastStateTimestamp = 0f;
    }

    @Override
    public void updateWorld(float dt) {
        if (world != null) {
            world.update(dt);
        }
    }

    @Override
    public void updatePlayerInput(float x, float y) {
        if (world != null) {
            world.updateLocalPlayerInput(x, y);
        }
    }

    @Override
    public void sendToServer(DatabaseService db, float timerNetwork) {
        if (!gameRunning || world == null) return;

        input.ts += timerNetwork;
        world.populatePlayerInput(input);

        db.pushPlayerInput(world.getLobbyId(), actions.getCurrentUserId(), input);
    }

    private void attachPlayListener() {
        db.listenToGameState(world.getLobbyId(), new DatabaseService.GameStateCallback() {

            @Override
            public void onDataReceived(GameState data) {
                Gdx.app.postRunnable(() -> {

                    // Safety check
                    if (!gameRunning || world == null) return;

                    if (data != null && data.ts >= lastStateTimestamp) {

                        lastStateTimestamp = data.ts;

                        world.applyGameState(data);
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
