package io.github.some_example_name.manager;

import io.github.some_example_name.entity.LivestockData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

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
            return;
        }
        
        String content = file.readString();
        // 改行コードを統一（\r\n -> \n）
        content = io.github.some_example_name.util.CSVParser.normalizeLineEndings(content);
        String[] lines = content.split("\n");
        
        if (lines.length < 2) {
            return;
        }
        
        try {
            // ヘッダー行をスキップ（最初の行）
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                // CSVのパース（引用符を考慮したカンマ区切り）
                List<String> parts;
                try {
                    parts = io.github.some_example_name.util.CSVParser.parseCSVLine(line);
                } catch (Exception e) {
                    continue;
                }
                
                if (parts.size() < 5) {
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
                    livestockData.name = parts.get(3).trim(); // nameは4番目のカラム（インデックス3）
                    livestockData.description = parts.get(4).trim(); // descriptionは5番目のカラム（インデックス4）
                    
                    // CSVから家畜属性を読み込む（カラムが存在する場合）
                    // ヘッダー: id,category,item_type,name,description,materials,requirements,tool_durability,tool_efficiency,water_requirement,meat_item_id,product_item_id,product_interval,required_civilization_level,color_r,color_g,color_b
                    // 実際のインデックス: 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16
                    // パース結果から: parts[9]=meat_item_id, parts[10]=product_item_id, parts[11]=product_interval, parts[12]=required_civilization_level, parts[13]=color_r, parts[14]=color_g, parts[15]=color_b
                    if (parts.size() > 9) {
                        String meatItemIdStr = parts.get(9).trim();
                        if (!meatItemIdStr.isEmpty()) {
                            // meat_item_id (実際のインデックス9)
                            livestockData.meatItemId = Integer.parseInt(meatItemIdStr);
                        }
                    }
                    if (parts.size() > 10) {
                        String productIdStr = parts.get(10).trim();
                        if (!productIdStr.isEmpty() && !"-1".equals(productIdStr)) {
                            // product_item_id (実際のインデックス10)
                            livestockData.productItemId = Integer.parseInt(productIdStr);
                        } else {
                            livestockData.productItemId = -1;
                        }
                    }
                    if (parts.size() > 11) {
                        String productIntervalStr = parts.get(11).trim();
                        if (!productIntervalStr.isEmpty()) {
                            // product_interval (実際のインデックス11)
                            livestockData.productInterval = Float.parseFloat(productIntervalStr);
                        }
                    }
                    if (parts.size() > 12) {
                        String civLevelStr = parts.get(12).trim();
                        if (!civLevelStr.isEmpty()) {
                            // required_civilization_level (実際のインデックス12)
                            livestockData.requiredCivilizationLevel = Integer.parseInt(civLevelStr);
                        }
                    }
                    if (parts.size() > 15) {
                        // color_r, color_g, color_b (実際のインデックス13, 14, 15)
                        String colorRStr = parts.get(13).trim();
                        String colorGStr = parts.get(14).trim();
                        String colorBStr = parts.get(15).trim();
                        if (!colorRStr.isEmpty() && !colorGStr.isEmpty() && !colorBStr.isEmpty()) {
                            float r = Float.parseFloat(colorRStr);
                            float g = Float.parseFloat(colorGStr);
                            float b = Float.parseFloat(colorBStr);
                            livestockData.setColor(r, g, b);
                        }
                    }
                    
                    // CSVにデータが存在しない場合は、デフォルト値を設定（後方互換性のため）
                    if (livestockData.meatItemId == -1 && livestockData.productItemId == -1 && livestockData.productInterval == 0.0f) {
                        setupLivestockAttributes(livestockData);
                    }
                    
                    // データを追加（例外が発生しなかった場合のみ）
                    livestockDataMap.put(livestockData.id, livestockData);
                    livestockDataList.add(livestockData);
                    
                } catch (NumberFormatException e) {
                    // 無効な数値フォーマットの行はスキップ
                    continue;
                } catch (Exception e) {
                    // その他の例外
                    continue;
                }
            }
            
        } catch (Exception e) {
            // エラーは静かに処理
        }
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
        if (livestockDataList == null) {
            return new Array<>();
        }
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
                break;
        }
    }
    
}
