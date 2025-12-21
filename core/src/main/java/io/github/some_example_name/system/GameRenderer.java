package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.entity.Player;
import io.github.some_example_name.manager.FarmManager;
import io.github.some_example_name.manager.ItemManager;
import io.github.some_example_name.manager.LivestockManager;
import io.github.some_example_name.manager.TerrainManager;
import io.github.some_example_name.ui.InventoryUI;
import io.github.some_example_name.ui.ItemEncyclopediaUI;
import io.github.some_example_name.ui.MenuSystem;
import io.github.some_example_name.ui.UIRenderer;

/**
 * ゲームの描画処理を担当するクラス。
 * 
 * <p>このクラスは以下の描画処理を統合管理します：</p>
 * <ul>
 *   <li>地形の描画</li>
 *   <li>ゲームオブジェクトの描画（アイテム、プレイヤー、農地、畜産）</li>
 *   <li>UIの描画（インベントリ、メニュー、メッセージ）</li>
 *   <li>グリッドの描画</li>
 * </ul>
 * 
 * @author game_like_factorio
 * @version 1.0.0
 */
public class GameRenderer {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private Viewport viewport;
    
    private UIRenderer uiRenderer;
    private TerrainManager terrainManager;
    private ItemManager itemManager;
    private FarmManager farmManager;
    private LivestockManager livestockManager;
    private Player player;
    private InventoryUI inventoryUI;
    private ItemEncyclopediaUI encyclopediaUI;
    private MenuSystem menuSystem;
    
    private int screenWidth;
    private int screenHeight;
    private boolean showGrid;
    
    // 文明レベルアップメッセージ関連
    private String civilizationLevelUpMessage;
    private static final float CIVILIZATION_MESSAGE_FONT_SCALE = 1.0f;
    private static final float CIVILIZATION_MESSAGE_BG_ALPHA = 0.7f;
    private static final float CIVILIZATION_MESSAGE_PADDING = 20f;
    
