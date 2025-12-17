package io.github.some_example_name.manager;

import io.github.some_example_name.entity.Player;
import io.github.some_example_name.entity.TerrainTile;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 地形を管理するクラス。
 */
public class TerrainManager {
    // 地形タイルのマップ（キー: "tileX,tileY"）
    private Map<String, TerrainTile> terrainTiles;
    
    // 生成済みのチャンクを記録（無限マップ用）
    private java.util.Set<String> generatedChunks;
    
    // ランダムジェネレーター（チャンクごとにシードを使用）
    private Random random;
    
    public TerrainManager() {
        this.terrainTiles = new HashMap<>();
        this.generatedChunks = new java.util.HashSet<>();
        this.random = new Random();
    }
    
    /**
     * 地形を更新します（カメラの視野範囲内の地形を生成）。
     * @param camera カメラ
     */
    public void update(OrthographicCamera camera) {
        generateTerrainInView(camera);
    }
    
    /**
     * カメラの視野範囲内の地形を生成します。
     * @param camera カメラ
     */
    private void generateTerrainInView(OrthographicCamera camera) {
        // チャンクサイズ（タイル単位）
        int chunkSize = 16;
        
        // カメラの視野範囲を計算（ズームを考慮）
        float actualViewportWidth = camera.viewportWidth * camera.zoom;
        float actualViewportHeight = camera.viewportHeight * camera.zoom;
        float cameraLeft = camera.position.x - actualViewportWidth / 2;
        float cameraRight = camera.position.x + actualViewportWidth / 2;
        float cameraBottom = camera.position.y - actualViewportHeight / 2;
        float cameraTop = camera.position.y + actualViewportHeight / 2;
        
        // マージンを追加
        float margin = Player.TILE_SIZE * chunkSize * 2;
        int startChunkX = (int)Math.floor((cameraLeft - margin) / (Player.TILE_SIZE * chunkSize));
        int endChunkX = (int)Math.ceil((cameraRight + margin) / (Player.TILE_SIZE * chunkSize));
        int startChunkY = (int)Math.floor((cameraBottom - margin) / (Player.TILE_SIZE * chunkSize));
        int endChunkY = (int)Math.ceil((cameraTop + margin) / (Player.TILE_SIZE * chunkSize));
        
        // 各チャンクをチェックして、未生成の場合は地形を生成
        for (int chunkX = startChunkX; chunkX <= endChunkX; chunkX++) {
            for (int chunkY = startChunkY; chunkY <= endChunkY; chunkY++) {
                String chunkKey = chunkX + "," + chunkY;
                if (!generatedChunks.contains(chunkKey)) {
                    generatedChunks.add(chunkKey);
                    generateChunkTerrain(chunkX, chunkY, chunkSize);
                }
            }
        }
    }
    
    /**
     * 指定されたチャンクの地形を生成します。
     * @param chunkX チャンクX座標
     * @param chunkY チャンクY座標
     * @param chunkSize チャンクサイズ（タイル単位）
     */
    private void generateChunkTerrain(int chunkX, int chunkY, int chunkSize) {
        // チャンクごとにシードを設定（同じチャンクは常に同じ地形を生成）
        long seed = (long)chunkX * 73856093L ^ (long)chunkY * 19349663L;
        Random chunkRandom = new Random(seed);
        
        int startTileX = chunkX * chunkSize;
        int startTileY = chunkY * chunkSize;
        
        for (int x = 0; x < chunkSize; x++) {
            for (int y = 0; y < chunkSize; y++) {
                int tileX = startTileX + x;
                int tileY = startTileY + y;
                String tileKey = tileX + "," + tileY;
                
                // 既に生成済みの場合はスキップ
                if (terrainTiles.containsKey(tileKey)) {
                    continue;
                }
                
                // 地形タイプを決定（ランダムだが、チャンクごとに一貫性がある）
                TerrainTile.TerrainType terrainType = determineTerrainType(tileX, tileY, chunkRandom);
                
                terrainTiles.put(tileKey, new TerrainTile(tileX, tileY, terrainType));
            }
        }
    }
    
