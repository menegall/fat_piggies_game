package com.fatpiggies.game.view.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.fatpiggies.game.model.Snapshot;
import com.fatpiggies.game.view.SkinManager;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

public class MenuState extends State {

    private Stage stage;
    private Skin skin;

    private TextField nameField;
    private TextField lobbyField;

    private TextButton joinButton;
    private TextButton hostButton;

    private Texture background;

    public MenuState() {

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = SkinManager.getSkin();
        background = TextureManager.getTexture(TextureId.MAIN_BACKGROUND);

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
        nameField.setTextFieldListener((textField, c) -> updateJoinButton());
        lobbyField.setTextFieldListener((textField, c) -> updateJoinButton());

        // Host button
        hostButton = new TextButton("Host", skin);
        hostButton.setDisabled(true);
        nameField.setTextFieldListener((textField, c) -> updateHostButton());

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

        Table table = new Table();
        table.setFillParent(true);

        table.defaults().pad(20);

        table.add(new Label("Name", skin)).left();
        table.add(nameField)
            .width(400)
            .height(70)
            .row();

        table.add(new Label("Lobby ID", skin)).left();
        table.add(lobbyField)
            .width(400)
            .height(70)
            .row();

        table.add(joinButton)
            .width(500)
            .height(120)
            .colspan(2)
            .padTop(40)
            .row();

        table.add(hostButton)
            .width(500)
            .height(120)
            .colspan(2)
            .padTop(20);

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
    public void render(SpriteBatch sb, Snapshot snapshot) {

        sb.begin();
        sb.draw(background, 0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());
        sb.end();

        stage.act();   // update UI
        stage.draw();  // draw UI
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
