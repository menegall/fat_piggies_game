package com.fatpiggies.game.controller;

import com.fatpiggies.game.network.dto.GameState;

public class HostPlayController implements IPlayController {
    private MainController main;

    public HostPlayController(MainController main) {
        this.main = main;
    }

    @Override
    public void startGame(String lobbyId) {
        // TODO specify information that is available here
        main.world = new GameWorld();
        main.dbs.startGame(lobbyId);
        main.dbs.pushGameState(lobbyId, new GameState());
        main.world.startWorld();
        main.gsm.setPlayScreen();
    }

    @Override
    public void endGame(String lobbyId) {
        main.dbs.endGame(lobbyId);
        // TODO: determine winner
        main.gsm.setOverScreen();
        main.world = null;

    }

    @Override
    public void movePig(int x, int y) {
        // TODO
    }
}
