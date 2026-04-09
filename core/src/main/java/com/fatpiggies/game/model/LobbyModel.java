package com.fatpiggies.game.model;

import com.badlogic.gdx.utils.Array;
import com.fatpiggies.game.network.dto.PlayerSetup;

import java.util.Map;

public class LobbyModel implements IReadOnlyLobbyModel {

    private boolean isHost;
    private String lobbyId;
    private String lobbyCode;
    private String playerId;
    private Map<String, PlayerSetup> playersSetup;

    @Override
    public String getLobbyId() {
        return lobbyId;
    }

    @Override
    public String getLobbyCode() {
        return lobbyCode;
    }

    @Override
    public Array<String> getPlayerNames() {
        Array<String> playerNames = new Array<>();
        if(playersSetup != null) {
            playersSetup.forEach((id, setup) -> {
                    playerNames.add(setup.name);
                }
            );
        }
        return playerNames;
    }

    public Map<String, PlayerSetup> getPlayersSetup() {
        return playersSetup;
    }


    public String getPlayerId() {
        return playerId;
    }

    public boolean getIsHost() {return isHost;}

    // Setters
    public void setIsHost(boolean isHost){
        this.isHost = isHost;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setLobbyCode(String lobbyCode) {
        this.lobbyCode = lobbyCode;
    }

    public void setPlayersSetup(Map<String, PlayerSetup> playersSetup) {
        this.playersSetup = playersSetup;
    }
}
