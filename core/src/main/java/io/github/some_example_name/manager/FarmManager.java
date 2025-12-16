package io.github.some_example_name.manager;

import io.github.some_example_name.entity.FarmTile;
import io.github.some_example_name.game.Inventory;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.HashMap;
import java.util.Map;

/**
 * 農地を管理するクラス。
 */
public class FarmManager {
    // 農地タイルのマップ（キー: "tileX,tileY"）
    private Map<String, FarmTile> farmTiles;
    
    // インベントリへの参照
    private Inventory inventory;
    
    // 種のアイテムID（仮の値、実際のアイテムIDに合わせて調整可能）
    private static final int SEED_ITEM_ID = 1; // 仮のID
    
    // 作物のアイテムID（収穫時に獲得）
    private static final int CROP_ITEM_ID = 2; // 仮のID
    
    public FarmManager() {
        this.farmTiles = new HashMap<>();
        this.inventory = null;
    }
    
    /**
     * インベントリを設定します。
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    
    /**
     * 農地を更新します。
     * @param deltaTime 前フレームからの経過時間（秒）
     */
    public void update(float deltaTime) {
        for (FarmTile farmTile : farmTiles.values()) {
            farmTile.update(deltaTime);
        }
    }
    
    /**
     * 指定されたタイル位置に種を植えます。
     * @param tileX タイルX座標（マップ升単位）
     * @param tileY タイルY座標（マップ升単位）
     * @return 種を植えられた場合true
     */
    public boolean plantSeed(int tileX, int tileY) {
        // インベントリに種があるかチェック
        if (inventory == null || inventory.getItemCount(SEED_ITEM_ID) <= 0) {
            return false; // 種がない
        }
        
        String key = tileX + "," + tileY;
        FarmTile farmTile = farmTiles.get(key);
        
        if (farmTile == null) {
            // 新しい農地タイルを作成
            farmTile = new FarmTile(tileX, tileY);
            farmTiles.put(key, farmTile);
        }
        
        // 種を植える
        if (farmTile.plantSeed()) {
            // インベントリから種を1個消費
            inventory.removeItem(SEED_ITEM_ID, 1);
            return true;
        }
        
        return false;
    }
    
    /**
     * 指定されたタイル位置の作物を収穫します。
     * @param tileX タイルX座標（マップ升単位）
     * @param tileY タイルY座標（マップ升単位）
     * @return 収穫できた場合true
     */
    public boolean harvest(int tileX, int tileY) {
        String key = tileX + "," + tileY;
        FarmTile farmTile = farmTiles.get(key);
        
        if (farmTile == null || !farmTile.isHarvestable()) {
            return false;
        }
        
        // 収穫
        if (farmTile.harvest()) {
            // インベントリに作物を追加
            if (inventory != null) {
                inventory.addItem(CROP_ITEM_ID, 1);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * 指定されたタイル位置に農地があるかどうかを返します。
     */
    public boolean hasFarmTile(int tileX, int tileY) {
        String key = tileX + "," + tileY;
        return farmTiles.containsKey(key);
    }
    
    /**
     * 指定されたタイル位置の農地を取得します。
     */
    public FarmTile getFarmTile(int tileX, int tileY) {
        String key = tileX + "," + tileY;
        return farmTiles.get(key);
    }
    
    /**
     * すべての農地を描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     */
    public void render(ShapeRenderer shapeRenderer) {
        for (FarmTile farmTile : farmTiles.values()) {
            farmTile.render(shapeRenderer);
        }
    }
    
    /**
     * すべての農地タイルを返します（セーブ用）。
     */
    public Map<String, FarmTile> getFarmTiles() {
        return farmTiles;
    }
    
    /**
     * 農地タイルを設定します（ロード用）。
     */
    public void setFarmTiles(Map<String, FarmTile> farmTiles) {
        this.farmTiles = farmTiles != null ? farmTiles : new HashMap<>();
    }
}
