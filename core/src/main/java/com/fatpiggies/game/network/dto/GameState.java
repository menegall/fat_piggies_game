package com.fatpiggies.game.network.dto;

import java.util.HashMap;
import java.util.Map;

public class GameState {
    public float ts = 0; // Increasing timestamp in milliseconds
    public Map<String, PlayerData> players;
    public Map<String, PowerupData> powerups;

    public GameState() {
        this.players = new HashMap<>();
        this.powerups = new HashMap<>();
    }
}
