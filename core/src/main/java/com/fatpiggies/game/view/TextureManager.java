package com.fatpiggies.game.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

import java.util.HashMap;
import java.util.Map;

public class TextureManager {

    private TextureManager() {}

    private static final Map<TextureId, Texture> textures = new HashMap<>();
    private static final Map<TextureId, Animation> animations = new HashMap<>();
    private static final Map<TextureId, AnimationConfig> configs = new HashMap<>();

    // ========================
    // CONFIG CLASS
    // ========================

    private static class AnimationConfig {
        String path;
        int rows, cols;
        int animatedFrames;
        int forcedFrame; // -1 = normal animation
        float animationTime;

        AnimationConfig(String path, int rows, int cols, int animatedFrames, int forcedFrame, float animationTime) {
            this.path = path;
            this.rows = rows;
            this.cols = cols;
            this.animatedFrames = animatedFrames;
            this.forcedFrame = forcedFrame;
            this.animationTime =animationTime;
        }
    }

    // ========================
    // LOAD
    // ========================

    public static void loadTextures() {

        if (!textures.isEmpty()) return;

        // --- STATIC ---
        textures.put(TextureId.LOGO, new Texture("logo.png"));
        textures.put(TextureId.MENU_BACKGROUND, new Texture("backgrounds/menuBackground.png"));
        textures.put(TextureId.PLAY_BACKGROUND, new Texture("backgrounds/arena.png"));
        textures.put(TextureId.OVER_BACKGROUND, new Texture("backgrounds/overBackground.png"));
        textures.put(TextureId.PODIUM, new Texture("backgrounds/podium.png"));

        textures.put(TextureId.BONUS, new Texture("events/items.png"));

        // ========================
        // CONFIG
        // ========================

        // pigs (top view)
        configs.put(TextureId.BLUE_PIG, new AnimationConfig("pig/top_pig_blue.png", 2, 1, 2, -1, 1f));
        configs.put(TextureId.GREEN_PIG, new AnimationConfig("pig/top_pig_green.png", 2, 1, 2, -1, 1f));
        configs.put(TextureId.RED_PIG, new AnimationConfig("pig/top_pig_red.png", 2, 1, 2, -1, 1f));
        configs.put(TextureId.YELLOW_PIG, new AnimationConfig("pig/top_pig_yellow.png", 2, 1, 2, -1, 1f));

        // life (3 animated + 4th accessible)
        configs.put(TextureId.LIFE_BLUE_PIG, new AnimationConfig("pig/life_pig_blue.png", 2, 2, 3, -1, 2f));
        configs.put(TextureId.LIFE_GREEN_PIG, new AnimationConfig("pig/life_pig_green.png", 2, 2, 3, -1, 2f));
        configs.put(TextureId.LIFE_RED_PIG, new AnimationConfig("pig/life_pig_red.png", 2, 2, 3, -1, 2f));
        configs.put(TextureId.LIFE_YELLOW_PIG, new AnimationConfig("pig/life_pig_yellow.png", 2, 2, 3, -1, 2f));

        // over
        configs.put(TextureId.OVER_BLUE_PIG, new AnimationConfig("pig/over_pig_blue.png", 2, 2, 4, -1, 2f));
        configs.put(TextureId.OVER_GREEN_PIG, new AnimationConfig("pig/over_pig_green.png", 2, 2, 4, -1, 2f));
        configs.put(TextureId.OVER_RED_PIG, new AnimationConfig("pig/over_pig_red.png", 2, 2, 4, -1, 2f));
        configs.put(TextureId.OVER_YELLOW_PIG, new AnimationConfig("pig/over_pig_yellow.png", 2, 2, 4, -1, 2f));

        configs.put(TextureId.CROWN, new AnimationConfig("events/crown.png", 2, 2, 4, -1, 2f));

        // items (same sheet, different frame)
        configs.put(TextureId.APPLE, new AnimationConfig("events/items.png", 2, 2, 4, 0, 2f));
        configs.put(TextureId.DONUT, new AnimationConfig("events/items.png", 2, 2, 4, 1, 2f));
        configs.put(TextureId.BEER, new AnimationConfig("events/items.png", 2, 2, 4, 2, 2f));
        configs.put(TextureId.LIFE, new AnimationConfig("events/items.png", 2, 2, 4, 3, 2f));

        // ========================
        // LOAD ANIMATIONS
        // ========================

        for (Map.Entry<TextureId, AnimationConfig> entry : configs.entrySet()) {

            AnimationConfig cfg = entry.getValue();

            Texture texture = new Texture(cfg.path);

            Animation anim = new Animation(
                texture,
                cfg.rows,
                cfg.cols,
                cfg.animatedFrames,
                cfg.animationTime
            );

            animations.put(entry.getKey(), anim);
            textures.put(entry.getKey(), texture);
        }
    }

