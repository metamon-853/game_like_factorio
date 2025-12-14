package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * テキスト入力を処理するクラス。
 */
public class TextInputHandler {
    private StringBuilder inputText = new StringBuilder();
    private boolean isTextInputActive = false;
    private String currentInputLabel = "";
    private int maxInputLength = 30;
    
    /**
     * テキスト入力を処理します。
     * @return Enterキーが押された場合true
     */
    public boolean handleInput() {
        if (!isTextInputActive) {
            return false;
        }
        
        // Enterキーで確定
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            return true;
        }
        
        // Backspaceキーで文字削除
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (inputText.length() > 0) {
                inputText.setLength(inputText.length() - 1);
            }
            return false;
        }
        
        // Shiftキーの状態を確認
        boolean isShiftPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || 
                                 Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        
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
            return false;
        }
        
        // 数字キー
        for (int i = Input.Keys.NUM_0; i <= Input.Keys.NUM_9; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                char c = (char)('0' + (i - Input.Keys.NUM_0));
                if (inputText.length() < maxInputLength) {
                    inputText.append(c);
                }
                return false;
            }
        }
        
        // スペースキー
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (inputText.length() < maxInputLength) {
                inputText.append(' ');
            }
            return false;
        }
        
        // アンダースコアとハイフン（Shift + ハイフンでアンダースコア）
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            char c = isShiftPressed ? '_' : '-';
            if (inputText.length() < maxInputLength) {
                inputText.append(c);
            }
            return false;
        }
        
        return false;
    }
    
    /**
     * 入力テキストを取得します。
     * @return 入力テキスト
     */
    public String getInputText() {
        return inputText.toString();
    }
    
    /**
     * 入力テキストをクリアします。
     */
    public void clearInput() {
        inputText.setLength(0);
    }
    
    /**
     * テキスト入力がアクティブかどうかを取得します。
     * @return アクティブな場合true
     */
    public boolean isTextInputActive() {
        return isTextInputActive;
    }
    
    /**
     * テキスト入力をアクティブ/非アクティブにします。
     * @param active アクティブにする場合true
     */
    public void setTextInputActive(boolean active) {
        this.isTextInputActive = active;
        if (!active) {
            clearInput();
            currentInputLabel = "";
        }
    }
    
    /**
     * 現在の入力ラベルを取得します。
     * @return 入力ラベル
     */
    public String getCurrentInputLabel() {
        return currentInputLabel;
    }
    
    /**
     * 現在の入力ラベルを設定します。
     * @param label 入力ラベル
     */
    public void setCurrentInputLabel(String label) {
        this.currentInputLabel = label;
    }
    
    /**
     * 最大入力長を取得します。
     * @return 最大入力長
     */
    public int getMaxInputLength() {
        return maxInputLength;
    }
}
