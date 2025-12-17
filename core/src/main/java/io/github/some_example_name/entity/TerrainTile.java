package io.github.some_example_name.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 地形タイルを表すクラス。
 */
public class TerrainTile {
    // 地形タイプ
    public enum TerrainType {
        GRASS,      // 草
        DIRT,       // 土
        SAND,       // 砂
        WATER,      // 水
        STONE,      // 岩
        FOREST      // 森
    }
    
    private int tileX;
    private int tileY;
    private TerrainType terrainType;
    
    public TerrainTile(int tileX, int tileY, TerrainType terrainType) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.terrainType = terrainType;
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
        switch (terrainType) {
            case GRASS:
                return new Color(0.3f, 0.6f, 0.2f, 1f); // 緑色
            case DIRT:
                return new Color(0.5f, 0.4f, 0.3f, 1f); // 茶色
            case SAND:
                return new Color(0.9f, 0.85f, 0.7f, 1f); // 砂色
            case WATER:
                return new Color(0.2f, 0.4f, 0.7f, 1f); // 青色
            case STONE:
                return new Color(0.5f, 0.5f, 0.5f, 1f); // 灰色
            case FOREST:
                return new Color(0.2f, 0.5f, 0.15f, 1f); // 濃い緑
            default:
                return Color.WHITE;
        }
    }
    
    /**
     * 地形タイプに応じた装飾を描画します。
     */
    private void renderDecoration(ShapeRenderer shapeRenderer, float pixelX, float pixelY) {
        switch (terrainType) {
            case GRASS:
                // 草の小さな点を描画
                shapeRenderer.setColor(new Color(0.2f, 0.5f, 0.1f, 0.5f));
                for (int i = 0; i < 3; i++) {
                    float x = pixelX + Player.TILE_SIZE * (0.2f + i * 0.3f);
                    float y = pixelY + Player.TILE_SIZE * (0.3f + (i % 2) * 0.4f);
                    shapeRenderer.circle(x, y, Player.TILE_SIZE * 0.05f);
                }
                break;
            case FOREST:
                // 木を描画
                shapeRenderer.setColor(new Color(0.3f, 0.2f, 0.1f, 1f)); // 茶色（幹）
                float trunkX = pixelX + Player.TILE_SIZE * 0.45f;
                float trunkY = pixelY + Player.TILE_SIZE * 0.3f;
                shapeRenderer.rect(trunkX, trunkY, Player.TILE_SIZE * 0.1f, Player.TILE_SIZE * 0.4f);
                
                shapeRenderer.setColor(new Color(0.1f, 0.4f, 0.1f, 1f)); // 濃い緑（葉）
                float leafX = pixelX + Player.TILE_SIZE * 0.3f;
                float leafY = pixelY + Player.TILE_SIZE * 0.5f;
                shapeRenderer.circle(leafX, leafY, Player.TILE_SIZE * 0.25f);
                break;
            case STONE:
                // 岩の模様を描画
                shapeRenderer.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
                shapeRenderer.circle(pixelX + Player.TILE_SIZE * 0.5f, pixelY + Player.TILE_SIZE * 0.5f, Player.TILE_SIZE * 0.2f);
                break;
            case WATER:
                // 水の波紋を描画
                shapeRenderer.setColor(new Color(0.3f, 0.5f, 0.8f, 0.5f));
                for (int i = 0; i < 2; i++) {
                    float waveX = pixelX + Player.TILE_SIZE * (0.2f + i * 0.6f);
                    float waveY = pixelY + Player.TILE_SIZE * 0.5f;
                    shapeRenderer.circle(waveX, waveY, Player.TILE_SIZE * 0.15f);
                }
                break;
            case SAND:
                // 砂の小さな点を描画
                shapeRenderer.setColor(new Color(0.8f, 0.75f, 0.6f, 0.6f));
                for (int i = 0; i < 4; i++) {
                    float x = pixelX + Player.TILE_SIZE * (0.15f + (i % 2) * 0.5f);
                    float y = pixelY + Player.TILE_SIZE * (0.2f + (i / 2) * 0.6f);
                    shapeRenderer.circle(x, y, Player.TILE_SIZE * 0.03f);
                }
                break;
        }
    }
}
