package com.fatpiggies.game.controller;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.controller.mainControllerInterfaces.IPlayActions;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.ecs.systems.move.NetworkLerpSystem;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.network.dto.PlayerInput;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.view.PlayerColor;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

import java.util.List;
import java.util.Map;

public class ClientPlayController implements IPlayController {

    private final IPlayActions actions;

    private Engine engine;
    private GameWorld world;

    private final PlayerInput input = new PlayerInput();

    private float lastStateTimestamp = 0f;
    private boolean gameRunning = false;
    private boolean firstStateReceived = false;
    private float lastSendTime = 0f;
    private static final float KEEP_ALIVE = 0.2f;    // 200 ms
    private static final float DEADZONE = 0.02f;

    public ClientPlayController(IPlayActions actions, String lobbyId) {
        this.actions = actions;

        engine = new Engine();

        engine.addSystem(new NetworkLerpSystem());

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
        lastStateTimestamp = 0f;
        firstStateReceived = false;
        lastSendTime = 0f;

        // To not have old inputs
        input.jx = 0f;
        input.jy = 0f;
        input.ts = 0f;

        String currentUser = actions.getCurrentUserId();

        for (Map.Entry<String, PlayerSetup> entry :
            actions.getLobbyModel().getPlayerSetups().entrySet()) {

            String playerId = entry.getKey();
            PlayerSetup setup = entry.getValue();

            TextureId texture = TextureManager.getPigTextureId(PlayerColor.valueOf(setup.color));

            if (playerId.equals(currentUser)) {
                world.createLocalPig(playerId, texture);
            } else {
                world.createRemotePig(playerId, texture);
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
    }

    @Override
    public void updateWorld(float dt) {
        if (!gameRunning || world == null) {
            return;
        }

        List<String> droppedPlayers = world.removeDisconnectedPlayers(actions.getLobbyModel().getPlayerSetups());
        for (String playerName : droppedPlayers) {
            actions.showMessage(playerName + " disconnected!");
        }

        world.update(dt);
    }

    @Override
    public void updatePlayerInput(float x, float y) {
        if (world == null) {
            return;
        }

        if (Math.abs(x) < DEADZONE) {
            x = 0f;
        }
        if (Math.abs(y) < DEADZONE) {
            y = 0f;
        }

        world.updateLocalPlayerInput(x, y);
    }

    @Override
    public void sendToServer(DatabaseService db, float timer) {
        if (!gameRunning || world == null) {
            return;
        }

        world.populatePlayerInput(input);

        boolean isMoving = Math.abs(input.jx) > 0.01f || Math.abs(input.jy) > 0.01f;
        boolean keepAlive = (input.ts - lastSendTime) >= KEEP_ALIVE;

        input.ts += timer;

        if (isMoving || keepAlive) {
            db.pushPlayerInput(world.getLobbyId(), actions.getCurrentUserId(), input);
            lastSendTime = input.ts;
        }
    }

    @Override
    public void attachPlayListener(DatabaseService db) {
        db.listenToGameState(world.getLobbyId(), new DatabaseService.GameStateCallback() {
            @Override
            public void onDataReceived(GameState data) {
                Gdx.app.postRunnable(() -> {
                    if (!gameRunning || world == null || data == null) {
                        return;
                    }

                    if (data.ts > lastStateTimestamp) {
                        lastStateTimestamp = data.ts;

                        if (!firstStateReceived) {
                            firstStateReceived = true;
                            world.applyGameStateInstant(data);
                        } else {
                            world.applyGameState(data);
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
