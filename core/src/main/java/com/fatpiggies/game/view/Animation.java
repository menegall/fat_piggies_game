package com.fatpiggies.game.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Animation {

    private final Array<TextureRegion> frames;
    private final float maxFrameTime;
    private float currentFrameTime;
    private final int frameCount;
    private int frame;

    // Grid animation
    public Animation(Texture texture, int rows, int cols, int includedFrames, float cycleTime) {

        frames = new Array<>();

        int frameWidth = texture.getWidth() / cols;
        int frameHeight = texture.getHeight() / rows;

        TextureRegion[][] tmp = TextureRegion.split(texture, frameWidth, frameHeight);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames.add(tmp[i][j]);
            }
        }

        this.frameCount = includedFrames;
        this.maxFrameTime = cycleTime / includedFrames;
        this.frame = 0;
    }

    public void update(float dt) {
        currentFrameTime += dt;

        if (currentFrameTime > maxFrameTime) {
            frame++;
            currentFrameTime = 0;
        }

        if (frame >= frameCount) {
            frame = 0;
        }
    }

    public TextureRegion getFrame() {
        return frames.get(frame);
    }

    public TextureRegion getLastFrame() {
        return frames.get(frames.size-1);
    }
}
