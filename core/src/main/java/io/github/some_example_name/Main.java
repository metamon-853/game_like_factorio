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
    
    // 音量設定（0.0f ～ 1.0f）
    private float masterVolume = 1.0f;
    
    // ミュート状態
    private boolean isMuted = false;
    
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
    
    // ゲーム名（Steam向けのセーブデータ保存先に使用）
    private static final String GAME_NAME = "game_like_factorio";
    
    // セーブファイル名のプレフィックス
    private static final String SAVE_FILE_PREFIX = "savegame_";
    private static final String SAVE_FILE_EXTENSION = ".json";
    
    // テキスト入力関連
    private StringBuilder inputText = new StringBuilder();
    private boolean isTextInputActive = false;
    private String currentInputLabel = "";
    private int maxInputLength = 30;
    
    /**
     * セーブデータの保存先ディレクトリを取得します。
     * Steamゲームとして標準的な場所（Documents/My Games/[ゲーム名]）を使用します。
     * @return セーブディレクトリのFileHandle
     */
    private com.badlogic.gdx.files.FileHandle getSaveDirectory() {
        try {
            // Windowsでは、システムプロパティからDocumentsフォルダのパスを取得
            String osName = System.getProperty("os.name").toLowerCase();
            String documentsPath;
            
            if (osName.contains("win")) {
                // Windowsの場合、環境変数からDocumentsフォルダを取得
                documentsPath = System.getenv("USERPROFILE");
                if (documentsPath == null) {
                    documentsPath = System.getProperty("user.home");
                }
                documentsPath += "\\Documents\\My Games\\" + GAME_NAME;
            } else if (osName.contains("mac")) {
                // macOSの場合
                documentsPath = System.getProperty("user.home") + "/Documents/My Games/" + GAME_NAME;
            } else {
                // Linuxの場合
                documentsPath = System.getProperty("user.home") + "/Documents/My Games/" + GAME_NAME;
            }
            
            // 絶対パスでFileHandleを作成
            com.badlogic.gdx.files.FileHandle saveDir = Gdx.files.absolute(documentsPath);
            
            // ディレクトリが存在しない場合は作成
            if (!saveDir.exists()) {
                saveDir.mkdirs();
                Gdx.app.log("SaveGame", "Created save directory at " + saveDir.file().getAbsolutePath());
            }
            
            return saveDir;
        } catch (Exception e) {
            Gdx.app.error("SaveGame", "Error getting save directory: " + e.getMessage());
            e.printStackTrace();
            // フォールバック：Gdx.files.external()を使用
            com.badlogic.gdx.files.FileHandle fallback = Gdx.files.external("Documents/My Games/" + GAME_NAME);
            fallback.mkdirs();
            Gdx.app.log("SaveGame", "Using fallback path: " + fallback.file().getAbsolutePath());
            return fallback;
        }
    }
    
    /**
     * セーブデータの保存先ファイルを取得します。
     * @param saveName セーブデータ名（nullの場合はデフォルト名）
     * @return セーブファイルのFileHandle
     */
    private com.badlogic.gdx.files.FileHandle getSaveFileHandle(String saveName) {
        com.badlogic.gdx.files.FileHandle saveDir = getSaveDirectory();
        String fileName;
        if (saveName == null || saveName.trim().isEmpty()) {
            fileName = SAVE_FILE_PREFIX + "default" + SAVE_FILE_EXTENSION;
        } else {
            // ファイル名に使用できない文字を置き換え
            String sanitizedName = saveName.replaceAll("[\\\\/:*?\"<>|]", "_");
            fileName = SAVE_FILE_PREFIX + sanitizedName + SAVE_FILE_EXTENSION;
        }
        return saveDir.child(fileName);
    }
    
    /**
     * 利用可能なセーブファイルのリストを取得します。
     * @return セーブファイル名のリスト（拡張子なし）
     */
    private java.util.List<String> getSaveFileList() {
        java.util.List<String> saveList = new java.util.ArrayList<>();
        try {
            com.badlogic.gdx.files.FileHandle saveDir = getSaveDirectory();
            if (saveDir.exists() && saveDir.isDirectory()) {
                com.badlogic.gdx.files.FileHandle[] files = saveDir.list();
                for (com.badlogic.gdx.files.FileHandle file : files) {
                    String fileName = file.name();
                    if (fileName.startsWith(SAVE_FILE_PREFIX) && fileName.endsWith(SAVE_FILE_EXTENSION)) {
                        // プレフィックスと拡張子を除去
                        String saveName = fileName.substring(
                            SAVE_FILE_PREFIX.length(),
                            fileName.length() - SAVE_FILE_EXTENSION.length()
                        );
                        saveList.add(saveName);
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("SaveGame", "Error getting save file list: " + e.getMessage());
            e.printStackTrace();
        }
        // 名前順にソート
        saveList.sort(String::compareToIgnoreCase);
        return saveList;
    }
    
    // ボタン関連
    private static class Button {
        float x, y, width, height;
        
        Button(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        boolean contains(float screenX, float screenY) {
            return screenX >= x && screenX <= x + width && screenY >= y && screenY <= y + height;
        }
    }
    
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
        
        // プレイヤーを原点に配置（無限マップなので任意の位置から開始可能）
        player = new Player(0, 0);
        // アイテムマネージャーを初期化（無限マップ対応）
        itemManager = new ItemManager();
        // ポーズ状態を初期化
        isPaused = false;
        
        // 音量を初期化（マスターボリュームを設定）
        updateMasterVolume();
        
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
                if (isTextInputActive) {
                    // テキスト入力中の場合、入力をキャンセル
                    isTextInputActive = false;
                    inputText.setLength(0);
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
        if (isTextInputActive) {
            handleTextInput();
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
            drawGrid();
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
        drawUI();
        
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
     * グリッドを描画します（カメラの視野範囲に基づいて動的に生成）。
     * マップ升（太い線）とプレイヤー升（細い線）の両方を描画します。
     */
    private void drawGrid() {
        int mapTileSize = Player.MAP_TILE_SIZE;
        int playerTileSize = Player.PLAYER_TILE_SIZE;
        
        // カメラの視野範囲を計算（ズームを考慮）
        float actualViewportWidth = camera.viewportWidth * camera.zoom;
        float actualViewportHeight = camera.viewportHeight * camera.zoom;
        float cameraLeft = camera.position.x - actualViewportWidth / 2;
        float cameraRight = camera.position.x + actualViewportWidth / 2;
        float cameraBottom = camera.position.y - actualViewportHeight / 2;
        float cameraTop = camera.position.y + actualViewportHeight / 2;
        
        // マージンを追加して少し広めに描画（見切れを防ぐ）
        float margin = mapTileSize * 2;
        
        // まず、プレイヤー升の細かいグリッドを描画（薄い色）
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f); // 薄いグレー
        
        int startPlayerTileX = (int)Math.floor((cameraLeft - margin) / playerTileSize);
        int endPlayerTileX = (int)Math.ceil((cameraRight + margin) / playerTileSize);
        int startPlayerTileY = (int)Math.floor((cameraBottom - margin) / playerTileSize);
        int endPlayerTileY = (int)Math.ceil((cameraTop + margin) / playerTileSize);
        
        // プレイヤー升の縦線を描画
        for (int x = startPlayerTileX; x <= endPlayerTileX; x++) {
            float lineX = x * playerTileSize;
            shapeRenderer.line(lineX, startPlayerTileY * playerTileSize, lineX, endPlayerTileY * playerTileSize);
        }
        
        // プレイヤー升の横線を描画
        for (int y = startPlayerTileY; y <= endPlayerTileY; y++) {
            float lineY = y * playerTileSize;
            shapeRenderer.line(startPlayerTileX * playerTileSize, lineY, endPlayerTileX * playerTileSize, lineY);
        }
        
        // 次に、マップ升の太いグリッドを描画（濃い色）
        shapeRenderer.setColor(Color.DARK_GRAY);
        
        int startMapTileX = (int)Math.floor((cameraLeft - margin) / mapTileSize);
        int endMapTileX = (int)Math.ceil((cameraRight + margin) / mapTileSize);
        int startMapTileY = (int)Math.floor((cameraBottom - margin) / mapTileSize);
        int endMapTileY = (int)Math.ceil((cameraTop + margin) / mapTileSize);
        
        // マップ升の縦線を描画
        for (int x = startMapTileX; x <= endMapTileX; x++) {
            float lineX = x * mapTileSize;
            shapeRenderer.line(lineX, startMapTileY * mapTileSize, lineX, endMapTileY * mapTileSize);
        }
        
        // マップ升の横線を描画
        for (int y = startMapTileY; y <= endMapTileY; y++) {
            float lineY = y * mapTileSize;
            shapeRenderer.line(startMapTileX * mapTileSize, lineY, endMapTileX * mapTileSize, lineY);
        }
    }
    
    /**
     * UI情報（取得アイテム数など）を描画します。
     */
    private void drawUI() {
        // UIは画面座標系で描画（ちらつきを防ぐため）
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // フォントサイズをUI用に調整
        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
        
        // 画面左上の位置を計算（画面座標系）
        float padding = 20;
        float leftX = padding;
        float topY = screenHeight - padding;
        
        // FPSを表示（左上に配置）
        int fps = Gdx.graphics.getFramesPerSecond();
        String fpsText = "FPS: " + fps;
        font.draw(batch, fpsText, leftX, topY);
        
        // 画面右上の位置を計算（画面座標系）
        float rightX = screenWidth - padding;
        
        // 取得したアイテム数を表示（右上に配置）
        String itemText = "Items: " + itemManager.getCollectedCount();
        GlyphLayout itemLayout = new GlyphLayout(font, itemText);
        float itemX = rightX - itemLayout.width;
        font.draw(batch, itemText, itemX, topY);
        
        // 現在のアイテム数も表示（その下に配置）
        String currentItemText = "On Map: " + itemManager.getItemCount();
        GlyphLayout currentLayout = new GlyphLayout(font, currentItemText);
        float currentX = rightX - currentLayout.width;
        font.draw(batch, currentItemText, currentX, topY - itemLayout.height - 10);
        
        // フォントサイズを元に戻す
        font.getData().setScale(2.0f);
        
        batch.end();
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
        drawButton(centerX - buttonWidth / 2, gridButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Grid: " + (showGrid ? "ON" : "OFF"), gridButton.contains(mouseX, mouseY));
        
        // セーブボタンを描画
        float saveButtonY = centerY - 20;
        Button saveButton = new Button(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        drawButton(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Save Game", saveButton.contains(mouseX, mouseY));
        
        // ロードボタンを描画
        float loadButtonY = centerY - buttonSpacing - 20;
        Button loadButton = new Button(centerX - buttonWidth / 2, loadButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        drawButton(centerX - buttonWidth / 2, loadButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Load Game", loadButton.contains(mouseX, mouseY));
        
        // Soundメニューボタンを描画
        float soundButtonY = centerY - buttonSpacing * 2 - 20;
        Button soundButton = new Button(centerX - buttonWidth / 2, soundButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        drawButton(centerX - buttonWidth / 2, soundButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Sound", soundButton.contains(mouseX, mouseY));
        
        // ゲーム終了ボタンを描画
        float quitButtonY = centerY - buttonSpacing * 3 - 20;
        Button quitButton = new Button(centerX - buttonWidth / 2, quitButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        drawButton(centerX - buttonWidth / 2, quitButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
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
        if (isMuted && newVolume > 0) {
            isMuted = false;
        }
        
        masterVolume = newVolume;
        updateMasterVolume();
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
        drawVolumeSlider(centerX, centerY);
        
        // 音量表示を描画
        font.getData().setScale(2.0f);
        font.setColor(isMuted ? Color.RED : Color.WHITE);
        String volumeText = "Volume: " + (int)(masterVolume * 100) + "%" + (isMuted ? " (MUTED)" : "");
        GlyphLayout volumeLayout = new GlyphLayout(font, volumeText);
        float volumeTextX = centerX - volumeLayout.width / 2;
        float volumeTextY = centerY + 50;
        font.draw(batch, volumeText, volumeTextX, volumeTextY);
        
        // 戻るボタンを描画
        float backButtonY = centerY - 200;
        Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        drawButton(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Back", backButton.contains(mouseX, mouseY));
        
        font.getData().setScale(2.0f); // 元に戻す
        
        batch.end();
    }
    
    /**
     * 音量スライダーを描画します。
     */
    private void drawVolumeSlider(float centerX, float centerY) {
        batch.end();
        
        float sliderWidth = 400;
        float sliderHeight = 20;
        float sliderX = centerX - sliderWidth / 2;
        float sliderY = centerY - sliderHeight / 2;
        
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        // スライダーの背景を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
        shapeRenderer.rect(sliderX, sliderY, sliderWidth, sliderHeight);
        shapeRenderer.end();
        
        // スライダーの枠線を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        shapeRenderer.rect(sliderX, sliderY, sliderWidth, sliderHeight);
        shapeRenderer.end();
        
        // スライダーのハンドルを描画（現在の音量位置に）
        float handleWidth = 30;
        float handleHeight = 40;
        float handleX = sliderX + (masterVolume * sliderWidth) - handleWidth / 2;
        float handleY = sliderY - (handleHeight - sliderHeight) / 2;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(isMuted ? Color.RED : Color.WHITE);
        shapeRenderer.rect(handleX, handleY, handleWidth, handleHeight);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(handleX, handleY, handleWidth, handleHeight);
        shapeRenderer.end();
        
        batch.begin();
    }
    
    /**
     * セーブメニューのマウスクリックを処理します。
     */
    private void handleSaveMenuClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !isTextInputActive) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            
            float buttonWidth = 320;
            float buttonHeight = 65;
            float centerX = screenWidth / 2;
            float centerY = screenHeight / 2;
            
            // 名前入力開始ボタン
            float inputButtonY = centerY - 80;
            Button inputButton = new Button(centerX - buttonWidth / 2, inputButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            // 戻るボタン
            float backButtonY = centerY - 200;
            Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            if (inputButton.contains(mouseX, mouseY)) {
                // テキスト入力を開始
                isTextInputActive = true;
                inputText.setLength(0);
                currentInputLabel = "Save Name";
            } else if (backButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.MAIN_MENU;
                isTextInputActive = false;
                inputText.setLength(0);
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
            
            java.util.List<String> saveList = getSaveFileList();
            
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
        
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // "SAVE GAME" テキストを中央に表示
        font.getData().setScale(3.0f);
        font.setColor(Color.WHITE);
        String titleText = "SAVE GAME";
        GlyphLayout titleLayout = new GlyphLayout(font, titleText);
        float titleX = (screenWidth - titleLayout.width) / 2;
        float titleY = screenHeight / 2 + 200;
        font.draw(batch, titleText, titleX, titleY);
        
        float buttonWidth = 320;
        float buttonHeight = 65;
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2;
        
        // テキスト入力フィールドを描画
        font.getData().setScale(2.0f);
        String inputLabel = "Save Name:";
        GlyphLayout labelLayout = new GlyphLayout(font, inputLabel);
        float labelX = centerX - labelLayout.width / 2;
        float labelY = centerY + 50;
        font.draw(batch, inputLabel, labelX, labelY);
        
        // 入力テキストを描画
        String displayText = isTextInputActive ? inputText.toString() + "_" : (inputText.length() > 0 ? inputText.toString() : "Click button to enter name");
        font.setColor(isTextInputActive ? Color.YELLOW : Color.LIGHT_GRAY);
        GlyphLayout textLayout = new GlyphLayout(font, displayText);
        float textX = centerX - textLayout.width / 2;
        float textY = centerY;
        font.draw(batch, displayText, textX, textY);
        
        // 名前入力開始ボタンを描画
        float inputButtonY = centerY - 80;
        Button inputButton = new Button(centerX - buttonWidth / 2, inputButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        drawButton(centerX - buttonWidth / 2, inputButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   isTextInputActive ? "Enter to confirm" : "Enter Save Name", inputButton.contains(mouseX, mouseY));
        
        // 戻るボタンを描画
        float backButtonY = centerY - 200;
        Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        drawButton(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Back", backButton.contains(mouseX, mouseY));
        
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
                drawButton(centerX - buttonWidth / 2, buttonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                          saveName, saveButton.contains(mouseX, mouseY));
            }
        }
        
        // 戻るボタンを描画
        float backButtonY = screenHeight / 2 - 200;
        Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        drawButton(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
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
        drawButton(yesButtonX, yesButtonY, buttonWidth, buttonHeight, 
                   "Yes", yesButton.contains(mouseX, mouseY));
        
        // Noボタンを描画（右側）
        float noButtonX = centerX + buttonSpacing - buttonWidth / 2;
        float noButtonY = centerY - buttonHeight / 2;
        Button noButton = new Button(noButtonX, noButtonY, buttonWidth, buttonHeight);
        drawButton(noButtonX, noButtonY, buttonWidth, buttonHeight, 
                   "No", noButton.contains(mouseX, mouseY));
        
        font.getData().setScale(2.0f); // 元に戻す
        
        batch.end();
    }
    
    /**
     * マスターボリュームを更新します。
     * 将来的に音声を追加したときに、この音量設定が適用されます。
     */
    private void updateMasterVolume() {
        // LibGDXのマスターボリュームを設定
        // 将来的にSoundやMusicを追加したときに、この設定が適用されます
        // Gdx.audio.setVolume(masterVolume); // このメソッドは存在しませんが、
        // 個別のSoundやMusicオブジェクトに対してsetVolume()を呼び出す必要があります
    }
    
    /**
     * 現在のマスターボリュームを取得します。
     * @return 0.0f ～ 1.0f の範囲の音量値
     */
    public float getMasterVolume() {
        return masterVolume;
    }
    
    /**
     * ボタンを描画します。
     * 注意: このメソッドはbatch.begin()が既に呼ばれている状態で呼び出す必要があります。
     * @param x ボタンのX座標
     * @param y ボタンのY座標
     * @param width ボタンの幅
     * @param height ボタンの高さ
     * @param text ボタンのテキスト
     * @param isHovered ホバー状態（trueの場合、視覚的に強調表示）
     */
    private void drawButton(float x, float y, float width, float height, String text, boolean isHovered) {
        // batchを一時的に終了してshapeRendererを使用
        batch.end();
        
        // ボタンの背景を描画
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // ホバー時は背景色を明るくする
        if (isHovered) {
            shapeRenderer.setColor(0.25f, 0.25f, 0.35f, 0.95f);
        } else {
            shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 0.95f);
        }
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        // ボタンの枠線を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // ホバー時は枠線を太く、明るくする
        if (isHovered) {
            shapeRenderer.setColor(0.8f, 0.8f, 1.0f, 1f);
            // 太い線を描画するために2回描画（簡易的な太線）
            shapeRenderer.rect(x - 1, y - 1, width + 2, height + 2);
            shapeRenderer.rect(x, y, width, height);
        } else {
            shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
            shapeRenderer.rect(x, y, width, height);
        }
        shapeRenderer.end();
        
        // batchを再開してテキストを描画
        batch.begin();
        font.getData().setScale(1.8f);
        // ホバー時はテキストを少し明るくする
        font.setColor(isHovered ? new Color(0.9f, 0.9f, 1.0f, 1f) : Color.WHITE);
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = x + (width - layout.width) / 2;
        float textY = y + (height + layout.height) / 2;
        font.draw(batch, text, textX, textY);
    }

    /**
     * テキスト入力を処理します。
     */
    private void handleTextInput() {
        // Enterキーで確定
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            String text = inputText.toString().trim();
            if (!text.isEmpty() && text.length() <= maxInputLength) {
                if (currentInputLabel.equals("Save Name")) {
                    // セーブ処理
                    if (saveGame(text)) {
                        Gdx.app.log("SaveGame", "Game saved successfully: " + text);
                        isPaused = false; // セーブ後はポーズを解除
                    } else {
                        Gdx.app.error("SaveGame", "Failed to save game");
                    }
                }
            }
            isTextInputActive = false;
            inputText.setLength(0);
            currentInputLabel = "";
            return;
        }
        
        // Backspaceキーで文字削除
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (inputText.length() > 0) {
                inputText.setLength(inputText.length() - 1);
            }
            return;
        }
        
        // Shiftキーの状態を確認
        boolean isShiftPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        
        // キーコードから文字を取得
        int keycode = -1;
        for (int i = Input.Keys.A; i <= Input.Keys.Z; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                keycode = i;
                break;
            }
        }
        
        if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
            char c = (char)(keycode - Input.Keys.A + (isShiftPressed ? 'A' : 'a'));
            if (inputText.length() < maxInputLength) {
                inputText.append(c);
            }
            return;
        }
        
        // 数字キー
        for (int i = Input.Keys.NUM_0; i <= Input.Keys.NUM_9; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                char c = (char)('0' + (i - Input.Keys.NUM_0));
                if (inputText.length() < maxInputLength) {
                    inputText.append(c);
                }
                return;
            }
        }
        
        // スペースキー
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (inputText.length() < maxInputLength) {
                inputText.append(' ');
            }
            return;
        }
        
        // アンダースコアとハイフン（Shift + ハイフンでアンダースコア）
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            char c = isShiftPressed ? '_' : '-';
            if (inputText.length() < maxInputLength) {
                inputText.append(c);
            }
            return;
        }
    }
    
    /**
     * ゲームの状態をセーブします。
     * @param saveName セーブデータ名（nullの場合はデフォルト名）
     * @return セーブが成功した場合true
     */
    public boolean saveGame(String saveName) {
        try {
            GameSaveData saveData = new GameSaveData();
            
            // プレイヤーの状態を保存
            saveData.playerTileX = player.getPlayerTileX();
            saveData.playerTileY = player.getPlayerTileY();
            
            // アイテムマネージャーの状態を保存
            saveData.collectedCount = itemManager.getCollectedCount();
            saveData.items = new java.util.ArrayList<>();
            for (Item item : itemManager.getItems()) {
                if (!item.isCollected()) {
                    GameSaveData.ItemData itemData = new GameSaveData.ItemData(
                        item.getTileX(),
                        item.getTileY(),
                        item.getType().name()
                    );
                    saveData.items.add(itemData);
                }
            }
            
            // 生成済みチャンクを保存
            saveData.generatedChunks = new java.util.ArrayList<>(itemManager.getGeneratedChunks());
            
            // 設定を保存
            saveData.showGrid = showGrid;
            saveData.masterVolume = masterVolume;
            saveData.isMuted = isMuted;
            saveData.cameraZoom = cameraZoom;
            
            // JSONにシリアライズして保存
            Json json = new Json();
            String jsonString = json.prettyPrint(saveData);
            
            com.badlogic.gdx.files.FileHandle saveFile = getSaveFileHandle(saveName);
            Gdx.app.log("SaveGame", "Writing to file: " + saveFile.file().getAbsolutePath());
            Gdx.app.log("SaveGame", "File exists before write: " + saveFile.exists());
            Gdx.app.log("SaveGame", "Parent directory exists: " + saveFile.parent().exists());
            Gdx.app.log("SaveGame", "Parent directory writable: " + saveFile.parent().file().canWrite());
            
            saveFile.writeString(jsonString, false);
            
            // 保存が成功したか確認
            if (saveFile.exists()) {
                Gdx.app.log("SaveGame", "File successfully written. Size: " + saveFile.length() + " bytes");
                return true;
            } else {
                Gdx.app.error("SaveGame", "File was not created after write operation");
                return false;
            }
        } catch (Exception e) {
            Gdx.app.error("SaveGame", "Failed to save game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * ゲームの状態をロードします。
     * @param saveName セーブデータ名
     * @return ロードが成功した場合true
     */
    public boolean loadGame(String saveName) {
        try {
            // セーブファイルが存在するか確認
            com.badlogic.gdx.files.FileHandle saveFile = getSaveFileHandle(saveName);
            if (!saveFile.exists()) {
                Gdx.app.log("LoadGame", "Save file not found");
                return false;
            }
            
            // セーブファイルを読み込む
            String jsonString = saveFile.readString();
            Json json = new Json();
            GameSaveData saveData = json.fromJson(GameSaveData.class, jsonString);
            
            // プレイヤーの状態を復元
            player.setPosition(saveData.playerTileX, saveData.playerTileY);
            
            // アイテムマネージャーの状態を復元
            itemManager.setCollectedCount(saveData.collectedCount);
            Array<Item> loadedItems = new Array<>();
            for (GameSaveData.ItemData itemData : saveData.items) {
                Item.ItemType type = Item.ItemType.valueOf(itemData.type);
                Item item = new Item(itemData.tileX, itemData.tileY, type);
                loadedItems.add(item);
            }
            itemManager.setItems(loadedItems);
            
            // 生成済みチャンクを復元
            java.util.Set<String> chunks = new java.util.HashSet<>(saveData.generatedChunks);
            itemManager.setGeneratedChunks(chunks);
            
            // 設定を復元
            showGrid = saveData.showGrid;
            masterVolume = saveData.masterVolume;
            isMuted = saveData.isMuted;
            cameraZoom = saveData.cameraZoom;
            updateMasterVolume();
            
            // カメラをプレイヤーの位置に設定
            float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
            float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
            camera.position.set(playerCenterX, playerCenterY, 0);
            camera.zoom = cameraZoom;
            camera.update();
            
            return true;
        } catch (Exception e) {
            Gdx.app.error("LoadGame", "Failed to load game: " + e.getMessage());
            return false;
        }
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
    }
    
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
