package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.some_example_name.ui.UIButton;

import java.util.List;

/**
 * タイトル画面を管理するクラス。
 * ゲーム開始時に表示されるタイトル画面を管理します。
 */
public class TitleScreen {
    // コールバックインターフェース
    public interface TitleScreenCallbacks {
        void onNewGame();
        void onLoadGame(String saveName);
        void onQuit();
        List<String> getSaveFileList();
    }
    
    private boolean isActive;
    private float animationTimer;
    private static final float ANIMATION_SPEED = 1.0f; // アニメーション速度
    
    // タイトルテキスト
    private static final String TITLE_TEXT = "Factorio風ゲーム";
    
    // ボタン
    private UIButton newGameButton;
    private UIButton loadGameButton;
    private UIButton quitButton;
    
    // 描画用のリソース
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    private SoundManager soundManager;
    private int screenWidth;
    private int screenHeight;
    
    // コールバック
    private TitleScreenCallbacks callbacks;
    
    // ロードメニュー表示フラグ
    private boolean showLoadMenu;
    
    // 終了確認ダイアログ表示フラグ
    private boolean showQuitConfirm;
    
    // 確認ダイアログのホバー状態を記録
    private boolean lastYesHovered = false;
    private boolean lastNoHovered = false;
    
    public TitleScreen() {
        this.isActive = true; // デフォルトでアクティブ
        this.animationTimer = 0f;
        this.showLoadMenu = false;
        this.showQuitConfirm = false;
    }
    
    /**
     * タイトル画面を開始します。
     */
    public void start() {
        this.isActive = true;
        this.animationTimer = 0f;
        this.showLoadMenu = false;
        this.showQuitConfirm = false;
    }
    
    /**
     * 描画用のリソースを設定します。
     */
    public void setRenderResources(ShapeRenderer shapeRenderer, SpriteBatch batch, 
                                   BitmapFont font, OrthographicCamera uiCamera,
                                   SoundManager soundManager, int screenWidth, int screenHeight) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.uiCamera = uiCamera;
        this.soundManager = soundManager;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        // ボタンを作成
        float buttonWidth = 400;
        float buttonHeight = 80;
        float centerX = screenWidth / 2;
        float buttonSpacing = 100;
        
        float newGameButtonY = screenHeight * 0.5f;
        float loadGameButtonY = screenHeight * 0.5f - buttonSpacing;
        float quitButtonY = screenHeight * 0.5f - buttonSpacing * 2;
        
        newGameButton = new UIButton(centerX - buttonWidth / 2, newGameButtonY - buttonHeight / 2,
                                    buttonWidth, buttonHeight, "新規開始");
        loadGameButton = new UIButton(centerX - buttonWidth / 2, loadGameButtonY - buttonHeight / 2,
                                     buttonWidth, buttonHeight, "ゲームをロード");
        quitButton = new UIButton(centerX - buttonWidth / 2, quitButtonY - buttonHeight / 2,
                                 buttonWidth, buttonHeight, "ゲームを終了");
        
