package com.fatpiggies.game.controller.mainControllerInterfaces;

import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.IReadOnlyGameWorld;
import com.fatpiggies.game.model.LobbyModel;

public interface IPlayActions {

    String getCurrentUserId();

    LobbyModel getLobbyModel();

    void goToPlayState(IReadOnlyGameWorld gameWorld);

    void goToGameOverState(boolean isHost);

    void setGameIsPlaying(boolean playing);

    void clearPlayController();

    void startGameOnServer(String lobbyId);

    void endGameOnServer(String lobbyId);

    void showError(String message);
}
