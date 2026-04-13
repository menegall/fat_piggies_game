package com.fatpiggies.game.model;

import com.badlogic.gdx.utils.Array;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.view.PlayerColor;

import java.util.LinkedHashMap;
import java.util.Map;

public interface IReadOnlyLobbyModel {
    // --- Lobby ---
    String getLobbyId();
    String getLobbyCode();
    Map<String, PlayerSetup> getPlayerSetups();
    LinkedHashMap<String, PlayerSetup> getFinalRanking();
}
