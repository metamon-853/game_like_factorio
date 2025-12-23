package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.entity.ItemData;
import io.github.some_example_name.entity.Player;
import io.github.some_example_name.game.CraftingSystem;
import io.github.some_example_name.game.Inventory;
import io.github.some_example_name.game.PreservedFoodManager;
import io.github.some_example_name.manager.BuildingManager;
import io.github.some_example_name.manager.FarmManager;
import io.github.some_example_name.manager.ItemManager;
import io.github.some_example_name.manager.LivestockManager;
import io.github.some_example_name.manager.TerrainConversionManager;
import io.github.some_example_name.manager.TerrainManager;
import io.github.some_example_name.manager.TileDataLoader;
import io.github.some_example_name.ui.FontManager;
import io.github.some_example_name.ui.HelpUI;
import io.github.some_example_name.ui.InventoryUI;
import io.github.some_example_name.ui.ItemEncyclopediaUI;
import io.github.some_example_name.ui.MenuSystem;
import io.github.some_example_name.ui.UIRenderer;

/**
 * ゲームの初期化処理を担当するクラス。
 * 
 * <p>このクラスは以下の初期化処理を統合管理します：</p>
 * <ul>
 *   <li>画面サイズとビューポートの設定</li>
 *   <li>カメラの初期化</li>
 *   <li>グラフィックスリソースの作成</li>
 *   <li>データローダーの初期化</li>
 *   <li>ゲームオブジェクトの作成</li>
 *   <li>UIコンポーネントの初期化</li>
 * </ul>
 * 
 * @author game_like_factorio
 * @version 1.0.0
 */
public class GameInitializer {
    // ゲーム開始時の初期アイテム数
    private static final int INITIAL_ITEM_COUNT = 100;
    
    // フォントスケール設定
    private static final float DEFAULT_FONT_SCALE = 2.0f;
    
    // ゲームの論理的な画面サイズ（ピクセル単位）- 基準サイズ
    private static final float BASE_VIEWPORT_SIZE = 20 * Player.TILE_SIZE;
    
    /**
     * ゲームの初期化結果を保持するクラス。
     */
    public static class InitializationResult {
        public ShapeRenderer shapeRenderer;
        public SpriteBatch batch;
        public BitmapFont font;
        public OrthographicCamera camera;
        public OrthographicCamera uiCamera;
        public Viewport viewport;
        public Player player;
        public Inventory inventory;
        public CraftingSystem craftingSystem;
        public PreservedFoodManager preservedFoodManager;
        public ItemManager itemManager;
        public FarmManager farmManager;
        public LivestockManager livestockManager;
        public BuildingManager buildingManager;
        public TerrainManager terrainManager;
        public TerrainConversionManager terrainConversionManager;
        public UIRenderer uiRenderer;
        public InventoryUI inventoryUI;
        public ItemEncyclopediaUI encyclopediaUI;
        public HelpUI helpUI;
        public MenuSystem menuSystem;
        public InputHandler inputHandler;
        public SoundManager soundManager;
        public SoundSettings soundSettings;
        public SaveGameManager saveGameManager;
        public TextInputHandler textInputHandler;
        public GameStateManager gameStateManager;
        public FontManager fontManager;
        public int screenWidth;
        public int screenHeight;
        public float viewportWidth;
        public float viewportHeight;
    }
    
