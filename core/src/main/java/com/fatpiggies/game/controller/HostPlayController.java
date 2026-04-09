package com.fatpiggies.game.controller;

import static com.fatpiggies.game.view.TextureId.*;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.assets.TextureId;
import com.fatpiggies.game.controller.mainControllerInterfaces.IPlayActions;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.ecs.systems.*;
import com.fatpiggies.game.model.ecs.systems.collision.*;
import com.fatpiggies.game.model.ecs.systems.move.*;

import com.fatpiggies.game.view.TextureId;

public class HostPlayController implements IPlayController {
    private final IPlayActions actions;
    // Keeps track of the latest received input timestamp for each player
    private final Map<String, Float> lastInputTimestamps = new HashMap<>();
    private final GameState gameState = new GameState();
    private Engine engine;
    private GameWorld world;

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
    public void startGame(String lobbyId, DatabaseService db) {
        actions.startGameOnServer(lobbyId);

        TextureId[] textures = {BLUE_PIG, GREEN_PIG, RED_PIG, YELLOW_PIG};
        int count = 0;

        for (String playerId : actions.getLobbyModel().getPlayersSetup().keySet()) {
            if (count < textures.length) {
                world.createHostPig(playerId, textures[count++]);
            }
        }

        attachPlayListener(db);

        actions.goToPlayState(world);
        actions.setGameIsPlaying(true);
    }

    @Override
    public void endGame(String lobbyId) {
        actions.setGameIsPlaying(false);
        actions.endGameOnServer(lobbyId);

        actions.goToGameOverState(true);

        world.cleanUpWorld();
        engine = null;
        world = null;
        lastInputTimestamps.clear();

        actions.clearPlayController();
    }

    @Override
    public void updateWorld(float dt) {
        world.update(dt);

        if (world.isThePlayFinish()) {
            endGame(actions.getLobbyModel().getLobbyId());
        }
    }

    @Override
    public void updatePlayerInput(float x, float y) {
        world.updateLocalPlayerInput(x, y);
    }

    @Override
    public void sendToServer(DatabaseService db, float timer) {
        gameState.ts += timer;
        world.populateGameState(gameState);
        db.pushGameState(world.getLobbyId(), gameState);
    }

    private void attachPlayListener(DatabaseService db) {
        db.listenToInputs(world.getLobbyId(), new DatabaseService.InputsCallback() {
            @Override
            public void onInputsReceived(Map<String, PlayerInput> inputs) {
                Gdx.app.postRunnable(() -> {
                    // Iterate through all received player inputs
                    for (Map.Entry<String, PlayerInput> entry : inputs.entrySet()) {
                        String playerId = entry.getKey();
                        PlayerInput input = entry.getValue();

                        // Safety check just in case the db sends null values
                        if (playerId == null || input == null) continue;

                        float lastKnownTs = lastInputTimestamps.getOrDefault(playerId, -1f);

                        // Process only if the packet is fresh (out-of-order prevention)
                        if (input.ts > lastKnownTs) {
                            lastInputTimestamps.put(playerId, input.ts);
                            world.applyRemoteInput(playerId, input);
                        }
                    }
                });
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {

            }
        });
    }
}
