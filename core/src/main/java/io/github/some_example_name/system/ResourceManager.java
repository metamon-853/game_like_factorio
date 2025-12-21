package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

import io.github.some_example_name.ui.FontManager;

import java.util.ArrayList;
import java.util.List;

/**
 * リソースの統一管理を行うクラス。
 * 
 * <p>このクラスは、ゲームで使用するすべてのリソース（グラフィックス、フォントなど）を
 * 一元管理し、適切な順序で初期化・解放を行います。</p>
 * 
 * <p>使用例：</p>
 * <pre>
 * ResourceManager resourceManager = new ResourceManager();
 * resourceManager.initialize();
 * // リソースを使用
 * resourceManager.dispose();
 * </pre>
 * 
 * @author game_like_factorio
 * @version 1.0.0
 */
public class ResourceManager implements Disposable {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private FontManager fontManager;
    private BitmapFont font;
    
    private List<Disposable> managedResources;
    private boolean initialized;
    
    /**
     * ResourceManagerを初期化します。
     */
    public ResourceManager() {
        this.managedResources = new ArrayList<>();
        this.initialized = false;
    }
    
    /**
     * リソースを初期化します。
     * 
     * @return 初期化に成功した場合true
     */
    public boolean initialize() {
        if (initialized) {
            Gdx.app.log("ResourceManager", "Already initialized");
            return true;
        }
        
        try {
            // グラフィックスリソースを作成
            shapeRenderer = new ShapeRenderer();
            batch = new SpriteBatch();
            managedResources.add(shapeRenderer);
            managedResources.add(batch);
            
            // フォントマネージャーを初期化
            fontManager = new FontManager();
            fontManager.initialize();
            managedResources.add(fontManager);
            
            font = fontManager.getJapaneseFont();
            if (font == null) {
                Gdx.app.error("ResourceManager", "Failed to get Japanese font");
                return false;
            }
            
            initialized = true;
            Gdx.app.log("ResourceManager", "Resources initialized successfully");
            return true;
        } catch (Exception e) {
            Gdx.app.error("ResourceManager", "Error initializing resources: " + e.getMessage(), e);
            dispose();
            return false;
        }
    }
    
    /**
     * ShapeRendererを取得します。
     * @return ShapeRenderer（初期化されていない場合はnull）
     */
    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
    
    /**
     * SpriteBatchを取得します。
     * @return SpriteBatch（初期化されていない場合はnull）
     */
    public SpriteBatch getBatch() {
        return batch;
    }
    
    /**
     * BitmapFontを取得します。
     * @return BitmapFont（初期化されていない場合はnull）
     */
    public BitmapFont getFont() {
        return font;
    }
    
    /**
     * FontManagerを取得します。
     * @return FontManager（初期化されていない場合はnull）
     */
    public FontManager getFontManager() {
        return fontManager;
    }
    
    /**
     * リソースが初期化されているかどうかを取得します。
     * @return 初期化されている場合true
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 管理対象のリソースを追加します。
     * @param resource リソース
     */
    public void addManagedResource(Disposable resource) {
        if (resource != null && !managedResources.contains(resource)) {
            managedResources.add(resource);
        }
    }
    
    /**
     * 管理対象のリソースを削除します。
     * @param resource リソース
     */
    public void removeManagedResource(Disposable resource) {
        managedResources.remove(resource);
    }
    
    /**
     * すべてのリソースを解放します。
     */
    @Override
    public void dispose() {
        // リソースを逆順で解放（依存関係を考慮）
        for (int i = managedResources.size() - 1; i >= 0; i--) {
            Disposable resource = managedResources.get(i);
            if (resource != null) {
                try {
                    resource.dispose();
                } catch (Exception e) {
                    Gdx.app.error("ResourceManager", "Error disposing resource: " + e.getMessage(), e);
                }
            }
        }
        
        managedResources.clear();
        
        // 明示的にnullを設定
        shapeRenderer = null;
        batch = null;
        fontManager = null;
        font = null;
        
        initialized = false;
        Gdx.app.log("ResourceManager", "All resources disposed");
    }
}
