package io.github.some_example_name.manager;

import io.github.some_example_name.entity.TerrainTile;
import io.github.some_example_name.game.Inventory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * 建物を管理するクラス。
 * 神殿などの恒久建築物を管理します。
 */
public class BuildingManager {
    // 建物タイプ
    public enum BuildingType {
        TEMPLE  // 神殿
    }
    
    // 建物タイルのマップ（キー: "tileX,tileY"）
    private Map<String, BuildingTile> buildings;
    
    // インベントリへの参照
    private Inventory inventory;
    
    // アイテムデータローダーへの参照
    private ItemDataLoader itemDataLoader;
    
    // 地形マネージャーへの参照
    private TerrainManager terrainManager;
    
    // 神殿の素材：鉄インゴット（ID: 37）5個、石（ID: 1）3個
    private static final int TEMPLE_IRON_INGOT_ID = 37;
    private static final int TEMPLE_IRON_INGOT_COUNT = 5;
    private static final int TEMPLE_STONE_ID = 1;
    private static final int TEMPLE_STONE_COUNT = 3;
    
    /**
     * 建物タイルを表すクラス。
     */
    public static class BuildingTile {
        private int tileX;
        private int tileY;
        private BuildingType buildingType;
        
        public BuildingTile(int tileX, int tileY, BuildingType buildingType) {
            this.tileX = tileX;
            this.tileY = tileY;
            this.buildingType = buildingType;
        }
        
        public int getTileX() {
            return tileX;
        }
        
        public int getTileY() {
            return tileY;
        }
        
        public BuildingType getBuildingType() {
            return buildingType;
        }
        
        /**
         * 建物を描画します。
         */
        public void render(ShapeRenderer shapeRenderer) {
            float pixelX = tileX * io.github.some_example_name.entity.Player.TILE_SIZE;
            float pixelY = tileY * io.github.some_example_name.entity.Player.TILE_SIZE;
            float size = io.github.some_example_name.entity.Player.TILE_SIZE;
            
            switch (buildingType) {
                case TEMPLE:
                    // 神殿：質素だが周囲より明らかに異質な建物
                    // 1.5倍のサイズで描画
                    float templeSize = size * 1.5f;
                    float templeX = pixelX - (templeSize - size) / 2;
                    float templeY = pixelY - (templeSize - size) / 2;
                    
                    // 基壇（1段高い）
                    shapeRenderer.setColor(0.5f, 0.45f, 0.4f, 1.0f); // 石色（少し明るい）
                    shapeRenderer.rect(templeX, templeY, templeSize, templeSize * 0.1f);
                    
                    // 本体（シンプルな長方形）
                    shapeRenderer.setColor(0.45f, 0.4f, 0.35f, 1.0f); // 石色（本体）
                    shapeRenderer.rect(templeX, templeY + templeSize * 0.1f, templeSize, templeSize * 0.9f);
                    
                    // 対称的な装飾線（直線）
                    shapeRenderer.setColor(0.4f, 0.35f, 0.3f, 1.0f);
                    float lineY = templeY + templeSize * 0.5f;
                    shapeRenderer.rect(templeX + templeSize * 0.1f, lineY, templeSize * 0.8f, size * 0.05f);
                    break;
            }
        }
    }
    
    public BuildingManager() {
        this.buildings = new HashMap<>();
        this.inventory = null;
        this.itemDataLoader = null;
        this.terrainManager = null;
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
     * 地形マネージャーを設定します。
     */
    public void setTerrainManager(TerrainManager terrainManager) {
        this.terrainManager = terrainManager;
    }
    
    /**
     * 指定されたタイル位置に神殿を建てます。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @return 建設に成功した場合true
     */
    public boolean buildTemple(int tileX, int tileY) {
        if (inventory == null || itemDataLoader == null || terrainManager == null) {
            return false;
        }
        
        // 既に建物があるかチェック
        String key = tileX + "," + tileY;
        if (buildings.containsKey(key)) {
            Gdx.app.log("Building", "この位置には既に建物があります");
            return false;
        }
        
        // 地形チェック：BARRENまたはSTONEにのみ建設可能
        TerrainTile tile = terrainManager.getTerrainTile(tileX, tileY);
        if (tile == null) {
            return false;
        }
        
        TerrainTile.TerrainType terrainType = tile.getTerrainType();
        if (terrainType != TerrainTile.TerrainType.BARREN && 
            terrainType != TerrainTile.TerrainType.STONE) {
            Gdx.app.log("Building", "神殿は荒地（BARREN）または岩（STONE）にのみ建設できます");
            return false;
        }
        
        // 素材チェック
        if (inventory.getItemCount(TEMPLE_IRON_INGOT_ID) < TEMPLE_IRON_INGOT_COUNT) {
            Gdx.app.log("Building", "神殿を建てるには鉄インゴットが" + TEMPLE_IRON_INGOT_COUNT + "個必要です");
            return false;
        }
        
        if (inventory.getItemCount(TEMPLE_STONE_ID) < TEMPLE_STONE_COUNT) {
            Gdx.app.log("Building", "神殿を建てるには石が" + TEMPLE_STONE_COUNT + "個必要です");
            return false;
        }
        
        // 素材を消費
        inventory.removeItem(TEMPLE_IRON_INGOT_ID, TEMPLE_IRON_INGOT_COUNT);
        inventory.removeItem(TEMPLE_STONE_ID, TEMPLE_STONE_COUNT);
        
        // 建物を配置
        BuildingTile buildingTile = new BuildingTile(tileX, tileY, BuildingType.TEMPLE);
        buildings.put(key, buildingTile);
        
        Gdx.app.log("Building", "神殿を建設しました！");
        return true;
    }
    
    /**
     * 指定されたタイル位置に建物があるかどうかを判定します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @return 建物がある場合true
     */
    public boolean hasBuilding(int tileX, int tileY) {
        String key = tileX + "," + tileY;
        return buildings.containsKey(key);
    }
    
    /**
     * 指定されたタイル位置の建物を取得します。
     * @param tileX タイルX座標
     * @param tileY タイルY座標
     * @return 建物タイル（存在しない場合はnull）
     */
    public BuildingTile getBuilding(int tileX, int tileY) {
        String key = tileX + "," + tileY;
        return buildings.get(key);
    }
    
    /**
     * 神殿の数を取得します（文明レベル進行条件用）。
     * @return 神殿の数
     */
    public int getTempleCount() {
        int count = 0;
        for (BuildingTile building : buildings.values()) {
            if (building.getBuildingType() == BuildingType.TEMPLE) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * すべての建物を描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     */
    public void render(ShapeRenderer shapeRenderer) {
        for (BuildingTile building : buildings.values()) {
            building.render(shapeRenderer);
        }
    }
    
    /**
     * すべての建物を返します（セーブ用）。
     */
    public Map<String, BuildingTile> getBuildings() {
        return buildings;
    }
    
    /**
     * 建物を設定します（ロード用）。
     */
    public void setBuildings(Map<String, BuildingTile> buildings) {
        this.buildings = buildings != null ? buildings : new HashMap<>();
    }
}
