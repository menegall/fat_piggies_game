package com.fatpiggies.game.network.dto;

public class PowerupData {
    public String textureId; // Ex: "apple"
    public float x;
    public float y;

    public PowerupData() {}

    public PowerupData(String textureId, float x, float y) {
        this.textureId = textureId;
        this.x = x;
        this.y = y;
    }
}
