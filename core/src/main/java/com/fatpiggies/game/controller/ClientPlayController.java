package com.fatpiggies.game.controller;

import static com.fatpiggies.game.view.TextureId.BLUE_PIG;
import static com.fatpiggies.game.view.TextureId.GREEN_PIG;
import static com.fatpiggies.game.view.TextureId.RED_PIG;
import static com.fatpiggies.game.view.TextureId.YELLOW_PIG;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.ecs.systems.move.MovementSystem;
import com.fatpiggies.game.model.ecs.systems.move.NetworkLerpSystem;
import com.fatpiggies.game.model.ecs.systems.move.NetworkReconciliationSystem;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.view.TextureId;

public class ClientPlayController implements IPlayController{
    private MainController main;
    private Engine engine;

    public ClientPlayController(MainController main) {
        this.main = main;

        main.dbs.listenToLobbyStatus(main.world.getLobbyId(), new DatabaseService.LobbyStatusCallback() {
            @Override
            public void onStatusUpdated(String status) {
                if("playing".equals(status)) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            startGame(main.world.getLobbyId());
                            main.gsm.setPlayState(main, main.world);
                        }
                    });
                }
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {
                // TODO: on error leave lobby?
            }
        });

        engine = new Engine();
        // add all systems for client
        engine.addSystem(new NetworkReconciliationSystem());
        engine.addSystem(new NetworkLerpSystem());
        engine.addSystem(new MovementSystem());

        main.world = new GameWorld(engine);
    }

    @Override
    public void startGame(String lobbyId) {
        TextureId[] textures = {BLUE_PIG, GREEN_PIG, RED_PIG,YELLOW_PIG};
        int count = 0;
        main.world.createLocalPig(main.auth.getCurrentUserId(), textures[count++], 0, 0);
        for (String playerId : main.world.getPlayersSetup().keySet()) {
            if(!playerId.equals(main.auth.getCurrentUserId()))  {
                if (count < textures.length) {
                    main.world.createRemotePig(playerId, textures[count++], 0, 0);
                }
            }
        }
        main.gsm.setPlayState(main, main.world);
    }

    @Override
    public void endGame(String lobbyId) {
        main.gsm.setOverState(main, main.world, false); // it is the client controller
    }

    @Override
    public void updatePlayerInput(float x, float y) {
        main.world.updatePlayerInput(x, y);
    }
}
