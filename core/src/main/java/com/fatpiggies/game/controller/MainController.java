package com.fatpiggies.game.controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.LobbyModel;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.view.states.GameStateManager;

public class MainController implements IViewActions {
    public GameWorld world;
    public LobbyModel lobbyModel;
    public IPlayController playController;
    public LobbyController lobbyController;
    public AuthService auth;
    public DatabaseService dbs;
    public GameStateManager gsm;

    public MainController(AuthService auth, DatabaseService db) {
        lobbyModel = new LobbyModel();
        lobbyController = new LobbyController(this, auth.getCurrentUserId(), db, lobbyModel);
        this.dbs = db;
        gsm = GameStateManager.getInstance();
        gsm.setMenuState(this);
    }

    public void update(SpriteBatch batch, float dt) {
        gsm.render(batch, dt);
        //world.update(dt);
    }

    @Override
    public void onStartClicked() {
        if(lobbyModel.getIsHost()){
            playController = new HostPlayController(this, lobbyModel.getLobbyId());
        }
        else {
            playController = new ClientPlayController(this, lobbyModel.getLobbyId());
        }


        playController.startGame(lobbyModel.getLobbyId());
    }

    @Override
    public void onHostLobbyClicked(String playerName) {
        lobbyController.hostLobby(playerName);
    }

    @Override
    public void onJoinLobbyClicked(String playerName, String lobbyCode) {
        lobbyController.joinLobby(playerName, lobbyCode);
    }

    @Override
    public void onLeaveClicked() {
        if (playController != null) {
            playController.endGame(lobbyModel.getLobbyId());
        } else {
            lobbyController.leaveLobby();
        }
        gsm.setMenuState(this);
    }

    @Override
    public void onJoystickMoved(float x, float y) {
        if(playController != null){
            playController.updatePlayerInput(x, y);
        }
    }
}
