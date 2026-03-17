package com.fatpiggies.game.controller;

public class PlayController implements IPlayController {
    private MainController main;

    public PlayController(MainController main) {
        this.main = main;
    }

    public void movePig(int x, int y) {
        // TODO: call movementsystem in ECS
        // main.world.movementsystem...
        // db.movePig??
    }

    public void dash() {
        // TODO: make pig dash forward (low priority)
    }
}
