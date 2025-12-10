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
    
    // アイテムの種類（色分け用）
    private ItemType type;
    
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
    
    public Item(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.collected = false;
        // ランダムに種類を決定
        ItemType[] types = ItemType.values();
        this.type = types[(int)(Math.random() * types.length)];
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
        shapeRenderer.setColor(type.getColor());
        shapeRenderer.circle(pixelX + Player.TILE_SIZE / 2, pixelY + Player.TILE_SIZE / 2, Player.TILE_SIZE / 3);
        
        // 中心に小さな点を描画
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(pixelX + Player.TILE_SIZE / 2, pixelY + Player.TILE_SIZE / 2, 2);
    }
}

