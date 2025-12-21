package io.github.some_example_name.manager;

import io.github.some_example_name.entity.TerrainTile;
import io.github.some_example_name.entity.ItemData;
import io.github.some_example_name.game.Inventory;
import com.badlogic.gdx.Gdx;

/**
 * 地形変換を管理するクラス。
 */
public class TerrainConversionManager {
    private TerrainManager terrainManager;
    private Inventory inventory;
    private ItemDataLoader itemDataLoader;
    
    public TerrainConversionManager() {
    }
    
    /**
     * 地形マネージャーを設定します。
     */
    public void setTerrainManager(TerrainManager terrainManager) {
        this.terrainManager = terrainManager;
    }
    
    /**
     * インベントリを設定します。
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    
    /**
     * アイテムデータローダーを設定します。
     */
    public void setItemDataLoader(ItemDataLoader itemDataLoader) {
        this.itemDataLoader = itemDataLoader;
    }
    
    /**
     * 指定されたタイル位置で地形変換を試みます。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @param toolItemId 使用する道具のアイテムID
     * @return 変換に成功した場合true
     */
    public boolean tryConvertTerrain(int tileX, int tileY, int toolItemId) {
        if (terrainManager == null || inventory == null || itemDataLoader == null) {
            return false;
        }
        
        // 道具のデータを取得
        ItemData toolData = itemDataLoader.getItemData(toolItemId);
        if (toolData == null || !toolData.isTool()) {
            return false;
        }
        
        // インベントリに道具があるかチェック
        if (inventory.getItemCount(toolItemId) <= 0) {
            Gdx.app.log("TerrainConversion", "道具がありません: " + toolData.name);
            return false;
        }
        
        // 現在の地形を取得
        TerrainTile currentTile = terrainManager.getTerrainTile(tileX, tileY);
        if (currentTile == null) {
            return false;
        }
        
        TerrainTile.TerrainType currentType = currentTile.getTerrainType();
        
        // 水路は畑や水田の上には掘れない
        if (currentType == TerrainTile.TerrainType.FARMLAND ||
            currentType == TerrainTile.TerrainType.PADDY) {
            Gdx.app.log("TerrainConversion", "畑や水田の上には水路を掘れません。まず畑や水田を壊してください。");
            return false;
        }
        
        // 水路はSTONEやWATERの上には掘れない
        if (currentType == TerrainTile.TerrainType.STONE ||
            currentType == TerrainTile.TerrainType.WATER) {
            Gdx.app.log("TerrainConversion", "岩や水の上には水路を掘れません。");
            return false;
        }
        
        TerrainTile.TerrainType newType = null;
        
        // 道具のIDに基づいて変換先を決定
        // 鍬 (51-53): DIRT → FARMLAND
        if (toolItemId >= 51 && toolItemId <= 53) {
            if (currentType == TerrainTile.TerrainType.DIRT) {
                newType = TerrainTile.TerrainType.FARMLAND;
            } else {
                Gdx.app.log("TerrainConversion", "鍬は土（DIRT）にのみ使用できます");
                return false;
            }
        }
        // 排水シャベル (54-56): MARSH → DRAINED_MARSH または DIRT/GRASS/DRAINED_MARSH → WATER_CHANNEL
        else if (toolItemId >= 54 && toolItemId <= 56) {
            // 水路の掘削（DIRT、GRASS、DRAINED_MARSHをCHANNELに変換）
            if (currentType == TerrainTile.TerrainType.DIRT ||
                currentType == TerrainTile.TerrainType.GRASS ||
                currentType == TerrainTile.TerrainType.DRAINED_MARSH) {
                // 水路は畑や水田の上には掘れない（既にチェック済み）
                newType = TerrainTile.TerrainType.WATER_CHANNEL;
            }
            // 湿地の排水（MARSH → DRAINED_MARSH）
            else if (currentType == TerrainTile.TerrainType.MARSH) {
                newType = TerrainTile.TerrainType.DRAINED_MARSH;
            } else {
                Gdx.app.log("TerrainConversion", "排水シャベルは土（DIRT）、草（GRASS）、排水後湿地（DRAINED_MARSH）、または湿地（MARSH）にのみ使用できます");
                return false;
            }
        }
        // 区画整理具 (57-59): DRAINED_MARSH → PADDY
        else if (toolItemId >= 57 && toolItemId <= 59) {
            if (currentType == TerrainTile.TerrainType.DRAINED_MARSH) {
                // 水田の水源チェック
                if (!terrainManager.isNearWaterSource(tileX, tileY)) {
                    Gdx.app.log("TerrainConversion", "水田を作るには水源（WATER、WATER_CHANNEL、PADDY）に隣接している必要があります");
                    return false;
                }
                newType = TerrainTile.TerrainType.PADDY;
            } else {
                Gdx.app.log("TerrainConversion", "区画整理具は排水後湿地（DRAINED_MARSH）にのみ使用できます");
                return false;
            }
        } else {
            Gdx.app.log("TerrainConversion", "この道具は地形変換に使用できません: " + toolItemId);
            return false;
        }
        
        // 地形を変更
        if (newType != null && terrainManager.changeTerrainType(tileX, tileY, newType)) {
            // 道具の耐久値を消費（1マスごとに1消費）
            // 道具の耐久値が0になったら削除
            int durability = toolData.getToolDurability();
            if (durability > 0) {
                // 耐久値を1減らす（簡易実装：実際には道具ごとに管理する必要がある）
                // ここでは道具を1個消費する形で実装
                inventory.removeItem(toolItemId, 1);
                Gdx.app.log("TerrainConversion", "地形を変換しました: " + currentType + " → " + newType);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 指定されたタイル位置で使用可能な道具を検索します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @return 使用可能な道具のID（見つからない場合は-1）
     */
    public int findUsableTool(int tileX, int tileY) {
        if (terrainManager == null || inventory == null || itemDataLoader == null) {
            return -1;
        }
        
        TerrainTile currentTile = terrainManager.getTerrainTile(tileX, tileY);
        if (currentTile == null) {
            return -1;
        }
        
        TerrainTile.TerrainType currentType = currentTile.getTerrainType();
        
        // 現在の地形に応じて使用可能な道具を検索
        if (currentType == TerrainTile.TerrainType.DIRT) {
            // 鍬を検索（高級なものから）
            for (int toolId = 53; toolId >= 51; toolId--) {
                if (inventory.getItemCount(toolId) > 0) {
                    return toolId;
                }
            }
            // 鍬がない場合は水路掘削用の排水シャベルを検索
            for (int toolId = 56; toolId >= 54; toolId--) {
                if (inventory.getItemCount(toolId) > 0) {
                    return toolId;
                }
            }
        } else if (currentType == TerrainTile.TerrainType.GRASS) {
            // 水路掘削用の排水シャベルを検索（高級なものから）
            for (int toolId = 56; toolId >= 54; toolId--) {
                if (inventory.getItemCount(toolId) > 0) {
                    return toolId;
                }
            }
        } else if (currentType == TerrainTile.TerrainType.DRAINED_MARSH) {
            // まず区画整理具を検索（高級なものから）
            for (int toolId = 59; toolId >= 57; toolId--) {
                if (inventory.getItemCount(toolId) > 0) {
                    return toolId;
                }
            }
            // 区画整理具がない場合は水路掘削用の排水シャベルを検索
            for (int toolId = 56; toolId >= 54; toolId--) {
                if (inventory.getItemCount(toolId) > 0) {
                    return toolId;
                }
            }
        } else if (currentType == TerrainTile.TerrainType.MARSH) {
            // 排水シャベルを検索（高級なものから）
            for (int toolId = 56; toolId >= 54; toolId--) {
                if (inventory.getItemCount(toolId) > 0) {
                    return toolId;
                }
            }
        }
        
        return -1;
    }
}
