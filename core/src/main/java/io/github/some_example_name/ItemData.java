package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;

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
    
    public ItemData() {
        this.civilizationLevel = 1; // デフォルトはレベル1
        this.color = Color.WHITE;
        this.category = "その他";
        this.tier = 1;
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
}
