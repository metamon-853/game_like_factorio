package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 畜産タイルを表すクラス。
 */
public class LivestockTile {
    // タイル座標（マップ升単位）
    private int tileX;
    private int tileY;
    
    // 動物が飼育されているか
    private boolean hasAnimal;
    
    // 動物の成長段階（0: 幼体、1: 成長中、2: 成熟）
    private int growthStage;
    
    // 成長タイマー（秒）
    private float growthTimer;
    
    // 製品の生産タイマー（秒）
    private float productTimer;
    
    // 製品が生産されているか
    private boolean hasProduct;
    
    // 各成長段階に到達するまでの時間（秒）
    private static final float STAGE_1_TIME = 5.0f; // 成長中になるまで
    private static final float STAGE_2_TIME = 10.0f; // 成熟するまで
    
    // 製品生産間隔（秒）
    private static final float PRODUCT_INTERVAL = 8.0f;
    
    // 最大成長段階
    private static final int MAX_STAGE = 2;
    
    public LivestockTile(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.hasAnimal = false;
        this.growthStage = 0;
        this.growthTimer = 0f;
        this.productTimer = 0f;
        this.hasProduct = false;
    }
    
    /**
     * 動物を配置します。
     * @return 動物を配置できた場合true
     */
    public boolean placeAnimal() {
        if (hasAnimal) {
            return false; // 既に動物がいる
        }
        hasAnimal = true;
        growthStage = 0;
        growthTimer = 0f;
        productTimer = 0f;
        hasProduct = false;
        return true;
    }
    
    /**
     * 畜産タイルを更新します。
     * @param deltaTime 前フレームからの経過時間（秒）
     */
    public void update(float deltaTime) {
        if (!hasAnimal) {
            return;
        }
        
        // 成長処理
        if (growthStage < MAX_STAGE) {
            growthTimer += deltaTime;
            
            // 成長段階を更新
            if (growthTimer >= STAGE_2_TIME) {
                growthStage = MAX_STAGE; // 成熟
            } else if (growthTimer >= STAGE_1_TIME) {
                growthStage = 1; // 成長中
            }
        }
        
        // 成熟したら製品を生産
        if (growthStage >= MAX_STAGE && !hasProduct) {
            productTimer += deltaTime;
            
            if (productTimer >= PRODUCT_INTERVAL) {
                hasProduct = true;
                productTimer = 0f;
            }
        }
    }
    
    /**
     * 製品を収穫します。
     * @return 収穫できた場合true
     */
    public boolean harvestProduct() {
        if (!hasAnimal || !hasProduct) {
            return false; // 製品がない
        }
        
        // 収穫後、製品をリセット
        hasProduct = false;
        productTimer = 0f;
        return true;
    }
    
    /**
     * 動物が飼育されているかどうかを返します。
     */
    public boolean hasAnimal() {
        return hasAnimal;
    }
    
    /**
     * 製品が収穫可能かどうかを返します。
     */
    public boolean hasProduct() {
        return hasAnimal && hasProduct;
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
     * 畜産タイルを描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     */
    public void render(ShapeRenderer shapeRenderer) {
        float pixelX = tileX * Player.TILE_SIZE;
        float pixelY = tileY * Player.TILE_SIZE;
        
        // 畜産タイルの背景（薄い茶色）
        shapeRenderer.setColor(new Color(0.7f, 0.6f, 0.5f, 1f)); // 薄い茶色
        shapeRenderer.rect(pixelX, pixelY, Player.TILE_SIZE, Player.TILE_SIZE);
        
        if (hasAnimal) {
            // 成長段階に応じて色とサイズを変更
            Color animalColor;
            float animalSize;
            
            switch (growthStage) {
                case 0: // 幼体
                    animalColor = new Color(0.8f, 0.6f, 0.4f, 1f); // 薄い茶色
                    animalSize = Player.TILE_SIZE * 0.3f;
                    break;
                case 1: // 成長中
                    animalColor = new Color(0.6f, 0.4f, 0.2f, 1f); // 茶色
                    animalSize = Player.TILE_SIZE * 0.5f;
                    break;
                case 2: // 成熟
                    animalColor = new Color(0.4f, 0.2f, 0.1f, 1f); // 濃い茶色
                    animalSize = Player.TILE_SIZE * 0.6f;
                    break;
                default:
                    animalColor = Color.WHITE;
                    animalSize = Player.TILE_SIZE * 0.4f;
            }
            
            // 動物を描画
            shapeRenderer.setColor(animalColor);
            float offset = (Player.TILE_SIZE - animalSize) / 2;
            shapeRenderer.rect(pixelX + offset, pixelY + offset, animalSize, animalSize);
            
            // 製品がある場合は小さな円を表示
            if (hasProduct) {
                shapeRenderer.setColor(new Color(1f, 1f, 0.8f, 1f)); // 薄い黄色
                shapeRenderer.circle(pixelX + Player.TILE_SIZE * 0.8f, pixelY + Player.TILE_SIZE * 0.8f, Player.TILE_SIZE * 0.15f);
            }
        }
    }
}
