package com.fatpiggies.game;


import static com.fatpiggies.game.utils.Config.TAG_APP;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.fatpiggies.game.controller.MainController;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.model.Snapshot;
import com.fatpiggies.game.view.TextureManager;
import com.fatpiggies.game.view.states.GameStateManager;


/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class FatPiggiesGame extends ApplicationAdapter {
    AuthService authService;
    DatabaseService databaseService;
    private SpriteBatch batch;

    public FatPiggiesGame(AuthService authService, DatabaseService databaseService) {
        this.authService = authService;
        this.databaseService = databaseService;
    }


    @Override
    public void create() {
        Gdx.app.log(TAG_APP, "Create App");
        authService.signIn(new AuthService.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                // TODO something with userId
            }

            @Override
            public void onFailure(String error) {
                // TODO handle error
            }
        });

        // To draw
        batch = new SpriteBatch();

        // Load only once
        TextureManager.loadTextures();
        TextureManager.loadSkin();
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
        Gdx.app.log(TAG_APP, "Dispose App");

        batch.dispose();
        TextureManager.dispose();

        // TODO implement leaveLobby() if user is in a lobby
        databaseService.stopListening();
        authService.signOut();
    }
}
