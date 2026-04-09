package com.fatpiggies.game.controller;

import static com.fatpiggies.game.assets.TextureId.*;

import com.badlogic.ashley.core.Engine;
import com.fatpiggies.game.controller.mainControllerInterfaces.IPlayActions;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.ecs.systems.*;
import com.fatpiggies.game.model.ecs.systems.collision.*;
import com.fatpiggies.game.model.ecs.systems.move.*;

import com.fatpiggies.game.assets.TextureId;

public class HostPlayController implements IPlayController {
    private final IPlayActions actions;
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
    public void startGame(String lobbyId) {
        actions.startGameOnServer(lobbyId);

        TextureId[] textures = {BLUE_PIG, GREEN_PIG, RED_PIG, YELLOW_PIG};
        int count = 0;

        for (String playerId : actions.getLobbyModel().getPlayersSetup().keySet()) {
            if (count < textures.length) {
                world.createHostPig(playerId, textures[count++]);
            }
        }

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
        world.updatePlayerInput(x, y);
    }
}
