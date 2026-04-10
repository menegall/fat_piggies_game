package com.fatpiggies.game.network.dto;

import com.fatpiggies.game.view.PlayerColor;

public class PlayerSetup {
    public String name;
    public PlayerColor color;

    public PlayerSetup() {}

    public PlayerSetup(String name, PlayerColor color) {
        this.name = name;
        this.color = color;
    }
}
