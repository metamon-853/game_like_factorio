package io.github.some_example_name.manager;

import io.github.some_example_name.entity.Player;
import io.github.some_example_name.entity.TerrainTile;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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
    
    // 探索済みのタイルを記録（マップ表示用）
    private java.util.Set<String> exploredTiles;
    
    // テクスチャマネージャー
    private TerrainTextureManager textureManager;
    
    // アニメーション用の時間変数
    private float animationTime;
    
    public TerrainManager() {
        this.terrainTiles = new HashMap<>();
        this.generatedChunks = new java.util.HashSet<>();
        this.exploredTiles = new java.util.HashSet<>();
        this.textureManager = new TerrainTextureManager();
        this.animationTime = 0f;
    }
    
    /**
     * 地形を更新します（カメラの視野範囲内の地形を生成）。
     * @param camera カメラ
     * @param playerTileX プレイヤーのマップ升X座標（探索済みエリア記録用）
     * @param playerTileY プレイヤーのマップ升Y座標（探索済みエリア記録用）
     * @param deltaTime 前フレームからの経過時間（秒）（アニメーション用）
     */
    public void update(OrthographicCamera camera, int playerTileX, int playerTileY, float deltaTime) {
        generateTerrainInView(camera);
        markExplored(playerTileX, playerTileY);
        // アニメーション時間を更新
        animationTime += deltaTime;
    }
    
    /**
     * プレイヤーの周囲を探索済みとしてマークします。
     * @param playerTileX プレイヤーのマップ升X座標
     * @param playerTileY プレイヤーのマップ升Y座標
     */
    private void markExplored(int playerTileX, int playerTileY) {
        // プレイヤーの周囲のタイルを探索済みとしてマーク（視野範囲を考慮）
        int exploreRadius = 5; // 探索半径（マップ升単位）
        for (int x = playerTileX - exploreRadius; x <= playerTileX + exploreRadius; x++) {
            for (int y = playerTileY - exploreRadius; y <= playerTileY + exploreRadius; y++) {
                // 円形の範囲内のみ探索済みとしてマーク
                int dx = x - playerTileX;
                int dy = y - playerTileY;
                if (dx * dx + dy * dy <= exploreRadius * exploreRadius) {
                    String tileKey = x + "," + y;
                    exploredTiles.add(tileKey);
                }
            }
        }
    }
    
    /**
     * 指定されたタイルが探索済みかどうかを判定します。
     * @param tileX マップ升X座標
     * @param tileY マップ升Y座標
     * @return 探索済みの場合true
     */
    public boolean isExplored(int tileX, int tileY) {
        String tileKey = tileX + "," + tileY;
        return exploredTiles.contains(tileKey);
    }
    
    /**
     * 探索済みタイルのセットを返します（セーブ用）。
     */
    public java.util.Set<String> getExploredTiles() {
        return exploredTiles;
    }
    
    /**
     * 探索済みタイルのセットを設定します（ロード用）。
     */
    public void setExploredTiles(java.util.Set<String> exploredTiles) {
        this.exploredTiles = exploredTiles != null ? exploredTiles : new java.util.HashSet<>();
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
                
                // 地形タイプを決定（エリアベースで連続した地形を生成）
                TerrainTile.TerrainType terrainType = determineTerrainType(tileX, tileY, null);
                
                terrainTiles.put(tileKey, new TerrainTile(tileX, tileY, terrainType));
            }
        }
    }
    
    /**
     * タイル位置に基づいて地形タイプを決定します。
     * エリアベースの生成で、連続した地形エリアを作成します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @param random ランダムジェネレーター（使用しないが互換性のため残す）
     * @return 地形タイプ
     */
    private TerrainTile.TerrainType determineTerrainType(int tileX, int tileY, Random random) {
        // 高度ノイズ（地形の高低を決定）- 複数オクターブで滑らかに
        double heightNoise = multiOctaveNoise(tileX, tileY, 0.05);
        
        // 湿度ノイズ（水や森の分布を決定）- 異なるスケールで
        double moistureNoise = multiOctaveNoise(tileX, tileY, 0.08);
        
        // 地形タイプノイズ（特殊地形の分布を決定）- さらに異なるスケールで
        double terrainNoise = multiOctaveNoise(tileX, tileY, 0.03);
        
        // 高度に基づいて基本地形を決定
        if (heightNoise < 0.25) {
            // 低地：水エリア
            return TerrainTile.TerrainType.WATER;
        } else if (heightNoise < 0.35) {
            // 低地：砂浜エリア（水の近く）
            return TerrainTile.TerrainType.SAND;
        } else if (heightNoise > 0.75) {
            // 高地：岩エリア
            return TerrainTile.TerrainType.STONE;
        } else {
            // 中地帯：湿度と地形ノイズに基づいて決定
            // まず湿地をチェック（水場の近くに生成されるように優先度を上げる）
            if (heightNoise < 0.45 && moistureNoise > 0.6) {
                // 低地＋高湿度：湿地エリア（水場の近くに生成）
                return TerrainTile.TerrainType.MARSH;
            } else if (moistureNoise > 0.65 && terrainNoise > 0.4) {
                // 高湿度＋適度な地形ノイズ：森林エリア
                return TerrainTile.TerrainType.FOREST;
            } else if (moistureNoise < 0.35 || terrainNoise < 0.3) {
                // 低湿度または低地形ノイズ：土エリア
                return TerrainTile.TerrainType.DIRT;
            } else {
                // その他：草原エリア
                return TerrainTile.TerrainType.GRASS;
            }
        }
    }
    
    /**
     * 滑らかなノイズ関数（パーリンノイズ風）。
     * 連続したエリアを生成するために、周囲のタイルの値を補間します。
     * @param x X座標
     * @param y Y座標
     * @param scale スケール（小さいほど大きなエリア）
     * @return 0.0から1.0の間の値
     */
    private double smoothNoise(int x, int y, double scale) {
        // グリッドポイントのノイズ値を取得
        double fx = x * scale;
        double fy = y * scale;
        
        int x0 = (int)Math.floor(fx);
        int x1 = x0 + 1;
        int y0 = (int)Math.floor(fy);
        int y1 = y0 + 1;
        
        // 各グリッドポイントのノイズ値を計算
        double n00 = gridNoise(x0, y0);
        double n10 = gridNoise(x1, y0);
        double n01 = gridNoise(x0, y1);
        double n11 = gridNoise(x1, y1);
        
        // 補間のための重み
        double sx = fx - x0;
        double sy = fy - y0;
        
        // スムーズステップ関数で補間
        sx = sx * sx * (3.0 - 2.0 * sx);
        sy = sy * sy * (3.0 - 2.0 * sy);
        
        // 双線形補間
        double nx0 = n00 * (1.0 - sx) + n10 * sx;
        double nx1 = n01 * (1.0 - sx) + n11 * sx;
        double result = nx0 * (1.0 - sy) + nx1 * sy;
        
        return result;
    }
    
    /**
     * グリッドポイントでのノイズ値を計算します。
     * @param x グリッドX座標
     * @param y グリッドY座標
     * @return 0.0から1.0の間の値
     */
    private double gridNoise(int x, int y) {
        long seed = (long)x * 73856093L ^ (long)y * 19349663L;
        Random noiseRandom = new Random(seed);
        return noiseRandom.nextDouble();
    }
    
    /**
     * 複数オクターブのノイズを合成して、より自然な地形を生成します。
     * @param x X座標
     * @param y Y座標
     * @param scale 基本スケール
     * @return 0.0から1.0の間の値
     */
    private double multiOctaveNoise(int x, int y, double scale) {
        double value = 0.0;
        double amplitude = 1.0;
        double frequency = scale;
        double maxValue = 0.0;
        
        // 3つのオクターブを合成
        for (int i = 0; i < 3; i++) {
            value += smoothNoise(x, y, frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= 0.5;
            frequency *= 2.0;
        }
        
        return value / maxValue;
    }
    
    /**
     * すべての地形を描画します（SpriteBatchを使用）。
     * @param batch SpriteBatchインスタンス
     * @param camera カメラ（視野範囲内の地形のみ描画）
     */
    public void render(SpriteBatch batch, OrthographicCamera camera) {
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
                    float pixelX = tile.getTileX() * Player.TILE_SIZE;
                    float pixelY = tile.getTileY() * Player.TILE_SIZE;
                    Texture texture = textureManager.getTexture(tile.getTerrainType());
                    if (texture != null) {
                        // アニメーション効果を適用
                        renderAnimatedTile(batch, texture, tile.getTerrainType(), 
                                          pixelX, pixelY, x, y);
                    }
                }
            }
        }
    }
    
    /**
     * アニメーション効果を適用してタイルを描画します。
     * @param batch SpriteBatchインスタンス
     * @param texture テクスチャ
     * @param terrainType 地形タイプ
     * @param pixelX ピクセルX座標
     * @param pixelY ピクセルY座標
     * @param tileX タイルX座標（アニメーションの位相をずらすため）
     * @param tileY タイルY座標（アニメーションの位相をずらすため）
     */
    private void renderAnimatedTile(SpriteBatch batch, Texture texture, 
                                   TerrainTile.TerrainType terrainType,
                                   float pixelX, float pixelY, int tileX, int tileY) {
        float drawX = pixelX;
        float drawY = pixelY;
        
        // 地形タイプに応じてアニメーション効果を適用
        switch (terrainType) {
            case WATER:
            case PADDY:
            case WATER_CHANNEL:
            case MARSH:
                // 水場の波アニメーション（位置を微調整して揺れる効果）
                float waterSpeed = 0.5f; // 波の速度
                float waterAmplitude = 0.8f; // 波の振幅（ピクセル単位）
                float waterPhaseX = (float)(tileX * 0.3f + tileY * 0.2f); // 位相をずらす
                float waterPhaseY = (float)(tileX * 0.2f + tileY * 0.3f);
                
                // 波のような動き（sin波とcos波の組み合わせ）
                float offsetX = (float)Math.sin(animationTime * waterSpeed + waterPhaseX) * waterAmplitude;
                float offsetY = (float)Math.cos(animationTime * waterSpeed * 1.3f + waterPhaseY) * waterAmplitude;
                
                drawX = pixelX + offsetX;
                drawY = pixelY + offsetY;
                break;
                
            case FOREST:
            case GRASS:
                // 森や草の揺れアニメーション（風に揺れる感じ）
                float windSpeed = 0.8f; // 風の速度
                float windAmplitude = 0.6f; // 揺れの振幅（ピクセル単位）
                float windPhase = (float)(tileX * 0.4f + tileY * 0.3f); // 位相をずらす
                
                // 水平方向の揺れ（左右に揺れる）
                float windOffsetX = (float)Math.sin(animationTime * windSpeed + windPhase) * windAmplitude;
                // 垂直方向の揺れ（上下に揺れる、より小さく）
                float windOffsetY = (float)Math.cos(animationTime * windSpeed * 0.7f + windPhase) * windAmplitude * 0.5f;
                
                drawX = pixelX + windOffsetX;
                drawY = pixelY + windOffsetY;
                break;
                
            default:
                // アニメーションなし（通常の描画）
                break;
        }
        
        // テクスチャを描画
        batch.draw(texture, drawX, drawY, Player.TILE_SIZE, Player.TILE_SIZE);
    }
    
    /**
     * リソースを解放します。
     */
    public void dispose() {
        if (textureManager != null) {
            textureManager.dispose();
        }
    }
    
    /**
     * 指定されたマップ升座標の地形タイルを取得します。
     * @param tileX マップ升X座標
     * @param tileY マップ升Y座標
     * @return 地形タイル（存在しない場合はnull）
     */
    public TerrainTile getTerrainTile(int tileX, int tileY) {
        String tileKey = tileX + "," + tileY;
        return terrainTiles.get(tileKey);
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
    
    /**
     * 指定されたタイル位置が水辺（水タイルに隣接）かどうかを判定します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @return 水辺の場合true
     */
    public boolean isNearWater(int tileX, int tileY) {
        // 周囲8方向をチェック
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        
        for (int i = 0; i < dx.length; i++) {
            int checkX = tileX + dx[i];
            int checkY = tileY + dy[i];
            TerrainTile tile = getTerrainTile(checkX, checkY);
            if (tile != null && tile.getTerrainType() == TerrainTile.TerrainType.WATER) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 指定されたタイル位置の地形を変更します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @param newType 新しい地形タイプ
     * @return 変更に成功した場合true
     */
    public boolean changeTerrainType(int tileX, int tileY, TerrainTile.TerrainType newType) {
        String tileKey = tileX + "," + tileY;
        TerrainTile tile = terrainTiles.get(tileKey);
        
        if (tile == null) {
            // タイルが存在しない場合は新規作成
            tile = new TerrainTile(tileX, tileY, newType);
            terrainTiles.put(tileKey, tile);
        } else {
            // 既存のタイルのタイプを変更
            // TerrainTileクラスにsetterがない場合は、新しいインスタンスで置き換え
            tile = new TerrainTile(tileX, tileY, newType);
            terrainTiles.put(tileKey, tile);
        }
        
        // CHANNELが作成された場合、通水状態を更新
        if (newType == TerrainTile.TerrainType.WATER_CHANNEL) {
            updateChannelWateredStateAround(tileX, tileY);
        } else {
            // 他の地形に変更された場合も、周囲のCHANNELの通水状態を更新
            updateChannelWateredStateAround(tileX, tileY);
        }
        
        return true;
    }
    
    /**
     * 指定されたタイル位置が水源（WATER、通水CHANNEL、PADDY）に隣接しているかどうかを判定します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @return 水源に隣接している場合true
     */
    public boolean isNearWaterSource(int tileX, int tileY) {
        // 周囲8方向をチェック
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        
        for (int i = 0; i < dx.length; i++) {
            int checkX = tileX + dx[i];
            int checkY = tileY + dy[i];
            TerrainTile tile = getTerrainTile(checkX, checkY);
            if (tile != null) {
                TerrainTile.TerrainType type = tile.getTerrainType();
                if (type == TerrainTile.TerrainType.WATER ||
                    type == TerrainTile.TerrainType.PADDY) {
                    return true;
                }
                // 通水しているCHANNELも水源として扱う
                if (type == TerrainTile.TerrainType.WATER_CHANNEL && tile.isWatered()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 指定されたタイル位置が水路（WATER_CHANNEL）に接続されているかどうかを判定します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @return 水路に接続されている場合true
     */
    public boolean isConnectedToWaterChannel(int tileX, int tileY) {
        // 周囲4方向（上下左右）をチェック
        int[] dx = {-1, 0, 0, 1};
        int[] dy = {0, -1, 1, 0};
        
        for (int i = 0; i < dx.length; i++) {
            int checkX = tileX + dx[i];
            int checkY = tileY + dy[i];
            TerrainTile tile = getTerrainTile(checkX, checkY);
            if (tile != null && tile.getTerrainType() == TerrainTile.TerrainType.WATER_CHANNEL) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 指定された水路タイルが通水しているかどうかを判定します。
     * 隣接8方向にWATERまたは通水しているCHANNELがあれば通水と判定します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @return 通水している場合true
     */
    public boolean isChannelWatered(int tileX, int tileY) {
        TerrainTile tile = getTerrainTile(tileX, tileY);
        if (tile == null || tile.getTerrainType() != TerrainTile.TerrainType.WATER_CHANNEL) {
            return false;
        }
        
        // 周囲8方向をチェック
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        
        for (int i = 0; i < dx.length; i++) {
            int checkX = tileX + dx[i];
            int checkY = tileY + dy[i];
            TerrainTile neighborTile = getTerrainTile(checkX, checkY);
            if (neighborTile != null) {
                TerrainTile.TerrainType neighborType = neighborTile.getTerrainType();
                // WATERに隣接している場合は通水
                if (neighborType == TerrainTile.TerrainType.WATER) {
                    return true;
                }
                // 通水しているCHANNELに隣接している場合も通水
                if (neighborType == TerrainTile.TerrainType.WATER_CHANNEL && neighborTile.isWatered()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 指定された水路タイルの通水状態を更新します。
     * 地形変更時に呼び出して、通水状態を再計算します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     */
    public void updateChannelWateredState(int tileX, int tileY) {
        TerrainTile tile = getTerrainTile(tileX, tileY);
        if (tile == null || tile.getTerrainType() != TerrainTile.TerrainType.WATER_CHANNEL) {
            return;
        }
        
        boolean watered = isChannelWatered(tileX, tileY);
        tile.setWatered(watered);
    }
    
    /**
     * 指定された位置とその周囲の水路タイルの通水状態を更新します。
     * 地形変更時に呼び出して、影響を受ける水路の通水状態を再計算します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     */
    public void updateChannelWateredStateAround(int tileX, int tileY) {
        // 周囲8方向と自分自身をチェック
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1, 0};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1, 0};
        
        for (int i = 0; i < dx.length; i++) {
            int checkX = tileX + dx[i];
            int checkY = tileY + dy[i];
            updateChannelWateredState(checkX, checkY);
        }
    }
}
