package com.fatpiggies.game.controller;

import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.view.states.GameStateManager;
import com.fatpiggies.game.view.states.LobbyState;

public class LobbyController {
    private boolean isHost;
    private String playerId;
    private String playerName;
    private String lobbyCode;

    private DatabaseService dbs;
    private GameStateManager gsm;

    // Is this the right way to get callback?
    private DatabaseService.LobbyCallback callback;

    public LobbyController(MainController main) {
        gsm = GameStateManager.getInstance();
        gsm.set(new LobbyState(isHost));
    }

    // TODO: Could host- and createLobby be joined together -> startGame()?
    public void hostLobby(String lobbyCode) {

    }

    public void createLobby() {
        isHost = true;

        // TODO: This will be implemented in gsm according to view?
        // gsm.setLobbyScreen();
        dbs.createLobby(playerId, playerName, callback);
        // Should a listen function be used to listen to changes in lobby status?
    }

    public void joinLobby(String playerId) {
        isHost = false;
        dbs.joinLobby(lobbyCode, playerId, playerName, callback);
        // Should a listen function be used to listen to changes in lobby status?
    }

    public void leaveLobby() {
        // lobbyCode = lobbyId?
        dbs.leaveLobby(lobbyCode, playerId);
        // gsm.set(new PlayState());
    }

}
