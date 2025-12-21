package io.github.some_example_name.entity;

import io.github.some_example_name.manager.TileDataLoader;

/**
 * 土壌のパラメータを保持するクラス。
 * 水分量、肥沃度、排水性、耕作難度の4つのパラメータを持つ。
 */
public class SoilData {
    // 水分量（0.0: 乾燥 ～ 1.0: 湿潤）
    private float moisture;
    
    // 肥沃度（0.0: 低 ～ 1.0: 高）
    private float fertility;
    
    // 排水性（0.0: 悪い ～ 1.0: 良い）
    private float drainage;
    
    // 耕作難度（0.0: 低 ～ 1.0: 高）
    private float tillageDifficulty;
    
    public SoilData() {
        this.moisture = 0.5f;
        this.fertility = 0.5f;
        this.drainage = 0.5f;
        this.tillageDifficulty = 0.5f;
    }
    
    public SoilData(float moisture, float fertility, float drainage, float tillageDifficulty) {
        this.moisture = clamp(moisture, 0.0f, 1.0f);
        this.fertility = clamp(fertility, 0.0f, 1.0f);
        this.drainage = clamp(drainage, 0.0f, 1.0f);
        this.tillageDifficulty = clamp(tillageDifficulty, 0.0f, 1.0f);
    }
    
    /**
     * 値を0.0～1.0の範囲にクランプします。
     */
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * 地形タイプから初期土壌パラメータを設定します。
     */
    public static SoilData fromTerrainType(TerrainTile.TerrainType terrainType) {
        TileDataLoader loader = TileDataLoader.getInstance();
        TileData tileData = loader.getTileData(terrainType);
        if (tileData != null) {
            return new SoilData(
                tileData.getMoisture(),
                tileData.getFertility(),
                tileData.getDrainage(),
                tileData.getTillageDifficulty()
            );
        }
        // フォールバック（デフォルト値）
        return new SoilData(0.5f, 0.5f, 0.5f, 0.5f);
    }
    
    public float getMoisture() {
        return moisture;
    }
    
    public void setMoisture(float moisture) {
        this.moisture = clamp(moisture, 0.0f, 1.0f);
    }
    
    public float getFertility() {
        return fertility;
    }
    
    public void setFertility(float fertility) {
        this.fertility = clamp(fertility, 0.0f, 1.0f);
    }
    
    public float getDrainage() {
        return drainage;
    }
    
    public void setDrainage(float drainage) {
        this.drainage = clamp(drainage, 0.0f, 1.0f);
    }
    
    public float getTillageDifficulty() {
        return tillageDifficulty;
    }
    
    public void setTillageDifficulty(float tillageDifficulty) {
        this.tillageDifficulty = clamp(tillageDifficulty, 0.0f, 1.0f);
    }
    
    /**
     * 土壌パラメータをコピーします。
     */
    public SoilData copy() {
        return new SoilData(moisture, fertility, drainage, tillageDifficulty);
    }
}
