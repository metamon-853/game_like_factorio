package io.github.some_example_name.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/**
 * 日本語対応フォントを管理するクラス。
 */
public class FontManager {
    private BitmapFont japaneseFont;
    private BitmapFont defaultFont;
    
    /**
     * フォントを初期化します。
     */
    public void initialize() {
        // デフォルトフォント（フォールバック用）
        defaultFont = new BitmapFont();
        defaultFont.getData().setScale(2.0f);
        defaultFont.setColor(Color.WHITE);
        
        // 日本語フォントを生成
        japaneseFont = createJapaneseFont();
        
        if (japaneseFont == null) {
            Gdx.app.log("FontManager", "Japanese font creation failed, using default font");
            japaneseFont = defaultFont;
        }
    }
    
    /**
     * 日本語フォントを生成します。
     */
    private BitmapFont createJapaneseFont() {
        try {
            // システムフォントのパスを試行（Windows）
            String[] fontPaths = {
                "C:/Windows/Fonts/meiryo.ttc",      // メイリオ
                "C:/Windows/Fonts/msgothic.ttc",     // MSゴシック
                "C:/Windows/Fonts/msmincho.ttc",      // MS明朝
                "C:/Windows/Fonts/yu Gothic.ttc",    // 游ゴシック
                "C:/Windows/Fonts/NotoSansCJK-Regular.ttc", // NotoSansCJK（インストールされている場合）
            };
            
            for (String fontPath : fontPaths) {
                FileHandle fontFile = Gdx.files.absolute(fontPath);
                if (fontFile.exists()) {
                    Gdx.app.log("FontManager", "Loading font from: " + fontPath);
                    return generateFontFromFile(fontFile);
                }
            }
            
            // アセットフォルダ内のフォントを試行
            FileHandle assetFont = Gdx.files.internal("fonts/japanese.ttf");
            if (assetFont.exists()) {
                Gdx.app.log("FontManager", "Loading font from assets");
                return generateFontFromFile(assetFont);
            }
            
            // フォントが見つからない場合はデフォルトフォントを使用
            Gdx.app.log("FontManager", "No Japanese font found, using default");
            return null;
        } catch (Exception e) {
            Gdx.app.error("FontManager", "Error creating Japanese font: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * フォントファイルからBitmapFontを生成します。
     */
    private BitmapFont generateFontFromFile(FileHandle fontFile) {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            
            // フォントサイズを設定（元のデフォルトフォントと同じサイズ）
            parameter.size = 12;
            parameter.color = Color.WHITE;
            
            // 日本語文字を含む範囲を設定
            // 必要な文字のみを含める（パフォーマンス向上のため）
            StringBuilder chars = new StringBuilder();
            // ASCII文字（基本文字）
            for (int i = 32; i < 127; i++) {
                chars.append((char)i);
            }
            // ひらがな（U+3040-U+309F）
            for (int i = 0x3040; i <= 0x309F; i++) {
                chars.append((char)i);
            }
            // カタカナ（U+30A0-U+30FF）
            for (int i = 0x30A0; i <= 0x30FF; i++) {
                chars.append((char)i);
            }
            // よく使う漢字のみ（ゲームで使用する文字）
            // NOTE: ここに含まれない漢字は描画されず、文字が欠けて見えることがあります。
            String commonKanji = "原始文明農耕青銅器鉄器中世産業革命電気化学情報宇宙超石木材生肉穀物粘土器銅錫インゴット工具小麦粉火薬帆船部品石炭鋼材蒸気エンジンプラスチック硫酸電子部品半導体サーバーソフトウェアモジュール核融合燃料宇宙合金衛星恒星エネルギーセルナノマテリアル文明コア図鑑" +
                // アイテム詳細で使用される文字
                "地殻形成基本材料道具建築不可欠得天然使用細粒子構成土原料制御燃焼現象光生成作成精錬作物育植物農業基盤収穫農産物重要食料源自然見金属人類最初高温加熱構造物焼成物質炉焼作硬容器保存炭素豊富燃精製強塊加工適形状鉄製恒久備蓄貯蔵耐久性建物高度表安定生産合金純古代柔軟" +
                // UIで使用される文字
                "閉戻" +
                // 文明レベル名で使用される文字
                "旧新石器時代";
            chars.append(commonKanji);
            // 全角数字・記号
            chars.append("０１２３４５６７８９");
            chars.append("：（）");
            
            parameter.characters = chars.toString();
            
            BitmapFont font = generator.generateFont(parameter);
            generator.dispose();
            
            Gdx.app.log("FontManager", "Japanese font generated successfully");
            return font;
        } catch (Exception e) {
            Gdx.app.error("FontManager", "Error generating font from file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 日本語対応フォントを取得します。
     */
    public BitmapFont getJapaneseFont() {
        return japaneseFont != null ? japaneseFont : defaultFont;
    }
    
    /**
     * デフォルトフォントを取得します。
     */
    public BitmapFont getDefaultFont() {
        return defaultFont;
    }
    
    /**
     * リソースを解放します。
     */
    public void dispose() {
        if (japaneseFont != null && japaneseFont != defaultFont) {
            japaneseFont.dispose();
        }
        if (defaultFont != null) {
            defaultFont.dispose();
        }
    }
}
