package com.fatpiggies.game.controller;

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

import java.util.ArrayList;

public class HostPlayController implements IPlayController {
    private final MainController main;
    private final Engine engine;

    public HostPlayController(MainController main) {
        this.main = main;

        engine = new PooledEngine();

        // add all systems to engine
        engine.addSystem(new MovementSystem());
        engine.addSystem(new CollisionDetectionSystem());
        engine.addSystem(new CollisionResolutionSystem());
        engine.addSystem(new StatSystem());
        engine.addSystem(new LifetimeSystem());
        engine.addSystem(new ArenaBoundsSystem());
        engine.addSystem(new RespawnSystem());

        main.world = new GameWorld(engine);
    }

    @Override
    public void startGame(String lobbyId, ArrayList<String> playerIds, ArrayList<String> textureIds) {
        // TODO specify information that is available here
        main.world = new GameWorld(new PooledEngine());
        main.dbs.startGame(lobbyId);
        main.dbs.pushGameState(lobbyId, new GameState());
        // create entities in gameworld

        // TODO change positions when view is implemented
        main.world.createHostPig(main.auth.getCurrentUserId(), textureIds.get(0), 0, 0);
        for (int i = 0; i < playerIds.size(); i++) {
            main.world.createRemotePig(playerIds.get(i), textureIds.get(i + 1), 0, 0);
        }

        main.gsm.setPlayState(main, main.world);
    }

    @Override
    public void endGame(String lobbyId) {
        main.dbs.endGame(lobbyId);
        // TODO: determine winner
        main.gsm.setOverState(main, main.world, true); // it is the host controller
        main.world = null;

    }

    @Override
    public void updatePlayerInput(float x, float y) {
        main.world.updatePlayerInput(x, y);
    }

}
