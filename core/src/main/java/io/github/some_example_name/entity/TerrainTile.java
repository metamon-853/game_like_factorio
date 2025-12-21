package io.github.some_example_name.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.some_example_name.manager.TileDataLoader;

/**
 * 地形タイルを表すクラス。
 */
public class TerrainTile {
    // 地形タイプ
    public enum TerrainType {
        GRASS,          // 草
        DIRT,           // 土
        SAND,           // 砂
        WATER,          // 水
        STONE,          // 岩
        FOREST,         // 森
        PADDY,          // 田（水田）
        FARMLAND,       // 畑
        MARSH,          // 湿地
        DRAINED_MARSH,  // 排水後湿地
        WATER_CHANNEL   // 水路
    }
    
    private int tileX;
    private int tileY;
    private TerrainType terrainType;
    
    // 水路（CHANNEL）の通水状態（WATER_CHANNELの場合のみ使用）
    private boolean isWatered;
    
    public TerrainTile(int tileX, int tileY, TerrainType terrainType) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.terrainType = terrainType;
        this.isWatered = false; // デフォルトは非通水
    }
    
    public int getTileX() {
        return tileX;
    }
    
    public int getTileY() {
        return tileY;
    }
    
    public TerrainType getTerrainType() {
        return terrainType;
    }
    
    /**
     * 水路の通水状態を取得します（WATER_CHANNELの場合のみ有効）。
     * @return 通水している場合true
     */
    public boolean isWatered() {
        return isWatered;
    }
    
    /**
     * 水路の通水状態を設定します（WATER_CHANNELの場合のみ有効）。
     * @param watered 通水状態
     */
    public void setWatered(boolean watered) {
        this.isWatered = watered;
    }
    
    /**
     * 地形タイルを描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     */
    public void render(ShapeRenderer shapeRenderer) {
        float pixelX = tileX * Player.TILE_SIZE;
        float pixelY = tileY * Player.TILE_SIZE;
        
        // 地形タイプに応じた色を設定
        Color terrainColor = getTerrainColor();
        shapeRenderer.setColor(terrainColor);
        shapeRenderer.rect(pixelX, pixelY, Player.TILE_SIZE, Player.TILE_SIZE);
        
        // 地形タイプに応じた装飾を描画
        renderDecoration(shapeRenderer, pixelX, pixelY);
    }
    
    /**
     * 地形タイプに応じた色を返します。
     */
    private Color getTerrainColor() {
        TileDataLoader loader = TileDataLoader.getInstance();
        TileData tileData = loader.getTileData(terrainType);
        if (tileData != null) {
            return tileData.getColor();
        }
        // フォールバック（デフォルト値）
        return Color.WHITE;
    }
    
    /**
     * 地形タイプに応じた装飾を描画します。
     */
    private void renderDecoration(ShapeRenderer shapeRenderer, float pixelX, float pixelY) {
        TileDataLoader loader = TileDataLoader.getInstance();
        TileData tileData = loader.getTileData(terrainType);
        if (tileData == null) {
            return;
        }
        
        TileData.DecorationType decorationType = tileData.getDecorationType();
        
        switch (decorationType) {
            case GRASS_DOTS:
                // 草の小さな点を描画
                shapeRenderer.setColor(tileData.getDecorationColor());
                int grassCount = tileData.getDecorationCount();
                for (int i = 0; i < grassCount; i++) {
                    float x = pixelX + Player.TILE_SIZE * (0.2f + i * 0.3f);
                    float y = pixelY + Player.TILE_SIZE * (0.3f + (i % 2) * 0.4f);
                    shapeRenderer.circle(x, y, Player.TILE_SIZE * 0.05f);
                }
                break;
            case TREE:
                // 木を描画
                shapeRenderer.setColor(tileData.getTrunkColor());
                float trunkX = pixelX + Player.TILE_SIZE * 0.45f;
                float trunkY = pixelY + Player.TILE_SIZE * 0.3f;
                shapeRenderer.rect(trunkX, trunkY, Player.TILE_SIZE * 0.1f, Player.TILE_SIZE * 0.4f);
                
                shapeRenderer.setColor(tileData.getLeafColor());
                float leafX = pixelX + Player.TILE_SIZE * 0.3f;
                float leafY = pixelY + Player.TILE_SIZE * 0.5f;
                shapeRenderer.circle(leafX, leafY, Player.TILE_SIZE * 0.25f);
                break;
            case STONE_PATTERN:
                // 岩の模様を描画
                shapeRenderer.setColor(tileData.getDecorationColor());
                shapeRenderer.circle(pixelX + Player.TILE_SIZE * 0.5f, pixelY + Player.TILE_SIZE * 0.5f, Player.TILE_SIZE * 0.2f);
                break;
            case WATER_WAVES:
                // 水の波紋を描画
                shapeRenderer.setColor(tileData.getDecorationColor());
                int waveCount = tileData.getDecorationCount();
                for (int i = 0; i < waveCount; i++) {
                    float waveX = pixelX + Player.TILE_SIZE * (0.2f + i * 0.6f);
                    float waveY = pixelY + Player.TILE_SIZE * 0.5f;
                    shapeRenderer.circle(waveX, waveY, Player.TILE_SIZE * 0.15f);
                }
                break;
            case SAND_DOTS:
                // 砂の小さな点を描画
                shapeRenderer.setColor(tileData.getDecorationColor());
                int sandCount = tileData.getDecorationCount();
                for (int i = 0; i < sandCount; i++) {
                    float x = pixelX + Player.TILE_SIZE * (0.15f + (i % 2) * 0.5f);
                    float y = pixelY + Player.TILE_SIZE * (0.2f + (i / 2) * 0.6f);
                    shapeRenderer.circle(x, y, Player.TILE_SIZE * 0.03f);
                }
                break;
            case FURROWS:
                // 畝（うね）を描画（平行線）
                shapeRenderer.setColor(tileData.getDecorationColor());
                int furrowCount = tileData.getDecorationCount();
                for (int i = 0; i < furrowCount; i++) {
                    float y = pixelY + Player.TILE_SIZE * (0.2f + i * (0.6f / (furrowCount - 1)));
                    shapeRenderer.rect(pixelX + Player.TILE_SIZE * 0.1f, y, 
                                     Player.TILE_SIZE * 0.8f, Player.TILE_SIZE * 0.05f);
                }
                break;
            case NONE:
            default:
                // 装飾なし
                break;
        }
    }
}
