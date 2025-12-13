package io.github.some_example_name;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

/**
 * アイテムの生成、更新、描画を管理するクラス。
 */
public class ItemManager {
    private Array<Item> items;
    
    // アイテム生成のタイマー
    private float spawnTimer;
    private float spawnInterval; // アイテム生成間隔（秒）
    
    // 取得したアイテム数
    private int collectedCount;
    
    // 生成済みのチャンクを記録（無限マップ用）
    private java.util.Set<String> generatedChunks;
    
    public ItemManager() {
        this.items = new Array<>();
        this.spawnTimer = 0f;
        this.spawnInterval = 3.0f; // 3秒ごとにアイテムを生成
        this.collectedCount = 0;
        this.generatedChunks = new java.util.HashSet<>();
    }
    
    /**
     * アイテムマネージャーを更新します。
     * @param deltaTime 前フレームからの経過時間（秒）
     * @param player プレイヤー（衝突判定用）
     * @param camera カメラ（視野範囲の計算用）
     */
    public void update(float deltaTime, Player player, OrthographicCamera camera) {
        // カメラの視野範囲内のチャンクを生成
        generateChunksInView(camera, player);
        
        // アイテム生成タイマーを更新
        spawnTimer += deltaTime;
        
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;
            spawnRandomItemInView(camera, player);
        }
        
        // プレイヤーとアイテムの衝突判定
        checkCollisions(player);
        
        // 取得済みアイテムを削除
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            if (item.isCollected()) {
                iterator.remove();
            }
        }
    }
    
    /**
     * カメラの視野範囲内のチャンクを生成します。
     * @param camera カメラ
     * @param player プレイヤー
     */
    private void generateChunksInView(OrthographicCamera camera, Player player) {
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
        float margin = Player.TILE_SIZE * chunkSize;
        int startChunkX = (int)Math.floor((cameraLeft - margin) / (Player.TILE_SIZE * chunkSize));
        int endChunkX = (int)Math.ceil((cameraRight + margin) / (Player.TILE_SIZE * chunkSize));
        int startChunkY = (int)Math.floor((cameraBottom - margin) / (Player.TILE_SIZE * chunkSize));
        int endChunkY = (int)Math.ceil((cameraTop + margin) / (Player.TILE_SIZE * chunkSize));
        
        // 各チャンクをチェックして、未生成の場合は生成済みとしてマーク
        for (int chunkX = startChunkX; chunkX <= endChunkX; chunkX++) {
            for (int chunkY = startChunkY; chunkY <= endChunkY; chunkY++) {
                String chunkKey = chunkX + "," + chunkY;
                if (!generatedChunks.contains(chunkKey)) {
                    generatedChunks.add(chunkKey);
                    // チャンク内にランダムにアイテムを生成（オプション）
                    // ここでは生成済みマークのみ行う
                }
            }
        }
    }
    
    /**
     * カメラの視野範囲内のランダムな位置にアイテムを生成します。
     * @param camera カメラ
     * @param player プレイヤー（プレイヤーの位置には生成しない）
     */
    private void spawnRandomItemInView(OrthographicCamera camera, Player player) {
        // カメラの視野範囲を計算（ズームを考慮）
        float actualViewportWidth = camera.viewportWidth * camera.zoom;
        float actualViewportHeight = camera.viewportHeight * camera.zoom;
        float cameraLeft = camera.position.x - actualViewportWidth / 2;
        float cameraRight = camera.position.x + actualViewportWidth / 2;
        float cameraBottom = camera.position.y - actualViewportHeight / 2;
        float cameraTop = camera.position.y + actualViewportHeight / 2;
        
        // マージンを追加して少し広めに生成
        float margin = Player.TILE_SIZE * 2;
        int startTileX = (int)Math.floor((cameraLeft - margin) / Player.TILE_SIZE);
        int endTileX = (int)Math.ceil((cameraRight + margin) / Player.TILE_SIZE);
        int startTileY = (int)Math.floor((cameraBottom - margin) / Player.TILE_SIZE);
        int endTileY = (int)Math.ceil((cameraTop + margin) / Player.TILE_SIZE);
        
        // ランダムな位置を生成（プレイヤーの位置を避ける）
        int attempts = 0;
        int tileX, tileY;
        
        do {
            tileX = startTileX + (int)(Math.random() * (endTileX - startTileX + 1));
            tileY = startTileY + (int)(Math.random() * (endTileY - startTileY + 1));
            attempts++;
        } while ((tileX == player.getTileX() && tileY == player.getTileY()) && attempts < 20);
        
        // 既存のアイテムと同じ位置でないことを確認
        boolean positionOccupied = false;
        for (Item item : items) {
            if (item.getTileX() == tileX && item.getTileY() == tileY) {
                positionOccupied = true;
                break;
            }
        }
        
        if (!positionOccupied) {
            items.add(new Item(tileX, tileY));
        }
    }
    
    /**
     * プレイヤーとアイテムの衝突判定を行います。
     * プレイヤーの中心がアイテムのマップ升内にある場合、アイテムを取得します。
     * 移動中も判定を行うため、より正確に衝突を検出できます。
     * @param player プレイヤー
     */
    private void checkCollisions(Player player) {
        // プレイヤーの中心座標を取得
        float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
        float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
        
        // プレイヤーの中心がどのマップ升内にあるかを計算
        int playerMapTileX = (int)Math.floor(playerCenterX / Player.MAP_TILE_SIZE);
        int playerMapTileY = (int)Math.floor(playerCenterY / Player.MAP_TILE_SIZE);
        
        for (Item item : items) {
            if (!item.isCollected() && 
                item.getTileX() == playerMapTileX && 
                item.getTileY() == playerMapTileY) {
                item.collect();
                collectedCount++;
            }
        }
    }
    
    /**
     * すべてのアイテムを描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     */
    public void render(ShapeRenderer shapeRenderer) {
        for (Item item : items) {
            item.render(shapeRenderer);
        }
    }
    
    /**
     * 取得したアイテム数を返します。
     */
    public int getCollectedCount() {
        return collectedCount;
    }
    
    /**
     * 現在のアイテム数を返します。
     */
    public int getItemCount() {
        return items.size;
    }
}

