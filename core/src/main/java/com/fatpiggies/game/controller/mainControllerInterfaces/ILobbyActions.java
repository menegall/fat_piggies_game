package com.fatpiggies.game.controller.mainControllerInterfaces;

import com.fatpiggies.game.model.IReadOnlyLobbyModel;
import com.fatpiggies.game.model.LobbyModel;

public interface ILobbyActions {

    void goToLobbyState(IReadOnlyLobbyModel lobbyModel, boolean isHost);

    void startClientGame(String lobbyId);

    void endGame(String lobbyId);

    void showError(String message);
}
