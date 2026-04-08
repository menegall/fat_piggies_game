package com.fatpiggies.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SoundsManager {

    private static Sound buttonClick;
    private static Sound startGame;
    private static Sound endGame;
    private static Sound respawn;
    private static Sound walk;
    private static Sound error;

    private static boolean enabled = true;

    private SoundsManager() {}

    public static void load() {
        if (buttonClick == null){
            buttonClick = Gdx.audio.newSound(Gdx.files.internal("audio/button.wav"));
            buttonClick.play(0f);
        }

//        if (startGame == null)
//            startGame = Gdx.audio.newSound(Gdx.files.internal("start.wav"));
//
//        if (endGame == null)
//            endGame = Gdx.audio.newSound(Gdx.files.internal("end.wav"));

        if (respawn == null)
            respawn = Gdx.audio.newSound(Gdx.files.internal("audio/respawn.wav"));

        if (walk == null)
            walk = Gdx.audio.newSound(Gdx.files.internal("audio/walk.wav"));

        if (error == null)
            error = Gdx.audio.newSound(Gdx.files.internal("audio/error.wav"));
    }

    // --- PLAY ---

    public static void playButton(float volume) {
        if (enabled && buttonClick != null) buttonClick.play(volume);
    }

    public static void playStart(float volume) {
        if (enabled && startGame != null) startGame.play(volume);
    }

    public static void playEnd(float volume) {
        if (enabled && endGame != null) endGame.play(volume);
    }

    public static void playRespawn(float volume) {
        if (enabled && respawn != null) respawn.play(volume);
    }

    public static void playWalk(float volume) {
        if (enabled && walk != null) walk.play(volume);
    }

    public static void playError(float volume) {
        if (enabled && error != null) error.play(volume);
    }

    // --- SETTINGS ---

    public static boolean isEnabled() {
        return enabled;
    }

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    // --- CLEANUP ---

    public static void dispose() {
        if (buttonClick != null) buttonClick.dispose();
        if (startGame != null) startGame.dispose();
        if (endGame != null) endGame.dispose();
        if (respawn != null) respawn.dispose();
        if (walk != null) walk.dispose();
        if (error != null) error.dispose();
    }
}
