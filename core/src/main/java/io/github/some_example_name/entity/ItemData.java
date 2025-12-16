package io.github.some_example_name.entity;

import com.badlogic.gdx.graphics.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * アイテムのデータを保持するクラス。
 */
public class ItemData {
    public int id;
    public String name;
    public String description;
    
    // カテゴリ
    public String category;
    
    // ティア（文明レベルと同じ値を使用）
    public int tier;
    
    // 文明レベル（tierから推測、または別途設定可能）
    private int civilizationLevel;
    
    // アイテムの色（描画用）
    private Color color;
    
    // クラフトに必要な素材（キー: アイテムID、値: 必要数）
    private Map<Integer, Integer> materials;
    
    public ItemData() {
        this.civilizationLevel = 1; // デフォルトはレベル1
        this.color = Color.WHITE;
        this.category = "その他";
        this.tier = 1;
        this.materials = new HashMap<>();
    }
    
    /**
     * 文明レベルを設定します。
     */
    public void setCivilizationLevel(int level) {
        this.civilizationLevel = level;
    }
    
    /**
     * 文明レベルを取得します。
     */
    public int getCivilizationLevel() {
        return civilizationLevel;
    }
    
    /**
     * アイテムの色を設定します。
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * アイテムの色を取得します。
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * クラフトに必要な素材を設定します。
     * @param materials 素材マップ（キー: アイテムID、値: 必要数）
     */
    public void setMaterials(Map<Integer, Integer> materials) {
        this.materials = materials != null ? new HashMap<>(materials) : new HashMap<>();
    }
    
    /**
     * クラフトに必要な素材を取得します。
     * @return 素材マップ（キー: アイテムID、値: 必要数）
     */
    public Map<Integer, Integer> getMaterials() {
        return new HashMap<>(materials);
    }
    
    /**
     * このアイテムがクラフト可能かどうかを返します（素材が必要な場合のみクラフト可能）。
     * @return クラフト可能な場合true
     */
    public boolean isCraftable() {
        return materials != null && !materials.isEmpty();
    }
}