    // ========================
    // UPDATE
    // ========================

    public static void update(float dt) {
        for (Animation anim : animations.values()) {
            anim.update(dt);
        }
    }

    // ========================
    // GETTERS
    // ========================

    public static TextureRegion getFrame(TextureId id) {

        if (animations.containsKey(id)) {
            AnimationConfig cfg = configs.get(id);

            if (cfg.forcedFrame != -1) {
                return animations.get(id).getFrame(cfg.forcedFrame);
            }

            return animations.get(id).getFrame();
        }

        Texture texture = textures.get(id);
        if (texture == null) {
            throw new RuntimeException("Texture not loaded: " + id);
        }

        return new TextureRegion(texture);
    }

    public static TextureRegion getFrame(TextureId id, int i) {
        if (animations.containsKey(id)) {
            return animations.get(id).getFrame(i);
        }
        return getFrame(id);
    }

//    public static Texture getTexture(TextureId id) {
//        Texture texture = textures.get(id);
//
//        if (texture == null) {
//            throw new RuntimeException("Texture not loaded: " + id);
//        }
//
//        return texture;
//    }

    // ========================
    // SKIN
    // ========================

    private static Skin skin;

    private static float getScreenWidth() { return Gdx.graphics.getWidth(); }
    private static float getScreenHeight() { return Gdx.graphics.getHeight(); }

