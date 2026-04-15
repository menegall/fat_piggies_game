package com.fatpiggies.game.controller.mainControllerInterfaces;

import com.fatpiggies.game.view.PlayerColor;
import com.fatpiggies.game.view.Theme;

public interface IViewActions {

    // Menu
    void onHostLobbyClicked(String playerName, PlayerColor playerColor);

    void onJoinLobbyClicked(String playerName, String lobbyCode, PlayerColor playerColor);

    void onShopClicked();

    // Shop
    void onMenuClicked();

    void onBuyThemeClicked(Theme theme);

    void onSelectTheme(Theme theme);

    boolean isThemeUnlocked(Theme theme);

    int getThemePrice(Theme theme);

    int getCoins();

    // Lobby
    void onStartClicked();

    void onLeaveClicked();

    // Play
    void onJoystickMoved(float x, float y);

    // Over
    void onLobbyClicked();
}