    /**
     * ゲームの初期化処理を行います。
     * 
     * @return 初期化結果
     */
    public static InitializationResult initialize() {
        InitializationResult result = new InitializationResult();
        
        try {
            // 画面サイズを取得
            result.screenWidth = Gdx.graphics.getWidth();
            result.screenHeight = Gdx.graphics.getHeight();
            
            // 画面のアスペクト比を計算
            float screenAspect = (float)result.screenWidth / (float)result.screenHeight;
            
            // 正方形の升を保つため、画面のアスペクト比に応じてビューポートサイズを調整
            if (screenAspect > 1.0f) {
                // 横長の画面：高さを基準にして幅を調整
                result.viewportHeight = BASE_VIEWPORT_SIZE;
                result.viewportWidth = BASE_VIEWPORT_SIZE * screenAspect;
            } else {
                // 縦長の画面：幅を基準にして高さを調整
                result.viewportWidth = BASE_VIEWPORT_SIZE;
                result.viewportHeight = BASE_VIEWPORT_SIZE / screenAspect;
            }
            
            // カメラとビューポートを初期化
            result.camera = new OrthographicCamera();
            result.camera.setToOrtho(false, result.viewportWidth, result.viewportHeight);
            result.viewport = new StretchViewport(result.viewportWidth, result.viewportHeight, result.camera);
            result.viewport.update(result.screenWidth, result.screenHeight);
            result.camera.update();
            
            // UI用のカメラを初期化（画面座標系）
            result.uiCamera = new OrthographicCamera();
            result.uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            result.uiCamera.update();
            
            // グラフィックスリソースを作成
            result.shapeRenderer = new ShapeRenderer();
            result.batch = new SpriteBatch();
            
            // タイルデータローダーを初期化（他のマネージャーより先に初期化）
            TileDataLoader.initialize();
            
            // フォントマネージャーを初期化
            result.fontManager = new FontManager();
            result.fontManager.initialize();
            result.font = result.fontManager.getJapaneseFont();
            result.font.getData().setScale(DEFAULT_FONT_SCALE);
            result.font.setColor(Color.WHITE);
            
            // ゲームオブジェクトを作成
            result.player = new Player(0, 0);
            result.inventory = new Inventory();
            result.preservedFoodManager = new PreservedFoodManager();
            result.craftingSystem = new CraftingSystem(result.inventory);
            result.craftingSystem.setPreservedFoodManager(result.preservedFoodManager);
            
            // ゲーム状態管理を初期化
            result.gameStateManager = new GameStateManager();
            
            // サウンドシステムを初期化
            result.soundSettings = new SoundSettings();
            result.soundManager = new SoundManager(result.soundSettings);
            result.player.setSoundManager(result.soundManager);
            
            // マネージャーを初期化
            initializeManagers(result);
            
            // UIコンポーネントを初期化
            initializeUI(result);
            
            // 入力ハンドラーを初期化
            initializeInputHandlers(result);
            
            // カメラをプレイヤーの初期位置に設定
            float playerCenterX = result.player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
            float playerCenterY = result.player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
            result.camera.position.set(playerCenterX, playerCenterY, 0);
            result.camera.update();
            
        } catch (Exception e) {
            Gdx.app.error("GameInitializer", "Error during initialization: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize game", e);
        }
        
        return result;
    }
    
    /**
     * マネージャーを初期化します。
     */
    private static void initializeManagers(InitializationResult result) {
        // アイテムマネージャーを初期化
        result.itemManager = new ItemManager();
        result.itemManager.setInventory(result.inventory);
        result.itemManager.setSoundManager(result.soundManager);
        
        // ゲーム開始時に全種類のアイテムを初期数追加
        for (ItemData itemData : result.itemManager.getItemDataLoader().getAllItems()) {
            result.inventory.addItem(itemData.id, INITIAL_ITEM_COUNT);
        }
        
        // 農地マネージャーを初期化
        result.farmManager = new FarmManager();
        result.farmManager.setInventory(result.inventory);
        result.farmManager.setItemDataLoader(result.itemManager.getItemDataLoader());
        
        // 地形マネージャーを初期化
        result.terrainManager = new TerrainManager();
        
        // 地形変換マネージャーを初期化
        result.terrainConversionManager = new TerrainConversionManager();
        result.terrainConversionManager.setTerrainManager(result.terrainManager);
        result.terrainConversionManager.setInventory(result.inventory);
        result.terrainConversionManager.setItemDataLoader(result.itemManager.getItemDataLoader());
        
        // 農地マネージャーに地形マネージャーを設定
        result.farmManager.setTerrainManager(result.terrainManager);
        
        // 畜産マネージャーを初期化
        result.livestockManager = new LivestockManager();
        result.livestockManager.setInventory(result.inventory);
        result.livestockManager.setTerrainManager(result.terrainManager);
        result.livestockManager.setCivilizationLevel(result.itemManager.getCivilizationLevel());
        
        // 建物マネージャーを初期化
        result.buildingManager = new BuildingManager();
        result.buildingManager.setInventory(result.inventory);
        result.buildingManager.setItemDataLoader(result.itemManager.getItemDataLoader());
        result.buildingManager.setTerrainManager(result.terrainManager);
        
        // プレイヤーに地形マネージャーを設定
        result.player.setTerrainManager(result.terrainManager);
    }
    
