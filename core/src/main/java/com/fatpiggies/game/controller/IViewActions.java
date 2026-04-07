package com.fatpiggies.game.controller;

public interface IViewActions {

    // Menu
    void onHostLobbyClicked(String name);
    void onJoinLobbyClicked(String name, String lobbyId);

    // Lobby
    void onStartClicked();
    void onLeaveClicked();

    // Play
    void onJoystickMoved(double x, double y);
}
