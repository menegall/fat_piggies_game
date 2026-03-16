package com.fatpiggies.game.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class SkinManager {

    private SkinManager() {}
    private static Skin skin;

    public static void load() {
        if (skin != null) return;

        skin = new Skin();

        BitmapFont font = new BitmapFont(Gdx.files.internal("uiAssets/font.fnt"));

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

        textFieldStyle.background.setLeftWidth(60);
        textFieldStyle.background.setRightWidth(40);
        textFieldStyle.background.setTopHeight(20);
        textFieldStyle.background.setBottomHeight(20);
    }

    public static Skin getSkin() {
        return skin;
    }

    public static void dispose() {
        if (skin != null) {
            skin.dispose();
        }
    }
}
