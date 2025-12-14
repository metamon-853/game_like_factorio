package io.github.some_example_name;

/**
 * UIボタンを表すクラス。
 */
public class Button {
    public float x, y, width, height;
    
    public Button(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * 指定された座標がボタン内にあるかどうかを判定します。
     * @param screenX 画面X座標
     * @param screenY 画面Y座標
     * @return ボタン内にある場合true
     */
    public boolean contains(float screenX, float screenY) {
        return screenX >= x && screenX <= x + width && screenY >= y && screenY <= y + height;
    }
}
