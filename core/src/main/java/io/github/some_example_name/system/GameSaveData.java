package io.github.some_example_name.system;

import java.util.ArrayList;
import java.util.List;

/**
 * ゲームのセーブデータを表すクラス。
 */
public class GameSaveData {
    // プレイヤーの状態
    public int playerTileX;
    public int playerTileY;
    
    // アイテムマネージャーの状態
    public int collectedCount;
    public List<ItemData> items;
    
    // 設定
    public boolean showGrid;
    public float masterVolume;
    public boolean isMuted;
    public float cameraZoom;
    
    // 文明レベル
    public int civilizationLevel;
    
    /**
     * アイテムのデータを表す内部クラス。
     */
    public static class ItemData {
        public int tileX;
        public int tileY;
        public String type; // ItemTypeの名前（RED, BLUE, YELLOW, PURPLE）
        
        public ItemData() {
            // JSONデシリアライズ用のデフォルトコンストラクタ
        }
        
        public ItemData(int tileX, int tileY, String type) {
            this.tileX = tileX;
            this.tileY = tileY;
            this.type = type;
        }
    }
    
    public GameSaveData() {
        // JSONデシリアライズ用のデフォルトコンストラクタ
        this.items = new ArrayList<>();
    }
}
