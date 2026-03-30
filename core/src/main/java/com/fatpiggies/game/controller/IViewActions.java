package com.fatpiggies.game.controller;

public interface IViewActions {

    void onPlayClicked();
    void onHostLobbyClicked(String playerId);
    void onJoinLobbyClicked(String playerId);
    void onExitClicked();
    void onJoystickMoved(double x, double y);
}
