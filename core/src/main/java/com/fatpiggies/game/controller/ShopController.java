package com.fatpiggies.game.controller;

import com.fatpiggies.game.setting.PreferencesManager;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

import java.util.HashSet;
import java.util.Set;

public class ShopController {

    private int coins;
    private Set<TextureId> unlocked;

    private static final int BACKGROUND_1_PRICE = 0;
    private static final int BACKGROUND_2_PRICE = 100;
    private static final int BACKGROUND_3_PRICE = 150;

    public ShopController() {
        load();
    }

    public boolean buy(TextureId bg) {
        if (unlocked.contains(bg)) return false;

        if (coins < getPrice(bg)) {
            System.out.println("Not enough coins");
            return false;
        }

        coins -= getPrice(bg);
        unlocked.add(bg);

        save();
        return true;
    }

    public void addCoins(int recompense){
        coins += recompense;
    }


    public boolean isUnlocked(TextureId bg) {
        return unlocked.contains(bg);
    }

    public void select(TextureId bg) {
        if (unlocked.contains(bg)) {
            TextureManager.setBackground(bg);
            PreferencesManager.saveSelected(bg);
        }
    }

    public int getCoins() {
        return coins;
    }

    public int getPrice(TextureId bg) {
        switch(bg){
            case PLAY_BACKGROUND_1 : return BACKGROUND_1_PRICE;
            case PLAY_BACKGROUND_2 : return BACKGROUND_2_PRICE;
            case PLAY_BACKGROUND_3 : return BACKGROUND_3_PRICE;
            default : return BACKGROUND_1_PRICE;
        }
    }

    // ===== SAVE / LOAD =====

    private void save() {
        PreferencesManager.saveCoins(coins);
        PreferencesManager.saveUnlocked(unlocked);
    }

    private void load() {
        coins = PreferencesManager.loadCoins();
        unlocked = PreferencesManager.loadUnlocked();

        // One unlocked bg
        if (unlocked.isEmpty()) {
            unlocked.add(TextureId.PLAY_BACKGROUND_1);
        }

        TextureManager.setBackground(PreferencesManager.loadSelected());
    }
}
