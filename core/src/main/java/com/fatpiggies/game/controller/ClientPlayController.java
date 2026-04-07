package com.fatpiggies.game.controller;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.ecs.systems.move.MovementSystem;
import com.fatpiggies.game.model.ecs.systems.move.NetworkLerpSystem;
import com.fatpiggies.game.model.ecs.systems.move.NetworkReconciliationSystem;
import java.util.ArrayList;

public class ClientPlayController implements IPlayController{
    private MainController main;
    private Engine engine;

    public ClientPlayController(MainController main) {
        this.main = main;
        engine = new PooledEngine();
        // add all systems for client
        engine.addSystem(new MovementSystem());
        engine.addSystem(new NetworkLerpSystem());
        engine.addSystem(new NetworkReconciliationSystem());
    }

    @Override
    public void startGame(String lobbyId, ArrayList<String> playerIds, ArrayList<String> textureIds){
        main.world = new GameWorld(new PooledEngine());
        main.world.createLocalPig(playerIds.get(0), textureIds.get(0),0,0);
        for(int i = 1; i < playerIds.size(); i++) {
            main.world.createRemotePig(playerIds.get(i), textureIds.get(i), 0,0);
        }
        main.gsm.setPlayState(main);
    }

    @Override
    public void endGame(String lobbyId) {
        main.gsm.setOverState(main, false); // it is the client controller
    }

    @Override
    public void updatePlayerInput(int x, int y) {
        main.world.updatePlayerInput(x,y);
    }
}
