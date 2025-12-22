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
            HEADING_4,     // #### 見出し4
            HEADING_5,     // ##### 見出し5
            HEADING_6,     // ###### 見出し6
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
            case BUILDINGS:
                return "guide/BUILDINGS.md";
            case ENDING:
                return "guide/ENDING.md";
            case GATHERING:
                return "guide/GATHERING.md";
            case MINING:
                return "guide/MINING.md";
            default:
                return null;
        }
    }
    
    /**
     * ガイドのサブメニュー項目（見出し1）のリストを取得します。
     * @param state ガイドの状態
     * @param livestockDataLoader 家畜データローダー（LIVESTOCKの場合のみ使用）
     * @return サブメニュー項目のリスト（見出し1のテキスト）
     */
    public static List<String> getSubMenuItems(HelpUI.GuideState state, LivestockDataLoader livestockDataLoader) {
        List<String> subMenuItems = new ArrayList<>();
        String fileName = getGuideFileName(state);
        if (fileName == null) {
            return subMenuItems;
        }
        
        FileHandle file = Gdx.files.internal(fileName);
        if (!file.exists()) {
            return subMenuItems;
        }
        
        try {
            String content = file.readString();
            content = io.github.some_example_name.util.CSVParser.normalizeLineEndings(content);
            
            // 家畜リストのプレースホルダーを置換
            if (state == HelpUI.GuideState.LIVESTOCK && livestockDataLoader != null) {
                content = replaceLivestockList(content, livestockDataLoader);
            }
            
            // 見出し1を抽出
            String[] lines = content.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("# ") && !line.startsWith("##")) {
                    subMenuItems.add(line.substring(2).trim());
                }
            }
        } catch (Exception e) {
            Gdx.app.error("GuideContentLoader", "Error loading submenu items: " + fileName + ", " + e.getMessage());
        }
        
        return subMenuItems;
    }
    
    /**
     * ガイドコンテンツを読み込みます。
     * @param state ガイドの状態
     * @param livestockDataLoader 家畜データローダー（LIVESTOCKの場合のみ使用）
     * @param sectionTitle 表示するセクションのタイトル（見出し1）。nullの場合はすべて表示
     * @return ガイド要素のリスト
     */
    public static List<GuideElement> loadGuideContent(HelpUI.GuideState state, LivestockDataLoader livestockDataLoader, String sectionTitle) {
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
            
            List<GuideElement> elements = parseMarkdown(content);
            
            // 特定のセクションのみを表示する場合
            if (sectionTitle != null && !sectionTitle.isEmpty()) {
                elements = filterBySection(elements, sectionTitle);
            }
            
            return elements;
        } catch (Exception e) {
            Gdx.app.error("GuideContentLoader", "Error loading guide file: " + fileName + ", " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * ガイドコンテンツを読み込みます（後方互換性のため）。
     * @param state ガイドの状態
     * @param livestockDataLoader 家畜データローダー（LIVESTOCKの場合のみ使用）
     * @return ガイド要素のリスト
     */
    public static List<GuideElement> loadGuideContent(HelpUI.GuideState state, LivestockDataLoader livestockDataLoader) {
        return loadGuideContent(state, livestockDataLoader, null);
    }
    
    /**
     * 指定されたセクション（見出し1）の要素のみをフィルタリングします。
     * @param elements 全要素
     * @param sectionTitle セクションタイトル（見出し1のテキスト）
     * @return フィルタリングされた要素のリスト
     */
    private static List<GuideElement> filterBySection(List<GuideElement> elements, String sectionTitle) {
        List<GuideElement> filtered = new ArrayList<>();
        boolean inTargetSection = false;
        
        for (GuideElement element : elements) {
            if (element.type == GuideElement.ElementType.HEADING_1) {
                // 見出し1が見つかったら、それが目的のセクションかチェック
                if (inTargetSection) {
                    // 既に目的のセクション内にいて、次の見出し1が見つかったら終了
                    break;
                }
                inTargetSection = element.text.equals(sectionTitle);
                if (inTargetSection) {
                    // 目的のセクションの見出し1も含める
                    filtered.add(element);
                }
            } else if (inTargetSection) {
                // 目的のセクション内の要素を追加
                filtered.add(element);
            }
            // セクション区切り（---）は通常の要素として扱う（inTargetSectionがtrueなら追加される）
        }
        
        return filtered;
    }
    
    /**
     * 家畜リストのプレースホルダーを実際のデータに置換します。
     */
    private static String replaceLivestockList(String content, LivestockDataLoader livestockDataLoader) {
        StringBuilder livestockList = new StringBuilder();
        Array<LivestockData> allLivestock = livestockDataLoader.getAllLivestock();
        
        for (LivestockData livestock : allLivestock) {
            StringBuilder itemText = new StringBuilder();
            itemText.append(livestock.name).append(": ");
            if (livestock.hasProduct()) {
                itemText.append("肉（ID:").append(livestock.meatItemId)
                       .append("）、製品（ID:").append(livestock.productItemId).append("）");
            } else {
                itemText.append("肉（ID:").append(livestock.meatItemId).append("）のみ");
            }
            if (livestock.description != null && !livestock.description.isEmpty()) {
                itemText.append(" - ").append(livestock.description);
            }
            livestockList.append("・").append(itemText.toString()).append("\n");
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
            
            // 見出し6 (######) - 最も長いものからチェック
            if (line.startsWith("###### ")) {
                elements.add(new GuideElement(GuideElement.ElementType.HEADING_6, line.substring(7).trim()));
                continue;
            }
            
            // 見出し5 (#####)
            if (line.startsWith("##### ")) {
                elements.add(new GuideElement(GuideElement.ElementType.HEADING_5, line.substring(6).trim()));
                continue;
            }
            
            // 見出し4 (####)
            if (line.startsWith("#### ")) {
                elements.add(new GuideElement(GuideElement.ElementType.HEADING_4, line.substring(5).trim()));
                continue;
            }
            
            // 見出し3 (###)
            if (line.startsWith("### ")) {
                elements.add(new GuideElement(GuideElement.ElementType.HEADING_3, line.substring(4).trim()));
                continue;
            }
            
            // 見出し2 (##)
            if (line.startsWith("## ")) {
                elements.add(new GuideElement(GuideElement.ElementType.HEADING_2, line.substring(3).trim()));
                continue;
            }
            
            // 見出し1 (#)
            if (line.startsWith("# ")) {
                elements.add(new GuideElement(GuideElement.ElementType.HEADING_1, line.substring(2).trim()));
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
