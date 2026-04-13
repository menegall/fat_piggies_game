package com.fatpiggies.game.controller.mainControllerInterfaces;

import com.fatpiggies.game.model.LobbyModel;
import com.fatpiggies.game.network.NetworkError;

import java.util.List;

public interface IPlayActions {

    String getCurrentUserId();

    LobbyModel getLobbyModel();

    float getTimerNetwork();

    void onGameFinishedByHost(List<String> finalRank);

    void showError(NetworkError error);
}
