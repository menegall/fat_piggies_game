package com.fatpiggies.game.controller.mainControllerInterfaces;

import com.fatpiggies.game.model.LobbyModel;
import com.fatpiggies.game.network.NetworkError;

public interface IPlayActions {

    String getCurrentUserId();

    LobbyModel getLobbyModel();

    void onGameFinishedByHost();

    void showError(NetworkError error);
}
