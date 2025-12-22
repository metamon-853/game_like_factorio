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
                // ゲームガイドで使用される追加文字（土壌パラメータ、操作説明など）
                "湿具合栄養流易難平衡最近通荒厳環餌間隔消累位検索別進条干畜産回変適材要範囲入追鳴基加現高集作場活流メッセージ下貯使料育終後データ保存再開楽し" +
                // 土壌パラメータ関連
                "パラメータ湿潤乾燥低高悪良" +
                // 操作説明で使用される文字
                "押持状態減選択装着効率多倍器時代満場合植向上特水辺必須弱土地育起点発展相性強度低可痩面積あたり" +
                // CONTROLS.mdで使用される文字
                "操作方移矢印マウスクリック位置開確認クラフト必要素材要求条件満可能ポーズメニューゲームセーブロード設定変更カメラホイールズームインアウトドラッグ農業種植作物収穫農地農具装着向上家畜配置製品殺肉取得地形変換行特定道具使用応適切採掘岩タイル上石鉱石荒地回復または土アイテム神殿建設鉄インゴット個" +
                // FARMING.mdで使用される文字
                "基本持状態押植成長場所一定時間収穫時農具効率土壌条件応収穫量変農地装着多農具耐久値使用減インベントリ選択可能農地システム農地土壌湿り具合肥沃度栄養分排水性水流易難耕難度地形タイプ初期決草原バランス型適最適肥沃度高砂排水性高肥沃度低森肥沃度高耕作難度高作物特性要求条件水分量非常高中高特徴水田必要排水性低方良水辺必須役割安定供給型主食インフラ整備後強成長適切土壌条件成長速度収穫量向上麦小麦要求条件水分量低中肥沃度中排水性高特徴乾燥気候育過湿弱役割加工産業起点パン文明発展相性良成長適切土壌条件成長速度収穫量向上芋サツマイモ系要求条件水分量低中肥沃度低可排水性中高特徴痩土地育単位面積カロリー高役割序盤救世主文明度低強成長適切土壌条件成長速度収穫量向上農具種類効率簡易農具効率木石作原始農具耐久値序盤使用可能青銅農具効率青銅製農具耐久値収穫量倍青銅器時代使用可能鉄農具効率鉄製農具耐久値収穫量倍鉄器時代使用可能土壌条件影響土壌条件作物要求満場合種植事出来土壌条件良成長速度収穫量向上肥沃度特に収穫量大影響農具効率収穫量影響効率農具収穫量倍農具耐久値使用出来" +
                // LIVESTOCK.mdで使用される文字
                "家畜配置製品収穫作物餌持状態押配置成熟家畜一定時間毎製品卵ミルク羊毛生産生産間隔異家畜殺肉取得家畜場所押殺肉取得家畜殺家畜消滅肉重要食料源家畜種類家畜種類必要文明レベル異文明レベル上高度家畜配置出来家畜育製品生産文明レベル上条件満" +
                // 家畜の種類名（CSVから）
                "鶏豚羊山羊牛馬" +
                // 家畜の説明文で使用される文字
                "家禽雑食性美味毛衣類山岳地帯大型乗用運搬鳥提供飼育される" +
                // 追加の文字（説明文で使用）
                "として材料になる品質" +
                // BUILDINGS.mdで使用される文字
                "高級建造物神殿建設荒地岩タイル上押神殿建設神殿恒久建築物文明発展象徴神殿建設文明レベル古代文明時代到達神殿建設条件必要素材鉄インゴット個石個建設可能地形荒地岩神殿重要性神殿以上建設文明レベル古代文明時代到達文明レベル到達エンディング表示神殿採掘荒地有効活用方法建設戦略採掘荒地作成場所神殿建設効率的鉄インゴット鉄鉱石石炭炉精錬作成神殿複数建設可能文明レベル到達十分" +
                // GATHERING.mdで使用される文字
                "採集自動採集マップ散存在アイテム近自動採集プレイヤーアイテム範囲内入自動的インベントリ追加採集音鳴採集確認採集出来アイテムマップ様々アイテム散存在基本的材料石木材種作物加工品他アイテム採集戦略序盤積極的アイテム採集材料集採集アイテムインベントリ確認採集アイテム使クラフト建設出来文明レベル上高度アイテム出現可能性" +
                // MINING.mdで使用される文字
                "採掘採掘岩タイル上押採掘採掘石鉱石取得採掘後地形荒地変換採掘得アイテム採掘確率ベース以下アイテム取得石確率取得一般的銅鉱石確率取得鉄鉱石確率取得錫鉱石確率取得採掘注意点採掘出来岩タイル採掘後地形荒地変換荒地農業畜産適建築使用出来荒地回復土アイテム必要採掘戦略序盤石集基本的道具作成銅鉱石錫鉱石集青銅作成鉄鉱石集鉄インゴット作成高級道具建物作成採掘荒地神殿建物建場所活用" +
                // TERRAIN.mdで使用される文字
                "地形種類草緑色草原基本的地形農業適土茶色土壌農業最適地形砂砂色砂浜水近生成水青色水域低地生成通過出来岩灰色岩場高地生成森濃緑色森林高湿度地域生成田灌漑水田稲作特化地形高水分量必要米栽培最適畑耕作農地穀物栽培適麦芋栽培最適高肥沃度持湿地水多排水悪土地耕作困難適切排水工事行水田変換排水後湿地排水工事完了湿地作物植水田整備可能区画整理行水田変換水路水導人工水路農地灌漑使用水源取水口水引水田エリア水運使用荒地採掘荒廃土地農業畜産適建築使用出来水分量肥沃度排水性非常厳環境" +
                // TERRAIN.mdで不足している文字（特に重要）
                "廃厳" +
                // OTHER_FEATURES.mdで使用される文字
                "他機能インベントリ開アイテムクラフト必要素材要求条件道具施設満クラフト可能クラフトアイテムインベントリ追加アイテム図鑑アイテム図鑑アイテム詳細確認アイテム説明必要素材要求条件確認カテゴリ別アイテム検索セーブロードポーズメニューゲームセーブロードセーブデータ複数保存可能プレイヤー位置インベントリ文明レベル保存文明レベル文明レベル上新アイテム利用可能文明レベル旧石器時代始文明レベル古代文明時代進行可能文明レベル上高度道具施設使用可能文明レベル進行条件レベルレベルパン保存干し肉保存レベルレベル畜産物累計生産レベルレベル畜産物累計生産レベルレベル神殿以上建設地形変換特定道具施設使用地形変換草原畑変換湿地水田変換地形変換適切道具材料必要荒地回復荒地土タイル上押荒地回復土アイテム持必要回復後地形草原変換" +
                // ENDING.mdで使用される文字
                "エンディングエンディング条件文明レベル古代文明時代到達エンディング表示文明レベル到達神殿以上建設必要エンディング流神殿建設文明レベル到達エンディング画面自動的表示エンディングメッセージ文明根下ろ表示エンディング約秒間表示エンディング道のりステップ文明レベル新石器時代レベルレベルパン保存干し肉保存貯蔵容器使食料保存ステップ文明レベル青銅器時代畜産物累計生産家畜配置製品収穫ステップ文明レベル鉄器時代畜産物累計生産継続的家畜育製品生産ステップ文明レベル古代文明時代神殿以上建設鉄インゴット個石個必要神殿建設エンディング表示エンディング後ゲームエンディング終了ゲーム継続プレイセーブデータ保存何時再開エンディング後農業畜産採掘建設楽し" +
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
