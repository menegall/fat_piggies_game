package com.fatpiggies.game.model.utils;

public final class GameConstants {
    // --- ARENA SETTINGS ---
    public static final float WORLD_WIDTH = 1000f;
    public static final float WORLD_HEIGHT = 1000f;

    public static final float LEFT_BOUND = 50f;
    public static final float RIGHT_BOUND = WORLD_WIDTH - 50f;
    public static final float BOTTOM_BOUND = 50f;
    public static final float TOP_BOUND = WORLD_HEIGHT - 50f;
    public static final float GROUND_FRICTION = 50f;
    public static final float FORCE_FACTOR = 10f;
    public static final float STOP_THRESHOLD = 0.0001f;
    public static final float INPUT_DEADZONE = 0.1f;

    // --- RESPAWN & SPAWN SETTINGS ---
    public static final float SAFE_SPAWN_RADIUS = 60f;
    public static final int MAX_SPAWN_ATTEMPTS = 10;

    // --- PLAYER SETTINGS ---
    public static final float PLAYER_BASE_VELOCITY = 4000;
    public static final float PLAYER_BASE_ACCELERATION = 200f;
    public static final float PLAYER_BASE_MASS = 10f;
    public static final int MAX_PLAYERS = 4;
    public static final int BASE_LIFE = 4;
    public static final float LERP_FACTOR = 20f;
    public static final int PLAYER_COLLISION_RADIUS = 95;
    public static final float PLAYER_BOUNCINESS = 0.8f;


    // --- POWERUP SETTINGS ---
    public static final float POWERUP_SPAWN_INTERVAL = 7.0f; // Spawns a new power-up every 7 seconds
    public static final float POWERUP_MIN_LIFETIME = 20.0f;  // Minimum seconds the power-up live
    public static final float POWERUP_MAX_LIFETIME = 40.0f;  // Maximum seconds the power-up live
    public static final int POWER_VELOCITY_MODIFIER = 20;
    public static final int POWER_ACCELERATION_MODIFIER = 20;
    public static final int POWER_MASS_MODIFIER = 20;
    public static final int POWERUP_COLLISION_RADIUS = 25;

    // --- RECONCILIATION THRESHOLDS (Tune these values during playtesting) ---
    public static final float IGNORE_THRESHOLD = 30f;
    public static final float SNAP_THRESHOLD = 150f;
    public static final float CORRECTION_LERP = 5.0f;

    // --- INPUT SETTINGS ---
    public static final float JOYSTICK_DEADZONE = 10f;


    // --- RENDER SETTINGS ---
    public static final float PIG_WIDTH = 130;
    public static final float PIG_HEIGHT = 320;
    public static final float PIG_ANGLE_OFFSET = -90;

    public static final float POWER_UP_WIDTH = 30f;
    public static final float POWER_UP_HEIGHT = 30f;
    public static final float POWER_UP_ANGLE_OFFSET = 0;

    private GameConstants() {
    }
}
