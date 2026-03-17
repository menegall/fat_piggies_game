package com.fatpiggies.game.controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fatpiggies.game.view.states.GameStateManager;
import com.fatpiggies.game.view.states.LobbyState;
import com.fatpiggies.game.view.states.State;

public class MenuController {

    private GameStateManager gsm;
    private SpriteBatch batch;
    private MainController main;

    public MenuController(MainController main) {
        this.main = main;
    }

    public void create(boolean isHost){
        batch = new SpriteBatch();
        gsm = new GameStateManager();
        gsm.set(new LobbyState(isHost));

    }

    public void render(){
        gsm.render(batch);
    }

    public void dispose(){
        batch.dispose();
    }


}
