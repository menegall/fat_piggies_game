package com.fatpiggies.game.model.utils;

public final class GameConstants {
    // --- ARENA SETTINGS ---
    public static final float WORLD_WIDTH = 2000f;
    public static final float WORLD_HEIGHT = 1000f;

    public static final float LEFT_BOUND = 100f;
    public static final float RIGHT_BOUND = WORLD_WIDTH - LEFT_BOUND;
    public static final float BOTTOM_BOUND = 70f;
    public static final float TOP_BOUND = WORLD_HEIGHT - BOTTOM_BOUND;
    public static final float GROUND_FRICTION = 400f;
    public static final float FORCE_FACTOR = 15f;
    public static final float STOP_THRESHOLD = 0.0001f;
    public static final float INPUT_DEADZONE = 0.1f;

    // --- RESPAWN & SPAWN SETTINGS ---
    public static final float SAFE_SPAWN_RADIUS = 100f;
    public static final int MAX_SPAWN_ATTEMPTS = 10;
    public static final float RESPAWN_TIMER = 2f;

    // --- PLAYER SETTINGS ---
    public static final float PLAYER_BASE_VELOCITY = 600;
    public static final float PLAYER_BASE_ACCELERATION = 1000f;
    public static final float PLAYER_BASE_MASS = 10f;
    public static final int MAX_PLAYERS = 4;
    public static final int BASE_LIFE = 5;
    public static final float LERP_FACTOR = 6f;
    public static final int PLAYER_COLLISION_RADIUS = 60;
    public static final float PLAYER_BOUNCINESS = 2f; // better if value between 1.5 and 3.0


    // --- POWERUP SETTINGS ---
    public static final float POWERUP_SPAWN_INTERVAL = 7.0f; // Spawns a new power-up every 7 seconds
    public static final float POWERUP_MIN_LIFETIME = 10.0f;  // Minimum seconds the power-up live
    public static final float POWERUP_MAX_LIFETIME = 30.0f;  // Maximum seconds the power-up live
    public static final int POWER_VELOCITY_MODIFIER = 250;
    public static final int POWER_ACCELERATION_MODIFIER = 1500;
    public static final int POWER_MASS_MODIFIER = 15;
    public static final int POWERUP_COLLISION_RADIUS = 30;

    // --- RECONCILIATION THRESHOLDS (Tune these values during playtesting) ---

    public static final float SNAP_THRESHOLD = 500f;
    // TODO normally we can delete the next two
    public static final float IGNORE_THRESHOLD = 100f;
    public static final float CORRECTION_LERP = 20f;

    // --- INPUT SETTINGS ---
    public static final float JOYSTICK_DEADZONE = 10f;


    // --- RENDER SETTINGS ---
    public static final float PIG_WIDTH = 150;
    public static final float PIG_HEIGHT = 170;
    public static final float PIG_ANGLE_OFFSET = -90;

    public static final float POWER_UP_WIDTH = 50f;
    public static final float POWER_UP_HEIGHT = 50f;
    public static final float POWER_UP_ANGLE_OFFSET = 0;

    // --- NETWORK Constants ---
    public static final float SEND_THRESHOLD = 1 / 20f; // 1/times per second

    private GameConstants() {
    }
}
