package com.fatpiggies.game.model;

import com.fatpiggies.game.network.dto.PlayerSetup;

import java.util.LinkedHashMap;
import java.util.Map;

public interface IReadOnlyLobbyModel {
    // --- Lobby ---
    String getLobbyId();
    String getLobbyCode();
    Map<String, PlayerSetup> getPlayerSetups();
    LinkedHashMap<String, PlayerSetup> getFinalRanking();
}
