package com.fatpiggies.game.utils;

public final class GameConstants {
    // --- ARENA BOUNDS ---
    public static final float LEFT_BOUND = 50f;
    public static final float RIGHT_BOUND = 1000f;
    public static final float TOP_BOUND = 1000f;
    public static final float BOTTOM_BOUND = 50f;
    // --- RESPAWN & SPAWN SETTINGS ---
    public static final float SAFE_SPAWN_RADIUS = 60f;
    public static final int MAX_SPAWN_ATTEMPTS = 10;
    // --- PLAYER SETTINGS ---
    public static final float PLAYER_BASE_SPEED = 150f;
    public static final float PLAYER_BASE_ACCELERATION = 200f;
    public static final float PLAYER_BASE_MASS = 10f;
    public static final int MAX_PLAYERS = 4;

    // --- POWERUP SETTINGS ---
    public static final float POWERUP_SPAWN_INTERVAL = 7.0f; // Spawns a new power-up every 7 seconds
    public static final float POWERUP_MIN_LIFETIME = 10.0f;  // Minimum seconds the power-up live
    public static final float POWERUP_MAX_LIFETIME = 40.0f;  // Maximum seconds the power-up live
    public static final int POWER_VELOCITY_MODIFIER = 20;
    public static final int POWER_ACCELERATION_MODIFIER = 20;
    public static final int POWER_MASS_MODIFIER = 20;

    private GameConstants() {
    }
}
