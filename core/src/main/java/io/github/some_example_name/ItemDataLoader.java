package io.github.some_example_name;

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
    private Map<String, ItemData> itemDataMap;
    private Array<ItemData> itemDataList;
    
    // 文明レベル1のアイテムID（定義から）
    private static final String[] LEVEL_1_ITEM_IDS = {
        "stone",      // 石
        "wood",       // 木材
        "raw_meat"    // 生肉（CSVにない場合は追加する必要がある）
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
                if (parts.length < 6) {
                    Gdx.app.log("ItemDataLoader", "Skipping invalid line: " + line);
                    continue;
                }
                
                ItemData itemData = new ItemData();
                itemData.id = parts[0].trim();
                itemData.name = parts[1].trim();
                itemData.description = parts[2].trim();
                try {
                    itemData.tier = Integer.parseInt(parts[3].trim());
                } catch (NumberFormatException e) {
                    itemData.tier = 0;
                }
                itemData.category = parts[4].trim();
                itemData.icon = parts[5].trim();
                
                // 文明レベルを設定（tierから推測、またはレベル1のアイテムリストから判定）
                itemData.setCivilizationLevel(determineCivilizationLevel(itemData.id, itemData.tier));
                
                // 色を設定（カテゴリやIDに基づいて）
                itemData.setColor(determineColor(itemData.id, itemData.category));
                
                itemDataMap.put(itemData.id, itemData);
                itemDataList.add(itemData);
            }
        } catch (Exception e) {
            Gdx.app.error("ItemDataLoader", "Error loading items.csv: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 生肉がCSVにない場合は追加
        if (!itemDataMap.containsKey("raw_meat")) {
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
        rawMeat.id = "raw_meat";
        rawMeat.name = "生肉";
        rawMeat.description = "狩猟で得られる生の肉。調理すると食べられる。";
        rawMeat.tier = 1;
        rawMeat.category = "food";
        rawMeat.icon = "icons/raw_meat.png";
        rawMeat.setCivilizationLevel(1);
        rawMeat.setColor(new Color(0.8f, 0.4f, 0.3f, 1.0f)); // 赤みがかった色
        
        itemDataMap.put("raw_meat", rawMeat);
        itemDataList.add(rawMeat);
    }
    
    /**
     * レベル1のアイテムが存在することを確認し、なければ追加します。
     */
    private void ensureLevel1Items() {
        // 石が存在しない場合は追加
        if (!itemDataMap.containsKey("stone")) {
            ItemData stone = new ItemData();
            stone.id = "stone";
            stone.name = "石";
            stone.description = "地殻を構成する基本的な材料。";
            stone.tier = 1;
            stone.category = "material";
            stone.icon = "icons/stone.png";
            stone.setCivilizationLevel(1);
            stone.setColor(new Color(0.5f, 0.5f, 0.5f, 1.0f)); // グレー
            itemDataMap.put("stone", stone);
            itemDataList.add(stone);
        }
        
        // 木材が存在しない場合は追加
        if (!itemDataMap.containsKey("wood")) {
            ItemData wood = new ItemData();
            wood.id = "wood";
            wood.name = "木材";
            wood.description = "樹木から得られる天然材料。";
            wood.tier = 1;
            wood.category = "material";
            wood.icon = "icons/wood.png";
            wood.setCivilizationLevel(1);
            wood.setColor(new Color(0.6f, 0.4f, 0.2f, 1.0f)); // 茶色
            itemDataMap.put("wood", wood);
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
    private int determineCivilizationLevel(String itemId, int tier) {
        // レベル1のアイテムリストに含まれているかチェック
        for (String level1Id : LEVEL_1_ITEM_IDS) {
            if (level1Id.equals(itemId)) {
                return 1;
            }
        }
        
        // tierから推測（簡易版：tier 0-1はレベル1、tier 2はレベル2など）
        // より正確には、別のマッピングテーブルが必要
        if (tier <= 1) {
            return 1;
        } else if (tier == 2) {
            return 2;
        } else {
            return Math.min(10, (tier / 2) + 1);
        }
    }
    
    /**
     * アイテムの色を決定します。
     */
    private Color determineColor(String itemId, String category) {
        // 文明レベル1のアイテムの色を設定
        if ("stone".equals(itemId)) {
            return new Color(0.5f, 0.5f, 0.5f, 1.0f); // グレー
        } else if ("wood".equals(itemId)) {
            return new Color(0.6f, 0.4f, 0.2f, 1.0f); // 茶色
        } else if ("raw_meat".equals(itemId)) {
            return new Color(0.8f, 0.4f, 0.3f, 1.0f); // 赤みがかった色
        }
        
        // カテゴリに基づいて色を決定
        switch (category) {
            case "element":
                return Color.CYAN;
            case "compound":
                return Color.BLUE;
            case "material":
                return Color.BROWN;
            case "component":
                return Color.YELLOW;
            case "tool":
                return Color.ORANGE;
            case "device":
                return Color.PURPLE;
            case "food":
                return new Color(0.8f, 0.4f, 0.3f, 1.0f);
            default:
                return Color.WHITE;
        }
    }
    
    /**
     * アイテムIDからアイテムデータを取得します。
     */
    public ItemData getItemData(String itemId) {
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
