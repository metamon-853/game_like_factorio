package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

/**
 * アイテムの生成、更新、描画を管理するクラス。
 */
public class ItemManager {
    private Array<Item> items;
    private int gridWidth;
    private int gridHeight;
    
    // アイテム生成のタイマー
    private float spawnTimer;
    private float spawnInterval; // アイテム生成間隔（秒）
    
    // 取得したアイテム数
    private int collectedCount;
    
    public ItemManager(int gridWidth, int gridHeight) {
        this.items = new Array<>();
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.spawnTimer = 0f;
        this.spawnInterval = 3.0f; // 3秒ごとにアイテムを生成
        this.collectedCount = 0;
    }
    
    /**
     * アイテムマネージャーを更新します。
     * @param deltaTime 前フレームからの経過時間（秒）
     * @param player プレイヤー（衝突判定用）
     */
    public void update(float deltaTime, Player player) {
        // アイテム生成タイマーを更新
        spawnTimer += deltaTime;
        
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;
            spawnRandomItem(player);
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
     * ランダムな位置にアイテムを生成します。
     * @param player プレイヤー（プレイヤーの位置には生成しない）
     */
    private void spawnRandomItem(Player player) {
        // ランダムな位置を生成（プレイヤーの位置を避ける）
        int attempts = 0;
        int tileX, tileY;
        
        do {
            tileX = (int)(Math.random() * gridWidth);
            tileY = (int)(Math.random() * gridHeight);
            attempts++;
        } while ((tileX == player.getTileX() && tileY == player.getTileY()) && attempts < 10);
        
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
     * @param player プレイヤー
     */
    private void checkCollisions(Player player) {
        int playerTileX = player.getTileX();
        int playerTileY = player.getTileY();
        
        for (Item item : items) {
            if (!item.isCollected() && 
                item.getTileX() == playerTileX && 
                item.getTileY() == playerTileY) {
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

