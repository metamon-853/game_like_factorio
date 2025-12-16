package io.github.some_example_name.manager;

import io.github.some_example_name.entity.ItemData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

/**
 * アイテムデータをCSVファイルから読み込むクラス。
 */
public class ItemDataLoader {
    private Map<Integer, ItemData> itemDataMap;
    private Array<ItemData> itemDataList;
    
    // 文明レベル1のアイテムID（定義から）
    private static final int[] LEVEL_1_ITEM_IDS = {
        14,      // wood (木材)
        15,      // stone (石)
        105     // raw_meat (生肉) - 仮のID、CSVにない場合は追加する必要がある
    };
    
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
            // フォールバック：生肉のみを追加
            addRawMeatItem();
            return;
        }
        
        try {
            String content = file.readString();
            // 改行コードを統一（\r\n -> \n）
            content = content.replace("\r\n", "\n").replace("\r", "\n");
            String[] lines = content.split("\n");
            
            if (lines.length < 2) {
                Gdx.app.error("ItemDataLoader", "items.csv has no data rows");
                addRawMeatItem();
                return;
            }
            
            // ヘッダー行をスキップ（最初の行）
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                // CSVのパース（カンマ区切り、ただし引用符内のカンマは考慮しない簡易版）
                String[] parts = parseCSVLine(line);
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
                
                // 文明レベルを設定（IDから判定）
                int civLevel = determineCivilizationLevel(itemData.id);
                itemData.setCivilizationLevel(civLevel);
                
                // ティアを設定（文明レベルと同じ値）
                itemData.tier = civLevel;
                
                // カテゴリを設定（IDから判定）
                itemData.category = determineCategory(itemData.id);
                
                // 色を設定（IDに基づいて）
                itemData.setColor(determineColor(itemData.id));
                
                itemDataMap.put(itemData.id, itemData);
                itemDataList.add(itemData);
            }
        } catch (Exception e) {
            Gdx.app.error("ItemDataLoader", "Error loading items.csv: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 生肉がCSVにない場合は追加（ID 105を使用）
        if (!itemDataMap.containsKey(105)) {
            addRawMeatItem();
        }
        
        // レベル1のアイテム（stone, wood）が存在することを確認
        ensureLevel1Items();
        
        Gdx.app.log("ItemDataLoader", "Loaded " + itemDataMap.size() + " items");
    }
    
    /**
     * 生肉アイテムを追加します。
     */
    private void addRawMeatItem() {
        ItemData rawMeat = new ItemData();
        rawMeat.id = 105;
        rawMeat.name = "Raw Meat";
        rawMeat.description = "Raw meat obtained from hunting. Can be eaten after cooking.";
        rawMeat.setCivilizationLevel(1);
        rawMeat.tier = 1;
        rawMeat.category = determineCategory(105);
        rawMeat.setColor(new Color(0.8f, 0.4f, 0.3f, 1.0f)); // 赤みがかった色
        
        itemDataMap.put(105, rawMeat);
        itemDataList.add(rawMeat);
    }
    
    /**
     * レベル1のアイテムが存在することを確認し、なければ追加します。
     */
    private void ensureLevel1Items() {
        // 石が存在しない場合は追加（ID 15）
        if (!itemDataMap.containsKey(15)) {
            ItemData stone = new ItemData();
            stone.id = 15;
            stone.name = "Stone";
            stone.description = "Basic material that forms the earth's crust.";
            stone.setCivilizationLevel(1);
            stone.tier = 1;
            stone.category = determineCategory(15);
            stone.setColor(new Color(0.5f, 0.5f, 0.5f, 1.0f)); // グレー
            itemDataMap.put(15, stone);
            itemDataList.add(stone);
        }
        
        // 木材が存在しない場合は追加（ID 14）
        if (!itemDataMap.containsKey(14)) {
            ItemData wood = new ItemData();
            wood.id = 14;
            wood.name = "Wood";
            wood.description = "Natural material obtained from trees.";
            wood.setCivilizationLevel(1);
            wood.tier = 1;
            wood.category = determineCategory(14);
            wood.setColor(new Color(0.6f, 0.4f, 0.2f, 1.0f)); // 茶色
            itemDataMap.put(14, wood);
            itemDataList.add(wood);
        }
    }
    
    /**
     * CSV行をパースします（簡易版）。
     */
    private String[] parseCSVLine(String line) {
        // 引用符を考慮しない簡易パース
        return line.split(",");
    }
    
    /**
     * アイテムの文明レベルを決定します。
     */
    private int determineCivilizationLevel(int itemId) {
        // レベル1のアイテムリストに含まれているかチェック
        for (int level1Id : LEVEL_1_ITEM_IDS) {
            if (level1Id == itemId) {
                return 1;
            }
        }
        
        // IDから簡易的に推測（IDが小さいほど基本的なアイテム）
        // より正確には、別のマッピングテーブルが必要
        if (itemId <= 20) {
            return 1;
        } else if (itemId <= 50) {
            return 2;
        } else if (itemId <= 75) {
            return 3;
        } else {
            return Math.min(10, (itemId / 15) + 1);
        }
    }
    
    /**
     * アイテムのカテゴリを決定します。
     */
    private String determineCategory(int itemId) {
        if (itemId <= 8) { // elements
            return "元素";
        } else if (itemId <= 11) { // compounds
            return "化合物";
        } else if (itemId <= 31) { // materials
            return "材料";
        } else if (itemId <= 50) { // components
            return "部品";
        } else if (itemId <= 60) { // tools
            return "工具";
        } else if (itemId <= 72) { // devices/vehicles/cultural
            return "装置・乗り物・文化";
        } else if (itemId <= 80) { // food
            return "食料";
        } else { // energy/phenomenon
            return "エネルギー・現象";
        }
    }
    
    /**
     * アイテムの色を決定します。
     */
    private Color determineColor(int itemId) {
        // 文明レベル1のアイテムの色を設定
        if (itemId == 15) { // stone
            return new Color(0.5f, 0.5f, 0.5f, 1.0f); // グレー
        } else if (itemId == 14) { // wood
            return new Color(0.6f, 0.4f, 0.2f, 1.0f); // 茶色
        } else if (itemId == 105) { // raw_meat
            return new Color(0.8f, 0.4f, 0.3f, 1.0f); // 赤みがかった色
        }
        
        // ID範囲に基づいて色を決定（簡易版）
        // より正確には、別のマッピングテーブルが必要
        if (itemId <= 8) { // elements
            return Color.CYAN;
        } else if (itemId <= 11) { // compounds
            return Color.BLUE;
        } else if (itemId <= 31) { // materials
            return Color.BROWN;
        } else if (itemId <= 50) { // components
            return Color.YELLOW;
        } else if (itemId <= 60) { // tools
            return Color.ORANGE;
        } else if (itemId <= 72) { // devices/vehicles/cultural
            return Color.PURPLE;
        } else if (itemId <= 80) { // food
            return new Color(0.8f, 0.4f, 0.3f, 1.0f);
        } else { // energy/phenomenon
            return Color.WHITE;
        }
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
