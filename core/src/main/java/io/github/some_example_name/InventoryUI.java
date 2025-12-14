package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Map;

/**
 * インベントリUIを描画するクラス。
 */
public class InventoryUI {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    private int screenWidth;
    private int screenHeight;
    
    // UIのサイズと位置
    private float panelWidth = 600;
    private float panelHeight = 500;
    private float panelX;
    private float panelY;
    
    // アイテムスロットの設定
    private static final int SLOTS_PER_ROW = 6;
    private static final int MAX_ROWS = 8;
    private static final float SLOT_SIZE = 70;
    private static final float SLOT_PADDING = 10;
    
    public InventoryUI(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                     OrthographicCamera uiCamera, int screenWidth, int screenHeight) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.uiCamera = uiCamera;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        updatePanelPosition();
    }
    
    /**
     * 画面サイズを更新します。
     */
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        updatePanelPosition();
    }
    
    /**
     * パネルの位置を更新します（画面中央に配置）。
     */
    private void updatePanelPosition() {
        panelX = (screenWidth - panelWidth) / 2;
        panelY = (screenHeight - panelHeight) / 2;
    }
    
    /**
     * インベントリUIを描画します。
     * @param inventory インベントリ
     * @param itemDataLoader アイテムデータローダー
     */
    public void render(Inventory inventory, ItemDataLoader itemDataLoader) {
        if (inventory == null) {
            return;
        }
        
        // パネルの背景を描画（batchは既に終了している前提）
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        shapeRenderer.end();
        
        // パネルの枠線を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        shapeRenderer.end();
        
        // batchを開始
        batch.begin();
        batch.setProjectionMatrix(uiCamera.combined);
        
        font.getData().setScale(2.0f);
        font.setColor(Color.WHITE);
        
        // タイトルを描画
        String title = "Inventory";
        GlyphLayout titleLayout = new GlyphLayout(font, title);
        float titleX = panelX + (panelWidth - titleLayout.width) / 2;
        float titleY = panelY + panelHeight - 30;
        font.draw(batch, title, titleX, titleY);
        
        // アイテムリストを描画
        float startX = panelX + 20;
        float startY = titleY - 50;
        float currentY = startY;
        
        Map<String, Integer> items = inventory.getAllItems();
        int itemIndex = 0;
        
        if (items.isEmpty()) {
            // 空のインベントリメッセージを表示
            font.getData().setScale(2.0f);
            font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
            String emptyText = "Inventory is empty";
            GlyphLayout emptyLayout = new GlyphLayout(font, emptyText);
            float emptyX = panelX + (panelWidth - emptyLayout.width) / 2;
            float emptyY = panelY + panelHeight / 2;
            font.draw(batch, emptyText, emptyX, emptyY);
            font.setColor(Color.WHITE);
        } else {
            for (Map.Entry<String, Integer> entry : items.entrySet()) {
                if (itemIndex >= SLOTS_PER_ROW * MAX_ROWS) {
                    break; // 最大表示数を超えたら終了
                }
                
                String itemId = entry.getKey();
                int count = entry.getValue();
                
                ItemData itemData = itemDataLoader.getItemData(itemId);
                if (itemData == null) {
                    continue;
                }
                
                // スロットの位置を計算
                int row = itemIndex / SLOTS_PER_ROW;
                int col = itemIndex % SLOTS_PER_ROW;
                float slotX = startX + col * (SLOT_SIZE + SLOT_PADDING);
                float slotY = currentY - row * (SLOT_SIZE + SLOT_PADDING);
                
                // スロットの背景を描画
                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
                shapeRenderer.rect(slotX, slotY - SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
                shapeRenderer.end();
                
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(0.5f, 0.5f, 0.7f, 1f);
                shapeRenderer.rect(slotX, slotY - SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
                shapeRenderer.end();
                
                // アイテムの色で円を描画（簡易的なアイコン）
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                Color itemColor = itemData.getColor();
                shapeRenderer.setColor(itemColor);
                float iconSize = SLOT_SIZE * 0.6f;
                float iconX = slotX + (SLOT_SIZE - iconSize) / 2;
                float iconY = slotY - SLOT_SIZE + (SLOT_SIZE - iconSize) / 2;
                shapeRenderer.circle(iconX + iconSize / 2, iconY + iconSize / 2, iconSize / 2);
                shapeRenderer.end();
                
                batch.begin();
                batch.setProjectionMatrix(uiCamera.combined);
                
                // アイテム名と数量を描画
                font.getData().setScale(1.2f);
                String displayName = itemData.name.length() > 8 ? itemData.name.substring(0, 8) : itemData.name;
                GlyphLayout nameLayout = new GlyphLayout(font, displayName);
                float nameX = slotX + (SLOT_SIZE - nameLayout.width) / 2;
                font.draw(batch, displayName, nameX, slotY - SLOT_SIZE + 15);
                
                // 数量を描画
                if (count > 1) {
                    String countText = "x" + count;
                    GlyphLayout countLayout = new GlyphLayout(font, countText);
                    float countX = slotX + SLOT_SIZE - countLayout.width - 5;
                    font.setColor(Color.YELLOW);
                    font.draw(batch, countText, countX, slotY - SLOT_SIZE + 5);
                    font.setColor(Color.WHITE);
                }
                
                itemIndex++;
            }
        }
        
        // 閉じるヒントを描画
        font.getData().setScale(1.5f);
        font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        String hint = "Press TAB to close";
        GlyphLayout hintLayout = new GlyphLayout(font, hint);
        float hintX = panelX + (panelWidth - hintLayout.width) / 2;
        font.draw(batch, hint, hintX, panelY + 20);
        font.setColor(Color.WHITE);
        
        font.getData().setScale(2.0f);
    }
}
