package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Array;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Player player;
    private ItemManager itemManager;
    
    // カメラとビューポート
    private OrthographicCamera camera;
    private Viewport viewport;
    
    // UI用のカメラ（画面座標系）
    private OrthographicCamera uiCamera;
    
    // 分離したクラス
    private UIRenderer uiRenderer;
    private SaveGameManager saveGameManager;
    private SoundSettings soundSettings;
    private TextInputHandler textInputHandler;
    
    // ポーズ状態
    private boolean isPaused;
    
    // メニュー状態
    private enum MenuState {
        MAIN_MENU,
        SOUND_MENU,
        SAVE_MENU,
        LOAD_MENU,
        QUIT_CONFIRM
    }
    private MenuState currentMenuState = MenuState.MAIN_MENU;
    
    // グリッド表示フラグ（デフォルトはオン）
    private boolean showGrid = true;
    
    // スライダー関連
    private boolean isDraggingSlider = false; // スライダーをドラッグ中かどうか
    
    // ゲームの論理的な画面サイズ（ピクセル単位）- 基準サイズ
    private static final float BASE_VIEWPORT_SIZE = 20 * Player.TILE_SIZE;
    
    // 画面サイズ
    private int screenWidth;
    private int screenHeight;
    
    // 現在のビューポートサイズ（正方形を保つため動的に調整）
    private float viewportWidth;
    private float viewportHeight;
    
    // ズーム関連
    private float cameraZoom = 1.0f; // 現在のズームレベル（1.0が基準）
    private static final float MIN_ZOOM = 0.3f; // 最小ズーム（縮小の限界）
    private static final float MAX_ZOOM = 3.0f; // 最大ズーム（拡大の限界）
    private static final float ZOOM_SPEED = 0.1f; // ズームの速度
    
    
    @Override
    public void create() {
        // 画面サイズを取得
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        
        // 画面のアスペクト比を計算
        float screenAspect = (float)screenWidth / (float)screenHeight;
        
        // 正方形の升を保つため、画面のアスペクト比に応じてビューポートサイズを調整
        if (screenAspect > 1.0f) {
            // 横長の画面：高さを基準にして幅を調整
            viewportHeight = BASE_VIEWPORT_SIZE;
            viewportWidth = BASE_VIEWPORT_SIZE * screenAspect;
        } else {
            // 縦長の画面：幅を基準にして高さを調整
            viewportWidth = BASE_VIEWPORT_SIZE;
            viewportHeight = BASE_VIEWPORT_SIZE / screenAspect;
        }
        
        // カメラとビューポートを初期化
        camera = new OrthographicCamera();
        camera.setToOrtho(false, viewportWidth, viewportHeight);
        viewport = new StretchViewport(viewportWidth, viewportHeight, camera);
        viewport.update(screenWidth, screenHeight);
        camera.update();
        
        // UI用のカメラを初期化（画面座標系）
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();
        
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.0f); // フォントサイズを大きく
        font.setColor(Color.WHITE);
        
        // 分離したクラスのインスタンスを作成
        uiRenderer = new UIRenderer(shapeRenderer, batch, font, uiCamera, screenWidth, screenHeight);
        saveGameManager = new SaveGameManager();
        soundSettings = new SoundSettings();
        textInputHandler = new TextInputHandler();
        
        // プレイヤーを原点に配置（無限マップなので任意の位置から開始可能）
        player = new Player(0, 0);
        // アイテムマネージャーを初期化（無限マップ対応）
        itemManager = new ItemManager();
        // ポーズ状態を初期化
        isPaused = false;
        
        // カメラをプレイヤーの初期位置に設定
        float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
        float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
        camera.position.set(playerCenterX, playerCenterY, 0);
        camera.zoom = cameraZoom;
        camera.update();
        
        // マウススクロール入力を処理するInputProcessorを設定
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                // スクロール量に応じてズームを変更
                // amountY > 0 は上スクロール（縮小）、amountY < 0 は下スクロール（拡大）
                float zoomChange = amountY * ZOOM_SPEED;
                cameraZoom += zoomChange;
                
                // ズームの範囲を制限
                cameraZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, cameraZoom));
                
                // カメラのズームを更新
                camera.zoom = cameraZoom;
                camera.update();
                
                return true; // イベントを処理したことを示す
            }
        });
    }

    @Override
    public void render() {
        // 画面サイズが変更された場合、ビューポートを更新
        if (screenWidth != Gdx.graphics.getWidth() || screenHeight != Gdx.graphics.getHeight()) {
            screenWidth = Gdx.graphics.getWidth();
            screenHeight = Gdx.graphics.getHeight();
            viewport.update(screenWidth, screenHeight);
            camera.update();
        }
        
        // ビューポートを適用
        viewport.apply();
        
        // 画面をクリア
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        
        // ESCキーでポーズ/再開を切り替え、またはメニューから戻る
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (isPaused) {
                if (textInputHandler.isTextInputActive()) {
                    // テキスト入力中の場合、入力をキャンセル
                    textInputHandler.setTextInputActive(false);
                } else if (currentMenuState == MenuState.SOUND_MENU || 
                          currentMenuState == MenuState.SAVE_MENU || 
                          currentMenuState == MenuState.LOAD_MENU ||
                          currentMenuState == MenuState.QUIT_CONFIRM) {
                    // サブメニューからメインメニューに戻る
                    currentMenuState = MenuState.MAIN_MENU;
                } else {
                    // ポーズ解除
                    isPaused = false;
                    currentMenuState = MenuState.MAIN_MENU;
                }
            } else {
                // ポーズ
                isPaused = true;
                currentMenuState = MenuState.MAIN_MENU;
            }
        }
        
        // テキスト入力処理
        if (textInputHandler.isTextInputActive()) {
            if (textInputHandler.handleInput()) {
                // Enterキーが押された場合の処理
                String text = textInputHandler.getInputText().trim();
                if (!text.isEmpty() && text.length() <= textInputHandler.getMaxInputLength()) {
                    if (textInputHandler.getCurrentInputLabel().equals("Save Name")) {
                        if (saveGameManager.saveGame(text, player, itemManager, showGrid, 
                                soundSettings.getMasterVolume(), soundSettings.isMuted(), cameraZoom)) {
                            Gdx.app.log("SaveGame", "Game saved successfully: " + text);
                            isPaused = false;
                        } else {
                            Gdx.app.error("SaveGame", "Failed to save game");
                        }
                    }
                }
                textInputHandler.setTextInputActive(false);
            }
        }
        
        // キーボード操作は無効化（マウス操作のみ）
        
        // ポーズ中にマウスクリックとドラッグを処理
        if (isPaused) {
            if (currentMenuState == MenuState.MAIN_MENU) {
                handlePauseMenuClick();
            } else if (currentMenuState == MenuState.SOUND_MENU) {
                handleSoundMenuClick();
                handleSoundMenuDrag();
            } else if (currentMenuState == MenuState.SAVE_MENU) {
                handleSaveMenuClick();
            } else if (currentMenuState == MenuState.LOAD_MENU) {
                handleLoadMenuClick();
            } else if (currentMenuState == MenuState.QUIT_CONFIRM) {
                handleQuitConfirmClick();
            }
        }
        
        // ポーズ中でない場合のみゲームを更新
        if (!isPaused) {
            // プレイヤーを更新
            float deltaTime = Gdx.graphics.getDeltaTime();
            player.update(deltaTime);
            
            // アイテムマネージャーを更新（カメラの視野範囲を渡す）
            itemManager.update(deltaTime, player, camera);
            
            // キーボード入力処理
            handleInput();
            
            // カメラをプレイヤーの位置に追従させる
            float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
            float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
            camera.position.set(playerCenterX, playerCenterY, 0);
        }
        
        // カメラのズームを適用（スクロールで変更された可能性があるため）
        camera.zoom = cameraZoom;
        
        // カメラを更新
        camera.update();
        
        // カメラのプロジェクション行列を設定
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        
        // グリッドを描画（Lineモード）- 表示フラグがオンの場合のみ
        if (showGrid) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            uiRenderer.drawGrid(camera);
            shapeRenderer.end();
        }
        
        // その他の描画（Filledモード）
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // アイテムを描画
        itemManager.render(shapeRenderer);
        
        // プレイヤーを描画
        player.render(shapeRenderer);
        
        shapeRenderer.end();
        
        // UI情報を描画（取得アイテム数など）
        uiRenderer.drawUI(itemManager);
        
        // ポーズメニューを描画（テキスト表示用）
        if (isPaused) {
            if (currentMenuState == MenuState.MAIN_MENU) {
                drawPauseMenu();
            } else if (currentMenuState == MenuState.SOUND_MENU) {
                drawSoundMenu();
            } else if (currentMenuState == MenuState.SAVE_MENU) {
                drawSaveMenu();
            } else if (currentMenuState == MenuState.LOAD_MENU) {
                drawLoadMenu();
            } else if (currentMenuState == MenuState.QUIT_CONFIRM) {
                drawQuitConfirmDialog();
            }
        }
    }
    
    /**
     * キーボード入力を処理します。
     */
    private void handleInput() {
        // 移動中は新しい入力を無視
        if (player.isMoving()) {
            return;
        }
        
        // 各方向のキーが押されているかチェック
        boolean up = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        
        // 斜め移動を優先的にチェック
        if (up && right) {
            // 右上
            player.move(1, 1);
        } else if (up && left) {
            // 左上
            player.move(-1, 1);
        } else if (down && right) {
            // 右下
            player.move(1, -1);
        } else if (down && left) {
            // 左下
            player.move(-1, -1);
        } else if (up) {
            // 上
            player.move(0, 1);
        } else if (down) {
            // 下
            player.move(0, -1);
        } else if (left) {
            // 左
            player.move(-1, 0);
        } else if (right) {
            // 右
            player.move(1, 0);
        }
    }
    
    
    /**
     * ポーズメニューのマウスクリックを処理します。
     */
    private void handlePauseMenuClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            // マウスの座標を取得（LibGDXの座標系はY軸が下から上なので変換が必要）
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            
            // ボタンの位置とサイズを計算（drawPauseMenuと同じ値を使用）
            float buttonWidth = 320;
            float buttonHeight = 65;
            float centerX = screenWidth / 2;
            float centerY = screenHeight / 2;
            float buttonSpacing = 80;
            
            // グリッド切り替えボタン
            float gridButtonY = centerY + buttonSpacing - 20;
            Button gridButton = new Button(centerX - buttonWidth / 2, gridButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            // セーブボタン
            float saveButtonY = centerY - 20;
            Button saveButton = new Button(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            // ロードボタン
            float loadButtonY = centerY - buttonSpacing - 20;
            Button loadButton = new Button(centerX - buttonWidth / 2, loadButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            // Soundメニューボタン
            float soundButtonY = centerY - buttonSpacing * 2 - 20;
            Button soundButton = new Button(centerX - buttonWidth / 2, soundButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            // ゲーム終了ボタン
            float quitButtonY = centerY - buttonSpacing * 3 - 20;
            Button quitButton = new Button(centerX - buttonWidth / 2, quitButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            // ボタンがクリックされたかを判定
            if (gridButton.contains(mouseX, mouseY)) {
                showGrid = !showGrid;
            } else if (saveButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.SAVE_MENU;
            } else if (loadButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.LOAD_MENU;
            } else if (soundButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.SOUND_MENU;
            } else if (quitButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.QUIT_CONFIRM;
            }
        }
    }
    
    /**
     * ポーズメニューを描画します。
     */
    private void drawPauseMenu() {
        // マウスの座標を取得（ホバー判定用）
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        // テキストを描画（画面座標系で）
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // ボタンの位置とサイズを計算
        float buttonWidth = 320;
        float buttonHeight = 65;
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2;
        float buttonSpacing = 80;
        
        // グリッド切り替えボタンを描画
        float gridButtonY = centerY + buttonSpacing - 20;
        Button gridButton = new Button(centerX - buttonWidth / 2, gridButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, gridButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Grid: " + (showGrid ? "ON" : "OFF"), gridButton.contains(mouseX, mouseY));
        
        // セーブボタンを描画
        float saveButtonY = centerY - 20;
        Button saveButton = new Button(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Save Game", saveButton.contains(mouseX, mouseY));
        
        // ロードボタンを描画
        float loadButtonY = centerY - buttonSpacing - 20;
        Button loadButton = new Button(centerX - buttonWidth / 2, loadButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, loadButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Load Game", loadButton.contains(mouseX, mouseY));
        
        // Soundメニューボタンを描画
        float soundButtonY = centerY - buttonSpacing * 2 - 20;
        Button soundButton = new Button(centerX - buttonWidth / 2, soundButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, soundButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Sound", soundButton.contains(mouseX, mouseY));
        
        // ゲーム終了ボタンを描画
        float quitButtonY = centerY - buttonSpacing * 3 - 20;
        Button quitButton = new Button(centerX - buttonWidth / 2, quitButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, quitButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Quit Game", quitButton.contains(mouseX, mouseY));
        
        font.getData().setScale(2.0f); // 元に戻す
        
        batch.end();
    }
    
    /**
     * サウンドメニューのマウスクリックを処理します。
     */
    private void handleSoundMenuClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            
            float buttonWidth = 320;
            float buttonHeight = 65;
            float centerX = screenWidth / 2;
            float centerY = screenHeight / 2;
            
            // 戻るボタン
            float backButtonY = centerY - 200;
            Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            // スライダーの位置を計算
            float sliderWidth = 400;
            float sliderHeight = 20;
            float sliderX = centerX - sliderWidth / 2;
            float sliderY = centerY - sliderHeight / 2;
            Button sliderArea = new Button(sliderX, sliderY, sliderWidth, sliderHeight);
            
            if (backButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.MAIN_MENU;
            } else if (sliderArea.contains(mouseX, mouseY)) {
                // スライダーをクリックした場合、その位置に音量を設定
                updateVolumeFromSliderPosition(mouseX, sliderX, sliderWidth);
                isDraggingSlider = true;
            }
        }
        
        // マウスボタンを離したらドラッグ終了
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            isDraggingSlider = false;
        }
    }
    
    /**
     * サウンドメニューのマウスドラッグを処理します。
     */
    private void handleSoundMenuDrag() {
        if (isDraggingSlider && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float centerX = screenWidth / 2;
            float sliderWidth = 400;
            float sliderX = centerX - sliderWidth / 2;
            
            updateVolumeFromSliderPosition(mouseX, sliderX, sliderWidth);
        }
    }
    
    /**
     * スライダーの位置から音量を更新します。
     */
    private void updateVolumeFromSliderPosition(float mouseX, float sliderX, float sliderWidth) {
        // マウス位置をスライダーの範囲内に制限
        float relativeX = Math.max(0, Math.min(sliderWidth, mouseX - sliderX));
        // 0.0f ～ 1.0f の範囲に変換
        float newVolume = relativeX / sliderWidth;
        
        // ミュート中の場合、ミュートを解除
        if (soundSettings.isMuted() && newVolume > 0) {
            soundSettings.setMuted(false);
        }
        
        soundSettings.setMasterVolume(newVolume);
    }
    
    /**
     * サウンドメニューを描画します。
     */
    private void drawSoundMenu() {
        // マウスの座標を取得（ホバー判定用）
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // "SOUND SETTINGS" テキストを中央に表示
        font.getData().setScale(3.0f);
        font.setColor(Color.WHITE);
        String titleText = "SOUND SETTINGS";
        GlyphLayout titleLayout = new GlyphLayout(font, titleText);
        float titleX = (screenWidth - titleLayout.width) / 2;
        float titleY = screenHeight / 2 + 150;
        font.draw(batch, titleText, titleX, titleY);
        
        // ボタンの位置とサイズを計算
        float buttonWidth = 320;
        float buttonHeight = 65;
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2;
        
        // スライダーを描画
        uiRenderer.drawVolumeSlider(centerX, centerY, soundSettings.getMasterVolume(), soundSettings.isMuted());
        
        // 音量表示を描画
        font.getData().setScale(2.0f);
        font.setColor(soundSettings.isMuted() ? Color.RED : Color.WHITE);
        String volumeText = "Volume: " + (int)(soundSettings.getMasterVolume() * 100) + "%" + (soundSettings.isMuted() ? " (MUTED)" : "");
        GlyphLayout volumeLayout = new GlyphLayout(font, volumeText);
        float volumeTextX = centerX - volumeLayout.width / 2;
        float volumeTextY = centerY + 50;
        font.draw(batch, volumeText, volumeTextX, volumeTextY);
        
        // 戻るボタンを描画
        float backButtonY = centerY - 200;
        Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Back", backButton.contains(mouseX, mouseY));
        
        font.getData().setScale(2.0f); // 元に戻す
        
        batch.end();
    }
    
    
    /**
     * セーブメニューのマウスクリックを処理します。
     */
    private void handleSaveMenuClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            
            float centerX = screenWidth / 2;
            float centerY = screenHeight / 2;
            
            // 入力フィールドの範囲
            float inputFieldWidth = 500;
            float inputFieldHeight = 60;
            float inputFieldX = centerX - inputFieldWidth / 2;
            float inputFieldY = centerY + 30;
            Button inputField = new Button(inputFieldX, inputFieldY, inputFieldWidth, inputFieldHeight);
            
            // ボタンのサイズ
            float buttonWidth = 280;
            float buttonHeight = 65;
            float buttonSpacing = 90;
            
            // 保存ボタン
            float saveButtonY = centerY - 100;
            Button saveButton = new Button(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            // 戻るボタン
            float backButtonY = centerY - buttonSpacing - 100;
            Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            if (inputField.contains(mouseX, mouseY)) {
                // 入力フィールドをクリックした場合、テキスト入力を開始
                textInputHandler.setTextInputActive(true);
                if (textInputHandler.getInputText().length() == 0) {
                    textInputHandler.setCurrentInputLabel("Save Name");
                }
            } else if (saveButton.contains(mouseX, mouseY) && textInputHandler.getInputText().length() > 0 && !textInputHandler.isTextInputActive()) {
                // 保存ボタンをクリックした場合（入力が完了している場合のみ）
                String text = textInputHandler.getInputText().trim();
                if (!text.isEmpty() && text.length() <= textInputHandler.getMaxInputLength()) {
                    if (saveGame(text)) {
                        Gdx.app.log("SaveGame", "Game saved successfully: " + text);
                        isPaused = false; // セーブ後はポーズを解除
                    } else {
                        Gdx.app.error("SaveGame", "Failed to save game");
                    }
                }
                textInputHandler.setTextInputActive(false);
            } else if (backButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.MAIN_MENU;
                textInputHandler.setTextInputActive(false);
            } else if (!inputField.contains(mouseX, mouseY)) {
                // 入力フィールド以外をクリックした場合、入力を終了
                if (textInputHandler.isTextInputActive()) {
                    textInputHandler.setTextInputActive(false);
                }
            }
        }
    }
    
    /**
     * ロードメニューのマウスクリックを処理します。
     */
    private void handleLoadMenuClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            
            float buttonWidth = 400;
            float buttonHeight = 50;
            float centerX = screenWidth / 2;
            float startY = screenHeight / 2 + 150;
            float buttonSpacing = 60;
            
            java.util.List<String> saveList = saveGameManager.getSaveFileList();
            
            // 各セーブファイルボタンをチェック
            for (int i = 0; i < saveList.size() && i < 10; i++) { // 最大10個まで表示
                float buttonY = startY - i * buttonSpacing;
                Button saveButton = new Button(centerX - buttonWidth / 2, buttonY - buttonHeight / 2, buttonWidth, buttonHeight);
                if (saveButton.contains(mouseX, mouseY)) {
                    String saveName = saveList.get(i);
                    if (loadGame(saveName)) {
                        Gdx.app.log("LoadGame", "Game loaded successfully: " + saveName);
                        isPaused = false; // ロード後はポーズを解除
                    } else {
                        Gdx.app.error("LoadGame", "Failed to load game: " + saveName);
                    }
                    break;
                }
            }
            
            // 戻るボタン
            float backButtonY = screenHeight / 2 - 200;
            Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            if (backButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.MAIN_MENU;
            }
        }
    }
    
    /**
     * セーブメニューを描画します。
     */
    private void drawSaveMenu() {
        // マウスの座標を取得（ホバー判定用）
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2;
        
        // ダイアログのサイズ
        float dialogWidth = 600;
        float dialogHeight = 450;
        float dialogX = (screenWidth - dialogWidth) / 2;
        float dialogY = (screenHeight - dialogHeight) / 2;
        
        // すべてのshapeRenderer描画を先に実行
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        // ダイアログの背景を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
        shapeRenderer.rect(dialogX, dialogY, dialogWidth, dialogHeight);
        shapeRenderer.end();
        
        // ダイアログの枠線を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        shapeRenderer.rect(dialogX, dialogY, dialogWidth, dialogHeight);
        shapeRenderer.end();
        
        // 入力フィールドの背景を描画
        float inputFieldWidth = 500;
        float inputFieldHeight = 60;
        float inputFieldX = centerX - inputFieldWidth / 2;
        float inputFieldY = centerY + 30;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // 入力フィールドの背景
        if (isTextInputActive) {
            shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
        } else {
            shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 1f);
        }
        shapeRenderer.rect(inputFieldX, inputFieldY, inputFieldWidth, inputFieldHeight);
        shapeRenderer.end();
        
        // 入力フィールドの枠線を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (isTextInputActive) {
            shapeRenderer.setColor(0.8f, 0.8f, 1.0f, 1f);
            // アクティブ時は太い線
            shapeRenderer.rect(inputFieldX - 2, inputFieldY - 2, inputFieldWidth + 4, inputFieldHeight + 4);
            shapeRenderer.rect(inputFieldX - 1, inputFieldY - 1, inputFieldWidth + 2, inputFieldHeight + 2);
        } else {
            shapeRenderer.setColor(0.5f, 0.5f, 0.7f, 1f);
        }
        shapeRenderer.rect(inputFieldX, inputFieldY, inputFieldWidth, inputFieldHeight);
        shapeRenderer.end();
        
        // テキスト描画
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // "SAVE GAME" タイトルを描画
        font.getData().setScale(3.5f);
        font.setColor(Color.WHITE);
        String titleText = "SAVE GAME";
        GlyphLayout titleLayout = new GlyphLayout(font, titleText);
        float titleX = centerX - titleLayout.width / 2;
        float titleY = dialogY + dialogHeight - 50;
        font.draw(batch, titleText, titleX, titleY);
        
        // ラベルを描画
        font.getData().setScale(2.2f);
        font.setColor(Color.WHITE);
        String inputLabel = "Save Name:";
        GlyphLayout labelLayout = new GlyphLayout(font, inputLabel);
        float labelX = centerX - labelLayout.width / 2;
        float labelY = inputFieldY + inputFieldHeight + 20;
        font.draw(batch, inputLabel, labelX, labelY);
        
        // 入力テキストを描画（LibGDXのフォントはベースライン基準なので、中央揃えを正しく計算）
        font.getData().setScale(2.0f);
        String inputText = textInputHandler.getInputText();
        String displayText = textInputHandler.isTextInputActive() ? inputText + "_" : 
                            (inputText.length() > 0 ? inputText : "Enter save name...");
        font.setColor(textInputHandler.isTextInputActive() ? Color.YELLOW : (inputText.length() > 0 ? Color.WHITE : Color.GRAY));
        GlyphLayout textLayout = new GlyphLayout(font, displayText);
        float textX = inputFieldX + 15; // 左側にパディング
        // ベースライン基準で中央揃え（Y座標はベースラインなので、height/2 + textLayout.height/2を加算）
        float textY = inputFieldY + inputFieldHeight / 2 + textLayout.height / 2;
        font.draw(batch, displayText, textX, textY);
        
        // 文字数制限を表示（入力フィールドの下、ラベルの上）
        font.getData().setScale(1.5f);
        font.setColor(Color.LIGHT_GRAY);
        String charCountText = inputText.length() + " / " + textInputHandler.getMaxInputLength();
        GlyphLayout charCountLayout = new GlyphLayout(font, charCountText);
        float charCountX = inputFieldX + inputFieldWidth - charCountLayout.width - 15;
        float charCountY = inputFieldY - 5; // 入力フィールドの少し上
        font.draw(batch, charCountText, charCountX, charCountY);
        
        // ボタンのサイズ
        float buttonWidth = 280;
        float buttonHeight = 65;
        float buttonSpacing = 90;
        
        // 保存ボタンを描画
        float saveButtonY = centerY - 100;
        Button saveButton = new Button(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        String saveButtonText = textInputHandler.isTextInputActive() ? "Press Enter to Save" : 
                               (inputText.length() > 0 ? "Save Game" : "Enter Name First");
        uiRenderer.drawButton(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   saveButtonText, saveButton.contains(mouseX, mouseY) && inputText.length() > 0);
        
        // 戻るボタンを描画
        float backButtonY = centerY - buttonSpacing - 100;
        Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Back", backButton.contains(mouseX, mouseY));
        
        // 既存のセーブファイルを表示（戻るボタンの下、ダイアログの下部に配置）
        java.util.List<String> saveList = getSaveFileList();
        if (!saveList.isEmpty() && saveList.size() <= 5) {
            font.getData().setScale(1.3f);
            font.setColor(Color.LIGHT_GRAY);
            String existingText = "Existing saves: " + saveList.size();
            GlyphLayout existingLayout = new GlyphLayout(font, existingText);
            float existingX = centerX - existingLayout.width / 2;
            // 戻るボタンの下に配置（ボタンのY座標 - ボタンの高さ/2 - マージン）
            float existingY = backButtonY - buttonHeight / 2 - 25;
            font.draw(batch, existingText, existingX, existingY);
        }
        
        font.getData().setScale(2.0f);
        batch.end();
    }
    
    /**
     * ロードメニューを描画します。
     */
    private void drawLoadMenu() {
        // マウスの座標を取得（ホバー判定用）
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // "LOAD GAME" テキストを中央に表示
        font.getData().setScale(3.0f);
        font.setColor(Color.WHITE);
        String titleText = "LOAD GAME";
        GlyphLayout titleLayout = new GlyphLayout(font, titleText);
        float titleX = (screenWidth - titleLayout.width) / 2;
        float titleY = screenHeight / 2 + 200;
        font.draw(batch, titleText, titleX, titleY);
        
        float buttonWidth = 400;
        float buttonHeight = 50;
        float centerX = screenWidth / 2;
        float startY = screenHeight / 2 + 150;
        float buttonSpacing = 60;
        
        java.util.List<String> saveList = getSaveFileList();
        
        // セーブファイルリストを描画
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        if (saveList.isEmpty()) {
            String noSaveText = "No save files found";
            GlyphLayout noSaveLayout = new GlyphLayout(font, noSaveText);
            float noSaveX = centerX - noSaveLayout.width / 2;
            float noSaveY = screenHeight / 2;
            font.draw(batch, noSaveText, noSaveX, noSaveY);
        } else {
            for (int i = 0; i < saveList.size() && i < 10; i++) { // 最大10個まで表示
                float buttonY = startY - i * buttonSpacing;
                String saveName = saveList.get(i);
                Button saveButton = new Button(centerX - buttonWidth / 2, buttonY - buttonHeight / 2, buttonWidth, buttonHeight);
                uiRenderer.drawButton(centerX - buttonWidth / 2, buttonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                          saveName, saveButton.contains(mouseX, mouseY));
            }
        }
        
        // 戻るボタンを描画
        float backButtonY = screenHeight / 2 - 200;
        Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Back", backButton.contains(mouseX, mouseY));
        
        font.getData().setScale(2.0f);
        batch.end();
    }
    
    /**
     * 終了確認ダイアログのマウスクリックを処理します。
     */
    private void handleQuitConfirmClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            
            float buttonWidth = 200;
            float buttonHeight = 65;
            float centerX = screenWidth / 2;
            float centerY = screenHeight / 2 - 50;
            float buttonSpacing = 120;
            
            // Yesボタン（左側）
            float yesButtonX = centerX - buttonSpacing - buttonWidth / 2;
            float yesButtonY = centerY - buttonHeight / 2;
            Button yesButton = new Button(yesButtonX, yesButtonY, buttonWidth, buttonHeight);
            
            // Noボタン（右側）
            float noButtonX = centerX + buttonSpacing - buttonWidth / 2;
            float noButtonY = centerY - buttonHeight / 2;
            Button noButton = new Button(noButtonX, noButtonY, buttonWidth, buttonHeight);
            
            if (yesButton.contains(mouseX, mouseY)) {
                Gdx.app.exit();
            } else if (noButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.MAIN_MENU;
            }
        }
    }
    
    /**
     * 終了確認ダイアログを描画します。
     */
    private void drawQuitConfirmDialog() {
        // マウスの座標を取得（ホバー判定用）
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        // ダイアログの背景を描画
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
        
        // メッセージテキストを描画
        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
        String messageText = "Quit Game?";
        GlyphLayout messageLayout = new GlyphLayout(font, messageText);
        float messageX = (screenWidth - messageLayout.width) / 2;
        float messageY = screenHeight / 2 + 50;
        font.draw(batch, messageText, messageX, messageY);
        
        // ボタンの位置とサイズを計算
        float buttonWidth = 200;
        float buttonHeight = 65;
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2 - 50;
        float buttonSpacing = 120;
        
        // Yesボタンを描画（左側）
        float yesButtonX = centerX - buttonSpacing - buttonWidth / 2;
        float yesButtonY = centerY - buttonHeight / 2;
        Button yesButton = new Button(yesButtonX, yesButtonY, buttonWidth, buttonHeight);
        uiRenderer.drawButton(yesButtonX, yesButtonY, buttonWidth, buttonHeight, 
                   "Yes", yesButton.contains(mouseX, mouseY));
        
        // Noボタンを描画（右側）
        float noButtonX = centerX + buttonSpacing - buttonWidth / 2;
        float noButtonY = centerY - buttonHeight / 2;
        Button noButton = new Button(noButtonX, noButtonY, buttonWidth, buttonHeight);
        uiRenderer.drawButton(noButtonX, noButtonY, buttonWidth, buttonHeight, 
                   "No", noButton.contains(mouseX, mouseY));
        
        font.getData().setScale(2.0f); // 元に戻す
        
        batch.end();
    }
    
    
    /**
     * ゲームの状態をセーブします。
     * @param saveName セーブデータ名
     * @return セーブが成功した場合true
     */
    public boolean saveGame(String saveName) {
        return saveGameManager.saveGame(saveName, player, itemManager, showGrid,
                soundSettings.getMasterVolume(), soundSettings.isMuted(), cameraZoom);
    }
    
    /**
     * ゲームの状態をロードします。
     * @param saveName セーブデータ名
     * @return ロードが成功した場合true
     */
    public boolean loadGame(String saveName) {
        SaveGameManager.LoadResult result = saveGameManager.loadGame(saveName, player, itemManager);
        if (result == null) {
            return false;
        }
        
        showGrid = result.showGrid;
        soundSettings.setMasterVolume(result.masterVolume);
        soundSettings.setMuted(result.isMuted);
        cameraZoom = result.cameraZoom;
        
        // カメラをプレイヤーの位置に設定
        float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
        float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
        camera.position.set(playerCenterX, playerCenterY, 0);
        camera.zoom = cameraZoom;
        camera.update();
        
        return true;
    }
    
    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        
        // ExtendViewportは自動的にアスペクト比を調整します
        viewport.update(width, height);
        camera.update();
        
        // UI用カメラも更新
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
        
        // UIRendererの画面サイズを更新
        if (uiRenderer != null) {
            uiRenderer.updateScreenSize(width, height);
        }
    }
    
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
