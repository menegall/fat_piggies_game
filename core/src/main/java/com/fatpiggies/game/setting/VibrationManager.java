package com.fatpiggies.game.setting;

import com.badlogic.gdx.Gdx;

public class VibrationManager {

    private static boolean enabled = true;

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void vibrate(int ms) {
        if (enabled) {
            Gdx.input.vibrate(ms);
        }
    }
}
