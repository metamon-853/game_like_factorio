package io.github.some_example_name.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;

/**
 * 日本語対応フォントを管理するクラス。
 */
public class FontManager implements Disposable {
    private BitmapFont japaneseFont;
    private BitmapFont defaultFont;
    
    /**
     * フォントを初期化します。
     */
    public void initialize() {
        // デフォルトフォント（フォールバック用）
        defaultFont = new BitmapFont();
        defaultFont.getData().setScale(0.5f);
        defaultFont.setColor(Color.WHITE);
        
        // 日本語フォントを生成
        japaneseFont = createJapaneseFont();
        
        if (japaneseFont == null) {
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
                    return generateFontFromFile(fontFile);
                }
            }
            
            // アセットフォルダ内のフォントを試行
            FileHandle assetFont = Gdx.files.internal("fonts/japanese.ttf");
            if (assetFont.exists()) {
                return generateFontFromFile(assetFont);
            }
            
            // フォントが見つからない場合はデフォルトフォントを使用
            return null;
        } catch (Exception e) {
            Gdx.app.error("FontManager", "Error creating Japanese font: " + e.getMessage());
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
            
            // フォントサイズを設定（大きめに生成して拡大時のにじみを防ぐ）
            parameter.size = 48;
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
                "旧新石器時代" +
                // HelpUIで使用される文字
                "操作方法農業家畜種類その他機能移動矢印キーインベントリポーズメニューカメラズーム種植作物収穫成長状態押持場所成熟製品卵ミルク羊毛生産取得殺詳細確認セーブロード利用可能上が新" +
                // 地形説明で使用される文字
                "地形種類草土砂水岩森緑色茶色砂色青色灰色濃低地近生成通過地域高湿度" +
                // 新地形関連（湿地、水路、田畑）
                "湿地排水後工事変換可能導人工灌漑田畑耕作穀物栽培適肥沃度持" +
                // 道具関連
                "鍬シャベル区画整理具排水用青銅鉄製高耐久大規模造成向効率化整備用安定広範囲取水口水源引き込起点施設周囲作業効率化制御計画的対応" +
                // ゲームガイドで使用される文字
                "高級戦略複数序盤救世主単位面積カロリー過湿乾燥気候加工産業発展相性インフラ整備供給型主食特徴役割成長速度影響満確率一般的注意点活用散確認出現可能性範囲内自動的積極的様々流根下約秒間道のりステップ継続的終了継続再開楽し" +
                // その他
                "悪土地困難適切行";
            chars.append(commonKanji);
            // 全角数字・記号
            chars.append("０１２３４５６７８９");
            chars.append("：（）【】・ー");
            
            parameter.characters = chars.toString();
            
            BitmapFont font = generator.generateFont(parameter);
            generator.dispose();
            
            return font;
        } catch (Exception e) {
            Gdx.app.error("FontManager", "Error generating font from file: " + e.getMessage());
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
