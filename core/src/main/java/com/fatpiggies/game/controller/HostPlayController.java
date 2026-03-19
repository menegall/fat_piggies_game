package com.fatpiggies.game.controller;

public class HostPlayController implements IPlayController {
    private MainController main;

    public HostPlayController(MainController main) {
        this.main = main;
    }

    @Override
    public void startGame(String lobbyId) {
        // TODO specify information that is available here
        main.gameWorld = new GameWorld();
        main.db.startGame(lobbyId);
        main.gsm.setPlayScreen();
    }

    @Override
    public void endGame(String lobbyId) {
        // TODO
    }

    @Override
    public void movePig(int x, int y) {
        // TODO
    }
}
