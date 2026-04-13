package com.fatpiggies.game.view.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.setting.SoundsManager;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.model.IReadOnlyLobbyModel;
import com.fatpiggies.game.setting.VibrationManager;
import com.fatpiggies.game.view.PlayerColor;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

import java.util.Map;

public class LobbyState extends State {

    private static final float MENU_SIZE_X_RATIO = 0.35f;
    private static final float MENU_SIZE_Y_RATIO = 0.84f;

    private static final float BUTTON_WIDTH_RATIO = 0.2f;
    private static final float BUTTON_HEIGHT_RATIO = 0.1f;

    private static final float PADDING_RATIO = 0.015f;
    private static final float START_BUTTON_TOP_PAD = 0.02f;

    private static final float TITLE_SCALE = 0.0018f;
    private static final float SUBTITLE_SCALE = 0.0013f;
    private static final float IMAGE_SIZE_X = 0.04f;
    private static final float IMAGE_SIZE_Y = 0.1f;
    private static final float PAD_LEFT = 0.03f;
    private static final float CODE_SCALE = 0.0025f;
    private static final float BUTTON_TEXT_SCALE = 0.0015f;

    private static final float CODE_X_RATIO = 0.73f;
    private static final float CODE_Y_RATIO = 0.5f;

    private static final float COPY_DURATION = 1.5f;

    private final boolean isHost;
    private final IReadOnlyLobbyModel lobbyModel;

    private final Array<String> lastNames = new Array<>();

    private Table playersTable;
    private TextButton startButton;
    private TextButton leaveButton;
    private Label lobbyCodeLabel;

    private final TextureRegion menuBackground;
    private final TextureRegion playBackground;

    private float copyTimer = 0f;

    public LobbyState(IViewActions viewActions, IReadOnlyLobbyModel lobbyModel, boolean isHost) {
        super(viewActions);
        this.lobbyModel = lobbyModel;
        this.isHost = isHost;

        menuBackground = TextureManager.getFrame(TextureId.MENU_BACKGROUND);
        playBackground = TextureManager.getFrame(TextureId.PLAY_BACKGROUND);

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

        startButton = new TextButton("Start", skin);
        startButton.getLabel().setFontScale(screenHeight * BUTTON_TEXT_SCALE);
        startButton.setDisabled(true);
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onStartClicked();
            }
        });

        playersTable = new Table();

        Table root = new Table();
        root.setFillParent(true);
        root.defaults().pad(screenHeight * PADDING_RATIO);

        Label playerLabel = new Label("Players", skin);
        playerLabel.setFontScale(screenHeight * TITLE_SCALE);

        root.add(playerLabel).row();
        root.add(playersTable).row();

        if (isHost) {
            root.add(startButton)
                .width(screenWidth * BUTTON_WIDTH_RATIO)
                .height(screenHeight * BUTTON_HEIGHT_RATIO)
                .padTop(screenHeight * START_BUTTON_TOP_PAD)
                .row();
        }

        root.add(leaveButton)
            .width(screenWidth * BUTTON_WIDTH_RATIO)
            .height(screenHeight * BUTTON_HEIGHT_RATIO);

        stage.addActor(root);

        lobbyCodeLabel = new Label("CODE ----", skin);
        lobbyCodeLabel.setFontScale(screenHeight * CODE_SCALE);
        lobbyCodeLabel.setPosition(
            screenWidth * CODE_X_RATIO,
            screenHeight * CODE_Y_RATIO
        );
        lobbyCodeLabel.setWrap(true);

        lobbyCodeLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String code = lobbyModel.getLobbyCode();

                if (code != null) {
                    Gdx.app.getClipboard().setContents(code);
                    VibrationManager.vibrate(200);
                    lobbyCodeLabel.setText("COPIED !");
                    copyTimer = COPY_DURATION;
                }
            }
        });

        stage.addActor(lobbyCodeLabel);
    }

    private void onStartClicked() {
        viewActions.onStartClicked();
        VibrationManager.vibrate(200);
        SoundsManager.playButton(1f);
    }

    private void onLeaveClicked() {
        viewActions.onLeaveClicked();
        VibrationManager.vibrate(200);
        SoundsManager.playButton(1f);
    }

    @Override
    public void update(float dt) {

        updatePlayers(lobbyModel.getPlayerSetups());
        lobbyCodeLabel.setText("CODE \n" + lobbyModel.getLobbyCode());

        if (copyTimer > 0) {
            copyTimer -= dt;
            if (copyTimer <= 0) {
                lobbyCodeLabel.setText("CODE \n" + lobbyModel.getLobbyCode());
            }
        }

        stage.act(dt);
    }

    @Override
    public void render(SpriteBatch sb) {

        sb.begin();

        float sizeX = screenWidth * MENU_SIZE_X_RATIO;
        float sizeY = screenHeight * MENU_SIZE_Y_RATIO;
        float x = (screenWidth - sizeX) / 2f;
        float y = (screenHeight - sizeY) / 2f;

        sb.draw(playBackground, 0, 0, screenWidth, screenHeight);
        sb.draw(menuBackground, x, y, sizeX, sizeY);

        sb.end();

        stage.draw();
    }

    private void updatePlayers(Map<String, PlayerSetup> players) {
        rebuildPlayers(players);

        if (isHost) {
            startButton.setDisabled(players.size() < 2);
        }
    }

    private void rebuildPlayers(Map<String, PlayerSetup> players) {
        playersTable.clear();

        for (PlayerSetup setup : players.values()) {

            if (setup == null || setup.name == null) continue;

            Image pig = new Image(TextureManager.getFrame(
                TextureManager.getLifeTextureId(
                    TextureManager.getPigTextureId(PlayerColor.valueOf(setup.color))
                )
            ));

            float sizeX = screenWidth * IMAGE_SIZE_X;
            float sizeY = screenHeight * IMAGE_SIZE_Y;

            Label label = new Label(setup.name, skin);
            label.setFontScale(screenHeight * SUBTITLE_SCALE);

            playersTable.add(pig).size(sizeX, sizeY);
            playersTable.add(label).padLeft(screenHeight * PAD_LEFT).row();
        }
    }
}
