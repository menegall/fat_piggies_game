package com.fatpiggies.game.controller;

import java.util.ArrayList;

public interface IPlayController {
    public void startGame(String lobbyId);
    void endGame(String lobbyId);

    void updatePlayerInput(float x, float y);
}
