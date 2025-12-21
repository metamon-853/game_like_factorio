package io.github.some_example_name.entity;

import com.badlogic.gdx.graphics.Color;

/**
 * タイルのデータを保持するクラス。
 */
public class TileData {
    // 地形タイプ
    private TerrainTile.TerrainType terrainType;
    
    // 基本情報
    private String name;
    private String description;
    
    // 色情報
    private Color color;
    
    // 装飾情報
    private DecorationType decorationType;
    private Color decorationColor;
    private int decorationCount; // 装飾の数（点や波紋など）
    
    // 木の装飾用の色（treeタイプの場合）
    private Color trunkColor;
    private Color leafColor;
    
    // 土壌パラメータ
    private float moisture;
    private float fertility;
    private float drainage;
    private float tillageDifficulty;
    
    /**
     * 装飾タイプを表す列挙型。
     */
    public enum DecorationType {
        NONE,           // 装飾なし
        GRASS_DOTS,    // 草の小さな点
        SAND_DOTS,     // 砂の小さな点
        WATER_WAVES,   // 水の波紋
        STONE_PATTERN, // 岩の模様
        TREE,          // 木（幹と葉）
        FURROWS        // 畝（うね）
    }
    
    public TileData(TerrainTile.TerrainType terrainType) {
        this.terrainType = terrainType;
        this.decorationType = DecorationType.NONE;
        this.decorationCount = 0;
        this.color = Color.WHITE;
        this.decorationColor = Color.WHITE;
        this.trunkColor = Color.WHITE;
        this.leafColor = Color.WHITE;
        this.moisture = 0.5f;
        this.fertility = 0.5f;
        this.drainage = 0.5f;
        this.tillageDifficulty = 0.5f;
    }
    
    public TerrainTile.TerrainType getTerrainType() {
        return terrainType;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public void setColor(float r, float g, float b, float a) {
        this.color = new Color(r, g, b, a);
    }
    
    public DecorationType getDecorationType() {
        return decorationType;
    }
    
    public void setDecorationType(DecorationType decorationType) {
        this.decorationType = decorationType;
    }
    
    public Color getDecorationColor() {
        return decorationColor;
    }
    
    public void setDecorationColor(Color decorationColor) {
        this.decorationColor = decorationColor;
    }
    
    public void setDecorationColor(float r, float g, float b, float a) {
        this.decorationColor = new Color(r, g, b, a);
    }
    
    public int getDecorationCount() {
        return decorationCount;
    }
    
    public void setDecorationCount(int decorationCount) {
        this.decorationCount = decorationCount;
    }
    
    public Color getTrunkColor() {
        return trunkColor;
    }
    
    public void setTrunkColor(Color trunkColor) {
        this.trunkColor = trunkColor;
    }
    
    public void setTrunkColor(float r, float g, float b, float a) {
        this.trunkColor = new Color(r, g, b, a);
    }
    
    public Color getLeafColor() {
        return leafColor;
    }
    
    public void setLeafColor(Color leafColor) {
        this.leafColor = leafColor;
    }
    
    public void setLeafColor(float r, float g, float b, float a) {
        this.leafColor = new Color(r, g, b, a);
    }
    
    public float getMoisture() {
        return moisture;
    }
    
    public void setMoisture(float moisture) {
        this.moisture = clamp(moisture, 0.0f, 1.0f);
    }
    
    public float getFertility() {
        return fertility;
    }
    
    public void setFertility(float fertility) {
        this.fertility = clamp(fertility, 0.0f, 1.0f);
    }
    
    public float getDrainage() {
        return drainage;
    }
    
    public void setDrainage(float drainage) {
        this.drainage = clamp(drainage, 0.0f, 1.0f);
    }
    
    public float getTillageDifficulty() {
        return tillageDifficulty;
    }
    
    public void setTillageDifficulty(float tillageDifficulty) {
        this.tillageDifficulty = clamp(tillageDifficulty, 0.0f, 1.0f);
    }
    
    /**
     * 値を0.0～1.0の範囲にクランプします。
     */
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