    /**
     * タイル位置に基づいて地形タイプを決定します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @param random ランダムジェネレーター
     * @return 地形タイプ
     */
    private TerrainTile.TerrainType determineTerrainType(int tileX, int tileY, Random random) {
        // ノイズ関数を使用して地形を生成（簡易版）
        double noise = simpleNoise(tileX, tileY);
        
        // 水（低地）
        if (noise < 0.2) {
            return TerrainTile.TerrainType.WATER;
        }
        // 砂（水の近く）
        else if (noise < 0.3) {
            return TerrainTile.TerrainType.SAND;
        }
        // 森（中程度の高さ）
        else if (noise > 0.7 && random.nextDouble() < 0.3) {
            return TerrainTile.TerrainType.FOREST;
        }
        // 岩（高い場所）
        else if (noise > 0.8) {
            return TerrainTile.TerrainType.STONE;
        }
        // 土（ランダム）
        else if (random.nextDouble() < 0.2) {
            return TerrainTile.TerrainType.DIRT;
        }
        // デフォルトは草
        else {
            return TerrainTile.TerrainType.GRASS;
        }
    }
    
    /**
     * 簡易ノイズ関数（パーリンノイズの簡易版）。
     * @param x X座標
     * @param y Y座標
     * @return 0.0から1.0の間の値
     */
    private double simpleNoise(int x, int y) {
        // シンプルなハッシュベースのノイズ
        long seed = (long)x * 73856093L ^ (long)y * 19349663L;
        Random noiseRandom = new Random(seed);
        
        // 複数のオクターブを合成
        double value = 0.0;
        double amplitude = 1.0;
        double frequency = 0.1;
        double maxValue = 0.0;
        
        for (int i = 0; i < 3; i++) {
            double sample = noiseRandom.nextDouble() * amplitude;
            value += sample;
            maxValue += amplitude;
            amplitude *= 0.5;
            frequency *= 2.0;
        }
        
        return value / maxValue;
    }
    
    /**
     * すべての地形を描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     * @param camera カメラ（視野範囲内の地形のみ描画）
     */
    public void render(ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        // カメラの視野範囲を計算
        float actualViewportWidth = camera.viewportWidth * camera.zoom;
        float actualViewportHeight = camera.viewportHeight * camera.zoom;
        float cameraLeft = camera.position.x - actualViewportWidth / 2;
        float cameraRight = camera.position.x + actualViewportWidth / 2;
        float cameraBottom = camera.position.y - actualViewportHeight / 2;
        float cameraTop = camera.position.y + actualViewportHeight / 2;
        
        // マージンを追加
        float margin = Player.TILE_SIZE * 2;
        int startTileX = (int)Math.floor((cameraLeft - margin) / Player.TILE_SIZE);
        int endTileX = (int)Math.ceil((cameraRight + margin) / Player.TILE_SIZE);
        int startTileY = (int)Math.floor((cameraBottom - margin) / Player.TILE_SIZE);
        int endTileY = (int)Math.ceil((cameraTop + margin) / Player.TILE_SIZE);
        
        // 視野範囲内の地形のみ描画
        for (int x = startTileX; x <= endTileX; x++) {
            for (int y = startTileY; y <= endTileY; y++) {
                String tileKey = x + "," + y;
                TerrainTile tile = terrainTiles.get(tileKey);
                if (tile != null) {
                    tile.render(shapeRenderer);
                }
            }
        }
    }
    
    /**
     * すべての地形タイルを返します（セーブ用）。
     */
    public Map<String, TerrainTile> getTerrainTiles() {
        return terrainTiles;
    }
    
    /**
     * 地形タイルを設定します（ロード用）。
     */
    public void setTerrainTiles(Map<String, TerrainTile> terrainTiles) {
        this.terrainTiles = terrainTiles != null ? terrainTiles : new HashMap<>();
    }
    
    /**
     * 生成済みチャンクのセットを返します（セーブ用）。
     */
    public java.util.Set<String> getGeneratedChunks() {
        return generatedChunks;
    }
    
    /**
     * 生成済みチャンクのセットを設定します（ロード用）。
     */
    public void setGeneratedChunks(java.util.Set<String> chunks) {
        this.generatedChunks = chunks != null ? chunks : new java.util.HashSet<>();
    }
}
