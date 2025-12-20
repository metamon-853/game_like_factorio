package io.github.some_example_name.entity;

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
            float centerX = pixelX + Player.TILE_SIZE / 2;
            float centerY = pixelY + Player.TILE_SIZE / 2;
            
            switch (growthStage) {
                case 0: // 種
                    renderSeed(shapeRenderer, centerX, centerY);
                    break;
                case 1: // 芽
                    renderSprout(shapeRenderer, centerX, centerY);
                    break;
                case 2: // 成長中（花が咲いた状態）
                    renderFlower(shapeRenderer, centerX, centerY);
                    break;
                case 3: // 収穫可能（実がなった状態）
                    renderFruit(shapeRenderer, centerX, centerY);
                    break;
                default:
                    // デフォルトは小さな円
                    shapeRenderer.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
                    shapeRenderer.circle(centerX, centerY, Player.TILE_SIZE * 0.1f);
            }
        }
    }
    
    /**
     * 種を描画します。
     */
    private void renderSeed(ShapeRenderer shapeRenderer, float centerX, float centerY) {
        // 種は小さな茶色の円（楕円の代わりに円を使用）
        shapeRenderer.setColor(new Color(0.3f, 0.2f, 0.1f, 1f)); // 濃い茶色
        float seedSize = Player.TILE_SIZE * 0.12f;
        shapeRenderer.circle(centerX, centerY, seedSize / 2);
    }
    
    /**
     * 芽を描画します。
     */
    private void renderSprout(ShapeRenderer shapeRenderer, float centerX, float centerY) {
        // 芽は小さな緑の葉っぱ（2つの葉）
        float leafSize = Player.TILE_SIZE * 0.12f;
        float stemHeight = Player.TILE_SIZE * 0.15f;
        
        // 茎を描画
        shapeRenderer.setColor(new Color(0.1f, 0.5f, 0.1f, 1f)); // 濃い緑
        shapeRenderer.rect(centerX - Player.TILE_SIZE * 0.02f, centerY - stemHeight / 2, 
                          Player.TILE_SIZE * 0.04f, stemHeight);
        
        // 左の葉（円で表現）
        shapeRenderer.setColor(new Color(0.2f, 0.6f, 0.2f, 1f)); // 薄い緑
        float leftLeafX = centerX - Player.TILE_SIZE * 0.1f;
        float leftLeafY = centerY + Player.TILE_SIZE * 0.08f;
        shapeRenderer.circle(leftLeafX, leftLeafY, leafSize / 2);
        
        // 右の葉（円で表現）
        float rightLeafX = centerX + Player.TILE_SIZE * 0.1f;
        float rightLeafY = centerY + Player.TILE_SIZE * 0.08f;
        shapeRenderer.circle(rightLeafX, rightLeafY, leafSize / 2);
    }
    
    /**
     * 花を描画します。
     */
    private void renderFlower(ShapeRenderer shapeRenderer, float centerX, float centerY) {
        // 花は中心に円、周りに花びら
        float flowerSize = Player.TILE_SIZE * 0.4f;
        float petalSize = Player.TILE_SIZE * 0.1f;
        
        // 茎を描画
        shapeRenderer.setColor(new Color(0.1f, 0.5f, 0.1f, 1f)); // 濃い緑
        float stemHeight = Player.TILE_SIZE * 0.3f;
        shapeRenderer.rect(centerX - Player.TILE_SIZE * 0.03f, centerY - stemHeight / 2, 
                          Player.TILE_SIZE * 0.06f, stemHeight);
        
        // 花びらを描画（5枚の花びらを円で表現）
        shapeRenderer.setColor(new Color(1f, 0.8f, 0.2f, 1f)); // 黄色
        int petalCount = 5;
        for (int i = 0; i < petalCount; i++) {
            float angle = (float)(i * 2 * Math.PI / petalCount);
            float petalX = centerX + (float)Math.cos(angle) * flowerSize * 0.25f;
            float petalY = centerY + (float)Math.sin(angle) * flowerSize * 0.25f;
            shapeRenderer.circle(petalX, petalY, petalSize / 2);
        }
        
        // 中心を描画
        shapeRenderer.setColor(new Color(1f, 0.6f, 0.1f, 1f)); // オレンジ
        shapeRenderer.circle(centerX, centerY, Player.TILE_SIZE * 0.08f);
        
        // 花びらの間に小さな葉を追加
        shapeRenderer.setColor(new Color(0.1f, 0.6f, 0.1f, 1f)); // 緑
        for (int i = 0; i < petalCount; i++) {
            float angle = (float)(i * 2 * Math.PI / petalCount + Math.PI / petalCount);
            float leafX = centerX + (float)Math.cos(angle) * flowerSize * 0.2f;
            float leafY = centerY + (float)Math.sin(angle) * flowerSize * 0.2f;
            shapeRenderer.circle(leafX, leafY, Player.TILE_SIZE * 0.04f);
        }
    }
    
    /**
     * 実を描画します。
     */
    private void renderFruit(ShapeRenderer shapeRenderer, float centerX, float centerY) {
        // 実がなった状態：複数の実と葉
        float fruitSize = Player.TILE_SIZE * 0.18f;
        float leafSize = Player.TILE_SIZE * 0.1f;
        
        // 茎を描画
        shapeRenderer.setColor(new Color(0.1f, 0.5f, 0.1f, 1f)); // 濃い緑
        float stemHeight = Player.TILE_SIZE * 0.35f;
        shapeRenderer.rect(centerX - Player.TILE_SIZE * 0.03f, centerY - stemHeight / 2, 
                          Player.TILE_SIZE * 0.06f, stemHeight);
        
        // 葉を描画（左右、円で表現）
        shapeRenderer.setColor(new Color(0.1f, 0.7f, 0.1f, 1f)); // 緑
        // 左の葉
        float leftLeafX = centerX - Player.TILE_SIZE * 0.15f;
        float leftLeafY = centerY + Player.TILE_SIZE * 0.12f;
        shapeRenderer.circle(leftLeafX, leftLeafY, leafSize / 2);
        
        // 右の葉
        float rightLeafX = centerX + Player.TILE_SIZE * 0.15f;
        float rightLeafY = centerY + Player.TILE_SIZE * 0.12f;
        shapeRenderer.circle(rightLeafX, rightLeafY, leafSize / 2);
        
        // 実を描画（3個）
        shapeRenderer.setColor(new Color(0.9f, 0.7f, 0.1f, 1f)); // 黄金色
        // 左の実
        float leftFruitX = centerX - Player.TILE_SIZE * 0.12f;
        float leftFruitY = centerY - Player.TILE_SIZE * 0.05f;
        shapeRenderer.circle(leftFruitX, leftFruitY, fruitSize / 2);
        
        // 中央の実
        float centerFruitY = centerY - Player.TILE_SIZE * 0.08f;
        shapeRenderer.circle(centerX, centerFruitY, fruitSize / 2);
        
        // 右の実
        float rightFruitX = centerX + Player.TILE_SIZE * 0.12f;
        float rightFruitY = centerY - Player.TILE_SIZE * 0.05f;
        shapeRenderer.circle(rightFruitX, rightFruitY, fruitSize / 2);
        
        // 実のハイライト（光の反射）
        shapeRenderer.setColor(new Color(1f, 0.9f, 0.3f, 0.7f)); // 明るい黄色
        shapeRenderer.circle(leftFruitX - fruitSize * 0.2f, leftFruitY + fruitSize * 0.2f, fruitSize * 0.25f);
        shapeRenderer.circle(centerX - fruitSize * 0.2f, centerFruitY + fruitSize * 0.2f, fruitSize * 0.25f);
        shapeRenderer.circle(rightFruitX - fruitSize * 0.2f, rightFruitY + fruitSize * 0.2f, fruitSize * 0.25f);
        
        // 実の表面に小さな点を追加（質感を出す）
        shapeRenderer.setColor(new Color(0.8f, 0.6f, 0.05f, 0.5f)); // 少し暗い黄金色
        shapeRenderer.circle(leftFruitX + fruitSize * 0.15f, leftFruitY - fruitSize * 0.15f, fruitSize * 0.1f);
        shapeRenderer.circle(centerX + fruitSize * 0.15f, centerFruitY - fruitSize * 0.15f, fruitSize * 0.1f);
        shapeRenderer.circle(rightFruitX + fruitSize * 0.15f, rightFruitY - fruitSize * 0.15f, fruitSize * 0.1f);
    }
}
