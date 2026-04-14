package com.fatpiggies.game.controller;

import com.fatpiggies.game.setting.PreferencesManager;
import com.fatpiggies.game.view.Theme;
import com.fatpiggies.game.view.TextureManager;

import java.util.HashSet;
import java.util.Set;

public class ShopController {

    private int coins;
    private Set<Theme> unlocked;

    private static final int FARM_PRICE = 0;
    private static final int VOLCANO_PRICE = 100;
    private static final int SPACE_PRICE = 300;

    public ShopController() {
        load();
    }

    public boolean buy(Theme theme) {
        if (unlocked.contains(theme)) return false;

        int price = getPrice(theme);

        if (coins < price) {
            System.out.println("Not enough coins");
            return false;
        }

        coins -= price;
        unlocked.add(theme);

        save();
        return true;
    }

    public void addCoins(int reward){
        coins += reward;
        save();
    }

    public boolean isUnlocked(Theme theme) {
        return unlocked.contains(theme);
    }

    public void select(Theme theme) {
        if (unlocked.contains(theme)) {
            TextureManager.setTheme(theme);
            PreferencesManager.saveTheme(theme);
        }
    }

    public int getCoins() {
        return coins;
    }

    public int getPrice(Theme theme) {
        switch(theme){
            case FARM: return FARM_PRICE;
            case VOLCANO: return VOLCANO_PRICE;
            case SPACE: return SPACE_PRICE;
            default: return FARM_PRICE;
        }
    }

    // ===== SAVE / LOAD =====

    private void save() {
        PreferencesManager.saveCoins(coins);
        PreferencesManager.saveUnlockedThemes(unlocked);
    }

    private void load() {
        coins = PreferencesManager.loadCoins();
        unlocked = PreferencesManager.loadUnlockedThemes();

        if (unlocked.isEmpty()) {
            unlocked.add(Theme.FARM);
        }

        TextureManager.setTheme(PreferencesManager.loadTheme());
    }
}
