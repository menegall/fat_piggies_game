package com.fatpiggies.game.controller;

import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.view.states.GameStateManager;
import com.fatpiggies.game.view.states.LobbyState;

public class LobbyController implements IGameStateObserver {
    private boolean isHost;
    private final String playerId;
    private String lobbyId;

    private DatabaseService dbs;
    private GameStateManager gsm;
    private MainController mc;

    public LobbyController(MainController main, String playerId) {
        this.mc = main;
        this.gsm = mc.gsm;
        // TODO: Should this still be included?
        this.playerId = playerId;
    }

    public void hostLobby(String playerName) {
        isHost = true;
        gsm.setLobbyScreen();
        dbs.createLobby(playerId, playerName, lobby -> {
            this.lobbyId = lobby.getLobbyId();
            listen();
        });
        // create play controller as host
        mc.playController = new HostPlayController(mc);
    }

    public void joinLobby(String playerId) {
        isHost = false;
        dbs.joinLobby(lobbyCode, playerId, playerName, lobby -> {
            lobbyId = lobby.getLobbyId();
            listen();
        });

        // create playcontroller as client
        mc.playController = new ClientPlayController(mc);
    }

    public void leaveLobby() {
        if(lobbyId != null) dbs.leaveLobby(lobbyId, playerId);
        dbs.stopListening();

        mc.playController = null;
    }

    private void listen() {
        dbs.listenToLobbyInfo(lobbyId, lobby -> {
            if (lobby.isStarted()) {
                mc.startGame(isHost);
            }
        });
    }

    @Override
    public void update() {
        //
        System.out.println("start Lobby");
    }
}
