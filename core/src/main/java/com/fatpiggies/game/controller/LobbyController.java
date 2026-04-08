package com.fatpiggies.game.controller;

import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.model.LobbyModel;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.network.dto.PlayerSetup;

import java.util.Map;

public class LobbyController {
    private final String playerId;
    private final DatabaseService dbs;
    private final MainController main;
    private final LobbyModel lobbyModel;

    public LobbyController(MainController main, String playerId, DatabaseService dbs, LobbyModel lobbyModel) {
        this.main = main;
        this.dbs = dbs;
        this.playerId = playerId;
        this.lobbyModel = lobbyModel;
    }

    public void hostLobby(String playerName) {
        lobbyModel.setIsHost(true);

        dbs.createLobby(playerId, playerName, new DatabaseService.LobbyCallback() {
            @Override
            public void onSuccess(String lobbyId) {
                Gdx.app.postRunnable(() -> {
                    lobbyModel.setLobbyId(lobbyId);
                    changeToLobbyState();
                });
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {
                showErrorInMainThread(errorMessage);
            }
        });
    }

    public void joinLobby(String playerName, String lobbyCode) {
        lobbyModel.setIsHost(false);

        dbs.joinLobby(lobbyCode, playerId, playerName, new DatabaseService.LobbyCallback() {
            @Override
            public void onSuccess(String lobbyId) {
                Gdx.app.postRunnable(() -> {

                    lobbyModel.setLobbyId(lobbyId);

                    changeToLobbyState();
                });
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {
                showErrorInMainThread(errorMessage);
            }
        });
    }

    public void leaveLobby() {
        if (lobbyModel.getLobbyId() != null) {
            dbs.leaveLobby(lobbyModel.getLobbyId(), playerId);
        }

        dbs.stopListening();
        main.gsm.setMenuState(main);
    }

    private void changeToLobbyState() {

        dbs.getLobbyCodeOnce(lobbyModel.getLobbyId(), new DatabaseService.CodeCallback() {
            @Override
            public void onCodeRetrieved(String code) {
                Gdx.app.postRunnable(() -> {
                    lobbyModel.setLobbyCode(code);
                });
            }

            @Override
            public void onError(String errorMessage) {}
        });

        dbs.listenToPlayersSetup(lobbyModel.getLobbyId(), new DatabaseService.PlayersSetupCallback() {
            @Override
            public void onPlayersSetupUpdated(Map<String, PlayerSetup> playersSetup) {
                lobbyModel.setPlayersSetup(playersSetup);
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {}
        });
        if (!lobbyModel.getIsHost())
            main.dbs.listenToLobbyStatus(lobbyModel.getLobbyId(), new DatabaseService.LobbyStatusCallback() {
                @Override
                public void onStatusUpdated(String status) {
                    if ("playing".equals(status)) {
                        Gdx.app.postRunnable(() -> {
                            main.playController = new ClientPlayController(main, lobbyModel.getLobbyId());
                            main.playController.startGame(lobbyModel.getLobbyId());
                            main.gsm.setPlayState(main, main.world);
                        });

                    }
                }

                @Override
                public void onError(NetworkError error, String errorMessage) {
                    showErrorInMainThread(errorMessage);
                }
            });

        main.gsm.setLobbyState(main, lobbyModel, lobbyModel.getIsHost());
    }

    private void showErrorInMainThread(String message){
        Gdx.app.postRunnable(() -> main.gsm.showError(message));
    }
}
