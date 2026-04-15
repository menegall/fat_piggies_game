package com.fatpiggies.game.model;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.fatpiggies.game.view.TextureId;

public interface IReadOnlyGameWorld {
    // --- Gameplay ---
    Engine getEngine();

    Entity getLocalPlayer();

    int getLocalPlayerLife();

    TextureId getLocalPlayerTexture();

    boolean isLocalPlayerAlive();
}
