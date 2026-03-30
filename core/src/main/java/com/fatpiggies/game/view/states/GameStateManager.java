package com.fatpiggies.game.view.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fatpiggies.game.controller.IViewActions;
import com.fatpiggies.game.controller.MainController;
import com.fatpiggies.game.model.Snapshot;

import java.util.ArrayList;
import java.util.Stack;

public class GameStateManager {
    private static GameStateManager gsm;
    private Stack<State> currentState;
    private MainController mc;
    private ArrayList<IViewActions> observers;

    private GameStateManager(MainController mc) {
        currentState = new Stack<State>();
        this.mc = mc;
    }

    public static GameStateManager getInstance(MainController mc) {
        if (gsm == null)
            gsm = new GameStateManager(mc);
        return gsm;
    }

    public void set(State state) {
        this.currentState.pop().dispose();
        this.currentState.push(state);
    }

    public void render(SpriteBatch sb, Snapshot snapshot) {
        currentState.peek().render(sb, snapshot);
    }

    public void setMenuScreen() {
        currentState.pop().dispose();
        this.currentState.push(new MenuState());
    }

    public void setLobbyScreen() {
        currentState.pop().dispose();
        this.currentState.push();
    }

    public void setPlayScreen() {

    }

    public void setOverScreen() {

    }

    public void addObserver(IViewActions observer) {
        observers.add(observer);
    }

    public void removeObserver(IViewActions observer) {
        observers.remove(observer);
    }

    public void notifAllObservers() {
        for(IViewActions observer : observers) {
            observer.update();
        }
    }
//
//    public void notify() {
//
//    }

}
