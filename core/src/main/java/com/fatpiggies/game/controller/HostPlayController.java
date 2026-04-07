package com.fatpiggies.game.controller;

import com.fatpiggies.game.model.GameWorld;
import com.fatpiggies.game.network.dto.GameState;

public class HostPlayController implements IPlayController {
    private MainController main;

    public HostPlayController(MainController main) {
        this.main = main;
    }

    @Override
    public void startGame(String lobbyId) {
        // TODO specify information that is available here
        main.world = new GameWorld();

        main.dbs.startGame(lobbyId);
        main.dbs.pushGameState(lobbyId, new GameState());
        // create entities in gameworld

        // TODO change positions when view is implemented
        main.world.createHostPig(main.auth.getCurrentUserId(), textureIds.get(0),0,0);
        for(int i = 0; i < playerIds.size(); i++) {
            main.world.createRemotePig(playerIds.get(i), textureIds.get(i+1), 0,0);
        }

        main.gsm.setPlayState(main);
    }

    @Override
    public void endGame(String lobbyId) {
        main.dbs.endGame(lobbyId);
        // TODO: determine winner
        main.gsm.setOverState(main, true); // it is the host controller
        main.world = null;

    }

    @Override
    public void movePig(double x, double y) {

    }

}
