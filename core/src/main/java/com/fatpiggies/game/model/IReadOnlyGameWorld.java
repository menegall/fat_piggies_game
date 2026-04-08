package com.fatpiggies.game.model;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.fatpiggies.game.network.dto.PlayerSetup;

import java.util.Map;

public interface IReadOnlyGameWorld {
    // --- Gameplay ---
    Engine getEngine();
    Entity getLocalPlayer();
}
