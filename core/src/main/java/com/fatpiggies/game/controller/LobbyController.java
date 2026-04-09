package com.fatpiggies.game.controller;

import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.controller.mainControllerInterfaces.ILobbyActions;
import com.fatpiggies.game.model.LobbyModel;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.network.dto.PlayerSetup;

import java.util.Map;

public class LobbyController {
    private final DatabaseService dbs;
    private final ILobbyActions actions;
    private final LobbyModel lobbyModel;

    public LobbyController(ILobbyActions actions, String playerId, DatabaseService dbs, LobbyModel lobbyModel) {
        this.actions = actions;
        this.dbs = dbs;
        this.lobbyModel = lobbyModel;
        lobbyModel.setPlayerId(playerId);
    }

    public void hostLobby(String playerName) {
        lobbyModel.setIsHost(true);

        dbs.createLobby(lobbyModel.getPlayerId(), playerName, new DatabaseService.LobbyCallback() {
            @Override
            public void onSuccess(String lobbyId) {
                Gdx.app.postRunnable(() -> {
                    lobbyModel.setLobbyId(lobbyId);
                    changeToLobbyState();
                });
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {
                showError(errorMessage);
            }
        });
    }

    public void joinLobby(String playerName, String lobbyCode) {
        lobbyModel.setIsHost(false);

        dbs.joinLobby(lobbyCode, lobbyModel.getPlayerId(), playerName, new DatabaseService.LobbyCallback() {
            @Override
            public void onSuccess(String lobbyId) {
                Gdx.app.postRunnable(() -> {
                    lobbyModel.setLobbyId(lobbyId);
                    changeToLobbyState();
                });
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {
                showError(errorMessage);
            }
        });
    }

    public void leaveLobby() {
        if (lobbyModel.getLobbyId() != null) {
            dbs.stopListening();
            dbs.leaveLobby(lobbyModel.getLobbyId(), lobbyModel.getPlayerId());
        }
    }

    private void changeToLobbyState() {

        dbs.getLobbyCodeOnce(lobbyModel.getLobbyId(), new DatabaseService.CodeCallback() {
            @Override
            public void onCodeRetrieved(String code) {
                Gdx.app.postRunnable(() -> lobbyModel.setLobbyCode(code));
            }

            @Override
            public void onError(String errorMessage) {
                showError(errorMessage);
            }
        });

        dbs.listenToPlayersSetup(lobbyModel.getLobbyId(), new DatabaseService.PlayersSetupCallback() {
            @Override
            public void onPlayersSetupUpdated(Map<String, PlayerSetup> playersSetup) {
                lobbyModel.setPlayersSetup(playersSetup);
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {
                showError(errorMessage);
            }
        });

        if (!lobbyModel.getIsHost()) {
            dbs.listenToLobbyStatus(lobbyModel.getLobbyId(), new DatabaseService.LobbyStatusCallback() {
                @Override
                public void onStatusUpdated(String status) {
                    Gdx.app.postRunnable(() -> {
                        if ("playing".equals(status)) {
                            actions.startClientGame(lobbyModel.getLobbyId());
                        } else if ("over".equals(status)) {
                            actions.endGame(lobbyModel.getLobbyId());
                        }
                    });
                }

                @Override
                public void onError(NetworkError error, String errorMessage) {
                    showError(errorMessage);
                }
            });
        }

        actions.goToLobbyState(lobbyModel, lobbyModel.getIsHost());
    }

    private void showError(String message) {
        Gdx.app.postRunnable(() -> actions.showError(message));
    }
}
