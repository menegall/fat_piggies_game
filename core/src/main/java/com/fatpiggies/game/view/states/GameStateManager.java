package com.fatpiggies.game.view.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.model.IReadOnlyGameWorld;
import com.fatpiggies.game.model.IReadOnlyLobbyModel;

import java.util.Stack;

public class GameStateManager {
    private static GameStateManager instance;
    private final Stack<State> states;

    private GameStateManager() {
        states = new Stack<>();
    }

    public static GameStateManager getInstance() {
        if (instance == null) {
            instance = new GameStateManager();
        }
        return instance;
    }

    // ========================
    // Core stack operations
    // ========================

    private void push(State state) {
        states.push(state);
        state.show();
    }

    private void popInternal() {
        if (!states.isEmpty()) {
            states.pop().dispose();
        }
    }

    private void set(State state) {
        dispose();
        states.push(state);
        state.show();
    }

    private void dispose() {
        while (!states.isEmpty()) {
            states.pop().dispose();
        }
    }

    // ========================
    // Public navigation API
    // ========================
    public void popToMenu() {
        while (states.size() > 1 && !(states.peek() instanceof MenuState)) {
            popInternal();
        }

        if (!states.isEmpty() && states.peek() instanceof MenuState) {
            states.peek().show();
        }
    }

    public void popToLobby() {
        while (states.size() > 1 && !(states.peek() instanceof LobbyState)) {
            popInternal();
        }

        if (!states.isEmpty() && states.peek() instanceof LobbyState) {
            states.peek().show();
        }
    }

    // ========================
    // Render loop
    // ========================

    public void render(SpriteBatch sb, float dt) {
        if (states.isEmpty()) return;

        State current = states.peek();
        current.update(dt);
        current.render(sb);
    }

    // ========================
    // State creation (navigation)
    // ========================

    public void pushMenuState(IViewActions viewActions) {
        push(new MenuState(viewActions));
    }

    public void pushLobbyState(IViewActions viewActions, IReadOnlyLobbyModel lobbyModel, boolean isHost) {
        push(new LobbyState(viewActions, lobbyModel, isHost));
    }

    public void pushPlayState(IViewActions viewActions, IReadOnlyGameWorld gameWorld) {
        push(new PlayState(viewActions, gameWorld));
    }

    public void pushOverState(IViewActions viewActions, IReadOnlyLobbyModel lobbyModel, boolean isHost) {
        push(new OverState(viewActions, lobbyModel, isHost));
    }

    // ========================
    // Reset navigation (rare use)
    // ========================

    public void setMenuState(IViewActions viewActions) {
        set(new MenuState(viewActions));
    }

    // ========================
    // Error handling
    // ========================

    public void showError(String message) {
        if (!states.isEmpty()) {
            states.peek().showError(message);
        }
    }
}
