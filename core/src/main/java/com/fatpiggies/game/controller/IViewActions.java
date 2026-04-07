package com.fatpiggies.game.controller;

public interface IViewActions {

    void onPlayClicked();
    void onHostLobbyClicked(String playerName);
    void onJoinLobbyClicked(String playerName, String lobbyCode, String lobbyId);
    void onExitClicked();
    void onJoystickMoved(double x, double y);
}
