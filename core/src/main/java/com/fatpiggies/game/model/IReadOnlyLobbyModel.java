package com.fatpiggies.game.model;

import com.badlogic.gdx.utils.Array;
import com.fatpiggies.game.network.dto.PlayerSetup;

import java.util.Map;

public interface IReadOnlyLobbyModel {
    // --- Lobby ---
    String getLobbyId();
    String getLobbyCode();
    Array<String> getPlayerNames();
}
