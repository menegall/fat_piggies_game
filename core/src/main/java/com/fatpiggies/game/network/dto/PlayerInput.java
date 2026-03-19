package com.fatpiggies.game.network.dto;

public class PlayerInput {
    public float vx;
    public float vy;
    public boolean atk;
    public long ts; // Timestamp

    public PlayerInput() {}

    public PlayerInput(float vx, float vy, boolean atk, long ts) {
        this.vx = vx;
        this.vy = vy;
        this.atk = atk;
        this.ts = ts;
    }
}
