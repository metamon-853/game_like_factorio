package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;

import io.github.some_example_name.entity.Player;
import io.github.some_example_name.game.CivilizationLevel;
import io.github.some_example_name.game.PreservedFoodManager;
import io.github.some_example_name.manager.BuildingManager;
import io.github.some_example_name.manager.FarmManager;
import io.github.some_example_name.manager.ItemManager;
import io.github.some_example_name.manager.LivestockManager;
import io.github.some_example_name.manager.TerrainManager;

/**
 * ゲームのロジック更新処理を担当するクラス。
 * 
 * <p>このクラスは以下の処理を統合管理します：</p>
 * <ul>
 *   <li>プレイヤーの更新</li>
 *   <li>マネージャーの更新（地形、アイテム、農地、畜産）</li>
 *   <li>文明レベルの進行チェック</li>
 *   <li>カメラの更新</li>
 * </ul>
 * 
 * @author game_like_factorio
 * @version 1.0.0
 */
public class GameController {
    private Player player;
    private TerrainManager terrainManager;
    private ItemManager itemManager;
    private FarmManager farmManager;
    private LivestockManager livestockManager;
    private BuildingManager buildingManager;
    private PreservedFoodManager preservedFoodManager;
    private OrthographicCamera camera;
    private EndingScreen endingScreen;
    
    // 文明レベルアップメッセージ関連
    private String civilizationLevelUpMessage;
    private float civilizationLevelUpMessageTimer;
    private static final float CIVILIZATION_MESSAGE_DURATION = 3.0f;
    
    /**
     * GameControllerを初期化します。
     */
    public GameController() {
        this.civilizationLevelUpMessage = null;
        this.civilizationLevelUpMessageTimer = 0f;
        this.endingScreen = new EndingScreen();
    }
    
    /**
     * ゲームオブジェクトを設定します。
     */
    public void setGameObjects(Player player, TerrainManager terrainManager,
                              ItemManager itemManager, FarmManager farmManager,
                              LivestockManager livestockManager, BuildingManager buildingManager,
                              PreservedFoodManager preservedFoodManager,
                              OrthographicCamera camera) {
        this.player = player;
        this.terrainManager = terrainManager;
        this.itemManager = itemManager;
        this.farmManager = farmManager;
        this.livestockManager = livestockManager;
        this.buildingManager = buildingManager;
        this.preservedFoodManager = preservedFoodManager;
        this.camera = camera;
    }
    
    /**
     * ゲームの更新処理を行います。
     * 
     * @param deltaTime 前フレームからの経過時間（秒）
     */
    public void update(float deltaTime) {
        try {
            // プレイヤーを更新
            if (player != null) {
                player.update(deltaTime);
            }
            
            // 地形マネージャーを更新（カメラの視野範囲を渡す）
            if (terrainManager != null && camera != null) {
                // プレイヤーのマップ升座標を取得
                int playerTileX = player.getTileX();
                int playerTileY = player.getTileY();
                terrainManager.update(camera, playerTileX, playerTileY, deltaTime);
            }
            
            // アイテムマネージャーを更新（カメラの視野範囲を渡す）
            if (itemManager != null && player != null && camera != null) {
                itemManager.update(deltaTime, player, camera);
            }
            
            // 農地マネージャーを更新
            if (farmManager != null) {
                farmManager.update(deltaTime);
            }
            
            // 畜産マネージャーを更新
            if (livestockManager != null) {
                livestockManager.update(deltaTime);
            }
            
            // エンディング画面を更新
            if (endingScreen != null) {
                endingScreen.update(deltaTime);
            }
            
            // エンディング中は他の更新をスキップ
            if (endingScreen != null && endingScreen.isActive()) {
                return;
            }
            
            // 文明レベル進行チェック
            checkCivilizationLevelProgress();
            
            // 文明レベルアップメッセージのタイマーを更新
            if (civilizationLevelUpMessage != null) {
                civilizationLevelUpMessageTimer += deltaTime;
                if (civilizationLevelUpMessageTimer >= CIVILIZATION_MESSAGE_DURATION) {
                    civilizationLevelUpMessage = null;
                    civilizationLevelUpMessageTimer = 0f;
                }
            }
            
            // カメラをプレイヤーの位置に追従させる
            updateCamera();
        } catch (Exception e) {
            Gdx.app.error("GameController", "Error in game update: " + e.getMessage(), e);
        }
    }
    
