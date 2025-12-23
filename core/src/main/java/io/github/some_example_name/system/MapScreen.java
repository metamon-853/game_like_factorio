package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.some_example_name.entity.Player;
import io.github.some_example_name.entity.TerrainTile;
import io.github.some_example_name.manager.TerrainManager;

/**
 * マップ画面を管理するクラス。
 * Mキーで開閉できるマップ画面を管理します。
 */
public class MapScreen {
    private boolean isActive;
    
    // マップ表示の設定
    private static final int MAP_TILE_SIZE_PIXELS = 2; // マップ上の1タイルのサイズ（ピクセル）
    
    // マップ表示領域
    private float mapX, mapY, mapWidth, mapHeight;
    
    // マップの中心位置（ワールド座標）
    private float mapCenterX, mapCenterY;
    
    public MapScreen() {
        this.isActive = false;
        this.mapCenterX = 0f;
        this.mapCenterY = 0f;
    }
    
    /**
     * マップ画面を開始します。
     */
    public void start() {
        this.isActive = true;
        Gdx.app.log("MapScreen", "マップ画面を開始しました");
    }
    
    /**
     * マップ画面を終了します。
     */
    public void end() {
        this.isActive = false;
    }
    
    /**
     * マップ画面がアクティブかどうかを返します。
     * @return アクティブな場合true
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * マップ画面を描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     * @param batch SpriteBatchインスタンス
     * @param font フォント
     * @param uiCamera UI用カメラ
     * @param screenWidth 画面幅
     * @param screenHeight 画面高さ
     * @param terrainManager 地形マネージャー
     * @param player プレイヤー
     */
    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                      OrthographicCamera uiCamera, int screenWidth, int screenHeight,
                      TerrainManager terrainManager, Player player) {
        if (!isActive) {
            return;
        }
        
        // UIカメラのプロジェクション行列を設定
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);
        
        // マップ表示領域を計算（画面の80%を使用）
        mapWidth = screenWidth * 0.8f;
        mapHeight = screenHeight * 0.8f;
        mapX = (screenWidth - mapWidth) / 2;
        mapY = (screenHeight - mapHeight) / 2;
        
        // プレイヤーの位置をマップの中心に設定
        if (player != null) {
            mapCenterX = player.getPixelX();
            mapCenterY = player.getPixelY();
        }
        
        // 半透明の黒背景を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.8f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        
        // マップの背景を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 1f);
        shapeRenderer.rect(mapX, mapY, mapWidth, mapHeight);
        shapeRenderer.end();
        
        // マップの枠を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(mapX, mapY, mapWidth, mapHeight);
        shapeRenderer.end();
        
        // マップの地形を描画
        if (terrainManager != null) {
            renderMapTerrain(shapeRenderer, terrainManager);
        }
        
        // プレイヤーの位置を描画
        if (player != null) {
            renderPlayerPosition(shapeRenderer, player);
        }
        
        // タイトルと説明を描画
        batch.begin();
        font.setColor(Color.WHITE);
        float originalScale = font.getData().scaleX;
        font.getData().setScale(2.0f);
        
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(font, "マップ");
        float titleX = (screenWidth - layout.width) / 2;
        float titleY = mapY + mapHeight + layout.height + 20;
        font.draw(batch, "マップ", titleX, titleY);
        
        font.getData().setScale(1.0f);
        layout.setText(font, "Mキーで閉じる");
        float hintX = (screenWidth - layout.width) / 2;
        float hintY = mapY - layout.height - 10;
        font.draw(batch, "Mキーで閉じる", hintX, hintY);
        
        batch.end();
        
        // フォントスケールを復元
        font.getData().setScale(originalScale);
    }
    
    /**
     * マップの地形を描画します。
     */
    private void renderMapTerrain(ShapeRenderer shapeRenderer, TerrainManager terrainManager) {
        // マップに表示する範囲を計算
        float worldToMapScale = MAP_TILE_SIZE_PIXELS / (float)Player.MAP_TILE_SIZE;
        int startTileX = (int)Math.floor((mapCenterX - mapWidth / 2 / worldToMapScale) / Player.MAP_TILE_SIZE);
        int endTileX = (int)Math.ceil((mapCenterX + mapWidth / 2 / worldToMapScale) / Player.MAP_TILE_SIZE);
        int startTileY = (int)Math.floor((mapCenterY - mapHeight / 2 / worldToMapScale) / Player.MAP_TILE_SIZE);
        int endTileY = (int)Math.ceil((mapCenterY + mapHeight / 2 / worldToMapScale) / Player.MAP_TILE_SIZE);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int x = startTileX; x <= endTileX; x++) {
            for (int y = startTileY; y <= endTileY; y++) {
                // 探索済みのタイルのみ表示
                if (!terrainManager.isExplored(x, y)) {
                    continue;
                }
                
                TerrainTile tile = terrainManager.getTerrainTile(x, y);
                if (tile == null) {
                    continue;
                }
                
                // ワールド座標をマップ座標に変換
                float worldX = x * Player.MAP_TILE_SIZE;
                float worldY = y * Player.MAP_TILE_SIZE;
                float mapPixelX = mapX + mapWidth / 2 + (worldX - mapCenterX) * worldToMapScale;
                float mapPixelY = mapY + mapHeight / 2 + (worldY - mapCenterY) * worldToMapScale;
                
                // マップ表示領域内かチェック
                if (mapPixelX < mapX || mapPixelX >= mapX + mapWidth ||
                    mapPixelY < mapY || mapPixelY >= mapY + mapHeight) {
                    continue;
                }
                
                // 地形タイプに応じた色を設定
                Color tileColor = getTerrainColor(tile.getTerrainType());
                shapeRenderer.setColor(tileColor);
                shapeRenderer.rect(mapPixelX, mapPixelY, MAP_TILE_SIZE_PIXELS, MAP_TILE_SIZE_PIXELS);
            }
        }
        
        shapeRenderer.end();
    }
    
    /**
     * 地形タイプに応じた色を返します。
     */
    private Color getTerrainColor(TerrainTile.TerrainType terrainType) {
        switch (terrainType) {
            case GRASS:
                return new Color(0.4f, 0.7f, 0.3f, 1f); // 緑
            case DIRT:
                return new Color(0.6f, 0.5f, 0.3f, 1f); // 茶色
            case SAND:
                return new Color(0.9f, 0.9f, 0.6f, 1f); // 黄色
            case WATER:
                return new Color(0.2f, 0.4f, 0.8f, 1f); // 青
            case STONE:
                return new Color(0.5f, 0.5f, 0.5f, 1f); // 灰色
            case FOREST:
                return new Color(0.2f, 0.5f, 0.2f, 1f); // 濃い緑
            case PADDY:
                return new Color(0.3f, 0.6f, 0.4f, 1f); // 水田の緑
            case FARMLAND:
                return new Color(0.7f, 0.6f, 0.4f, 1f); // 畑の茶色
            case MARSH:
                return new Color(0.4f, 0.5f, 0.4f, 1f); // 湿地の緑
            case DRAINED_MARSH:
                return new Color(0.5f, 0.6f, 0.5f, 1f); // 排水後湿地
            case WATER_CHANNEL:
                return new Color(0.3f, 0.5f, 0.7f, 1f); // 水路の青
            case BARREN:
                return new Color(0.4f, 0.4f, 0.4f, 1f); // 荒地の灰色
            default:
                return Color.GRAY;
        }
    }
    
    /**
     * プレイヤーの位置を描画します。
     */
    private void renderPlayerPosition(ShapeRenderer shapeRenderer, Player player) {
        float worldToMapScale = MAP_TILE_SIZE_PIXELS / (float)Player.MAP_TILE_SIZE;
        float playerWorldX = player.getPixelX();
        float playerWorldY = player.getPixelY();
        float mapPixelX = mapX + mapWidth / 2 + (playerWorldX - mapCenterX) * worldToMapScale;
        float mapPixelY = mapY + mapHeight / 2 + (playerWorldY - mapCenterY) * worldToMapScale;
        
        // マップ表示領域内かチェック
        if (mapPixelX < mapX || mapPixelX >= mapX + mapWidth ||
            mapPixelY < mapY || mapPixelY >= mapY + mapHeight) {
            return;
        }
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        // プレイヤーを小さな円で表示
        float playerSize = MAP_TILE_SIZE_PIXELS * 1.5f;
        shapeRenderer.circle(mapPixelX, mapPixelY, playerSize / 2);
        shapeRenderer.end();
    }
    
    /**
     * 入力処理を行います（Mキーでマップを閉じる）。
     * @return マップを閉じる入力があった場合true
     */
    public boolean handleInput() {
        if (!isActive) {
            return false;
        }
        
        // Mキーでマップを閉じる
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            end();
            return true;
        }
        
        return false;
    }
}

