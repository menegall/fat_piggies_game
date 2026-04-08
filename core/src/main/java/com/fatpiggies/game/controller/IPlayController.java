package com.fatpiggies.game.controller;

public interface IPlayController {
    public void startGame(String lobbyId);
    void endGame(String lobbyId);

    void updateWorld(float dt);

    void updatePlayerInput(float x, float y);
}
