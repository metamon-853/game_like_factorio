package io.github.some_example_name.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * レシピのデータを保持するクラス。
 * 
 * <p>レシピは、複数の素材から1つのアイテムを作成する方法を定義します。
 * ItemDataのmaterialsフィールドとは独立して管理され、
 * より柔軟なレシピシステムを実現します。</p>
 * 
 * @author game_like_factorio
 * @version 1.0.0
 */
public class RecipeData {
    /** レシピID（一意の識別子） */
    public int id;
    
    /** 作成されるアイテムのID */
    public int resultItemId;
    
    /** 作成されるアイテムの数量（デフォルト: 1） */
    public int resultAmount;
    
    /** 必要な素材（キー: アイテムID、値: 必要数） */
    private Map<Integer, Integer> ingredients;
    
    /** レシピ名（表示用） */
    public String name;
    
    /** レシピの説明 */
    public String description;
    
    /** 必要な文明レベル（このレベル以上でアンロック） */
    public int requiredCivilizationLevel;
    
    /** レシピがアンロックされているかどうか */
    private boolean unlocked;
    
    /** レシピのカテゴリ（分類用） */
    public String category;
    
    /**
     * RecipeDataを初期化します。
     */
    public RecipeData() {
        this.id = -1;
        this.resultItemId = -1;
        this.resultAmount = 1;
        this.ingredients = new HashMap<>();
        this.name = "";
        this.description = "";
        this.requiredCivilizationLevel = 1;
        this.unlocked = false;
        this.category = "その他";
    }
    
    /**
     * 必要な素材を設定します。
     * @param ingredients 素材マップ（キー: アイテムID、値: 必要数）
     */
    public void setIngredients(Map<Integer, Integer> ingredients) {
        this.ingredients = ingredients != null ? new HashMap<>(ingredients) : new HashMap<>();
    }
    
    /**
     * 必要な素材を取得します。
     * @return 素材マップ（キー: アイテムID、値: 必要数）
     */
    public Map<Integer, Integer> getIngredients() {
        return new HashMap<>(ingredients);
    }
    
    /**
     * 素材を追加します。
     * @param itemId アイテムID
     * @param amount 必要数
     */
    public void addIngredient(int itemId, int amount) {
        if (amount > 0) {
            ingredients.put(itemId, amount);
        }
    }
    
    /**
     * レシピがアンロックされているかどうかを取得します。
     * @return アンロックされている場合true
     */
    public boolean isUnlocked() {
        return unlocked;
    }
    
    /**
     * レシピのアンロック状態を設定します。
     * @param unlocked アンロック状態
     */
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
    
    /**
     * このレシピが有効かどうかを判定します。
     * @return 有効な場合true（resultItemIdが設定され、少なくとも1つの素材がある）
     */
    public boolean isValid() {
        return resultItemId > 0 && !ingredients.isEmpty();
    }
    
    /**
     * レシピの文字列表現を返します。
     * @return レシピの文字列表現
     */
    @Override
    public String toString() {
        return String.format("Recipe[ID=%d, Result=%d x%d, Ingredients=%d, Unlocked=%s]", 
            id, resultItemId, resultAmount, ingredients.size(), unlocked);
    }
}
