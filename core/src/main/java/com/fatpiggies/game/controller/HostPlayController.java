package com.fatpiggies.game.controller;

import static com.fatpiggies.game.view.TextureId.BLUE_PIG;
import static com.fatpiggies.game.view.TextureId.GREEN_PIG;
import static com.fatpiggies.game.view.TextureId.RED_PIG;
import static com.fatpiggies.game.view.TextureId.YELLOW_PIG;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.ecs.systems.LifetimeSystem;
import com.fatpiggies.game.model.ecs.systems.StatSystem;
import com.fatpiggies.game.model.ecs.systems.collision.ArenaBoundsSystem;
import com.fatpiggies.game.model.ecs.systems.collision.CollisionDetectionSystem;
import com.fatpiggies.game.model.ecs.systems.collision.CollisionResolutionSystem;
import com.fatpiggies.game.model.ecs.systems.move.MovementSystem;
import com.fatpiggies.game.model.ecs.systems.move.RespawnSystem;
import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.view.TextureId;

import java.util.ArrayList;

public class HostPlayController implements IPlayController {
    private final MainController main;
    private final Engine engine;

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

        main.world = new GameWorld(engine);
        main.world.setLobbyId(lobbyId);
    }

    @Override
    public void startGame(String lobbyId) {
        // TODO specify information that is available here+
        main.dbs.startGame(lobbyId);
        main.dbs.pushGameState(lobbyId, new GameState());
        // create entities in gameworld
        TextureId[] textures = {BLUE_PIG, GREEN_PIG, RED_PIG,YELLOW_PIG};
        int count = 0;
        for (String playerId : main.lobbyModel.getPlayersSetup().keySet()) {
            if (count < textures.length) {
                main.world.createHostPig(playerId, textures[count++], 0, 0);
            }
        }

        main.gsm.setPlayState(main, main.world);
    }

    @Override
    public void endGame(String lobbyId) {
        main.dbs.endGame(lobbyId);
        // TODO: determine winner
        main.gsm.setOverState(main, main.lobbyModel, true); // it is the host controller
        main.world = null;
    }

    @Override
    public void updatePlayerInput(float x, float y) {
        main.world.updatePlayerInput(x, y);
    }

}
