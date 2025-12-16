package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.HashMap;
import java.util.Map;

/**
 * 畜産を管理するクラス。
 */
public class LivestockManager {
    // 畜産タイルのマップ（キー: "tileX,tileY"）
    private Map<String, LivestockTile> livestockTiles;
    
    // インベントリへの参照
    private Inventory inventory;
    
    // 餌のアイテムID（仮の値、実際のアイテムIDに合わせて調整可能）
    private static final int FEED_ITEM_ID = 3; // 仮のID
    
    // 畜産製品のアイテムID（収穫時に獲得）
    private static final int PRODUCT_ITEM_ID = 4; // 仮のID（肉やミルクなど）
    
    public LivestockManager() {
        this.livestockTiles = new HashMap<>();
        this.inventory = null;
    }
    
    /**
     * インベントリを設定します。
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    
    /**
     * 畜産タイルを更新します。
     * @param deltaTime 前フレームからの経過時間（秒）
     */
    public void update(float deltaTime) {
        for (LivestockTile livestockTile : livestockTiles.values()) {
            livestockTile.update(deltaTime);
        }
    }
    
    /**
     * 指定されたタイル位置に動物を配置します。
     * @param tileX タイルX座標（マップ升単位）
     * @param tileY タイルY座標（マップ升単位）
     * @return 動物を配置できた場合true
     */
    public boolean placeAnimal(int tileX, int tileY) {
        // インベントリに餌があるかチェック
        if (inventory == null || inventory.getItemCount(FEED_ITEM_ID) <= 0) {
            return false; // 餌がない
        }
        
        String key = tileX + "," + tileY;
        LivestockTile livestockTile = livestockTiles.get(key);
        
        if (livestockTile == null) {
            // 新しい畜産タイルを作成
            livestockTile = new LivestockTile(tileX, tileY);
            livestockTiles.put(key, livestockTile);
        }
        
        // 動物を配置
        if (livestockTile.placeAnimal()) {
            // インベントリから餌を1個消費
            inventory.removeItem(FEED_ITEM_ID, 1);
            return true;
        }
        
        return false;
    }
    
    /**
     * 指定されたタイル位置の製品を収穫します。
     * @param tileX タイルX座標（マップ升単位）
     * @param tileY タイルY座標（マップ升単位）
     * @return 収穫できた場合true
     */
    public boolean harvest(int tileX, int tileY) {
        String key = tileX + "," + tileY;
        LivestockTile livestockTile = livestockTiles.get(key);
        
        if (livestockTile == null || !livestockTile.hasProduct()) {
            return false;
        }
        
        // 収穫
        if (livestockTile.harvestProduct()) {
            // インベントリに製品を追加
            if (inventory != null) {
                inventory.addItem(PRODUCT_ITEM_ID, 1);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * 指定されたタイル位置に畜産タイルがあるかどうかを返します。
     */
    public boolean hasLivestockTile(int tileX, int tileY) {
        String key = tileX + "," + tileY;
        return livestockTiles.containsKey(key);
    }
    
    /**
     * 指定されたタイル位置の畜産タイルを取得します。
     */
    public LivestockTile getLivestockTile(int tileX, int tileY) {
        String key = tileX + "," + tileY;
        return livestockTiles.get(key);
    }
    
    /**
     * すべての畜産タイルを描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     */
    public void render(ShapeRenderer shapeRenderer) {
        for (LivestockTile livestockTile : livestockTiles.values()) {
            livestockTile.render(shapeRenderer);
        }
    }
    
    /**
     * すべての畜産タイルを返します（セーブ用）。
     */
    public Map<String, LivestockTile> getLivestockTiles() {
        return livestockTiles;
    }
    
    /**
     * 畜産タイルを設定します（ロード用）。
     */
    public void setLivestockTiles(Map<String, LivestockTile> livestockTiles) {
        this.livestockTiles = livestockTiles != null ? livestockTiles : new HashMap<>();
    }
}
