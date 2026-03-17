package com.fatpiggies.game.controller;

import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.view.GameStateManager;

public class MainController {

    public GameWorld world;
    public PlayController playController;
    public MenuController menuController;
    public GameStateManager gsm;
    // TODO: add AuthService and DBService into constructor, when firebase is merged
    public MainController() {
        playController = new PlayController(this);
        menuController = new MenuController(this);
        gsm = new GameStateManager();
        world = new GameWorld();
    }


    public void update(float dt) {
        // buffer? @patrick

    }
}
