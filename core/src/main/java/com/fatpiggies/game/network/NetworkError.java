package com.fatpiggies.game.network;

public enum NetworkError {
    LOBBY_NOT_FOUND,        // Inserted code doesn't exist
    LOBBY_FULL,             // Lobby is full
    LOBBY_ALREADY_STARTED,  // The game for the provided lobby is started
    NAME_ALREADY_EXIST,     // Name already exist
    DATABASE_ERROR,         // Database error
}


