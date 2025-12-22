package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * UIテキスト描画のヘルパークラス（折り返し処理など）。
 */
public class UITextHelper {
    /**
     * テキストを描画します（長い場合は折り返し）。
     * @param batch SpriteBatch
     * @param font BitmapFont
     * @param text 描画するテキスト
     * @param x 開始X座標
     * @param y 開始Y座標
     * @param maxWidth 最大幅
     * @param lineSpacing 行間隔
     * @return 使用した行数（折り返し後の行数）
     */
    public static int drawWrappedText(SpriteBatch batch, BitmapFont font, String text, 
                                      float x, float y, float maxWidth, float lineSpacing) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        GlyphLayout layout = new GlyphLayout(font, text);
        
        if (layout.width <= maxWidth) {
            // 1行で収まる場合
            font.draw(batch, text, x, y);
            return 1;
        }
        
        // 長い場合は折り返し処理（簡易版）
        String[] words = text.split("");
        StringBuilder line = new StringBuilder();
        float currentX = x;
        float currentY = y;
        int lineCount = 0;
        
        for (String word : words) {
            String testLine = line.toString() + word;
            GlyphLayout testLayout = new GlyphLayout(font, testLine);
            if (testLayout.width > maxWidth && line.length() > 0) {
                font.draw(batch, line.toString(), currentX, currentY);
                currentY -= lineSpacing;
                lineCount++;
                line = new StringBuilder(word);
            } else {
                line.append(word);
            }
        }
        if (line.length() > 0) {
            font.draw(batch, line.toString(), currentX, currentY);
            lineCount++;
        }
        
        return lineCount;
    }
    
    /**
     * テキストを描画し、描画後のY座標を返します。
     * @param batch SpriteBatch
     * @param font BitmapFont
     * @param text 描画するテキスト
     * @param x 開始X座標
     * @param y 開始Y座標
     * @param maxWidth 最大幅
     * @param lineSpacing 行間隔
     * @return 描画後のY座標（次のテキストの開始位置）
     */
    public static float drawWrappedTextAndReturnY(SpriteBatch batch, BitmapFont font, String text,
                                                 float x, float y, float maxWidth, float lineSpacing) {
        int lineCount = drawWrappedText(batch, font, text, x, y, maxWidth, lineSpacing);
        return y - (lineCount * lineSpacing);
    }
    
    /**
     * 空の状態メッセージを中央に描画します。
     * @param batch SpriteBatch
     * @param font BitmapFont
     * @param text メッセージテキスト
     * @param panelX パネルのX座標
     * @param panelY パネルのY座標
     * @param panelWidth パネルの幅
     * @param panelHeight パネルの高さ
     * @param textColor テキストの色（nullの場合はグレー）
     */
    public static void drawEmptyStateMessage(SpriteBatch batch, BitmapFont font, String text,
                                            float panelX, float panelY, float panelWidth, float panelHeight,
                                            Color textColor) {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        Color originalColor = font.getColor().cpy();
        float originalScaleX = font.getData().scaleX;
        float originalScaleY = font.getData().scaleY;
        
        try {
            font.getData().setScale(0.825f);
            font.setColor(textColor != null ? textColor : new Color(0.7f, 0.7f, 0.7f, 1f));
            
            GlyphLayout layout = new GlyphLayout(font, text);
            float textX = panelX + (panelWidth - layout.width) / 2;
            float textY = panelY + panelHeight / 2;
            font.draw(batch, text, textX, textY);
        } finally {
            font.setColor(originalColor);
            font.getData().setScale(originalScaleX, originalScaleY);
        }
    }
}
