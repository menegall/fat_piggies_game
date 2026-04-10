package com.fatpiggies.game.controller.mainControllerInterfaces;

import com.fatpiggies.game.view.PlayerColor;

public interface IViewActions {

    // Menu
    void onHostLobbyClicked(String playerName, PlayerColor playerColor);

    void onJoinLobbyClicked(String playerName, String lobbyCode, PlayerColor playerColor);

    // Lobby
    void onStartClicked();

    void onLeaveClicked();

    // Play
    void onJoystickMoved(float x, float y);

    // Over
    void onLobbyClicked();
}
