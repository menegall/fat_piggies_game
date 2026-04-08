package com.fatpiggies.game.view.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fatpiggies.game.controller.IViewActions;
import com.fatpiggies.game.model.GameWorld;
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

    public void render(SpriteBatch sb, float dt) {
        states.peek().update(dt);
        states.peek().render(sb);
    }

    // The functions to change states
    public void setMenuState(IViewActions viewActions) {
        set(new MenuState(viewActions));
    }

    public void setLobbyState(IViewActions viewActions, GameWorld gameWorld, boolean isHost) {
        set(new LobbyState(viewActions, gameWorld, isHost));
    }

    public void setPlayState(IViewActions viewActions, GameWorld gameWorld) {
        set(new PlayState(viewActions, gameWorld));
    }

    public void setOverState(IViewActions viewActions, GameWorld gameWorld, boolean isHost) {
        set(new OverState(viewActions, gameWorld, isHost));
    }

    // Error Handling
    public void showError(String message) {
        states.peek().showError(message);
    }
}
