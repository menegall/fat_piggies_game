package com.fatpiggies.game.view.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.model.IReadOnlyLobbyModel;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.setting.SoundsManager;
import com.fatpiggies.game.setting.VibrationManager;
import com.fatpiggies.game.view.PlayerColor;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

import java.util.LinkedHashMap;
import java.util.Map;

public class OverState extends State {

    // ================= CONSTANTS =================
    private static final float BUTTON_WIDTH = 0.11f;
    private static final float BUTTON_HEIGHT = 0.08f;
    private static final float BUTTON_TEXT_SCALE = 0.001f;

    private static final float SCORE_PAD_TOP = 0.08f;
    private static final float SCORE_PAD_LEFT = 0.06f;

    private static final float PODIUM_WIDTH = 0.53f;
    private static final float PODIUM_HEIGHT = 0.7f;
    private static final float PODIUM_ANCHOR_X = 0.5f;
    private static final float PODIUM_ANCHOR_Y = 0.15f;

    private static final float PIG_SIZE_RATIO = 0.25f;

    private static final float FIRST_X = 0.5f;
    private static final float FIRST_Y = 0.655f;

    private static final float SECOND_X = 0.25f;
    private static final float SECOND_Y = 0.52f;

    private static final float THIRD_X = 0.75f;
    private static final float THIRD_Y = 0.46f;

    private static final float CROWN_X = 0.495f;
    private static final float CROWN_Y = 0.98f;
    private static final float CROWN_HEIGHT_RATIO = 0.6f;

    private static final float SCORE_SIZE_X = 0.33f;
    private static final float SCORE_SIZE_Y = 0.6f;
    private static final float SCORE_ANCHOR_X = 0.03f;
    private static final float SCORE_ANCHOR_Y = 0.35f;

    private static final float MENU_PAD_TOP = 0.5f;
    private static final float MENU_PAD_LEFT = 0.8f;

    private static final float MENU_SIZE_X = 0.17f;
    private static final float MENU_SIZE_Y = 0.3f;
    private static final float MENU_ANCHOR_X = 0.9f;
    private static final float MENU_ANCHOR_Y = 0.25f;

    // ================= DATA =================
    private final boolean isHost;
    private final IReadOnlyLobbyModel lobbyModel;

    private Array<String> rankedNames = new Array<>();
    private Array<String> lastNames = new Array<>();

    // ================= UI =================
    private TextButton lobbyButton;
    private TextButton leaveButton;
    private Table scoreTable;

    private final TextureRegion menuBackground;
    private final TextureRegion playBackground;
    private final TextureRegion overBackground;
    private final TextureRegion podium;

    public OverState(IViewActions viewActions, IReadOnlyLobbyModel lobbyModel, boolean isHost) {
        super(viewActions);
        this.lobbyModel = lobbyModel;
        this.isHost = isHost;

        menuBackground = TextureManager.getFrame(TextureId.MENU_BACKGROUND);
        playBackground = TextureManager.getFrame(TextureId.PLAY_BACKGROUND);
        overBackground = TextureManager.getFrame(TextureId.OVER_BACKGROUND);
        podium = TextureManager.getFrame(TextureId.PODIUM);

        createUI();
    }

