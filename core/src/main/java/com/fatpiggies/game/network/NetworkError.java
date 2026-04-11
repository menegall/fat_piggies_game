package com.fatpiggies.game.network;

/**
 * Represents the various error conditions that can occur during network and database operations.
 * <p>
 * This enumeration is utilized within the error callbacks of network services
 * (such as {@link DatabaseService}) to provide specific, actionable error types
 * rather than generic string messages. This allows the game's controllers to easily
 * handle different failure scenarios using a switch statement and provide the player
 * with accurate UI feedback.
 */
public enum NetworkError {
    LOBBY_NOT_FOUND,        // Inserted code doesn't exist
    LOBBY_FULL,             // Lobby is full
    LOBBY_ALREADY_STARTED,  // The game for the provided lobby is started
    NAME_ALREADY_EXIST,     // Name already exist
    COLOR_ALREADY_TAKEN,    // Color already exist
    DATABASE_ERROR,         // Database error
    HOST_LEFT_LOBBY
}


