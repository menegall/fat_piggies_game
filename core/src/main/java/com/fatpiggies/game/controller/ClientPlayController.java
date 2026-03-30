package com.fatpiggies.game.controller;

import com.badlogic.ashley.core.PooledEngine;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.view.states.*;

import java.util.ArrayList;

public class ClientPlayController implements IPlayController{
    private MainController main;

    public ClientPlayController(MainController main) {
        this.main = main;
    }

    @Override
    public void startGame(String lobbyId, ArrayList<String> playerIds, ArrayList<String> textureIds) {
        main.world = new GameWorld(new PooledEngine());
        main.world.createLocalPig(playerIds.get(0), textureIds.get(0),0,0);
        for(int i = 1; i < playerIds.size(); i++) {
            main.world.createRemotePig(playerIds.get(i), textureIds.get(i), 0,0);
        }
        main.gsm.set(new PlayState());
        main.gsm.setPlayScreen();
    }

    @Override
    public void endGame(String lobbyId) {
        main.gsm.set(new GameOverState());
        main.gsm.setOverScreen();
    }

    @Override
    public void movePig(int x, int y) {
        main.world.movePlayerPig(x,y);
    }
}
