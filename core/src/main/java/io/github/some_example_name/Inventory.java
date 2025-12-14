package io.github.some_example_name;

import java.util.HashMap;
import java.util.Map;

/**
 * プレイヤーのインベントリを管理するクラス。
 */
public class Inventory {
    // アイテムIDをキーとして、数量を保持
    private Map<Integer, Integer> items;
    
    public Inventory() {
        this.items = new HashMap<>();
    }
    
    /**
     * アイテムを追加します。
     * @param itemId アイテムID
     * @param amount 追加する数量（デフォルトは1）
     */
    public void addItem(int itemId, int amount) {
        if (amount <= 0) {
            return;
        }
        items.put(itemId, items.getOrDefault(itemId, 0) + amount);
    }
    
    /**
     * アイテムを1個追加します。
     * @param itemId アイテムID
     */
    public void addItem(int itemId) {
        addItem(itemId, 1);
    }
    
    /**
     * アイテムを追加します（ItemDataから）。
     * @param itemData アイテムデータ
     */
    public void addItem(ItemData itemData) {
        if (itemData != null) {
            addItem(itemData.id, 1);
        }
    }
    
    /**
     * アイテムの数量を取得します。
     * @param itemId アイテムID
     * @return 数量（持っていない場合は0）
     */
    public int getItemCount(int itemId) {
        return items.getOrDefault(itemId, 0);
    }
    
    /**
     * アイテムを削除します。
     * @param itemId アイテムID
     * @param amount 削除する数量
     * @return 削除に成功した場合true
     */
    public boolean removeItem(int itemId, int amount) {
        int currentCount = getItemCount(itemId);
        if (currentCount < amount) {
            return false;
        }
        
        int newCount = currentCount - amount;
        if (newCount <= 0) {
            items.remove(itemId);
        } else {
            items.put(itemId, newCount);
        }
        return true;
    }
    
    /**
     * アイテムを1個削除します。
     * @param itemId アイテムID
     * @return 削除に成功した場合true
     */
    public boolean removeItem(int itemId) {
        return removeItem(itemId, 1);
    }
    
    /**
     * すべてのアイテムエントリを取得します。
     * @return アイテムIDと数量のマップ
     */
    public Map<Integer, Integer> getAllItems() {
        return new HashMap<>(items);
    }
    
    /**
     * インベントリが空かどうかを返します。
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    /**
     * インベントリの総アイテム数を返します。
     */
    public int getTotalItemCount() {
        int total = 0;
        for (int count : items.values()) {
            total += count;
        }
        return total;
    }
}
