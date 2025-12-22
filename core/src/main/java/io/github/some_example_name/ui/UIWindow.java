package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * UIウィンドウ（パネル）を表すクラス（描画処理を含む）。
 */
public class UIWindow {
    public float x, y, width, height;
    
    // ヘッダーエリア（オプション）
    private boolean hasHeader = false;
    private float headerY;
    private float headerHeight;
    
    // タイトル（オプション）
    private String title;
    private float titleFontSize = 1.0f;
    
    // 描画用のリソース（外部から設定）
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    
    // カスタマイズ可能な色
    private Color backgroundColor = new Color(0.1f, 0.1f, 0.15f, 0.95f);
    private Color headerBackgroundColor = new Color(0.15f, 0.15f, 0.2f, 0.95f);
    private Color borderColor = new Color(0.6f, 0.6f, 0.8f, 1f);
    private Color titleColor = Color.WHITE;
    
    /**
     * UIWindowを作成します。
     * @param x X座標
     * @param y Y座標
     * @param width 幅
     * @param height 高さ
     */
    public UIWindow(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * 描画用のリソースを設定します。
     */
    public void setRenderResources(ShapeRenderer shapeRenderer, SpriteBatch batch, 
                                   BitmapFont font, OrthographicCamera uiCamera) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.uiCamera = uiCamera;
    }
    
    /**
     * ヘッダーエリアを設定します。
     * @param headerY ヘッダーのY座標
     * @param headerHeight ヘッダーの高さ
     */
    public void setHeader(float headerY, float headerHeight) {
        this.hasHeader = true;
        this.headerY = headerY;
        this.headerHeight = headerHeight;
    }
    
    /**
     * ヘッダーを無効にします。
     */
    public void removeHeader() {
        this.hasHeader = false;
    }
    
    /**
     * タイトルを設定します。
     * @param title タイトルテキスト
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * タイトルのフォントサイズを設定します。
     * @param fontSize フォントサイズ
     */
    public void setTitleFontSize(float fontSize) {
        this.titleFontSize = fontSize;
    }
    
    /**
     * 色を設定します。
     */
    public void setColors(Color backgroundColor, Color headerBackgroundColor, 
                         Color borderColor, Color titleColor) {
        this.backgroundColor = backgroundColor;
        this.headerBackgroundColor = headerBackgroundColor;
        this.borderColor = borderColor;
        this.titleColor = titleColor;
    }
    
    /**
     * ウィンドウを描画します。
     * @param drawTitle タイトルを描画するかどうか
     */
    public void render(boolean drawTitle) {
        if (shapeRenderer == null || batch == null || font == null || uiCamera == null) {
            return; // リソースが設定されていない場合は描画しない
        }
        
        // batchが既に開始されているかチェック
        boolean batchWasActive = batch.isDrawing();
        
        // batchが開始されている場合は終了してからShapeRendererを使用
        if (batchWasActive) {
            batch.end();
        }
        
        // パネルの背景を描画
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(backgroundColor);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        // ヘッダーエリアの背景を描画（オプション）
        if (hasHeader) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(headerBackgroundColor);
            shapeRenderer.rect(x, headerY, width, headerHeight);
            shapeRenderer.end();
        }
        
        // パネルの枠線を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(borderColor);
        if (hasHeader) {
            // ヘッダーがある場合は、ヘッダーとボディの区切り線も描画
            shapeRenderer.line(x, y + height, x + width, y + height); // 上辺
            shapeRenderer.line(x, y + height, x, y); // 左辺
            shapeRenderer.line(x + width, y + height, x + width, y); // 右辺
            shapeRenderer.line(x, headerY, x + width, headerY); // ヘッダーとボディの区切り線
        } else {
            // ヘッダーがない場合は通常の矩形
            shapeRenderer.rect(x, y, width, height);
        }
        shapeRenderer.end();
        
        // batchを再開（元々開始されていた場合は再度開始、されていなかった場合は新規開始）
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // タイトルを描画（オプション）
        if (drawTitle && title != null && !title.isEmpty()) {
            font.getData().setScale(titleFontSize);
            font.setColor(titleColor);
            GlyphLayout titleLayout = new GlyphLayout(font, title);
            float titleX = x + (width - titleLayout.width) / 2;
            float titleY;
            if (hasHeader) {
                // ヘッダーがある場合はヘッダー中央に配置
                titleY = headerY + headerHeight / 2 + titleLayout.height / 2;
            } else {
                // ヘッダーがない場合は上部に配置
                titleY = y + height - 45;
            }
            font.draw(batch, title, titleX, titleY);
        }
        
        // batchが元々開始されていなかった場合は終了する
        // （呼び出し元で開始されていた場合は、呼び出し元で終了を管理）
        if (!batchWasActive) {
            batch.end();
        }
    }
    
    /**
     * ウィンドウを描画します（タイトルは描画しない）。
     */
    public void render() {
        render(false);
    }
    
    /**
     * 指定された座標がウィンドウ内にあるかどうかを判定します。
     * @param screenX 画面X座標
     * @param screenY 画面Y座標
     * @return ウィンドウ内にある場合true
     */
    public boolean contains(float screenX, float screenY) {
        return screenX >= x && screenX <= x + width && screenY >= y && screenY <= y + height;
    }
}