    private void createUI() {
        leaveButton = new TextButton("Leave", skin);
        leaveButton.getLabel().setFontScale(screenHeight * BUTTON_TEXT_SCALE);
        leaveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onLeaveClicked();
            }
        });

        lobbyButton = new TextButton("Lobby", skin);
        lobbyButton.getLabel().setFontScale(screenHeight * BUTTON_TEXT_SCALE);
        lobbyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onLobbyClicked();
            }
        });

        Table menuTable = new Table();
        menuTable.setFillParent(true);
        menuTable.defaults().pad(screenHeight * 0.02f);
        menuTable.padTop(screenHeight * MENU_PAD_TOP);
        menuTable.padLeft(screenWidth * MENU_PAD_LEFT);

        if (isHost) {
            menuTable.add(lobbyButton)
                .width(screenWidth * BUTTON_WIDTH)
                .height(screenHeight * BUTTON_HEIGHT)
                .row();
        }

        menuTable.add(leaveButton)
            .width(screenWidth * BUTTON_WIDTH)
            .height(screenHeight * BUTTON_HEIGHT);

        stage.addActor(menuTable);

        scoreTable = new Table();
        scoreTable.setFillParent(true);
        scoreTable.top().left();
        scoreTable.padTop(screenHeight * SCORE_PAD_TOP);
        scoreTable.padLeft(screenWidth * SCORE_PAD_LEFT);

        stage.addActor(scoreTable);
    }

    // ================= ACTIONS =================
    private void onLobbyClicked() {
        viewActions.onLobbyClicked();
        VibrationManager.vibrate(200);
        SoundsManager.playButton(1f);
    }

    private void onLeaveClicked() {
        viewActions.onLeaveClicked();
        VibrationManager.vibrate(200);
        SoundsManager.playButton(1f);
    }



    private String getRankLabel(int index) {
        switch (index) {
            case 0: return "1st.";
            case 1: return "2nd.";
            case 2: return "3rd.";
            default: return (index + 1) + "th.";
        }
    }

    private String getCoinRecompense(int index) {
        switch (index) {
            case 0: return " + 20 coins.";
            case 1: return " + 10 coins.";
            case 2: return " +  5 coin.";
            default: return (index + 1) + "";
        }
    }

    private TextureId getPigTextureForPlayer(String playerName) {
        for (PlayerSetup setup : lobbyModel.getPlayerSetups().values()) {
            if (setup != null && setup.name != null && setup.name.equals(playerName)) {
                return TextureManager.getOverTextureId(TextureManager.getPigTextureId(PlayerColor.valueOf(setup.color)));
            }
        }

        return TextureManager.getOverTextureId(TextureManager.getPigTextureId(PlayerColor.BLUE));
    }



    // ================= SCORE =================
    private void updateScoreBoard(Array<String> names) {
        scoreTable.clear();

        for (int i = 0; i < names.size; i++) {
            Label rankLabel = new Label(getRankLabel(i), skin);
            Label nameLabel = new Label(names.get(i) + getCoinRecompense(i), skin);

            scoreTable.add(rankLabel).left().padRight(10);
            scoreTable.add(nameLabel).left().row();
        }
    }

    private Array<String> buildRankedNames(Array<String> sourceNames) {
        Array<String> result = new Array<>(); // First of the given list is last ranked
        for (int i = sourceNames.size - 1; i >= 0; i--) {
            System.out.println(sourceNames);
            result.add(sourceNames.get(i));
        }
        return result;
    }

    @Override
    public void update(float dt) {
        Array<String> currentNames = new Array<>();

        LinkedHashMap<String, PlayerSetup> ranking = lobbyModel.getFinalRanking();

        if (ranking == null || ranking.isEmpty()) return;

        for (PlayerSetup setup : ranking.values()) {
            if (setup != null && setup.name != null) {
                currentNames.add(setup.name);
            }
        }

        if (!currentNames.equals(lastNames)) {
            lastNames = new Array<>(currentNames);
            rankedNames = buildRankedNames(lastNames);
            updateScoreBoard(rankedNames);
        }
    }

    // ================= RENDER =================
    @Override
    public void render(SpriteBatch sb) {
        sb.begin();

        // Background
        sb.draw(playBackground, 0, 0, screenWidth, screenHeight);

        // Podium
        float podiumW = screenWidth * PODIUM_WIDTH;
        float podiumH = screenHeight * PODIUM_HEIGHT;

        float podiumX = screenWidth * PODIUM_ANCHOR_X - podiumW / 2f;
        float podiumY = screenHeight * PODIUM_ANCHOR_Y - podiumH / 4f;

        sb.draw(podium, podiumX, podiumY, podiumW, podiumH);

        float pigSize = podiumW * PIG_SIZE_RATIO;

        // 1st
        if (rankedNames.size > 0) {
            sb.draw(
                TextureManager.getFrame(getPigTextureForPlayer(rankedNames.get(0))),
                podiumX + podiumW * FIRST_X - pigSize / 2f,
                podiumY + podiumH * FIRST_Y,
                pigSize,
                pigSize
            );
        }

        // 2nd
        if (rankedNames.size > 1) {
            sb.draw(
                TextureManager.getFrame(getPigTextureForPlayer(rankedNames.get(1))),
                podiumX + podiumW * SECOND_X - pigSize / 2f,
                podiumY + podiumH * SECOND_Y,
                pigSize,
                pigSize
            );
        }

        // 3rd
        if (rankedNames.size > 2) {
            sb.draw(
                TextureManager.getFrame(getPigTextureForPlayer(rankedNames.get(2))),
                podiumX + podiumW * THIRD_X - pigSize / 2f,
                podiumY + podiumH * THIRD_Y,
                pigSize,
                pigSize
            );
        }

        // Crown
        if (rankedNames.size > 0) {
            sb.draw(
                TextureManager.getFrame(TextureId.CROWN),
                podiumX + podiumW * CROWN_X - pigSize / 2f,
                podiumY + podiumH * CROWN_Y,
                pigSize,
                pigSize * CROWN_HEIGHT_RATIO
            );
        }

        // Score panel
        float scoreSizeX = screenWidth * SCORE_SIZE_X;
        float scoreSizeY = screenHeight * SCORE_SIZE_Y;
        float scoreX = screenWidth * SCORE_ANCHOR_X;
        float scoreY = screenHeight * SCORE_ANCHOR_Y;

        sb.draw(overBackground, scoreX, scoreY, scoreSizeX, scoreSizeY);

        // Menu panel
        float menuSizeX = screenWidth * MENU_SIZE_X;
        float menuSizeY = screenHeight * MENU_SIZE_Y;
        float menuX = screenWidth * MENU_ANCHOR_X - menuSizeX / 2f;
        float menuY = screenHeight * MENU_ANCHOR_Y - menuSizeY / 2f;

        sb.draw(menuBackground, menuX, menuY, menuSizeX, menuSizeY);

        sb.end();

        stage.draw();
    }
}
