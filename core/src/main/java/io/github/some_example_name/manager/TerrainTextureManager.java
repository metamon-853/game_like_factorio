package io.github.some_example_name.manager;

import io.github.some_example_name.entity.TerrainTile;
import io.github.some_example_name.entity.Player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

/**
 * 地形タイル用のテクスチャを管理するクラス。
 */
public class TerrainTextureManager implements Disposable {
    private Map<TerrainTile.TerrainType, Texture> textures;
    
    public TerrainTextureManager() {
        textures = new HashMap<>();
        generateTextures();
    }
    
    /**
     * 各地形タイプのテクスチャを生成します。
     */
    private void generateTextures() {
        int tileSize = Player.TILE_SIZE;
        
        for (TerrainTile.TerrainType type : TerrainTile.TerrainType.values()) {
            Pixmap pixmap = createTerrainPixmap(type, tileSize);
            Texture texture = new Texture(pixmap);
            textures.put(type, texture);
            pixmap.dispose();
        }
    }
    
    /**
     * 地形タイプに応じたPixmapを生成します。
     */
    private Pixmap createTerrainPixmap(TerrainTile.TerrainType type, int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        
        switch (type) {
            case GRASS:
                createGrassTexture(pixmap, size);
                break;
            case DIRT:
                createDirtTexture(pixmap, size);
                break;
            case SAND:
                createSandTexture(pixmap, size);
                break;
            case WATER:
                createWaterTexture(pixmap, size);
                break;
            case STONE:
                createStoneTexture(pixmap, size);
                break;
            case FOREST:
                createForestTexture(pixmap, size);
                break;
        }
        
        return pixmap;
    }
    
    /**
     * 草のテクスチャを生成します。
     */
    private void createGrassTexture(Pixmap pixmap, int size) {
        // ベース色（緑）
        Color baseColor = new Color(0.3f, 0.6f, 0.2f, 1f);
        fillPixmap(pixmap, baseColor);
        
        // 草の模様を追加
        for (int i = 0; i < 8; i++) {
            int x = (int)(size * (0.1f + (i % 3) * 0.3f));
            int y = (int)(size * (0.2f + (i / 3) * 0.4f));
            int radius = size / 12;
            drawCircle(pixmap, x, y, radius, new Color(0.2f, 0.5f, 0.1f, 0.6f));
        }
        
        // 明るいハイライト
        for (int i = 0; i < 3; i++) {
            int x = (int)(size * (0.2f + i * 0.3f));
            int y = (int)(size * (0.3f + (i % 2) * 0.4f));
            int radius = size / 20;
            drawCircle(pixmap, x, y, radius, new Color(0.4f, 0.7f, 0.3f, 0.8f));
        }
    }
    
    /**
     * 土のテクスチャを生成します。
     */
    private void createDirtTexture(Pixmap pixmap, int size) {
        // ベース色（茶色）
        Color baseColor = new Color(0.5f, 0.4f, 0.3f, 1f);
        fillPixmap(pixmap, baseColor);
        
        // 土の質感を追加（ランダムな点）
        for (int i = 0; i < 15; i++) {
            int x = (int)(Math.random() * size);
            int y = (int)(Math.random() * size);
            int radius = size / 25;
            float brightness = 0.7f + (float)(Math.random() * 0.3f);
            Color dirtColor = new Color(
                baseColor.r * brightness,
                baseColor.g * brightness,
                baseColor.b * brightness,
                1f
            );
            drawCircle(pixmap, x, y, radius, dirtColor);
        }
    }
    
    /**
     * 砂のテクスチャを生成します。
     */
    private void createSandTexture(Pixmap pixmap, int size) {
        // ベース色（砂色）
        Color baseColor = new Color(0.9f, 0.85f, 0.7f, 1f);
        fillPixmap(pixmap, baseColor);
        
        // 砂の粒を追加
        for (int i = 0; i < 20; i++) {
            int x = (int)(Math.random() * size);
            int y = (int)(Math.random() * size);
            int radius = size / 30;
            float brightness = 0.8f + (float)(Math.random() * 0.2f);
            Color sandColor = new Color(
                baseColor.r * brightness,
                baseColor.g * brightness,
                baseColor.b * brightness,
                0.8f
            );
            drawCircle(pixmap, x, y, radius, sandColor);
        }
    }
    
