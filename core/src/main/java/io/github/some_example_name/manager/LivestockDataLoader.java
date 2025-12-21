package io.github.some_example_name.manager;

import io.github.some_example_name.entity.LivestockData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家畜データをCSVファイルから読み込むクラス。
 */
public class LivestockDataLoader {
    private Map<Integer, LivestockData> livestockDataMap;
    private Array<LivestockData> livestockDataList;
    
    public LivestockDataLoader() {
        this.livestockDataMap = new HashMap<>();
        this.livestockDataList = new Array<>();
        loadLivestockData();
    }
    
    /**
     * CSVファイルから家畜データを読み込みます。
     */
    private void loadLivestockData() {
        FileHandle file = Gdx.files.internal("items/entity.csv");
        if (!file.exists()) {
            Gdx.app.error("LivestockDataLoader", "entity.csv not found at: items/entity.csv");
            return;
        }
        
        try {
            String content = file.readString();
            // 改行コードを統一（\r\n -> \n）
            content = content.replace("\r\n", "\n").replace("\r", "\n");
            String[] lines = content.split("\n");
            
            if (lines.length < 2) {
                Gdx.app.error("LivestockDataLoader", "entity.csv has no data rows");
                return;
            }
            
            // ヘッダー行をスキップ（最初の行）
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                // CSVのパース（引用符を考慮したカンマ区切り）
                List<String> parts = parseCSVLine(line);
                if (parts.size() < 5) {
                    Gdx.app.log("LivestockDataLoader", "Skipping invalid line (need at least 5 columns): " + line);
                    continue;
                }
                
                // カテゴリが"動物"の場合のみ処理
                String category = parts.get(1).trim();
                if (!"動物".equals(category)) {
                    continue;
                }
                
                LivestockData livestockData = new LivestockData();
                try {
                    livestockData.id = Integer.parseInt(parts.get(0).trim());
                    livestockData.name = parts.get(2).trim();
                    livestockData.description = parts.get(3).trim();
                    
                    // 家畜IDから対応する肉IDと製品IDを自動設定
                    setupLivestockAttributes(livestockData);
                    
                } catch (NumberFormatException e) {
                    Gdx.app.log("LivestockDataLoader", "Invalid number format in line: " + line + " - " + e.getMessage());
                    continue;
                }
                
                livestockDataMap.put(livestockData.id, livestockData);
                livestockDataList.add(livestockData);
            }
        } catch (Exception e) {
            Gdx.app.error("LivestockDataLoader", "Error loading entity.csv: " + e.getMessage());
            e.printStackTrace();
        }
        
        Gdx.app.log("LivestockDataLoader", "Loaded " + livestockDataMap.size() + " livestock types");
    }
    
    /**
     * 家畜IDから家畜データを取得します。
     */
    public LivestockData getLivestockData(int livestockId) {
        return livestockDataMap.get(livestockId);
    }
    
    /**
     * すべての家畜データのリストを取得します。
     */
    public Array<LivestockData> getAllLivestock() {
        return livestockDataList;
    }
    
    /**
     * 家畜IDに基づいて属性（肉ID、製品ID、色、生産間隔）を設定します。
     * @param livestockData 設定する家畜データ
     */
    private void setupLivestockAttributes(LivestockData livestockData) {
        switch (livestockData.id) {
            case 19: // 鶏
                livestockData.meatItemId = 25; // 鶏肉
                livestockData.productItemId = 26; // 卵
                livestockData.productInterval = 8.0f; // 8秒ごとに卵を産む
                livestockData.requiredCivilizationLevel = 1; // 旧石器時代から利用可能
                livestockData.setColor(1.0f, 0.9f, 0.7f); // 薄い黄色（鶏の色）
                break;
            case 20: // 豚
                livestockData.meatItemId = 27; // 豚肉
                livestockData.productItemId = -1; // 製品なし
                livestockData.productInterval = 0.0f;
                livestockData.requiredCivilizationLevel = 1; // 旧石器時代から利用可能
                livestockData.setColor(0.9f, 0.7f, 0.8f); // ピンクがかった色
                break;
            case 21: // 羊
                livestockData.meatItemId = 28; // 羊肉
                livestockData.productItemId = 29; // 羊毛
                livestockData.productInterval = 15.0f; // 15秒ごとに羊毛を生産
                livestockData.requiredCivilizationLevel = 2; // 新石器時代から利用可能
                livestockData.setColor(0.95f, 0.95f, 0.95f); // 白（羊の色）
                break;
            case 22: // 山羊
                livestockData.meatItemId = 30; // 山羊肉
                livestockData.productItemId = 31; // ヤギミルク
                livestockData.productInterval = 10.0f; // 10秒ごとにミルクを生産
                livestockData.requiredCivilizationLevel = 2; // 新石器時代から利用可能
                livestockData.setColor(0.6f, 0.5f, 0.4f); // 茶色（山羊の色）
                break;
            case 23: // 牛
                livestockData.meatItemId = 32; // 牛肉
                livestockData.productItemId = 33; // 牛乳
                livestockData.productInterval = 12.0f; // 12秒ごとにミルクを生産
                livestockData.requiredCivilizationLevel = 3; // 青銅器時代から利用可能
                livestockData.setColor(0.4f, 0.3f, 0.2f); // 濃い茶色（牛の色）
                break;
            case 24: // 馬
                livestockData.meatItemId = 34; // 馬肉
                livestockData.productItemId = -1; // 製品なし
                livestockData.productInterval = 0.0f;
                livestockData.requiredCivilizationLevel = 4; // 鉄器時代から利用可能
                livestockData.setColor(0.5f, 0.4f, 0.3f); // 茶色（馬の色）
                break;
            default:
                // 未知の家畜IDの場合はデフォルト値
                livestockData.meatItemId = -1;
                livestockData.productItemId = -1;
                livestockData.productInterval = 8.0f;
                livestockData.requiredCivilizationLevel = 1;
                livestockData.setColor(1.0f, 1.0f, 1.0f);
                Gdx.app.log("LivestockDataLoader", "Unknown livestock ID: " + livestockData.id + ", using default values");
                break;
        }
    }
    
    /**
     * CSV行をパースします（引用符を考慮）。
     * @param line CSV行
     * @return カラムのリスト
     */
    private List<String> parseCSVLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // 引用符の開始/終了
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // エスケープされた引用符（""）
                    current.append('"');
                    i++; // 次の文字をスキップ
                } else {
                    // 引用符の開始/終了を切り替え
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // 引用符の外側のカンマは区切り文字
                parts.add(current.toString());
                current = new StringBuilder();
            } else {
                // 通常の文字
                current.append(c);
            }
        }
        
        // 最後のカラムを追加
        parts.add(current.toString());
        
        return parts;
    }
}
