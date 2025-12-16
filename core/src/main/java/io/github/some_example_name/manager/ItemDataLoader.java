package io.github.some_example_name.manager;

import io.github.some_example_name.entity.ItemData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * アイテムデータをCSVファイルから読み込むクラス。
 */
public class ItemDataLoader {
    private Map<Integer, ItemData> itemDataMap;
    private Array<ItemData> itemDataList;
    
    public ItemDataLoader() {
        this.itemDataMap = new HashMap<>();
        this.itemDataList = new Array<>();
        loadItemData();
    }
    
    /**
     * CSVファイルからアイテムデータを読み込みます。
     */
    private void loadItemData() {
        FileHandle file = Gdx.files.internal("items/items.csv");
        if (!file.exists()) {
            Gdx.app.error("ItemDataLoader", "items.csv not found at: items/items.csv");
            return;
        }
        
        try {
            String content = file.readString();
            // 改行コードを統一（\r\n -> \n）
            content = content.replace("\r\n", "\n").replace("\r", "\n");
            String[] lines = content.split("\n");
            
            if (lines.length < 2) {
                Gdx.app.error("ItemDataLoader", "items.csv has no data rows");
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
                if (parts.size() < 3) {
                    Gdx.app.log("ItemDataLoader", "Skipping invalid line: " + line);
                    continue;
                }
                
                ItemData itemData = new ItemData();
                try {
                    itemData.id = Integer.parseInt(parts.get(0).trim());
                } catch (NumberFormatException e) {
                    Gdx.app.log("ItemDataLoader", "Invalid item ID: " + parts.get(0));
                    continue;
                }
                itemData.name = parts.get(1).trim();
                itemData.description = parts.get(2).trim();
                
                // 素材情報を読み込む（4番目のカラム、存在する場合）
                if (parts.size() >= 4 && !parts.get(3).trim().isEmpty()) {
                    String materialsStr = parts.get(3).trim();
                    // 引用符を除去
                    if (materialsStr.startsWith("\"") && materialsStr.endsWith("\"")) {
                        materialsStr = materialsStr.substring(1, materialsStr.length() - 1);
                    }
                    Map<Integer, Integer> materials = parseMaterials(materialsStr);
                    itemData.setMaterials(materials);
                }
                
                // その他のフィールドはItemDataのデフォルト値を使用
                
                itemDataMap.put(itemData.id, itemData);
                itemDataList.add(itemData);
            }
        } catch (Exception e) {
            Gdx.app.error("ItemDataLoader", "Error loading items.csv: " + e.getMessage());
            e.printStackTrace();
        }
        
        Gdx.app.log("ItemDataLoader", "Loaded " + itemDataMap.size() + " items");
    }
    
    /**
     * アイテムIDからアイテムデータを取得します。
     */
    public ItemData getItemData(int itemId) {
        return itemDataMap.get(itemId);
    }
    
    /**
     * 指定された文明レベルで利用可能なアイテムのリストを取得します。
     */
    public Array<ItemData> getAvailableItems(int civilizationLevel) {
        Array<ItemData> available = new Array<>();
        for (ItemData itemData : itemDataList) {
            if (itemData.getCivilizationLevel() <= civilizationLevel) {
                available.add(itemData);
            }
        }
        return available;
    }
    
    /**
     * すべてのアイテムデータのリストを取得します。
     */
    public Array<ItemData> getAllItems() {
        return itemDataList;
    }
    
    /**
     * 素材情報の文字列をパースします。
     * フォーマット: "アイテムID:必要数,アイテムID:必要数"
     * 例: "1:2,3:1" → アイテムID1が2個、アイテムID3が1個必要
     * @param materialsStr 素材情報の文字列
     * @return 素材マップ（キー: アイテムID、値: 必要数）
     */
    private Map<Integer, Integer> parseMaterials(String materialsStr) {
        Map<Integer, Integer> materials = new HashMap<>();
        
        if (materialsStr == null || materialsStr.trim().isEmpty()) {
            return materials;
        }
        
        // カンマで分割
        String[] materialPairs = materialsStr.split(",");
        for (String pair : materialPairs) {
            pair = pair.trim();
            if (pair.isEmpty()) {
                continue;
            }
            
            // コロンで分割
            String[] parts = pair.split(":");
            if (parts.length != 2) {
                Gdx.app.log("ItemDataLoader", "Invalid material format: " + pair);
                continue;
            }
            
            try {
                int itemId = Integer.parseInt(parts[0].trim());
                int amount = Integer.parseInt(parts[1].trim());
                if (amount > 0) {
                    materials.put(itemId, amount);
                }
            } catch (NumberFormatException e) {
                Gdx.app.log("ItemDataLoader", "Invalid material ID or amount: " + pair);
            }
        }
        
        return materials;
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
