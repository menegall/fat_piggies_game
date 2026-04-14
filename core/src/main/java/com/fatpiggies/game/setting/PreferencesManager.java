package com.fatpiggies.game.setting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.fatpiggies.game.view.TextureId;

import java.util.HashSet;
import java.util.Set;

public class PreferencesManager {

    private static final Preferences prefs =
        Gdx.app.getPreferences("fatpiggies_save");

    private PreferencesManager() {}

    // ===== COINS =====
    public static void saveCoins(int coins) {
        prefs.putInteger("coins", coins);
        prefs.flush();
    }

    public static int loadCoins() {
        return prefs.getInteger("coins", 90);
    }

    // ===== UNLOCKED =====
    public static void saveUnlocked(Set<TextureId> unlocked) {
        StringBuilder sb = new StringBuilder();

        for (TextureId id : unlocked) {
            sb.append(id.name()).append(",");
        }

        prefs.putString("unlocked", sb.toString());
        prefs.flush();
    }

    public static Set<TextureId> loadUnlocked() {
        String data = prefs.getString("unlocked", "");
        Set<TextureId> result = new HashSet<>();

        if (!data.isEmpty()) {
            String[] split = data.split(",");
            for (String s : split) {
                if (!s.isEmpty()) {
                    result.add(TextureId.valueOf(s));
                }
            }
        }

        return result;
    }

    // ===== SELECTED =====
    public static void saveSelected(TextureId bg) {
        prefs.putString("selected_bg", bg.name());
        prefs.flush();
    }

    public static TextureId loadSelected() {
        return TextureId.valueOf(
            prefs.getString("selected_bg", TextureId.PLAY_BACKGROUND_1.name())
        );
    }

    // ===== RESET =====
    public static void reset() {
        prefs.clear();
        prefs.flush();
    }
}
