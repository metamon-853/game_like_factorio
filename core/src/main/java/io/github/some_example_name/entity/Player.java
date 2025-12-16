package io.github.some_example_name.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * プレイヤーを表すクラス。タイルベースの移動を管理します。
 */
public class Player {
    // マップ升サイズ（ピクセル単位）- 3で割り切れる数にする
    public static final int MAP_TILE_SIZE = 30;
    
    // プレイヤー升サイズ（ピクセル単位）- マップ升の1/3
    public static final int PLAYER_TILE_SIZE = MAP_TILE_SIZE / 3;
    
    // 1マップ升あたりのプレイヤー升の数
    public static final int SUBDIVISIONS = 3;
    
    // 後方互換性のため、TILE_SIZEはマップ升サイズを指す
    public static final int TILE_SIZE = MAP_TILE_SIZE;
    
    // プレイヤー升座標（細かいグリッド上の位置）
    private int playerTileX;
    private int playerTileY;
    
    // 現在のピクセル座標（移動アニメーション用）
    private float pixelX;
    private float pixelY;
    
    // 移動状態
    private boolean isMoving;
    private float moveProgress; // 0.0 から 1.0 まで
    private float moveSpeed = 0.15f; // 移動速度（1プレイヤー升移動するのにかかる時間）
    
    // 移動方向
    private int targetPlayerTileX;
    private int targetPlayerTileY;
    
    public Player(int startTileX, int startTileY) {
        // マップ升座標からプレイヤー升座標に変換（マップ升の中心のプレイヤー升に配置）
        this.playerTileX = startTileX * SUBDIVISIONS + SUBDIVISIONS / 2;
        this.playerTileY = startTileY * SUBDIVISIONS + SUBDIVISIONS / 2;
        this.pixelX = playerTileX * PLAYER_TILE_SIZE;
        this.pixelY = playerTileY * PLAYER_TILE_SIZE;
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
                playerTileX = targetPlayerTileX;
                playerTileY = targetPlayerTileY;
                pixelX = playerTileX * PLAYER_TILE_SIZE;
                pixelY = playerTileY * PLAYER_TILE_SIZE;
                isMoving = false;
            } else {
                // 移動中：線形補間でスムーズに移動
                float startX = playerTileX * PLAYER_TILE_SIZE;
                float startY = playerTileY * PLAYER_TILE_SIZE;
                float endX = targetPlayerTileX * PLAYER_TILE_SIZE;
                float endY = targetPlayerTileY * PLAYER_TILE_SIZE;
                
                pixelX = startX + (endX - startX) * moveProgress;
                pixelY = startY + (endY - startY) * moveProgress;
            }
        }
    }
    
    /**
     * 指定された方向に移動を開始します。
     * @param dx X方向の移動量（-1, 0, 1）- プレイヤー升単位
     * @param dy Y方向の移動量（-1, 0, 1）- プレイヤー升単位
     * @return 移動を開始できた場合true
     */
    public boolean move(int dx, int dy) {
        if (isMoving) {
            return false; // 移動中は新しい移動を受け付けない
        }
        
        int newPlayerTileX = playerTileX + dx;
        int newPlayerTileY = playerTileY + dy;
        
        // 無限マップなので境界チェックなし
        
        targetPlayerTileX = newPlayerTileX;
        targetPlayerTileY = newPlayerTileY;
        
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
     * 現在のマップ升X座標を返します（後方互換性のため）。
     */
    public int getTileX() {
        return playerTileX / SUBDIVISIONS;
    }
    
    /**
     * 現在のマップ升Y座標を返します（後方互換性のため）。
     */
    public int getTileY() {
        return playerTileY / SUBDIVISIONS;
    }
    
    /**
     * 現在のプレイヤー升X座標を返します。
     */
    public int getPlayerTileX() {
        return playerTileX;
    }
    
    /**
     * 現在のプレイヤー升Y座標を返します。
     */
    public int getPlayerTileY() {
        return playerTileY;
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
     * プレイヤーの位置を設定します（ロード用）。
     * @param playerTileX プレイヤー升X座標
     * @param playerTileY プレイヤー升Y座標
     */
    public void setPosition(int playerTileX, int playerTileY) {
        this.playerTileX = playerTileX;
        this.playerTileY = playerTileY;
        this.pixelX = playerTileX * PLAYER_TILE_SIZE;
        this.pixelY = playerTileY * PLAYER_TILE_SIZE;
        this.isMoving = false; // 移動を停止
    }
    
    /**
     * プレイヤーを描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     */
    public void render(ShapeRenderer shapeRenderer) {
        // プレイヤーのサイズはプレイヤー升サイズに合わせる
        float playerSize = PLAYER_TILE_SIZE * 0.8f; // 少し小さくして見やすくする
        float offset = (PLAYER_TILE_SIZE - playerSize) / 2;
        
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(pixelX + offset, pixelY + offset, playerSize, playerSize);
        
        // プレイヤーの中心に小さな円を描画
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(pixelX + PLAYER_TILE_SIZE / 2, pixelY + PLAYER_TILE_SIZE / 2, 3);
    }
}

