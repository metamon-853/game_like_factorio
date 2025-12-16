package io.github.some_example_name.game;

/**
 * 文明レベルを管理するクラス。
 */
public class CivilizationLevel {
    private int level;
    
    // 文明レベルの最大値
    public static final int MAX_LEVEL = 10;
    
    // 文明レベル1の名前
    public static final String LEVEL_1_NAME = "原始文明";
    // 文明レベル2の名前
    public static final String LEVEL_2_NAME = "農耕文明";
    
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
            case 3: return "青銅器文明";
            case 4: return "鉄器文明";
            case 5: return "中世文明";
            case 6: return "産業革命文明";
            case 7: return "電気・化学文明";
            case 8: return "情報文明";
            case 9: return "宇宙文明";
            case 10: return "超文明";
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
