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
        FileHandle file = Gdx.files.internal("livestock/livestock.csv");
        if (!file.exists()) {
            Gdx.app.error("LivestockDataLoader", "livestock.csv not found at: livestock/livestock.csv");
            return;
        }
        
        try {
            String content = file.readString();
            // 改行コードを統一（\r\n -> \n）
            content = content.replace("\r\n", "\n").replace("\r", "\n");
            String[] lines = content.split("\n");
            
            if (lines.length < 2) {
                Gdx.app.error("LivestockDataLoader", "livestock.csv has no data rows");
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
                if (parts.size() < 9) {
                    Gdx.app.log("LivestockDataLoader", "Skipping invalid line (need at least 9 columns): " + line);
                    continue;
                }
                
                LivestockData livestockData = new LivestockData();
                try {
                    livestockData.id = Integer.parseInt(parts.get(0).trim());
                    livestockData.name = parts.get(1).trim();
                    livestockData.description = parts.get(2).trim();
                    livestockData.meatItemId = Integer.parseInt(parts.get(3).trim());
                    
                    String productItemIdStr = parts.get(4).trim();
                    if (productItemIdStr.equals("-1") || productItemIdStr.isEmpty()) {
                        livestockData.productItemId = -1;
                    } else {
                        livestockData.productItemId = Integer.parseInt(productItemIdStr);
                    }
                    
                    livestockData.productInterval = Float.parseFloat(parts.get(5).trim());
                    
                    float r = Float.parseFloat(parts.get(6).trim());
                    float g = Float.parseFloat(parts.get(7).trim());
                    float b = Float.parseFloat(parts.get(8).trim());
                    livestockData.setColor(r, g, b);
                    
                } catch (NumberFormatException e) {
                    Gdx.app.log("LivestockDataLoader", "Invalid number format in line: " + line + " - " + e.getMessage());
                    continue;
                }
                
                livestockDataMap.put(livestockData.id, livestockData);
                livestockDataList.add(livestockData);
            }
        } catch (Exception e) {
            Gdx.app.error("LivestockDataLoader", "Error loading livestock.csv: " + e.getMessage());
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
