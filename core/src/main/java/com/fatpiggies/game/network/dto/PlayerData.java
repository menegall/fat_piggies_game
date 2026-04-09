package com.fatpiggies.game.network.dto;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {
    public float x;
    public float y;
    public int hp;
    public Map<String, Boolean> fx; // Power-up

    public PlayerData() {
        this.fx = new HashMap<>();
    }

    public PlayerData(float x, float y, int hp, Map<String, Boolean> fx) {
        this.x = x;
        this.y = y;
        this.hp = hp;
    }
}
