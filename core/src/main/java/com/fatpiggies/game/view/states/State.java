package com.fatpiggies.game.view.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.fatpiggies.game.model.Snapshot;
import com.fatpiggies.game.view.TextureManager;

public abstract class State {
    protected OrthographicCamera cam;
    protected Vector3 touchPoint;
    protected final float screenWidth = Gdx.graphics.getWidth();
    protected final float screenHeight = Gdx.graphics.getHeight();
    protected Stage stage;
    protected Skin skin;


    protected State() {
        cam = new OrthographicCamera();
        touchPoint = new Vector3();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = TextureManager.getSkin();
    }

    public abstract void update(Snapshot snapshot, float dt);

    public abstract void render(SpriteBatch sb);

    public void dispose() {
        stage.dispose();
    }

    public abstract void showError(String message);
}
