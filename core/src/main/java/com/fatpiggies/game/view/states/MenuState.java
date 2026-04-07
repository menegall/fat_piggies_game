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
import com.fatpiggies.game.model.Snapshot;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

public class MenuState extends State {
    private TextField nameField;
    private TextField lobbyField;

    private TextButton joinButton;
    private TextButton hostButton;

    private Texture logo;
    private final Texture menuBackground;
    private final Texture playBackground;

    // Manage errors
    private Label errorLabel;

    public MenuState() {
        menuBackground = TextureManager.getTexture(TextureId.MENU_BACKGROUND);
        playBackground = TextureManager.getTexture(TextureId.PLAY_BACKGROUND);

        createUI();
    }

    private void createUI() {

        nameField = new TextField("", skin);
        nameField.setMessageText("Name");

        lobbyField = new TextField("", skin);
        lobbyField.setMessageText("Lobby ID");

        // Join button
        joinButton = new TextButton("Join", skin);
        joinButton.setDisabled(true);
        nameField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateJoinButton();
                updateHostButton();
            }
        });

        // Host button
        hostButton = new TextButton("Host", skin);
        hostButton.setDisabled(true);
        lobbyField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateJoinButton();
            }
        });

        joinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onJoinClicked();
            }
        });

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
            screenWidth*0.4f,
            -screenHeight
        );
        errorLabel.setSize(screenWidth*0.2f, screenHeight*0.05f);

        stage.addActor(errorLabel);

        Table table = new Table();
        table.setFillParent(true);

        table.defaults().pad(screenHeight*0.00f);

        table.add(new Label("Name", skin)).left();
        table.add(nameField)
            .width(screenWidth*0.18f)
            .height(screenHeight*0.18f)
            .row();

        table.add(new Label("Lobby ID", skin)).left();
        table.add(lobbyField)
            .width(screenWidth*0.18f)
            .height(screenHeight*0.18f)
            .row();

        table.add(joinButton)
            .width(screenWidth*0.2f)
            .height(screenHeight*0.12f)
            .colspan(2)
            .padTop(screenHeight*0.02f)
            .row();

        table.add(hostButton)
            .width(screenWidth*0.2f)
            .height(screenHeight*0.12f)
            .colspan(2)
            .padTop(screenHeight*0.02f);

        stage.addActor(table);
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


    // FOR NOW WITHOUT CONTROLLER
    private void onJoinClicked() {
        String name = nameField.getText();
        String lobbyId = lobbyField.getText();

        System.out.println("Join lobby " + lobbyId + " as " + name);
    }

    private void onHostClicked() {
        String name = nameField.getText();

        System.out.println("Host lobby as " + name);
    }

    @Override
    public void showError(String message) {
        errorLabel.clearActions();
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        float targetY = screenHeight * 0.06f;

        errorLabel.addAction(Actions.sequence(
            Actions.moveTo(screenWidth*0.4f, targetY, 1f, Interpolation.swingIn),
            Actions.delay(2.5f),
            Actions.moveTo(screenWidth*0.4f, -screenHeight, 0.1f, Interpolation.swingOut),
            Actions.run(() -> errorLabel.setVisible(false))
        ));
    }

    @Override
    public void update(Snapshot snapshot, float dt){
        stage.act(dt);   // update UI
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
}
