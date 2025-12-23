package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * タイトル画面を管理するクラス。
 * ゲーム開始時に表示されるタイトル画面を管理します。
 */
public class TitleScreen {
    private boolean isActive;
    private float animationTimer;
    private static final float ANIMATION_SPEED = 1.0f; // アニメーション速度
    
    // タイトルテキスト
    private static final String TITLE_TEXT = "Factorio風ゲーム";
    private static final String SUBTITLE_TEXT = "スペースキーまたはクリックで開始";
    
    public TitleScreen() {
        this.isActive = true; // デフォルトでアクティブ
        this.animationTimer = 0f;
    }
    
    /**
     * タイトル画面を開始します。
     */
    public void start() {
        this.isActive = true;
        this.animationTimer = 0f;
        Gdx.app.log("TitleScreen", "タイトル画面を開始しました");
    }
    
    /**
     * タイトル画面を更新します。
     * @param deltaTime 前フレームからの経過時間（秒）
     */
    public void update(float deltaTime) {
        if (!isActive) {
            return;
        }
        
        animationTimer += deltaTime * ANIMATION_SPEED;
    }
    
    /**
     * タイトル画面を描画します。
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
        
        // 背景を描画（グラデーション風）
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // 上部（暗い青）
        shapeRenderer.setColor(0.1f, 0.15f, 0.2f, 1f);
        shapeRenderer.rect(0, screenHeight / 2, screenWidth, screenHeight / 2);
        
        // 下部（より暗い青）
        shapeRenderer.setColor(0.05f, 0.1f, 0.15f, 1f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight / 2);
        
        shapeRenderer.end();
        
        // タイトルテキストを描画
        batch.begin();
        
        // フォント設定を保存
        float originalScale = font.getData().scaleX;
        Color originalColor = font.getColor().cpy();
        
        // タイトルテキスト（大きく、点滅アニメーション）
        font.getData().setScale(3.0f);
        float alpha = 0.7f + 0.3f * (float)Math.sin(animationTimer * 2.0f);
        font.setColor(1f, 1f, 1f, alpha);
        
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        titleLayout.setText(font, TITLE_TEXT);
        float titleX = (screenWidth - titleLayout.width) / 2;
        float titleY = screenHeight * 0.7f + titleLayout.height / 2;
        
        font.draw(batch, TITLE_TEXT, titleX, titleY);
        
        // サブタイトルテキスト（小さく、点滅アニメーション）
        font.getData().setScale(1.5f);
        float subtitleAlpha = 0.5f + 0.5f * (float)Math.sin(animationTimer * 3.0f);
        font.setColor(0.9f, 0.9f, 0.9f, subtitleAlpha);
        
        com.badlogic.gdx.graphics.g2d.GlyphLayout subtitleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        subtitleLayout.setText(font, SUBTITLE_TEXT);
        float subtitleX = (screenWidth - subtitleLayout.width) / 2;
        float subtitleY = screenHeight * 0.3f + subtitleLayout.height / 2;
        
        font.draw(batch, SUBTITLE_TEXT, subtitleX, subtitleY);
        
        batch.end();
        
        // フォント設定を復元
        font.getData().setScale(originalScale);
        font.setColor(originalColor);
    }
    
    /**
     * タイトル画面がアクティブかどうかを返します。
     * @return アクティブな場合true
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * タイトル画面を終了します。
     */
    public void end() {
        this.isActive = false;
        this.animationTimer = 0f;
    }
    
    /**
     * 入力処理を行います（スペースキーまたはクリックでゲーム開始）。
     * @return ゲーム開始の入力があった場合true
     */
    public boolean handleInput() {
        if (!isActive) {
            return false;
        }
        
        // スペースキーまたはマウスクリックでゲーム開始
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || 
            Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            end();
            return true;
        }
        
        return false;
    }
}

