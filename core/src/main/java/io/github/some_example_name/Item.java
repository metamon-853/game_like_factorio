package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * アイテムを表すクラス。
 */
public class Item {
    // タイル座標
    private int tileX;
    private int tileY;
    
    // 取得済みかどうか
    private boolean collected;
    
    // アイテムの種類（色分け用）- 後方互換性のため残す
    private ItemType type;
    
    // アイテムデータ（新しいシステム）
    private ItemData itemData;
    
    public enum ItemType {
        RED(Color.RED),
        BLUE(Color.BLUE),
        YELLOW(Color.YELLOW),
        PURPLE(Color.PURPLE);
        
        private final Color color;
        
        ItemType(Color color) {
            this.color = color;
        }
        
        public Color getColor() {
            return color;
        }
    }
    
    /**
     * アイテムデータを指定して作成します（推奨）。
     */
    public Item(int tileX, int tileY, ItemData itemData) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.collected = false;
        this.itemData = itemData;
        // 後方互換性のため、ItemTypeも設定（色から推測）
        this.type = determineItemType(itemData.getColor());
    }
    
    /**
     * 後方互換性のためのコンストラクタ（ItemTypeを使用）。
     */
    public Item(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.collected = false;
        // ランダムに種類を決定
        ItemType[] types = ItemType.values();
        this.type = types[(int)(Math.random() * types.length)];
        this.itemData = null;
    }
    
    /**
     * アイテムの種類を指定して作成します（ロード用）。
     */
    public Item(int tileX, int tileY, ItemType type) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.collected = false;
        this.type = type;
        this.itemData = null;
    }
    
    /**
     * 色からItemTypeを推測します。
     */
    private ItemType determineItemType(Color color) {
        // 簡易的な色マッチング
        if (color.r > 0.7f && color.g < 0.5f && color.b < 0.5f) {
            return ItemType.RED;
        } else if (color.r < 0.5f && color.g < 0.5f && color.b > 0.7f) {
            return ItemType.BLUE;
        } else if (color.r > 0.7f && color.g > 0.7f && color.b < 0.5f) {
            return ItemType.YELLOW;
        } else {
            return ItemType.PURPLE;
        }
    }
    
    /**
     * アイテムを取得します。
     */
    public void collect() {
        this.collected = true;
    }
    
    /**
     * 取得済みかどうかを返します。
     */
    public boolean isCollected() {
        return collected;
    }
    
    /**
     * タイルX座標を返します。
     */
    public int getTileX() {
        return tileX;
    }
    
    /**
     * タイルY座標を返します。
     */
    public int getTileY() {
        return tileY;
    }
    
    /**
     * アイテムの種類を返します（後方互換性のため）。
     */
    public ItemType getType() {
        return type;
    }
    
    /**
     * アイテムデータを返します。
     */
    public ItemData getItemData() {
        return itemData;
    }
    
    /**
     * アイテムの色を返します。
     */
    public Color getColor() {
        if (itemData != null) {
            return itemData.getColor();
        }
        return type.getColor();
    }
    
    /**
     * アイテムを描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     */
    public void render(ShapeRenderer shapeRenderer) {
        if (collected) {
            return; // 取得済みのアイテムは描画しない
        }
        
        float pixelX = tileX * Player.TILE_SIZE;
        float pixelY = tileY * Player.TILE_SIZE;
        
        // アイテムを円形で描画
        Color itemColor = getColor();
        shapeRenderer.setColor(itemColor);
        shapeRenderer.circle(pixelX + Player.TILE_SIZE / 2, pixelY + Player.TILE_SIZE / 2, Player.TILE_SIZE / 3);
        
        // 中心に小さな点を描画
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(pixelX + Player.TILE_SIZE / 2, pixelY + Player.TILE_SIZE / 2, 2);
    }
}

