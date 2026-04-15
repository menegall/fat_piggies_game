package com.fatpiggies.game.view.states;

import static com.fatpiggies.game.model.utils.GameConstants.BOTTOM_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.JOYSTICK_DEADZONE;
import static com.fatpiggies.game.model.utils.GameConstants.LEFT_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.RIGHT_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.TOP_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.WORLD_HEIGHT;
import static com.fatpiggies.game.model.utils.GameConstants.WORLD_WIDTH;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.model.IReadOnlyGameWorld;
import com.fatpiggies.game.model.ecs.components.RenderComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.ColliderComponent;
import com.fatpiggies.game.model.utils.GameConstants;
import com.fatpiggies.game.setting.SoundsManager;
import com.fatpiggies.game.setting.VibrationManager;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

public class PlayState extends State {

    // ================= CONST =================
    private static final float JOYSTICK_SIZE = 0.15f;
    private static final float BACK_SIZE = 0.08f;
    private static final float BACK_MARGIN_X = 0.03f;
    private static final float BACK_MARGIN_Y = 0.03f;

    private static final float MESSAGE_X_RATIO = 0.4f;
    private static final float MESSAGE_Y_RATIO = 0.81f;
    private static final float MESSAGE_WIDTH_RATIO = 0.2f;
    private static final float MESSAGE_HEIGHT_RATIO = 0.05f;

    private static final int MAX_LIFE = 5;
    private static final float LIFE_WIDTH = 0.06f;
    private static final float LIFE_HEIGHT = 0.12f;
    private static final float LIFE_START_X = 0.05f;
    private static final float LIFE_SPACING = 0.07f;
    private static final float LIFE_Y = 0.8f;

    // ================= DATA =================
    private final ShapeRenderer shapeRenderer;
    private final IReadOnlyGameWorld gameWorld;
    private final boolean isHost;

    private final ImmutableArray<Entity> renderables;

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<RenderComponent> rm = ComponentMapper.getFor(RenderComponent.class);
    private final ComponentMapper<ColliderComponent> cm = ComponentMapper.getFor(ColliderComponent.class);

    // ================= UI =================
    private Touchpad touchpad;
    private Button backButton;
    private Label messageLabel;
    private InputMultiplexer multiplexer;
    private boolean joystickActive = false;
    private int joystickPointer = -1;
    private final TextureRegion bg;
    private final TextureId localTexture;

    public PlayState(IViewActions viewActions, IReadOnlyGameWorld gameWorld, String playerId, boolean isHost) {
        super(viewActions);
        shapeRenderer = new ShapeRenderer();
        this.gameWorld = gameWorld;
        this.isHost = isHost;

        Engine engine = gameWorld.getEngine();
        this.renderables = engine.getEntitiesFor(
            Family.all(TransformComponent.class, RenderComponent.class).get()
        );

        bg = TextureManager.getFrame(TextureId.PLAY_BACKGROUND);
        localTexture = gameWorld.getLocalPlayerTexture();

        createUI();
        setupInput();
    }

