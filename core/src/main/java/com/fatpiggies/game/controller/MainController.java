package com.fatpiggies.game.controller;

import static com.fatpiggies.game.model.utils.GameConstants.SEND_THRESHOLD;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fatpiggies.game.controller.mainControllerInterfaces.ILobbyActions;
import com.fatpiggies.game.controller.mainControllerInterfaces.IPlayActions;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.model.IReadOnlyLobbyModel;
import com.fatpiggies.game.model.LobbyModel;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.setting.PreferencesManager;
import com.fatpiggies.game.view.PlayerColor;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;
import com.fatpiggies.game.view.states.GameStateManager;

import java.util.List;

public class MainController implements IViewActions, ILobbyActions, IPlayActions {

    private final LobbyModel lobbyModel;
    private final LobbyController lobbyController;
    private final ShopController shopController;

    private final AuthService auth;
    private final DatabaseService dbs;
    private IPlayController playController;
    private float timerNetwork = 0f;
    private boolean returningToLobby = false;

    public MainController(AuthService auth, DatabaseService db) {
        this.auth = auth;
        this.dbs = db;

        lobbyModel = new LobbyModel();
        shopController = new ShopController();

        lobbyController = new LobbyController(
            this,
            auth.getCurrentUserId(),
            db,
            lobbyModel
        );

        GameStateManager.getInstance().setMenuState(this);
    }

    // ================= UPDATE =================

    public void update(SpriteBatch batch, float dt) {

        if (playController != null) {
            playController.updateWorld(dt);

            timerNetwork += dt;
            if (timerNetwork >= SEND_THRESHOLD) {
                playController.sendToServer(dbs, timerNetwork);
                timerNetwork = 0f;
            }
        }

        TextureManager.update(dt);
        GameStateManager.getInstance().render(batch, dt);
    }

    // ================= GAME FLOW =================

    private void startHostGame() {
        if (lobbyModel.getLobbyId() == null) return;

        playController = new HostPlayController(this, lobbyModel.getLobbyId());

        dbs.startGame(lobbyModel.getLobbyId());

        playController.startGame();
        playController.attachPlayListener(dbs);

        GameStateManager.getInstance().pushPlayState(this,
            playController.getWorld(),
            lobbyModel.getPlayerId(),
            true
        );
    }

    private void startClientGame() {
        if (lobbyModel.getLobbyId() == null) return;

        playController = new ClientPlayController(this, lobbyModel.getLobbyId());

        playController.startGame();
        playController.attachPlayListener(dbs);

        GameStateManager.getInstance().pushPlayState(this,
            playController.getWorld(),
            lobbyModel.getPlayerId(),
            false
        );
    }

    private void quitPlayState() {
        if (playController == null) return;

        playController.endGame();
        playController = null;
        timerNetwork = 0f;
    }

    private void rewardIfWinner(List<String> ranking) {
        if (ranking == null || ranking.isEmpty()) return;

        int size = ranking.size();
        String playerId = getCurrentUserId();

        // 1st
        if (size >= 1 && ranking.get(size - 1).equals(playerId)) {
            shopController.addCoins(20);
        }
        // 2nd
        else if (size >= 2 && ranking.get(size - 2).equals(playerId)) {
            shopController.addCoins(10);
        }
        // 3rd
        else if (size >= 3 && ranking.get(size - 3).equals(playerId)) {
            shopController.addCoins(5);
        }
    }

    public void resize(int width, int height) {
        GameStateManager.getInstance().resize(width, height);
    }

    // ================= VIEW ACTIONS =================

    @Override
    public void onHostLobbyClicked(String playerName, PlayerColor playerColor) {
        String uid = auth.getCurrentUserId();
        if (uid == null) {
            showError(NetworkError.LOGIN_REQUIRED);
            return;
        }
        lobbyController.hostLobby(playerName, playerColor);
    }

    @Override
    public void onJoinLobbyClicked(String playerName, String lobbyCode, PlayerColor playerColor) {
        String uid = auth.getCurrentUserId();
        if (uid == null) {
            showError(NetworkError.LOGIN_REQUIRED);
            return;
        }
        lobbyController.joinLobby(playerName, lobbyCode, playerColor);
    }

