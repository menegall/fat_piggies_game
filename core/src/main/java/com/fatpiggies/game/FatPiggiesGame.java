package com.fatpiggies.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.fatpiggies.game.model.Snapshot;
import com.fatpiggies.game.view.TextureManager;
import com.fatpiggies.game.view.states.GameStateManager;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class FatPiggiesGame extends ApplicationAdapter {
    private SpriteBatch batch;


    @Override
    public void create() {
        batch = new SpriteBatch();

        TextureManager.loadTextures();
        TextureManager.loadSkin();

        // TODO FOR TESTING
        GameStateManager.getInstance().setMenuState();
        GameStateManager.getInstance().setLobbyState(true);
        GameStateManager.getInstance().setPlayState();
        GameStateManager.getInstance().setOverState(true);
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // FOR TESTING
        Snapshot snapshot = new Snapshot();
        GameStateManager.getInstance().render(batch, snapshot, dt);
    }

    @Override
    public void dispose() {
        TextureManager.dispose();
        batch.dispose();
    }
}
