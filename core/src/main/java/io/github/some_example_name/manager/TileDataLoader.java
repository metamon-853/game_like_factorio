package io.github.some_example_name.manager;

import io.github.some_example_name.entity.TerrainTile;
import io.github.some_example_name.entity.TileData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * タイルデータをJSONファイルから読み込むクラス。
 */
public class TileDataLoader {
    private static TileDataLoader instance;
    private Map<TerrainTile.TerrainType, TileData> tileDataMap;
    
    private TileDataLoader() {
        this.tileDataMap = new HashMap<>();
        loadTileData();
    }
    
    /**
     * シングルトンインスタンスを取得します。
     */
    public static TileDataLoader getInstance() {
        if (instance == null) {
            instance = new TileDataLoader();
        }
        return instance;
    }
    
    /**
     * インスタンスを初期化します（Mainクラスから呼び出し）。
     */
    public static void initialize() {
        if (instance == null) {
            instance = new TileDataLoader();
        }
    }
    
    /**
     * JSONファイルからタイルデータを読み込みます。
     */
    private void loadTileData() {
        FileHandle file = Gdx.files.internal("tiles/tiles.json");
        if (!file.exists()) {
            Gdx.app.error("TileDataLoader", "tiles.json not found at: tiles/tiles.json");
            // デフォルトデータを設定
            createDefaultTileData();
            return;
        }
        
        try {
            String jsonString = file.readString();
            JsonValue root = new JsonReader().parse(jsonString);
            
            // 各タイルタイプを処理
            for (JsonValue tileEntry = root.child(); tileEntry != null; tileEntry = tileEntry.next()) {
                String terrainTypeName = tileEntry.name();
                JsonValue tileData = tileEntry;
                
                try {
                    TerrainTile.TerrainType terrainType = TerrainTile.TerrainType.valueOf(terrainTypeName);
                    TileData data = new TileData(terrainType);
                    
                    // 基本情報
                    if (tileData.has("name")) {
                        data.setName(tileData.getString("name"));
                    }
                    if (tileData.has("description")) {
                        data.setDescription(tileData.getString("description"));
                    }
                    
                    // 色情報
                    if (tileData.has("color")) {
                        JsonValue colorArray = tileData.get("color");
                        if (colorArray.isArray() && colorArray.size >= 3) {
                            float r = colorArray.getFloat(0);
                            float g = colorArray.getFloat(1);
                            float b = colorArray.getFloat(2);
                            float a = colorArray.size >= 4 ? colorArray.getFloat(3) : 1.0f;
                            data.setColor(r, g, b, a);
                        }
                    }
                    
                    // 装飾情報
                    if (tileData.has("decorationType")) {
                        data.setDecorationType(parseDecorationType(tileData.getString("decorationType")));
                    }
                    if (tileData.has("decorationColor")) {
                        JsonValue colorArray = tileData.get("decorationColor");
                        if (colorArray.isArray() && colorArray.size >= 3) {
                            float r = colorArray.getFloat(0);
                            float g = colorArray.getFloat(1);
                            float b = colorArray.getFloat(2);
                            float a = colorArray.size >= 4 ? colorArray.getFloat(3) : 1.0f;
                            data.setDecorationColor(r, g, b, a);
                        }
                    }
                    if (tileData.has("decorationCount")) {
                        data.setDecorationCount(tileData.getInt("decorationCount"));
                    }
                    
                    // 木の装飾用の色
                    if (tileData.has("trunkColor")) {
                        JsonValue colorArray = tileData.get("trunkColor");
                        if (colorArray.isArray() && colorArray.size >= 3) {
                            float r = colorArray.getFloat(0);
                            float g = colorArray.getFloat(1);
                            float b = colorArray.getFloat(2);
                            float a = colorArray.size >= 4 ? colorArray.getFloat(3) : 1.0f;
                            data.setTrunkColor(r, g, b, a);
                        }
                    }
                    if (tileData.has("leafColor")) {
                        JsonValue colorArray = tileData.get("leafColor");
                        if (colorArray.isArray() && colorArray.size >= 3) {
                            float r = colorArray.getFloat(0);
                            float g = colorArray.getFloat(1);
                            float b = colorArray.getFloat(2);
                            float a = colorArray.size >= 4 ? colorArray.getFloat(3) : 1.0f;
                            data.setLeafColor(r, g, b, a);
                        }
                    }
                    
                    // 土壌パラメータ
                    if (tileData.has("moisture")) {
                        data.setMoisture(tileData.getFloat("moisture"));
                    }
                    if (tileData.has("fertility")) {
                        data.setFertility(tileData.getFloat("fertility"));
                    }
                    if (tileData.has("drainage")) {
                        data.setDrainage(tileData.getFloat("drainage"));
                    }
                    if (tileData.has("tillageDifficulty")) {
                        data.setTillageDifficulty(tileData.getFloat("tillageDifficulty"));
                    }
                    
                    tileDataMap.put(terrainType, data);
                } catch (IllegalArgumentException e) {
                    // 未知の地形タイプは無視
                } catch (Exception e) {
                    // パースエラーは無視
                }
            }
            
        } catch (Exception e) {
            Gdx.app.error("TileDataLoader", "Error loading tiles.json: " + e.getMessage());
            // エラー時はデフォルトデータを設定
            createDefaultTileData();
        }
    }
    
