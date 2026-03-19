package com.fatpiggies.game.network;

/**
 * Interface defining the authentication service for the game.
 * It abstracts the platform-specific implementation
 * allowing the Core Module to authenticate users without relying on Android libraries.
 */
public interface AuthService {
    /**
     * Authenticates the user silently in the background.
     * This should ideally be called when the game starts
     * so the player is ready to join or host a lobby without explicit login screens.
     *
     * @param callback Triggered when the authentication process finishes, returning the user's ID on success.
     */
    void signIn(AuthCallback callback);
    /**
     * Signs out the current user and clears their session.
     */
    void signOut();
    /**
     * Retrieves the unique identifier (UID) of the currently authenticated player.
     * This ID is crucial and should be used as the 'playerId' or 'hostId'
     * in all DatabaseService operations.
     *
     * @return The unique String ID of the current user, or null if the user is not authenticated.
     */
    String getCurrentUserId();
    /**
     * Callback interface to handle the asynchronous result of the sign-in process.
     */
    interface AuthCallback {
        /**
         * Called when the sign-in process completes successfully.
         *
         * @param userId The unique identifier assigned to the authenticated user.
         */
        void onSuccess(String userId);
        /**
         * Called when the sign-in process fails (e.g., due to network issues).
         *
         * @param error A message describing the reason for the failure.
         */
        void onFailure(String error);
    }
}
