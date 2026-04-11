package com.fatpiggies.game.controller.mainControllerInterfaces;

import com.fatpiggies.game.model.IReadOnlyLobbyModel;
import com.fatpiggies.game.network.NetworkError;

public interface ILobbyActions {

    void goToMenuState();

    void goToLobbyState(IReadOnlyLobbyModel lobbyModel, boolean isHost);

    void goBackToLobbyState();

    void goToPlayState();

    void goToOverState();

    void showError(NetworkError error);
}
