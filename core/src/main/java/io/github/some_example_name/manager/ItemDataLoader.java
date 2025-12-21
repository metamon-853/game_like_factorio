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
        FileHandle file = Gdx.files.internal("items/entity.csv");
        if (!file.exists()) {
            Gdx.app.error("ItemDataLoader", "entity.csv not found at: items/entity.csv");
            return;
        }
        
        try {
            String content = file.readString();
            // 改行コードを統一（\r\n -> \n）
            content = content.replace("\r\n", "\n").replace("\r", "\n");
            String[] lines = content.split("\n");
            
            if (lines.length < 2) {
                Gdx.app.error("ItemDataLoader", "entity.csv has no data rows");
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
                if (parts.size() < 4) {
                    Gdx.app.log("ItemDataLoader", "Skipping invalid line: " + line);
                    continue;
                }
                
                // カテゴリが"動物"の場合はスキップ（家畜データはLivestockDataLoaderで処理）
                String category = parts.get(1).trim();
                if ("動物".equals(category)) {
                    continue;
                }
                
                ItemData itemData = new ItemData();
                try {
                    itemData.id = Integer.parseInt(parts.get(0).trim());
                } catch (NumberFormatException e) {
                    Gdx.app.log("ItemDataLoader", "Invalid item ID: " + parts.get(0));
                    continue;
                }
                itemData.category = category;
                itemData.name = parts.get(2).trim();
                itemData.description = parts.get(3).trim();
                
                // 素材情報を読み込む（5番目のカラム、存在する場合）
                if (parts.size() >= 5 && !parts.get(4).trim().isEmpty()) {
                    String materialsStr = parts.get(4).trim();
                    // 引用符を除去
                    if (materialsStr.startsWith("\"") && materialsStr.endsWith("\"")) {
                        materialsStr = materialsStr.substring(1, materialsStr.length() - 1);
                    }
                    Map<Integer, Integer> materials = parseMaterials(materialsStr);
                    itemData.setMaterials(materials);
                }
                
                // 要求条件情報を読み込む（6番目のカラム、存在する場合）
                if (parts.size() >= 6 && !parts.get(5).trim().isEmpty()) {
                    String requirementsStr = parts.get(5).trim();
                    // 引用符を除去
                    if (requirementsStr.startsWith("\"") && requirementsStr.endsWith("\"")) {
                        requirementsStr = requirementsStr.substring(1, requirementsStr.length() - 1);
                    }
                    Map<String, Integer> requirements = parseRequirements(requirementsStr);
                    itemData.setRequirements(requirements);
                }
                
                // 農具の耐久値を読み込む（7番目のカラム、存在する場合）
                if (parts.size() >= 7 && !parts.get(6).trim().isEmpty()) {
                    try {
                        int durability = Integer.parseInt(parts.get(6).trim());
                        itemData.setToolDurability(durability);
                    } catch (NumberFormatException e) {
                        Gdx.app.log("ItemDataLoader", "Invalid tool durability: " + parts.get(6));
                    }
                }
                
                // 農具の効率を読み込む（8番目のカラム、存在する場合）
                if (parts.size() >= 8 && !parts.get(7).trim().isEmpty()) {
                    try {
                        float efficiency = Float.parseFloat(parts.get(7).trim());
                        itemData.setToolEfficiency(efficiency);
                    } catch (NumberFormatException e) {
                        Gdx.app.log("ItemDataLoader", "Invalid tool efficiency: " + parts.get(7));
                    }
                }
                
                // 水条件を読み込む（9番目のカラム、存在する場合）
                if (parts.size() >= 9 && !parts.get(8).trim().isEmpty()) {
                    String waterRequirement = parts.get(8).trim();
                    itemData.setRequiresWater("true".equalsIgnoreCase(waterRequirement) || "NEAR_WATER".equalsIgnoreCase(waterRequirement));
                }
                
                // 種の場合は土壌条件を自動設定
                if ("植物".equals(category) && itemData.name != null && itemData.name.contains("種")) {
                    itemData.setSoilRequirementsFromSeedId(itemData.id);
                }
                
                // その他のフィールドはItemDataのデフォルト値を使用
                
                itemDataMap.put(itemData.id, itemData);
                itemDataList.add(itemData);
            }
        } catch (Exception e) {
            Gdx.app.error("ItemDataLoader", "Error loading entity.csv: " + e.getMessage());
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
     * 要求条件情報の文字列をパースします。
     * フォーマット: "タイプ:アイテムID,タイプ:アイテムID"
     * 例: "tool:51" → 道具ID51が必要
     * 例: "facility:34" → 施設ID34が必要
     * @param requirementsStr 要求条件情報の文字列
     * @return 要求条件マップ（キー: タイプ、値: アイテムID）
     */
    private Map<String, Integer> parseRequirements(String requirementsStr) {
        Map<String, Integer> requirements = new HashMap<>();
        
        if (requirementsStr == null || requirementsStr.trim().isEmpty()) {
            return requirements;
        }
        
        // カンマで分割
        String[] requirementPairs = requirementsStr.split(",");
        for (String pair : requirementPairs) {
            pair = pair.trim();
            if (pair.isEmpty()) {
                continue;
            }
            
            // コロンで分割
            String[] parts = pair.split(":");
            if (parts.length != 2) {
                Gdx.app.log("ItemDataLoader", "Invalid requirement format: " + pair);
                continue;
            }
            
            try {
                String type = parts[0].trim();
                int itemId = Integer.parseInt(parts[1].trim());
                requirements.put(type, itemId);
            } catch (NumberFormatException e) {
                Gdx.app.log("ItemDataLoader", "Invalid requirement type or item ID: " + pair);
            }
        }
        
        return requirements;
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
