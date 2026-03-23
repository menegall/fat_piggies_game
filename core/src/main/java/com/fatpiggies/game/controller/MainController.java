package com.fatpiggies.game.controller;

import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.network.dto.GameState;
import com.fatpiggies.game.view.states.GameStateManager;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.view.states.GameStateManager;

public class MainController {

    public GameWorld world;
    public IPlayController playController;
    public LobbyController lobbyController;
    public AuthService auth;
    public DatabaseService dbs;
    public GameStateManager gsm;
    // TODO: add AuthService and DBService into constructor, when firebase is merged
    public MainController(AuthService auth, DatabaseService db) {
        lobbyController = new LobbyController(this);
        gsm = GameStateManager.getInstance();
    }


    public void update(float dt) {
        // buffer? @patrick

    }
}
