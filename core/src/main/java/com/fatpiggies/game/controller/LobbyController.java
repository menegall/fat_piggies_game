package com.fatpiggies.game.controller;

import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.view.states.GameStateManager;
import com.fatpiggies.game.view.states.LobbyState;

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

    public void joinLobby(String playerName, String lobbyCode, String lobbyId) {
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
        mc.gsm.setMenuState();
    }

    public boolean getIsHost(){
        return this.isHost;
    }

    public String getLobbyId(){
        return this.lobbyId;
    }

    private void changeToLobbyState(){
        mc.gsm.setLobbyState(isHost);
        // TODO put reading lobby code and give to view. PATRICK
        dbs.listenToPlayersSetup(lobbyId, new DatabaseService.PlayersSetupCallback() {
            @Override
            public void onPlayersSetupUpdated(Map<String, PlayerSetup> playersSetup) {
                // TODO Pass the player setup to the view. GABIN
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
    }

}
