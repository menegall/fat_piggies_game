package com.fatpiggies.game.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.fatpiggies.game.FatPiggiesGame;
import com.fatpiggies.game.lwjgl3.network.DesktopAuth;
import com.fatpiggies.game.lwjgl3.network.DesktopDatabase;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;

import io.github.cdimascio.dotenv.Dotenv;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    private static AuthService authService;
    private static DatabaseService databaseService;

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.


        Dotenv dotenv = Dotenv.load();

        String apiKey = dotenv.get("FIREBASE_API_KEY");
        String dbUrl = dotenv.get("FIREBASE_DB_URL");

        DesktopAuth desktopAuth = new DesktopAuth(apiKey);

        authService = desktopAuth;
        databaseService = new DesktopDatabase(dbUrl, desktopAuth);

        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new FatPiggiesGame(authService, databaseService), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("fat-piggies-game");

        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);

        configuration.setWindowedMode(1280, 720);

        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);

        return configuration;
    }
}