    /**
     * GameRendererを初期化します。
     * 
     * @param shapeRenderer 図形描画用のShapeRenderer
     * @param batch スプライト描画用のSpriteBatch
     * @param font フォント
     * @param camera ゲームカメラ
     * @param uiCamera UI用カメラ
     * @param viewport ビューポート
     * @param screenWidth 画面幅
     * @param screenHeight 画面高さ
     */
    public GameRenderer(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                       OrthographicCamera camera, OrthographicCamera uiCamera, Viewport viewport,
                       int screenWidth, int screenHeight) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.camera = camera;
        this.uiCamera = uiCamera;
        this.viewport = viewport;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.showGrid = true;
    }
    
    /**
     * マネージャーとUIコンポーネントを設定します。
     */
    public void setManagers(TerrainManager terrainManager, ItemManager itemManager,
                           FarmManager farmManager, LivestockManager livestockManager,
                           Player player) {
        this.terrainManager = terrainManager;
        this.itemManager = itemManager;
        this.farmManager = farmManager;
        this.livestockManager = livestockManager;
        this.player = player;
    }
    
    /**
     * UIコンポーネントを設定します。
     */
    public void setUIComponents(UIRenderer uiRenderer, InventoryUI inventoryUI,
                               ItemEncyclopediaUI encyclopediaUI, MenuSystem menuSystem) {
        this.uiRenderer = uiRenderer;
        this.inventoryUI = inventoryUI;
        this.encyclopediaUI = encyclopediaUI;
        this.menuSystem = menuSystem;
    }
    
    /**
     * 画面サイズを更新します。
     */
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
    
    /**
     * グリッド表示フラグを設定します。
     */
    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }
    
    /**
     * 文明レベルアップメッセージを設定します。
     */
    public void setCivilizationLevelUpMessage(String message) {
        this.civilizationLevelUpMessage = message;
    }
    
    /**
     * ゲームの描画処理を行います。
     * 
     * @param isPaused ポーズ中かどうか
     * @param inventoryOpen インベントリが開いているかどうか
     * @param showEncyclopedia アイテム図鑑が開いているかどうか
     * @param inventory インベントリ（インベントリUI描画時に必要）
     */
    public void render(boolean isPaused, boolean inventoryOpen, boolean showEncyclopedia, 
                      io.github.some_example_name.game.Inventory inventory) {
        // ビューポートを適用
        if (viewport != null) {
            viewport.apply();
        }
        
        // 画面をクリア
        ScreenUtils.clear(0.2f, 0.25f, 0.3f, 1f);
        
        // カメラのプロジェクション行列を設定
        if (shapeRenderer != null && camera != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);
        }
        if (batch != null && camera != null) {
            batch.setProjectionMatrix(camera.combined);
        }
        
        // グリッドを描画
        renderGrid();
        
        // 地形を描画
        renderTerrain();
        
        // ゲームオブジェクトを描画
        renderGameObjects();
        
        // UI情報を描画
        renderUI();
        
        // インベントリUIまたはアイテム図鑑UIを描画
        renderInventoryUI(inventoryOpen, showEncyclopedia, inventory);
        
        // 文明レベルアップメッセージを描画
        renderCivilizationLevelUpMessage();
        
        // ポーズメニューを描画
        renderMenu(isPaused);
    }
    
    /**
     * グリッドを描画します。
     */
    private void renderGrid() {
        if (!showGrid || shapeRenderer == null || uiRenderer == null || camera == null) {
            return;
        }
        
        try {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            uiRenderer.drawGrid(camera);
            shapeRenderer.end();
        } catch (Exception e) {
            Gdx.app.error("GameRenderer", "Error drawing grid: " + e.getMessage(), e);
            if (shapeRenderer.isDrawing()) {
                shapeRenderer.end();
            }
        }
    }
    
    /**
     * 地形を描画します。
     */
    private void renderTerrain() {
        if (batch == null || terrainManager == null || camera == null) {
            return;
        }
        
        try {
            batch.begin();
            terrainManager.render(batch, camera);
            batch.end();
        } catch (Exception e) {
            Gdx.app.error("GameRenderer", "Error rendering terrain: " + e.getMessage(), e);
            if (batch.isDrawing()) {
                batch.end();
            }
        }
    }
    
    /**
     * ゲームオブジェクトを描画します。
     */
    private void renderGameObjects() {
        if (shapeRenderer == null) {
            return;
        }
        
        try {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            // 農地を描画
            if (farmManager != null) {
                farmManager.render(shapeRenderer);
            }
            
            // 畜産タイルを描画
            if (livestockManager != null) {
                livestockManager.render(shapeRenderer);
            }
            
            // アイテムを描画
            if (itemManager != null) {
                itemManager.render(shapeRenderer);
            }
            
            // プレイヤーを描画
            if (player != null) {
                player.render(shapeRenderer);
            }
            
            shapeRenderer.end();
        } catch (Exception e) {
            Gdx.app.error("GameRenderer", "Error rendering game objects: " + e.getMessage(), e);
            if (shapeRenderer.isDrawing()) {
                shapeRenderer.end();
            }
        }
    }
    
    /**
     * UI情報を描画します。
     */
    private void renderUI() {
        if (uiRenderer == null || itemManager == null) {
            return;
        }
        
        try {
            int totalLivestockProducts = livestockManager != null ? 
                livestockManager.getTotalLivestockProductsProduced() : 0;
            uiRenderer.drawUI(itemManager, totalLivestockProducts);
        } catch (Exception e) {
            Gdx.app.error("GameRenderer", "Error drawing UI: " + e.getMessage(), e);
        }
    }
    
    /**
     * インベントリUIまたはアイテム図鑑UIを描画します。
     * 
     * <p>注意：InventoryUIとItemEncyclopediaUIはbatchを自己完結的に管理します。
     * これらのクラスはbatchが既に開始されているかチェックし、
     * 必要に応じて開始/終了を行います。</p>
     */
    private void renderInventoryUI(boolean inventoryOpen, boolean showEncyclopedia, 
                                   io.github.some_example_name.game.Inventory inventory) {
        if (!inventoryOpen) {
            return;
        }
        
        if (showEncyclopedia) {
            // アイテム図鑑を表示
            if (encyclopediaUI != null && itemManager != null && 
                itemManager.getItemDataLoader() != null) {
                try {
                    // ItemEncyclopediaUIはbatchを自己完結的に管理する
                    encyclopediaUI.render(itemManager.getItemDataLoader());
                } catch (Exception e) {
                    Gdx.app.error("GameRenderer", "Error rendering encyclopedia: " + e.getMessage(), e);
                }
            }
        } else {
            // インベントリを表示
            if (inventoryUI != null && inventory != null && itemManager != null && 
                itemManager.getItemDataLoader() != null) {
                try {
                    // InventoryUIはbatchを自己完結的に管理する
                    inventoryUI.render(inventory, itemManager.getItemDataLoader());
                } catch (Exception e) {
                    Gdx.app.error("GameRenderer", "Error rendering inventory: " + e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * 文明レベルアップメッセージを描画します。
     */
    private void renderCivilizationLevelUpMessage() {
        if (civilizationLevelUpMessage == null || batch == null || 
            shapeRenderer == null || font == null) {
            return;
        }
        
        // フォント設定を保存
        float originalFontScale = font.getData().scaleX;
        Color originalFontColor = font.getColor().cpy();
        
        try {
            // テキストレイアウトを計算
            font.getData().setScale(CIVILIZATION_MESSAGE_FONT_SCALE);
            font.setColor(Color.WHITE);
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout = 
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, civilizationLevelUpMessage);
            float x = (screenWidth - layout.width) / 2;
            float y = screenHeight / 2;
            
            // 背景を描画（半透明の黒）
            shapeRenderer.setProjectionMatrix(uiCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0f, CIVILIZATION_MESSAGE_BG_ALPHA);
            shapeRenderer.rect(x - CIVILIZATION_MESSAGE_PADDING, 
                    y - layout.height - CIVILIZATION_MESSAGE_PADDING, 
                    layout.width + CIVILIZATION_MESSAGE_PADDING * 2, 
                    layout.height + CIVILIZATION_MESSAGE_PADDING * 2);
            shapeRenderer.end();
            
            // テキストを描画
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            font.draw(batch, civilizationLevelUpMessage, x, y);
            batch.end();
        } catch (Exception e) {
            Gdx.app.error("GameRenderer", "Error rendering civilization message: " + e.getMessage(), e);
            if (batch.isDrawing()) {
                batch.end();
            }
            if (shapeRenderer.isDrawing()) {
                shapeRenderer.end();
            }
        } finally {
            // フォント設定を復元
            font.getData().setScale(originalFontScale);
            font.setColor(originalFontColor);
        }
    }
    
    /**
     * ポーズメニューを描画します。
     */
    private void renderMenu(boolean isPaused) {
        if (isPaused && menuSystem != null) {
            try {
                menuSystem.render();
            } catch (Exception e) {
                Gdx.app.error("GameRenderer", "Error rendering menu: " + e.getMessage(), e);
            }
        }
    }
}
