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

    // ===== PLAYER NAME =====
    private static final String KEY_NAME = "player_name";

    public static void savePlayerName(String name) {
        prefs.putString(KEY_NAME, name);
        prefs.flush();
    }

    public static String loadPlayerName() {
        return prefs.getString(KEY_NAME, "");
    }

    // ===== PIG COLOR (last one) =====
    public static void savePig(TextureId pig) {
        prefs.putString("selected_pig", pig.name());
        prefs.flush();
    }

    public static TextureId loadPig() {
        return TextureId.valueOf(
            prefs.getString("selected_pig", TextureId.OVER_BLUE_PIG.name())
        );
    }

    // ===== COINS =====
    public static void saveCoins(int coins) {
        prefs.putInteger("coins", coins);
        prefs.flush();
    }

    public static int loadCoins() {
        return prefs.getInteger("coins", 90);
    }

    // ===== UNLOCKED BACKGROUNDS =====
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

    // ===== SELECTED BACKGROUNDS =====
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
