package com.fatpiggies.game;


import static com.fatpiggies.game.utils.Config.TAG_APP;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.fatpiggies.game.controller.MainController;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.setting.MusicManager;
import com.fatpiggies.game.setting.PreferencesManager;
import com.fatpiggies.game.setting.SoundsManager;
import com.fatpiggies.game.view.TextureManager;
import com.fatpiggies.game.view.states.GameStateManager;


/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class FatPiggiesGame extends ApplicationAdapter {
    AuthService authService;
    DatabaseService databaseService;
    private SpriteBatch batch;
    private MainController main;

    BitmapFont font;

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
                Gdx.app.postRunnable(() -> {
                    main = new MainController(authService, databaseService);
                    main.getLobbyModel().setPlayerId(userId);
                });
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


        //PreferencesManager.reset();

        // To draw
        batch = new SpriteBatch();
        font = TextureManager.getSkin().getFont("default-font");

    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(Color.valueOf("EA6BEC"));

        // This should only appear on the first launch
        if (main == null){
            batch.begin();
            font.draw(batch, "Loading...", Gdx.graphics.getWidth()*0.43f, Gdx.graphics.getHeight()*0.5f);
            batch.end();
            return;
        }

        // Main loop
        main.update(batch, dt);
    }

    @Override
    public void resize(int width, int height) {
        if (main != null) main.resize(width, height);
    }


    @Override
    public void dispose() {
        Gdx.app.log(TAG_APP, "Dispose App");

        if (main != null) main.dispose();

        authService.signOut();

        if (batch != null) batch.dispose();

        TextureManager.dispose();
        SoundsManager.dispose();
        MusicManager.dispose();

        GameStateManager.getInstance().dispose();
    }

    @Override
    public void pause() {
        if (main != null) main.pause();
    }
}
