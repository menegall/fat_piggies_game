package com.fatpiggies.game.controller;

public interface IViewActions {

    // Menu
    void onHostLobbyClicked(String playerName);

    void onJoinLobbyClicked(String playerName, String lobbyCode);

    // Lobby
    void onStartClicked();

    void onLeaveClicked();

    // Play
    void onJoystickMoved(float x, float y);
}
