package com.fatpiggies.game.view.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.setting.PreferencesManager;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;
import com.fatpiggies.game.view.Theme;

public class ShopState extends State {

    // ===== BUTTON =====
    private static final float BUTTON_WIDTH_RATIO = 0.22f;
    private static final float BUTTON_HEIGHT_RATIO = 0.12f;

    // ===== MENU BUTTON =====
    private static final float MENU_X_RATIO = 0.71f;
    private static final float MENU_Y_RATIO = 0.8f;

    // ===== PANELS =====
    private static final float PANEL_WIDTH_RATIO = 0.22f;
    private static final float PANEL_HEIGHT_RATIO = 0.12f;

    private static final float PANEL_1_X_RATIO = 0.07f;
    private static final float PANEL_1_Y_RATIO = 0.8f;

    private static final float PANEL_2_X_RATIO = 0.07f;
    private static final float PANEL_2_Y_RATIO = 0.65f;

    // ===== COINS =====
    private static final float COINS_X_RATIO = 0.09f;
    private static final float COINS_Y_RATIO = 0.86f;

    // ===== PRICE =====
    private static final float PRICE_X_RATIO = 0.09f;
    private static final float PRICE_Y_RATIO = 0.71f;

    // ===== BUY =====
    private static final float BUY_X_RATIO = 0.71f;
    private static final float BUY_Y_RATIO = 0.65f;

    // ===== ARROW =====
    private static final float ARROW_SIZE_X_RATIO = 0.11f;
    private static final float ARROW_SIZE_Y_RATIO = 0.26f;
    private static final float ARROW_X_RATIO = 0.73f;
    private static final float ARROW_Y_RATIO = 0.2f;

    // ===== PIG PREVIEW =====
    private static final float PIG_X_RATIO = 0.4f;
    private static final float PIG_Y_RATIO = 0.3f;
    private static final float PIG_WIDTH_RATIO = 0.2f;
    private static final float PIG_HEIGHT_RATIO = 0.3f;

    // ===== UI =====
    private TextButton menuButton;
    private TextButton buyButton;
    private ImageButton nextButton;

    private Label coinsLabel;
    private Label priceLabel;

    private TextureRegion menuBackground;

    // ===== THEME =====
    private Theme currentPreview;

    public ShopState(IViewActions viewActions) {
        super(viewActions);

        menuBackground = TextureManager.getFrame(TextureId.MENU_BACKGROUND);

        currentPreview = PreferencesManager.loadTheme();
        TextureManager.setPreviewTheme(currentPreview);

        createUI();
    }

    private void createUI() {

        float btnWidth = screenWidth * BUTTON_WIDTH_RATIO;
        float btnHeight = screenHeight * BUTTON_HEIGHT_RATIO;

        // ===== MENU BUTTON =====
        menuButton = new TextButton("Menu", skin);
        menuButton.setSize(btnWidth, btnHeight);
        menuButton.setPosition(
            screenWidth * MENU_X_RATIO,
            screenHeight * MENU_Y_RATIO
        );

        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                TextureManager.clearPreviewTheme();
                viewActions.onMenuClicked();
            }
        });
        stage.addActor(menuButton);

        // ===== COINS =====
        coinsLabel = new Label("", skin);
        coinsLabel.setPosition(
            screenWidth * COINS_X_RATIO,
            screenHeight * COINS_Y_RATIO
        );
        stage.addActor(coinsLabel);

        // ===== PRICE =====
        priceLabel = new Label("", skin);
        priceLabel.setPosition(
            screenWidth * PRICE_X_RATIO,
            screenHeight * PRICE_Y_RATIO
        );
        stage.addActor(priceLabel);

        // ===== BUY BUTTON =====
        buyButton = new TextButton("Buy", skin);
        buyButton.setSize(btnWidth, btnHeight);
        buyButton.setPosition(
            screenWidth * BUY_X_RATIO,
            screenHeight * BUY_Y_RATIO
        );

        buyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                viewActions.onBuyThemeClicked(currentPreview);

                updateAll();
            }
        });
        stage.addActor(buyButton);

        // ===== NEXT BUTTON =====
        nextButton = new ImageButton(new TextureRegionDrawable(TextureManager.getFrame(TextureId.ARROW)));
        nextButton.setSize(screenWidth * ARROW_SIZE_X_RATIO, screenHeight * ARROW_SIZE_Y_RATIO);
        nextButton.setPosition(
            screenWidth * ARROW_X_RATIO,
            screenHeight * ARROW_Y_RATIO
        );
        nextButton.getImageCell().expand().fill();

        nextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentPreview = nextTheme(currentPreview);
                TextureManager.setPreviewTheme(currentPreview);

                viewActions.onSelectTheme(currentPreview);

                updateAll();
            }
        });

        stage.addActor(nextButton);

        updateAll();
    }

    // ===== THEME LOOP =====
    private Theme nextTheme(Theme current) {
        Theme[] values = Theme.values();
        return values[(current.ordinal() + 1) % values.length];
    }

    // ===== UPDATE =====
    private void updateAll() {
        updateCoinsLabel();
        updatePriceLabel();
        updateBuyButton();
    }

    private void updateCoinsLabel() {
        coinsLabel.setText("Coins : " + viewActions.getCoins());
    }

    private void updatePriceLabel() {
        priceLabel.setText("Price : " + viewActions.getThemePrice(currentPreview));
    }

    private void updateBuyButton() {

        boolean unlocked = viewActions.isThemeUnlocked(currentPreview);
        int coins = viewActions.getCoins();
        int price = viewActions.getThemePrice(currentPreview);

        buyButton.setVisible(!unlocked);
        priceLabel.setVisible(!unlocked);

        if (unlocked) {
            buyButton.setText("Owned");
            buyButton.setDisabled(true);
            return;
        }

        if (coins < price) {
            buyButton.setText("Not enough!");
            buyButton.setDisabled(true);
        } else {
            buyButton.setText("Buy");
            buyButton.setDisabled(false);
        }
    }

    // ===== LOOP =====
    @Override
    public void update(float dt) {
        stage.act(dt);
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.begin();

        // ===== BACKGROUND =====
        sb.draw(
            TextureManager.getFrame(TextureId.PLAY_BACKGROUND),
            0, 0,
            screenWidth, screenHeight
        );

        // ===== PIG PREVIEW =====
        sb.draw(
            TextureManager.getFrame(TextureId.BLUE_PIG),
            screenWidth * PIG_X_RATIO,
            screenHeight * PIG_Y_RATIO,
            screenWidth * PIG_WIDTH_RATIO,
            screenHeight * PIG_HEIGHT_RATIO
        );

        // ===== PANEL 1 =====
        float sizeX = screenWidth * PANEL_WIDTH_RATIO;
        float sizeY = screenHeight * PANEL_HEIGHT_RATIO;

        float x = screenWidth * PANEL_1_X_RATIO;
        float y = screenHeight * PANEL_1_Y_RATIO;

        sb.draw(menuBackground, x, y, sizeX, sizeY);

        // ===== PANEL 2 =====
        x = screenWidth * PANEL_2_X_RATIO;
        y = screenHeight * PANEL_2_Y_RATIO;

        if (priceLabel.isVisible()) {
            sb.draw(menuBackground, x, y, sizeX, sizeY);
        }

        sb.end();

        stage.draw();
    }
}
