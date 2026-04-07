package com.fatpiggies.game.controller;

import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.view.states.GameStateManager;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.model.GameWorld;

public class MainController implements IViewActions {

    public GameWorld world;
    public IPlayController playController;
    public LobbyController lobbyController;
    public AuthService auth;
    public DatabaseService dbs;
    public GameStateManager gsm;

    // TODO: add AuthService and DBService into constructor, when firebase is merged
    public MainController(AuthService auth, DatabaseService db) {
        lobbyController = new LobbyController(this, auth.getCurrentUserId());
        gsm = GameStateManager.getInstance();
    }


    @Override
    public void onHostLobbyClicked(String name) {
        lobbyController.hostLobby(name);
    }

    @Override
    public void onJoinLobbyClicked(String name, String lobbyId) {
        lobbyController.joinLobby(name, lobbyId);
    }

    @Override
    public void onStartClicked() {

        if(lobbyController.getIsHost()){
            playController = new HostPlayController(this);
        }
        else {
            playController = new ClientPlayController(this);
        }
        //playController.startGame(lobbyController.getLobbyId(), playerIds, textureIds);
        lobbyController.leaveLobby();
    }

    @Override
    public void onLeaveClicked() {
        playController.endGame(lobbyController.getLobbyId());
        gsm.setMenuState(this);
    }

    @Override
    public void onJoystickMoved(float x, float y){
        if(playController != null){
            playController.updatePlayerInput(x, y);
        }
    }
}
