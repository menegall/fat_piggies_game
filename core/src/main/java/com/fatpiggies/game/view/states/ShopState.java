package com.fatpiggies.game.view.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.fatpiggies.game.controller.mainControllerInterfaces.IViewActions;
import com.fatpiggies.game.view.TextureId;
import com.fatpiggies.game.view.TextureManager;

public class ShopState extends State {

    // ===== RATIOS =====
    private static final float BUTTON_WIDTH_RATIO = 0.2f;
    private static final float BUTTON_HEIGHT_RATIO = 0.12f;

    private static final float MENU_X_RATIO = 0.73f;
    private static final float MENU_Y_RATIO = 0.8f;

    private static final float MENU_1_SIZE_X_RATIO = 0.2f;
    private static final float MENU_1_SIZE_Y_RATIO = 0.12f;
    private static final float MENU_1_ANCHOR_X = 0.07f;
    private static final float MENU_1_ANCHOR_Y = 0.8f;

    private static final float COIN_ICON_X_RATIO = 0.18f;
    private static final float COIN_ICON_Y_RATIO = 0.79f;
    private static final float COIN_SIZE_RATIO = 0.06f;

    private static final float COIN_X_RATIO = 0.09f;
    private static final float COIN_Y_RATIO = 0.86f;

    private static final float MENU_2_SIZE_X_RATIO = 0.2f;
    private static final float MENU_2_SIZE_Y_RATIO = 0.12f;
    private static final float MENU_2_ANCHOR_X = 0.07f;
    private static final float MENU_2_ANCHOR_Y = 0.65f;

    private static final float PRICE_X_RATIO = 0.09f;
    private static final float PRICE_Y_RATIO = 0.71f;

    private static final float BUY_X_RATIO = 0.73f;
    private static final float BUY_Y_RATIO = 0.65f;

    private static final float ARROW_SIZE_X_RATIO = 0.09f;
    private static final float ARROW_SIZE_Y_RATIO = 0.21f;
    private static final float ARROW_X_RATIO = 0.73f;
    private static final float ARROW_Y_RATIO = 0.2f;

    // ===== UI =====
    private TextButton menuButton;
    private TextButton buyButton;
    private ImageButton nextBackgroundButton;

    private Label coinsLabel;
    private Label priceLabel;
    private Image coinImage;

    // 🔥 IMPORTANT : preview local (FIX BUG)
    private TextureId currentPreview;

    private TextureRegion menuBackground;

    public ShopState(IViewActions viewActions) {
        super(viewActions);

        menuBackground = TextureManager.getFrame(TextureId.MENU_BACKGROUND);

        // 👉 start avec le background sélectionné
        currentPreview = TextureManager.getCurrentBackground();

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
                viewActions.onMenuClicked();
            }
        });
        stage.addActor(menuButton);

        // ===== COIN ICON =====
        float coinSize = screenWidth * COIN_SIZE_RATIO;

        coinImage = new Image(TextureManager.getFrame(TextureId.COIN));
        coinImage.setSize(coinSize, coinSize);
        coinImage.setPosition(
            screenWidth * COIN_ICON_X_RATIO,
            screenHeight * COIN_ICON_Y_RATIO
        );
        stage.addActor(coinImage);

        // ===== COINS LABEL =====
        coinsLabel = new Label("", skin);
        coinsLabel.setPosition(
            screenWidth * COIN_X_RATIO,
            screenHeight * COIN_Y_RATIO
        );
        stage.addActor(coinsLabel);

        // ===== PRICE LABEL =====
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
                viewActions.onBuyBackgroundClicked(currentPreview);

                updateCoinsLabel();
                updateBuyButton();
                updatePriceLabel();
            }
        });
        stage.addActor(buyButton);

        // ===== ARROW BUTTON =====
        float arrowSizeX = screenWidth * ARROW_SIZE_X_RATIO;
        float arrowSizeY = screenHeight * ARROW_SIZE_Y_RATIO;

        TextureRegionDrawable arrowDrawable =
            new TextureRegionDrawable(TextureManager.getFrame(TextureId.ARROW));

        nextBackgroundButton = new ImageButton(arrowDrawable);
        nextBackgroundButton.setSize(arrowSizeX, arrowSizeY);
        nextBackgroundButton.setPosition(
            screenWidth * ARROW_X_RATIO,
            screenHeight * ARROW_Y_RATIO
        );
        nextBackgroundButton.getImageCell().expand().fill();

        nextBackgroundButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                currentPreview = TextureManager.nextBackground(currentPreview);

                updateBuyButton();
                updatePriceLabel();

                if (viewActions.isBackgroundUnlocked(currentPreview)) {
                    viewActions.onSelectBackground(currentPreview);
                }
            }
        });

        stage.addActor(nextBackgroundButton);

        // ===== INIT =====
        updateCoinsLabel();
        updateBuyButton();
        updatePriceLabel();
    }

    private void updateBuyButton() {
        boolean unlocked = viewActions.isBackgroundUnlocked(currentPreview);
        int coins = viewActions.getCoins();
        int price = viewActions.getPrice(currentPreview);

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

    private void updateCoinsLabel() {
        coinsLabel.setText(String.valueOf(viewActions.getCoins()));
    }

    private void updatePriceLabel() {
        priceLabel.setText("Price : " + viewActions.getPrice(currentPreview));
    }

    @Override
    public void update(float dt) {
        stage.act(dt);
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.begin();

        // Show preview
        sb.draw(
            TextureManager.getFrame(currentPreview),
            0, 0,
            screenWidth, screenHeight
        );

        // panel 1
        float sizeX = screenWidth * MENU_1_SIZE_X_RATIO;
        float sizeY = screenHeight * MENU_1_SIZE_Y_RATIO;
        float x = screenWidth * MENU_1_ANCHOR_X;
        float y = screenHeight * MENU_1_ANCHOR_Y;

        sb.draw(menuBackground, x, y, sizeX, sizeY);

        // panel 2 (price)
        sizeX = screenWidth * MENU_2_SIZE_X_RATIO;
        sizeY = screenHeight * MENU_2_SIZE_Y_RATIO;
        x = screenWidth * MENU_2_ANCHOR_X;
        y = screenHeight * MENU_2_ANCHOR_Y;

        if (priceLabel.isVisible()) {
            sb.draw(menuBackground, x, y, sizeX, sizeY);
        }

        sb.end();

        stage.draw();
    }
}
