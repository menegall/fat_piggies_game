package com.fatpiggies.game.controller;

import java.util.ArrayList;

public interface IViewActions {

    // Menu
    void onHostLobbyClicked(String name);
    void onJoinLobbyClicked(String name, String lobbyId);

    // Lobby
    void onStartClicked();
    void onLeaveClicked();

    // Play
    void onJoystickMoved(float x, float y);
}
