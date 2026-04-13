package com.fatpiggies.game.controller;

import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.network.DatabaseService;

public interface IPlayController {
    GameWorld getWorld();

    void startGame();

    void endGame();

    void attachPlayListener(DatabaseService db);

    void updateWorld(float dt);

    void updatePlayerInput(float x, float y);

    void sendToServer(DatabaseService db, float timer_network);
}
