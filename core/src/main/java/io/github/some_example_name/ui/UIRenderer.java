package io.github.some_example_name.ui;

import io.github.some_example_name.entity.Player;
import io.github.some_example_name.manager.ItemManager;
import io.github.some_example_name.game.CivilizationLevel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * UI描画を担当するクラス。
 */
public class UIRenderer {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    private int screenWidth;
    private int screenHeight;
    
    // ゲームガイドボタン
    private Button guideButton;
    
    public UIRenderer(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font, 
                     OrthographicCamera uiCamera, int screenWidth, int screenHeight) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.uiCamera = uiCamera;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    /**
     * 画面サイズを更新します。
     */
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
    
    /**
     * グリッドを描画します（カメラの視野範囲に基づいて動的に生成）。
     * @param camera カメラ
     */
    public void drawGrid(OrthographicCamera camera) {
        int mapTileSize = Player.MAP_TILE_SIZE;
        int playerTileSize = Player.PLAYER_TILE_SIZE;
        
        float actualViewportWidth = camera.viewportWidth * camera.zoom;
        float actualViewportHeight = camera.viewportHeight * camera.zoom;
        float cameraLeft = camera.position.x - actualViewportWidth / 2;
        float cameraRight = camera.position.x + actualViewportWidth / 2;
        float cameraBottom = camera.position.y - actualViewportHeight / 2;
        float cameraTop = camera.position.y + actualViewportHeight / 2;
        
        float margin = mapTileSize * 2;
        
        // プレイヤー升の細かいグリッドを描画
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        
        int startPlayerTileX = (int)Math.floor((cameraLeft - margin) / playerTileSize);
        int endPlayerTileX = (int)Math.ceil((cameraRight + margin) / playerTileSize);
        int startPlayerTileY = (int)Math.floor((cameraBottom - margin) / playerTileSize);
        int endPlayerTileY = (int)Math.ceil((cameraTop + margin) / playerTileSize);
        
        for (int x = startPlayerTileX; x <= endPlayerTileX; x++) {
            float lineX = x * playerTileSize;
            shapeRenderer.line(lineX, startPlayerTileY * playerTileSize, lineX, endPlayerTileY * playerTileSize);
        }
        
        for (int y = startPlayerTileY; y <= endPlayerTileY; y++) {
            float lineY = y * playerTileSize;
            shapeRenderer.line(startPlayerTileX * playerTileSize, lineY, endPlayerTileX * playerTileSize, lineY);
        }
        
        // マップ升の太いグリッドを描画
        shapeRenderer.setColor(Color.DARK_GRAY);
        
        int startMapTileX = (int)Math.floor((cameraLeft - margin) / mapTileSize);
        int endMapTileX = (int)Math.ceil((cameraRight + margin) / mapTileSize);
        int startMapTileY = (int)Math.floor((cameraBottom - margin) / mapTileSize);
        int endMapTileY = (int)Math.ceil((cameraTop + margin) / mapTileSize);
        
        for (int x = startMapTileX; x <= endMapTileX; x++) {
            float lineX = x * mapTileSize;
            shapeRenderer.line(lineX, startMapTileY * mapTileSize, lineX, endMapTileY * mapTileSize);
        }
        
        for (int y = startMapTileY; y <= endMapTileY; y++) {
            float lineY = y * mapTileSize;
            shapeRenderer.line(startMapTileX * mapTileSize, lineY, endMapTileX * mapTileSize, lineY);
        }
    }
    
    /**
     * UI情報（取得アイテム数など）を描画します。
     * @param itemManager アイテムマネージャー
     * @param totalLivestockProducts 畜産物の累計生産数（nullの場合は表示しない）
     */
    public void drawUI(ItemManager itemManager, Integer totalLivestockProducts) {
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        font.getData().setScale(0.625f);
        font.setColor(Color.WHITE);
        
        float padding = 20;
        float leftX = padding;
        float topY = screenHeight - padding;
        
        // FPSを表示
        int fps = Gdx.graphics.getFramesPerSecond();
        String fpsText = "FPS: " + fps;
        font.draw(batch, fpsText, leftX, topY);
        
        // 文明レベルを表示（日本語対応）
        CivilizationLevel civLevel = itemManager.getCivilizationLevel();
        String civText = "文明レベル: " + civLevel.getLevel() + " (" + civLevel.getLevelName() + ")";
        GlyphLayout civLayout = new GlyphLayout(font, civText);
        float currentY = topY - civLayout.height - 10;
        font.draw(batch, civText, leftX, currentY);
        
        // 文明進捗を表示（次のレベルへの進捗）
        if (civLevel.getLevel() < CivilizationLevel.MAX_LEVEL) {
            int nextLevel = civLevel.getLevel() + 1;
            String progressText = getCivilizationProgressText(civLevel, nextLevel, totalLivestockProducts);
            if (progressText != null) {
                GlyphLayout progressLayout = new GlyphLayout(font, progressText);
                currentY -= progressLayout.height + 10;
                font.draw(batch, progressText, leftX, currentY);
                
                // 進捗バーを描画
                drawProgressBar(leftX, currentY - 20, 200, 10, civLevel, nextLevel, totalLivestockProducts);
            }
        }
        
        // ゲームガイドボタンを描画
        float rightX = screenWidth - padding;
        float buttonWidth = 150;
        float buttonHeight = 40;
        float buttonX = rightX - buttonWidth;
        float buttonY = topY - buttonHeight;
        
        // ボタンの位置を更新
        guideButton = new Button(buttonX, buttonY, buttonWidth, buttonHeight);
        
        // マウスホバー判定
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        boolean isHovered = guideButton.contains(mouseX, mouseY);
        
        batch.end();
        
        // ボタンを描画（drawButton内でbatchの開始/終了を管理）
        drawButton(buttonX, buttonY, buttonWidth, buttonHeight, "ゲームガイド", isHovered);
        
        batch.end();
    }
    
