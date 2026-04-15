package com.fatpiggies.game.setting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.fatpiggies.game.view.PlayerColor;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.Theme;

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
    public static void saveColor(PlayerColor color) {
        prefs.putString("selected_pig", color.name());
        prefs.flush();
    }

    public static PlayerColor loadColor() {
        return PlayerColor.valueOf(
            prefs.getString("selected_pig", PlayerColor.BLUE.name())
        );
    }

    // ===== COINS =====
    public static void saveCoins(int coins) {
        prefs.putInteger("coins", coins);
        prefs.flush();
    }

    public static int loadCoins() {
        return prefs.getInteger("coins", 130);
    }

    // ===== THEME =====
    public static void saveTheme(Theme theme) {
        prefs.putString("theme", theme.name());
        prefs.flush();
    }

    public static Theme loadTheme() {
        return Theme.valueOf(
            prefs.getString("theme", Theme.FARM.name())
        );
    }

    // ===== UNLOCKED THEMES =====
    public static void saveUnlockedThemes(Set<Theme> themes) {
        StringBuilder sb = new StringBuilder();

        for (Theme t : themes) {
            sb.append(t.name()).append(",");
        }

        prefs.putString("themes", sb.toString());
        prefs.flush();
    }

    public static Set<Theme> loadUnlockedThemes() {
        String data = prefs.getString("themes", "");
        Set<Theme> result = new HashSet<>();

        if (!data.isEmpty()) {
            for (String s : data.split(",")) {
                if (!s.isEmpty()) {
                    result.add(Theme.valueOf(s));
                }
            }
        }

        return result;
    }

    // ===== RESET =====
    public static void reset() {
        prefs.clear();
        prefs.flush();
    }
}
