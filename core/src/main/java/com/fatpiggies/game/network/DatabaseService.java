package com.fatpiggies.game.network;

import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.network.dto.LobbyInfo;
import com.fatpiggies.game.network.dto.PlayerInput;

import java.util.Map;

/**
 * Interface defining the network operations for the game using a real-time database.
 * This service handles lobby management, matchmaking, and high-frequency game state synchronization.
 */
public interface DatabaseService {
    // ==========================
    // ---- Lobby Management ----
    // ==========================

    /**
     * Creates a new multiplayer lobby. The caller automatically becomes the Host.
     * This method initializes the lobby and populate it with the Host's data.
     *
     * @param hostId     The unique identifier of the user creating the lobby (from AuthService).
     * @param playerName The display name chosen by the host.
     * @param callback   Triggered with the unique lobby ID on success, or a specific {@link NetworkError}.
     */
    void createLobby(String hostId, String playerName, LobbyCallback callback);
    /**
     * Attempts to join an existing lobby using a short 6-character code.
     * If successful, the player's data is added to the lobby.
     *
     * @param lobbyCode  The short 6-character code shared by the Host.
     * @param playerId   The unique identifier of the user joining (from AuthService).
     * @param playerName The display name chosen by the player.
     * @param callback   Triggered with the unique lobby ID on success, or a specific {@link NetworkError}.
     */
    void joinLobby(String lobbyCode, String playerId, String playerName, LobbyCallback callback);
    /**
     * Removes a player from the lobby.
     * If the caller is a Client, only their data is removed.
     * If the caller is the Host, the entire lobby is destroyed.
     *
     * @param lobbyId  The unique ID of the lobby.
     * @param playerId The unique ID of the player leaving.
     */
    void leaveLobby(String lobbyId, String playerId);
    /**
     * Changes the lobby status from "waiting" to "playing".
     * This signals to all listening clients that they should transition to the PlayState.
     * This should typically only be called by the Host.
     *
     * @param lobbyId The unique ID of the lobby.
     */
    void startGame(String lobbyId);

    /**
     * Changes the lobby status to "over".
     * Signals to clients that the match has concluded and they should transition to the OverState.
     *
     * @param lobbyId The unique ID of the lobby.
     */
    void endGame(String lobbyId);
    /**
     * Listens for changes in the lobby's setup information (e.g., new players joining, status changes).
     * This is primarily used in the LobbyState while waiting for the game to start.
     * Remember to call {@link #stopListening()} when leaving the waiting area.
     * Is also used in PlayState to listen for changes in lobby status.
     *
     * @param lobbyId  The unique ID of the lobby.
     * @param callback Triggered every time the lobby info changes.
     */
    void listenToLobbyInfo(String lobbyId, LobbyInfoCallback callback);

    // ======================================
    // ---- Game State Management (Host) ----
    // ======================================
    /**
     * Pushes the authoritative game state to the database.
     * This includes entity positions, health, and power-ups calculated by the server logic.
     * This method is high-frequency (fire-and-forget) and MUST ONLY be called by the Host.
     *
     * @param lobbyId The unique ID of the lobby.
     * @param state   The full calculated game state object.
     */
    void pushGameState(String lobbyId, GameState state);
    /**
     * Listens for incoming movement and action inputs from all clients.
     * The Host uses these inputs to calculate the physics simulation.
     *
     * @param lobbyId  The unique ID of the lobby.
     * @param callback Triggered with a map of player IDs to their respective inputs.
     */
    void listenToInputs(String lobbyId, InputsCallback callback);

    // ========================================
    // ---- Game State Management (Client) ----
    // ========================================
    /**
     * Pushes the local player's input to the database.
     * This updates only the specific player's node to prevent overwriting others.
     * This method is high-frequency (fire-and-forget).
     *
     * @param lobbyId  The unique ID of the lobby.
     * @param playerId The unique ID of the client sending the input.
     * @param data     The input data containing velocities and actions.
     */
    void pushPlayerInput(String lobbyId, String playerId, PlayerInput data);
    /**
     * Listens for the authoritative game state calculated by the Host.
     * Clients use this data to correct local predictions and update remote entities.
     *
     * @param lobbyId  The unique ID of the lobby.
     * @param callback Triggered every time the Host updates the game state.
     */
    void listenToGameState(String lobbyId, GameStateCallback callback);

    // ==============================
    // ---- Cleanup and Callback ----
    // ==============================
    /**
     * Removes all active database listeners (LobbyInfo, GameState, Inputs).
     * MUST be called to prevent memory leaks and unnecessary network usage when
     * exiting a match or returning to the main menu.
     */
    void stopListening();

    // ---- Callback ----
    interface LobbyCallback {
        void onSuccess(String lobbyId);
        void onError(NetworkError error, String errorMessage);
    }
    interface LobbyInfoCallback {
        void onInfoUpdated(LobbyInfo info);
        void onError(NetworkError error, String errorMessage);
    }
    interface InputsCallback {
        void onInputsReceived(Map<String, PlayerInput> inputs);
        void onError(NetworkError error, String errorMessage);
    }
    interface GameStateCallback {
        void onDataReceived(GameState data);
        void onError(NetworkError error, String errorMessage);

    }
}
