package io.github.some_example_name.game;

import java.util.HashMap;
import java.util.Map;

/**
 * 保存食の保存量を管理するクラス。
 * 保存食は文明レベル解禁条件に使用されます。
 */
public class PreservedFoodManager {
    // 保存食の保存量（キー: アイテムID、値: 保存量）
    private Map<Integer, Integer> preservedFoods;
    
    public PreservedFoodManager() {
        this.preservedFoods = new HashMap<>();
    }
    
    /**
     * 保存食の保存量を追加します。
     * @param itemId アイテムID
     * @param amount 追加する数量
     */
    public void addPreservedFood(int itemId, int amount) {
        if (amount <= 0) {
            return;
        }
        preservedFoods.put(itemId, preservedFoods.getOrDefault(itemId, 0) + amount);
    }
    
    /**
     * 保存食の保存量を取得します。
     * @param itemId アイテムID
     * @return 保存量（保存されていない場合は0）
     */
    public int getPreservedFoodAmount(int itemId) {
        return preservedFoods.getOrDefault(itemId, 0);
    }
    
    /**
     * 指定された保存食が指定された数量以上保存されているかどうかを判定します。
     * @param itemId アイテムID
     * @param requiredAmount 必要な数量
     * @return 条件を満たしている場合true
     */
    public boolean hasPreservedFood(int itemId, int requiredAmount) {
        return getPreservedFoodAmount(itemId) >= requiredAmount;
    }
    
    /**
     * すべての保存食の保存量を返します（セーブ用）。
     * @return 保存食マップ
     */
    public Map<Integer, Integer> getAllPreservedFoods() {
        return new HashMap<>(preservedFoods);
    }
    
    /**
     * 保存食の保存量を設定します（ロード用）。
     * @param preservedFoods 保存食マップ
     */
    public void setPreservedFoods(Map<Integer, Integer> preservedFoods) {
        this.preservedFoods = preservedFoods != null ? new HashMap<>(preservedFoods) : new HashMap<>();
    }
}
