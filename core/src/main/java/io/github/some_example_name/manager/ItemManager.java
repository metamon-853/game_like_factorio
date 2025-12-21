package io.github.some_example_name.manager;

import io.github.some_example_name.entity.Item;
import io.github.some_example_name.entity.ItemData;
import io.github.some_example_name.entity.Player;
import io.github.some_example_name.game.CivilizationLevel;
import io.github.some_example_name.game.Inventory;
import io.github.some_example_name.system.SoundManager;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

/**
 * アイテムの生成、更新、描画を管理するクラス。
 */
public class ItemManager {
    private Array<Item> items;
    
    // 取得したアイテム数
    private int collectedCount;
    
    // アイテムデータローダー
    private ItemDataLoader itemDataLoader;
    
    // 文明レベル（アイテム生成に使用）
    private CivilizationLevel civilizationLevel;
    
    // インベントリへの参照
    private Inventory inventory;
    
    // サウンドマネージャーへの参照
    private SoundManager soundManager;
    
    public ItemManager() {
        this.items = new Array<>();
        this.collectedCount = 0;
        this.itemDataLoader = new ItemDataLoader();
        this.civilizationLevel = new CivilizationLevel(1); // 初期はレベル1
        this.inventory = null; // 後で設定される
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
     * アイテムデータローダーを取得します。
     */
    public ItemDataLoader getItemDataLoader() {
        return itemDataLoader;
    }
}

