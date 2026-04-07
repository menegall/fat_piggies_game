package com.fatpiggies.game.network.dto;

import java.util.HashMap;
import java.util.Map;

public class GameState {
    public long ts; // Timestamp in milliseconds
    public Map<String, PlayerData> players;
    public Map<String, PowerupData> powerups;

    public GameState() {
        this.players = new HashMap<>();
        this.powerups = new HashMap<>();
    }
}
