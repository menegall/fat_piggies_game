package com.fatpiggies.game.controller;

import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.view.states.GameOverState;
import com.fatpiggies.game.view.states.GameStateManager;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.view.states.GameStateManager;
import com.fatpiggies.game.view.states.PlayState;

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
        gsm = GameStateManager.getInstance(this);
    }

    @Override
    public void onPlayClicked() {

        if(lobbyController.getIsHost()){
            playController = new HostPlayController(this);
        }else {
            playController = new ClientPlayController(this);
        }

        playController.startGame(lobbyController.getLobbyId());
        lobbyController.leaveLobby();

    }

    @Override
    public void onHostLobbyClicked(String playerId) {
        lobbyController.hostLobby(playerId);
    }

    @Override
    public void onJoinLobbyClicked(String playerId) {
        lobbyController.joinLobby(playerId);
    }

    @Override
    public void onExitClicked() {
        playController.endGame(lobbyController.getLobbyId());
        gsm.set(new GameOverState());
    }

    @Override
    public void onJoystickMoved(double x, double y) {
        if(playController != null){
            playController.movePig(x, y);
        }
    }
}
