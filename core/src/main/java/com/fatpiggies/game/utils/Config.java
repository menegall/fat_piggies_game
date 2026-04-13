package com.fatpiggies.game.utils;

public class Config {
    // ==========================================
    // ---- TAGS NETWORKING (Firebase) ---
    // ==========================================
    public static final String TAG_AUTH     = "FatPig_Auth";     // Login, logout, session
    public static final String TAG_DATABASE = "FatPig_DB";       // CreateLobby, joinLobby, error DB
    public static final String TAG_NETWORK  = "FatPig_Net";      // Latency, disconnection,

    // ==========================================
    // ---- TAGS ARCHITECTURE (MVC) -------
    // ==========================================
    public static final String TAG_APP      = "FatPig_App";      // Lifecycle (create, pause, resume, dispose)
    public static final String TAG_STATE    = "FatPig_State";    // For state management (Menu -> Lobby -> Play)
    public static final String TAG_CTRL     = "FatPig_Ctrl";     // Controller Logic (PlayController, MainController)

    // ==========================================
    // ---- TAGS ECS  ----
    // ==========================================
    public static final String TAG_ECS      = "FatPig_ECS";      // Creation/destroy entity
    public static final String TAG_PHYSICS  = "FatPig_Phys";     // For Collision or movements (MovementSystem, CollisionSystem)

    // ==========================================
    // ---- TAGS ASSETS and RESOURCE -----------
    // ==========================================
    public static final String TAG_ASSETS   = "FatPig_Assets";   // For TextureManager (Load Images, ...)
}
