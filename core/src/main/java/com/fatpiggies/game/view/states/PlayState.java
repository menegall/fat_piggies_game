package com.fatpiggies.game.view.states;

import static com.fatpiggies.game.model.utils.GameConstants.JOYSTICK_DEADZONE;
import static com.fatpiggies.game.model.utils.GameConstants.WORLD_HEIGHT;
import static com.fatpiggies.game.model.utils.GameConstants.WORLD_WIDTH;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.model.IReadOnlyGameWorld;
import com.fatpiggies.game.model.ecs.components.HealthComponent;
import com.fatpiggies.game.model.ecs.components.RenderComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkIdentityComponent;
import com.fatpiggies.game.model.utils.GameConstants;
import com.fatpiggies.game.view.Animation;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;
import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;

public class PlayState extends State {
    private Touchpad touchpad;
    private final TextureRegion playBackground;

    private final IReadOnlyGameWorld gameWorld;
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<RenderComponent> rm = ComponentMapper.getFor(RenderComponent.class);
    private final ComponentMapper<NetworkIdentityComponent> nm = ComponentMapper.getFor(NetworkIdentityComponent.class);
    private final ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);

    private final ImmutableArray<Entity> renderableEntities;
    private final ImmutableArray<Entity> networkEntities;
    private final Engine engine;
    private final String playerId;
    private int remainingLife = 5;

    public PlayState(IViewActions viewActions, IReadOnlyGameWorld gameWorld, String playerId) {
        super(viewActions);
        this.gameWorld = gameWorld;
        this.playerId = playerId;

        playBackground = TextureManager.getFrame(TextureId.PLAY_BACKGROUND);

        this.engine = gameWorld.getEngine();
        this.renderableEntities = engine.getEntitiesFor(Family.all(TransformComponent.class, RenderComponent.class).get());
        this.networkEntities = engine.getEntitiesFor(Family.all(NetworkIdentityComponent.class).get());
        createUI();
    }

    private void createUI() {
        touchpad = new Touchpad(JOYSTICK_DEADZONE, skin.get("touchpad", Touchpad.TouchpadStyle.class));

        // Position
        touchpad.setBounds(screenWidth*0.125f, screenHeight*0.1f, screenWidth*0.15f, screenWidth*0.15f);

        stage.addActor(touchpad);
    }


    // FOR NOW WITHOUT CONTROLLER

    @Override
    public void update(float dt){

        // JOYSTICK INPUT
        float joyX = touchpad.getKnobPercentX();
        float joyY = touchpad.getKnobPercentY();
        float joyPower = (float)Math.sqrt(joyX * joyX + joyY * joyY);

        if (joyPower > 0.01f) {
            viewActions.onJoystickMoved(joyX, joyY);
        }

        stage.act(dt); // update UI
    }

    @Override
    public void render(SpriteBatch sb) {

        // BACKGROUND
        sb.begin();
        sb.draw(playBackground, 0, 0, screenWidth, screenHeight);
        sb.end();

        // ECS RENDER
        sb.begin();

        float scaleX = screenWidth / GameConstants.WORLD_HEIGHT;
        float scaleY = screenHeight / GameConstants.WORLD_HEIGHT;

        for (int i = 0; i < renderableEntities.size(); ++i) {
            Entity entity = renderableEntities.get(i);

            TransformComponent transformComp = tm.get(entity);
            RenderComponent renderComp = rm.get(entity);

            float drawX = (transformComp.x - renderComp.width / 2f) * scaleX;
            float drawY = (transformComp.y - renderComp.height / 2f) * scaleY;

            sb.draw(
                TextureManager.getFrame(renderComp.textureId),

                (transformComp.x - renderComp.width / 2f) * scaleX,
                (transformComp.y - renderComp.height / 2f) * scaleY,

                (renderComp.width / 2f) * scaleX,
                (renderComp.height / 2f) * scaleY,

                renderComp.width * scaleX,
                renderComp.height * scaleY,

                // Scale
                1f, 1f,

                // Rotation
                transformComp.angle + renderComp.angleOffset
            );
        }

        sb.end();

        // UI (life)
        sb.begin();
        for (int i = 0; i < networkEntities.size(); ++i) {
            Entity entity = networkEntities.get(i);
            NetworkIdentityComponent networkId = nm.get(entity);
            if(networkId.playerId.equals(playerId)){
                remainingLife = hm.get(entity).currentLife;
            }
        }

        for (int i = 1; i <= 5; i++) {

            boolean isAlive = i <= remainingLife;

            TextureRegion frame = isAlive
                ? TextureManager.getFrame(TextureId.LIFE_BLUE_PIG)
                : TextureManager.getFrame(TextureId.LIFE_BLUE_PIG, 3);

            float width = screenWidth*0.06f;
            float height = screenHeight*0.12f;

            if (!isAlive) {
                sb.setColor(0.3f, 0.3f, 0.3f, 1f);
            }

            sb.draw(frame,
                screenWidth*0.05f + i * screenWidth*0.07f,
                screenHeight*0.8f,
                width,
                height
            );

            sb.setColor(1f, 1f, 1f, 1f);
        }

        sb.end();

        // UI Scene2D
        stage.draw();
    }
}
