package io.github.some_example_name.game;

import io.github.some_example_name.entity.ItemData;

import java.util.Map;

/**
 * クラフト機能を管理するクラス。
 */
public class CraftingSystem {
    private Inventory inventory;
    
    public CraftingSystem(Inventory inventory) {
        this.inventory = inventory;
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
        
        Map<Integer, Integer> materials = itemData.getMaterials();
        for (Map.Entry<Integer, Integer> entry : materials.entrySet()) {
            int materialId = entry.getKey();
            int requiredAmount = entry.getValue();
            int currentAmount = inventory.getItemCount(materialId);
            
            if (currentAmount < requiredAmount) {
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
        
        return true;
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
