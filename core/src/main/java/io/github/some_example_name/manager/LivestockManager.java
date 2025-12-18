package io.github.some_example_name.manager;

import io.github.some_example_name.entity.LivestockData;
import io.github.some_example_name.entity.LivestockTile;
import io.github.some_example_name.game.Inventory;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
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
    
    // 家畜データローダー
    private LivestockDataLoader livestockDataLoader;
    
    // 餌のアイテムID（作物を使用）
    private static final int FEED_ITEM_ID = 6; // 作物
    
    public LivestockManager() {
        this.livestockTiles = new HashMap<>();
        this.inventory = null;
        this.livestockDataLoader = new LivestockDataLoader();
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
     * @param livestockData 家畜のデータ（nullの場合はランダム）
     * @return 動物を配置できた場合true
     */
    public boolean placeAnimal(int tileX, int tileY, LivestockData livestockData) {
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
        LivestockData dataToPlace = livestockData;
        if (dataToPlace == null) {
            // ランダムに種類を選択
            Array<LivestockData> allLivestock = livestockDataLoader.getAllLivestock();
            if (allLivestock.size > 0) {
                dataToPlace = allLivestock.random();
            } else {
                return false; // 家畜データが読み込まれていない
            }
        }
        
        if (livestockTile.placeAnimal(dataToPlace)) {
            // インベントリから餌を1個消費
            inventory.removeItem(FEED_ITEM_ID, 1);
            return true;
        }
        
        return false;
    }
    
    /**
     * 指定されたタイル位置に動物を配置します（ランダムな種類）。
     * @param tileX タイルX座標（マップ升単位）
     * @param tileY タイルY座標（マップ升単位）
     * @return 動物を配置できた場合true
     */
    public boolean placeAnimal(int tileX, int tileY) {
        return placeAnimal(tileX, tileY, null);
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
        
        // 家畜のデータを取得
        LivestockData data = livestockTile.getLivestockData();
        if (data == null || !data.hasProduct()) {
            return false;
        }
        
        // 収穫
        if (livestockTile.harvestProduct()) {
            // インベントリに製品を追加（種類ごとの製品ID）
            if (inventory != null) {
                inventory.addItem(data.productItemId, 1);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * 指定されたタイル位置の家畜を殺して肉を取得します。
     * @param tileX タイルX座標（マップ升単位）
     * @param tileY タイルY座標（マップ升単位）
     * @return 殺すことができた場合true
     */
    public boolean killAnimal(int tileX, int tileY) {
        String key = tileX + "," + tileY;
        LivestockTile livestockTile = livestockTiles.get(key);
        
        if (livestockTile == null || !livestockTile.hasAnimal()) {
            return false;
        }
        
        // 家畜を殺して肉のアイテムIDを取得
        int meatId = livestockTile.killAnimal();
        if (meatId != -1) {
            // インベントリに肉を追加
            if (inventory != null) {
                inventory.addItem(meatId, 1);
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
    
    /**
     * 家畜データローダーを取得します。
     */
    public LivestockDataLoader getLivestockDataLoader() {
        return livestockDataLoader;
    }
}