    public static void loadSkin() {
        if (skin != null) {
            skin.dispose();
        }

        skin = new Skin();

        BitmapFont font = new BitmapFont(Gdx.files.internal("uiAssets/font_35.fnt"));
        font.getData().setScale(getScreenWidth()*0.0006f);

        skin.add("default-font", font);
        skin.add("white", Color.WHITE);


        // --- LABEL---
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        // --- TEXT BUTTON ---
        Texture buttonUpTexture = new Texture(Gdx.files.internal("uiAssets/buttonUp.png"));
        Texture buttonDownTexture = new Texture(Gdx.files.internal("uiAssets/buttonDown.png"));
        TextureRegionDrawable buttonUpDrawable = new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        TextureRegionDrawable buttonDownDrawable = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = buttonUpDrawable;
        buttonStyle.down = buttonDownDrawable;
        buttonStyle.disabled = buttonDownDrawable;
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        skin.add("default", buttonStyle);

        // --- TEXT FIELD ---
        Texture textfieldTexture = new Texture(Gdx.files.internal("uiAssets/textfield.png"));
        Texture cursorTexture = new Texture(Gdx.files.internal("uiAssets/cursor.png"));
        TextureRegionDrawable textfieldDrawable = new TextureRegionDrawable(new TextureRegion(textfieldTexture));
        TextureRegionDrawable cursorDrawable = new TextureRegionDrawable(new TextureRegion(cursorTexture));

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = font;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.background = textfieldDrawable;
        textFieldStyle.cursor = cursorDrawable;

        skin.add("default", textFieldStyle);

        textFieldStyle.background.setLeftWidth(getScreenWidth()*0.04f);
        textFieldStyle.background.setRightWidth(getScreenWidth()*0.04f);
        textFieldStyle.background.setTopHeight(getScreenHeight()*0.02f);
        textFieldStyle.background.setBottomHeight(getScreenHeight()*0.03f);

        textFieldStyle.cursor.setMinSize(getScreenWidth()*0.04f, getScreenHeight()*0.02f);

        // --- JOYSTICK ---
        Texture joystickBg = new Texture(Gdx.files.internal("uiAssets/joystick_bg.png"));
        Texture joystickKnob = new Texture(Gdx.files.internal("uiAssets/joystick_knob.png"));
        TextureRegionDrawable joystickBgDrawable = new TextureRegionDrawable(new TextureRegion(joystickBg));
        TextureRegionDrawable joystickKnobDrawable = new TextureRegionDrawable(new TextureRegion(joystickKnob));

        joystickBgDrawable.setMinWidth(getScreenWidth()*0.25f);
        joystickBgDrawable.setMinHeight(getScreenWidth()*0.25f);
        Drawable joystickBgDrawableTransparent = joystickBgDrawable.tint(new Color(1f, 1f, 1f, 0.4f));
        joystickKnobDrawable.setMinWidth(getScreenWidth()*0.08f);
        joystickKnobDrawable.setMinHeight(getScreenWidth()*0.08f);

        Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
        touchpadStyle.background = joystickBgDrawableTransparent;
        touchpadStyle.knob = joystickKnobDrawable;

        skin.add("touchpad", touchpadStyle);

        // --- MUSIC BUTTON ---
        Texture musicOn = new Texture(Gdx.files.internal("uiAssets/music_on.png"));
        Texture musicOff = new Texture(Gdx.files.internal("uiAssets/music_off.png"));

        TextureRegionDrawable musicOnDrawable = new TextureRegionDrawable(new TextureRegion(musicOn));
        TextureRegionDrawable musicOffDrawable = new TextureRegionDrawable(new TextureRegion(musicOff));

        CheckBox.CheckBoxStyle musicStyle = new CheckBox.CheckBoxStyle();
        musicStyle.checkboxOff = musicOnDrawable;   // ON
        musicStyle.checkboxOn = musicOffDrawable;  // OFF
        musicStyle.font = skin.getFont("default-font");

        skin.add("musicButton", musicStyle);


        // --- SOUND BUTTON ---
        Texture soundOn = new Texture(Gdx.files.internal("uiAssets/sound_on.png"));
        Texture soundOff = new Texture(Gdx.files.internal("uiAssets/sound_off.png"));

        TextureRegionDrawable soundOnDrawable = new TextureRegionDrawable(new TextureRegion(soundOn));
        TextureRegionDrawable soundOffDrawable = new TextureRegionDrawable(new TextureRegion(soundOff));

        CheckBox.CheckBoxStyle soundStyle = new CheckBox.CheckBoxStyle();
        soundStyle.checkboxOff = soundOnDrawable;
        soundStyle.checkboxOn = soundOffDrawable;
        soundStyle.font = skin.getFont("default-font");

        skin.add("soundButton", soundStyle);


        // --- VIBRATION BUTTON ---
        Texture vibrationOn = new Texture(Gdx.files.internal("uiAssets/vibration_on.png"));
        Texture vibrationOff = new Texture(Gdx.files.internal("uiAssets/vibration_off.png"));

        TextureRegionDrawable vibrationOnDrawable = new TextureRegionDrawable(new TextureRegion(vibrationOn));
        TextureRegionDrawable vibrationOffDrawable = new TextureRegionDrawable(new TextureRegion(vibrationOff));

        CheckBox.CheckBoxStyle vibrationStyle = new CheckBox.CheckBoxStyle();
        vibrationStyle.checkboxOff = vibrationOnDrawable;
        vibrationStyle.checkboxOn = vibrationOffDrawable;
        vibrationStyle.font = skin.getFont("default-font");

        skin.add("vibrationButton", vibrationStyle);

        // --- BACK BUTTON ---
        Texture backTexture = new Texture(Gdx.files.internal("uiAssets/back.png"));
        TextureRegionDrawable backDrawable = new TextureRegionDrawable(new TextureRegion(backTexture));

        backDrawable.setMinWidth(getScreenWidth() * 0.08f);
        backDrawable.setMinHeight(getScreenWidth() * 0.08f);

        Button.ButtonStyle backStyle = new Button.ButtonStyle();
        backStyle.up = backDrawable;
        backStyle.down = backDrawable.tint(new Color(0.8f, 0.8f, 0.8f, 1f)); // effet click

        skin.add("backButton", backStyle);

    }

    public static Skin getSkin() {
        return skin;
    }

    // ========================
    // DISPOSE
    // ========================

    public static void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
        animations.clear();
        configs.clear();

        if (skin != null) {
            skin.dispose();
        }
    }
}
