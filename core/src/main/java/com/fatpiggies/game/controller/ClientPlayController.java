package com.fatpiggies.game.controller;

import static com.fatpiggies.game.view.TextureId.BLUE_PIG;
import static com.fatpiggies.game.view.TextureId.GREEN_PIG;
import static com.fatpiggies.game.view.TextureId.RED_PIG;
import static com.fatpiggies.game.view.TextureId.YELLOW_PIG;

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
import com.fatpiggies.game.view.TextureId;

public class ClientPlayController implements IPlayController {
    private final IPlayActions actions;
    private float lastStateTimestamps = 0;
    private Engine engine;
    private GameWorld world;
    private PlayerInput input = new PlayerInput();

    public ClientPlayController(IPlayActions actions, String lobbyId) {
        this.actions = actions;

        engine = new Engine();

        engine.addSystem(new NetworkReconciliationSystem());
        engine.addSystem(new NetworkLerpSystem());
        engine.addSystem(new MovementSystem());

        world = new GameWorld(engine);
    }

    @Override
    public void startGame(String lobbyId, DatabaseService db) {
        TextureId[] textures = {BLUE_PIG, GREEN_PIG, RED_PIG, YELLOW_PIG};
        int count = 0;

        String currentUser = actions.getCurrentUserId();

        world.createLocalPig(currentUser, textures[count++]);

        for (String playerId : actions.getLobbyModel().getPlayersSetup().keySet()) {
            if (!playerId.equals(currentUser) && count < textures.length) {
                world.createRemotePig(playerId, textures[count++]);
            }
        }

        attachPlayListener(db);

        actions.goToPlayState(world);
        actions.setGameIsPlaying(true);
    }

    @Override
    public void endGame(String lobbyId) {
        actions.setGameIsPlaying(false);
        actions.goToGameOverState(false);

        world.cleanUpWorld();
        engine = null;
        world = null;
        lastStateTimestamps = 0;

        actions.clearPlayController();
    }

    @Override
    public void updateWorld(float dt) {
        world.update(dt);
    }

    @Override
    public void updatePlayerInput(float x, float y) {
        world.updateLocalPlayerInput(x, y);
    }

    @Override
    public void sendToServer(DatabaseService db, float timer_network) {
        input.ts += timer_network;
        world.populatePlayerInput(input);
        db.pushPlayerInput(world.getLobbyId(), actions.getCurrentUserId(), input);
    }

    private void attachPlayListener(DatabaseService db) {
        db.listenToGameState(world.getLobbyId(), new DatabaseService.GameStateCallback() {
            @Override
            public void onDataReceived(GameState data) {
                Gdx.app.postRunnable(() -> {
                    if (data != null && data.ts >= lastStateTimestamps) {
                        lastStateTimestamps = data.ts;
                        world.applyGameState(data);
                    }
                });
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {

            }
        });
    }
}
