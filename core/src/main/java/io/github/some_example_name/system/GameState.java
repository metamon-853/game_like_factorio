package io.github.some_example_name.system;

/**
 * ゲームの状態を表す列挙型。
 * 
 * <p>UIの状態管理を一元化するために使用します。
 * 各状態は排他的であり、同時に複数の状態になることはありません。</p>
 * 
 * @author game_like_factorio
 * @version 1.0.0
 */
public enum GameState {
    /** ゲームプレイ中 */
    PLAYING,
    /** ポーズ中（メインメニュー） */
    PAUSED,
    /** インベントリが開いている */
    INVENTORY_OPEN,
    /** アイテム図鑑が開いている */
    ENCYCLOPEDIA_OPEN,
    /** ヘルプメニューが開いている */
    HELP_MENU
}
