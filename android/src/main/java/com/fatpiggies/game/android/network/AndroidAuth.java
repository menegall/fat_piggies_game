package com.fatpiggies.game.android.network;

import static com.fatpiggies.game.utils.Config.TAG_AUTH;

import android.util.Log;

import androidx.annotation.NonNull;

import com.fatpiggies.game.network.AuthService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AndroidAuth implements AuthService {
    private FirebaseAuth mAuth;

    public AndroidAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void signIn(AuthCallback callback) {
        Log.i(TAG_AUTH, "signInAnonymously");
        mAuth.signInAnonymously()
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG_AUTH, "signInAnonymously:success");
                        callback.onSuccess(getCurrentUserId());
                    } else {
                        // If sign in fails.
                        Log.w(TAG_AUTH, "signInAnonymously:failure", task.getException());
                        callback.onFailure("Authentication failed.");
                    }
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
        String userId = mAuth.getCurrentUser().getUid();
        return userId;
    }
}
