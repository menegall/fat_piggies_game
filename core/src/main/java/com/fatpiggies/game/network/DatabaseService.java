package com.fatpiggies.game.network;

import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.network.dto.PlayerInput;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.view.PlayerColor;

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
     * @param playerColor The display color chosen by the player.
     * @param callback   Triggered with the unique lobby ID on success, or a specific {@link NetworkError}.
     */
    void createLobby(String hostId, String playerName, PlayerColor playerColor, LobbyCallback callback);

    /**
     * Attempts to join an existing lobby using a short 6-character code.
     * If successful, the player's data is added to the lobby.
     *
     * @param lobbyCode  The short code shared by the Host.
     * @param playerId   The unique identifier of the user joining (from AuthService).
     * @param playerName The display name chosen by the player.
     * @param playerColor The display color chosen by the player.
     * @param callback   Triggered with the unique lobby ID on success, or a specific {@link NetworkError}.
     */
    void joinLobby(String lobbyCode, String playerId, String playerName, PlayerColor playerColor, LobbyCallback callback);

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
     * Changes the lobby status to "waiting".
     * Signals to clients that the match has concluded and they should transition to the LobbyState.
     *
     * @param lobbyId The unique ID of the lobby.
     */
    void resetLobbyToWaiting(String lobbyId);

    /**
     * Listens for real-time updates regarding the lobby's lifecycle state ( waiting, playing, over).
     * <p>
     * This is essential for triggering transitions between different game states and ensuring
     * all clients are synchronized with the server's master state.
     * Ensure {@link #stopListening()} is called when the listener is no longer needed to
     * prevent memory leaks and unnecessary network traffic.
     * </p>
     *
     * @param lobbyId  The unique ID of the lobby to monitor.
     * @param callback Triggered whenever the lobby's operational status changes.
     */
    void listenToLobbyStatus(String lobbyId, LobbyStatusCallback callback);

    /**
     * Monitors changes players configurations within a specific lobby.
     * <p>
     * This method tracks changes in players joined to the specified lobby.
     * It is typically used to dynamically refresh the player list UI during the pre-game assembly phase.
     * Remember to call {@link #stopListening()} when navigating away from the lobby screen.
     * </p>
     *
     * @param lobbyId  The unique ID of the lobby.
     * @param callback Triggered every time a player's setup or readiness is updated.
     */
    void listenToPlayersSetup(String lobbyId, PlayersSetupCallback callback);

    /**
     * Fetches the lobby code once and returns it via a callback.
     * * @param lobbyId The unique ID of the lobby.
     *
     * @param callback A simple interface to handle the result asynchronously.
     */
    void getLobbyCodeOnce(String lobbyId, CodeCallback callback);

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

    // ======================================
    // ---- End Game & Ranking -----------
    // ======================================

    /**
     * Pushes the final ranking of the match to the database.
     * This MUST ONLY be called by the Host exactly when the game finishes,
     * right before or together with calling endGame().
     *
     * @param lobbyId         The unique ID of the lobby.
     * @param rankedPlayerIds A list of player IDs ordered from 1st place (index 0) to last place.
     */
    void pushFinalRank(String lobbyId, java.util.List<String> rankedPlayerIds);

    /**
     * Retrieves the final ranking of the match once it's over.
     * Clients call this when transitioning to the OverState to display the leaderboard.
     *
     * @param lobbyId  The unique ID of the lobby.
     * @param callback Triggered with the ordered list of player IDs.
     */
    void getFinalRank(String lobbyId, FinalRankCallback callback);

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

        void onError(NetworkError error);
    }

    interface LobbyStatusCallback {
        void onStatusUpdated(String status);

        void onError(NetworkError error);
    }

    interface PlayersSetupCallback {
        void onPlayersSetupUpdated(Map<String, PlayerSetup> playersSetup);

        void onError(NetworkError error);
    }

    interface InputsCallback {
        void onInputsReceived(Map<String, PlayerInput> inputs);

        void onError(NetworkError error);
    }

    interface GameStateCallback {
        void onDataReceived(GameState data);

        void onError(NetworkError error);

    }

    interface CodeCallback {
        void onCodeRetrieved(String code);

        void onError(NetworkError error);;
    }

    interface FinalRankCallback {
        void onRankRetrieved(java.util.List<String> rankedPlayerIds);

        void onError(NetworkError error);
    }


}
