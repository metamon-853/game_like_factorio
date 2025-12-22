package io.github.some_example_name.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * UIスクロールバーを表すクラス（描画、ドラッグ処理を含む）。
 */
public class UIScrollBar {
    // スクロール位置
    private float scrollOffset = 0;
    private float maxScrollOffset = 0;
    private float totalContentHeight = 0;
    
    // スクロールバーの位置とサイズ
    private float scrollBarX;
    private float scrollBarY;
    private float scrollBarWidth = 10f;
    private float scrollBarHeight;
    
    // コンテンツエリアの位置とサイズ
    private float contentAreaX;
    private float contentAreaY;
    private float contentAreaWidth;
    private float contentAreaHeight;
    
    // スクロールバーのドラッグ状態
    private boolean isDraggingScrollThumb = false;
    private float scrollThumbGrabOffsetY = 0f;
    
    // 描画用のリソース
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera uiCamera;
    private int screenHeight;
    
    // スクロール速度
    private float scrollSpeed = 30f;
    
    /**
     * UIScrollBarを作成します。
     * @param shapeRenderer ShapeRenderer
     * @param uiCamera OrthographicCamera
     * @param screenHeight 画面の高さ
     */
    public UIScrollBar(ShapeRenderer shapeRenderer, OrthographicCamera uiCamera, int screenHeight) {
        this.shapeRenderer = shapeRenderer;
        this.uiCamera = uiCamera;
        this.screenHeight = screenHeight;
    }
    
    /**
     * コンテンツエリアを設定します。
     * @param contentAreaX コンテンツエリアのX座標
     * @param contentAreaY コンテンツエリアのY座標
     * @param contentAreaWidth コンテンツエリアの幅
     * @param contentAreaHeight コンテンツエリアの高さ
     */
    public void setContentArea(float contentAreaX, float contentAreaY, 
                              float contentAreaWidth, float contentAreaHeight) {
        this.contentAreaX = contentAreaX;
        this.contentAreaY = contentAreaY;
        this.contentAreaWidth = contentAreaWidth;
        this.contentAreaHeight = contentAreaHeight;
        
        // スクロールバーの位置を計算
        this.scrollBarX = contentAreaX + contentAreaWidth - scrollBarWidth - 8f;
        this.scrollBarY = contentAreaY;
        this.scrollBarHeight = contentAreaHeight;
    }
    
    /**
     * コンテンツの高さを設定します。
     * @param totalContentHeight コンテンツの合計高さ
     */
    public void setTotalContentHeight(float totalContentHeight) {
        this.totalContentHeight = totalContentHeight;
        // 最大スクロールオフセットを計算
        this.maxScrollOffset = Math.max(0, totalContentHeight - contentAreaHeight + 40);
    }
    
    /**
     * スクロール位置をリセットします。
     */
    public void resetScroll() {
        scrollOffset = 0;
    }
    
    /**
     * スクロール位置を取得します。
     */
    public float getScrollOffset() {
        return scrollOffset;
    }
    
    /**
     * スクロール速度を設定します。
     */
    public void setScrollSpeed(float scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }
    
    /**
     * スクロール処理を行います。
     * @param amountY スクロール量（LibGDXでは amountY > 0 が上スクロール、amountY < 0 が下スクロール）
     */
    public void handleScroll(float amountY) {
        scrollOffset += amountY * scrollSpeed;
        scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));
    }
    
    /**
     * スクロールバー（つまみ）のドラッグ入力を処理します。
     */
    public void handleScrollBarDragInput() {
        if (maxScrollOffset <= 0) {
            isDraggingScrollThumb = false;
            return;
        }

        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY(); // UI座標（下が0）

        ScrollBarMetrics m = computeScrollBarMetrics();

        // 押した瞬間に、つまみを掴んだか判定
        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            if (mouseX >= m.thumbX && mouseX <= m.thumbX + m.thumbWidth &&
                mouseY >= m.thumbY && mouseY <= m.thumbY + m.thumbHeight) {
                isDraggingScrollThumb = true;
                scrollThumbGrabOffsetY = mouseY - m.thumbY;
            } else {
                isDraggingScrollThumb = false;
            }
        }

        // ドラッグ中：つまみ位置からscrollOffsetへ反映
        if (isDraggingScrollThumb) {
            if (!Gdx.input.isButtonPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
                isDraggingScrollThumb = false;
                return;
            }

            float trackYMin = m.scrollBarY;
            float trackYMax = m.scrollBarY + m.scrollBarHeight - m.thumbHeight;
            float newThumbY = mouseY - scrollThumbGrabOffsetY;
            newThumbY = Math.max(trackYMin, Math.min(trackYMax, newThumbY));

            float trackRange = Math.max(1f, (m.scrollBarHeight - m.thumbHeight));
            float scrollRatio = 1f - ((newThumbY - m.scrollBarY) / trackRange);
            scrollOffset = scrollRatio * maxScrollOffset;
            scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));
        }
    }
    
    /**
     * スクロールバーのメトリクスを計算します。
     */
    private static class ScrollBarMetrics {
        float scrollBarX;
        float scrollBarY;
        float scrollBarWidth;
        float scrollBarHeight;
        float thumbX;
        float thumbY;
        float thumbWidth;
        float thumbHeight;
    }
    
    private ScrollBarMetrics computeScrollBarMetrics() {
        ScrollBarMetrics m = new ScrollBarMetrics();

        m.scrollBarWidth = scrollBarWidth;
        m.scrollBarX = scrollBarX;
        m.scrollBarHeight = scrollBarHeight;
        m.scrollBarY = scrollBarY;

        float safeTotalContentHeight = Math.max(1f, totalContentHeight);
        m.thumbHeight = Math.max(30f, scrollBarHeight * (scrollBarHeight / safeTotalContentHeight));

        float scrollRatio = maxScrollOffset > 0 ? scrollOffset / maxScrollOffset : 0f;
        m.thumbY = m.scrollBarY + (m.scrollBarHeight - m.thumbHeight) * (1.0f - scrollRatio);

        m.thumbX = m.scrollBarX + 1f;
        m.thumbWidth = m.scrollBarWidth - 2f;

        return m;
    }
    
    /**
     * スクロールバーを描画します。
     */
    public void render() {
        if (maxScrollOffset <= 0) return;
        
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        ScrollBarMetrics m = computeScrollBarMetrics();
        
        // スクロールバーの背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 0.9f);
        shapeRenderer.rect(m.scrollBarX, m.scrollBarY, m.scrollBarWidth, m.scrollBarHeight);
        shapeRenderer.end();
        
        // スクロールバーのつまみ
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.5f, 0.5f, 0.7f, 0.95f);
        shapeRenderer.rect(m.thumbX, m.thumbY, m.thumbWidth, m.thumbHeight);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.7f, 0.7f, 0.9f, 1f);
        shapeRenderer.rect(m.thumbX, m.thumbY, m.thumbWidth, m.thumbHeight);
        shapeRenderer.end();
    }
}
