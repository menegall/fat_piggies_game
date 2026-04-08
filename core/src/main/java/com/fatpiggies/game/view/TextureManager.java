package com.fatpiggies.game.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.HashMap;
import java.util.Map;

public class TextureManager {

    private TextureManager() {}
    private static final Map<TextureId, Texture> textures = new HashMap<>();

    // Load textures
    public static void loadTextures() {

        if (!textures.isEmpty()) return;

        textures.put(TextureId.LOGO, new Texture("logo.png"));
        textures.put(TextureId.MENU_BACKGROUND, new Texture("backgrounds/menuBackground.png"));
        textures.put(TextureId.PLAY_BACKGROUND, new Texture("backgrounds/arena.png"));
        textures.put(TextureId.OVER_BACKGROUND, new Texture("backgrounds/overBackground.png"));
        textures.put(TextureId.PODIUM, new Texture("backgrounds/podium.png"));


        textures.put(TextureId.LIFE_BLUE_PIG, new Texture("pig/life_pig_blue.png"));
        textures.put(TextureId.LIFE_GREEN_PIG, new Texture("pig/life_pig_green.png"));
        textures.put(TextureId.LIFE_RED_PIG, new Texture("pig/life_pig_red.png"));
        textures.put(TextureId.LIFE_YELLOW_PIG, new Texture("pig/life_pig_yellow.png"));

        textures.put(TextureId.OVER_BLUE_PIG, new Texture("pig/over_pig_blue.png"));
        textures.put(TextureId.OVER_GREEN_PIG, new Texture("pig/over_pig_green.png"));
        textures.put(TextureId.OVER_RED_PIG, new Texture("pig/over_pig_red.png"));
        textures.put(TextureId.OVER_YELLOW_PIG, new Texture("pig/over_pig_yellow.png"));

        textures.put(TextureId.CROWN, new Texture("events/crown.png"));

        /*
        textures.put(TextureId.PLAY_BACKGROUND, new Texture("playBackground.png"));

        textures.put(TextureId.BLUE_PIG, new Texture("bluePig.png"));
        textures.put(TextureId.GREEN_PIG, new Texture("greenPig.png"));
        textures.put(TextureId.RED_PIG, new Texture("redPig.png"));
        textures.put(TextureId.YELLOW_PIG, new Texture("yellowPig.png"));


        textures.put(TextureId.DONUT, new Texture("donut.png"));
        textures.put(TextureId.BEER, new Texture("beer.png"));
        textures.put(TextureId.EXTRA_LIFE, new Texture("extralife.png"));
        textures.put(TextureId.APPLE, new Texture("apple.png"));

        textures.put(TextureId.BUMP_EFFECT, new Texture("bumpEffect.png"));
         */
    }

    private static Skin skin;
    private static float getScreenWidth() { return Gdx.graphics.getWidth(); }
    private static float getScreenHeight() { return Gdx.graphics.getHeight(); }

    // Load skin and buttons
    public static void loadSkin() {
        if (skin != null) {
            skin.dispose();
        }

        skin = new Skin();

        /*
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("uiAssets/font.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = (int)(getScreenHeight() * 0.04f); // taille dynamique

        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();*/

        BitmapFont font = new BitmapFont(Gdx.files.internal("uiAssets/font_25.fnt"));

        Texture buttonUpTexture = new Texture(Gdx.files.internal("uiAssets/buttonUp.png"));
        Texture buttonDownTexture = new Texture(Gdx.files.internal("uiAssets/buttonDown.png"));
        Texture textfieldTexture = new Texture(Gdx.files.internal("uiAssets/textfield.png"));

        TextureRegionDrawable buttonUpDrawable =
            new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        TextureRegionDrawable buttonDownDrawable =
            new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        TextureRegionDrawable textfieldDrawable =
            new TextureRegionDrawable(new TextureRegion(textfieldTexture));

        skin.add("default-font", font);
        skin.add("white", Color.WHITE);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = buttonUpDrawable;
        buttonStyle.down = buttonDownDrawable;
        buttonStyle.disabled = buttonDownDrawable;
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        skin.add("default", buttonStyle);

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = font;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.background = textfieldDrawable;
        textFieldStyle.cursor = textfieldDrawable;
        skin.add("default", textFieldStyle);

        textFieldStyle.background.setLeftWidth(getScreenWidth()*0.04f);
        textFieldStyle.background.setRightWidth(getScreenWidth()*0.04f);
        textFieldStyle.background.setTopHeight(getScreenHeight()*0.00f);
        textFieldStyle.background.setBottomHeight(getScreenHeight()*0.01f);


        // Joystick
        Texture joystickBg = new Texture(Gdx.files.internal("uiAssets/joystick_bg.png"));
        Texture joystickKnob = new Texture(Gdx.files.internal("uiAssets/joystick_knob.png"));

        TextureRegionDrawable joystickBgDrawable =
            new TextureRegionDrawable(new TextureRegion(joystickBg));

        TextureRegionDrawable joystickKnobDrawable =
            new TextureRegionDrawable(new TextureRegion(joystickKnob));

        joystickBgDrawable.setMinWidth(getScreenWidth()*0.25f);
        joystickBgDrawable.setMinHeight(getScreenWidth()*0.25f);
        Drawable joystickBgDrawableTransparent = joystickBgDrawable.tint(new Color(1f, 1f, 1f, 0.4f));
        joystickKnobDrawable.setMinWidth(getScreenWidth()*0.08f);
        joystickKnobDrawable.setMinHeight(getScreenWidth()*0.08f);

        Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
        touchpadStyle.background = joystickBgDrawableTransparent;
        touchpadStyle.knob = joystickKnobDrawable;

        skin.add("touchpad", touchpadStyle);
    }

    // Getters
    public static Texture getTexture(TextureId id) {
        Texture texture = textures.get(id);

        if (texture == null) {
            throw new RuntimeException("Texture not loaded: " + id);
        }

        return texture;
    }

    public static Skin getSkin() {
        return skin;
    }

    // Dispose
    public static void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();

        if (skin != null) {
            skin.dispose();
        }
    }
}
