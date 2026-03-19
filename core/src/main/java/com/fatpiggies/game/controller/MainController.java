package com.fatpiggies.game.controller;

import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.view.GameStateManager;

public class MainController {

    public GameWorld world;
    public PlayController playController;
    public LobbyController lobbyController;
    public GameStateManager gsm;
    // TODO: add AuthService and DBService into constructor, when firebase is merged
    public MainController(AuthService auth, DatabaseService db) {
        lobbyController = new LobbyController(this);
        gsm = new GameStateManager();
    }


    public void update(float dt) {
        // buffer? @patrick

    }
}
