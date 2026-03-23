package com.fatpiggies.game.controller;

public class ClientPlayController implements IPlayController{
    private MainController main;
    public ClientPlayController(MainController main) {
        this.main = main;
    }
    @Override
    public void startGame(String lobbyId) {
        main.gsm.setPlayScreen();
    }

    @Override
    public void endGame(String lobbyId) {
        main.gsm.setOverScreen();
    }

    @Override
    public void movePig(int x, int y) {
        // TODO: how to move pig/give input into ecs
        main.world.engine.update();
    }
}
