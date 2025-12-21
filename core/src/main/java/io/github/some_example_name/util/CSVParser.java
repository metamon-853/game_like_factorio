package io.github.some_example_name.util;

import java.util.ArrayList;
import java.util.List;

/**
 * CSVファイルのパース処理を行うユーティリティクラス。
 */
public class CSVParser {
    /**
     * 改行コードを統一します（\r\n -> \n, \r -> \n）。
     * @param content 元の文字列
     * @return 改行コードを統一した文字列
     */
    public static String normalizeLineEndings(String content) {
        return content.replace("\r\n", "\n").replace("\r", "\n");
    }
    
    /**
     * CSV行をパースします（引用符を考慮）。
     * @param line CSV行
     * @return カラムのリスト
     */
    public static List<String> parseCSVLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // 引用符の開始/終了
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // エスケープされた引用符（""）
                    current.append('"');
                    i++; // 次の文字をスキップ
                } else {
                    // 引用符の開始/終了を切り替え
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // 引用符の外側のカンマは区切り文字
                parts.add(current.toString());
                current = new StringBuilder();
            } else {
                // 通常の文字
                current.append(c);
            }
        }
        
        // 最後のカラムを追加
        parts.add(current.toString());
        
        return parts;
    }
}
