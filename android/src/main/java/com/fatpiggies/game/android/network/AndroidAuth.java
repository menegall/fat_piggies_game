package com.fatpiggies.game.android.network;

import static com.fatpiggies.game.utils.Config.TAG_AUTH;

import android.util.Log;

import com.fatpiggies.game.network.AuthService;
import com.google.firebase.auth.FirebaseAuth;

public class AndroidAuth implements AuthService {
    private final FirebaseAuth mAuth;

    public AndroidAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    // Constructor for testing
    @androidx.annotation.VisibleForTesting
    public AndroidAuth(FirebaseAuth auth) {
        this.mAuth = auth;
    }

    @Override
    public void signIn(AuthCallback callback) {
        Log.i(TAG_AUTH, "signInAnonymously");
        // Used to monitor response time
        final long startTime = System.currentTimeMillis();

        mAuth.signInAnonymously()
            .addOnCompleteListener(task -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                if (task.isSuccessful()) {
                    // Sign in success
                    Log.d(TAG_AUTH, "signInAnonymously:success. Duration: " + duration + "ms");
                    callback.onSuccess(getCurrentUserId());
                } else {
                    // If sign in fails.
                    Log.w(TAG_AUTH, "signInAnonymously:failure. Duration: " + duration + "ms", task.getException());
                    callback.onFailure("Authentication failed.");
                }
            });
    }

    @Override
    public void signOut() {
        if (mAuth != null) {
            mAuth.signOut();
            Log.i(TAG_AUTH, "User signed out.");
        }
    }

    @Override
    public String getCurrentUserId() {
        com.google.firebase.auth.FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }
}
