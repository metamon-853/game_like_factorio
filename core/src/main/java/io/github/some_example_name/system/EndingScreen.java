package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * エンディング画面を管理するクラス。
 * 古代文明到達時に表示されるエンディング演出を管理します。
 */
public class EndingScreen {
    private boolean isActive;
    private float timer;
    private static final float ENDING_DURATION = 12.0f; // 12秒間表示
    
    // エンディングテキスト
    private static final String ENDING_TEXT = "文明は、ここに根を下ろした。";
    
    public EndingScreen() {
        this.isActive = false;
        this.timer = 0f;
    }
    
    /**
     * エンディングを開始します。
     */
    public void start() {
        this.isActive = true;
        this.timer = 0f;
        Gdx.app.log("EndingScreen", "エンディングを開始しました");
    }
    
    /**
     * エンディングを更新します。
     * @param deltaTime 前フレームからの経過時間（秒）
     */
    public void update(float deltaTime) {
        if (!isActive) {
            return;
        }
        
        timer += deltaTime;
        
        // エンディング時間が経過したら終了
        if (timer >= ENDING_DURATION) {
            isActive = false;
            Gdx.app.log("EndingScreen", "エンディングが終了しました");
        }
    }
    
    /**
     * エンディング画面を描画します。
     * @param shapeRenderer ShapeRendererインスタンス
     * @param batch SpriteBatchインスタンス
     * @param font フォント
     * @param uiCamera UI用カメラ
     * @param screenWidth 画面幅
     * @param screenHeight 画面高さ
     */
    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                      OrthographicCamera uiCamera, int screenWidth, int screenHeight) {
        if (!isActive) {
            return;
        }
        
        // UIカメラのプロジェクション行列を設定
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);
        
        // 半透明の黒背景を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.7f); // 70%の透明度
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        
        // テキストを描画
        batch.begin();
        font.setColor(Color.WHITE);
        float originalScale = font.getData().scaleX;
        font.getData().setScale(1.5f);
        
        // テキストのサイズを計算（GlyphLayoutを使用）
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(font, ENDING_TEXT);
        
        // 画面中央に配置
        float x = (screenWidth - layout.width) / 2;
        float y = screenHeight / 2 + layout.height / 2;
        
        font.draw(batch, ENDING_TEXT, x, y);
        batch.end();
        
        // フォントスケールをリセット
        font.getData().setScale(originalScale);
    }
    
    /**
     * エンディングがアクティブかどうかを返します。
     * @return アクティブな場合true
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * エンディングを終了します。
     */
    public void end() {
        this.isActive = false;
        this.timer = 0f;
    }
}
