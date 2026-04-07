package com.fatpiggies.game.view.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.fatpiggies.game.model.Snapshot;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

import java.util.ArrayList;
import java.util.List;

public class LobbyState extends State {
    private final boolean isHost;

    private Table playersTable;
    private TextButton startButton;
    private TextButton leaveButton;

    private final Texture menuBackground;
    private final Texture playBackground;

    private List<String> lastNames = new ArrayList<>();
    private Label lobbyIdLabel;

    public LobbyState(boolean isHost) {
        this.isHost = isHost;
        menuBackground = TextureManager.getTexture(TextureId.MENU_BACKGROUND);
        playBackground = TextureManager.getTexture(TextureId.PLAY_BACKGROUND);

        createUI();
    }

    private void createUI() {

        leaveButton = new TextButton("Leave", skin);
        leaveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onLeaveClicked();
            }
        });

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
        root.defaults().pad(screenHeight*0.015f);

        Label playerLabel = new Label("Players", skin);
        playerLabel.setFontScale(2f);

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

        lobbyIdLabel = new Label("ID : ----", skin);
        lobbyIdLabel.setFontScale(3f);
        lobbyIdLabel.setPosition(
            Gdx.graphics.getWidth()*2.85f/4f,
            Gdx.graphics.getHeight()/2f
        );
        stage.addActor(lobbyIdLabel);
    }

    // FOR NOW WITHOUT CONTROLLER
    private void onStartClicked() {

        System.out.println("Start");
    }

    private void onLeaveClicked() {

        System.out.println("Leave");
    }

    @Override
    public void update(Snapshot snapshot, float dt){
        if (snapshot != null) {
            updatePlayers(snapshot);
            lobbyIdLabel.setText("ID : " + snapshot.getId());
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

    private void updatePlayers(Snapshot snapshot) {
        if (!lastNames.equals(snapshot.getNames())){
            rebuildPlayers(snapshot);
            lastNames = new ArrayList<>(snapshot.getNames());
        }

        if (isHost) {
            startButton.setDisabled(snapshot.getNames().size() < 2);
        }
    }

    private void rebuildPlayers(Snapshot snapshot) {
        playersTable.clear();
        Label playerListLabel;

        for (String name : snapshot.getNames()) {
            playerListLabel = new Label(name, skin);
            playerListLabel.setFontScale(1.5f);

            playersTable.add(playerListLabel).row();
        }
    }

    @Override
    public void showError(String message) {

    }
}
