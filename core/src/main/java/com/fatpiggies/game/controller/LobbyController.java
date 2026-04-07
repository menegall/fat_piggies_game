package com.fatpiggies.game.controller;

import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.view.states.GameStateManager;
import com.fatpiggies.game.view.states.LobbyState;

public class LobbyController {
    private boolean isHost;
    private final String playerId;
    private String lobbyId;

    private DatabaseService dbs;
    private MainController mc;

    public LobbyController(MainController main, String playerId) {
        this.mc = main;
        // TODO: Should this still be included?
        this.playerId = playerId;
    }

    public void hostLobby(String name) {
        isHost = true;
        mc.gsm.setLobbyState(mc, getIsHost());
    }

    public void joinLobby(String name, String lobbyId) {
        isHost = false;
        this.lobbyId = lobbyId;
        mc.gsm.setLobbyState(mc, getIsHost());
    }

    public void leaveLobby() {
        if(lobbyId != null) dbs.leaveLobby(lobbyId, playerId);
        dbs.stopListening();

        mc.playController = null;
    }

    public boolean getIsHost(){return isHost;}

    public String getLobbyId(){return lobbyId;}

}
