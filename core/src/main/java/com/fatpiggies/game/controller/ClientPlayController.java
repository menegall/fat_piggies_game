package com.fatpiggies.game.controller;

import com.fatpiggies.game.view.states.GameOverState;
import com.fatpiggies.game.view.states.PlayState;

public class ClientPlayController implements IPlayController{
    private MainController main;

    public ClientPlayController(MainController main) {
        this.main = main;
    }

    @Override
    public void startGame(String lobbyId) {
        main.gsm.set(new PlayState());
        main.gsm.setPlayScreen();
    }

    @Override
    public void endGame(String lobbyId) {
        main.gsm.set(new GameOverState());
        main.gsm.setOverScreen();
    }

    @Override
    public void movePig(double x, double y) {
        // TODO: how to move pig/give input into ecs
        main.world.engine.update();
    }
}
