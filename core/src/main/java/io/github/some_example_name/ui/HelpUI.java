package io.github.some_example_name.ui;

import io.github.some_example_name.manager.LivestockDataLoader;
import io.github.some_example_name.entity.LivestockData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import java.util.List;

/**
 * ヘルプ/ガイドUIを描画するクラス。
 */
public class HelpUI {
    /**
     * ガイドの状態を表すenum
     */
    public enum GuideState {
        MENU,           // メニュー画面
        CONTROLS,       // 操作方法
        FARMING,        // 農業
        LIVESTOCK,      // 家畜
        TERRAIN,        // 地形
        OTHER_FEATURES  // その他の機能
    }
    
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    private int screenWidth;
    private int screenHeight;
    
    // 現在の状態
    private GuideState currentState = GuideState.MENU;
    
    // UIのサイズと位置
    private float panelWidth = 1200;
    private float panelHeight = 900;
    private float panelX;
    private float panelY;
    
    // ヘッダーエリア（タイトル用）
    private float headerY;
    private float headerHeight;
    
    // コンテンツエリア（タイトルとフッターの間）
    private float contentAreaY;
    private float contentAreaHeight;
    
    // フッターエリア（戻るボタン用）
    private float footerY;
    private float footerHeight;
    
    // スクロール位置
    private float scrollOffset = 0;
    private static final float SCROLL_SPEED = 30f;
    private float maxScrollOffset = 0;
    private float totalContentHeight = 0; // 実際のコンテンツの高さ

    // スクロールバーのドラッグ状態
    private boolean isDraggingScrollThumb = false;
    private float scrollThumbGrabOffsetY = 0f; // つまみ内で掴んだ位置（Y）
    
    // ボタン
    private Button backButton;
    private Button controlsButton;
    private Button farmingButton;
    private Button livestockButton;
    private Button terrainButton;
    private Button otherFeaturesButton;
    
    // サウンドマネージャー
    private io.github.some_example_name.system.SoundManager soundManager;
    
    // 前回のホバー状態を記録
    private boolean lastBackButtonHovered = false;
    private boolean lastControlsButtonHovered = false;
    private boolean lastFarmingButtonHovered = false;
    private boolean lastLivestockButtonHovered = false;
    private boolean lastTerrainButtonHovered = false;
    private boolean lastOtherFeaturesButtonHovered = false;
    
