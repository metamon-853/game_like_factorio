package io.github.some_example_name.system;

import io.github.some_example_name.entity.Player;
import io.github.some_example_name.entity.TerrainTile;
import io.github.some_example_name.manager.FarmManager;
import io.github.some_example_name.manager.LivestockManager;
import io.github.some_example_name.manager.TerrainManager;
import io.github.some_example_name.manager.TerrainConversionManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * キーボード入力を処理するクラス。
 */
public class InputHandler {
    private Player player;
    private FarmManager farmManager;
    private LivestockManager livestockManager;
    private TerrainManager terrainManager;
    private TerrainConversionManager terrainConversionManager;
    
    public InputHandler(Player player, FarmManager farmManager, LivestockManager livestockManager) {
        this.player = player;
        this.farmManager = farmManager;
        this.livestockManager = livestockManager;
        this.terrainManager = null; // 後で設定される
        this.terrainConversionManager = null; // 後で設定される
    }
    
    /**
     * 地形マネージャーを設定します。
     */
    public void setTerrainManager(TerrainManager terrainManager) {
        this.terrainManager = terrainManager;
    }
    
    /**
     * 地形変換マネージャーを設定します。
     */
    public void setTerrainConversionManager(TerrainConversionManager terrainConversionManager) {
        this.terrainConversionManager = terrainConversionManager;
    }
    
    /**
     * キーボード入力を処理します。
     */
    public void handleInput() {
        // Fキーで種を植える/収穫する（移動中でも可能）
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            handleFarmAction();
            return; // 農業アクション時は移動しない
        }
        
