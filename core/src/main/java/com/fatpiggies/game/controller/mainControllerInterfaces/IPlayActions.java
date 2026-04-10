package com.fatpiggies.game.controller.mainControllerInterfaces;

import com.fatpiggies.game.model.LobbyModel;

public interface IPlayActions {

    String getCurrentUserId();

    LobbyModel getLobbyModel();

    void onGameFinishedByHost();

    void showError(String message);
}
