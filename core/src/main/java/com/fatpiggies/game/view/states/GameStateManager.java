package com.fatpiggies.game.view.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fatpiggies.game.model.Snapshot;

import java.util.Stack;

public class GameStateManager {
    private static GameStateManager instance;
    private final Stack<State> states;

    private GameStateManager() {
        states = new Stack<State>();
    }

    public static GameStateManager getInstance() {
        if (instance == null) {
            instance = new GameStateManager();
        }
        return instance;
    }

    private void push(State state) {
        states.push(state);
    }

    private void pop() {
        states.pop().dispose();
    }

    private void set(State state) {
        if (!states.empty()) states.pop().dispose();
        states.push(state);
    }

    public void render(SpriteBatch sb, Snapshot snapshot, float dt) {
        states.peek().update(snapshot, dt);
        states.peek().render(sb);
    }

    // The functions to change states
    public void setMenuState(){set(new MenuState());}
    public void setLobbyState(boolean isHost){set(new LobbyState(isHost));}
    public void setPlayState(){set(new PlayState());}
    public void setOverState(boolean isHost){set(new OverState(isHost));}


}
