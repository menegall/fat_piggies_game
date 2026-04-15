package com.fatpiggies.game.network.dto;


public class PlayerSetup {
    public String name;
    public String color;

    public PlayerSetup() {}

    public PlayerSetup(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
