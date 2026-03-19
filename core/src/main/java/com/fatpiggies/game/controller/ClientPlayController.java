package com.fatpiggies.game.controller;

public class ClientPlayController implements IPlayController{
    private MainController main;
    public ClientPlayController(MainController main) {
        this.main = main;
    }
    @Override
    public void startGame(String lobbyId) {
        throw new java.lang.Error("User is client and cannot start the game.");
    }

    @Override
    public void endGame(String lobbyId) {

    }

    @Override
    public void movePig(int x, int y) {

    }
}