    public HelpUI(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                 OrthographicCamera uiCamera, int screenWidth, int screenHeight) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.uiCamera = uiCamera;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        updatePanelPosition();
    }
    
    /**
     * サウンドマネージャーを設定します。
     */
    public void setSoundManager(io.github.some_example_name.system.SoundManager soundManager) {
        this.soundManager = soundManager;
    }
    
    /**
     * スクロール位置をリセットします。
     */
    public void resetScroll() {
        scrollOffset = 0; // 最上部から開始
    }
    
    /**
     * ヘルプ画面が開かれたときに呼び出されます。
     */
    public void onOpen() {
        resetScroll();
        currentState = GuideState.MENU; // メニュー画面に戻る
    }
    
    /**
     * 現在の状態を取得します。
     */
    public GuideState getCurrentState() {
        return currentState;
    }
    
    /**
     * 状態を設定します。
     */
    public void setState(GuideState state) {
        this.currentState = state;
        resetScroll();
    }
    
    /**
     * 画面サイズを更新します。
     */
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        updatePanelPosition();
    }
    
    /**
     * パネルの位置を更新します（画面中央に配置）。
     */
    private void updatePanelPosition() {
        panelX = (screenWidth - panelWidth) / 2;
        panelY = (screenHeight - panelHeight) / 2;
        
        // エリアの設定
        float bodyPadding = 30;
        float buttonHeight = 75;
        float footerPadding = 20;
        
        // ヘッダーエリアの設定（上部）
        headerHeight = 80;
        headerY = panelY + panelHeight - headerHeight;
        
        // フッターエリアの設定（ウィンドウ下端に接地）
        // LibGDXではY=0が下部、Y=screenHeightが上部なので、
        // panelYが下部、panelY + panelHeightが上部
        footerHeight = buttonHeight + footerPadding * 2;
        footerY = panelY; // フッター下辺 = ウィンドウ下辺
        
        // コンテンツエリアの設定（ガイド表示用、ヘッダーとフッターの間）
        contentAreaY = footerY + footerHeight + bodyPadding;
        contentAreaHeight = headerY - contentAreaY - bodyPadding;
        
        // 戻るボタンの位置を設定（フッターエリア内に配置、下部）
        float buttonWidth = 300;
        float buttonX = panelX + 20;
        float buttonY = footerY + footerPadding;
        backButton = new Button(buttonX, buttonY, buttonWidth, buttonHeight);
        
        // メニューボタンの位置を設定（ボディエリア内に配置）
        float menuButtonWidth = 500;
        float menuButtonHeight = 80;
        float menuButtonSpacing = 20;
        float menuStartX = panelX + (panelWidth - menuButtonWidth) / 2;
        // ボタンをヘッダーの下から配置（上から下へ）
        float menuStartY = headerY - bodyPadding - menuButtonHeight;
        
        controlsButton = new Button(menuStartX, menuStartY, menuButtonWidth, menuButtonHeight);
        farmingButton = new Button(menuStartX, menuStartY - (menuButtonHeight + menuButtonSpacing), menuButtonWidth, menuButtonHeight);
        livestockButton = new Button(menuStartX, menuStartY - (menuButtonHeight + menuButtonSpacing) * 2, menuButtonWidth, menuButtonHeight);
        terrainButton = new Button(menuStartX, menuStartY - (menuButtonHeight + menuButtonSpacing) * 3, menuButtonWidth, menuButtonHeight);
        otherFeaturesButton = new Button(menuStartX, menuStartY - (menuButtonHeight + menuButtonSpacing) * 4, menuButtonWidth, menuButtonHeight);
    }
    
    /**
     * マウスクリックを処理します。
     * @param screenX スクリーンX座標（LibGDXの座標系、左上が原点）
     * @param screenY スクリーンY座標（LibGDXの座標系、左上が原点）
     * @return 戻るボタンがクリックされた場合true（ゲームを終了する必要がある場合）
     */
    public boolean handleClick(int screenX, int screenY) {
        // スクリーン座標をUI座標に変換（LibGDXはY座標が下から上）
        float uiY = screenHeight - screenY;
        
        if (currentState == GuideState.MENU) {
            // メニュー画面でのクリック処理
            if (controlsButton != null && controlsButton.contains((float)screenX, uiY)) {
                currentState = GuideState.CONTROLS;
                resetScroll();
                return false;
            }
            if (farmingButton != null && farmingButton.contains((float)screenX, uiY)) {
                currentState = GuideState.FARMING;
                resetScroll();
                return false;
            }
            if (livestockButton != null && livestockButton.contains((float)screenX, uiY)) {
                currentState = GuideState.LIVESTOCK;
                resetScroll();
                return false;
            }
            if (terrainButton != null && terrainButton.contains((float)screenX, uiY)) {
                currentState = GuideState.TERRAIN;
                resetScroll();
                return false;
            }
            if (otherFeaturesButton != null && otherFeaturesButton.contains((float)screenX, uiY)) {
                currentState = GuideState.OTHER_FEATURES;
                resetScroll();
                return false;
            }
            // メニュー画面での戻るボタンのクリック判定（ゲームガイドを閉じる）
            if (backButton != null && backButton.contains((float)screenX, uiY)) {
                return true; // ゲームガイドを閉じる
            }
        } else {
            // 各ガイド画面での戻るボタンのクリック判定
            if (backButton != null && backButton.contains((float)screenX, uiY)) {
                currentState = GuideState.MENU;
                resetScroll();
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * スクロール処理を行います。
     * @param amountY スクロール量（LibGDXでは amountY > 0 が上スクロール、amountY < 0 が下スクロール）
     */
    public void handleScroll(float amountY) {
        // amountY > 0 のとき（上スクロール）は scrollOffset を増やす（コンテンツを上にスクロール、下のコンテンツが見える）
        // amountY < 0 のとき（下スクロール）は scrollOffset を減らす（コンテンツを下にスクロール、上のコンテンツが見える）
        scrollOffset += amountY * SCROLL_SPEED;
        // スクロール範囲を制限
        scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));
    }

    /**
     * スクロールバー（つまみ）のドラッグ入力を処理します。
     * MenuSystem側の毎フレーム入力処理から呼び出してください。
     */
    public void handleScrollBarDragInput() {
        if (maxScrollOffset <= 0) {
            isDraggingScrollThumb = false;
            return;
        }

        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY(); // UI座標（下が0）

        ScrollBarMetrics m = computeScrollBarMetrics();

        // 押した瞬間に、つまみを掴んだか判定
        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            if (mouseX >= m.thumbX && mouseX <= m.thumbX + m.thumbWidth &&
                mouseY >= m.thumbY && mouseY <= m.thumbY + m.thumbHeight) {
                isDraggingScrollThumb = true;
                scrollThumbGrabOffsetY = mouseY - m.thumbY;
            } else {
                isDraggingScrollThumb = false;
            }
        }

        // ドラッグ中：つまみ位置からscrollOffsetへ反映
        if (isDraggingScrollThumb) {
            if (!Gdx.input.isButtonPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
                isDraggingScrollThumb = false;
                return;
            }

            float trackYMin = m.scrollBarY;
            float trackYMax = m.scrollBarY + m.scrollBarHeight - m.thumbHeight;
            float newThumbY = mouseY - scrollThumbGrabOffsetY;
            newThumbY = Math.max(trackYMin, Math.min(trackYMax, newThumbY));

            float trackRange = Math.max(1f, (m.scrollBarHeight - m.thumbHeight));
            // drawScrollBar() と同じマッピング：
            // thumbY = scrollBarY + (scrollBarHeight - thumbHeight) * (1 - scrollRatio)
            // => scrollRatio = 1 - (thumbY - scrollBarY) / trackRange
            float scrollRatio = 1f - ((newThumbY - m.scrollBarY) / trackRange);
            scrollOffset = scrollRatio * maxScrollOffset;
            scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));
        }
    }

    private static final class ScrollBarMetrics {
        float scrollBarX;
        float scrollBarY;
        float scrollBarWidth;
        float scrollBarHeight;
        float thumbX;
        float thumbY;
        float thumbWidth;
        float thumbHeight;
    }

    private ScrollBarMetrics computeScrollBarMetrics() {
        ScrollBarMetrics m = new ScrollBarMetrics();

        m.scrollBarWidth = 10f;
        m.scrollBarX = panelX + panelWidth - m.scrollBarWidth - 8f;
        m.scrollBarHeight = contentAreaHeight;
        m.scrollBarY = contentAreaY;

        float safeTotalContentHeight = Math.max(1f, totalContentHeight);
        m.thumbHeight = Math.max(30f, contentAreaHeight * (contentAreaHeight / safeTotalContentHeight));

        float scrollRatio = maxScrollOffset > 0 ? scrollOffset / maxScrollOffset : 0f;
        m.thumbY = m.scrollBarY + (m.scrollBarHeight - m.thumbHeight) * (1.0f - scrollRatio);

        m.thumbX = m.scrollBarX + 1f;
        m.thumbWidth = m.scrollBarWidth - 2f;

        return m;
    }
    
    /**
     * コンテンツの高さを計算してスクロール範囲を設定します。
     */
    private void calculateContentHeight(LivestockDataLoader livestockDataLoader) {
        float lineSpacing = 35f;
        float totalHeight = 0;
        
        switch (currentState) {
            case CONTROLS:
            case FARMING:
            case LIVESTOCK:
            case TERRAIN:
            case OTHER_FEATURES:
                // Markdownファイルから読み込んだ要素の高さを計算
                List<GuideContentLoader.GuideElement> elements = GuideContentLoader.loadGuideContent(currentState, livestockDataLoader);
                totalHeight = calculateGuideElementsHeight(elements, lineSpacing);
                break;
            default:
                totalHeight = 0;
                break;
        }
        
        // 実際のコンテンツの高さを保存
        totalContentHeight = totalHeight;
        
        // 最大スクロールオフセットを計算
        // コンテンツが表示エリアより大きい場合、その差分だけスクロール可能
        maxScrollOffset = Math.max(0, totalHeight - contentAreaHeight + 40); // パディング分も考慮
    }
    
    /**
     * ヘルプUIを描画します。
     */
    public void render(LivestockDataLoader livestockDataLoader) {
        // パネルの背景を描画
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        shapeRenderer.end();
        
        // ヘッダーエリアの背景を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 0.95f);
        shapeRenderer.rect(panelX, headerY, panelWidth, headerHeight);
        shapeRenderer.end();
        
        // パネルの枠線を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        shapeRenderer.line(panelX, panelY + panelHeight, panelX + panelWidth, panelY + panelHeight);
        shapeRenderer.line(panelX, panelY + panelHeight, panelX, panelY);
        shapeRenderer.line(panelX + panelWidth, panelY + panelHeight, panelX + panelWidth, panelY);
        // ヘッダーとボディの区切り線
        shapeRenderer.line(panelX, headerY, panelX + panelWidth, headerY);
        shapeRenderer.end();
        
        // batchを開始
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // ヘッダーにタイトルを描画
        font.getData().setScale(1.0f);
        font.setColor(Color.WHITE);
        String title = "ゲームガイド";
        GlyphLayout titleLayout = new GlyphLayout(font, title);
        float titleX = panelX + (panelWidth - titleLayout.width) / 2;
        float titleY = headerY + headerHeight / 2 + titleLayout.height / 2;
        font.draw(batch, title, titleX, titleY);
        
        batch.end();
        
        // 状態に応じて描画
        if (currentState == GuideState.MENU) {
            renderMenu();
        } else {
            renderGuide(livestockDataLoader);
        }
    }
    
    /**
     * メニュー画面を描画します。
     */
    private void renderMenu() {
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        // 操作方法ボタン
        if (controlsButton != null) {
            boolean isHovered = controlsButton.contains(mouseX, mouseY);
            if (isHovered && !lastControlsButtonHovered && soundManager != null) {
                soundManager.playHoverSound();
            }
            lastControlsButtonHovered = isHovered;
            drawButton(controlsButton, "操作方法", isHovered);
        }
        
        // 農業ボタン
        if (farmingButton != null) {
            boolean isHovered = farmingButton.contains(mouseX, mouseY);
            if (isHovered && !lastFarmingButtonHovered && soundManager != null) {
                soundManager.playHoverSound();
            }
            lastFarmingButtonHovered = isHovered;
            drawButton(farmingButton, "農業", isHovered);
        }
        
        // 家畜ボタン
        if (livestockButton != null) {
            boolean isHovered = livestockButton.contains(mouseX, mouseY);
            if (isHovered && !lastLivestockButtonHovered && soundManager != null) {
                soundManager.playHoverSound();
            }
            lastLivestockButtonHovered = isHovered;
            drawButton(livestockButton, "家畜", isHovered);
        }
        
        // 地形ボタン
        if (terrainButton != null) {
            boolean isHovered = terrainButton.contains(mouseX, mouseY);
            if (isHovered && !lastTerrainButtonHovered && soundManager != null) {
                soundManager.playHoverSound();
            }
            lastTerrainButtonHovered = isHovered;
            drawButton(terrainButton, "地形", isHovered);
        }
        
        // その他の機能ボタン
        if (otherFeaturesButton != null) {
            boolean isHovered = otherFeaturesButton.contains(mouseX, mouseY);
            if (isHovered && !lastOtherFeaturesButtonHovered && soundManager != null) {
                soundManager.playHoverSound();
            }
            lastOtherFeaturesButtonHovered = isHovered;
            drawButton(otherFeaturesButton, "その他の機能", isHovered);
        }
        
        // 戻るボタンを描画（フッター）
        if (backButton != null) {
            boolean isHovered = backButton.contains(mouseX, mouseY);
            if (isHovered && !lastBackButtonHovered && soundManager != null) {
                soundManager.playHoverSound();
            }
            lastBackButtonHovered = isHovered;
            drawButton(backButton, "戻る", isHovered);
        }
    }
    
    /**
     * 各ガイド画面を描画します。
     */
    private void renderGuide(LivestockDataLoader livestockDataLoader) {
        // コンテンツの高さを計算
        calculateContentHeight(livestockDataLoader);
        
        // 戻るボタンを描画
        if (backButton != null) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            boolean isHovered = backButton.contains(mouseX, mouseY);
            
            if (isHovered && !lastBackButtonHovered && soundManager != null) {
                soundManager.playHoverSound();
            }
            lastBackButtonHovered = isHovered;
            
            drawButton(backButton, "戻る", isHovered);
        }
        
        // クリッピング領域を設定（コンテンツエリアのみ描画）
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.flush();
        Rectangle scissors = new Rectangle();
        Rectangle clipBounds = new Rectangle(
            panelX, contentAreaY, panelWidth, contentAreaHeight
        );
        ScissorStack.calculateScissors(uiCamera, batch.getTransformMatrix(), clipBounds, scissors);
        boolean scissorsPushed = ScissorStack.pushScissors(scissors);
        
        // コンテンツを描画
        float startX = panelX + 40;
        float startY = contentAreaY + contentAreaHeight - 20 + scrollOffset;
        float currentY = startY;
        float lineSpacing = 35f;
        
        font.getData().setScale(0.7f);
        font.setColor(Color.WHITE);
        
        // Markdownファイルからガイドコンテンツを読み込んで描画
        List<GuideContentLoader.GuideElement> elements = GuideContentLoader.loadGuideContent(currentState, livestockDataLoader);
        currentY = renderGuideElements(batch, elements, startX, currentY, lineSpacing);
        
        font.getData().setScale(0.825f);
        font.setColor(Color.WHITE);
        
        // クリッピングを解除
        batch.flush();
        if (scissorsPushed) {
            ScissorStack.popScissors();
        }
        batch.end();
        
        // スクロール可能な場合、スクロールバーを描画
        if (maxScrollOffset > 0) {
            drawScrollBar();
        }
    }
    
    /**
     * ボタンを描画します。
     */
    private void drawButton(Button button, String text, boolean isHovered) {
        // batchが既に開始されているかチェック
        boolean batchWasActive = batch.isDrawing();
        
        // batchが開始されている場合は終了してからShapeRendererを使用
        if (batchWasActive) {
            batch.end();
        }
        
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (isHovered) {
            shapeRenderer.setColor(0.25f, 0.25f, 0.35f, 0.95f);
        } else {
            shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 0.95f);
        }
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (isHovered) {
            shapeRenderer.setColor(0.8f, 0.8f, 1.0f, 1f);
        } else {
            shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        }
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        shapeRenderer.end();
        
        // batchを開始（元々開始されていた場合は再度開始、されていなかった場合は新規開始）
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.getData().setScale(0.675f);
        font.setColor(isHovered ? new Color(0.9f, 0.9f, 1.0f, 1f) : Color.WHITE);
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = button.x + (button.width - layout.width) / 2;
        float textY = button.y + button.height / 2 + layout.height / 2;
        font.draw(batch, text, textX, textY);
        
        // batchが元々開始されていなかった場合は終了する
        // （呼び出し元で開始されていた場合は、呼び出し元で終了を管理）
        if (!batchWasActive) {
            batch.end();
        }
    }
    
    /**
     * ガイド要素のリストを描画します。
     * @param batch SpriteBatch
     * @param elements ガイド要素のリスト
     * @param startX 開始X座標
     * @param startY 開始Y座標
     * @param lineSpacing 行間隔
     * @return 描画後のY座標
     */
    private float renderGuideElements(SpriteBatch batch, List<GuideContentLoader.GuideElement> elements, 
                                     float startX, float startY, float lineSpacing) {
        float currentY = startY;
        
        for (GuideContentLoader.GuideElement element : elements) {
            switch (element.type) {
                case HEADING_1:
                    font.setColor(new Color(0.8f, 0.9f, 1.0f, 1f));
                    font.getData().setScale(0.75f);
                    font.draw(batch, "【" + element.text + "】", startX, currentY);
                    currentY -= lineSpacing;
                    break;
                    
                case HEADING_2:
                    font.setColor(new Color(0.8f, 0.9f, 1.0f, 1f));
                    font.getData().setScale(0.75f);
                    font.draw(batch, element.text, startX, currentY);
                    currentY -= lineSpacing;
                    break;
                    
                case HEADING_3:
                    font.setColor(new Color(0.7f, 0.8f, 0.95f, 1f));
                    font.getData().setScale(0.7f);
                    font.draw(batch, element.text, startX, currentY);
                    currentY -= lineSpacing * 0.9f;
                    break;
                    
                case LIST_ITEM:
                    font.getData().setScale(0.6f);
                    font.setColor(Color.WHITE);
                    float indentX = startX + 20 + (element.indentLevel * 20);
                    drawTextLine(batch, "・" + element.text, indentX, currentY);
                    currentY -= lineSpacing * 0.8f;
                    break;
                    
                case TEXT:
                    font.getData().setScale(0.6f);
                    font.setColor(Color.WHITE);
                    drawTextLine(batch, element.text, startX, currentY);
                    currentY -= lineSpacing * 0.8f;
                    break;
                    
                case SEPARATOR:
                    currentY -= lineSpacing * 0.5f; // 区切り線の前後にスペース
                    break;
            }
        }
        
        return currentY;
    }
    
    /**
     * ガイド要素のリストの高さを計算します。
     * @param elements ガイド要素のリスト
     * @param lineSpacing 行間隔
     * @return 合計の高さ
     */
    private float calculateGuideElementsHeight(List<GuideContentLoader.GuideElement> elements, float lineSpacing) {
        float totalHeight = 0;
        
        for (GuideContentLoader.GuideElement element : elements) {
            switch (element.type) {
                case HEADING_1:
                case HEADING_2:
                    totalHeight += lineSpacing;
                    break;
                case HEADING_3:
                    totalHeight += lineSpacing * 0.9f;
                    break;
                case LIST_ITEM:
                case TEXT:
                    totalHeight += lineSpacing * 0.8f;
                    break;
                case SEPARATOR:
                    totalHeight += lineSpacing * 0.5f;
                    break;
            }
        }
        
        return totalHeight;
    }
    
    /**
     * スクロールバーを描画します。
     */
    private void drawScrollBar() {
        if (maxScrollOffset <= 0) return;
        
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        ScrollBarMetrics m = computeScrollBarMetrics();
        
        // スクロールバーの背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 0.9f);
        shapeRenderer.rect(m.scrollBarX, m.scrollBarY, m.scrollBarWidth, m.scrollBarHeight);
        shapeRenderer.end();
        
        // スクロールバーのつまみ
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.5f, 0.5f, 0.7f, 0.95f);
        shapeRenderer.rect(m.thumbX, m.thumbY, m.thumbWidth, m.thumbHeight);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.7f, 0.7f, 0.9f, 1f);
        shapeRenderer.rect(m.thumbX, m.thumbY, m.thumbWidth, m.thumbHeight);
        shapeRenderer.end();
    }
    
    /**
     * テキストを1行描画します（長い場合は折り返し）。
     */
    private void drawTextLine(SpriteBatch batch, String text, float x, float y) {
        float maxWidth = panelWidth - 80;
        GlyphLayout layout = new GlyphLayout(font, text);
        
        if (layout.width > maxWidth) {
            // 長い場合は折り返し処理（簡易版）
            String[] words = text.split("");
            StringBuilder line = new StringBuilder();
            float currentX = x;
            float currentY = y;
            
            for (String word : words) {
                String testLine = line.toString() + word;
                GlyphLayout testLayout = new GlyphLayout(font, testLine);
                if (testLayout.width > maxWidth && line.length() > 0) {
                    font.draw(batch, line.toString(), currentX, currentY);
                    currentY -= font.getLineHeight() * 0.8f;
                    line = new StringBuilder(word);
                } else {
                    line.append(word);
                }
            }
            if (line.length() > 0) {
                font.draw(batch, line.toString(), currentX, currentY);
            }
        } else {
            font.draw(batch, text, x, y);
        }
    }
}
