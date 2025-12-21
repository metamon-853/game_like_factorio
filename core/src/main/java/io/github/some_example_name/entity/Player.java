package io.github.some_example_name.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.some_example_name.system.SoundManager;
import io.github.some_example_name.manager.TerrainManager;

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
    private float moveSpeed = 0.03f; // 移動速度（1プレイヤー升移動するのにかかる時間）- 5倍速
    
    // 移動方向
    private int targetPlayerTileX;
    private int targetPlayerTileY;
    
    // サウンドマネージャーへの参照
    private SoundManager soundManager;
    
    // 地形マネージャーへの参照（タイルタイプを取得するため）
    private TerrainManager terrainManager;
    
    // 足音のタイミング管理
    private float footstepTimer = 0f;
    private static final float FOOTSTEP_INTERVAL = 0.1f; // 足音の間隔（秒）- 移動速度に合わせて調整
    private boolean footstepPlayedThisMove = false; // この移動で足音を再生したか
    
    // 移動方向（最後に移動した方向を記録）
    private int lastMoveX = 0;
    private int lastMoveY = 1; // デフォルトは下向き
    
    public Player(int startTileX, int startTileY) {
        // マップ升座標からプレイヤー升座標に変換（マップ升の中心のプレイヤー升に配置）
        this.playerTileX = startTileX * SUBDIVISIONS + SUBDIVISIONS / 2;
        this.playerTileY = startTileY * SUBDIVISIONS + SUBDIVISIONS / 2;
        this.pixelX = playerTileX * PLAYER_TILE_SIZE;
        this.pixelY = playerTileY * PLAYER_TILE_SIZE;
        this.isMoving = false;
    }
    
    /**
     * サウンドマネージャーを設定します。
     */
    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }
    
    /**
     * 地形マネージャーを設定します。
     */
    public void setTerrainManager(TerrainManager terrainManager) {
        this.terrainManager = terrainManager;
    }
    
    /**
     * プレイヤーを更新します。
     * @param deltaTime 前フレームからの経過時間（秒）
     */
    public void update(float deltaTime) {
        if (isMoving) {
            moveProgress += deltaTime / moveSpeed;
            
            // 足音を再生（移動中のみ）
            if (soundManager != null) {
                footstepTimer += deltaTime;
                if (footstepTimer >= FOOTSTEP_INTERVAL) {
                    // 現在の位置のタイルタイプを取得して適切な足音を再生
                    TerrainTile.TerrainType terrainType = getCurrentTerrainType();
                    soundManager.playFootstepSound(terrainType);
                    footstepTimer = 0f; // タイマーをリセット
                    footstepPlayedThisMove = true;
                }
            }
            
            if (moveProgress >= 1.0f) {
                // 移動完了
                moveProgress = 1.0f;
                playerTileX = targetPlayerTileX;
                playerTileY = targetPlayerTileY;
                pixelX = playerTileX * PLAYER_TILE_SIZE;
                pixelY = playerTileY * PLAYER_TILE_SIZE;
                isMoving = false;
                footstepTimer = 0f; // 移動終了時にタイマーをリセット
                footstepPlayedThisMove = false; // フラグをリセット
            } else {
                // 移動中：線形補間でスムーズに移動
                float startX = playerTileX * PLAYER_TILE_SIZE;
                float startY = playerTileY * PLAYER_TILE_SIZE;
                float endX = targetPlayerTileX * PLAYER_TILE_SIZE;
                float endY = targetPlayerTileY * PLAYER_TILE_SIZE;
                
                pixelX = startX + (endX - startX) * moveProgress;
                pixelY = startY + (endY - startY) * moveProgress;
            }
        } else {
            // 移動していない場合はタイマーをリセット
            footstepTimer = 0f;
            footstepPlayedThisMove = false;
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
        
        // 移動方向を記録
        if (dx != 0 || dy != 0) {
            lastMoveX = dx;
            lastMoveY = dy;
        }
        
        // 移動を開始
        isMoving = true;
        moveProgress = 0.0f;
        footstepTimer = 0f; // タイマーをリセット
        footstepPlayedThisMove = false; // フラグをリセット
        
        // 移動開始時にすぐ足音を再生
        if (soundManager != null) {
            // 現在の位置のタイルタイプを取得して適切な足音を再生
            TerrainTile.TerrainType terrainType = getCurrentTerrainType();
            soundManager.playFootstepSound(terrainType);
            footstepPlayedThisMove = true;
        }
        
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
     * 現在の位置のタイルタイプを取得します。
     * @return タイルタイプ（取得できない場合はGRASSを返す）
     */
    private TerrainTile.TerrainType getCurrentTerrainType() {
        if (terrainManager == null) {
            return TerrainTile.TerrainType.GRASS; // デフォルトは草
        }
        
        // 現在のマップ升座標を取得
        int tileX = getTileX();
        int tileY = getTileY();
        
        // 地形タイルを取得
        TerrainTile tile = terrainManager.getTerrainTile(tileX, tileY);
        if (tile != null) {
            return tile.getTerrainType();
        }
        
        // タイルが取得できない場合はデフォルトのGRASSを返す
        return TerrainTile.TerrainType.GRASS;
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
        // プレイヤーの実際のピクセル位置を基準に描画（細かい移動も反映される）
        // プレイヤー升の中心を基準にする
        float playerCenterX = pixelX + PLAYER_TILE_SIZE / 2;
        float playerCenterY = pixelY + PLAYER_TILE_SIZE / 2;
        
        // プレイヤーのサイズを4マップ升サイズに設定
        float playerSize = TILE_SIZE * 4.0f;
        
        // 移動アニメーション用のオフセット（上下に少し揺れる）
        float bounceOffset = 0f;
        if (isMoving) {
            bounceOffset = (float)Math.sin(moveProgress * Math.PI * 2) * 3f;
        }
        
        float drawY = playerCenterY - playerSize / 2 + bounceOffset;
        
        // 影を描画（シンプルな円形）
        shapeRenderer.setColor(new Color(0f, 0f, 0f, 0.25f));
        shapeRenderer.ellipse(playerCenterX - playerSize * 0.4f, playerCenterY - playerSize / 2 - 5, 
                             playerSize * 0.8f, playerSize * 0.2f);
        
        // 体（シンプルな丸いキャラクター）
        float bodyRadius = playerSize * 0.35f;
        shapeRenderer.setColor(new Color(0.3f, 0.6f, 0.9f, 1f)); // 明るい青
        shapeRenderer.circle(playerCenterX, drawY + playerSize * 0.6f, bodyRadius);
        
        // 体のハイライト
        shapeRenderer.setColor(new Color(0.4f, 0.7f, 1f, 0.6f));
        shapeRenderer.circle(playerCenterX - bodyRadius * 0.3f, drawY + playerSize * 0.55f, bodyRadius * 0.4f);
        
        // 頭（大きめの丸）
        float headRadius = playerSize * 0.25f;
        shapeRenderer.setColor(new Color(1f, 0.9f, 0.8f, 1f)); // 明るい肌色
        shapeRenderer.circle(playerCenterX, drawY + playerSize * 0.75f, headRadius);
        
        // 頭のハイライト
        shapeRenderer.setColor(new Color(1f, 0.95f, 0.85f, 0.7f));
        shapeRenderer.circle(playerCenterX - headRadius * 0.3f, drawY + playerSize * 0.7f, headRadius * 0.4f);
        
        // 目（かわいい大きな目）
        float eyeSize = headRadius * 0.25f;
        float eyeSpacing = headRadius * 0.4f;
        float eyeY = drawY + playerSize * 0.75f;
        
        // 移動方向に応じて目の位置を調整
        float eyeX1 = playerCenterX - eyeSpacing;
        float eyeX2 = playerCenterX + eyeSpacing;
        
        if (lastMoveX > 0) { // 右向き
            eyeX1 += eyeSize * 0.5f;
            eyeX2 += eyeSize * 0.5f;
        } else if (lastMoveX < 0) { // 左向き
            eyeX1 -= eyeSize * 0.5f;
            eyeX2 -= eyeSize * 0.5f;
        }
        if (lastMoveY > 0) { // 上向き
            eyeY += eyeSize * 0.3f;
        } else if (lastMoveY < 0) { // 下向き
            eyeY -= eyeSize * 0.3f;
        }
        
        // 白目
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(eyeX1, eyeY, eyeSize * 1.2f);
        shapeRenderer.circle(eyeX2, eyeY, eyeSize * 1.2f);
        
        // 黒目
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(eyeX1, eyeY, eyeSize);
        shapeRenderer.circle(eyeX2, eyeY, eyeSize);
        
        // 目のハイライト
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(eyeX1 - eyeSize * 0.2f, eyeY + eyeSize * 0.2f, eyeSize * 0.3f);
        shapeRenderer.circle(eyeX2 - eyeSize * 0.2f, eyeY + eyeSize * 0.2f, eyeSize * 0.3f);
        
        // 口（シンプルな笑顔）
        shapeRenderer.setColor(new Color(0.8f, 0.3f, 0.3f, 1f)); // 赤
        float mouthY = drawY + playerSize * 0.65f;
        shapeRenderer.arc(playerCenterX, mouthY, headRadius * 0.3f, 0, 180);
        
        // 手足（シンプルな円）
        float limbRadius = playerSize * 0.08f;
        shapeRenderer.setColor(new Color(1f, 0.9f, 0.8f, 1f)); // 肌色
        
        // 移動中は手足を動かす
        float limbOffset = 0f;
        if (isMoving) {
            limbOffset = (float)Math.sin(moveProgress * Math.PI * 4) * playerSize * 0.1f;
        }
        
        // 左腕
        shapeRenderer.circle(playerCenterX - bodyRadius * 0.7f, drawY + playerSize * 0.5f + limbOffset, limbRadius);
        // 右腕
        shapeRenderer.circle(playerCenterX + bodyRadius * 0.7f, drawY + playerSize * 0.5f - limbOffset, limbRadius);
        // 左足
        shapeRenderer.circle(playerCenterX - bodyRadius * 0.4f, drawY + playerSize * 0.15f + limbOffset * 0.5f, limbRadius);
        // 右足
        shapeRenderer.circle(playerCenterX + bodyRadius * 0.4f, drawY + playerSize * 0.15f - limbOffset * 0.5f, limbRadius);
        
        // アクセサリ（小さな星やハート）
        if (isMoving) {
            shapeRenderer.setColor(new Color(1f, 0.8f, 0.2f, 0.8f)); // 黄色
            float starSize = playerSize * 0.05f;
            float starX = playerCenterX + headRadius * 0.6f;
            float starY = drawY + playerSize * 0.85f;
            // 簡単な星の形（5つの点）
            for (int i = 0; i < 5; i++) {
                float angle = (float)(i * Math.PI * 2 / 5);
                float x = starX + (float)Math.cos(angle) * starSize;
                float y = starY + (float)Math.sin(angle) * starSize;
                shapeRenderer.circle(x, y, starSize * 0.3f);
            }
        }
    }
}

