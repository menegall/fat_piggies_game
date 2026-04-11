package com.fatpiggies.game.view.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.view.TextureManager;

public abstract class State {
    protected OrthographicCamera cam;
    protected Vector3 touchPoint;
    protected final float screenWidth = Gdx.graphics.getWidth();
    protected final float screenHeight = Gdx.graphics.getHeight();
    protected Stage stage;
    protected Skin skin;
    protected IViewActions viewActions;


    protected State(IViewActions viewActions) {
        cam = new OrthographicCamera();
        touchPoint = new Vector3();
        this.viewActions = viewActions;

        stage = new Stage(new ScreenViewport());
        skin = TextureManager.getSkin();
    }

    public abstract void update(float dt);

    public abstract void render(SpriteBatch sb);

    public void dispose() {
        stage.dispose();
    }

    public void showError(String message){};

    public void show() {Gdx.input.setInputProcessor(getInputProcessor());}

    public InputProcessor getInputProcessor() {return stage;}
}