        // ボタンにリソースを設定
        newGameButton.setRenderResources(shapeRenderer, batch, font, uiCamera);
        newGameButton.setSoundManager(soundManager);
        loadGameButton.setRenderResources(shapeRenderer, batch, font, uiCamera);
        loadGameButton.setSoundManager(soundManager);
        quitButton.setRenderResources(shapeRenderer, batch, font, uiCamera);
        quitButton.setSoundManager(soundManager);
    }
    
    /**
     * コールバックを設定します。
     */
    public void setCallbacks(TitleScreenCallbacks callbacks) {
        this.callbacks = callbacks;
    }
    
    /**
     * 画面サイズを更新します。
     */
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        
        // ボタンの位置を再計算
        if (newGameButton != null && loadGameButton != null && quitButton != null) {
            float buttonWidth = 400;
            float buttonHeight = 80;
            float centerX = screenWidth / 2;
            float buttonSpacing = 100;
            
            float newGameButtonY = screenHeight * 0.5f;
            float loadGameButtonY = screenHeight * 0.5f - buttonSpacing;
            float quitButtonY = screenHeight * 0.5f - buttonSpacing * 2;
            
            newGameButton.x = centerX - buttonWidth / 2;
            newGameButton.y = newGameButtonY - buttonHeight / 2;
            
            loadGameButton.x = centerX - buttonWidth / 2;
            loadGameButton.y = loadGameButtonY - buttonHeight / 2;
            
            quitButton.x = centerX - buttonWidth / 2;
            quitButton.y = quitButtonY - buttonHeight / 2;
        }
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
        float titleY = screenHeight * 0.75f + titleLayout.height / 2;
        
        font.draw(batch, TITLE_TEXT, titleX, titleY);
        
        batch.end();
        
        // ロードメニューが表示されている場合はロードメニューを描画
        if (showLoadMenu) {
            renderLoadMenu();
        } else if (showQuitConfirm) {
            // 終了確認ダイアログを描画
            renderQuitConfirmDialog();
        } else {
            // ボタンを描画
            if (newGameButton != null && loadGameButton != null && quitButton != null) {
                float mouseX = Gdx.input.getX();
                float mouseY = screenHeight - Gdx.input.getY();
                
                // updateAndRenderメソッドを使用してホバー音を自動的に再生
                newGameButton.updateAndRender(mouseX, mouseY);
                loadGameButton.updateAndRender(mouseX, mouseY);
                quitButton.updateAndRender(mouseX, mouseY);
            }
        }
        
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
     * ロードメニューを描画します。
     */
    private void renderLoadMenu() {
        if (callbacks == null) {
            return;
        }
        
        List<String> saveList = callbacks.getSaveFileList();
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        float buttonWidth = 400;
        float buttonHeight = 60;
        float centerX = screenWidth / 2;
        float startY = screenHeight * 0.6f;
        float buttonSpacing = 70;
        
        // 背景を半透明で描画
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.7f);
        shapeRenderer.rect(centerX - buttonWidth / 2 - 20, startY - saveList.size() * buttonSpacing - 20,
                          buttonWidth + 40, saveList.size() * buttonSpacing + 100);
        shapeRenderer.end();
        
        // セーブファイルリストを描画
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(font, "セーブファイルを選択");
        float titleX = (screenWidth - layout.width) / 2;
        font.draw(batch, "セーブファイルを選択", titleX, startY + 40);
        batch.end();
        
        // セーブファイルボタンを描画
        for (int i = 0; i < saveList.size() && i < 8; i++) {
            float buttonY = startY - i * buttonSpacing;
            UIButton saveButton = new UIButton(centerX - buttonWidth / 2, buttonY - buttonHeight / 2,
                                              buttonWidth, buttonHeight, saveList.get(i));
            saveButton.setRenderResources(shapeRenderer, batch, font, uiCamera);
            saveButton.setSoundManager(soundManager);
            
            boolean hovered = saveButton.contains(mouseX, mouseY);
            saveButton.render(hovered);
        }
        
        // 戻るボタンを描画
        float backButtonY = startY - saveList.size() * buttonSpacing - 20;
        UIButton backButton = new UIButton(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2,
                                          buttonWidth, buttonHeight, "戻る");
        backButton.setRenderResources(shapeRenderer, batch, font, uiCamera);
        backButton.setSoundManager(soundManager);
        boolean backHovered = backButton.contains(mouseX, mouseY);
        backButton.render(backHovered);
    }
    
    /**
     * 終了確認ダイアログを描画します。
     */
    private void renderQuitConfirmDialog() {
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        // ダイアログ背景を描画
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
        float dialogWidth = 500;
        float dialogHeight = 250;
        float dialogX = (screenWidth - dialogWidth) / 2;
        float dialogY = (screenHeight - dialogHeight) / 2;
        shapeRenderer.rect(dialogX, dialogY, dialogWidth, dialogHeight);
        shapeRenderer.end();
        
        // ダイアログの枠線を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        shapeRenderer.rect(dialogX, dialogY, dialogWidth, dialogHeight);
        shapeRenderer.end();
        
        // テキストとボタンを描画
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        font.getData().setScale(0.625f);
        font.setColor(Color.WHITE);
        String messageText = "ゲームを終了しますか？";
        com.badlogic.gdx.graphics.g2d.GlyphLayout messageLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        messageLayout.setText(font, messageText);
        float messageX = (screenWidth - messageLayout.width) / 2;
        float messageY = screenHeight / 2 + 50;
        font.draw(batch, messageText, messageX, messageY);
        
        float buttonWidth = 200;
        float buttonHeight = 65;
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2 - 50;
        float buttonSpacing = 120;
        
        // はいボタン
        float yesButtonX = centerX - buttonSpacing - buttonWidth / 2;
        float yesButtonY = centerY - buttonHeight / 2;
        UIButton yesButton = new UIButton(yesButtonX, yesButtonY, buttonWidth, buttonHeight, "はい");
        yesButton.setRenderResources(shapeRenderer, batch, font, uiCamera);
        yesButton.setSoundManager(soundManager);
        boolean yesHovered = yesButton.contains(mouseX, mouseY);
        
        // いいえボタン
        float noButtonX = centerX + buttonSpacing - buttonWidth / 2;
        float noButtonY = centerY - buttonHeight / 2;
        UIButton noButton = new UIButton(noButtonX, noButtonY, buttonWidth, buttonHeight, "いいえ");
        noButton.setRenderResources(shapeRenderer, batch, font, uiCamera);
        noButton.setSoundManager(soundManager);
        boolean noHovered = noButton.contains(mouseX, mouseY);
        
        // ホバー状態が変わったときに音を再生
        if (yesHovered && !lastYesHovered && soundManager != null) {
            soundManager.playHoverSound();
        }
        if (noHovered && !lastNoHovered && soundManager != null) {
            soundManager.playHoverSound();
        }
        
        lastYesHovered = yesHovered;
        lastNoHovered = noHovered;
        
        yesButton.render(yesHovered);
        noButton.render(noHovered);
        
        batch.end();
    }
    
    /**
     * 入力処理を行います。
     * @return ゲーム開始の入力があった場合true
     */
    public boolean handleInput() {
        if (!isActive) {
            return false;
        }
        
        if (showQuitConfirm) {
            // 終了確認ダイアログのクリック処理
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                float mouseX = Gdx.input.getX();
                float mouseY = screenHeight - Gdx.input.getY();
                
                float buttonWidth = 200;
                float buttonHeight = 65;
                float centerX = screenWidth / 2;
                float centerY = screenHeight / 2 - 50;
                float buttonSpacing = 120;
                
                // はいボタン
                float yesButtonX = centerX - buttonSpacing - buttonWidth / 2;
                float yesButtonY = centerY - buttonHeight / 2;
                UIButton yesButton = new UIButton(yesButtonX, yesButtonY, buttonWidth, buttonHeight, "はい");
                if (yesButton.contains(mouseX, mouseY)) {
                    if (callbacks != null) {
                        callbacks.onQuit();
                    }
                    return false;
                }
                
                // いいえボタン
                float noButtonX = centerX + buttonSpacing - buttonWidth / 2;
                float noButtonY = centerY - buttonHeight / 2;
                UIButton noButton = new UIButton(noButtonX, noButtonY, buttonWidth, buttonHeight, "いいえ");
                if (noButton.contains(mouseX, mouseY)) {
                    showQuitConfirm = false;
                    return false;
                }
            }
            
            // ESCキーで確認ダイアログを閉じる
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                showQuitConfirm = false;
                return false;
            }
        } else if (showLoadMenu) {
            // ロードメニューのクリック処理
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                float mouseX = Gdx.input.getX();
                float mouseY = screenHeight - Gdx.input.getY();
                
                if (callbacks != null) {
                    List<String> saveList = callbacks.getSaveFileList();
                    float buttonWidth = 400;
                    float buttonHeight = 60;
                    float centerX = screenWidth / 2;
                    float startY = screenHeight * 0.6f;
                    float buttonSpacing = 70;
                    
                    // セーブファイルボタンのクリック処理
                    for (int i = 0; i < saveList.size() && i < 8; i++) {
                        float buttonY = startY - i * buttonSpacing;
                        UIButton saveButton = new UIButton(centerX - buttonWidth / 2, buttonY - buttonHeight / 2,
                                                          buttonWidth, buttonHeight, saveList.get(i));
                        if (saveButton.contains(mouseX, mouseY)) {
                            String saveName = saveList.get(i);
                            callbacks.onLoadGame(saveName);
                            showLoadMenu = false;
                            end();
                            return true;
                        }
                    }
                    
                    // 戻るボタンのクリック処理
                    float backButtonY = startY - saveList.size() * buttonSpacing - 20;
                    UIButton backButton = new UIButton(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2,
                                                      buttonWidth, buttonHeight, "戻る");
                    if (backButton.contains(mouseX, mouseY)) {
                        showLoadMenu = false;
                        return false;
                    }
                }
            }
            
            // ESCキーでロードメニューを閉じる
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                showLoadMenu = false;
                return false;
            }
        } else {
            // 通常のボタンクリック処理
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                float mouseX = Gdx.input.getX();
                float mouseY = screenHeight - Gdx.input.getY();
                
                if (newGameButton != null && newGameButton.contains(mouseX, mouseY)) {
                    if (callbacks != null) {
                        callbacks.onNewGame();
                    }
                    end();
                    return true;
                } else if (loadGameButton != null && loadGameButton.contains(mouseX, mouseY)) {
                    showLoadMenu = true;
                    return false;
                } else if (quitButton != null && quitButton.contains(mouseX, mouseY)) {
                    showQuitConfirm = true;
                    return false;
                }
            }
        }
        
        return false;
    }
}

