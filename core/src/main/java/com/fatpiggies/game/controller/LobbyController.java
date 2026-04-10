package com.fatpiggies.game.controller;

import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.controller.mainControllerInterfaces.ILobbyActions;
import com.fatpiggies.game.model.LobbyModel;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.view.PlayerColor;

import java.util.Map;

public class LobbyController {

    private final DatabaseService dbs;
    private final ILobbyActions actions;
    private final LobbyModel lobbyModel;

    // Avoid infinite loop when attaching the lobby status listener:
    // Firebase immediately sends the current value once.
    private boolean ignoreFirstLobbyStatusEvent = false;

    public LobbyController(ILobbyActions actions, String playerId, DatabaseService dbs, LobbyModel lobbyModel) {
        this.actions = actions;
        this.dbs = dbs;
        this.lobbyModel = lobbyModel;
        lobbyModel.setPlayerId(playerId);
    }

    public void hostLobby(String playerName, PlayerColor playerColor) {
        lobbyModel.setIsHost(true);

        dbs.createLobby(lobbyModel.getPlayerId(), playerName, playerColor, new DatabaseService.LobbyCallback() {
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

    public void joinLobby(String playerName, String lobbyCode, PlayerColor playerColor) {
        lobbyModel.setIsHost(false);

        dbs.joinLobby(lobbyCode, lobbyModel.getPlayerId(), playerName, playerColor, new DatabaseService.LobbyCallback() {
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

    public void goBackToLobby() {
        if (lobbyModel.getLobbyId() == null) return;

        // Only host resets the lobby data in Firebase.
        if (lobbyModel.getIsHost()) {
            dbs.resetLobbyToWaiting(lobbyModel.getLobbyId());
        }

        // Stop listening then reattach the listeners
        dbs.stopListening();
        changeToLobbyState();
    }

    private void changeToLobbyState() {
        String lobbyId = lobbyModel.getLobbyId();
        if (lobbyId == null) return;

        ignoreFirstLobbyStatusEvent = true;

        dbs.getLobbyCodeOnce(lobbyId, new DatabaseService.CodeCallback() {
            @Override
            public void onCodeRetrieved(String code) {
                Gdx.app.postRunnable(() -> lobbyModel.setLobbyCode(code));
            }

            @Override
            public void onError(String errorMessage) {
                showError(errorMessage);
            }
        });

        dbs.listenToPlayersSetup(lobbyId, new DatabaseService.PlayersSetupCallback() {
            @Override
            public void onPlayersSetupUpdated(Map<String, PlayerSetup> playersSetup) {
                Gdx.app.postRunnable(() -> lobbyModel.setPlayersSetup(playersSetup));
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {
                showError(errorMessage);
            }
        });

        // CLIENT ONLY
        if (!lobbyModel.getIsHost()) {

            dbs.listenToLobbyStatus(lobbyId, new DatabaseService.LobbyStatusCallback() {

                @Override
                public void onStatusUpdated(String status) {
                    Gdx.app.postRunnable(() -> {

                        if (status == null) return;

                        // ignore first callback
                        if (ignoreFirstLobbyStatusEvent) {
                            ignoreFirstLobbyStatusEvent = false;
                            return;
                        }

                        switch (status) {

                            case "playing":
                                actions.goToPlayState();
                                break;

                            case "over":
                                actions.goToOverState();
                                break;

                            case "waiting":
                                actions.goBackToLobbyState();
                                break;
                        }
                    });
                }

                @Override
                public void onError(NetworkError error, String errorMessage) {
                    Gdx.app.postRunnable(() -> {
                        actions.showError("Host left the lobby");
                        actions.goToMenuState();
                    });
                }
            });
        }

        actions.goToLobbyState(lobbyModel, lobbyModel.getIsHost());
    }

    private void showError(String message) {
        Gdx.app.postRunnable(() -> actions.showError(message));
    }
}
