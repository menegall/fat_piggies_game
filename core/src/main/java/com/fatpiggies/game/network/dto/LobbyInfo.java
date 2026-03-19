package com.fatpiggies.game.network.dto;

import java.util.HashMap;
import java.util.Map;

public class LobbyInfo {
    public String status;
    public String code;
    public String hostId;
    public Map<String, PlayerSetup> playersSetup;

    public LobbyInfo() {
        this.playersSetup = new HashMap<>();
    }
    public LobbyInfo(String status, String code, String hostId) {
        this.status = status;
        this.code = code;
        this.hostId = hostId;
        this.playersSetup = new HashMap<>();
    }

}
