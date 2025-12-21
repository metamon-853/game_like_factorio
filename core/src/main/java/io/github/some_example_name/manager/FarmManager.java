package io.github.some_example_name.manager;

import io.github.some_example_name.entity.FarmTile;
import io.github.some_example_name.entity.ItemData;
import io.github.some_example_name.entity.TerrainTile;
import io.github.some_example_name.entity.CropSoilRequirements;
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
    
    // アイテムデータローダーへの参照
    private ItemDataLoader itemDataLoader;
    
    // 地形マネージャーへの参照
    private TerrainManager terrainManager;
    
    public FarmManager() {
        this.farmTiles = new HashMap<>();
        this.inventory = null;
        this.itemDataLoader = null;
        this.terrainManager = null;
    }
    
    /**
     * インベントリを設定します。
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    
    /**
     * アイテムデータローダーを設定します。
     */
    public void setItemDataLoader(ItemDataLoader itemDataLoader) {
        this.itemDataLoader = itemDataLoader;
    }
    
    /**
     * 地形マネージャーを設定します。
     */
    public void setTerrainManager(TerrainManager terrainManager) {
        this.terrainManager = terrainManager;
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
     * 指定されたタイル位置に種を植えます（インベントリから自動的に種を探します）。
     * @param tileX タイルX座標（マップ升単位）
     * @param tileY タイルY座標（マップ升単位）
     * @return 種を植えられた場合true
     */
    public boolean plantSeed(int tileX, int tileY) {
        if (inventory == null || itemDataLoader == null) {
            return false;
        }
        
        // インベントリから種を探す
        Map<Integer, Integer> allItems = inventory.getAllItems();
        for (Map.Entry<Integer, Integer> entry : allItems.entrySet()) {
            int itemId = entry.getKey();
            int count = entry.getValue();
            
            if (count <= 0) {
                continue;
            }
            
            // アイテムデータを取得
            ItemData itemData = itemDataLoader.getItemData(itemId);
            if (itemData == null) {
                continue;
            }
            
            // 種かどうかをチェック（カテゴリが「植物」で名前に「種」が含まれる、またはIDが8）
            boolean isSeed = false;
            if (itemId == 8) {
                // 基本的な種
                isSeed = true;
            } else if ("植物".equals(itemData.category) && itemData.name != null && itemData.name.contains("種")) {
                // 植物カテゴリで名前に「種」が含まれる
                isSeed = true;
            }
            
            if (isSeed) {
                // 種が見つかったので、3引数のメソッドを呼び出す
                return plantSeed(tileX, tileY, itemId);
            }
        }
        
        return false; // 種が見つからなかった
    }
    
    /**
     * 指定されたタイル位置に種を植えます。
     * @param tileX タイルX座標（マップ升単位）
     * @param tileY タイルY座標（マップ升単位）
     * @param seedItemId 種のアイテムID
     * @return 種を植えられた場合true
     */
    public boolean plantSeed(int tileX, int tileY, int seedItemId) {
        // インベントリに種があるかチェック
        if (inventory == null || inventory.getItemCount(seedItemId) <= 0) {
            return false; // 種がない
        }
        
        // 現在の地形を取得
        TerrainTile currentTerrain = null;
        if (terrainManager != null) {
            currentTerrain = terrainManager.getTerrainTile(tileX, tileY);
        }
        
        // 米（ID 11）はPADDYのみに植えられる
        if (seedItemId == 11) {
            if (currentTerrain == null || currentTerrain.getTerrainType() != TerrainTile.TerrainType.PADDY) {
                return false; // 米は水田（PADDY）にのみ植えられる
            }
        }
        
        // 種のデータを取得して水条件をチェック
        if (itemDataLoader != null) {
            ItemData seedData = itemDataLoader.getItemData(seedItemId);
            if (seedData != null && seedData.requiresWater()) {
                // 水辺必須の場合は水辺チェック
                if (terrainManager == null || !terrainManager.isNearWater(tileX, tileY)) {
                    return false; // 水辺にいない
                }
            }
        }
        
        String key = tileX + "," + tileY;
        FarmTile farmTile = farmTiles.get(key);
        
        if (farmTile == null) {
            // 新しい農地タイルを作成
            farmTile = new FarmTile(tileX, tileY);
            
            // 地形タイプから土壌パラメータを初期化
            if (terrainManager != null) {
                TerrainTile terrainTile = terrainManager.getTerrainTile(tileX, tileY);
                if (terrainTile != null) {
                    farmTile.initializeSoilFromTerrain(terrainTile.getTerrainType());
                }
            }
            
            farmTiles.put(key, farmTile);
        }
        
        // 種のデータを取得して土壌条件を設定
        CropSoilRequirements soilRequirements = null;
        if (itemDataLoader != null) {
            ItemData seedData = itemDataLoader.getItemData(seedItemId);
            if (seedData != null) {
                // 種のIDから土壌条件を設定
                seedData.setSoilRequirementsFromSeedId(seedItemId);
                soilRequirements = seedData.getSoilRequirements();
            }
        }
        
        // 芋（ID 10）の場合は地形チェック（DIRTまたはFARMLANDのみ）
        if (seedItemId == 10) {
            if (currentTerrain == null) {
                return false;
            }
            TerrainTile.TerrainType terrainType = currentTerrain.getTerrainType();
            if (terrainType != TerrainTile.TerrainType.DIRT && 
                terrainType != TerrainTile.TerrainType.FARMLAND) {
                return false; // 芋はDIRTまたはFARMLANDにのみ植えられる
            }
        }
        
        // 種を植える（土壌条件チェック付き）
        if (farmTile.plantSeed(seedItemId, soilRequirements)) {
            // 芋（ID 10）の場合は地形に応じて収穫量倍率を調整
            if (seedItemId == 10 && currentTerrain != null) {
                TerrainTile.TerrainType terrainType = currentTerrain.getTerrainType();
                float terrainYieldMultiplier = 1.0f;
                if (terrainType == TerrainTile.TerrainType.DIRT) {
                    // DIRTでは収穫量が少ない（0.5倍）
                    terrainYieldMultiplier = 0.5f;
                } else if (terrainType == TerrainTile.TerrainType.FARMLAND) {
                    // FARMLANDでは収穫量が普通（1.0倍）
                    terrainYieldMultiplier = 1.0f;
                }
                // 既存の収穫量倍率に地形倍率を掛ける
                float currentYieldMultiplier = farmTile.getYieldMultiplier();
                farmTile.setYieldMultiplier(currentYieldMultiplier * terrainYieldMultiplier);
            }
            
            // インベントリから種を1個消費
            inventory.removeItem(seedItemId, 1);
            return true;
        }
        
        return false;
    }
    
    /**
     * 指定されたタイル位置の作物を収穫します（デフォルトの作物IDを使用）。
     * @param tileX タイルX座標（マップ升単位）
     * @param tileY タイルY座標（マップ升単位）
     * @return 収穫できた場合true
     */
    public boolean harvest(int tileX, int tileY) {
        // デフォルトの作物ID（9）を使用
        return harvest(tileX, tileY, 9);
    }
    
    /**
     * 指定されたタイル位置の作物を収穫します。
     * @param tileX タイルX座標（マップ升単位）
     * @param tileY タイルY座標（マップ升単位）
     * @param cropItemId 作物のアイテムID
     * @return 収穫できた場合true
     */
    public boolean harvest(int tileX, int tileY, int cropItemId) {
        String key = tileX + "," + tileY;
        FarmTile farmTile = farmTiles.get(key);
        
        if (farmTile == null || !farmTile.isHarvestable()) {
            return false;
        }
        
        // 収穫
        if (farmTile.harvest()) {
            // 農具の効率と土壌条件の収穫量倍率を考慮して収穫量を計算
            float toolEfficiency = farmTile.getToolEfficiency();
            float yieldMultiplier = farmTile.getYieldMultiplier();
            float totalEfficiency = toolEfficiency * yieldMultiplier;
            int harvestAmount = Math.max(1, Math.round(totalEfficiency)); // 最低1個、効率に応じて増加
            
            // インベントリに作物を追加
            if (inventory != null) {
                inventory.addItem(cropItemId, harvestAmount);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * 指定されたタイル位置の農地に農具を装着します。
     * @param tileX タイルX座標（マップ升単位）
     * @param tileY タイルY座標（マップ升単位）
     * @param toolItemId 農具のアイテムID
     * @return 装着に成功した場合true
     */
    public boolean equipTool(int tileX, int tileY, int toolItemId) {
        if (inventory == null || itemDataLoader == null) {
            return false;
        }
        
        // インベントリに農具があるかチェック
        if (inventory.getItemCount(toolItemId) <= 0) {
            return false;
        }
        
        // 農具のデータを取得
        ItemData toolData = itemDataLoader.getItemData(toolItemId);
        if (toolData == null || !toolData.isTool()) {
            return false; // 農具ではない
        }
        
        String key = tileX + "," + tileY;
        FarmTile farmTile = farmTiles.get(key);
        
        if (farmTile == null) {
            return false; // 農地が存在しない
        }
        
        // 農具を装着
        int durability = toolData.getToolDurability();
        float efficiency = toolData.getToolEfficiency();
        if (farmTile.equipTool(toolItemId, durability, efficiency)) {
            // インベントリから農具を1個消費
            inventory.removeItem(toolItemId, 1);
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
