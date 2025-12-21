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
import io.github.some_example_name.manager.TerrainManager;
import io.github.some_example_name.manager.TerrainConversionManager;
import io.github.some_example_name.manager.BuildingManager;
import io.github.some_example_name.manager.TileDataLoader;
import io.github.some_example_name.ui.UIRenderer;
import io.github.some_example_name.ui.InventoryUI;
import io.github.some_example_name.ui.ItemEncyclopediaUI;
import io.github.some_example_name.ui.MenuSystem;
import io.github.some_example_name.ui.HelpUI;
import io.github.some_example_name.ui.FontManager;
import io.github.some_example_name.ui.Button;
import io.github.some_example_name.system.SaveGameManager;
import io.github.some_example_name.system.SoundSettings;
import io.github.some_example_name.system.SoundManager;
import io.github.some_example_name.system.TextInputHandler;
import io.github.some_example_name.system.InputHandler;
import io.github.some_example_name.system.GameStateManager;
import io.github.some_example_name.system.GameState;
import io.github.some_example_name.system.GameRenderer;
import io.github.some_example_name.system.GameController;
import io.github.some_example_name.system.PerformanceProfiler;
import io.github.some_example_name.game.Inventory;
import io.github.some_example_name.game.CraftingSystem;
import io.github.some_example_name.game.PreservedFoodManager;
import io.github.some_example_name.entity.ItemData;

/**
 * メインゲームクラス。
 * 
 * <p>このクラスはゲームのエントリーポイントであり、以下の責務を持ちます：</p>
 * <ul>
 *   <li>ゲームループの管理（render, update）</li>
 *   <li>リソースの初期化と解放（create, dispose）</li>
 *   <li>入力処理の統合</li>
 *   <li>ゲーム状態の管理（ポーズ、インベントリ、メニューなど）</li>
 *   <li>描画処理の統合</li>
 * </ul>
 * 
 * <p>注意：このクラスは大きくなりすぎているため、将来的には以下のように分割することを推奨します：</p>
 * <ul>
 *   <li>GameStateManager: 状態管理</li>
 *   <li>GameRenderer: 描画処理</li>
 *   <li>GameController: ゲームロジック</li>
 * </ul>
 * 
 * @author game_like_factorio
 * @version 1.0.0
 */
