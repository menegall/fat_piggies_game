package com.fatpiggies.game.controller;

import static com.fatpiggies.game.view.TextureId.*;

import com.badlogic.ashley.core.Engine;
import com.fatpiggies.game.controller.mainControllerInterfaces.IPlayActions;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.ecs.systems.move.*;

import com.fatpiggies.game.view.TextureId;

public class ClientPlayController implements IPlayController {
    private final IPlayActions actions;
    private Engine engine;
    private GameWorld world;

    public ClientPlayController(IPlayActions actions, String lobbyId) {
        this.actions = actions;

        engine = new Engine();

        engine.addSystem(new NetworkReconciliationSystem());
        engine.addSystem(new NetworkLerpSystem());
        engine.addSystem(new MovementSystem());

        world = new GameWorld(engine);
    }

    @Override
    public void startGame(String lobbyId) {
        TextureId[] textures = {BLUE_PIG, GREEN_PIG, RED_PIG, YELLOW_PIG};
        int count = 0;

        String currentUser = actions.getCurrentUserId();

        world.createLocalPig(currentUser, textures[count++]);

        for (String playerId : actions.getLobbyModel().getPlayersSetup().keySet()) {
            if (!playerId.equals(currentUser) && count < textures.length) {
                world.createRemotePig(playerId, textures[count++]);
            }
        }

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

        actions.clearPlayController();
    }

    @Override
    public void updateWorld(float dt) {
        world.update(dt);
    }

    @Override
    public void updatePlayerInput(float x, float y) {
        world.updatePlayerInput(x, y);
    }
}