    /**
     * カメラをプレイヤーの位置に追従させます。
     */
    private void updateCamera() {
        if (player != null && camera != null) {
            float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
            float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
            camera.position.set(playerCenterX, playerCenterY, 0);
        }
    }
    
    /**
     * 文明レベルの進行をチェックします。
     */
    private void checkCivilizationLevelProgress() {
        if (itemManager == null || preservedFoodManager == null) {
            return;
        }
        
        try {
            CivilizationLevel civLevel = itemManager.getCivilizationLevel();
            if (civLevel == null) {
                return;
            }
            
            int currentLevel = civLevel.getLevel();
            int totalLivestockProducts = livestockManager != null ? 
                livestockManager.getTotalLivestockProductsProduced() : 0;
            
            // レベル1からレベル2への進行条件：保存食の条件を満たす
            if (currentLevel == 1) {
                if (civLevel.canProgressToLevel(2, preservedFoodManager, totalLivestockProducts)) {
                    if (civLevel.levelUp()) {
                        Gdx.app.log("Civilization", "Civilization level increased to " + 
                            civLevel.getLevel() + " (" + civLevel.getLevelName() + ")!");
                        showCivilizationLevelUpMessage(civLevel.getLevelName());
                    }
                }
            }
            // レベル2からレベル3への進行条件：畜産物を累計20生産
            else if (currentLevel == 2) {
                if (civLevel.canProgressToLevel(3, preservedFoodManager, totalLivestockProducts)) {
                    if (civLevel.levelUp()) {
                        Gdx.app.log("Civilization", "Civilization level increased to " + 
                            civLevel.getLevel() + " (" + civLevel.getLevelName() + ")!");
                        showCivilizationLevelUpMessage(civLevel.getLevelName());
                    }
                }
            }
            // レベル3からレベル4への進行条件：畜産物を累計100生産
            else if (currentLevel == 3) {
                if (civLevel.canProgressToLevel(4, preservedFoodManager, totalLivestockProducts)) {
                    if (civLevel.levelUp()) {
                        Gdx.app.log("Civilization", "Civilization level increased to " + 
                            civLevel.getLevel() + " (" + civLevel.getLevelName() + ")!");
                        showCivilizationLevelUpMessage(civLevel.getLevelName());
                    }
                }
            }
            // レベル4からレベル5への進行条件：神殿を1つ以上建設
            else if (currentLevel == 4) {
                int templeCount = buildingManager != null ? buildingManager.getTempleCount() : 0;
                if (civLevel.canProgressToLevel(5, preservedFoodManager, totalLivestockProducts, templeCount)) {
                    if (civLevel.levelUp()) {
                        Gdx.app.log("Civilization", "Civilization level increased to " + 
                            civLevel.getLevel() + " (" + civLevel.getLevelName() + ")!");
                        showCivilizationLevelUpMessage(civLevel.getLevelName());
                        
                        // エンディングを開始
                        if (endingScreen != null) {
                            endingScreen.start();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("GameController", "Error checking civilization level progress: " + 
                e.getMessage(), e);
        }
    }
    
    /**
     * 文明レベルアップ時のメッセージを設定します。
     */
    private void showCivilizationLevelUpMessage(String levelName) {
        // 文明レベルに応じたメッセージを設定
        switch (levelName) {
            case "新石器時代":
                civilizationLevelUpMessage = "余剰食料が生まれ、集落は拡大した。";
                break;
            case "青銅器時代":
                civilizationLevelUpMessage = "畜産が定着し、人々は定住を始めた。";
                break;
            case "鉄器時代":
                civilizationLevelUpMessage = "生産と保存が成立し、文明は次の段階へ進んだ。";
                break;
            case "古代文明時代":
                civilizationLevelUpMessage = "高度な技術が確立し、文明は成熟した。";
                break;
            default:
                civilizationLevelUpMessage = "文明が進歩した。";
                break;
        }
        civilizationLevelUpMessageTimer = 0f;
    }
    
    /**
     * 文明レベルアップメッセージを取得します。
     * @return メッセージ（表示する必要がない場合はnull）
     */
    public String getCivilizationLevelUpMessage() {
        return civilizationLevelUpMessage;
    }
    
    /**
     * エンディング画面を取得します。
     * @return エンディング画面
     */
    public EndingScreen getEndingScreen() {
        return endingScreen;
    }
}
