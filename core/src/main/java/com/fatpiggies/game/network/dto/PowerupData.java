package com.fatpiggies.game.network.dto;

public class PowerupData {
    public String type; // Ex: "apple"
    public float x;
    public float y;

    public PowerupData() {}

    public PowerupData(String type, float x, float y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }
}
