package com.fatpiggies.game.network.dto;

public class PlayerSetup {
    public int texture_id;
    public String name;

    public PlayerSetup() {}

    public PlayerSetup(int texture_id, String name) {
        this.texture_id = texture_id;
        this.name = name;
    }
}
