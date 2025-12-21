package io.github.some_example_name.manager;

import io.github.some_example_name.entity.TerrainTile;
import io.github.some_example_name.entity.TileData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

/**
 * タイルデータをMarkdownファイルから読み込むクラス。
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
     * Markdownファイルからタイルデータを読み込みます。
     */
    private void loadTileData() {
        FileHandle file = Gdx.files.internal("tiles/tiles.md");
        if (!file.exists()) {
            Gdx.app.error("TileDataLoader", "tiles.md not found at: tiles/tiles.md");
            // デフォルトデータを設定
            createDefaultTileData();
            return;
        }
        
        try {
            String content = file.readString();
            // 改行コードを統一（\r\n -> \n）
            content = io.github.some_example_name.util.CSVParser.normalizeLineEndings(content);
            String[] lines = content.split("\n");
            
            TerrainTile.TerrainType currentType = null;
            TileData currentData = null;
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                
                // セクション開始（### で始まる行）
                if (line.startsWith("### ")) {
                    // 前のタイルデータを保存
                    if (currentType != null && currentData != null) {
                        tileDataMap.put(currentType, currentData);
                    }
                    
                    // 新しいタイルタイプを解析
                    String typeStr = line.substring(4).trim().split(" ")[0]; // "### GRASS (草)" -> "GRASS"
                    currentType = parseTerrainType(typeStr);
                    if (currentType != null) {
                        currentData = new TileData(currentType);
                    } else {
                        currentData = null;
                    }
                    continue;
                }
                
                // データ行の解析
                if (currentData != null && line.startsWith("- **")) {
                    parseDataLine(line, currentData);
                }
            }
            
            // 最後のタイルデータを保存
            if (currentType != null && currentData != null) {
                tileDataMap.put(currentType, currentData);
            }
            
        } catch (Exception e) {
            Gdx.app.error("TileDataLoader", "Error loading tiles.md: " + e.getMessage());
            e.printStackTrace();
            // エラー時はデフォルトデータを設定
            createDefaultTileData();
        }
        
        Gdx.app.log("TileDataLoader", "Loaded " + tileDataMap.size() + " tile types");
    }
    
    /**
     * 文字列からTerrainTypeを解析します。
     */
    private TerrainTile.TerrainType parseTerrainType(String typeStr) {
        try {
            return TerrainTile.TerrainType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            Gdx.app.log("TileDataLoader", "Unknown terrain type: " + typeStr);
            return null;
        }
    }
    
    /**
     * データ行を解析します。
     */
    private void parseDataLine(String line, TileData data) {
        // "- **キー**: 値" の形式を解析
        if (!line.contains("**") || !line.contains(":")) {
            return;
        }
        
        int keyStart = line.indexOf("**") + 2;
        int keyEnd = line.indexOf("**", keyStart);
        if (keyEnd == -1) {
            return;
        }
        
        String key = line.substring(keyStart, keyEnd).trim();
        int valueStart = line.indexOf(":", keyEnd) + 1;
        String value = line.substring(valueStart).trim();
        
        try {
            switch (key) {
                case "名前":
                    data.setName(value);
                    break;
                case "説明":
                    data.setDescription(value);
                    break;
                case "色":
                    parseColor(value, data::setColor);
                    break;
                case "装飾タイプ":
                    data.setDecorationType(parseDecorationType(value));
                    break;
                case "装飾色":
                    parseColor(value, data::setDecorationColor);
                    break;
                case "装飾数":
                    data.setDecorationCount(Integer.parseInt(value));
                    break;
                case "幹の色":
                    parseColor(value, data::setTrunkColor);
                    break;
                case "葉の色":
                    parseColor(value, data::setLeafColor);
                    break;
                case "水分量":
                    data.setMoisture(Float.parseFloat(value));
                    break;
                case "肥沃度":
                    data.setFertility(Float.parseFloat(value));
                    break;
                case "排水性":
                    data.setDrainage(Float.parseFloat(value));
                    break;
                case "耕作難度":
                    data.setTillageDifficulty(Float.parseFloat(value));
                    break;
            }
        } catch (Exception e) {
            Gdx.app.log("TileDataLoader", "Error parsing line: " + line + " - " + e.getMessage());
        }
    }
    
    /**
     * 色の文字列を解析します。
     * フォーマット: "0.3, 0.6, 0.2, 1.0 (RGBA)"
     */
    private void parseColor(String colorStr, ColorSetter setter) {
        try {
            // "(RGBA)" を除去
            colorStr = colorStr.replace("(RGBA)", "").trim();
            String[] parts = colorStr.split(",");
            if (parts.length >= 3) {
                float r = Float.parseFloat(parts[0].trim());
                float g = Float.parseFloat(parts[1].trim());
                float b = Float.parseFloat(parts[2].trim());
                float a = parts.length >= 4 ? Float.parseFloat(parts[3].trim()) : 1.0f;
                setter.set(r, g, b, a);
            }
        } catch (Exception e) {
            Gdx.app.log("TileDataLoader", "Error parsing color: " + colorStr);
        }
    }
    
    /**
     * 装飾タイプの文字列を解析します。
     */
    private TileData.DecorationType parseDecorationType(String typeStr) {
        switch (typeStr.toLowerCase()) {
            case "none":
                return TileData.DecorationType.NONE;
            case "grass_dots":
                return TileData.DecorationType.GRASS_DOTS;
            case "sand_dots":
                return TileData.DecorationType.SAND_DOTS;
            case "water_waves":
                return TileData.DecorationType.WATER_WAVES;
            case "stone_pattern":
                return TileData.DecorationType.STONE_PATTERN;
            case "tree":
                return TileData.DecorationType.TREE;
            case "furrows":
                return TileData.DecorationType.FURROWS;
            default:
                return TileData.DecorationType.NONE;
        }
    }
    
    /**
     * 色を設定するための関数型インターフェース。
     */
    @FunctionalInterface
    private interface ColorSetter {
        void set(float r, float g, float b, float a);
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
