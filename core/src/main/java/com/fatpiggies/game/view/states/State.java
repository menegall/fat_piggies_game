package com.fatpiggies.game.view.states;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.fatpiggies.game.model.Snapshot;

public abstract class State {
    protected OrthographicCamera cam;
    protected Vector3 touchPoint;


    protected State() {
        cam = new OrthographicCamera();
        touchPoint = new Vector3();
    }

    public abstract void update(Snapshot snapshot, float dt);

    public abstract void render(SpriteBatch sb);

    public abstract void dispose();

    public abstract void showError(String message);
}
