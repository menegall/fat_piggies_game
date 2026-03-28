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
import com.fatpiggies.game.controller.MainController;
import com.fatpiggies.game.model.Snapshot;
import com.fatpiggies.game.view.SkinManager;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

import java.util.ArrayList;
import java.util.List;

public class LobbyState extends State {
    private boolean isHost;

    private Stage stage;
    private Skin skin;

    private Table playersTable;
    private TextButton startButton;
    private TextButton leaveButton;

    private Texture background;
    private List<String> lastNames = new ArrayList<>();
    private Label lobbyIdLabel;

    private MainController mc;

    public LobbyState(boolean isHost) {
        this.isHost = isHost;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = SkinManager.getSkin();
        background = TextureManager.getTexture(TextureId.MAIN_BACKGROUND);

        createUI();
    }

    private void createUI() {

        leaveButton = new TextButton("Leave", skin);

        startButton = new TextButton("Start", skin);
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
        root.defaults().pad(20);

        root.add(new Label("Players", skin)).row();
        root.add(playersTable).row();

        if(isHost) {
            root.add(startButton)
                .width(500)
                .height(120)
                .padTop(40)
                .row();
        }

        root.add(leaveButton)
            .width(500)
            .height(120);

        stage.addActor(root);

        lobbyIdLabel = new Label("ID : ----", skin);
        lobbyIdLabel.setFontScale(2f);
        lobbyIdLabel.setPosition(
            Gdx.graphics.getWidth()*3/4f,
            Gdx.graphics.getHeight()/2f
        );
        stage.addActor(lobbyIdLabel);
    }

    // FOR NOW WITHOUT CONTROLLER
    private void onStartClicked() {

        System.out.println("Start");
    }

    @Override
    public void render(SpriteBatch sb, Snapshot snapshot) {

        sb.begin();
        sb.draw(background, 0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());
        sb.end();

        if (snapshot != null) {
            updatePlayers(snapshot);
            lobbyIdLabel.setText("ID : " + snapshot.getId());
        }

        stage.act();
        stage.draw();
    }

    private void updatePlayers(Snapshot snapshot) {
        if(!snapshot.getNames().equals(lastNames)){
            rebuildPlayers(snapshot);
            lastNames = new ArrayList<>(snapshot.getNames());
        }

        if (isHost) {
            startButton.setDisabled(snapshot.getNames().size() < 2);
        }
    }

    private void rebuildPlayers(Snapshot snapshot) {
        playersTable.clear();

        for (String name : snapshot.getNames()) {
            playersTable.add(new Label(name, skin)).row();
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
