package com.fatpiggies.game.view.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fatpiggies.game.model.Snapshot;
import com.fatpiggies.game.view.TextureManager;

import java.util.Stack;

public class GameStateManager {
    private static GameStateManager gsm;
    private Stack<State> states;

    private GameStateManager() {
        states = new Stack<State>();
    }

    public static GameStateManager getInstance() {
        if (gsm == null)
            gsm = new GameStateManager();
        return gsm;
    }

    public void push(State state) {
        states.push(state);
    }

    public void pop() {
        states.pop().dispose();
    }

    public void set(State state) {
        states.pop().dispose();
        states.push(state);
    }

    public void render(SpriteBatch sb, Snapshot snapshot) {
        states.peek().render(sb, snapshot);
    }

    public void setMenuScreen() {
        pop();
        push(new MenuState());
    }

    public void setLobbyScreen() {

    }

    public void setPlayScreen() {

    }

    public void setOverScreen() {

    }

}
