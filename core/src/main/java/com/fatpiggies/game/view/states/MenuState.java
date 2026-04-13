package com.fatpiggies.game.view.states;

import static com.fatpiggies.game.network.NetworkError.COLOR_ALREADY_TAKEN;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.setting.MusicManager;
import com.fatpiggies.game.setting.SoundsManager;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.setting.VibrationManager;
import com.fatpiggies.game.view.PlayerColor;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

public class MenuState extends State {

    // ================= UI CONSTANTS =================
    private static final float BTN_SIZE_X_RATIO = 0.09f;
    private static final float BTN_SIZE_Y_RATIO = 0.21f;

    private static final float BTN_X_RATIO = 0.22f;
    private static final float COLOR_Y_RATIO = 0.53f;
    private static final float MUSIC_Y_RATIO = 0.37f;
    private static final float SOUND_Y_RATIO = 0.25f;
    private static final float VIBRATE_Y_RATIO = 0.11f;

    private static final float PANEL_WIDTH_RATIO = 0.1f;
    private static final float PANEL_HEIGHT_RATIO = 0.65f;

    private static final float ERROR_Y_RATIO = 0.81f;
    private static final float CROSS_SIZE_X_RATIO = 0.04f;
    private static final float CROSS_SIZE_Y_RATIO = 0.1f;
    private static final float CROSS_X_RATIO = 0.26f;
    private static final float CROSS_Y_RATIO = 0.55f;
    private static final float BUBBLE_SIZE_X_RATIO = 0.19f;
    private static final float BUBBLE_SIZE_Y_RATIO = 0.35f;
    private static final float BUBBLE_X_RATIO = 0.05f;
    private static final float BUBBLE_Y_RATIO = 0.52f;

    private static final float MENU_SIZE_X_RATIO = 0.35f;
    private static final float MENU_SIZE_Y_RATIO = 0.79f;

    private static final float FIELD_WIDTH_RATIO = 0.18f;
    private static final float FIELD_HEIGHT_RATIO = 0.18f;

    private static final float BUTTON_WIDTH_RATIO = 0.2f;
    private static final float BUTTON_HEIGHT_RATIO = 0.12f;
    private static final float BUTTON_HEIGHT_PAD = 0.03f;

    private static final float ERROR_X_RATIO = 0.4f;
    private static final float ERROR_WIDTH_RATIO = 0.2f;
    private static final float ERROR_HEIGHT_RATIO = 0.05f;

    // ================= UI =================
    private TextField nameField;
    private TextField lobbyField;

    private TextButton joinButton;
    private TextButton hostButton;
    private ImageButton colorButton;
    private TextureId currentPig = TextureId.OVER_BLUE_PIG;
    private boolean showPigSelectionInfo = false;
    private CheckBox musicButton;
    private CheckBox soundButton;
    private CheckBox vibrationButton;

    private final TextureRegion menuBackground;
    private final TextureRegion playBackground;
    private final TextureRegion cross;
    private final TextureRegion bubble;

    private Label errorLabel;

    private String lastNameField;
    private float btnSizeX;

    public MenuState(IViewActions viewActions) {
        super(viewActions);
        menuBackground = TextureManager.getFrame(TextureId.MENU_BACKGROUND);
        playBackground = TextureManager.getFrame(TextureId.PLAY_BACKGROUND);
        cross = TextureManager.getFrame(TextureId.CROSS);
        bubble = TextureManager.getFrame(TextureId.BUBBLE);

        createUI("");
    }