    @Override
    public void onShopClicked(){
        GameStateManager.getInstance().pushShopState(this);
    }

    @Override
    public void onMenuClicked(){
        GameStateManager.getInstance().popToMenu();
    }

    @Override
    public void onBuyBackgroundClicked(TextureId bg) {
        boolean success = shopController.buy(bg);

        if (success) {
            shopController.select(bg);
        }
    }

    @Override
    public void onSelectBackground(TextureId bg) {
        if (shopController.isUnlocked(bg)) {
            shopController.select(bg);
        }
    }

    @Override
    public boolean isBackgroundUnlocked(TextureId bg) {
        return shopController.isUnlocked(bg);
    }

    @Override
    public int getCoins() {
        return shopController.getCoins();
    }

    @Override
    public int getPrice(TextureId bg) {
        return shopController.getPrice(bg);
    }

    @Override
    public void onStartClicked() {
        startHostGame();
    }

    @Override
    public void onLeaveClicked() {
        goToMenuState();
    }

    @Override
    public void onJoystickMoved(float x, float y) {
        if (playController != null) {
            playController.updatePlayerInput(x, y);
        }
    }

    @Override
    public void onLobbyClicked() {
        goBackToLobbyState();
    }

    // ================= LOBBY ACTIONS =================

    @Override
    public void goToMenuState() {
        quitPlayState();

        dbs.stopListening();
        lobbyController.leaveLobby();

        GameStateManager.getInstance().popToMenu();
    }

    @Override
    public void goToLobbyState(IReadOnlyLobbyModel lobbyModel, boolean isHost) {
        returningToLobby = false;
        GameStateManager.getInstance().pushLobbyState(this, lobbyModel, isHost);
    }

    @Override
    public void goBackToLobbyState() {
        if (returningToLobby) return;
        returningToLobby = true;

        quitPlayState();
        lobbyController.goBackToLobby();

        GameStateManager.getInstance().popToLobby();

        returningToLobby = false;
    }

    @Override
    public void goToPlayState() {
        startClientGame();
    }

    @Override
    public void goToOverState() {
        quitPlayState();
        dbs.getFinalRank(lobbyModel.getLobbyId(), new DatabaseService.FinalRankCallback() {
            @Override
            public void onRankRetrieved(List<String> rankedPlayerIds) {
                Gdx.app.postRunnable(() -> {
                    lobbyModel.setFinalRanking(rankedPlayerIds);
                    rewardIfWinner(rankedPlayerIds);
                });
            }

            @Override
            public void onError(NetworkError error) {
                Gdx.app.postRunnable(() -> {
                    showError(error);
                });
            }
        });
        GameStateManager.getInstance().pushOverState(this, lobbyModel, lobbyModel.getIsHost());
    }

    @Override
    public void showError(NetworkError error) {
        GameStateManager.getInstance().showError(error);
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
    public void onGameFinishedByHost(List<String> finalRank) {

        if (playController == null) return;

        if (lobbyModel.getIsHost()) {
            dbs.pushFinalRank(lobbyModel.getLobbyId(), finalRank);
            dbs.endGame(lobbyModel.getLobbyId());
            rewardIfWinner(finalRank);
        }
        quitPlayState();
        GameStateManager.getInstance().pushOverState(this, lobbyModel, lobbyModel.getIsHost());
    }

    @Override
    public float getTimerNetwork() {
        return timerNetwork;
    }

    @Override
    public void showMessage(String message) {
        GameStateManager.getInstance().showMessage(message);
    }


    // ================= APP LIFECYCLE =================

    public void pause() {
        // When the app goes to the background (Home button pressed),
        // we disconnect the user to avoid leaving a "ghost" player in the lobby/game.

        // We only need to disconnect if the user is actually in a lobby or a game.
        if (lobbyModel.getLobbyId() != null) {
            Gdx.app.log("MainController", "App paused: leaving lobby and returning to menu.");
            goToMenuState();
        }
    }

    public void dispose() {
        if (lobbyModel.getLobbyId() != null) {
            goToMenuState();
        }

        dbs.stopListening();

        lobbyModel.setLobbyId(null);
        lobbyModel.setPlayerId(null);
    }
}
