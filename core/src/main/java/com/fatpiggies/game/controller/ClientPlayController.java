package com.fatpiggies.game.controller;

public class ClientPlayController implements IPlayController{
    private MainController main;

    public ClientPlayController(MainController main) {
        this.main = main;
    }

    @Override
    public void startGame(String lobbyId) {
        main.gsm.setPlayState(main);
    }

    @Override
    public void endGame(String lobbyId) {
        main.gsm.setOverState(main, false); // it is the client controller
    }

    @Override
    public void movePig(double x, double y) {
        // TODO: how to move pig/give input into ecs
        main.world.engine.update();
    }
}
