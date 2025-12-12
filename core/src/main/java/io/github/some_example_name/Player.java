package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * プレイヤーを表すクラス。タイルベースの移動を管理します。
 */
public class Player {
    // タイルサイズ（ピクセル単位）
    public static final int TILE_SIZE = 32;
    
    // タイル座標（グリッド上の位置）
    private int tileX;
    private int tileY;
    
    // 現在のピクセル座標（移動アニメーション用）
    private float pixelX;
    private float pixelY;
    
    // 移動状態
    private boolean isMoving;
    private float moveProgress; // 0.0 から 1.0 まで
    private float moveSpeed = 0.2f; // 移動速度（1タイル移動するのにかかる時間）
    
    // 移動方向
    private int targetTileX;
    private int targetTileY;
    
    public Player(int startTileX, int startTileY) {
        this.tileX = startTileX;
        this.tileY = startTileY;
        this.pixelX = startTileX * TILE_SIZE;
        this.pixelY = startTileY * TILE_SIZE;
        this.isMoving = false;
    }
    
    /**
     * プレイヤーを更新します。
     * @param deltaTime 前フレームからの経過時間（秒）
     */
    public void update(float deltaTime) {
        if (isMoving) {
            moveProgress += deltaTime / moveSpeed;
            
            if (moveProgress >= 1.0f) {
                // 移動完了
                moveProgress = 1.0f;
                tileX = targetTileX;
                tileY = targetTileY;
                pixelX = tileX * TILE_SIZE;
                pixelY = tileY * TILE_SIZE;
                isMoving = false;
            } else {
                // 移動中：線形補間でスムーズに移動
                float startX = tileX * TILE_SIZE;
                float startY = tileY * TILE_SIZE;
                float endX = targetTileX * TILE_SIZE;
                float endY = targetTileY * TILE_SIZE;
                
                pixelX = startX + (endX - startX) * moveProgress;
                pixelY = startY + (endY - startY) * moveProgress;
            }
        }
    }
    
    /**
     * 指定された方向に移動を開始します。
     * @param dx X方向の移動量（-1, 0, 1）
     * @param dy Y方向の移動量（-1, 0, 1）
     * @return 移動を開始できた場合true
     */
    public boolean move(int dx, int dy) {
        if (isMoving) {
            return false; // 移動中は新しい移動を受け付けない
        }
        
        int newTileX = tileX + dx;
        int newTileY = tileY + dy;
        
        // 無限マップなので境界チェックなし
        
        targetTileX = newTileX;
        targetTileY = newTileY;
        
        // 移動を開始
        isMoving = true;
        moveProgress = 0.0f;
        
        return true;
    }
    
    /**
     * 移動中かどうかを返します。
     */
    public boolean isMoving() {
        return isMoving;
    }
    
    /**
     * 現在のタイルX座標を返します。
     */
    public int getTileX() {
        return tileX;
    }
    
    /**
     * 現在のタイルY座標を返します。
     */
    public int getTileY() {
        return tileY;
    }
    
    /**
     * 現在のピクセルX座標を返します。
     */
    public float getPixelX() {
        return pixelX;
    }
    
    /**
     * 現在のピクセルY座標を返します。
     */
    public float getPixelY() {
        return pixelY;
    }
    
    /**
     * プレイヤーを描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     */
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
        
        // プレイヤーの中心に小さな円を描画
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(pixelX + TILE_SIZE / 2, pixelY + TILE_SIZE / 2, 4);
    }
}

