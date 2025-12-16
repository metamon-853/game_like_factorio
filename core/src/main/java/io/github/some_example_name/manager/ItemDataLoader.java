package io.github.some_example_name.manager;

import io.github.some_example_name.entity.ItemData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
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
                
                // CSVのパース（カンマ区切り）
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    Gdx.app.log("ItemDataLoader", "Skipping invalid line: " + line);
                    continue;
                }
                
                ItemData itemData = new ItemData();
                try {
                    itemData.id = Integer.parseInt(parts[0].trim());
                } catch (NumberFormatException e) {
                    Gdx.app.log("ItemDataLoader", "Invalid item ID: " + parts[0]);
                    continue;
                }
                itemData.name = parts[1].trim();
                itemData.description = parts[2].trim();
                
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
}
