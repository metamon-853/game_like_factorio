package io.github.some_example_name.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import io.github.some_example_name.manager.LivestockDataLoader;
import io.github.some_example_name.entity.LivestockData;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

/**
 * ガイドコンテンツ（Markdownファイル）を読み込むクラス。
 */
public class GuideContentLoader {
    
    /**
     * ガイドコンテンツの要素を表すクラス。
     */
    public static class GuideElement {
        public enum ElementType {
            HEADING_1,      // # 見出し1
            HEADING_2,     // ## 見出し2
            HEADING_3,     // ### 見出し3
            TEXT,          // 通常のテキスト
            LIST_ITEM,     // ・リスト項目
            SEPARATOR      // --- 区切り線
        }
        
        public ElementType type;
        public String text;
        public int indentLevel; // インデントレベル（リスト項目用）
        
        public GuideElement(ElementType type, String text) {
            this.type = type;
            this.text = text;
            this.indentLevel = 0;
        }
        
        public GuideElement(ElementType type, String text, int indentLevel) {
            this.type = type;
            this.text = text;
            this.indentLevel = indentLevel;
        }
    }
    
    /**
     * ガイドの状態に対応するMarkdownファイル名を取得します。
     */
    private static String getGuideFileName(HelpUI.GuideState state) {
        switch (state) {
            case CONTROLS:
                return "guide/CONTROLS.md";
            case FARMING:
                return "guide/FARMING.md";
            case LIVESTOCK:
                return "guide/LIVESTOCK.md";
            case TERRAIN:
                return "guide/TERRAIN.md";
            case OTHER_FEATURES:
                return "guide/OTHER_FEATURES.md";
            default:
                return null;
        }
    }
    
    /**
     * ガイドコンテンツを読み込みます。
     * @param state ガイドの状態
     * @param livestockDataLoader 家畜データローダー（LIVESTOCKの場合のみ使用）
     * @return ガイド要素のリスト
     */
    public static List<GuideElement> loadGuideContent(HelpUI.GuideState state, LivestockDataLoader livestockDataLoader) {
        String fileName = getGuideFileName(state);
        if (fileName == null) {
            return new ArrayList<>();
        }
        
        FileHandle file = Gdx.files.internal(fileName);
        if (!file.exists()) {
            Gdx.app.error("GuideContentLoader", "Guide file not found: " + fileName);
            return new ArrayList<>();
        }
        
        try {
            String content = file.readString();
            // 改行コードを統一
            content = io.github.some_example_name.util.CSVParser.normalizeLineEndings(content);
            
            // 家畜リストのプレースホルダーを置換
            if (state == HelpUI.GuideState.LIVESTOCK && livestockDataLoader != null) {
                content = replaceLivestockList(content, livestockDataLoader);
            }
            
            return parseMarkdown(content);
        } catch (Exception e) {
            Gdx.app.error("GuideContentLoader", "Error loading guide file: " + fileName + ", " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 家畜リストのプレースホルダーを実際のデータに置換します。
     */
    private static String replaceLivestockList(String content, LivestockDataLoader livestockDataLoader) {
        StringBuilder livestockList = new StringBuilder();
        Array<LivestockData> allLivestock = livestockDataLoader.getAllLivestock();
        
        for (LivestockData livestock : allLivestock) {
            livestockList.append("・").append(livestock.name).append(": ");
            if (livestock.hasProduct()) {
                livestockList.append("肉（ID:").append(livestock.meatItemId)
                           .append("）、製品（ID:").append(livestock.productItemId).append("）");
            } else {
                livestockList.append("肉（ID:").append(livestock.meatItemId).append("）のみ");
            }
            livestockList.append("\n");
            if (livestock.description != null && !livestock.description.isEmpty()) {
                livestockList.append("  ").append(livestock.description).append("\n");
            }
        }
        
        return content.replace("{{LIVESTOCK_LIST}}", livestockList.toString().trim());
    }
    
    /**
     * Markdownテキストをパースしてガイド要素のリストに変換します。
     */
    private static List<GuideElement> parseMarkdown(String content) {
        List<GuideElement> elements = new ArrayList<>();
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.isEmpty()) {
                continue; // 空行はスキップ
            }
            
            // 区切り線
            if (line.startsWith("---")) {
                elements.add(new GuideElement(GuideElement.ElementType.SEPARATOR, ""));
                continue;
            }
            
            // 見出し1 (#)
            if (line.startsWith("# ")) {
                elements.add(new GuideElement(GuideElement.ElementType.HEADING_1, line.substring(2).trim()));
                continue;
            }
            
            // 見出し2 (##)
            if (line.startsWith("## ")) {
                elements.add(new GuideElement(GuideElement.ElementType.HEADING_2, line.substring(3).trim()));
                continue;
            }
            
            // 見出し3 (###)
            if (line.startsWith("### ")) {
                elements.add(new GuideElement(GuideElement.ElementType.HEADING_3, line.substring(4).trim()));
                continue;
            }
            
            // リスト項目 (・)
            if (line.startsWith("・")) {
                // インデントレベルを計算（スペースの数）
                int indentLevel = 0;
                String text = line.substring(1).trim();
                // 先頭のスペースをカウント（2スペース = 1レベル）
                int spaceCount = 0;
                for (int i = 0; i < line.length() && line.charAt(i) == ' '; i++) {
                    spaceCount++;
                }
                indentLevel = spaceCount / 2;
                elements.add(new GuideElement(GuideElement.ElementType.LIST_ITEM, text, indentLevel));
                continue;
            }
            
            // 通常のテキスト
            elements.add(new GuideElement(GuideElement.ElementType.TEXT, line));
        }
        
        return elements;
    }
}