    /**
     * UIコンポーネントを初期化します。
     */
    private static void initializeUI(InitializationResult result) {
        result.uiRenderer = new UIRenderer(result.shapeRenderer, result.batch, result.font, 
            result.uiCamera, result.screenWidth, result.screenHeight);
        result.inventoryUI = new InventoryUI(result.shapeRenderer, result.batch, result.font, 
            result.uiCamera, result.screenWidth, result.screenHeight);
        result.encyclopediaUI = new ItemEncyclopediaUI(result.shapeRenderer, result.batch, 
            result.font, result.uiCamera, result.screenWidth, result.screenHeight);
        result.helpUI = new HelpUI(result.shapeRenderer, result.batch, result.font, 
            result.uiCamera, result.screenWidth, result.screenHeight);
        
        // UIコンポーネントにSoundManagerを設定
        result.inventoryUI.setSoundManager(result.soundManager);
        result.encyclopediaUI.setSoundManager(result.soundManager);
        result.helpUI.setSoundManager(result.soundManager);
        
        // InventoryUIにクラフトシステムとItemDataLoaderを設定
        result.inventoryUI.setCraftingSystem(result.craftingSystem);
        result.inventoryUI.setItemDataLoader(result.itemManager.getItemDataLoader());
    }
    
    /**
     * 入力ハンドラーを初期化します。
     */
    private static void initializeInputHandlers(InitializationResult result) {
        result.saveGameManager = new SaveGameManager();
        result.textInputHandler = new TextInputHandler();
        result.inputHandler = new InputHandler(result.player, result.farmManager, 
            result.livestockManager);
        result.inputHandler.setTerrainManager(result.terrainManager);
        result.inputHandler.setTerrainConversionManager(result.terrainConversionManager);
        result.inputHandler.setBuildingManager(result.buildingManager);
        
        // MenuSystemのコールバック実装
        MenuSystem.MenuCallbacks menuCallbacks = new MenuSystem.MenuCallbacks() {
            @Override
            public void onSaveGame(String saveName) {
                // このコールバックはMainクラスで実装される
            }
            
            @Override
            public void onLoadGame(String saveName) {
                // このコールバックはMainクラスで実装される
            }
            
            @Override
            public void onToggleGrid() {
                // このコールバックはMainクラスで実装される
            }
            
            @Override
            public void onQuit() {
                Gdx.app.exit();
            }
            
            @Override
            public void onReturnToTitle() {
                // このコールバックはMainクラスで実装される
            }
            
            @Override
            public boolean isGridVisible() {
                return true; // デフォルト値、Mainクラスで上書きされる
            }
            
            @Override
            public float getCameraZoom() {
                return 1.0f; // デフォルト値、Mainクラスで上書きされる
            }
            
            @Override
            public void setPaused(boolean paused) {
                // このコールバックはMainクラスで実装される
            }
        };
        
        result.menuSystem = new MenuSystem(result.uiRenderer, result.saveGameManager, 
            result.soundSettings, result.soundManager, result.textInputHandler,
            result.shapeRenderer, result.batch, result.font, result.uiCamera, 
            result.screenWidth, result.screenHeight, menuCallbacks, result.helpUI);
        
        // MenuSystemにLivestockDataLoaderを設定
        result.menuSystem.setLivestockDataLoader(result.livestockManager.getLivestockDataLoader());
    }
}
