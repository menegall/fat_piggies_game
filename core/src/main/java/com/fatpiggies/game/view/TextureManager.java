package com.fatpiggies.game.view;

import com.badlogic.gdx.graphics.Texture;
import com.fatpiggies.game.view.TextureId;

import java.util.HashMap;
import java.util.Map;

public class TextureManager {

    private TextureManager() {}
    private static final Map<TextureId, Texture> textures = new HashMap<>();

    public static void loadAll() {

        if (!textures.isEmpty()) return;

        textures.put(TextureId.MAIN_BACKGROUND, new Texture("mainBackground.png"));

        /*
        textures.put(TextureId.PLAY_BACKGROUND, new Texture("playBackground.png"));

        textures.put(TextureId.BLACK_PIG, new Texture("blackPig.png"));
        textures.put(TextureId.RED_PIG, new Texture("redPig.png"));
        textures.put(TextureId.ORANGE_PIG, new Texture("orangePig.png"));
        textures.put(TextureId.BLUE_PIG, new Texture("bluePig.png"));
        textures.put(TextureId.GREEN_PIG, new Texture("greenPig.png"));

        textures.put(TextureId.DONUT, new Texture("donut.png"));
        textures.put(TextureId.BEER, new Texture("beer.png"));
        textures.put(TextureId.EXTRA_LIFE, new Texture("extralife.png"));
        textures.put(TextureId.APPLE, new Texture("apple.png"));

        textures.put(TextureId.BUMP_EFFECT, new Texture("bumpEffect.png"));
         */
    }

    public static Texture getTexture(TextureId id) {
        Texture texture = textures.get(id);

        if (texture == null) {
            throw new RuntimeException("Texture not loaded: " + id);
        }

        return texture;
    }

    public static void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
    }
}
