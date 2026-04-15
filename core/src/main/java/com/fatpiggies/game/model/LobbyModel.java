package com.fatpiggies.game.model;

import com.fatpiggies.game.network.dto.PlayerSetup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LobbyModel implements IReadOnlyLobbyModel {

    private boolean isHost;
    private String lobbyId;
    private String lobbyCode;
    private String playerId;
    private Map<String, PlayerSetup> playerSetups = new LinkedHashMap<>();
    private List<String> finalRanking;

    @Override
    public String getLobbyId() {
        return lobbyId;
    }

    @Override
    public String getLobbyCode() {
        return lobbyCode;
    }

    public Map<String, PlayerSetup> getPlayerSetups() {
        return playerSetups;
    }

    @Override
    public LinkedHashMap<String, PlayerSetup> getFinalRanking() {
        LinkedHashMap<String, PlayerSetup> finalRankingMap = new LinkedHashMap<>();

        if (this.finalRanking == null) {
            return finalRankingMap;
        }

        for (String playerId : this.finalRanking) {
            PlayerSetup playerSetup = playerSetups.get(playerId);
            if (playerSetup != null) {
                finalRankingMap.put(playerId, playerSetup);
            }
        }

        return finalRankingMap;
    }

    public void setFinalRanking(List<String> finalRanking) {
        this.finalRanking = finalRanking;
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
        this.playerSetups = new LinkedHashMap<>(playersSetup);
    }
}
