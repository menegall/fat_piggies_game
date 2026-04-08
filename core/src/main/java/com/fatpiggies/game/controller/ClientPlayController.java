package com.fatpiggies.game.controller;

import static com.fatpiggies.game.assets.TextureId.BLUE_PIG;
import static com.fatpiggies.game.assets.TextureId.GREEN_PIG;
import static com.fatpiggies.game.assets.TextureId.RED_PIG;
import static com.fatpiggies.game.assets.TextureId.YELLOW_PIG;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.ecs.systems.move.MovementSystem;
import com.fatpiggies.game.model.ecs.systems.move.NetworkLerpSystem;
import com.fatpiggies.game.model.ecs.systems.move.NetworkReconciliationSystem;
import com.fatpiggies.game.assets.TextureId;


public class ClientPlayController implements IPlayController{
    private MainController main;
    private Engine engine;
    private GameWorld world;

    public ClientPlayController(MainController main, String lobbyId) {
        this.main = main;

        engine = new Engine();

        engine.addSystem(new NetworkReconciliationSystem());
        engine.addSystem(new NetworkLerpSystem());
        engine.addSystem(new MovementSystem());

        world = new GameWorld(engine);
    }

    @Override
    public void startGame(String lobbyId) {
        TextureId[] textures = {BLUE_PIG, GREEN_PIG, RED_PIG,YELLOW_PIG};
        int count = 0;
        world.createLocalPig(main.auth.getCurrentUserId(), textures[count++]);
        for (String playerId : main.lobbyModel.getPlayersSetup().keySet()) {
            if(!playerId.equals(main.auth.getCurrentUserId()))  {
                if (count < textures.length) {
                    world.createRemotePig(playerId, textures[count++]);
                }
            }
        }

        main.gsm.setPlayState(main, world);
        main.setGameIsPlaying(true);
    }

    @Override
    public void endGame(String lobbyId) {
        main.setGameIsPlaying(false);
        main.gsm.setOverState(main, main.lobbyModel, false); // it is the client controller
        // Clean up the Ashley engine and World
        world.cleanUpWorld();
        engine = null;
        world = null;
        // Destroy this controller
        main.playController = null;
    }

    @Override
    public void updateWorld(float dt) {
        world.update(dt);
    }

    @Override
    public void updatePlayerInput(float x, float y) {
        world.updatePlayerInput(x, y);
    }

    private void showErrorInMainThread(String message) {
        Gdx.app.postRunnable(() -> main.gsm.showError(message));
    }
}
