package com.fatpiggies.game.view.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.fatpiggies.game.model.Snapshot;
import com.fatpiggies.game.view.Animation;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

public class PlayState extends State {

    private final Stage stage;
    private final Skin skin;

    private final float screenWidth = Gdx.graphics.getWidth();
    private final float screenHeight = Gdx.graphics.getHeight();

    private Touchpad touchpad;
    private final Texture playBackground;

    private final float DEADZONE = 10;

    private Animation life;

    public PlayState() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = TextureManager.getSkin();
        playBackground = TextureManager.getTexture(TextureId.PLAY_BACKGROUND);
        life = new Animation(TextureManager.getTexture(TextureId.LIFE_BLUE_PIG), 2, 2, 3, 2f);

        createUI();
    }

    private void createUI() {
        touchpad = new Touchpad(DEADZONE, skin.get("touchpad", Touchpad.TouchpadStyle.class));

        // Position
        touchpad.setBounds(screenWidth*0.125f, screenHeight*0.1f, screenWidth*0.15f, screenWidth*0.15f);

        stage.addActor(touchpad);
    }


    // FOR NOW WITHOUT CONTROLLER

    @Override
    public void update(Snapshot snapshot, float dt){

        // JOYSTICK INPUT
        float joyX = touchpad.getKnobPercentX();
        float joyY = touchpad.getKnobPercentY();
        float joyPower = (float)Math.sqrt(joyX * joyX + joyY * joyY);

        // Deadzone
        if (joyPower < DEADZONE/100f) {
            joyX = 0;
            joyY = 0;
            joyPower = 0;
        }

        if (joyPower > 0.01f) {
            System.out.println(
                "Dir: (" + String.format("%.2f", joyX) + ", " +
                    String.format("%.2f", joyY) + ") | Power: " +
                    String.format("%.2f", joyPower)
            );
        }


        stage.act(dt); // update UI

        life.update(dt); // for the animation
    }

    @Override
    public void render(SpriteBatch sb) {

        // Draw world
        sb.begin();
        //float size = Math.min(screenWidth, screenHeight) * 0.7f;

        sb.draw(playBackground, 0, 0, screenWidth, screenHeight);
        sb.end();

        // Interface
        sb.begin();

        // FOR NOW LETS SAY THERE IS 3 LIFE REMAINING
        int remainingLife = 3;

        // Draw the right number of life
        float scale = 4f;
        for (int i = 1; i <= 5; i++) {

            boolean isAlive = i <= remainingLife;

            TextureRegion frame = isAlive
                ? life.getFrame()
                : life.getLastFrame();

            float width = frame.getRegionWidth() * scale;
            float height = frame.getRegionHeight() * scale;

            if (!isAlive) {
                sb.setColor(0.3f, 0.3f, 0.3f, 1f);
            } else {
                sb.setColor(1f, 1f, 1f, 1f);
            }

            sb.draw(frame,
                screenWidth*0.05f + i * screenWidth*0.07f,
                screenHeight*0.8f,
                width,
                height
            );

            // Reset
            sb.setColor(1f, 1f, 1f, 1f);
        }
        sb.end();

        stage.draw();  // Draw UI
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
