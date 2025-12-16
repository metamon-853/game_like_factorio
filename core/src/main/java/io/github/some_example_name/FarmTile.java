package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 農地タイルを表すクラス。
 */
public class FarmTile {
    // タイル座標（マップ升単位）
    private int tileX;
    private int tileY;
    
    // 種が植えられているか
    private boolean hasSeed;
    
    // 成長段階（0: 種、1: 芽、2: 成長中、3: 収穫可能）
    private int growthStage;
    
    // 成長タイマー（秒）
    private float growthTimer;
    
    // 各成長段階に到達するまでの時間（秒）
    private static final float STAGE_1_TIME = 3.0f; // 芽が出るまで
    private static final float STAGE_2_TIME = 6.0f; // 成長中になるまで
    private static final float STAGE_3_TIME = 10.0f; // 収穫可能になるまで
    
    // 最大成長段階
    private static final int MAX_STAGE = 3;
    
    public FarmTile(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.hasSeed = false;
        this.growthStage = 0;
        this.growthTimer = 0f;
    }
    
    /**
     * 種を植えます。
     * @return 種を植えられた場合true
     */
    public boolean plantSeed() {
        if (hasSeed) {
            return false; // 既に種が植えられている
        }
        hasSeed = true;
        growthStage = 0;
        growthTimer = 0f;
        return true;
    }
    
    /**
     * 農地を更新します。
     * @param deltaTime 前フレームからの経過時間（秒）
     */
    public void update(float deltaTime) {
        if (!hasSeed || growthStage >= MAX_STAGE) {
            return;
        }
        
        growthTimer += deltaTime;
        
        // 成長段階を更新
        if (growthTimer >= STAGE_3_TIME) {
            growthStage = MAX_STAGE; // 収穫可能
        } else if (growthTimer >= STAGE_2_TIME) {
            growthStage = 2; // 成長中
        } else if (growthTimer >= STAGE_1_TIME) {
            growthStage = 1; // 芽
        }
    }
    
    /**
     * 作物を収穫します。
     * @return 収穫できた場合true
     */
    public boolean harvest() {
        if (!hasSeed || growthStage < MAX_STAGE) {
            return false; // まだ収穫できない
        }
        
        // 収穫後、農地をリセット
        hasSeed = false;
        growthStage = 0;
        growthTimer = 0f;
        return true;
    }
    
    /**
     * 種が植えられているかどうかを返します。
     */
    public boolean hasSeed() {
        return hasSeed;
    }
    
    /**
     * 収穫可能かどうかを返します。
     */
    public boolean isHarvestable() {
        return hasSeed && growthStage >= MAX_STAGE;
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
     * 成長段階を返します。
     */
    public int getGrowthStage() {
        return growthStage;
    }
    
    /**
     * 農地を描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     */
    public void render(ShapeRenderer shapeRenderer) {
        float pixelX = tileX * Player.TILE_SIZE;
        float pixelY = tileY * Player.TILE_SIZE;
        
        // 農地の背景（茶色）
        shapeRenderer.setColor(new Color(0.6f, 0.4f, 0.2f, 1f)); // 茶色
        shapeRenderer.rect(pixelX, pixelY, Player.TILE_SIZE, Player.TILE_SIZE);
        
        if (hasSeed) {
            // 成長段階に応じて色とサイズを変更
            Color cropColor;
            float cropSize;
            
            switch (growthStage) {
                case 0: // 種
                    cropColor = new Color(0.3f, 0.2f, 0.1f, 1f); // 濃い茶色
                    cropSize = Player.TILE_SIZE * 0.1f;
                    break;
                case 1: // 芽
                    cropColor = new Color(0.2f, 0.6f, 0.2f, 1f); // 薄い緑
                    cropSize = Player.TILE_SIZE * 0.2f;
                    break;
                case 2: // 成長中
                    cropColor = new Color(0.1f, 0.7f, 0.1f, 1f); // 緑
                    cropSize = Player.TILE_SIZE * 0.4f;
                    break;
                case 3: // 収穫可能
                    cropColor = new Color(0.0f, 0.8f, 0.0f, 1f); // 明るい緑
                    cropSize = Player.TILE_SIZE * 0.6f;
                    break;
                default:
                    cropColor = Color.WHITE;
                    cropSize = Player.TILE_SIZE * 0.3f;
            }
            
            shapeRenderer.setColor(cropColor);
            float offset = (Player.TILE_SIZE - cropSize) / 2;
            shapeRenderer.circle(pixelX + Player.TILE_SIZE / 2, pixelY + Player.TILE_SIZE / 2, cropSize / 2);
        }
    }
}