    /**
     * 装飾タイプの文字列を解析します。
     */
    private TileData.DecorationType parseDecorationType(String typeStr) {
        switch (typeStr.toUpperCase()) {
            case "NONE":
                return TileData.DecorationType.NONE;
            case "GRASS_DOTS":
                return TileData.DecorationType.GRASS_DOTS;
            case "SAND_DOTS":
                return TileData.DecorationType.SAND_DOTS;
            case "WATER_WAVES":
                return TileData.DecorationType.WATER_WAVES;
            case "STONE_PATTERN":
                return TileData.DecorationType.STONE_PATTERN;
            case "TREE":
                return TileData.DecorationType.TREE;
            case "FURROWS":
                return TileData.DecorationType.FURROWS;
            default:
                return TileData.DecorationType.NONE;
        }
    }
    
    /**
     * デフォルトのタイルデータを作成します（ファイル読み込み失敗時用）。
     */
    private void createDefaultTileData() {
        // GRASS
        TileData grass = new TileData(TerrainTile.TerrainType.GRASS);
        grass.setName("草");
        grass.setColor(0.3f, 0.6f, 0.2f, 1.0f);
        grass.setDecorationType(TileData.DecorationType.GRASS_DOTS);
        grass.setDecorationColor(0.2f, 0.5f, 0.1f, 0.5f);
        grass.setDecorationCount(3);
        grass.setMoisture(0.5f);
        grass.setFertility(0.6f);
        grass.setDrainage(0.6f);
        grass.setTillageDifficulty(0.3f);
        tileDataMap.put(TerrainTile.TerrainType.GRASS, grass);
        
        // DIRT
        TileData dirt = new TileData(TerrainTile.TerrainType.DIRT);
        dirt.setName("土");
        dirt.setColor(0.5f, 0.4f, 0.3f, 1.0f);
        dirt.setDecorationType(TileData.DecorationType.NONE);
        dirt.setMoisture(0.5f);
        dirt.setFertility(0.7f);
        dirt.setDrainage(0.5f);
        dirt.setTillageDifficulty(0.4f);
        tileDataMap.put(TerrainTile.TerrainType.DIRT, dirt);
        
        // SAND
        TileData sand = new TileData(TerrainTile.TerrainType.SAND);
        sand.setName("砂");
        sand.setColor(0.9f, 0.85f, 0.7f, 1.0f);
        sand.setDecorationType(TileData.DecorationType.SAND_DOTS);
        sand.setDecorationColor(0.8f, 0.75f, 0.6f, 0.6f);
        sand.setDecorationCount(4);
        sand.setMoisture(0.3f);
        sand.setFertility(0.3f);
        sand.setDrainage(0.9f);
        sand.setTillageDifficulty(0.2f);
        tileDataMap.put(TerrainTile.TerrainType.SAND, sand);
        
        // WATER
        TileData water = new TileData(TerrainTile.TerrainType.WATER);
        water.setName("水");
        water.setColor(0.2f, 0.4f, 0.7f, 1.0f);
        water.setDecorationType(TileData.DecorationType.WATER_WAVES);
        water.setDecorationColor(0.3f, 0.5f, 0.8f, 0.5f);
        water.setDecorationCount(2);
        water.setMoisture(1.0f);
        water.setFertility(0.2f);
        water.setDrainage(0.0f);
        water.setTillageDifficulty(1.0f);
        tileDataMap.put(TerrainTile.TerrainType.WATER, water);
        
        // STONE
        TileData stone = new TileData(TerrainTile.TerrainType.STONE);
        stone.setName("岩");
        stone.setColor(0.5f, 0.5f, 0.5f, 1.0f);
        stone.setDecorationType(TileData.DecorationType.STONE_PATTERN);
        stone.setDecorationColor(0.4f, 0.4f, 0.4f, 1.0f);
        stone.setMoisture(0.2f);
        stone.setFertility(0.1f);
        stone.setDrainage(0.8f);
        stone.setTillageDifficulty(1.0f);
        tileDataMap.put(TerrainTile.TerrainType.STONE, stone);
        
        // FOREST
        TileData forest = new TileData(TerrainTile.TerrainType.FOREST);
        forest.setName("森");
        forest.setColor(0.2f, 0.5f, 0.15f, 1.0f);
        forest.setDecorationType(TileData.DecorationType.TREE);
        forest.setTrunkColor(0.3f, 0.2f, 0.1f, 1.0f);
        forest.setLeafColor(0.1f, 0.4f, 0.1f, 1.0f);
        forest.setMoisture(0.6f);
        forest.setFertility(0.8f);
        forest.setDrainage(0.4f);
        forest.setTillageDifficulty(0.7f);
        tileDataMap.put(TerrainTile.TerrainType.FOREST, forest);
        
        // PADDY
        TileData paddy = new TileData(TerrainTile.TerrainType.PADDY);
        paddy.setName("田");
        paddy.setDescription("灌漑された水田。稲作に特化した地形。");
        paddy.setColor(0.25f, 0.45f, 0.35f, 1.0f);
        paddy.setDecorationType(TileData.DecorationType.WATER_WAVES);
        paddy.setDecorationColor(0.3f, 0.5f, 0.4f, 0.4f);
        paddy.setDecorationCount(1);
        paddy.setMoisture(0.95f);
        paddy.setFertility(0.7f);
        paddy.setDrainage(0.2f);
        paddy.setTillageDifficulty(0.8f);
        tileDataMap.put(TerrainTile.TerrainType.PADDY, paddy);
        
        // FARMLAND
        TileData farmland = new TileData(TerrainTile.TerrainType.FARMLAND);
        farmland.setName("畑");
        farmland.setDescription("耕作された農地。穀物栽培に適している。");
        farmland.setColor(0.45f, 0.35f, 0.25f, 1.0f);
        farmland.setDecorationType(TileData.DecorationType.FURROWS);
        farmland.setDecorationColor(0.4f, 0.3f, 0.2f, 0.6f);
        farmland.setDecorationCount(3);
        farmland.setMoisture(0.45f);
        farmland.setFertility(0.75f);
        farmland.setDrainage(0.6f);
        farmland.setTillageDifficulty(0.3f);
        tileDataMap.put(TerrainTile.TerrainType.FARMLAND, farmland);
        
        // MARSH
        TileData marsh = new TileData(TerrainTile.TerrainType.MARSH);
        marsh.setName("湿地");
        marsh.setDescription("水は多いが排水が悪い土地。耕作は困難ですが、適切な排水工事を行うことで水田に変換できます。");
        marsh.setColor(0.2f, 0.4f, 0.25f, 1.0f);
        marsh.setDecorationType(TileData.DecorationType.WATER_WAVES);
        marsh.setDecorationColor(0.25f, 0.45f, 0.3f, 0.5f);
        marsh.setDecorationCount(2);
        marsh.setMoisture(0.85f);
        marsh.setFertility(0.6f);
        marsh.setDrainage(0.1f);
        marsh.setTillageDifficulty(0.9f);
        tileDataMap.put(TerrainTile.TerrainType.MARSH, marsh);
        
        // DRAINED_MARSH
        TileData drainedMarsh = new TileData(TerrainTile.TerrainType.DRAINED_MARSH);
        drainedMarsh.setName("排水後湿地");
        drainedMarsh.setDescription("排水工事が完了した湿地。まだ作物は植えられませんが、水田整備が可能です。");
        drainedMarsh.setColor(0.25f, 0.42f, 0.28f, 1.0f);
        drainedMarsh.setDecorationType(TileData.DecorationType.WATER_WAVES);
        drainedMarsh.setDecorationColor(0.28f, 0.45f, 0.32f, 0.3f);
        drainedMarsh.setDecorationCount(1);
        drainedMarsh.setMoisture(0.6f);
        drainedMarsh.setFertility(0.65f);
        drainedMarsh.setDrainage(0.5f);
        drainedMarsh.setTillageDifficulty(0.7f);
        tileDataMap.put(TerrainTile.TerrainType.DRAINED_MARSH, drainedMarsh);
        
        // WATER_CHANNEL
        TileData waterChannel = new TileData(TerrainTile.TerrainType.WATER_CHANNEL);
        waterChannel.setName("水路");
        waterChannel.setDescription("水を導くための人工水路。農地への灌漑に使用されます。");
        waterChannel.setColor(0.25f, 0.45f, 0.65f, 1.0f);
        waterChannel.setDecorationType(TileData.DecorationType.WATER_WAVES);
        waterChannel.setDecorationColor(0.3f, 0.5f, 0.75f, 0.6f);
        waterChannel.setDecorationCount(2);
        waterChannel.setMoisture(1.0f);
        waterChannel.setFertility(0.3f);
        waterChannel.setDrainage(0.0f);
        waterChannel.setTillageDifficulty(1.0f);
        tileDataMap.put(TerrainTile.TerrainType.WATER_CHANNEL, waterChannel);
        
        // BARREN
        TileData barren = new TileData(TerrainTile.TerrainType.BARREN);
        barren.setName("荒地");
        barren.setDescription("採掘によって荒廃した土地。農業や畜産には適さないが、建築には使用できる。");
        barren.setColor(0.4f, 0.35f, 0.3f, 1.0f);
        barren.setDecorationType(TileData.DecorationType.NONE);
        barren.setMoisture(0.1f);
        barren.setFertility(0.0f);
        barren.setDrainage(0.9f);
        barren.setTillageDifficulty(1.0f);
        tileDataMap.put(TerrainTile.TerrainType.BARREN, barren);
    }
    
    /**
     * 地形タイプからタイルデータを取得します。
     */
    public TileData getTileData(TerrainTile.TerrainType terrainType) {
        return tileDataMap.get(terrainType);
    }
    
    /**
     * すべてのタイルデータのマップを取得します。
     */
    public Map<TerrainTile.TerrainType, TileData> getAllTileData() {
        return tileDataMap;
    }
}
