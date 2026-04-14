package com.fatpiggies.game;


import static com.fatpiggies.game.utils.Config.TAG_APP;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.fatpiggies.game.controller.MainController;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.setting.MusicManager;
import com.fatpiggies.game.setting.SoundsManager;
import com.fatpiggies.game.view.TextureManager;


/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class FatPiggiesGame extends ApplicationAdapter {
    AuthService authService;
    DatabaseService databaseService;
    private SpriteBatch batch;
    private MainController main;

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
            }

            @Override
            public void onFailure(String error) {
            }
        });

        // Load only once
        TextureManager.loadTextures();
        TextureManager.loadSkin();
        SoundsManager.load();
        MusicManager.load();

        MusicManager.enable();

        // To draw
        batch = new SpriteBatch();
        main = new MainController(authService, databaseService);

    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // Main loop
        main.update(batch, dt);
    }

    @Override
    public void resize(int width, int height) {
        main.resize(width, height);
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

    @Override
    public void pause() {
        main.pause();
    }
}
