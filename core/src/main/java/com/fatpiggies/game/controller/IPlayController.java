package com.fatpiggies.game.controller;

import com.fatpiggies.game.network.DatabaseService;

public interface IPlayController {
    public void startGame(String lobbyId, DatabaseService db);
    void endGame(String lobbyId);

    void updateWorld(float dt);

    void updatePlayerInput(float x, float y);

    void sendToServer(DatabaseService db, float timer_network);
}