        // Lキーで動物を配置/製品を収穫する（移動中でも可能）
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            handleLivestockAction();
            return; // 畜産アクション時は移動しない
        }
        
        // Kキーで家畜を殺して肉を取得する（移動中でも可能）
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            handleKillLivestockAction();
            return; // 家畜を殺すアクション時は移動しない
        }
        
        // Tキーで地形変換を行う（移動中でも可能）
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            handleTerrainConversionAction();
            return; // 地形変換アクション時は移動しない
        }
        
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
            if (canMoveTo(1, 1)) {
                player.move(1, 1);
            }
        } else if (up && left) {
            // 左上
            if (canMoveTo(-1, 1)) {
                player.move(-1, 1);
            }
        } else if (down && right) {
            // 右下
            if (canMoveTo(1, -1)) {
                player.move(1, -1);
            }
        } else if (down && left) {
            // 左下
            if (canMoveTo(-1, -1)) {
                player.move(-1, -1);
            }
        } else if (up) {
            // 上
            if (canMoveTo(0, 1)) {
                player.move(0, 1);
            }
        } else if (down) {
            // 下
            if (canMoveTo(0, -1)) {
                player.move(0, -1);
            }
        } else if (left) {
            // 左
            if (canMoveTo(-1, 0)) {
                player.move(-1, 0);
            }
        } else if (right) {
            // 右
            if (canMoveTo(1, 0)) {
                player.move(1, 0);
            }
        }
    }
    
    /**
     * 農業アクション（種を植える/収穫する）を処理します。
     */
    private void handleFarmAction() {
        // プレイヤーの中心座標を取得
        float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
        float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
        
        // プレイヤーの中心がどのマップ升内にあるかを計算
        int tileX = (int)Math.floor(playerCenterX / Player.MAP_TILE_SIZE);
        int tileY = (int)Math.floor(playerCenterY / Player.MAP_TILE_SIZE);
        
        // まず収穫可能かチェック
        if (farmManager.harvest(tileX, tileY)) {
            Gdx.app.log("Farm", "作物を収穫しました！");
            return;
        }
        
        // 収穫できない場合は種を植える
        if (farmManager.plantSeed(tileX, tileY)) {
            Gdx.app.log("Farm", "種を植えました！");
        } else {
            Gdx.app.log("Farm", "種がありません、または既に種が植えられています。");
        }
    }
    
    /**
     * 畜産アクション（動物を配置/製品を収穫する）を処理します。
     */
    private void handleLivestockAction() {
        // プレイヤーの中心座標を取得
        float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
        float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
        
        // プレイヤーの中心がどのマップ升内にあるかを計算
        int tileX = (int)Math.floor(playerCenterX / Player.MAP_TILE_SIZE);
        int tileY = (int)Math.floor(playerCenterY / Player.MAP_TILE_SIZE);
        
        // まず収穫可能かチェック
        if (livestockManager.harvest(tileX, tileY)) {
            Gdx.app.log("Livestock", "製品を収穫しました！");
            return;
        }
        
        // 収穫できない場合は動物を配置
        if (livestockManager.placeAnimal(tileX, tileY)) {
            Gdx.app.log("Livestock", "動物を配置しました！");
        } else {
            Gdx.app.log("Livestock", "餌がありません、または既に動物が配置されています。");
        }
    }
    
    /**
     * 家畜を殺すアクションを処理します。
     */
    private void handleKillLivestockAction() {
        // プレイヤーの中心座標を取得
        float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
        float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
        
        // プレイヤーの中心がどのマップ升内にあるかを計算
        int tileX = (int)Math.floor(playerCenterX / Player.MAP_TILE_SIZE);
        int tileY = (int)Math.floor(playerCenterY / Player.MAP_TILE_SIZE);
        
        // 家畜を殺して肉を取得
        if (livestockManager.killAnimal(tileX, tileY)) {
            Gdx.app.log("Livestock", "家畜を殺して肉を取得しました！");
        } else {
            Gdx.app.log("Livestock", "家畜がいません。");
        }
    }
    
    /**
     * 地形変換アクションを処理します。
     */
    private void handleTerrainConversionAction() {
        if (terrainConversionManager == null) {
            Gdx.app.log("TerrainConversion", "地形変換マネージャーが設定されていません");
            return;
        }
        
        // プレイヤーの中心座標を取得
        float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
        float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
        
        // プレイヤーの中心がどのマップ升内にあるかを計算
        int tileX = (int)Math.floor(playerCenterX / Player.MAP_TILE_SIZE);
        int tileY = (int)Math.floor(playerCenterY / Player.MAP_TILE_SIZE);
        
        // 使用可能な道具を検索
        int toolId = terrainConversionManager.findUsableTool(tileX, tileY);
        if (toolId == -1) {
            Gdx.app.log("TerrainConversion", "この地形で使用できる道具がありません");
            return;
        }
        
        // 地形変換を試みる
        if (terrainConversionManager.tryConvertTerrain(tileX, tileY, toolId)) {
            Gdx.app.log("TerrainConversion", "地形を変換しました！");
        } else {
            Gdx.app.log("TerrainConversion", "地形変換に失敗しました");
        }
    }
    
    /**
     * 指定された方向に移動できるかチェックします。
     * プレイヤーが4マップ升サイズなので、移動先の範囲内に水タイルがないかチェックします。
     * @param dx X方向の移動量（プレイヤー升単位）
     * @param dy Y方向の移動量（プレイヤー升単位）
     * @return 移動可能な場合true
     */
    private boolean canMoveTo(int dx, int dy) {
        if (terrainManager == null) {
            return true; // 地形マネージャーが設定されていない場合は移動可能
        }
        
        // 現在のプレイヤー升座標
        int currentPlayerTileX = player.getPlayerTileX();
        int currentPlayerTileY = player.getPlayerTileY();
        
        // 移動先のプレイヤー升座標
        int newPlayerTileX = currentPlayerTileX + dx;
        int newPlayerTileY = currentPlayerTileY + dy;
        
        // プレイヤーの中心座標（移動後）
        float playerCenterX = newPlayerTileX * Player.PLAYER_TILE_SIZE + Player.PLAYER_TILE_SIZE / 2;
        float playerCenterY = newPlayerTileY * Player.PLAYER_TILE_SIZE + Player.PLAYER_TILE_SIZE / 2;
        
        // プレイヤーのサイズは4マップ升
        float playerSize = Player.TILE_SIZE * 4.0f;
        float playerLeft = playerCenterX - playerSize / 2;
        float playerRight = playerCenterX + playerSize / 2;
        float playerBottom = playerCenterY - playerSize / 2;
        float playerTop = playerCenterY + playerSize / 2;
        
        // プレイヤーの範囲内のマップ升をチェック
        int startTileX = (int)Math.floor(playerLeft / Player.TILE_SIZE);
        int endTileX = (int)Math.ceil(playerRight / Player.TILE_SIZE);
        int startTileY = (int)Math.floor(playerBottom / Player.TILE_SIZE);
        int endTileY = (int)Math.ceil(playerTop / Player.TILE_SIZE);
        
        // 範囲内のすべてのマップ升をチェック
        for (int tileX = startTileX; tileX <= endTileX; tileX++) {
            for (int tileY = startTileY; tileY <= endTileY; tileY++) {
                TerrainTile tile = terrainManager.getTerrainTile(tileX, tileY);
                if (tile != null && tile.getTerrainType() == TerrainTile.TerrainType.WATER) {
                    // 水タイルがある場合は移動不可
                    return false;
                }
            }
        }
        
        return true; // 水タイルがなければ移動可能
    }
}
