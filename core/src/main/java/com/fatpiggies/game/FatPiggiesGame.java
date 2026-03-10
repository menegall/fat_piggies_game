package com.fatpiggies.game;


import static com.fatpiggies.game.utils.Config.TAG_APP;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.fatpiggies.game.network.AuthService;
import com.fatpiggies.game.network.DatabaseService;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class FatPiggiesGame extends ApplicationAdapter {
    AuthService authService;
    DatabaseService databaseService;
    private SpriteBatch batch;
    private Texture image;

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
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();
    }

    @Override
    public void dispose() {
        Gdx.app.log(TAG_APP, "Dispose App");
        batch.dispose();
        image.dispose();
        databaseService.stopListening();
        authService.signOut();
    }
}
