package com.fatpiggies.game.controller.mainControllerInterfaces;

import com.fatpiggies.game.view.PlayerColor;
import com.fatpiggies.game.view.TextureId;

public interface IViewActions {

    // Menu
    void onHostLobbyClicked(String playerName, PlayerColor playerColor);

    void onJoinLobbyClicked(String playerName, String lobbyCode, PlayerColor playerColor);

    void onShopClicked();


    // Shop
    void onMenuClicked();

    void onBuyBackgroundClicked(TextureId bg);

    void onSelectBackground(TextureId bg);

    int getCoins();

    int getPrice(TextureId bg);

    boolean isBackgroundUnlocked(TextureId bg);

    // Lobby
    void onStartClicked();

    void onLeaveClicked();

    // Play
    void onJoystickMoved(float x, float y);

    // Over
    void onLobbyClicked();
}
