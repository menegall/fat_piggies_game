package com.fatpiggies.game.network.dto;

import com.fatpiggies.game.assets.TextureId;

public class PowerupData {
    public TextureId textureId; // Ex: "apple"
    public float x;
    public float y;

    public PowerupData() {}

    public PowerupData(TextureId textureId, float x, float y) {
        this.textureId = textureId;
        this.x = x;
        this.y = y;
    }
}
