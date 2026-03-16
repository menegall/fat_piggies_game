package com.fatpiggies.game.view.states;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

    public abstract void render(SpriteBatch sb, Snapshot snapshot);

    public abstract void dispose();
}
