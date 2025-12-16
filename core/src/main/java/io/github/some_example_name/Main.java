package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.entity.Player;
import io.github.some_example_name.manager.ItemManager;
import io.github.some_example_name.manager.FarmManager;
import io.github.some_example_name.manager.LivestockManager;
import io.github.some_example_name.ui.UIRenderer;
import io.github.some_example_name.ui.InventoryUI;
import io.github.some_example_name.ui.ItemEncyclopediaUI;
import io.github.some_example_name.ui.MenuSystem;
import io.github.some_example_name.ui.FontManager;
import io.github.some_example_name.system.SaveGameManager;
import io.github.some_example_name.system.SoundSettings;
import io.github.some_example_name.system.TextInputHandler;
import io.github.some_example_name.system.InputHandler;
import io.github.some_example_name.game.Inventory;
import io.github.some_example_name.game.CivilizationLevel;
import io.github.some_example_name.entity.ItemData;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Player player;
    private ItemManager itemManager;
    private FarmManager farmManager;
    private LivestockManager livestockManager;
    
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
    private MenuSystem menuSystem;
    private InputHandler inputHandler;
    
    // インベントリシステム
    private Inventory inventory;
    private InventoryUI inventoryUI;
    private ItemEncyclopediaUI encyclopediaUI;
    private boolean inventoryOpen = false;
    private boolean showEncyclopedia = false; // アイテム図鑑を表示するかどうか
    
    // フォント管理
    private FontManager fontManager;
    
    // ポーズ状態
    private boolean isPaused;
    
    // グリッド表示フラグ（デフォルトはオン）
    private boolean showGrid = true;
    
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
        
        // フォントマネージャーを初期化
        fontManager = new FontManager();
        fontManager.initialize();
        font = fontManager.getJapaneseFont(); // 日本語対応フォントを使用
        font.getData().setScale(2.0f); // フォントサイズを大きく
        font.setColor(Color.WHITE);
        
        // プレイヤーを原点に配置（無限マップなので任意の位置から開始可能）
        player = new Player(0, 0);
        // インベントリを初期化
        inventory = new Inventory();
        // アイテムマネージャーを初期化（無限マップ対応）
        itemManager = new ItemManager();
        itemManager.setInventory(inventory); // インベントリを設定
        
        // 農地マネージャーを初期化
        farmManager = new FarmManager();
        farmManager.setInventory(inventory); // インベントリを設定
        
        // 畜産マネージャーを初期化
        livestockManager = new LivestockManager();
        livestockManager.setInventory(inventory); // インベントリを設定
        
        // ポーズ状態を初期化
        isPaused = false;
        
        // 分離したクラスのインスタンスを作成
        uiRenderer = new UIRenderer(shapeRenderer, batch, font, uiCamera, screenWidth, screenHeight);
        inventoryUI = new InventoryUI(shapeRenderer, batch, font, uiCamera, screenWidth, screenHeight);
        encyclopediaUI = new ItemEncyclopediaUI(shapeRenderer, batch, font, uiCamera, screenWidth, screenHeight);
        saveGameManager = new SaveGameManager();
        soundSettings = new SoundSettings();
        textInputHandler = new TextInputHandler();
        inputHandler = new InputHandler(player, farmManager, livestockManager);
        
        // MenuSystemのコールバック実装
        MenuSystem.MenuCallbacks menuCallbacks = new MenuSystem.MenuCallbacks() {
            @Override
            public void onSaveGame(String saveName) {
                if (saveGame(saveName)) {
                    Gdx.app.log("SaveGame", "Game saved successfully: " + saveName);
                    isPaused = false;
                } else {
                    Gdx.app.error("SaveGame", "Failed to save game");
                }
            }
            
            @Override
            public void onLoadGame(String saveName) {
                if (loadGame(saveName)) {
                    Gdx.app.log("LoadGame", "Game loaded successfully: " + saveName);
                    isPaused = false;
                } else {
                    Gdx.app.error("LoadGame", "Failed to load game: " + saveName);
                }
            }
            
            @Override
            public void onToggleGrid() {
                showGrid = !showGrid;
            }
            
            @Override
            public void onQuit() {
                Gdx.app.exit();
            }
            
            @Override
            public boolean isGridVisible() {
                return showGrid;
            }
            
            @Override
            public float getCameraZoom() {
                return cameraZoom;
            }
            
            @Override
            public void setPaused(boolean paused) {
                isPaused = paused;
            }
        };
        
        menuSystem = new MenuSystem(uiRenderer, saveGameManager, soundSettings, textInputHandler,
                shapeRenderer, batch, font, uiCamera, screenWidth, screenHeight, menuCallbacks);
        
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
                if (!menuSystem.handleEscapeKey()) {
                    // メニューが閉じられなかった場合（サブメニューから戻ったなど）
                }
            } else {
                // ポーズ
                isPaused = true;
                menuSystem.setCurrentMenuState(MenuSystem.MenuState.MAIN_MENU);
            }
        }
        
        // Eキーでインベントリを開閉（ポーズ中でない場合のみ）
        if (!isPaused && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            inventoryOpen = !inventoryOpen;
            showEncyclopedia = false; // インベントリを開くときは図鑑を閉じる
        }
        
        // インベントリまたは図鑑が開いているときはESCで閉じる
        if ((inventoryOpen || showEncyclopedia) && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            inventoryOpen = false;
            showEncyclopedia = false;
        }
        
        // インベントリが開いているときのマウスクリック処理
        if (inventoryOpen && !showEncyclopedia && Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            int mouseX = Gdx.input.getX();
            int mouseY = Gdx.input.getY();
            ItemData clickedItem = inventoryUI.handleClick(mouseX, mouseY);
            
            // アイテム図鑑ボタンがクリックされた場合（特殊値-1を使用）
            if (clickedItem != null && clickedItem.id == -1) {
                showEncyclopedia = true; // アイテム図鑑を表示
            }
        }
        
        // アイテム図鑑が開いているときのマウスクリック処理
        if (showEncyclopedia && Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            int mouseX = Gdx.input.getX();
            int mouseY = Gdx.input.getY();
            boolean backClicked = encyclopediaUI.handleClick(mouseX, mouseY);
            
            // 戻るボタンがクリックされた場合
            if (backClicked) {
                showEncyclopedia = false; // インベントリに戻る
            }
        }
        
        // テキスト入力処理
        if (textInputHandler.isTextInputActive()) {
            if (textInputHandler.handleInput()) {
                // Enterキーが押された場合の処理
                String text = textInputHandler.getInputText().trim();
                if (!text.isEmpty() && text.length() <= textInputHandler.getMaxInputLength()) {
                    if (textInputHandler.getCurrentInputLabel().equals("Save Name")) {
                        if (saveGame(text)) {
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
        
        // ポーズ中にマウスクリックとドラッグを処理
        if (isPaused) {
            menuSystem.handleMenuInput();
        }
        
        // ポーズ中でない場合のみゲームを更新
        if (!isPaused) {
            // プレイヤーを更新
            float deltaTime = Gdx.graphics.getDeltaTime();
            player.update(deltaTime);
            
            // アイテムマネージャーを更新（カメラの視野範囲を渡す）
            itemManager.update(deltaTime, player, camera);
            
            // 農地マネージャーを更新
            farmManager.update(deltaTime);
            
            // 畜産マネージャーを更新
            livestockManager.update(deltaTime);
            
            // 文明レベル進行チェック
            checkCivilizationLevelProgress();
            
            // キーボード入力処理
            inputHandler.handleInput();
            
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
        
        // 農地を描画（アイテムより下に描画）
        farmManager.render(shapeRenderer);
        
        // 畜産タイルを描画（農地の上に描画）
        livestockManager.render(shapeRenderer);
        
        // アイテムを描画
        itemManager.render(shapeRenderer);
        
        // プレイヤーを描画
        player.render(shapeRenderer);
        
        shapeRenderer.end();
        
        // UI情報を描画（取得アイテム数など）
        uiRenderer.drawUI(itemManager);
        
        // インベントリUIまたはアイテム図鑑UIを描画
        if (inventoryOpen) {
            if (showEncyclopedia) {
                // アイテム図鑑を表示
                encyclopediaUI.render(itemManager.getItemDataLoader());
                // ItemEncyclopediaUIがbatchを開始しているので、終了する
                batch.end();
            } else {
                // インベントリを表示
                inventoryUI.render(inventory, itemManager.getItemDataLoader());
                // InventoryUIがbatchを開始しているので、終了する
                batch.end();
            }
        }
        
        // ポーズメニューを描画
        if (isPaused) {
            menuSystem.render();
        }
    }
    
    
    
    /**
     * 文明レベルの進行をチェックします。
     */
    private void checkCivilizationLevelProgress() {
        io.github.some_example_name.game.CivilizationLevel civLevel = itemManager.getCivilizationLevel();
        
        // レベル1からレベル2への進行条件：アイテムを10個収集
        if (civLevel.getLevel() == 1 && itemManager.getCollectedCount() >= 10) {
            if (civLevel.levelUp()) {
                Gdx.app.log("Civilization", "Civilization level increased to " + civLevel.getLevel() + " (" + civLevel.getLevelName() + ")!");
            }
        }
        // レベル2以降の進行条件は今後追加
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
        
        // InventoryUIの画面サイズを更新
        if (inventoryUI != null) {
            inventoryUI.updateScreenSize(width, height);
        }
        
        // ItemEncyclopediaUIの画面サイズを更新
        if (encyclopediaUI != null) {
            encyclopediaUI.updateScreenSize(width, height);
        }
        
        // MenuSystemの画面サイズを更新
        if (menuSystem != null) {
            menuSystem.updateScreenSize(width, height);
        }
    }
    
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        if (fontManager != null) {
            fontManager.dispose();
        }
    }
}
