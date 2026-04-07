package com.fatpiggies.game.controller;

public class ClientPlayController implements IPlayController{
    private MainController main;
    private Engine engine;

    public ClientPlayController(MainController main) {
        this.main = main;
        engine = new PooledEngine();
        // add all systems for client
        engine.addSystem(new MovementSystem());
        engine.addSystem(new NetworkLerpSystem());
        engine.addSystem(new NetworkReconciliationSystem());
    }

    @Override
    public void startGame(String lobbyId) {
        main.world = new GameWorld(new PooledEngine());
        main.world.createLocalPig(playerIds.get(0), textureIds.get(0),0,0);
        for(int i = 1; i < playerIds.size(); i++) {
            main.world.createRemotePig(playerIds.get(i), textureIds.get(i), 0,0);
        }
        main.gsm.setPlayState(main);
    }

    @Override
    public void endGame(String lobbyId) {
        main.gsm.setOverState(main, false); // it is the client controller
    }

    @Override
    public void updatePlayerInput(int x, int y) {
        main.world.updatePlayerInput(x,y);
    }
}
