package com.fatpiggies.game.controller;

import static com.fatpiggies.game.view.TextureId.BLUE_PIG;
import static com.fatpiggies.game.view.TextureId.GREEN_PIG;
import static com.fatpiggies.game.view.TextureId.RED_PIG;
import static com.fatpiggies.game.view.TextureId.YELLOW_PIG;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Array;
import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.model.ecs.systems.move.MovementSystem;
import com.fatpiggies.game.model.ecs.systems.move.NetworkLerpSystem;
import com.fatpiggies.game.model.ecs.systems.move.NetworkReconciliationSystem;
import com.fatpiggies.game.network.DatabaseService;
import com.fatpiggies.game.network.NetworkError;
import com.fatpiggies.game.view.TextureId;

import java.util.ArrayList;

public class ClientPlayController implements IPlayController{
    private MainController main;
    private Engine engine;

    public ClientPlayController(MainController main) {
        this.main = main;

        main.dbs.listenToLobbyStatus(main.world.lobbyId, new DatabaseService.LobbyStatusCallback() {
            @Override
            public void onStatusUpdated(String status) {
                if("playing".equals(status)) {
                    startGame(main.world.lobbyId);
                    main.gsm.setPlayState(main);
                }
            }

            @Override
            public void onError(NetworkError error, String errorMessage) {

            }
        });

        engine = new PooledEngine();
        // add all systems for client
        engine.addSystem(new MovementSystem());
        engine.addSystem(new NetworkLerpSystem());
        engine.addSystem(new NetworkReconciliationSystem());

        main.world = new GameWorld(engine);
    }

    @Override
    public void startGame(String lobbyId) {
        TextureId[] textures = {BLUE_PIG, GREEN_PIG, RED_PIG,YELLOW_PIG};
        int count = 0;
        main.world.createLocalPig(main.auth.getCurrentUserId(), textures[count++], 0, 0);
        for (String playerId : main.world.playersSetup.keySet()) {
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
