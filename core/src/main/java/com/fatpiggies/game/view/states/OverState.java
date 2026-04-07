package com.fatpiggies.game.view.states;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.fatpiggies.game.controller.IViewActions;
import com.fatpiggies.game.model.Snapshot;
import com.fatpiggies.game.view.Animation;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

import java.util.ArrayList;
import java.util.List;

public class OverState extends State {
    private final boolean isHost;

    private TextButton lobbyButton;
    private TextButton leaveButton;

    private Table scoreTable;

    private final Texture menuBackground;
    private final Texture playBackground;
    private final Texture overBackground;
    private final Texture podium;

    private final Animation bluePig;
    private final Animation greenPig;
    private final Animation redPig;
    private final Animation yellowPig;

    private final Animation crown;

    private List<String> lastNames = new ArrayList<>();


    public OverState(IViewActions viewActions, boolean isHost) {
        super(viewActions);
        this.isHost = isHost;

        menuBackground = TextureManager.getTexture(TextureId.MENU_BACKGROUND);
        playBackground = TextureManager.getTexture(TextureId.PLAY_BACKGROUND);
        overBackground = TextureManager.getTexture(TextureId.OVER_BACKGROUND);
        podium = TextureManager.getTexture(TextureId.PODIUM);

        bluePig = new Animation(TextureManager.getTexture(TextureId.OVER_BLUE_PIG), 2, 2, 4, 2f);
        greenPig = new Animation(TextureManager.getTexture(TextureId.OVER_GREEN_PIG), 2, 2, 4, 2f);
        redPig = new Animation(TextureManager.getTexture(TextureId.OVER_RED_PIG), 2, 2, 4, 2f);
        yellowPig = new Animation(TextureManager.getTexture(TextureId.OVER_YELLOW_PIG), 2, 2, 4, 2f);
        crown = new Animation(TextureManager.getTexture(TextureId.CROWN), 2, 2, 4, 2f);


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

        lobbyButton = new TextButton("Lobby", skin);

        lobbyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onLobbyClicked();
            }
        });

        Table menuTable = new Table();
        menuTable.setFillParent(true);
        menuTable.defaults().pad(screenHeight*0.02f);
        menuTable.padTop(screenHeight*0.5f);
        menuTable.padLeft(screenWidth*0.8f);

        if(isHost) {
            menuTable.add(lobbyButton)
                .width(screenWidth*0.11f)
                .height(screenHeight*0.08f)
                .row();
        }

        menuTable.add(leaveButton)
            .width(screenWidth*0.11f)
            .height(screenHeight*0.08f);

        stage.addActor(menuTable);

        scoreTable = new Table();
        scoreTable.setFillParent(true);
        scoreTable.top().left();

        scoreTable.padTop(screenHeight*0.08f);
        scoreTable.padLeft(screenWidth*0.03f);
        stage.addActor(scoreTable);
    }

    // FOR NOW WITHOUT CONTROLLER
    private void onLobbyClicked() {
        // TODO: add this feature
    }

    private void onLeaveClicked() {
        viewActions.onLeaveClicked();
    }

    private void updateScoreBoard(List<String> players) {
        scoreTable.clear(); // reset

        for (int i = 0; i < players.size(); i++) {
            String rank = (i + 1) + "th.";

            if (i == 0) rank = "1st.";
            else if (i == 1) rank = "2nd.";
            else if (i == 2) rank = "3rd.";

            Label rankLabel = new Label(rank, skin);
            Label nameLabel = new Label(players.get(i), skin);

            scoreTable.add(rankLabel).left().padRight(10);
            scoreTable.add(nameLabel).left().row();
        }
    }


    @Override
    public void update(Snapshot snapshot, float dt) {
        bluePig.update(dt);
        crown.update(dt);

        if (!snapshot.getNames().equals(lastNames)) {
            lastNames = new ArrayList<>(snapshot.getNames());
            updateScoreBoard(lastNames);
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        float anchorX;
        float anchorY;

        sb.begin();

        // Background
        sb.draw(playBackground, 0, 0, screenWidth, screenHeight);

        float baseSize = Math.min(screenWidth, screenHeight);

        // Podium
        float podiumWidth = baseSize * 1.2f;
        float podiumHeight = baseSize * 0.7f;
        anchorX = screenWidth * 0.5f;
        anchorY = screenHeight * 0.15f;
        float podiumX = anchorX - podiumWidth / 2f;
        float podiumY = anchorY - podiumHeight / 4f;
        sb.draw(podium, podiumX, podiumY, podiumWidth, podiumHeight);

        // Pigs
        float pigSize = podiumWidth * 0.25f;

        // 1st
        float pig1X = podiumX + podiumWidth * 0.5f - pigSize / 2f;
        float pig1Y = podiumY + podiumHeight * 0.655f;
        sb.draw(bluePig.getFrame(), pig1X, pig1Y, pigSize, pigSize);

        // 2nd
        float pig2X = podiumX + podiumWidth * 0.25f - pigSize / 2f;
        float pig2Y = podiumY + podiumHeight * 0.52f;
        sb.draw(greenPig.getFrame(), pig2X, pig2Y, pigSize, pigSize);

        // 3rd
        float pig3X = podiumX + podiumWidth * 0.75f - pigSize / 2f;
        float pig3Y = podiumY + podiumHeight * 0.46f;
        sb.draw(redPig.getFrame(), pig3X, pig3Y, pigSize, pigSize);

        // Crown
        float crownX = podiumX + podiumWidth * 0.495f - pigSize / 2f;
        float crownY = podiumY + podiumHeight * 0.98f;
        sb.draw(crown.getFrame(), crownX, crownY, pigSize, pigSize*0.6f);

        // Score
        float scoreSize = baseSize * 0.6f;
        anchorX = screenWidth * 0.15f;
        anchorY = screenHeight * 0.65f;
        float overX = anchorX - scoreSize / 2f;
        float overY = anchorY - scoreSize / 2f;
        sb.draw(overBackground, overX, overY, scoreSize, scoreSize);

        // Menu
        float menuSize = baseSize * 0.3f;
        anchorX = screenWidth * 0.9f;
        anchorY = screenHeight * 0.25f;
        float menuX = anchorX - menuSize / 2f;
        float menuY = anchorY - menuSize / 2f;
        sb.draw(menuBackground, menuX, menuY, menuSize, menuSize);

        sb.end();

        stage.draw();
    }


    @Override
    public void showError(String message) {

    }


}
