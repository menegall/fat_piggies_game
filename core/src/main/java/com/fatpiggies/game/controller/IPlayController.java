package com.fatpiggies.game.controller;

public interface IPlayController {
    void startGame(String lobbyId);
    void endGame(String lobbyId);
    void movePig(double x, double y);
}
