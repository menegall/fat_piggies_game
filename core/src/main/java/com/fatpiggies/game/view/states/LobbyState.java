package com.fatpiggies.game.view.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.fatpiggies.game.audio.SoundsManager;
import com.fatpiggies.game.controller.IViewActions;
import com.fatpiggies.game.model.IReadOnlyLobbyModel;
import com.fatpiggies.game.assets.TextureId;
import com.fatpiggies.game.assets.TextureManager;

public class LobbyState extends State {
    private final boolean isHost;

    private Table playersTable;
    private TextButton startButton;
    private TextButton leaveButton;

    private final Texture menuBackground;
    private final Texture playBackground;
    private float copyTimer = 0f;

    private final IReadOnlyLobbyModel lobbyModel;
    private Array<String> lastNames = new Array<>();
    private Label LobbyCodeLabel;

    public LobbyState(IViewActions viewActions, IReadOnlyLobbyModel lobbyModel, boolean isHost) {
        super(viewActions);
        this.lobbyModel =lobbyModel;
        this.isHost = isHost;
        menuBackground = TextureManager.getTexture(TextureId.MENU_BACKGROUND);
        playBackground = TextureManager.getTexture(TextureId.PLAY_BACKGROUND);

        createUI();
    }

    private void createUI() {

        leaveButton = new TextButton("Leave", skin);
        leaveButton.getLabel().setFontScale(screenHeight*0.0015f);
        leaveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onLeaveClicked();
            }
        });

        startButton = new TextButton("Start", skin);
        startButton.getLabel().setFontScale(screenHeight*0.0015f);
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
        root.defaults().pad(screenHeight*0.015f);

        Label playerLabel = new Label("Players", skin);
        playerLabel.setFontScale(screenHeight*0.002f);

        root.add(playerLabel).row();
        root.add(playersTable).row();

        if(isHost) {
            root.add(startButton)
                .width(screenWidth*0.2f)
                .height(screenHeight*0.12f)
                .padTop(screenHeight*0.05f)
                .row();
        }

        root.add(leaveButton)
            .width(screenWidth*0.2f)
            .height(screenHeight*0.12f);

        stage.addActor(root);

        LobbyCodeLabel = new Label("CODE : ----", skin);

        LobbyCodeLabel.setFontScale(screenHeight*0.0025f);
        LobbyCodeLabel.setPosition(
            Gdx.graphics.getWidth()*0.72f,
            Gdx.graphics.getHeight()*0.5f
        );

        LobbyCodeLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String code = lobbyModel.getLobbyCode();

                if (code != null) {
                    Gdx.app.getClipboard().setContents(code);
                    Gdx.input.vibrate(200);
                    LobbyCodeLabel.setText("COPIED !");
                    copyTimer = 1.5f;
                }
            }
        });

        stage.addActor(LobbyCodeLabel);
    }

    // FOR NOW WITHOUT CONTROLLER
    private void onStartClicked() {
        viewActions.onStartClicked();
        Gdx.input.vibrate(200);
        SoundsManager.playButton(1f);
    }

    private void onLeaveClicked() {
        viewActions.onLeaveClicked();
        Gdx.input.vibrate(200);
        SoundsManager.playButton(1f);
    }

    @Override
    public void update(float dt){
        if (lobbyModel.getPlayerNames() != null) {
            updatePlayers(lobbyModel.getPlayerNames());
            LobbyCodeLabel.setText("CODE : " + lobbyModel.getLobbyCode());
        }

        if (copyTimer > 0) {
            copyTimer -= dt;
            if (copyTimer <= 0) {
                LobbyCodeLabel.setText("CODE : " + lobbyModel.getLobbyCode());
            }
        }

        stage.act(dt); // update UI
    }

    @Override
    public void render(SpriteBatch sb) {

        sb.begin();
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        float size = Math.min(screenWidth, screenHeight) * 0.75f;

        float x = (screenWidth - size) / 2f;
        float y = (screenHeight - size) / 2f;

        sb.draw(playBackground, 0, 0, screenWidth, screenHeight);
        sb.draw(menuBackground, x, y, size, size);
        sb.end();

        stage.draw();  // draw UI
    }


    private void updatePlayers(Array<String> currentNames) {
        if (!currentNames.equals(lastNames)) {
            rebuildPlayers(currentNames);
            lastNames = new Array<>(currentNames);
        }

        if (isHost) {
            startButton.setDisabled(currentNames.size < 1);
        }
    }

    private void rebuildPlayers(Array<String> names) {
        playersTable.clear();

        for (String name : names) {
            Label playerListLabel = new Label(name, skin);
            playerListLabel.setFontScale(1.5f);

            playersTable.add(playerListLabel).row();
        }
    }

    @Override
    public void showError(String message) {

    }
}
