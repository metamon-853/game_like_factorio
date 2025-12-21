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
    
    // クラフトに必要な素材（キー: アイテムID、値: 必要数）- 消費されるもの
    private Map<Integer, Integer> materials;
    
    // クラフトに必要な要求条件（キー: タイプ、値: アイテムID）
    // タイプ: "tool"（道具）、"facility"（施設）など
    private Map<String, Integer> requirements;
    
    // 農具関連の属性
    private int toolDurability = -1; // 農具の内部耐久値（-1は農具でないことを示す）
    private float toolEfficiency = 1.0f; // 農具の効率（収穫量倍率、デフォルト1.0）
    
    // 作物関連の属性
    private boolean requiresWater = false; // 水辺必須かどうか
    
    public ItemData() {
        this.civilizationLevel = 1; // デフォルトはレベル1
        this.color = Color.WHITE;
        this.category = "その他";
        this.tier = 1;
        this.materials = new HashMap<>();
        this.requirements = new HashMap<>();
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
     * クラフトに必要な要求条件を設定します。
     * @param requirements 要求条件マップ（キー: タイプ、値: アイテムID）
     */
    public void setRequirements(Map<String, Integer> requirements) {
        this.requirements = requirements != null ? new HashMap<>(requirements) : new HashMap<>();
    }
    
    /**
     * クラフトに必要な要求条件を取得します。
     * @return 要求条件マップ（キー: タイプ、値: アイテムID）
     */
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(requirements);
    }
    
    /**
     * このアイテムがクラフト可能かどうかを返します（素材が必要な場合のみクラフト可能）。
     * @return クラフト可能な場合true
     */
    public boolean isCraftable() {
        return materials != null && !materials.isEmpty();
    }
    
    /**
     * 農具の内部耐久値を設定します。
     * @param durability 耐久値（-1は農具でないことを示す）
     */
    public void setToolDurability(int durability) {
        this.toolDurability = durability;
    }
    
    /**
     * 農具の内部耐久値を取得します。
     * @return 耐久値（-1の場合は農具ではない）
     */
    public int getToolDurability() {
        return toolDurability;
    }
    
    /**
     * このアイテムが農具かどうかを返します。
     * @return 農具の場合true
     */
    public boolean isTool() {
        return toolDurability > 0;
    }
    
    /**
     * 農具の効率（収穫量倍率）を設定します。
     * @param efficiency 効率（1.0が基準）
     */
    public void setToolEfficiency(float efficiency) {
        this.toolEfficiency = efficiency;
    }
    
    /**
     * 農具の効率（収穫量倍率）を取得します。
     * @return 効率
     */
    public float getToolEfficiency() {
        return toolEfficiency;
    }
    
    /**
     * 水辺必須かどうかを設定します。
     * @param requiresWater 水辺必須の場合true
     */
    public void setRequiresWater(boolean requiresWater) {
        this.requiresWater = requiresWater;
    }
    
    /**
     * 水辺必須かどうかを取得します。
     * @return 水辺必須の場合true
     */
    public boolean requiresWater() {
        return requiresWater;
    }
}