    private void createUI(String lastNameField) {

        // ================= INPUTS =================
        nameField = new TextField(lastNameField, skin);
        nameField.setMessageText("Name");
        nameField.setMaxLength(20);
        nameField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateJoinButton();
                updateHostButton();
            }
        });

        nameField.setTextFieldListener((textField, c) -> {
            String text = textField.getText();
            if (!text.isEmpty()) {
                text = text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
                textField.setText(text);
                textField.setCursorPosition(text.length());
            }
        });

        lobbyField = new TextField("", skin);
        lobbyField.setMessageText("Code");
        lobbyField.setTextFieldFilter((textField, c) -> Character.isDigit(c));
        lobbyField.setMaxLength(4);
        lobbyField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateJoinButton();
            }
        });

        // ================= BUTTONS =================
        joinButton = new TextButton("Join", skin);
        joinButton.getLabel().setFontScale(screenHeight * 0.0015f);
        joinButton.setDisabled(true);
        joinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onJoinClicked();
            }
        });

        hostButton = new TextButton("Host", skin);
        hostButton.getLabel().setFontScale(screenHeight * 0.0015f);
        hostButton.setDisabled(true);
        hostButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onHostClicked();
            }
        });

        // ================= ERROR =================
        errorLabel = new Label("", skin);
        errorLabel.setColor(Color.RED);
        errorLabel.setAlignment(Align.center);
        errorLabel.setVisible(false);
        errorLabel.setPosition(screenWidth * ERROR_X_RATIO, -screenHeight);
        errorLabel.setSize(
            screenWidth * ERROR_WIDTH_RATIO,
            screenHeight * ERROR_HEIGHT_RATIO
        );
        stage.addActor(errorLabel);

        // ================= TABLE =================
        Table table = new Table();
        table.setFillParent(true);

        Label nameLabel = new Label("Click Me!", skin);
        nameLabel.setFontScale(screenHeight * 0.0015f);

        table.add(nameLabel).left();
        table.add(nameField)
            .width(screenWidth * FIELD_WIDTH_RATIO)
            .height(screenHeight * FIELD_HEIGHT_RATIO)
            .row();

        Label lobbyLabel = new Label("Lobby", skin);
        lobbyLabel.setFontScale(screenHeight * 0.0015f);

        table.add(lobbyLabel).left();
        table.add(lobbyField)
            .width(screenWidth * FIELD_WIDTH_RATIO)
            .height(screenHeight * FIELD_HEIGHT_RATIO)
            .row();

        table.add(joinButton)
            .width(screenWidth * BUTTON_WIDTH_RATIO)
            .height(screenHeight * BUTTON_HEIGHT_RATIO)
            .colspan(2)
            .row();

        table.add(hostButton)
            .width(screenWidth * BUTTON_WIDTH_RATIO)
            .height(screenHeight * BUTTON_HEIGHT_RATIO)
            .colspan(2)
            .padTop(screenHeight * BUTTON_HEIGHT_PAD);

        stage.addActor(table);

        updateJoinButton();
        updateHostButton();

        // ================= SETTINGS BUTTONS =================
        btnSizeX = screenWidth * BTN_SIZE_X_RATIO;
        float btnSizeY = screenHeight * BTN_SIZE_Y_RATIO;

        TextureRegionDrawable drawable = new TextureRegionDrawable(TextureManager.getFrame(currentPig));
        colorButton = new ImageButton(drawable);

        colorButton.setSize(btnSizeX, btnSizeY);
        colorButton.setPosition(
            screenWidth * BTN_X_RATIO,
            screenHeight * COLOR_Y_RATIO
        );
        colorButton.getImageCell().expand().fill();

        colorButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentPig = TextureManager.nextPig(currentPig);
                showPigSelectionInfo = false;
                updateColorButton();
            }
        });
        stage.addActor(colorButton);
        updateColorButton();

        musicButton = new CheckBox("", skin, "musicButton");
        musicButton.setSize(btnSizeX, btnSizeY);
        musicButton.setPosition(
            screenWidth * BTN_X_RATIO,
            screenHeight * MUSIC_Y_RATIO
        );
        musicButton.getImageCell().expand().fill();
        musicButton.setChecked(!MusicManager.isEnabled());

        musicButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean muted = musicButton.isChecked();
                MusicManager.setEnabled(!muted);
                SoundsManager.playButton(1f);
                VibrationManager.vibrate(200);
            }
        });

        stage.addActor(musicButton);

        soundButton = new CheckBox("", skin, "soundButton");
        soundButton.setSize(btnSizeX, btnSizeY);
        soundButton.setPosition(
            screenWidth * BTN_X_RATIO,
            screenHeight * SOUND_Y_RATIO
        );
        soundButton.getImageCell().expand().fill();
        soundButton.setChecked(!SoundsManager.isEnabled());

        soundButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean muted = soundButton.isChecked();
                SoundsManager.setEnabled(!muted);
                SoundsManager.playButton(1f);
                VibrationManager.vibrate(200);
            }
        });
        stage.addActor(soundButton);

        vibrationButton = new CheckBox("", skin, "vibrationButton");
        vibrationButton.setSize(btnSizeX, btnSizeY);
        vibrationButton.setPosition(
            screenWidth * BTN_X_RATIO,
            screenHeight * VIBRATE_Y_RATIO
        );
        vibrationButton.getImageCell().expand().fill();
        vibrationButton.setChecked(!SoundsManager.isEnabled());

        vibrationButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean muted = vibrationButton.isChecked();
                VibrationManager.setEnabled(!muted);
                SoundsManager.playButton(1f);
                VibrationManager.vibrate(200);
            }
        });
        stage.addActor(vibrationButton);
    }

    private void updateJoinButton() {
        joinButton.setDisabled(
            nameField.getText().trim().isEmpty()
                || lobbyField.getText().trim().isEmpty()
        );
    }

    private void updateHostButton() {
        hostButton.setDisabled(
            nameField.getText().trim().isEmpty()
        );
    }


    private void updateColorButton() {
        TextureRegion region = TextureManager.getFrame(currentPig);
        colorButton.getStyle().imageUp = new TextureRegionDrawable(region);
    }

    private void onJoinClicked() {
        lastNameField = nameField.getText();

        PlayerColor color = TextureManager.getColorFromTexture(currentPig);

        viewActions.onJoinLobbyClicked(
            lastNameField,
            lobbyField.getText(),
            color
        );

        VibrationManager.vibrate(200);
        SoundsManager.playButton(1f);
    }

    private void onHostClicked() {
        lastNameField = nameField.getText();

        PlayerColor color = TextureManager.getColorFromTexture(currentPig);

        viewActions.onHostLobbyClicked(
            lastNameField,
            color
        );

        VibrationManager.vibrate(200);
        SoundsManager.playButton(1f);
    }

    @Override
    public void update(float dt) {
        updateColorButton();
        stage.act(dt);
    }

    @Override
    public void render(SpriteBatch sb) {

        sb.begin();

        sb.draw(playBackground, 0, 0, screenWidth, screenHeight);

        float sizeX = screenWidth * MENU_SIZE_X_RATIO;
        float sizeY = screenHeight * MENU_SIZE_Y_RATIO;
        float x = (screenWidth - sizeX) * 0.5f;
        float y = (screenHeight - sizeY) * 0.5f;

        sb.draw(menuBackground, x, y, sizeX, sizeY);

        float panelWidth = screenWidth * PANEL_WIDTH_RATIO;
        float panelHeight = screenHeight * PANEL_HEIGHT_RATIO;

        x = screenWidth * BTN_X_RATIO - (panelWidth - btnSizeX) * 0.5f;
        y = (screenHeight - sizeY) * 0.5f;

        sb.draw(menuBackground, x, y, panelWidth, panelHeight);

        sb.end();

        stage.draw();

        sb.begin();
        if (showPigSelectionInfo){
            sizeX = screenWidth * CROSS_SIZE_X_RATIO;
            sizeY = screenHeight * CROSS_SIZE_Y_RATIO;
            sb.draw(cross, screenWidth * CROSS_X_RATIO, screenHeight * CROSS_Y_RATIO, sizeX, sizeY);
            sizeX = screenWidth * BUBBLE_SIZE_X_RATIO;
            sizeY = screenHeight * BUBBLE_SIZE_Y_RATIO;
            sb.draw(bubble, screenWidth * BUBBLE_X_RATIO, screenHeight * BUBBLE_Y_RATIO, sizeX, sizeY);

        }

        sb.end();
    }

    @Override
    public void showError(NetworkError error) {
        SoundsManager.playError(3f);

        errorLabel.clearActions();
        errorLabel.setText(getErrorMessage(error));
        errorLabel.setVisible(true);

        float targetY = screenHeight * ERROR_Y_RATIO;

        errorLabel.addAction(Actions.sequence(
            Actions.moveTo(screenWidth * ERROR_X_RATIO, targetY, 0.3f, Interpolation.swingIn),
            Actions.delay(2f),
            Actions.moveTo(screenWidth * ERROR_X_RATIO, -screenHeight, 0.1f),
            Actions.run(() -> errorLabel.setVisible(false))
        ));

        if(error == COLOR_ALREADY_TAKEN) {
            showPigSelectionInfo = true;
        }
    }

    private String getErrorMessage(NetworkError error) {
        switch (error) {

            case NAME_ALREADY_EXIST:
                return "That name's taken";

            case COLOR_ALREADY_TAKEN:
                return "That pig's taken";

            case LOBBY_NOT_FOUND:
                return "Can't find that lobby";

            case LOBBY_FULL:
                return "That lobby is full";

            case LOBBY_ALREADY_STARTED:
                return "The game already started";

            case DATABASE_ERROR:
                return "Connection error - try again";

            default:
                return "Something went wrong";
        }
    }

    @Override
    public void show() {
        super.show();
        stage.clear();
        showPigSelectionInfo = false;
        createUI(lastNameField);
    }
}
