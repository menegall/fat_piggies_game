package com.fatpiggies.game.controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.Snapshot;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.view.states.GameStateManager;

public class MainController implements IViewActions {

    public GameWorld world;
    public IPlayController playController;
    public LobbyController lobbyController;
    public AuthService auth;
    public DatabaseService dbs;
    public GameStateManager gsm;

    public MainController(AuthService auth, DatabaseService db) {
        lobbyController = new LobbyController(this, auth.getCurrentUserId(), db);
        this.dbs = db;
        gsm = GameStateManager.getInstance();
        gsm.setMenuState(this);
    }

    public void update(SpriteBatch batch, float dt) {
        Snapshot snapshot = new Snapshot();
        gsm.render(batch, snapshot, dt);
    }

    @Override
    public void onStartClicked() {
        //playController.startGame(lobbyController.getLobbyId(), playerIds, textureIds);
        lobbyController.leaveLobby();
    }

    @Override
    public void onHostLobbyClicked(String playerName) {
        playController = new HostPlayController(this);
        lobbyController.hostLobby(playerName);
    }

    @Override
    public void onJoinLobbyClicked(String playerName, String lobbyCode) {
        lobbyController.joinLobby(playerName, lobbyCode);
        playController = new ClientPlayController(this);
    }

    @Override
    public void onLeaveClicked() {
        playController.endGame(lobbyController.getLobbyId());
        gsm.setMenuState(this);
    }

    @Override
    public void onJoystickMoved(float x, float y) {
        if(playController != null){
            playController.updatePlayerInput(x, y);
        }
    }
}
