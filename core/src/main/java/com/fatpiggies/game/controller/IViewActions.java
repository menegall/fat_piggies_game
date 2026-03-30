package com.fatpiggies.game.controller;

import java.util.ArrayList;

public interface IViewActions {

    void onPlayClicked(ArrayList<String> playerIds, ArrayList<String> textureIds);
    void onHostLobbyClicked(String playerId);
    void onJoinLobbyClicked(String playerId);
    void onExitClicked();
    void onJoystickMoved(int x, int y);
}
