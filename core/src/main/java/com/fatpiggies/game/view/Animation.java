package com.fatpiggies.game.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Animation {

    private final Array<TextureRegion> frames;
    private final float maxFrameTime;
    private float currentFrameTime;

    private final int frameCount; // animation only
    private int frame;

    public Animation(Texture texture, int rows, int cols, int includedFrames, float cycleTime) {

        frames = new Array<>();

        int frameWidth = texture.getWidth() / cols;
        int frameHeight = texture.getHeight() / rows;

        TextureRegion[][] tmp = TextureRegion.split(texture, frameWidth, frameHeight);

        // KEEP ALL FRAMES
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames.add(tmp[i][j]);
            }
        }

        this.frameCount = includedFrames; // Only animated ones
        this.maxFrameTime = cycleTime / frameCount;
        this.frame = 0;
        this.currentFrameTime = 0f;
    }

    public void update(float dt) {
        currentFrameTime += dt;

        if (currentFrameTime >= maxFrameTime) {
            frame = (frame + 1) % frameCount; // Only animated frames
            currentFrameTime = 0;
        }
    }

    public TextureRegion getFrame() {
        return frames.get(frame);
    }

    public TextureRegion getFrame(int i) {
        if (i < 0 || i >= frames.size) {
            throw new IllegalArgumentException("Frame index out of bounds: " + i);
        }
        return frames.get(i);
    }

    public TextureRegion getLastFrame() {
        return frames.get(frames.size - 1);
    }
}
