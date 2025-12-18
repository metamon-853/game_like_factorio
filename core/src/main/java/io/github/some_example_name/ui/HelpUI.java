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

/**
 * ヘルプ/ガイドUIを描画するクラス。
 */
public class HelpUI {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    private int screenWidth;
    private int screenHeight;
    
    // UIのサイズと位置
    private float panelWidth = 1200;
    private float panelHeight = 900;
    private float panelX;
    private float panelY;
    
    // コンテンツエリア（タイトルとボタンの下）
    private float contentAreaY;
    private float contentAreaHeight;
    
    // スクロール位置
    private float scrollOffset = 0;
    private static final float SCROLL_SPEED = 30f;
    private float maxScrollOffset = 0;
    
    // 戻るボタン
    private Button backButton;
    
    // サウンドマネージャー
    private io.github.some_example_name.system.SoundManager soundManager;
    
    // 前回のホバー状態を記録
    private boolean lastBackButtonHovered = false;
    
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
        scrollOffset = 0;
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
        
        // コンテンツエリアの設定
        float titleHeight = 60;
        float buttonHeight = 75;
        float padding = 20;
        contentAreaY = panelY + padding;
        contentAreaHeight = panelHeight - titleHeight - buttonHeight - padding * 3;
        
        // 戻るボタンの位置を設定（下部に配置）
        float buttonWidth = 300;
        float buttonX = panelX + 20;
        float buttonY = panelY + 20;
        backButton = new Button(buttonX, buttonY, buttonWidth, buttonHeight);
    }
    
    /**
     * マウスクリックを処理します。
     * @param screenX スクリーンX座標
     * @param screenY スクリーンY座標
     * @return 戻るボタンがクリックされた場合true
     */
    public boolean handleClick(int screenX, int screenY) {
        // スクリーン座標をUI座標に変換（LibGDXはY座標が下から上）
        float uiY = screenHeight - screenY;
        
        // 戻るボタンのクリック判定
        if (backButton != null && backButton.contains(screenX, uiY)) {
            return true; // 戻るボタンがクリックされた
        }
        
        return false;
    }
    
    /**
     * スクロール処理を行います。
     */
    public void handleScroll(float amountY) {
        scrollOffset += amountY * SCROLL_SPEED;
        // スクロール範囲を制限
        scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));
    }
    
    /**
     * コンテンツの高さを計算してスクロール範囲を設定します。
     */
    private void calculateContentHeight(LivestockDataLoader livestockDataLoader) {
        float lineSpacing = 35f;
        float sectionSpacing = 50f;
        float totalHeight = 0;
        
        // 操作方法セクション
        totalHeight += lineSpacing * 4 + sectionSpacing;
        
        // 農業セクション
        totalHeight += lineSpacing * 4 + sectionSpacing;
        
        // 家畜セクション
        totalHeight += lineSpacing * 5 + sectionSpacing;
        
        // 家畜の種類セクション
        if (livestockDataLoader != null) {
            Array<LivestockData> allLivestock = livestockDataLoader.getAllLivestock();
            totalHeight += lineSpacing; // タイトル
            for (LivestockData livestock : allLivestock) {
                totalHeight += lineSpacing * 0.8f; // 基本情報
                if (livestock.description != null && !livestock.description.isEmpty()) {
                    totalHeight += lineSpacing * 0.8f; // 説明
                }
            }
            totalHeight += sectionSpacing * 0.5f;
        }
        
        // その他の機能セクション
        totalHeight += lineSpacing * 4;
        
        // 最大スクロールオフセットを計算
        maxScrollOffset = Math.max(0, totalHeight - contentAreaHeight);
    }
    
    /**
     * ヘルプUIを描画します。
     */
    public void render(LivestockDataLoader livestockDataLoader) {
        // コンテンツの高さを計算
        calculateContentHeight(livestockDataLoader);
        
        // パネルの背景を描画
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        shapeRenderer.end();
        
        // パネルの枠線を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        shapeRenderer.end();
        
        // batchを開始
        batch.begin();
        batch.setProjectionMatrix(uiCamera.combined);
        
        font.getData().setScale(0.825f);
        font.setColor(Color.WHITE);
        
        // タイトルを描画
        String title = "ゲームガイド";
        GlyphLayout titleLayout = new GlyphLayout(font, title);
        float titleX = panelX + (panelWidth - titleLayout.width) / 2;
        float titleY = panelY + panelHeight - 45;
        font.draw(batch, title, titleX, titleY);
        
        // 戻るボタンを描画
        if (backButton != null) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            boolean isHovered = backButton.contains(mouseX, mouseY);
            
            // ホバー状態が変わったときに音を再生
            if (isHovered && !lastBackButtonHovered && soundManager != null) {
                soundManager.playHoverSound();
            }
            lastBackButtonHovered = isHovered;
            
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (isHovered) {
                shapeRenderer.setColor(0.25f, 0.25f, 0.35f, 0.95f);
            } else {
                shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 0.95f);
            }
            shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
            shapeRenderer.end();
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            if (isHovered) {
                shapeRenderer.setColor(0.8f, 0.8f, 1.0f, 1f);
            } else {
                shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
            }
            shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
            shapeRenderer.end();
            
            batch.begin();
            font.getData().setScale(0.675f);
            font.setColor(isHovered ? new Color(0.9f, 0.9f, 1.0f, 1f) : Color.WHITE);
            String backText = "戻る";
            GlyphLayout backLayout = new GlyphLayout(font, backText);
            float backTextX = backButton.x + (backButton.width - backLayout.width) / 2;
            float backTextY = backButton.y + backButton.height / 2 + backLayout.height / 2;
            font.draw(batch, backText, backTextX, backTextY);
        }
        
        // スクロール可能な場合、スクロールバーを描画
        if (maxScrollOffset > 0) {
            batch.end();
            drawScrollBar();
            batch.begin();
        }
        
        // クリッピング領域を設定（コンテンツエリアのみ描画）
        batch.flush();
        Rectangle scissors = new Rectangle();
        Rectangle clipBounds = new Rectangle(
            panelX, contentAreaY, panelWidth, contentAreaHeight
        );
        ScissorStack.calculateScissors(uiCamera, batch.getTransformMatrix(), clipBounds, scissors);
        ScissorStack.pushScissors(scissors);
        
        // コンテンツを描画
        float startX = panelX + 40;
        float startY = contentAreaY + contentAreaHeight - 20 - scrollOffset;
        float currentY = startY;
        float lineSpacing = 35f;
        float sectionSpacing = 50f;
        
        font.getData().setScale(0.7f);
        font.setColor(Color.WHITE);
        
        // 操作方法セクション
        font.setColor(new Color(0.8f, 0.9f, 1.0f, 1f));
        font.getData().setScale(0.75f);
        font.draw(batch, "【操作方法】", startX, currentY);
        currentY -= lineSpacing;
        
        font.getData().setScale(0.6f);
        font.setColor(Color.WHITE);
        drawTextLine(batch, "移動: WASDキー または 矢印キー", startX, currentY);
        currentY -= lineSpacing;
        drawTextLine(batch, "インベントリ: Eキー", startX, currentY);
        currentY -= lineSpacing;
        drawTextLine(batch, "ポーズメニュー: ESCキー", startX, currentY);
        currentY -= lineSpacing;
        drawTextLine(batch, "カメラズーム: マウスホイール", startX, currentY);
        currentY -= sectionSpacing;
        
        // 農業セクション
        font.setColor(new Color(0.8f, 0.9f, 1.0f, 1f));
        font.getData().setScale(0.75f);
        font.draw(batch, "【農業】", startX, currentY);
        currentY -= lineSpacing;
        
        font.getData().setScale(0.6f);
        font.setColor(Color.WHITE);
        drawTextLine(batch, "Fキー: 種を植える / 作物を収穫", startX, currentY);
        currentY -= lineSpacing;
        drawTextLine(batch, "・種を持っている状態でFキーを押すと種を植えます", startX + 20, currentY);
        currentY -= lineSpacing * 0.8f;
        drawTextLine(batch, "・成長した作物がある場所でFキーを押すと収穫できます", startX + 20, currentY);
        currentY -= lineSpacing * 0.8f;
        drawTextLine(batch, "・作物は一定時間で成長し、収穫可能になります", startX + 20, currentY);
        currentY -= sectionSpacing;
        
        // 家畜セクション
        font.setColor(new Color(0.8f, 0.9f, 1.0f, 1f));
        font.getData().setScale(0.75f);
        font.draw(batch, "【家畜】", startX, currentY);
        currentY -= lineSpacing;
        
        font.getData().setScale(0.6f);
        font.setColor(Color.WHITE);
        drawTextLine(batch, "Lキー: 家畜を配置 / 製品を収穫", startX, currentY);
        currentY -= lineSpacing;
        drawTextLine(batch, "・作物（餌）を持っている状態でLキーを押すと家畜を配置します", startX + 20, currentY);
        currentY -= lineSpacing * 0.8f;
        drawTextLine(batch, "・成熟した家畜は一定時間ごとに製品（卵、ミルク、羊毛など）を生産します", startX + 20, currentY);
        currentY -= lineSpacing * 0.8f;
        drawTextLine(batch, "・製品が生産されたらLキーで収穫できます", startX + 20, currentY);
        currentY -= lineSpacing;
        
        drawTextLine(batch, "Kキー: 家畜を殺して肉を取得", startX, currentY);
        currentY -= lineSpacing;
        drawTextLine(batch, "・家畜がいる場所でKキーを押すと家畜を殺して肉を取得できます", startX + 20, currentY);
        currentY -= sectionSpacing;
        
        // 家畜の種類
        if (livestockDataLoader != null) {
            font.setColor(new Color(0.8f, 0.9f, 1.0f, 1f));
            font.getData().setScale(0.75f);
            font.draw(batch, "【家畜の種類】", startX, currentY);
            currentY -= lineSpacing;
            
            font.getData().setScale(0.6f);
            font.setColor(Color.WHITE);
            Array<LivestockData> allLivestock = livestockDataLoader.getAllLivestock();
            for (LivestockData livestock : allLivestock) {
                String livestockInfo = "・" + livestock.name + ": ";
                if (livestock.hasProduct()) {
                    livestockInfo += "肉（ID:" + livestock.meatItemId + "）、製品（ID:" + livestock.productItemId + "）";
                } else {
                    livestockInfo += "肉（ID:" + livestock.meatItemId + "）のみ";
                }
                drawTextLine(batch, livestockInfo, startX + 20, currentY);
                currentY -= lineSpacing * 0.8f;
                if (livestock.description != null && !livestock.description.isEmpty()) {
                    drawTextLine(batch, "  " + livestock.description, startX + 30, currentY);
                    currentY -= lineSpacing * 0.8f;
                }
            }
            currentY -= sectionSpacing * 0.5f;
        }
        
        // その他の機能
        font.setColor(new Color(0.8f, 0.9f, 1.0f, 1f));
        font.getData().setScale(0.75f);
        font.draw(batch, "【その他の機能】", startX, currentY);
        currentY -= lineSpacing;
        
        font.getData().setScale(0.6f);
        font.setColor(Color.WHITE);
        drawTextLine(batch, "・インベントリでアイテムをクラフトできます", startX + 20, currentY);
        currentY -= lineSpacing * 0.8f;
        drawTextLine(batch, "・アイテム図鑑でアイテムの詳細を確認できます", startX + 20, currentY);
        currentY -= lineSpacing * 0.8f;
        drawTextLine(batch, "・ポーズメニューからゲームをセーブ/ロードできます", startX + 20, currentY);
        currentY -= lineSpacing * 0.8f;
        drawTextLine(batch, "・文明レベルが上がると新しいアイテムが利用可能になります", startX + 20, currentY);
        
        font.getData().setScale(0.825f);
        font.setColor(Color.WHITE);
        
        // クリッピングを解除
        batch.flush();
        ScissorStack.popScissors();
        
        batch.end();
    }
    
    /**
     * スクロールバーを描画します。
     */
    private void drawScrollBar() {
        if (maxScrollOffset <= 0) return;
        
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        float scrollBarWidth = 10;
        float scrollBarX = panelX + panelWidth - scrollBarWidth - 8;
        float scrollBarHeight = contentAreaHeight;
        float scrollBarY = contentAreaY;
        
        // スクロールバーの背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 0.9f);
        shapeRenderer.rect(scrollBarX, scrollBarY, scrollBarWidth, scrollBarHeight);
        shapeRenderer.end();
        
        // スクロールバーのつまみ
        float totalContentHeight = contentAreaHeight + maxScrollOffset;
        float thumbHeight = Math.max(30, contentAreaHeight * (contentAreaHeight / totalContentHeight));
        float scrollRatio = maxScrollOffset > 0 ? scrollOffset / maxScrollOffset : 0;
        float thumbY = scrollBarY + (scrollBarHeight - thumbHeight) * scrollRatio;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.5f, 0.5f, 0.7f, 0.95f);
        shapeRenderer.rect(scrollBarX + 1, thumbY, scrollBarWidth - 2, thumbHeight);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.7f, 0.7f, 0.9f, 1f);
        shapeRenderer.rect(scrollBarX + 1, thumbY, scrollBarWidth - 2, thumbHeight);
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
