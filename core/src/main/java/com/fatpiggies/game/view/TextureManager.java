package com.fatpiggies.game.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.*;

public class TextureManager {

    private TextureManager() {}

    private static final Map<String, Texture> textures = new HashMap<>();
    private static final Map<String, Animation> animations = new HashMap<>();
    private static final Map<String, AnimationConfig> configs = new HashMap<>();

    private static Theme currentTheme = Theme.FARM;
    private static Theme previewTheme = null;

    // ========================
    // THEME
    // ========================

    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }

    public static void setPreviewTheme(Theme theme) {
        previewTheme = theme;
    }

    public static void clearPreviewTheme() {
        previewTheme = null;
    }

    private static Theme getActiveTheme() {
        return (previewTheme != null) ? previewTheme : currentTheme;
    }

    // ========================
    // CONFIG
    // ========================

    private static class AnimationConfig {
        String path;
        int rows, cols;
        int animatedFrames;
        int forcedFrame;
        float animationTime;

        AnimationConfig(String path, int rows, int cols, int animatedFrames, int forcedFrame, float animationTime) {
            this.path = path;
            this.rows = rows;
            this.cols = cols;
            this.animatedFrames = animatedFrames;
            this.forcedFrame = forcedFrame;
            this.animationTime = animationTime;
        }
    }

    // ========================
    // LOAD
    // ========================

    public static void loadTextures() {

        if (!textures.isEmpty()) return;

        textures.put("LOGO", new Texture("logo.png"));
        textures.put("MENU_BACKGROUND", new Texture("backgrounds/menuBackground.png"));
        textures.put("OVER_BACKGROUND", new Texture("backgrounds/overBackground.png"));
        textures.put("PODIUM", new Texture("backgrounds/podium.png"));

        textures.put("NEXT", new Texture("uiAssets/next.png"));
        textures.put("PREVIOUS", new Texture("uiAssets/previous.png"));
        textures.put("COIN", new Texture("uiAssets/coin.png"));

        textures.put("CROSS", new Texture("uiAssets/cross.png"));
        textures.put("BUBBLE", new Texture("uiAssets/bubble.png"));

        // ===== COLORS =====
        loadColors();

        // ===== THEMES =====
        loadThemes();

        configs.put("CROWN", new AnimationConfig("events/crown.png", 2, 2, 4, -1, 2f));

        // ===== EVENTS =====
        configs.put("APPLE", new AnimationConfig("events/items.png", 2, 2, 4, 0, 2f));
        configs.put("DONUT", new AnimationConfig("events/items.png", 2, 2, 4, 1, 2f));
        configs.put("BEER", new AnimationConfig("events/items.png", 2, 2, 4, 2, 2f));
        configs.put("LIFE", new AnimationConfig("events/items.png", 2, 2, 4, 3, 2f));

        // build animations
        for (Map.Entry<String, AnimationConfig> entry : configs.entrySet()) {
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

    private static void loadColors() {

        for (PlayerColor color : PlayerColor.values()) {

            String colorName = color.name().toLowerCase();

            // ===== LIFE =====
            configs.put(
                "LIFE_" + color.name() + "_PIG",
                new AnimationConfig(
                    "pig/life/" + colorName + ".png",
                    2, 2, 3, -1, 2f
                )
            );

            // ===== OVER =====
            configs.put(
                "OVER_" + color.name() + "_PIG",
                new AnimationConfig(
                    "pig/over/" + colorName + ".png",
                    2, 2, 4, -1, 2f
                )
            );
        }
    }

    private static void loadThemes() {

        for (Theme theme : Theme.values()) {

            String themeName = theme.name().toLowerCase();

            // ===== BACKGROUND =====
            textures.put(
                "PLAY_BACKGROUND_" + theme.name(),
                new Texture("backgrounds/arena_" + themeName + ".png")
            );

            // ===== PIGS =====
            for (PlayerColor color : PlayerColor.values()) {

                String colorName = color.name().toLowerCase();

                String id = color.name() + "_PIG_" + theme.name();

                configs.put(
                    id,
                    new AnimationConfig(
                        "pig/" + themeName + "/" + colorName + ".png",
                        2, 1, 2, -1, 1f
                    )
                );
            }
        }
    }

    private static void loadPigsForTheme(Theme theme) {
        String themeName = theme.name().toLowerCase();

        for (PlayerColor color : PlayerColor.values()) {
            String colorName = color.name().toLowerCase();

            String id = color.name() + "_PIG_" + theme.name();

            configs.put(id,
                new AnimationConfig(
                    "pig/" + themeName + "/" + colorName + ".png",
                    2, 1, 2, -1, 1f
                )
            );
        }
    }

    // ========================
    // RESOLVE
    // ========================

    private static String resolve(TextureId id) {

        Theme theme = getActiveTheme();

        // BACKGROUND
        if (id == TextureId.PLAY_BACKGROUND) {
            return "PLAY_BACKGROUND_" + theme.name();
        }

        // PIG
        if (isPig(id)) {
            return id.name() + "_" + theme.name();
        }

        return id.name();
    }

    private static boolean isPig(TextureId id) {
        return id.name().endsWith("_PIG")
            && !id.name().startsWith("LIFE_")
            && !id.name().startsWith("OVER_");
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
    // GET FRAME
    // ========================

    public static TextureRegion getFrame(TextureId id) {

        String key = resolve(id);

        if (animations.containsKey(key)) {
            AnimationConfig cfg = configs.get(key);

            if (cfg.forcedFrame != -1) {
                return animations.get(key).getFrame(cfg.forcedFrame);
            }

            return animations.get(key).getFrame();
        }

        Texture texture = textures.get(key);

        if (texture == null) {
            throw new RuntimeException("Missing texture: " + key);
        }

        return new TextureRegion(texture);
    }

    // ========================
    // GETTERS
    // ========================


    public static TextureRegion getFrame(TextureId id, int i) {
        String key = resolve(id);

        if (animations.containsKey(key)) {
            return animations.get(key).getFrame(i);
        }

        return getFrame(id);
    }

    public static TextureId getPigTextureId(PlayerColor color) {
        if (color == null) return TextureId.BLUE_PIG;

        return TextureId.valueOf(color.name() + "_PIG");
    }

    public static TextureRegion getLifeFrame(PlayerColor color) {
        if (color == null) color = PlayerColor.BLUE;

        String key = "LIFE_" + color.name() + "_PIG";
        return animations.get(key).getFrame();
    }

    public static TextureRegion getLifeFrame(PlayerColor color, int frameIndex) {
        if (color == null) color = PlayerColor.BLUE;

        String key = "LIFE_" + color.name() + "_PIG";
        return animations.get(key).getFrame(frameIndex);
    }

    public static TextureRegion getOverFrame(PlayerColor color) {
        if (color == null) color = PlayerColor.BLUE;

        String key = "OVER_" + color.name() + "_PIG";
        return animations.get(key).getFrame();
    }

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
        font.getData().setScale(getScreenHeight()*0.0018f);

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
        NinePatchDrawable cursorDrawable = new NinePatchDrawable(new NinePatch(cursorTexture, 2, 2, 2, 2));

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

        musicOnDrawable.setMinWidth(getScreenWidth() * 0.06f);
        musicOnDrawable.setMinHeight(getScreenWidth() * 0.06f);
        musicOffDrawable.setMinWidth(getScreenWidth() * 0.06f);
        musicOffDrawable.setMinHeight(getScreenWidth() * 0.06f);


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

        soundOnDrawable.setMinWidth(getScreenWidth() * 0.06f);
        soundOnDrawable.setMinHeight(getScreenWidth() * 0.06f);
        soundOffDrawable.setMinWidth(getScreenWidth() * 0.06f);
        soundOffDrawable.setMinHeight(getScreenWidth() * 0.06f);

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

        vibrationOnDrawable.setMinWidth(getScreenWidth() * 0.06f);
        vibrationOnDrawable.setMinHeight(getScreenWidth() * 0.06f);
        vibrationOffDrawable.setMinWidth(getScreenWidth() * 0.06f);
        vibrationOffDrawable.setMinHeight(getScreenWidth() * 0.06f);

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
        backStyle.down = backDrawable.tint(new Color(0.8f, 0.8f, 0.8f, 1f));

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
