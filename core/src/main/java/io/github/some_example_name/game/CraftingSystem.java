package io.github.some_example_name.game;

import io.github.some_example_name.entity.ItemData;

import java.util.Map;

/**
 * クラフト機能を管理するクラス。
 */
public class CraftingSystem {
    private Inventory inventory;
    private PreservedFoodManager preservedFoodManager;
    
    public CraftingSystem(Inventory inventory) {
        this.inventory = inventory;
        this.preservedFoodManager = null;
    }
    
    /**
     * 保存食マネージャーを設定します。
     */
    public void setPreservedFoodManager(PreservedFoodManager preservedFoodManager) {
        this.preservedFoodManager = preservedFoodManager;
    }
    
    /**
     * 指定されたアイテムをクラフトできるかどうかをチェックします。
     * @param itemData クラフトしたいアイテムのデータ
     * @return クラフト可能な場合true
     */
    public boolean canCraft(ItemData itemData) {
        if (itemData == null || !itemData.isCraftable()) {
            return false;
        }
        
        // 素材（消費されるもの）をチェック
        Map<Integer, Integer> materials = itemData.getMaterials();
        for (Map.Entry<Integer, Integer> entry : materials.entrySet()) {
            int materialId = entry.getKey();
            int requiredAmount = entry.getValue();
            int currentAmount = inventory.getItemCount(materialId);
            
            if (currentAmount < requiredAmount) {
                return false;
            }
        }
        
        // 要求条件（持っていればいいもの）をチェック
        Map<String, Integer> requirements = itemData.getRequirements();
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            int requiredItemId = entry.getValue();
            int currentAmount = inventory.getItemCount(requiredItemId);
            
            // 道具や施設は1個以上持っていればOK（消費しない）
            if (currentAmount <= 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 指定されたアイテムをクラフトします。
     * @param itemData クラフトしたいアイテムのデータ
     * @return クラフトに成功した場合true
     */
    public boolean craft(ItemData itemData) {
        if (!canCraft(itemData)) {
            return false;
        }
        
        // 素材を消費
        Map<Integer, Integer> materials = itemData.getMaterials();
        for (Map.Entry<Integer, Integer> entry : materials.entrySet()) {
            int materialId = entry.getKey();
            int requiredAmount = entry.getValue();
            inventory.removeItem(materialId, requiredAmount);
        }
        
        // クラフトしたアイテムを追加
        inventory.addItem(itemData.id, 1);
        
        // 保存食の場合は保存食マネージャーにも追加
        if (preservedFoodManager != null && isPreservedFood(itemData)) {
            preservedFoodManager.addPreservedFood(itemData.id, 1);
        }
        
        return true;
    }
    
    /**
     * アイテムが保存食かどうかを判定します。
     * @param itemData アイテムデータ
     * @return 保存食の場合true
     */
    private boolean isPreservedFood(ItemData itemData) {
        if (itemData == null) {
            return false;
        }
        
        // カテゴリが"中間素材"で、名前に「保存」や「干し」「乾燥」が含まれるもの
        String name = itemData.name;
        String category = itemData.category;
        
        // パン（ID: 47）、干し肉（ID: 48）、乾燥作物（ID: 49）など
        if ("中間素材".equals(category)) {
            return name.contains("パン") || name.contains("干し") || name.contains("乾燥") || 
                   name.contains("保存");
        }
        
        return false;
    }
    
    /**
     * インベントリを設定します。
     * @param inventory インベントリ
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    
    /**
     * 指定されたアイテムIDの現在の数量を取得します。
     * @param itemId アイテムID
     * @return 現在の数量
     */
    public int getItemCount(int itemId) {
        if (inventory == null) {
            return 0;
        }
        return inventory.getItemCount(itemId);
    }
}
