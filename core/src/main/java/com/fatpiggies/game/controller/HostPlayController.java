package com.fatpiggies.game.controller;

import static com.fatpiggies.game.assets.TextureId.BLUE_PIG;
import static com.fatpiggies.game.assets.TextureId.GREEN_PIG;
import static com.fatpiggies.game.assets.TextureId.RED_PIG;
import static com.fatpiggies.game.assets.TextureId.YELLOW_PIG;

import com.badlogic.ashley.core.Engine;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.ecs.systems.LifetimeSystem;
import com.fatpiggies.game.model.ecs.systems.StatSystem;
import com.fatpiggies.game.model.ecs.systems.collision.ArenaBoundsSystem;
import com.fatpiggies.game.model.ecs.systems.collision.CollisionDetectionSystem;
import com.fatpiggies.game.model.ecs.systems.collision.CollisionResolutionSystem;
import com.fatpiggies.game.model.ecs.systems.move.MovementSystem;
import com.fatpiggies.game.model.ecs.systems.move.RespawnSystem;
import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.assets.TextureId;

public class HostPlayController implements IPlayController {
    private final MainController main;
    private Engine engine;
    private GameWorld world;

    public HostPlayController(MainController main, String lobbyId) {
        this.main = main;

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
        main.dbs.startGame(lobbyId);
        // create entities in gameworld
        TextureId[] textures = {BLUE_PIG, GREEN_PIG, RED_PIG, YELLOW_PIG};
        int count = 0;
        for (String playerId : main.lobbyModel.getPlayersSetup().keySet()) {
            if (count < textures.length) {
                world.createHostPig(playerId, textures[count++]);
            }
        }
        main.gsm.setPlayState(main, world);
        main.setGameIsPlaying(true);
    }

    @Override
    public void endGame(String lobbyId) {
        main.setGameIsPlaying(false);
        main.dbs.endGame(lobbyId);
        // TODO: determine winner
        main.gsm.setOverState(main, main.lobbyModel, true); // it is the host controller
        // Clean up the Ashley engine and World
        world.cleanUpWorld();
        engine = null;
        world = null;
        // Destroy this controller
        main.playController = null;
    }

    @Override
    public void updateWorld(float dt) {
        world.update(dt);
        if (world.isThePlayFinish()) {
            endGame(main.lobbyModel.getLobbyId());
        }
        ;
    }

    @Override
    public void updatePlayerInput(float x, float y) {
        world.updatePlayerInput(x, y);
    }


}
