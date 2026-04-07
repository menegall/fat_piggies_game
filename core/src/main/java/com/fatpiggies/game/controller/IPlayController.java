package com.fatpiggies.game.controller;

import java.util.ArrayList;

public interface IPlayController {
    void startGame(String lobbyId, ArrayList<String> playerIds, ArrayList<String> textureIds);
    void endGame(String lobbyId);
    void updatePlayerInput(float x, float y);
}
