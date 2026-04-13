package com.fatpiggies.game.model;

import com.badlogic.gdx.utils.Array;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.view.PlayerColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LobbyModel implements IReadOnlyLobbyModel {

    private boolean isHost;
    private String lobbyId;
    private String lobbyCode;
    private String playerId;
    private Map<String, PlayerSetup> playerSetups;
    private List<String> finalRanking;

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
        if(playerSetups != null) {
            playerSetups.forEach((id, setup) -> {
                    playerNames.add(setup.name);
                }
            );
        }
        return playerNames;
    }

    @Override
    public Array<PlayerColor> getPlayerColors() {
        Array<PlayerColor> playerColors = new Array<>();

        if (playerSetups != null) {
            playerSetups.forEach((id, setup) -> {
                playerColors.add(setup.color);
            });
        }

        return playerColors;
    }

    public Map<String, PlayerSetup> getPlayerSetups() {
        return playerSetups;
    }

    @Override
    public Map<String, PlayerSetup> getFinalRanking() {
        Map<String, PlayerSetup> finalRanking = new HashMap<>();
        for (String playerId : this.finalRanking) {
            PlayerSetup playerSetup = playerSetups.get(playerId);
            if (playerSetup != null) {
                finalRanking.put(playerId, playerSetup);
            }
        }
        return finalRanking;
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
        this.playerSetups = playersSetup;
    }
}
