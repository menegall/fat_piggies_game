package com.fatpiggies.game.model;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.fatpiggies.game.model.ecs.components.HealthComponent;
import com.fatpiggies.game.model.ecs.components.RenderComponent;
import com.fatpiggies.game.network.dto.PlayerSetup;
import com.fatpiggies.game.view.TextureId;

import java.util.Map;

public interface IReadOnlyGameWorld {
    // --- Gameplay ---
    Engine getEngine();

    Entity getLocalPlayer();

    int getLocalPlayerLife();

    TextureId getLocalPlayerTexture();

    boolean isLocalPlayerAlive();
}