    /**
     * 文明進捗のテキストを取得します。
     */
    private String getCivilizationProgressText(CivilizationLevel civLevel, int nextLevel, Integer totalLivestockProducts) {
        if (nextLevel == 3) {
            // レベル3への進行条件：畜産物を累計20生産
            if (totalLivestockProducts != null) {
                return "畜産物生産: " + totalLivestockProducts + " / 20";
            }
        } else if (nextLevel == 4) {
            // レベル4への進行条件：畜産物を累計100生産
            if (totalLivestockProducts != null) {
                return "畜産物生産: " + totalLivestockProducts + " / 100";
            }
        }
        return null;
    }
    
    /**
     * 進捗バーを描画します。
     */
    private void drawProgressBar(float x, float y, float width, float height, 
                                  CivilizationLevel civLevel, int nextLevel, Integer totalLivestockProducts) {
        batch.end();
        
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // 背景（グレー）
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(x, y, width, height);
        
        // 進捗（緑）
        float progress = 0f;
        if (nextLevel == 3 && totalLivestockProducts != null) {
            progress = Math.min(1f, totalLivestockProducts / 20f);
        } else if (nextLevel == 4 && totalLivestockProducts != null) {
            progress = Math.min(1f, totalLivestockProducts / 100f);
        }
        
        shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 1f);
        shapeRenderer.rect(x, y, width * progress, height);
        
        shapeRenderer.end();
        
        batch.begin();
    }
    
    /**
     * ボタンを描画します。
     * @param x ボタンのX座標
     * @param y ボタンのY座標
     * @param width ボタンの幅
     * @param height ボタンの高さ
     * @param text ボタンのテキスト
     * @param isHovered ホバー状態
     */
    public void drawButton(float x, float y, float width, float height, String text, boolean isHovered) {
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
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (isHovered) {
            shapeRenderer.setColor(0.8f, 0.8f, 1.0f, 1f);
            shapeRenderer.rect(x - 1, y - 1, width + 2, height + 2);
            shapeRenderer.rect(x, y, width, height);
        } else {
            shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
            shapeRenderer.rect(x, y, width, height);
        }
        shapeRenderer.end();
        
        // batchを開始（元々開始されていた場合は再度開始、されていなかった場合は新規開始）
        batch.begin();
        font.getData().setScale(0.45f);
        font.setColor(isHovered ? new Color(0.9f, 0.9f, 1.0f, 1f) : Color.WHITE);
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = x + (width - layout.width) / 2;
        float textY = y + height / 2 + layout.height / 2;
        font.draw(batch, text, textX, textY);
        
        // batchが元々開始されていなかった場合は終了する
        // （呼び出し元で開始されていた場合は、呼び出し元で終了を管理）
        if (!batchWasActive) {
            batch.end();
        }
    }
    
    /**
     * 音量スライダーを描画します。
     * @param centerX 中心X座標
     * @param centerY 中心Y座標
     * @param masterVolume マスターボリューム
     * @param isMuted ミュート状態
     */
    public void drawVolumeSlider(float centerX, float centerY, float masterVolume, boolean isMuted) {
        batch.end();
        
        float sliderWidth = 400;
        float sliderHeight = 20;
        float sliderX = centerX - sliderWidth / 2;
        float sliderY = centerY - sliderHeight / 2;
        
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        // スライダーの背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
        shapeRenderer.rect(sliderX, sliderY, sliderWidth, sliderHeight);
        shapeRenderer.end();
        
        // スライダーの枠線
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        shapeRenderer.rect(sliderX, sliderY, sliderWidth, sliderHeight);
        shapeRenderer.end();
        
        // スライダーのハンドル
        float handleWidth = 30;
        float handleHeight = 40;
        float handleX = sliderX + (masterVolume * sliderWidth) - handleWidth / 2;
        float handleY = sliderY - (handleHeight - sliderHeight) / 2;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(isMuted ? Color.RED : Color.WHITE);
        shapeRenderer.rect(handleX, handleY, handleWidth, handleHeight);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(handleX, handleY, handleWidth, handleHeight);
        shapeRenderer.end();
        
        batch.begin();
    }
    
    /**
     * フォントを取得します。
     */
    public BitmapFont getFont() {
        return font;
    }
    
    /**
     * 画面幅を取得します。
     */
    public int getScreenWidth() {
        return screenWidth;
    }
    
    /**
     * 画面高さを取得します。
     */
    public int getScreenHeight() {
        return screenHeight;
    }
    
    /**
     * ゲームガイドボタンを取得します。
     * @return ゲームガイドボタン（存在しない場合はnull）
     */
    public Button getGuideButton() {
        return guideButton;
    }
    
}
