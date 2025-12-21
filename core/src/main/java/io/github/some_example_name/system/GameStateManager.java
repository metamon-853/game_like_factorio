package io.github.some_example_name.system;

/**
 * ゲームの状態を管理するクラス。
 * 
 * <p>UIの状態遷移を一元管理し、状態の整合性を保証します。
 * このクラスを使用することで、複数のbooleanフラグによる状態管理を避け、
 * より明確で保守しやすいコードを実現できます。</p>
 * 
 * <p>使用例：</p>
 * <pre>
 * GameStateManager stateManager = new GameStateManager();
 * stateManager.setState(GameState.INVENTORY_OPEN);
 * if (stateManager.isInventoryOpen()) {
 *     // インベントリを描画
 * }
 * </pre>
 * 
 * @author game_like_factorio
 * @version 1.0.0
 */
public class GameStateManager {
    private GameState currentState;
    private GameState previousState;
    
    /**
     * GameStateManagerを初期化します。
     * デフォルト状態はPLAYINGです。
     */
    public GameStateManager() {
        this.currentState = GameState.PLAYING;
        this.previousState = null;
    }
    
    /**
     * 現在のゲーム状態を取得します。
     * @return 現在のゲーム状態
     */
    public GameState getCurrentState() {
        return currentState;
    }
    
    /**
     * 前のゲーム状態を取得します。
     * @return 前のゲーム状態（存在しない場合はnull）
     */
    public GameState getPreviousState() {
        return previousState;
    }
    
    /**
     * ゲーム状態を変更します。
     * @param newState 新しいゲーム状態
     */
    public void setState(GameState newState) {
        if (newState == null) {
            return;
        }
        
        if (newState != currentState) {
            previousState = currentState;
            currentState = newState;
        }
    }
    
    /**
     * 前の状態に戻ります。
     * @return 状態が変更された場合true
     */
    public boolean returnToPreviousState() {
        if (previousState != null) {
            GameState temp = currentState;
            currentState = previousState;
            previousState = temp;
            return true;
        }
        return false;
    }
    
    /**
     * ゲームがポーズ中かどうかを判定します。
     * @return ポーズ中の場合true
     */
    public boolean isPaused() {
        return currentState == GameState.PAUSED || 
               currentState == GameState.HELP_MENU ||
               currentState == GameState.INVENTORY_OPEN ||
               currentState == GameState.ENCYCLOPEDIA_OPEN;
    }
    
    /**
     * ゲームがプレイ中かどうかを判定します。
     * @return プレイ中の場合true
     */
    public boolean isPlaying() {
        return currentState == GameState.PLAYING;
    }
    
    /**
     * インベントリが開いているかどうかを判定します。
     * @return インベントリが開いている場合true
     */
    public boolean isInventoryOpen() {
        return currentState == GameState.INVENTORY_OPEN || 
               currentState == GameState.ENCYCLOPEDIA_OPEN;
    }
    
    /**
     * アイテム図鑑が開いているかどうかを判定します。
     * @return アイテム図鑑が開いている場合true
     */
    public boolean isEncyclopediaOpen() {
        return currentState == GameState.ENCYCLOPEDIA_OPEN;
    }
    
    /**
     * ヘルプメニューが開いているかどうかを判定します。
     * @return ヘルプメニューが開いている場合true
     */
    public boolean isHelpMenuOpen() {
        return currentState == GameState.HELP_MENU;
    }
}