public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Player player;
    private ItemManager itemManager;
    private FarmManager farmManager;
    private LivestockManager livestockManager;
    private TerrainManager terrainManager;
    private TerrainConversionManager terrainConversionManager;
    private BuildingManager buildingManager;
    
    // カメラとビューポート
    private OrthographicCamera camera;
    private Viewport viewport;
    
    // UI用のカメラ（画面座標系）
    private OrthographicCamera uiCamera;
    
    // 分離したクラス
    private UIRenderer uiRenderer;
    private SaveGameManager saveGameManager;
    private SoundSettings soundSettings;
    private SoundManager soundManager;
    private TextInputHandler textInputHandler;
    private MenuSystem menuSystem;
    private InputHandler inputHandler;
    
    // インベントリシステム
    private Inventory inventory;
    private CraftingSystem craftingSystem;
    private PreservedFoodManager preservedFoodManager;
    private InventoryUI inventoryUI;
    private ItemEncyclopediaUI encyclopediaUI;
    private HelpUI helpUI;
    
    // ゲーム状態管理
    private GameStateManager gameStateManager;
    
    // ゲームシステム
    private GameRenderer gameRenderer;
    private GameController gameController;
    private PerformanceProfiler performanceProfiler;
    
    // フォント管理
    private FontManager fontManager;
    
    // 後方互換性のためのフラグ（段階的にGameStateManagerに移行）
    @Deprecated
    private boolean inventoryOpen = false;
    @Deprecated
    private boolean showEncyclopedia = false; // アイテム図鑑を表示するかどうか
    @Deprecated
    private boolean isPaused;
    
    // グリッド表示フラグ（デフォルトはオン）
    private boolean showGrid = true;
    
    // ゲームの論理的な画面サイズ（ピクセル単位）- 基準サイズ
    private static final float BASE_VIEWPORT_SIZE = 20 * Player.TILE_SIZE;
    
    // ゲーム開始時の初期アイテム数
    private static final int INITIAL_ITEM_COUNT = 100;
    
    // フォントスケール設定
    private static final float DEFAULT_FONT_SCALE = 2.0f;
    private static final float CIVILIZATION_MESSAGE_FONT_SCALE = 1.0f;
    
    // 文明レベルアップメッセージの背景透明度
    private static final float CIVILIZATION_MESSAGE_BG_ALPHA = 0.7f;
    private static final float CIVILIZATION_MESSAGE_PADDING = 20f;
    
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
    
        // 文明レベルアップメッセージ（GameControllerに移動されたが、後方互換性のため残す）
    @Deprecated
    private String civilizationLevelUpMessage = null;
    @Deprecated
    private float civilizationLevelUpMessageTimer = 0f;
    @Deprecated
    private static final float CIVILIZATION_MESSAGE_DURATION = 3.0f; // 3秒間表示
    
    
    /**
     * ゲームの初期化処理を行います。
     * 
     * <p>このメソッドでは以下の順序で初期化を行います：</p>
     * <ol>
     *   <li>画面サイズとビューポートの設定</li>
     *   <li>カメラの初期化</li>
     *   <li>グラフィックスリソースの作成（ShapeRenderer, SpriteBatch）</li>
     *   <li>データローダーの初期化</li>
     *   <li>フォントマネージャーの初期化</li>
     *   <li>ゲームオブジェクトの作成（Player, Inventory, Managers）</li>
     *   <li>UIコンポーネントの初期化</li>
     *   <li>入力ハンドラーの設定</li>
     * </ol>
     * 
     * <p>エラーが発生した場合、ログに記録されますが、ゲームは続行されます。</p>
     */
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
        
        // タイルデータローダーを初期化（他のマネージャーより先に初期化）
        TileDataLoader.initialize();
        
        // フォントマネージャーを初期化
        fontManager = new FontManager();
        fontManager.initialize();
        font = fontManager.getJapaneseFont(); // 日本語対応フォントを使用
        font.getData().setScale(DEFAULT_FONT_SCALE); // フォントサイズを大きく
        font.setColor(Color.WHITE);
        
        // プレイヤーを原点に配置（無限マップなので任意の位置から開始可能）
        player = new Player(0, 0);
        // インベントリを初期化
        inventory = new Inventory();
        
        // 保存食マネージャーを初期化
        preservedFoodManager = new PreservedFoodManager();
        
        // クラフトシステムを初期化
        craftingSystem = new CraftingSystem(inventory);
        craftingSystem.setPreservedFoodManager(preservedFoodManager);
        
        // ゲーム状態管理を初期化
        gameStateManager = new GameStateManager();
        
        // ポーズ状態を初期化（後方互換性のため）
        isPaused = false;
        
        // 分離したクラスのインスタンスを作成
        uiRenderer = new UIRenderer(shapeRenderer, batch, font, uiCamera, screenWidth, screenHeight);
        inventoryUI = new InventoryUI(shapeRenderer, batch, font, uiCamera, screenWidth, screenHeight);
        encyclopediaUI = new ItemEncyclopediaUI(shapeRenderer, batch, font, uiCamera, screenWidth, screenHeight);
        helpUI = new HelpUI(shapeRenderer, batch, font, uiCamera, screenWidth, screenHeight);
        saveGameManager = new SaveGameManager();
        soundSettings = new SoundSettings();
        soundManager = new SoundManager(soundSettings); // SoundManagerを先に作成
        
        // プレイヤーにSoundManagerを設定
        player.setSoundManager(soundManager);
        
        // アイテムマネージャーを初期化（無限マップ対応）
        itemManager = new ItemManager();
        itemManager.setInventory(inventory); // インベントリを設定
        itemManager.setSoundManager(soundManager); // サウンドマネージャーを設定
        
        // ゲーム開始時に全種類のアイテムを初期数追加
        for (ItemData itemData : itemManager.getItemDataLoader().getAllItems()) {
            inventory.addItem(itemData.id, INITIAL_ITEM_COUNT);
        }
        
        // 農地マネージャーを初期化
        farmManager = new FarmManager();
        farmManager.setInventory(inventory); // インベントリを設定
        farmManager.setItemDataLoader(itemManager.getItemDataLoader()); // アイテムデータローダーを設定
        
        // 地形マネージャーを初期化
        terrainManager = new TerrainManager();
        
        // 地形変換マネージャーを初期化
        terrainConversionManager = new TerrainConversionManager();
        terrainConversionManager.setTerrainManager(terrainManager);
        terrainConversionManager.setInventory(inventory);
        terrainConversionManager.setItemDataLoader(itemManager.getItemDataLoader());
        
        // 農地マネージャーに地形マネージャーを設定
        farmManager.setTerrainManager(terrainManager);
        
        // 畜産マネージャーを初期化
        livestockManager = new LivestockManager();
        livestockManager.setInventory(inventory); // インベントリを設定
        livestockManager.setTerrainManager(terrainManager); // 地形マネージャーを設定
        livestockManager.setCivilizationLevel(itemManager.getCivilizationLevel()); // 文明レベルを設定
        
        // 建物マネージャーを初期化
        buildingManager = new BuildingManager();
        buildingManager.setInventory(inventory);
        buildingManager.setItemDataLoader(itemManager.getItemDataLoader());
        buildingManager.setTerrainManager(terrainManager);
        
        textInputHandler = new TextInputHandler();
        inputHandler = new InputHandler(player, farmManager, livestockManager);
        inputHandler.setTerrainManager(terrainManager); // 地形マネージャーを設定
        inputHandler.setTerrainConversionManager(terrainConversionManager); // 地形変換マネージャーを設定
        inputHandler.setBuildingManager(buildingManager); // 建物マネージャーを設定
        
        // プレイヤーに地形マネージャーを設定（タイルタイプに応じた足音のため）
        player.setTerrainManager(terrainManager);
        
        // UIコンポーネントにSoundManagerを設定
        inventoryUI.setSoundManager(soundManager);
        encyclopediaUI.setSoundManager(soundManager);
        helpUI.setSoundManager(soundManager);
        
        // InventoryUIにクラフトシステムとItemDataLoaderを設定
        inventoryUI.setCraftingSystem(craftingSystem);
        inventoryUI.setItemDataLoader(itemManager.getItemDataLoader());
        
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
        
        menuSystem = new MenuSystem(uiRenderer, saveGameManager, soundSettings, soundManager, textInputHandler,
                shapeRenderer, batch, font, uiCamera, screenWidth, screenHeight, menuCallbacks, helpUI);
        
        // MenuSystemにLivestockDataLoaderを設定
        menuSystem.setLivestockDataLoader(livestockManager.getLivestockDataLoader());
        
        // GameRendererを初期化
        gameRenderer = new GameRenderer(shapeRenderer, batch, font, camera, uiCamera, viewport, 
            screenWidth, screenHeight);
        gameRenderer.setManagers(terrainManager, itemManager, farmManager, livestockManager, buildingManager, player);
        gameRenderer.setUIComponents(uiRenderer, inventoryUI, encyclopediaUI, menuSystem);
        gameRenderer.setShowGrid(showGrid);
        
        // GameControllerを初期化
        gameController = new GameController();
        gameController.setGameObjects(player, terrainManager, itemManager, farmManager, 
            livestockManager, buildingManager, preservedFoodManager, camera);
        
        // GameRendererにGameControllerを設定（エンディング画面用）
        gameRenderer.setGameController(gameController);
        
        // パフォーマンスプロファイラーを初期化（デフォルトでは無効）
        performanceProfiler = PerformanceProfiler.getInstance();
        // デバッグモードで有効化する場合は以下のコメントを外す
        // performanceProfiler.setEnabled(true);
        
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
                // ヘルプメニューが開いている場合はHelpUIにスクロール入力を渡す
                if (isPaused && menuSystem != null && 
                    menuSystem.getCurrentMenuState() == MenuSystem.MenuState.HELP_MENU) {
                    if (helpUI != null) {
                        helpUI.handleScroll(amountY);
                        return true;
                    }
                }
                
                // 通常時はカメラズームを変更
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

    /**
     * ゲームのメインループです。
     * 
     * <p>このメソッドは毎フレーム呼び出され、以下の処理を行います：</p>
     * <ol>
     *   <li>画面サイズの変更を検出してビューポートを更新</li>
     *   <li>入力処理（キーボード、マウス）</li>
     *   <li>ゲーム状態の更新（ポーズ中でない場合）</li>
     *   <li>描画処理（地形、アイテム、プレイヤー、UI）</li>
     * </ol>
     * 
     * <p>エラーが発生した場合、ログに記録されますが、ゲームは続行されます。</p>
     */
    @Override
    public void render() {
        // パフォーマンスプロファイリング: フレーム開始
        if (performanceProfiler != null && performanceProfiler.isEnabled()) {
            performanceProfiler.startFrame();
        }
        
        // 画面サイズが変更された場合、ビューポートを更新
        if (screenWidth != Gdx.graphics.getWidth() || screenHeight != Gdx.graphics.getHeight()) {
            screenWidth = Gdx.graphics.getWidth();
            screenHeight = Gdx.graphics.getHeight();
            viewport.update(screenWidth, screenHeight);
            camera.update();
        }
        
        // ビューポートを適用
        viewport.apply();
        
        // 画面をクリア（地形が描画されるので、より明るい背景色に）
        ScreenUtils.clear(0.2f, 0.25f, 0.3f, 1f);
        
        // ESCキーでポーズ/再開を切り替え、またはメニューから戻る
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (menuSystem != null && gameStateManager != null) {
                // ゲームガイドが開いている場合は直接ゲーム画面に戻る
                if (menuSystem.getCurrentMenuState() == MenuSystem.MenuState.HELP_MENU) {
                    gameStateManager.setState(GameState.PLAYING);
                    isPaused = false;
                    menuSystem.setCurrentMenuState(MenuSystem.MenuState.MAIN_MENU);
                } else if (isPaused) {
                    if (!menuSystem.handleEscapeKey()) {
                        // メニューが閉じられなかった場合（サブメニューから戻ったなど）
                    } else {
                        // メニューが閉じられた場合、ゲーム状態を更新
                        gameStateManager.setState(GameState.PLAYING);
                        isPaused = false;
                    }
                } else {
                    // ポーズ
                    gameStateManager.setState(GameState.PAUSED);
                    isPaused = true;
                    menuSystem.setCurrentMenuState(MenuSystem.MenuState.MAIN_MENU);
                }
            } else {
                // menuSystemがnullの場合は単純にポーズを切り替え
                isPaused = !isPaused;
                if (gameStateManager != null) {
                    gameStateManager.setState(isPaused ? GameState.PAUSED : GameState.PLAYING);
                }
            }
        }
        
        // Hキーでヘルプを開閉
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            if (menuSystem != null && gameStateManager != null) {
                if (menuSystem.getCurrentMenuState() == MenuSystem.MenuState.HELP_MENU) {
                    // ヘルプが開いている場合は閉じる
                    gameStateManager.setState(GameState.PLAYING);
                    menuSystem.setCurrentMenuState(MenuSystem.MenuState.MAIN_MENU);
                    isPaused = false;
                } else {
                    // ヘルプを開く
                    gameStateManager.setState(GameState.HELP_MENU);
                    isPaused = true;
                    menuSystem.setCurrentMenuState(MenuSystem.MenuState.HELP_MENU);
                }
            }
        }
        
        // Eキーでインベントリを開閉（ポーズ中でない場合のみ）
        if (!isPaused && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (gameStateManager.isInventoryOpen()) {
                gameStateManager.setState(GameState.PLAYING);
                inventoryOpen = false;
                showEncyclopedia = false;
            } else {
                gameStateManager.setState(GameState.INVENTORY_OPEN);
                inventoryOpen = true;
                showEncyclopedia = false;
            }
        }
        
        // インベントリまたは図鑑が開いているときはESCで閉じる
        if ((inventoryOpen || showEncyclopedia) && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            gameStateManager.setState(GameState.PLAYING);
            inventoryOpen = false;
            showEncyclopedia = false;
        }
        
        // インベントリが開いているときのマウスクリック処理
        if (inventoryOpen && !showEncyclopedia && inventoryUI != null && Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            try {
                int mouseX = Gdx.input.getX();
                int mouseY = Gdx.input.getY();
                ItemData clickedItem = inventoryUI.handleClick(mouseX, mouseY);
                
                // アイテム図鑑ボタンがクリックされた場合（特殊値-1を使用）
                if (clickedItem != null && clickedItem.id == -1) {
                    gameStateManager.setState(GameState.ENCYCLOPEDIA_OPEN);
                    showEncyclopedia = true; // アイテム図鑑を表示
                }
                // クラフトが実行された場合（特殊値-2を使用）
                if (clickedItem != null && clickedItem.id == -2) {
                    // クラフト成功（音を再生するなど）
                    if (soundManager != null) {
                        soundManager.playCraftSound();
                    }
                }
            } catch (Exception e) {
                Gdx.app.error("Main", "Error handling inventory click: " + e.getMessage(), e);
            }
        }
        
        // アイテム図鑑が開いているときのマウスクリック処理
        if (showEncyclopedia && encyclopediaUI != null && Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            try {
                int mouseX = Gdx.input.getX();
                int mouseY = Gdx.input.getY();
                boolean backClicked = encyclopediaUI.handleClick(mouseX, mouseY);
                
                // 戻るボタンがクリックされた場合
                if (backClicked) {
                    gameStateManager.setState(GameState.INVENTORY_OPEN);
                    showEncyclopedia = false; // インベントリに戻る
                }
            } catch (Exception e) {
                Gdx.app.error("Main", "Error handling encyclopedia click: " + e.getMessage(), e);
            }
        }
        
        // ゲームガイドボタンのクリック処理（ポーズ中でない場合のみ）
        if (!isPaused && !inventoryOpen && !showEncyclopedia && uiRenderer != null && menuSystem != null && Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            try {
                int mouseX = Gdx.input.getX();
                int mouseY = Gdx.input.getY();
                Button guideButton = uiRenderer.getGuideButton();
                if (guideButton != null) {
                    float uiY = screenHeight - mouseY;
                    if (guideButton.contains((float)mouseX, uiY)) {
                        // ゲームガイドを開く
                        gameStateManager.setState(GameState.HELP_MENU);
                        isPaused = true;
                        menuSystem.setCurrentMenuState(MenuSystem.MenuState.HELP_MENU);
                        if (helpUI != null) {
                            helpUI.onOpen();
                        }
                    }
                }
            } catch (Exception e) {
                Gdx.app.error("Main", "Error handling guide button click: " + e.getMessage(), e);
            }
        }
        
        // テキスト入力処理
        if (textInputHandler != null && textInputHandler.isTextInputActive()) {
            try {
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
            } catch (Exception e) {
                Gdx.app.error("Main", "Error handling text input: " + e.getMessage(), e);
            }
        }
        
        // ポーズ中にマウスクリックとドラッグを処理
        if (isPaused && menuSystem != null) {
            try {
                menuSystem.handleMenuInput();
            } catch (Exception e) {
                Gdx.app.error("Main", "Error handling menu input: " + e.getMessage(), e);
            }
        }
        
        // ポーズ中でない場合のみゲームを更新
        // パフォーマンス: ポーズ中はゲームロジックをスキップして描画のみ行う
        if (!isPaused && gameController != null) {
            try {
                if (performanceProfiler != null && performanceProfiler.isEnabled()) {
                    performanceProfiler.startSection("update");
                }
                
                float deltaTime = Gdx.graphics.getDeltaTime();
                gameController.update(deltaTime);
                
                // キーボード入力処理
                if (inputHandler != null) {
                    if (performanceProfiler != null && performanceProfiler.isEnabled()) {
                        performanceProfiler.startSection("input");
                    }
                    inputHandler.handleInput();
                    if (performanceProfiler != null && performanceProfiler.isEnabled()) {
                        performanceProfiler.endSection("input");
                    }
                }
                
                if (performanceProfiler != null && performanceProfiler.isEnabled()) {
                    performanceProfiler.endSection("update");
                }
                
                // 文明レベルアップメッセージをGameRendererに渡す
                String civMessage = gameController.getCivilizationLevelUpMessage();
                if (gameRenderer != null) {
                    gameRenderer.setCivilizationLevelUpMessage(civMessage);
                }
            } catch (Exception e) {
                Gdx.app.error("Main", "Error in game update: " + e.getMessage(), e);
            }
        }
        
        // カメラのズームを適用（スクロールで変更された可能性があるため）
        if (camera != null) {
            camera.zoom = cameraZoom;
            camera.update();
        }
        
        // GameRendererを使用して描画処理を行う
        if (gameRenderer != null) {
            if (performanceProfiler != null && performanceProfiler.isEnabled()) {
                performanceProfiler.startSection("render");
            }
            gameRenderer.render(isPaused, inventoryOpen, showEncyclopedia, inventory);
            if (performanceProfiler != null && performanceProfiler.isEnabled()) {
                performanceProfiler.endSection("render");
            }
        } else {
            // フォールバック: 旧コードを使用（後方互換性のため）
            renderFallback();
        }
        
        // パフォーマンスプロファイリング: フレーム終了
        if (performanceProfiler != null && performanceProfiler.isEnabled()) {
            performanceProfiler.endFrame();
            
            // 60フレームごとに結果をログ出力
            if (performanceProfiler.getFPS() > 0 && 
                (long)(performanceProfiler.getFPS() * 60) % 3600 == 0) {
                performanceProfiler.logResults();
            }
        }
    }
    
    /**
     * フォールバック用の描画処理（後方互換性のため）。
     * GameRendererが使用できない場合にのみ呼び出されます。
     */
    private void renderFallback() {
        // このメソッドは後方互換性のため残していますが、
        // 通常はGameRendererが使用されるため呼び出されることはありません
        Gdx.app.log("Main", "Using fallback render method");
    }
    
    /**
     * 文明レベルアップメッセージを描画します。
     * @deprecated GameRendererを使用してください
     */
    @Deprecated
    private void drawCivilizationLevelUpMessage() {
        if (civilizationLevelUpMessage == null || batch == null || shapeRenderer == null || font == null) {
            return;
        }
        
        // フォント設定を保存
        float originalFontScale = font.getData().scaleX;
        Color originalFontColor = font.getColor().cpy();
        
        try {
            // テキストレイアウトを計算
            font.getData().setScale(CIVILIZATION_MESSAGE_FONT_SCALE);
            font.setColor(Color.WHITE);
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, civilizationLevelUpMessage);
            float x = (screenWidth - layout.width) / 2;
            float y = screenHeight / 2;
            
            // 背景を描画（半透明の黒）
            shapeRenderer.setProjectionMatrix(uiCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0f, CIVILIZATION_MESSAGE_BG_ALPHA);
            shapeRenderer.rect(x - CIVILIZATION_MESSAGE_PADDING, y - layout.height - CIVILIZATION_MESSAGE_PADDING, 
                    layout.width + CIVILIZATION_MESSAGE_PADDING * 2, layout.height + CIVILIZATION_MESSAGE_PADDING * 2);
            shapeRenderer.end();
            
            // テキストを描画
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            font.draw(batch, civilizationLevelUpMessage, x, y);
            batch.end();
        } finally {
            // フォント設定を復元
            font.getData().setScale(originalFontScale);
            font.setColor(originalFontColor);
        }
    }
    
    
    
    /**
     * 文明レベルの進行をチェックします。
     * @deprecated GameControllerを使用してください
     */
    @Deprecated
    private void checkCivilizationLevelProgress() {
        // このメソッドはGameControllerに移動されました
        // 後方互換性のため残していますが、使用されません
    }
    
    /**
     * 文明レベルアップ時のメッセージを表示します。
     * @deprecated GameControllerを使用してください
     */
    @Deprecated
    private void showCivilizationLevelUpMessage(String levelName) {
        // このメソッドはGameControllerに移動されました
        // 後方互換性のため残していますが、使用されません
    }
    
    /**
     * ゲームの状態をセーブします。
     * 
     * <p>セーブされるデータには以下が含まれます：</p>
     * <ul>
     *   <li>プレイヤーの位置と状態</li>
     *   <li>アイテムマネージャーの状態（収集したアイテムなど）</li>
     *   <li>グリッド表示設定</li>
     *   <li>サウンド設定（ボリューム、ミュート状態）</li>
     *   <li>カメラズームレベル</li>
     * </ul>
     * 
     * @param saveName セーブデータ名（nullの場合はデフォルト名が使用される）
     * @return セーブが成功した場合true、失敗した場合false
     */
    public boolean saveGame(String saveName) {
        return saveGameManager.saveGame(saveName, player, itemManager, showGrid,
                soundSettings.getMasterVolume(), soundSettings.isMuted(), cameraZoom);
    }
    
    /**
     * ゲームの状態をロードします。
     * 
     * <p>ロード後、以下の処理が自動的に行われます：</p>
     * <ul>
     *   <li>プレイヤーの位置と状態の復元</li>
     *   <li>アイテムマネージャーの状態の復元</li>
     *   <li>設定の復元（グリッド表示、サウンド設定、ズームレベル）</li>
     *   <li>カメラ位置の更新</li>
     * </ul>
     * 
     * @param saveName セーブデータ名（nullの場合はデフォルト名が使用される）
     * @return ロードが成功した場合true、失敗した場合false
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
    
    /**
     * 画面サイズが変更されたときに呼び出されます。
     * 
     * <p>このメソッドでは以下の処理を行います：</p>
     * <ul>
     *   <li>ビューポートの更新</li>
     *   <li>カメラの更新</li>
     *   <li>UIコンポーネントの画面サイズの更新</li>
     * </ul>
     * 
     * @param width 新しい画面幅（ピクセル）
     * @param height 新しい画面高さ（ピクセル）
     */
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
        
        // GameRendererの画面サイズを更新
        if (gameRenderer != null) {
            gameRenderer.updateScreenSize(width, height);
        }
    }
    
    /**
     * リソースの解放処理を行います。
     * 
     * <p>このメソッドはゲーム終了時に呼び出され、以下のリソースを解放します：</p>
     * <ul>
     *   <li>SoundManager（サウンドリソース）</li>
     *   <li>TerrainManager（テクスチャリソース）</li>
     *   <li>FontManager（フォントリソース）</li>
     *   <li>ShapeRenderer、SpriteBatch（グラフィックスリソース）</li>
     * </ul>
     * 
     * <p>リソースは適切な順序で解放されます（依存関係を考慮）。</p>
     */
    @Override
    public void dispose() {
        // リソースを適切な順序で解放
        if (soundManager != null) {
            soundManager.dispose();
            soundManager = null;
        }
        
        if (terrainManager != null) {
            terrainManager.dispose();
            terrainManager = null;
        }
        
        if (itemManager != null) {
            // ItemManagerにdispose()があれば呼び出す
            // 現時点では明示的なdispose()はないが、将来の拡張に備える
            itemManager = null;
        }
        
        if (farmManager != null) {
            farmManager = null;
        }
        
        if (livestockManager != null) {
            livestockManager = null;
        }
        
        if (terrainConversionManager != null) {
            terrainConversionManager = null;
        }
        
        if (fontManager != null) {
            fontManager.dispose();
            fontManager = null;
        }
        
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
    }
}
