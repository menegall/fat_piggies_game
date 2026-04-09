package com.fatpiggies.game.view.states;

import static com.fatpiggies.game.model.utils.GameConstants.JOYSTICK_DEADZONE;
import static com.fatpiggies.game.model.utils.GameConstants.WORLD_HEIGHT;
import static com.fatpiggies.game.model.utils.GameConstants.WORLD_WIDTH;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.model.IReadOnlyGameWorld;
import com.fatpiggies.game.model.ecs.components.*;
import com.fatpiggies.game.model.ecs.components.network.NetworkIdentityComponent;
import com.fatpiggies.game.setting.SoundsManager;
import com.fatpiggies.game.setting.VibrationManager;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;
import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;

public class PlayState extends State {

    // ================= CONSTANTS =================
    private static final float JOYSTICK_X = 0.125f;
    private static final float JOYSTICK_Y = 0.1f;
    private static final float JOYSTICK_SIZE = 0.15f;

    private static final float BACK_SIZE = 0.08f;
    private static final float BACK_MARGIN_X = 0.03f;
    private static final float BACK_MARGIN_Y = 0.03f;

    private static final float INPUT_THRESHOLD = 0.01f;

    private static final int MAX_LIFE = 5;

    private static final float LIFE_WIDTH = 0.06f;
    private static final float LIFE_HEIGHT = 0.12f;
    private static final float LIFE_START_X = 0.05f;
    private static final float LIFE_SPACING = 0.07f;
    private static final float LIFE_Y = 0.8f;

    // ================= DATA =================
    private final IReadOnlyGameWorld gameWorld;
    private final boolean isHost;
    private final String playerId;

    private final ImmutableArray<Entity> renderableEntities;
    private final ImmutableArray<Entity> networkEntities;

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<RenderComponent> rm = ComponentMapper.getFor(RenderComponent.class);
    private final ComponentMapper<NetworkIdentityComponent> nm = ComponentMapper.getFor(NetworkIdentityComponent.class);
    private final ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);

    // ================= UI =================
    private Touchpad touchpad;
    private Button backButton;
    private int remainingLife = MAX_LIFE;

    private final TextureRegion playBackground;

    public PlayState(IViewActions viewActions, IReadOnlyGameWorld gameWorld, String playerId, boolean isHost) {
        super(viewActions);

        this.gameWorld = gameWorld;
        this.playerId = playerId;
        this.isHost = isHost;

        Engine engine = gameWorld.getEngine();

        this.renderableEntities = engine.getEntitiesFor(
            Family.all(TransformComponent.class, RenderComponent.class).get()
        );

        this.networkEntities = engine.getEntitiesFor(
            Family.all(NetworkIdentityComponent.class).get()
        );

        playBackground = TextureManager.getFrame(TextureId.PLAY_BACKGROUND);

        createUI();
    }

    private void createUI() {
        // Joystick
        touchpad = new Touchpad(
            JOYSTICK_DEADZONE,
            skin.get("touchpad", Touchpad.TouchpadStyle.class)
        );

        float size = screenWidth * JOYSTICK_SIZE;

        touchpad.setBounds(
            screenWidth * JOYSTICK_X,
            screenHeight * JOYSTICK_Y,
            size,
            size
        );

        stage.addActor(touchpad);

        // Back button
        float backSize = screenWidth * BACK_SIZE;

        float x = screenWidth - backSize - screenWidth * BACK_MARGIN_X;
        float y = screenHeight - backSize - screenHeight * BACK_MARGIN_Y;

        backButton = new Button(skin, "backButton");
        backButton.setSize(backSize, backSize);
        backButton.setPosition(x, y);

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                viewActions.onLobbyClicked();
                VibrationManager.vibrate(200);
                SoundsManager.playButton(1f);
            }
        });

        if(isHost) stage.addActor(backButton);
    }

    // ================= UPDATE =================
    @Override
    public void update(float dt) {

        float joyX = touchpad.getKnobPercentX();
        float joyY = touchpad.getKnobPercentY();

        float power = (float) Math.sqrt(joyX * joyX + joyY * joyY);

        if (power > INPUT_THRESHOLD) {
            viewActions.onJoystickMoved(joyX, joyY);
        }

        stage.act(dt);
    }

    // ================= RENDER =================
    @Override
    public void render(SpriteBatch sb) {

        // BACKGROUND
        sb.begin();
        sb.draw(playBackground, 0, 0, screenWidth, screenHeight);
        sb.end();

        // ================= ECS DRAW (READ ONLY) =================
        sb.begin();

        float scaleX = screenWidth / WORLD_WIDTH;
        float scaleY = screenHeight / WORLD_HEIGHT;

        for (int i = 0; i < renderableEntities.size(); ++i) {
            Entity e = renderableEntities.get(i);

            TransformComponent t = tm.get(e);
            RenderComponent r = rm.get(e);

            sb.draw(
                TextureManager.getFrame(r.textureId),

                (t.x - r.width / 2f) * scaleX,
                (t.y - r.height / 2f) * scaleY,

                (r.width / 2f) * scaleX,
                (r.height / 2f) * scaleY,

                r.width * scaleX,
                r.height * scaleY,

                1f, 1f,
                t.angle + r.angleOffset
            );
        }

        sb.end();

        // ================= LIFE UI =================
        sb.begin();

        for (int i = 0; i < networkEntities.size(); ++i) {
            Entity e = networkEntities.get(i);
            if (nm.get(e).playerId.equals(playerId)) {
                remainingLife = hm.get(e).currentLife;
            }
        }

        float width = screenWidth * LIFE_WIDTH;
        float height = screenHeight * LIFE_HEIGHT;

        for (int i = 1; i <= MAX_LIFE; i++) {

            boolean alive = i <= remainingLife;

            TextureRegion frame = alive
                ? TextureManager.getFrame(TextureId.LIFE_BLUE_PIG)
                : TextureManager.getFrame(TextureId.LIFE_BLUE_PIG, 3);

            if (!alive) {
                sb.setColor(0.3f, 0.3f, 0.3f, 1f);
            }

            sb.draw(
                frame,
                screenWidth * LIFE_START_X + i * screenWidth * LIFE_SPACING,
                screenHeight * LIFE_Y,
                width,
                height
            );

            sb.setColor(1f, 1f, 1f, 1f);
        }

        sb.end();

        // ================= UI =================
        stage.draw();
    }
}
