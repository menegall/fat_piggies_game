package com.fatpiggies.game.network.dto;

public class PlayerInput {
    public float jx;
    public float jy;
    public float ts; // Timestamp

    public PlayerInput() {}

    public PlayerInput(float jx, float jy, long ts) {
        this.jx = jx;
        this.jy = jy;
        this.ts = ts;
    }
}
