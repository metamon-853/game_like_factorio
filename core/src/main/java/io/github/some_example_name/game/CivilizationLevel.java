package io.github.some_example_name.game;

/**
 * 文明レベルを管理するクラス。
 * 
 * 文明レベルの進行条件（assets/civilization/civilization_jouken.md参照）:
 * - レベル1 (旧石器時代): ゲーム開始時
 * - レベル2 (新石器時代): 定住拠点を設立する、土器を1つ作成する、作物を1回収穫する
 * - レベル3 (青銅器時代): 銅を精錬できる環境を整える、食料を一定量貯蔵する
 * - レベル4 (鉄器時代): 高温炉を建設する、鉄鉱石を入手する
 * - レベル5 (古代文明時代): 鉄製の恒久建築物を1つ完成させる、食料生産を安定させる
 */
public class CivilizationLevel {
    private int level;
    
    // 文明レベルの最大値
    public static final int MAX_LEVEL = 5;
    
    // 文明レベル名の定数
    public static final String LEVEL_1_NAME = "旧石器時代";
    public static final String LEVEL_2_NAME = "新石器時代";
    public static final String LEVEL_3_NAME = "青銅器時代";
    public static final String LEVEL_4_NAME = "鉄器時代";
    public static final String LEVEL_5_NAME = "古代文明時代";
    
    public CivilizationLevel() {
        this.level = 1; // ゲーム開始時はレベル1
    }
    
    public CivilizationLevel(int level) {
        this.level = Math.max(1, Math.min(MAX_LEVEL, level));
    }
    
    /**
     * 現在の文明レベルを返します。
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * 文明レベルを設定します。
     */
    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(MAX_LEVEL, level));
    }
    
    /**
     * 文明レベルを上げます。
     * @return レベルアップに成功した場合true
     */
    public boolean levelUp() {
        if (level < MAX_LEVEL) {
            level++;
            return true;
        }
        return false;
    }
    
    /**
     * 文明レベルの名前を返します。
     */
    public String getLevelName() {
        switch (level) {
            case 1: return LEVEL_1_NAME;
            case 2: return LEVEL_2_NAME;
            case 3: return LEVEL_3_NAME;
            case 4: return LEVEL_4_NAME;
            case 5: return LEVEL_5_NAME;
            default: return "未知の文明";
        }
    }
    
    /**
     * 指定された文明レベルで利用可能なアイテムかどうかを判定します。
     * @param itemCivilizationLevel アイテムの文明レベル
     * @return 利用可能な場合true
     */
    public boolean isItemAvailable(int itemCivilizationLevel) {
        return itemCivilizationLevel <= level;
    }
}
