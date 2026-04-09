package com.fatpiggies.game.controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fatpiggies.game.controller.mainControllerInterfaces.ILobbyActions;
import com.fatpiggies.game.controller.mainControllerInterfaces.IPlayActions;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.IReadOnlyGameWorld;
import com.fatpiggies.game.model.IReadOnlyLobbyModel;
import com.fatpiggies.game.model.LobbyModel;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.view.TextureManager;
import com.fatpiggies.game.view.states.GameStateManager;

public class MainController implements IViewActions, ILobbyActions, IPlayActions {

    private final LobbyModel lobbyModel;
    private final LobbyController lobbyController;
    private IPlayController playController;
    private final AuthService auth;
    private final DatabaseService dbs;
    private final GameStateManager gsm;
    private boolean gameIsPlaying = false;

    public MainController(AuthService auth, DatabaseService db) {
        this.auth = auth;
        this.dbs = db;

        lobbyModel = new LobbyModel();

        lobbyController = new LobbyController(
            this,
            auth.getCurrentUserId(),
            db,
            lobbyModel
        );

        gsm = GameStateManager.getInstance();
        gsm.setMenuState(this);
    }

    public void update(SpriteBatch batch, float dt) {
        if (gameIsPlaying && playController != null) {
            playController.updateWorld(dt);
        }

        TextureManager.update(dt); // For the right animated frame to be changed
        gsm.render(batch, dt);
    }

    // ================= VIEW ACTIONS =================

    @Override
    public void onStartClicked() {
        playController = new HostPlayController(this, lobbyModel.getLobbyId());
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
        }
        lobbyController.leaveLobby();
        gsm.popToMenu();
    }

    @Override
    public void onJoystickMoved(float x, float y) {
        if (playController != null) {
            playController.updatePlayerInput(x, y);
        }
    }

    @Override
    public void onLobbyClicked(){
        gsm.popToLobby();
    }

    // ================= LOBBY ACTIONS =================

    @Override
    public void goToLobbyState(IReadOnlyLobbyModel lobbyModel, boolean isHost) {
        gsm.pushLobbyState(this, lobbyModel, isHost);
    }

    @Override
    public void startClientGame(String lobbyId) {
        playController = new ClientPlayController(this, lobbyId);
        playController.startGame(lobbyId);
    }

    @Override
    public void endGame(String lobbyId) {
        if (playController != null) {
            playController.endGame(lobbyId);
        }
    }

    @Override
    public void showError(String message) {
        gsm.showError(message);
    }

    // ================= PLAY ACTIONS =================

    @Override
    public String getCurrentUserId() {
        return auth.getCurrentUserId();
    }

    @Override
    public LobbyModel getLobbyModel() {
        return lobbyModel;
    }

    @Override
    public void goToPlayState(IReadOnlyGameWorld gameWorld) {
        gsm.pushPlayState(this, gameWorld, lobbyModel.getPlayerId());
    }

    @Override
    public void goToGameOverState(boolean isHost) {
        gsm.pushOverState(this, lobbyModel, isHost);
    }

    @Override
    public void setGameIsPlaying(boolean playing) {
        this.gameIsPlaying = playing;
    }

    @Override
    public void clearPlayController() {
        playController = null;
    }

    @Override
    public void startGameOnServer(String lobbyId) {
        dbs.startGame(lobbyId);
    }

    @Override
    public void endGameOnServer(String lobbyId) {
        dbs.endGame(lobbyId);
    }
}
