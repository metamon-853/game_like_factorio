package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * UI描画のコンテキストを管理するクラス。
 * 
 * <p>このクラスは、UI描画に必要なリソース（SpriteBatch、ShapeRenderer、Font、Camera）を
 * 一元管理し、batch.begin()/end()の責任を明確化します。</p>
 * 
 * <p>使用例：</p>
 * <pre>
 * RenderContext context = new RenderContext(batch, shapeRenderer, font, uiCamera);
 * context.beginBatch();
 * try {
 *     // 描画処理
 * } finally {
 *     context.endBatch();
 * }
 * </pre>
 * 
 * @author game_like_factorio
 * @version 1.0.0
 */
public class RenderContext {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    
    private boolean batchStartedByThisContext;
    private boolean shapeRendererStartedByThisContext;
    private ShapeRenderer.ShapeType currentShapeType;
    
    /**
     * RenderContextを初期化します。
     * 
     * @param batch SpriteBatch
     * @param shapeRenderer ShapeRenderer
     * @param font BitmapFont
     * @param uiCamera UI用カメラ
     */
    public RenderContext(SpriteBatch batch, ShapeRenderer shapeRenderer, 
                        BitmapFont font, OrthographicCamera uiCamera) {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.font = font;
        this.uiCamera = uiCamera;
        this.batchStartedByThisContext = false;
        this.shapeRendererStartedByThisContext = false;
    }
    
    /**
     * SpriteBatchを開始します。
     * 既に開始されている場合は何もしません。
     * 
     * @return このコンテキストで開始した場合true、既に開始されていた場合false
     */
    public boolean beginBatch() {
        if (batch == null) {
            return false;
        }
        
        if (!batch.isDrawing()) {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            batchStartedByThisContext = true;
            return true;
        }
        batchStartedByThisContext = false;
        return false;
    }
    
    /**
     * SpriteBatchを終了します。
     * このコンテキストで開始した場合のみ終了します。
     */
    public void endBatch() {
        if (batch != null && batch.isDrawing() && batchStartedByThisContext) {
            batch.end();
            batchStartedByThisContext = false;
        }
    }
    
    /**
     * ShapeRendererを開始します。
     * 既に開始されている場合は終了してから開始します。
     * 
     * @param shapeType シェイプタイプ
     * @return このコンテキストで開始した場合true、既に開始されていた場合false
     */
    public boolean beginShapeRenderer(ShapeRenderer.ShapeType shapeType) {
        if (shapeRenderer == null) {
            return false;
        }
        
        // batchが開始されている場合は終了
        if (batch != null && batch.isDrawing()) {
            endBatch();
        }
        
        // ShapeRendererが既に開始されている場合は終了
        if (shapeRenderer.isDrawing()) {
            shapeRenderer.end();
            shapeRendererStartedByThisContext = false;
        }
        
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(shapeType);
        shapeRendererStartedByThisContext = true;
        currentShapeType = shapeType;
        return true;
    }
    
    /**
     * ShapeRendererを終了します。
     * このコンテキストで開始した場合のみ終了します。
     */
    public void endShapeRenderer() {
        if (shapeRenderer != null && shapeRenderer.isDrawing() && shapeRendererStartedByThisContext) {
            shapeRenderer.end();
            shapeRendererStartedByThisContext = false;
        }
    }
    
    /**
     * SpriteBatchを取得します。
     * @return SpriteBatch
     */
    public SpriteBatch getBatch() {
        return batch;
    }
    
    /**
     * ShapeRendererを取得します。
     * @return ShapeRenderer
     */
    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
    
    /**
     * BitmapFontを取得します。
     * @return BitmapFont
     */
    public BitmapFont getFont() {
        return font;
    }
    
    /**
     * UI用カメラを取得します。
     * @return UI用カメラ
     */
    public OrthographicCamera getUiCamera() {
        return uiCamera;
    }
    
    /**
     * すべてのリソースをクリーンアップします。
     * このコンテキストで開始したリソースをすべて終了します。
     */
    public void cleanup() {
        endBatch();
        endShapeRenderer();
    }
}