    // ================= UI =================
    private void createUI() {

        touchpad = new Touchpad(
            JOYSTICK_DEADZONE,
            skin.get("touchpad", Touchpad.TouchpadStyle.class)
        );

        float size = screenWidth * JOYSTICK_SIZE;
        touchpad.setSize(size, size);

        touchpad.setVisible(true);
        touchpad.setTouchable(Touchable.enabled);
        touchpad.getColor().a = 0f;
        touchpad.setResetOnTouchUp(true);

        stage.addActor(touchpad);

        if (isHost) {
            float s = screenWidth * BACK_SIZE;

            backButton = new Button(skin, "backButton");
            backButton.setSize(s, s);
            backButton.setPosition(
                screenWidth - s - screenWidth * BACK_MARGIN_X,
                screenHeight - s - screenHeight * BACK_MARGIN_Y
            );

            backButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    viewActions.onLobbyClicked();
                    VibrationManager.vibrate(200);
                    SoundsManager.playButton(1f);
                }
            });

            stage.addActor(backButton);
        }

        // ================= MESSAGE =================
        messageLabel = new Label("", skin);
        messageLabel.setColor(Color.YELLOW);
        messageLabel.setAlignment(Align.center);
        messageLabel.setVisible(false);
        messageLabel.setPosition(screenWidth * MESSAGE_X_RATIO, -screenHeight);
        messageLabel.setSize(
            screenWidth * MESSAGE_WIDTH_RATIO,
            screenHeight * MESSAGE_HEIGHT_RATIO
        );
        stage.addActor(messageLabel);
    }

    // ================= INPUT =================
    private void setupInput() {

        InputAdapter input = new InputAdapter() {

            @Override
            public boolean touchDown(int x, int y, int pointer, int button) {

                if (!gameWorld.isLocalPlayerAlive()) return false;
                Vector2 pos = screenToStage(x, y);

                Actor hit = stage.hit(pos.x, pos.y, true);
                if (hit != null && hit != touchpad) return false;

                if (joystickActive) return false;

                joystickActive = true;
                joystickPointer = pointer;

                touchpad.setPosition(
                    pos.x - touchpad.getWidth() / 2f,
                    pos.y - touchpad.getHeight() / 2f
                );

                touchpad.getColor().a = 1f;
                return false;
            }

            @Override
            public boolean touchUp(int x, int y, int pointer, int button) {

                if (pointer != joystickPointer) return false;

                joystickActive = false;
                joystickPointer = -1;

                touchpad.getColor().a = 0f;
                viewActions.onJoystickMoved(0f, 0f);

                return false;
            }
        };

        multiplexer = new InputMultiplexer(input, stage);
    }

    @Override
    public InputProcessor getInputProcessor() {
        return multiplexer;
    }

    private Vector2 screenToStage(int x, int y) {
        return stage.screenToStageCoordinates(new Vector2(x, y));
    }

    // ================= UPDATE =================
    @Override
    public void update(float dt) {

        boolean isAlive = gameWorld.isLocalPlayerAlive();

        if (!isAlive && joystickActive) {
            joystickActive = false;
            joystickPointer = -1;

            touchpad.getColor().a = 0f;
            viewActions.onJoystickMoved(0f, 0f);
        }

        if (joystickActive && isAlive) {
            viewActions.onJoystickMoved(
                touchpad.getKnobPercentX(),
                touchpad.getKnobPercentY()
            );
        }

        stage.act(dt);
    }

    // ================= RENDER =================
    @Override
    public void render(SpriteBatch sb) {

        // BG
        sb.begin();
        sb.draw(TextureManager.getFrame(TextureId.PLAY_BACKGROUND), 0, 0, screenWidth, screenHeight);
        sb.end();

        // ENTITIES
        sb.begin();

        float sx = screenWidth / WORLD_WIDTH;
        float sy = screenHeight / WORLD_HEIGHT;

        for (Entity e : renderables) {
            TransformComponent t = tm.get(e);
            RenderComponent r = rm.get(e);

            sb.draw(
                TextureManager.getFrame(r.textureId),
                (t.x - r.width / 2f) * sx,
                (t.y - r.height / 2f) * sy,
                (r.width / 2f) * sx,
                (r.height / 2f) * sy,
                r.width * sx,
                r.height * sy,
                1f, 1f,
                t.angle + r.angleOffset
            );
        }

        sb.end();

        // UI LIFE
        sb.begin();

        int life = gameWorld.getLocalPlayerLife();
        boolean isAlive = gameWorld.isLocalPlayerAlive();

        if (!isAlive) {
            sb.setColor(0f, 0f, 0f, 0.5f);
            sb.draw(bg, 0, 0, screenWidth, screenHeight);
            sb.setColor(1f, 1f, 1f, 1f);
        }

        float w = screenWidth * LIFE_WIDTH;
        float h = screenHeight * LIFE_HEIGHT;

        for (int i = 1; i <= MAX_LIFE; i++) {

            boolean heartAlive = i <= life;

            TextureRegion frame = heartAlive
                ? TextureManager.getFrame(TextureManager.getLifeTextureId(localTexture))
                : TextureManager.getFrame(TextureManager.getLifeTextureId(localTexture), 3);

            if (!heartAlive) sb.setColor(0.3f, 0.3f, 0.3f, 1f);

            sb.draw(
                frame,
                screenWidth * LIFE_START_X + i * screenWidth * LIFE_SPACING,
                screenHeight * LIFE_Y,
                w, h
            );

            sb.setColor(1f, 1f, 1f, 1f);
        }
        sb.end();

        // ================= DEBUG =================
        if (GameConstants.DEBUG) {
            shapeRenderer.setProjectionMatrix(sb.getProjectionMatrix());

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(
                LEFT_BOUND * sx,
                BOTTOM_BOUND * sy,
                (RIGHT_BOUND - LEFT_BOUND) * sx,
                (TOP_BOUND - BOTTOM_BOUND) * sy
            );

            for (Entity e : renderables) {
                TransformComponent t = tm.get(e);
                ColliderComponent c = cm.get(e);

                if (t != null && c != null) {
                    if (e == gameWorld.getLocalPlayer()) {
                        shapeRenderer.setColor(Color.GREEN);
                    } else {
                        shapeRenderer.setColor(Color.YELLOW);
                    }

                    float radiusX = c.radius * sx;
                    float radiusY = c.radius * sy;

                    float drawX = (t.x * sx) - radiusX;
                    float drawY = (t.y * sy) - radiusY;

                    shapeRenderer.ellipse(
                        drawX,
                        drawY,
                        radiusX * 2f,
                        radiusY * 2f
                    );
                }
            }
            shapeRenderer.end();
        }

        stage.draw();
    }

    @Override
    public void showMessage(String message) {
        SoundsManager.playError(3f);

        messageLabel.clearActions();
        messageLabel.setText(message);
        messageLabel.setVisible(true);

        float targetY = screenHeight * MESSAGE_Y_RATIO;

        messageLabel.addAction(Actions.sequence(
            Actions.moveTo(screenWidth * MESSAGE_X_RATIO, targetY, 0.3f, Interpolation.swingIn),
            Actions.delay(2f),
            Actions.moveTo(screenWidth * MESSAGE_X_RATIO, -screenHeight, 0.1f),
            Actions.run(() -> messageLabel.setVisible(false))
        ));
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
    }
}