    /**
     * 水のテクスチャを生成します。
     */
    private void createWaterTexture(Pixmap pixmap, int size) {
        // ベース色（青）
        Color baseColor = new Color(0.2f, 0.4f, 0.7f, 1f);
        fillPixmap(pixmap, baseColor);
        
        // 波紋を追加
        for (int i = 0; i < 3; i++) {
            int centerX = (int)(size * (0.2f + i * 0.3f));
            int centerY = size / 2;
            int radius = size / 6;
            drawCircle(pixmap, centerX, centerY, radius, new Color(0.3f, 0.5f, 0.8f, 0.5f));
        }
        
        // ハイライト（光の反射）
        for (int i = 0; i < 2; i++) {
            int x = (int)(size * (0.3f + i * 0.4f));
            int y = (int)(size * (0.2f + i * 0.3f));
            int radius = size / 15;
            drawCircle(pixmap, x, y, radius, new Color(0.5f, 0.6f, 0.9f, 0.7f));
        }
    }
    
    /**
     * 岩のテクスチャを生成します。
     */
    private void createStoneTexture(Pixmap pixmap, int size) {
        // ベース色（灰色）
        Color baseColor = new Color(0.5f, 0.5f, 0.5f, 1f);
        fillPixmap(pixmap, baseColor);
        
        // 岩の模様を追加
        int centerX = size / 2;
        int centerY = size / 2;
        int radius = size / 4;
        drawCircle(pixmap, centerX, centerY, radius, new Color(0.4f, 0.4f, 0.4f, 1f));
        
        // ハイライト
        drawCircle(pixmap, centerX - radius / 3, centerY - radius / 3, radius / 3, 
                   new Color(0.6f, 0.6f, 0.6f, 0.8f));
        
        // 影
        drawCircle(pixmap, centerX + radius / 3, centerY + radius / 3, radius / 3, 
                   new Color(0.3f, 0.3f, 0.3f, 0.8f));
    }
    
    /**
     * 森のテクスチャを生成します。
     */
    private void createForestTexture(Pixmap pixmap, int size) {
        // ベース色（濃い緑）
        Color baseColor = new Color(0.2f, 0.5f, 0.15f, 1f);
        fillPixmap(pixmap, baseColor);
        
        // 木の幹
        int trunkX = (int)(size * 0.45f);
        int trunkY = (int)(size * 0.3f);
        int trunkWidth = size / 10;
        int trunkHeight = (int)(size * 0.4f);
        drawRect(pixmap, trunkX, trunkY, trunkWidth, trunkHeight, 
                 new Color(0.3f, 0.2f, 0.1f, 1f));
        
        // 葉（円形）
        int leafX = (int)(size * 0.3f);
        int leafY = (int)(size * 0.5f);
        int leafRadius = size / 4;
        drawCircle(pixmap, leafX, leafY, leafRadius, new Color(0.1f, 0.4f, 0.1f, 1f));
        
        // 追加の葉
        drawCircle(pixmap, leafX + size / 6, leafY - size / 8, leafRadius / 2, 
                   new Color(0.15f, 0.45f, 0.12f, 1f));
    }
    
    /**
     * Pixmapを指定された色で塗りつぶします。
     */
    private void fillPixmap(Pixmap pixmap, Color color) {
        pixmap.setColor(color.r, color.g, color.b, color.a);
        pixmap.fill();
    }
    
    /**
     * 円を描画します。
     */
    private void drawCircle(Pixmap pixmap, int x, int y, int radius, Color color) {
        pixmap.setColor(color.r, color.g, color.b, color.a);
        pixmap.fillCircle(x, y, radius);
    }
    
    /**
     * 矩形を描画します。
     */
    private void drawRect(Pixmap pixmap, int x, int y, int width, int height, Color color) {
        pixmap.setColor(color.r, color.g, color.b, color.a);
        pixmap.fillRectangle(x, y, width, height);
    }
    
    /**
     * 指定された地形タイプのテクスチャを取得します。
     */
    public Texture getTexture(TerrainTile.TerrainType type) {
        return textures.get(type);
    }
    
    @Override
    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
    }
}
