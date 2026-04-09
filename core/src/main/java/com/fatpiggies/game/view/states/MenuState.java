package com.fatpiggies.game.view.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.fatpiggies.game.audio.SoundsManager;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.assets.TextureId;
import com.fatpiggies.game.assets.TextureManager;

public class MenuState extends State {
    private TextField nameField;
    private TextField lobbyField;

    private TextButton joinButton;
    private TextButton hostButton;
    private final Texture menuBackground;
    private final Texture playBackground;
    private String lastNameField;

    // Manage errors
    private Label errorLabel;

    public MenuState(IViewActions viewActions) {
        super(viewActions);
        menuBackground = TextureManager.getTexture(TextureId.MENU_BACKGROUND);
        playBackground = TextureManager.getTexture(TextureId.PLAY_BACKGROUND);

        createUI("");
    }

    private void createUI(String lastNameField) {
        nameField = new TextField(lastNameField, skin);
        nameField.setMessageText("Name");
        nameField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateJoinButton();
                updateHostButton();
            }
        });

        lobbyField = new TextField("", skin);
        lobbyField.setMessageText("Code");
        lobbyField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateJoinButton();
            }
        });

        // Join button
        joinButton = new TextButton("Join", skin);
        joinButton.getLabel().setFontScale(screenHeight * 0.0015f);
        joinButton.setDisabled(true);
        joinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onJoinClicked();
            }
        });

        // Host button
        hostButton = new TextButton("Host", skin);
        hostButton.getLabel().setFontScale(screenHeight * 0.0015f);
        hostButton.setDisabled(true);
        hostButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onHostClicked();
            }
        });

        // Errors
        errorLabel = new Label("", skin);
        errorLabel.setColor(Color.RED);
        errorLabel.setAlignment(Align.center);
        errorLabel.setVisible(false);

        // Initial position out of window
        errorLabel.setPosition(
            screenWidth * 0.4f,
            -screenHeight
        );
        errorLabel.setSize(screenWidth * 0.2f, screenHeight * 0.05f);

        stage.addActor(errorLabel);

        Table table = new Table();
        table.setFillParent(true);

        table.defaults().pad(screenHeight * 0.00f);

        Label nameLabel = new Label("Name", skin);
        nameLabel.setFontScale(screenHeight * 0.0015f);
        table.add(nameLabel).left();
        table.add(nameField)
            .width(screenWidth * 0.18f)
            .height(screenHeight * 0.18f)
            .padTop(screenHeight * 0.015f)
            .row();

        Label lobbyCodeLabel = new Label("Lobby", skin);
        lobbyCodeLabel.setFontScale(screenHeight * 0.0015f);
        table.add(lobbyCodeLabel).left();
        table.add(lobbyField)
            .width(screenWidth * 0.18f)
            .height(screenHeight * 0.18f)
            .padTop(screenHeight * 0.015f)
            .row();

        table.add(joinButton)
            .width(screenWidth * 0.2f)
            .height(screenHeight * 0.12f)
            .colspan(2)
            .padTop(screenHeight * 0.02f)
            .row();

        table.add(hostButton)
            .width(screenWidth * 0.2f)
            .height(screenHeight * 0.12f)
            .colspan(2)
            .padTop(screenHeight * 0.02f);

        stage.addActor(table);

        updateJoinButton();
        updateHostButton();
    }

    private void updateJoinButton() {
        boolean nameEmpty = nameField.getText().trim().isEmpty();
        boolean lobbyEmpty = lobbyField.getText().trim().isEmpty();

        joinButton.setDisabled(nameEmpty || lobbyEmpty);
    }

    private void updateHostButton() {
        boolean nameEmpty = nameField.getText().trim().isEmpty();

        hostButton.setDisabled(nameEmpty);
    }

    private void onJoinClicked() {
        lastNameField = nameField.getText();
        String lobbyId = lobbyField.getText();

        viewActions.onJoinLobbyClicked(lastNameField, lobbyId);
        Gdx.input.vibrate(200);
        SoundsManager.playButton(1f);
    }

    private void onHostClicked() {
        lastNameField = nameField.getText();

        viewActions.onHostLobbyClicked(lastNameField);
        Gdx.input.vibrate(200);
        SoundsManager.playButton(1f);
    }

    @Override
    public void update(float dt) {
        stage.act(dt);   // update UI
    }

    @Override
    public void render(SpriteBatch sb) {

        sb.begin();
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        float size = Math.min(screenWidth, screenHeight) * 0.79f;

        float x = (screenWidth - size) * 0.5f;
        float y = (screenHeight - size) * 0.5f;

        sb.draw(playBackground, 0, 0, screenWidth, screenHeight);
        sb.draw(menuBackground, x, y, size, size);
        sb.end();

        stage.draw();  // draw UI
    }

    @Override
    public void showError(String message) {
        SoundsManager.playError(3f);
        errorLabel.clearActions();
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setFontScale(screenHeight * 0.002f);

        float targetY = screenHeight * 0.93f;

        errorLabel.addAction(Actions.sequence(
            Actions.moveTo(screenWidth * 0.4f, targetY, 0.3f, Interpolation.swingIn),
            Actions.delay(2f),
            Actions.moveTo(screenWidth * 0.4f, -screenHeight, 0.1f, Interpolation.swingOut),
            Actions.run(() -> errorLabel.setVisible(false))
        ));
    }

    @Override
    public void show(){
        super.show();
        stage.clear();
        createUI(lastNameField);
    }
}
