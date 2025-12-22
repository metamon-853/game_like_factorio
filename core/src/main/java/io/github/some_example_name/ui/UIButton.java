package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.some_example_name.system.SoundManager;

/**
 * UIボタンを表すクラス（描画、ホバー管理、ホバー音を含む）。
 */
public class UIButton {
    public float x, y, width, height;
    private String text;
    private boolean lastHoveredState = false;
    
    // 描画用のリソース（外部から設定）
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    private SoundManager soundManager;
    
    // カスタマイズ可能なオプション
    private float fontSize = 0.675f;
    private Color normalBgColor = new Color(0.15f, 0.15f, 0.25f, 0.95f);
    private Color hoverBgColor = new Color(0.25f, 0.25f, 0.35f, 0.95f);
    private Color normalBorderColor = new Color(0.6f, 0.6f, 0.8f, 1f);
    private Color hoverBorderColor = new Color(0.8f, 0.8f, 1.0f, 1f);
    private Color normalTextColor = Color.WHITE;
    private Color hoverTextColor = new Color(0.9f, 0.9f, 1.0f, 1f);
    
    /**
     * UIButtonを作成します。
     * @param x X座標
     * @param y Y座標
     * @param width 幅
     * @param height 高さ
     * @param text ボタンのテキスト
     */
    public UIButton(float x, float y, float width, float height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
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
     * サウンドマネージャーを設定します。
     */
    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }
    
    /**
     * フォントサイズを設定します。
     */
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }
    
    /**
     * 色を設定します。
     */
    public void setColors(Color normalBgColor, Color hoverBgColor, 
                         Color normalBorderColor, Color hoverBorderColor,
                         Color normalTextColor, Color hoverTextColor) {
        this.normalBgColor = normalBgColor;
        this.hoverBgColor = hoverBgColor;
        this.normalBorderColor = normalBorderColor;
        this.hoverBorderColor = hoverBorderColor;
        this.normalTextColor = normalTextColor;
        this.hoverTextColor = hoverTextColor;
    }
    
    /**
     * テキストを設定します。
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * 指定された座標がボタン内にあるかどうかを判定します。
     * @param screenX 画面X座標
     * @param screenY 画面Y座標
     * @return ボタン内にある場合true
     */
    public boolean contains(float screenX, float screenY) {
        return screenX >= x && screenX <= x + width && screenY >= y && screenY <= y + height;
    }
    
    /**
     * ボタンを更新して描画します。
     * マウス座標を渡すことで、ホバー状態を自動的に判定し、必要に応じてホバー音を再生します。
     * @param mouseX マウスX座標
     * @param mouseY マウスY座標（UI座標系）
     * @return ホバーされているかどうか
     */
    public boolean updateAndRender(float mouseX, float mouseY) {
        boolean isHovered = contains(mouseX, mouseY);
        
        // ホバー状態が変わったときに音を再生
        if (isHovered && !lastHoveredState && soundManager != null) {
            soundManager.playHoverSound();
        }
        lastHoveredState = isHovered;
        
        // ボタンを描画
        render(isHovered);
        
        return isHovered;
    }
    
    /**
     * ボタンを描画します。
     * @param isHovered ホバーされているかどうか
     */
    public void render(boolean isHovered) {
        if (shapeRenderer == null || batch == null || font == null || uiCamera == null) {
            return; // リソースが設定されていない場合は描画しない
        }
        
        // batchが既に開始されているかチェック
        boolean batchWasActive = batch.isDrawing();
        
        // batchが開始されている場合は終了してからShapeRendererを使用
        if (batchWasActive) {
            batch.end();
        }
        
        // 背景を描画
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(isHovered ? hoverBgColor : normalBgColor);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        // 枠線を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(isHovered ? hoverBorderColor : normalBorderColor);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        // テキストを描画
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.getData().setScale(fontSize);
        font.setColor(isHovered ? hoverTextColor : normalTextColor);
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = x + (width - layout.width) / 2;
        float textY = y + height / 2 + layout.height / 2;
        font.draw(batch, text, textX, textY);
        
        // batchが元々開始されていなかった場合は終了する
        if (!batchWasActive) {
            batch.end();
        }
    }
    
    /**
     * ホバー状態をリセットします。
     */
    public void resetHoverState() {
        lastHoveredState = false;
    }
}
