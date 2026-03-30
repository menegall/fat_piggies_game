package com.fatpiggies.game.controller;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.view.states.GameOverState;
import com.fatpiggies.game.view.states.PlayState;

import java.util.ArrayList;

public class HostPlayController implements IPlayController {
    private MainController main;

    public HostPlayController(MainController main) {
        this.main = main;
    }

    @Override
    public void startGame(String lobbyId, ArrayList<String> playerIds, ArrayList<String> textureIds) {
        // TODO specify information that is available here
        main.world = new GameWorld(new PooledEngine());
        main.dbs.startGame(lobbyId);
        main.dbs.pushGameState(lobbyId, new GameState());
        // create entities in gameworld
        // TODO change positions when view is implemented
        main.world.createHostPig(main.auth.getCurrentUserId(), textureIds.get(0),0,0);
        for(int i = 0; i < playerIds.size(); i++) {
            main.world.createRemotePig(playerIds.get(i), textureIds.get(i+1), 0,0);
        }
        main.gsm.setPlayScreen();
    }

    @Override
    public void endGame(String lobbyId) {
        main.dbs.endGame(lobbyId);
        // TODO: determine winner, look for player with > 0 lifes
        main.gsm.set(new GameOverState());
        main.gsm.setOverScreen();
        main.world = null;

    }

    @Override
    public void movePig(int x, int y) {
        main.world.movePlayerPig(x,y);
    }

}
