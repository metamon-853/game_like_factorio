package io.github.some_example_name.entity;

/**
 * 作物が要求する土壌条件を定義するクラス。
 */
public class CropSoilRequirements {
    // 要求する最小水分量（0.0～1.0）
    private float minMoisture;
    
    // 要求する最大水分量（0.0～1.0、-1の場合は上限なし）
    private float maxMoisture;
    
    // 要求する最小肥沃度（0.0～1.0）
    private float minFertility;
    
    // 要求する最小排水性（0.0～1.0、-1の場合は要求なし）
    private float minDrainage;
    
    // 要求する最大排水性（0.0～1.0、-1の場合は上限なし）
    // 米の場合は排水性が低い方が良い（水田）
    private float maxDrainage;
    
    // 許容する最大耕作難度（0.0～1.0）
    private float maxTillageDifficulty;
    
    // 肥沃度の影響度（0.0～1.0）
    // 高いほど肥沃度が収穫量に大きく影響する
    private float fertilityImpact;
    
    public CropSoilRequirements() {
        // デフォルト値：緩い条件
        this.minMoisture = 0.0f;
        this.maxMoisture = -1f; // 上限なし
        this.minFertility = 0.0f;
        this.minDrainage = -1f; // 要求なし
        this.maxDrainage = -1f; // 上限なし
        this.maxTillageDifficulty = 1.0f;
        this.fertilityImpact = 0.5f;
    }
    
    /**
     * 米（稲作）の土壌条件を作成します。
     */
    public static CropSoilRequirements rice() {
        CropSoilRequirements req = new CropSoilRequirements();
        req.minMoisture = 0.8f; // 非常に高い水分量が必要
        req.maxMoisture = 1.0f;
        req.minFertility = 0.4f; // 中〜高
        req.minDrainage = -1f; // 排水性は低い方が良い（水田）
        req.maxDrainage = 0.3f; // 排水性が高すぎるとダメ
        req.maxTillageDifficulty = 0.8f; // 高い耕作難度でも可能
        req.fertilityImpact = 0.7f; // 肥沃度の影響が大きい
        return req;
    }
    
    /**
     * 麦の土壌条件を作成します。
     */
    public static CropSoilRequirements wheat() {
        CropSoilRequirements req = new CropSoilRequirements();
        req.minMoisture = 0.2f; // 低〜中
        req.maxMoisture = 0.6f; // 過湿に弱い
        req.minFertility = 0.4f; // 中
        req.minDrainage = 0.5f; // 高い排水性が良い
        req.maxDrainage = -1f; // 上限なし
        req.maxTillageDifficulty = 0.6f; // 中程度の耕作難度まで
        req.fertilityImpact = 0.6f; // 肥沃度の影響が中程度
        return req;
    }
    
    /**
     * 芋（サツマイモ系）の土壌条件を作成します。
     */
    public static CropSoilRequirements potato() {
        CropSoilRequirements req = new CropSoilRequirements();
        req.minMoisture = 0.2f; // 低〜中
        req.maxMoisture = 0.6f;
        req.minFertility = 0.1f; // 低い肥沃度でも育つ
        req.minDrainage = 0.4f; // 中〜高
        req.maxDrainage = -1f; // 上限なし
        req.maxTillageDifficulty = 0.4f; // 低い耕作難度が良い
        req.fertilityImpact = 0.3f; // 肥沃度の影響が小さい（痩せた土地でも育つ）
        return req;
    }
    
    /**
     * 土壌がこの作物の要求を満たしているかチェックします。
     */
    public boolean isSuitable(SoilData soil) {
        // 水分量チェック
        if (soil.getMoisture() < minMoisture) {
            return false;
        }
        if (maxMoisture >= 0 && soil.getMoisture() > maxMoisture) {
            return false;
        }
        
        // 肥沃度チェック
        if (soil.getFertility() < minFertility) {
            return false;
        }
        
        // 排水性チェック
        if (minDrainage >= 0 && soil.getDrainage() < minDrainage) {
            return false;
        }
        if (maxDrainage >= 0 && soil.getDrainage() > maxDrainage) {
            return false;
        }
        
        // 耕作難度チェック
        if (soil.getTillageDifficulty() > maxTillageDifficulty) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 土壌条件に基づいて成長速度の倍率を計算します（0.0～1.0）。
     */
    public float calculateGrowthMultiplier(SoilData soil) {
        float multiplier = 1.0f;
        
        // 水分量の適合度
        float moistureFit = 1.0f;
        if (soil.getMoisture() < minMoisture) {
            moistureFit = 0.0f; // 最低条件を満たさない
        } else if (maxMoisture >= 0) {
            // 最適範囲内かどうか
            float optimalMoisture = (minMoisture + maxMoisture) / 2.0f;
            float moistureDiff = Math.abs(soil.getMoisture() - optimalMoisture);
            float moistureRange = (maxMoisture - minMoisture) / 2.0f;
            moistureFit = Math.max(0.0f, 1.0f - (moistureDiff / moistureRange));
        }
        multiplier *= (0.5f + moistureFit * 0.5f); // 50%～100%の範囲
        
        // 肥沃度の影響
        float fertilityBonus = soil.getFertility() * fertilityImpact;
        multiplier *= (0.7f + fertilityBonus * 0.3f); // 70%～100%の範囲
        
        // 排水性の適合度（要求がある場合のみ）
        if (minDrainage >= 0 || maxDrainage >= 0) {
            float drainageFit = 1.0f;
            if (minDrainage >= 0 && soil.getDrainage() < minDrainage) {
                drainageFit = 0.5f; // 低い排水性はペナルティ
            } else if (maxDrainage >= 0 && soil.getDrainage() > maxDrainage) {
                drainageFit = 0.5f; // 高い排水性はペナルティ（米の場合）
            }
            multiplier *= drainageFit;
        }
        
        // 耕作難度の影響
        float tillagePenalty = soil.getTillageDifficulty() * 0.2f;
        multiplier *= (1.0f - tillagePenalty); // 最大20%のペナルティ
        
        return Math.max(0.1f, Math.min(1.0f, multiplier)); // 0.1～1.0の範囲にクランプ
    }
    
    /**
     * 土壌条件に基づいて収穫量の倍率を計算します（0.0～1.5）。
     */
    public float calculateYieldMultiplier(SoilData soil) {
        float multiplier = 1.0f;
        
        // 肥沃度の影響（収穫量に大きく影響）
        multiplier *= (0.5f + soil.getFertility() * fertilityImpact * 1.0f); // 50%～150%の範囲
        
        // 水分量の適合度
        float moistureFit = 1.0f;
        if (maxMoisture >= 0) {
            float optimalMoisture = (minMoisture + maxMoisture) / 2.0f;
            float moistureDiff = Math.abs(soil.getMoisture() - optimalMoisture);
            float moistureRange = (maxMoisture - minMoisture) / 2.0f;
            moistureFit = Math.max(0.5f, 1.0f - (moistureDiff / moistureRange));
        }
        multiplier *= moistureFit;
        
        return Math.max(0.3f, Math.min(1.5f, multiplier)); // 0.3～1.5の範囲にクランプ
    }
    
    // ゲッター
    public float getMinMoisture() { return minMoisture; }
    public float getMaxMoisture() { return maxMoisture; }
    public float getMinFertility() { return minFertility; }
    public float getMinDrainage() { return minDrainage; }
    public float getMaxDrainage() { return maxDrainage; }
    public float getMaxTillageDifficulty() { return maxTillageDifficulty; }
    public float getFertilityImpact() { return fertilityImpact; }
}
