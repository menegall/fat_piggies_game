package com.fatpiggies.game.controller;

import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.view.states.GameStateManager;
import com.fatpiggies.game.view.states.LobbyState;

public class LobbyController {
    private boolean isHost;
    private final String playerId;
    private final String playerName;
    private String lobbyId;

    private DatabaseService dbs;
    private GameStateManager gsm;
    private MainController mc;

    public LobbyController(MainController main, String playerId, String playerName) {
        this.mc = main;
        this.gsm = mc.gsm;
        // TODO: Should this still be included?
        this.gsm.set(new LobbyState(isHost));
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public void startGame() {
        isHost = true;
        gsm.setLobbyScreen();
        dbs.createLobby(playerId, playerName, lobby -> {
            this.lobbyId = lobby.getLobbyId();
            listen();
        });
    }

    public void joinLobby(String playerId) {
        isHost = false;
        dbs.joinLobby(lobbyCode, playerId, playerName, lobby -> {
            lobbyId = lobby.getLobbyId();
            listen();
        });
    }

    public void leaveLobby() {
        if(lobbyId != null) dbs.leaveLobby(lobbyId, playerId);
        dbs.stopListening();
    }

    public void startGame() {
        if (isHost) {
            dbs.startGame(lobbyId);
            gsm.set(new PlayState());
        };
    }

    private void listen() {
        dbs.listenToLobbyInfo(lobbyId, lobby -> {
            if (lobby.isStarted()) {
                mc.startGame(isHost);
            }
        });
    }
}
