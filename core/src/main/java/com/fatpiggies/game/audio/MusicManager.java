package com.fatpiggies.game.audio;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class MusicManager {

    private static Music music;
    private static boolean enabled = true;

    private MusicManager() {}

    public static void load() {
        if (music != null) return;

        music = Gdx.audio.newMusic(Gdx.files.internal("audio/musicLobby.mp3"));
        music.setLooping(true);
        music.setVolume(0.1f);

        if (enabled) {
            music.play();
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void enable() {
        enabled = true;
        music.play();
    }

    public static void disable() {
        enabled = false;
        music.stop();
    }

    public static void dispose() {
        if (music != null) {
            music.stop();
            music.dispose();
            music = null;
        }
    }
}
