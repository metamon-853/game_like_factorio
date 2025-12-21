package io.github.some_example_name.manager;

import io.github.some_example_name.entity.Item;
import io.github.some_example_name.entity.ItemData;
import io.github.some_example_name.entity.Player;
import io.github.some_example_name.entity.TerrainTile;
import io.github.some_example_name.game.CivilizationLevel;
import io.github.some_example_name.game.Inventory;
import io.github.some_example_name.system.SoundManager;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;
import java.util.Random;

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
    
    // アイテムデータローダー
    private ItemDataLoader itemDataLoader;
    
    // 文明レベル（アイテム生成に使用）
    private CivilizationLevel civilizationLevel;
    
    // インベントリへの参照
    private Inventory inventory;
    
    // サウンドマネージャーへの参照
    private SoundManager soundManager;
    
    // 地形マネージャーへの参照（地形別採集用）
    private TerrainManager terrainManager;
    
    // ランダムジェネレーター
    private Random random;
    
    public ItemManager() {
        this.items = new Array<>();
        this.spawnTimer = 0f;
        this.spawnInterval = 3.0f; // 3秒ごとにアイテムを生成
        this.collectedCount = 0;
        this.generatedChunks = new java.util.HashSet<>();
        this.itemDataLoader = new ItemDataLoader();
        this.civilizationLevel = new CivilizationLevel(1); // 初期はレベル1
        this.inventory = null; // 後で設定される
        this.terrainManager = null; // 後で設定される
        this.random = new Random();
    }
    
    /**
     * インベントリを設定します。
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    
    /**
     * サウンドマネージャーを設定します。
     */
    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }
    
    /**
     * 地形マネージャーを設定します。
     */
    public void setTerrainManager(TerrainManager terrainManager) {
        this.terrainManager = terrainManager;
    }
    
    /**
     * インベントリを取得します。
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * 文明レベルを設定します。
     */
    public void setCivilizationLevel(CivilizationLevel level) {
        this.civilizationLevel = level;
    }
    
    /**
     * 文明レベルを取得します。
     */
    public CivilizationLevel getCivilizationLevel() {
        return civilizationLevel;
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
            // 地形に応じたアイテムを生成
            ItemData selectedItemData = getItemForTerrain(tileX, tileY);
            if (selectedItemData != null) {
                items.add(new Item(tileX, tileY, selectedItemData));
            }
        }
    }
    
    /**
     * 指定された地形タイプに応じたアイテムを取得します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @return 生成するアイテムデータ（生成しない場合はnull）
     */
    private ItemData getItemForTerrain(int tileX, int tileY) {
        if (terrainManager == null) {
            // 地形マネージャーが設定されていない場合は従来の方法を使用
            Array<ItemData> availableItems = itemDataLoader.getAvailableItems(civilizationLevel.getLevel());
            if (availableItems.size > 0) {
                return availableItems.random();
            }
            return null;
        }
        
        TerrainTile tile = terrainManager.getTerrainTile(tileX, tileY);
        if (tile == null) {
            return null;
        }
        
        TerrainTile.TerrainType terrainType = tile.getTerrainType();
        
        // 地形別のドロップテーブル
        switch (terrainType) {
            case GRASS:
                // 草 → 種（低確率：10%）
                if (random.nextFloat() < 0.1f) {
                    return itemDataLoader.getItemData(8); // 種
                }
                return null; // 何も生成しない
                
            case FOREST:
                // 森 → 木材（確率：30%）
                if (random.nextFloat() < 0.3f) {
                    return itemDataLoader.getItemData(7); // 木材
                }
                return null;
                
            case STONE:
                // 岩 → 石（確率：20%）
                if (random.nextFloat() < 0.2f) {
                    return itemDataLoader.getItemData(1); // 石
                }
                return null;
                
            case SAND:
                // 砂 → 何もなし
                return null;
                
            case BARREN:
                // 荒地 → 採集不可
                return null;
                
            default:
                // その他の地形 → 従来の方法
                Array<ItemData> availableItems = itemDataLoader.getAvailableItems(civilizationLevel.getLevel());
                if (availableItems.size > 0 && random.nextFloat() < 0.1f) {
                    return availableItems.random();
                }
                return null;
        }
    }
    
    /**
     * プレイヤーとアイテムの衝突判定を行います。
     * プレイヤーが4マップ升サイズなので、プレイヤーの範囲内にアイテムがある場合、アイテムを取得します。
     * 移動中も判定を行うため、より正確に衝突を検出できます。
     * @param player プレイヤー
     */
    private void checkCollisions(Player player) {
        // プレイヤーの中心座標を取得
        float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
        float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
        
        // プレイヤーのサイズは4マップ升（TILE_SIZE * 4）
        float playerSize = Player.TILE_SIZE * 4.0f;
        float playerLeft = playerCenterX - playerSize / 2;
        float playerRight = playerCenterX + playerSize / 2;
        float playerBottom = playerCenterY - playerSize / 2;
        float playerTop = playerCenterY + playerSize / 2;
        
        for (Item item : items) {
            if (item.isCollected()) {
                continue;
            }
            
            // アイテムの位置（マップ升の中心）
            float itemX = item.getTileX() * Player.TILE_SIZE + Player.TILE_SIZE / 2;
            float itemY = item.getTileY() * Player.TILE_SIZE + Player.TILE_SIZE / 2;
            
            // アイテムがプレイヤーの範囲内にあるかチェック
            if (itemX >= playerLeft && itemX <= playerRight &&
                itemY >= playerBottom && itemY <= playerTop) {
                item.collect();
                collectedCount++;
                
                // インベントリにアイテムを追加
                if (inventory != null && item.getItemData() != null) {
                    inventory.addItem(item.getItemData());
                }
                
                // アイテム取得音を再生（ItemDataの有無に関わらず）
                if (soundManager != null) {
                    soundManager.playCollectSound();
                } else {
                    com.badlogic.gdx.Gdx.app.log("ItemManager", "SoundManager is null when trying to play collect sound");
                }
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
    
    /**
     * 現在のアイテムリストを返します（セーブ用）。
     */
    public Array<Item> getItems() {
        return items;
    }
    
    /**
     * アイテムリストを設定します（ロード用）。
     */
    public void setItems(Array<Item> items) {
        this.items = items;
    }
    
    /**
     * 取得したアイテム数を設定します（ロード用）。
     */
    public void setCollectedCount(int count) {
        this.collectedCount = count;
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
        this.generatedChunks = chunks;
    }
    
    /**
     * アイテムデータローダーを取得します。
     */
    public ItemDataLoader getItemDataLoader() {
        return itemDataLoader;
    }
}

