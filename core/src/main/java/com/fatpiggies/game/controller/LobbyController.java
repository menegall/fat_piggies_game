package com.fatpiggies.game.controller;

import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.network.dto.PlayerSetup;

import java.util.Map;

public class LobbyController {
    private boolean isHost;
    private final String playerId;
    private String lobbyId;
    private final DatabaseService dbs;
    private final MainController mc;

    public LobbyController(MainController main, String playerId, DatabaseService dbs) {
        this.mc = main;
        this.dbs = dbs;
        this.playerId = playerId;
    }

    public void hostLobby(String playerName) {
        isHost = true;
        dbs.createLobby(playerId, playerName, new DatabaseService.LobbyCallback() {
            @Override
            public void onSuccess(String lobbyId) {
                // This post the state change back to the main thread
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        setLobbyId(lobbyId);
                        changeToLobbyState();
                    }
                });
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {
                showErrorInMainThread(errorMessage);
            }
        });
    }

    public void joinLobby(String playerName, String lobbyCode) {
        isHost = false;
        dbs.joinLobby(lobbyCode, playerId, playerName, new DatabaseService.LobbyCallback() {
            @Override
            public void onSuccess(String lobbyId) {
                // This post the state change back to the main thread
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        setLobbyId(lobbyId);
                        changeToLobbyState();
                    }
                });
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {
                showErrorInMainThread(errorMessage);
            }
        });
    }

    public void leaveLobby() {
        if(lobbyId != null) dbs.leaveLobby(lobbyId, playerId);
        dbs.stopListening();
        mc.gsm.setMenuState(mc);
    }

    public boolean getIsHost() {
        return isHost;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    private void changeToLobbyState(){
        dbs.getLobbyCodeOnce(lobbyId, new DatabaseService.CodeCallback() {
            @Override
            public void onCodeRetrieved(String code) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        mc.world.lobbyCode = code;
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                // TODO something with this maybe
            }
        });
        mc.gsm.setLobbyState(mc, isHost);
        dbs.listenToPlayersSetup(lobbyId, new DatabaseService.PlayersSetupCallback() {
            @Override
            public void onPlayersSetupUpdated(Map<String, PlayerSetup> playersSetup) {
                mc.world.playersSetup = playersSetup;
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {
                // TODO something with this maybe
            }
        });
    }

    private void showErrorInMainThread(String message){
        // This post the state change back to the main thread
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                mc.gsm.showError(message);
            }
        });
    }

    public void setLobbyId(String lobbyId){
        this.lobbyId=lobbyId;
        mc.world.lobbyId = lobbyId;
    }

}
