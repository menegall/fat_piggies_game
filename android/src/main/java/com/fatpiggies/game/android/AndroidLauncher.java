package com.fatpiggies.game.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.fatpiggies.game.FatPiggiesGame;
import com.fatpiggies.game.android.network.AndroidAuth;
import com.fatpiggies.game.android.network.AndroidDatabase;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;

/**
 * Launches the Android application.
 */
public class AndroidLauncher extends AndroidApplication {
    private AuthService authService;
    private DatabaseService databaseService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.

        this.authService = new AndroidAuth();
        this.databaseService = new AndroidDatabase();
        initialize(new FatPiggiesGame(this.authService, this.databaseService), configuration);
    }
}
