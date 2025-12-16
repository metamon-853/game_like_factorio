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
     */
    public void drawUI(ItemManager itemManager) {
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        font.getData().setScale(2.5f);
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
        font.draw(batch, civText, leftX, topY - civLayout.height - 10);
        
        // インベントリ操作の説明を表示
        float rightX = screenWidth - padding;
        String hintText = "Eでインベントリ";
        GlyphLayout hintLayout = new GlyphLayout(font, hintText);
        float hintX = rightX - hintLayout.width;
        font.draw(batch, hintText, hintX, topY);
        
        font.getData().setScale(2.0f);
        batch.end();
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
        batch.end();
        
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
        
        batch.begin();
        font.getData().setScale(1.8f);
        font.setColor(isHovered ? new Color(0.9f, 0.9f, 1.0f, 1f) : Color.WHITE);
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = x + (width - layout.width) / 2;
        float textY = y + height / 2 + layout.height / 2;
        font.draw(batch, text, textX, textY);
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
    
}
