package io.github.some_example_name.entity;

import com.badlogic.gdx.graphics.Color;

/**
 * 家畜のデータを保持するクラス。
 */
public class LivestockData {
    public int id;
    public String name;
    public String description;
    
    // 肉のアイテムID
    public int meatItemId;
    
    // 製品のアイテムID（-1の場合は製品なし）
    public int productItemId;
    
    // 製品生産間隔（秒）
    public float productInterval;
    
    // 描画色
    private Color color;
    
    public LivestockData() {
        this.meatItemId = -1;
        this.productItemId = -1;
        this.productInterval = 8.0f;
        this.color = Color.WHITE;
    }
    
    /**
     * 色を設定します。
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * 色を設定します（RGB値から）。
     */
    public void setColor(float r, float g, float b) {
        this.color = new Color(r, g, b, 1f);
    }
    
    /**
     * 色を取得します。
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * 製品を生産するかどうかを返します。
     */
    public boolean hasProduct() {
        return productItemId != -1;
    }
}
