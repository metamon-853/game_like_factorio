package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;
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
    
    // アイテム詳細表示
    private ItemData selectedItemData = null;
    private int selectedItemCount = 0;
    
    // スロット情報を保持（クリック判定用）
    private List<SlotInfo> slotInfos;
    
    /**
     * スロット情報を保持する内部クラス
     */
    private static class SlotInfo {
        float x, y;
        String itemId;
        ItemData itemData;
        int count;
        
        SlotInfo(float x, float y, String itemId, ItemData itemData, int count) {
            this.x = x;
            this.y = y;
            this.itemId = itemId;
            this.itemData = itemData;
            this.count = count;
        }
    }
    
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
     * マウスクリックを処理します。
     * @param screenX スクリーンX座標
     * @param screenY スクリーンY座標
     * @return クリックされたアイテムデータ（クリックされなかった場合はnull）
     */
    public ItemData handleClick(int screenX, int screenY) {
        if (slotInfos == null) {
            return null;
        }
        
        // スクリーン座標をUI座標に変換（LibGDXはY座標が下から上）
        float uiY = screenHeight - screenY;
        
        for (SlotInfo slot : slotInfos) {
            if (screenX >= slot.x && screenX <= slot.x + SLOT_SIZE &&
                uiY >= slot.y && uiY <= slot.y + SLOT_SIZE) {
                selectedItemData = slot.itemData;
                selectedItemCount = slot.count;
                return slot.itemData;
            }
        }
        
        // 詳細パネルの外側をクリックした場合は詳細を閉じる
        if (selectedItemData != null) {
            float detailX = panelX + panelWidth + 20;
            float detailY = panelY;
            float detailWidth = 400;
            float detailHeight = 300;
            
            if (!(screenX >= detailX && screenX <= detailX + detailWidth &&
                  uiY >= detailY && uiY <= detailY + detailHeight)) {
                selectedItemData = null;
            }
        }
        
        return null;
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
        
        // スロット情報をリセット
        slotInfos = new ArrayList<>();
        
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
                
                // スロット情報を保存（クリック判定用）
                slotInfos.add(new SlotInfo(slotX, slotY - SLOT_SIZE, itemId, itemData, count));
                
                // 選択されているアイテムかどうかで色を変える
                boolean isSelected = selectedItemData != null && selectedItemData.id.equals(itemId);
                
                // スロットの背景を描画
                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                if (isSelected) {
                    shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f); // 選択時は少し明るく
                } else {
                    shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
                }
                shapeRenderer.rect(slotX, slotY - SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
                shapeRenderer.end();
                
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                if (isSelected) {
                    shapeRenderer.setColor(0.8f, 0.8f, 1.0f, 1f); // 選択時は明るい枠線
                } else {
                    shapeRenderer.setColor(0.5f, 0.5f, 0.7f, 1f);
                }
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
        String hint = "Click item for details | Press TAB to close";
        GlyphLayout hintLayout = new GlyphLayout(font, hint);
        float hintX = panelX + (panelWidth - hintLayout.width) / 2;
        font.draw(batch, hint, hintX, panelY + 20);
        font.setColor(Color.WHITE);
        
        // アイテム詳細パネルを描画
        if (selectedItemData != null) {
            renderItemDetail(selectedItemData, selectedItemCount);
        }
        
        font.getData().setScale(2.0f);
    }
    
    /**
     * アイテム詳細パネルを描画します。
     */
    private void renderItemDetail(ItemData itemData, int count) {
        float detailX = panelX + panelWidth + 20;
        float detailY = panelY;
        float detailWidth = 400;
        float detailHeight = 300;
        
        batch.end();
        
        // 詳細パネルの背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 0.95f);
        shapeRenderer.rect(detailX, detailY, detailWidth, detailHeight);
        shapeRenderer.end();
        
        // 詳細パネルの枠線
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.7f, 0.7f, 0.9f, 1f);
        shapeRenderer.rect(detailX, detailY, detailWidth, detailHeight);
        shapeRenderer.end();
        
        // アイテムの色で円を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(itemData.getColor());
        float iconSize = 60;
        float iconX = detailX + 20;
        float iconY = detailY + detailHeight - 80;
        shapeRenderer.circle(iconX + iconSize / 2, iconY + iconSize / 2, iconSize / 2);
        shapeRenderer.end();
        
        batch.begin();
        batch.setProjectionMatrix(uiCamera.combined);
        
        font.getData().setScale(2.0f);
        font.setColor(Color.WHITE);
        
        // アイテム名
        float textX = detailX + 100;
        float textY = detailY + detailHeight - 40;
        font.draw(batch, itemData.name, textX, textY);
        
        // 数量
        if (count > 1) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "x" + count, textX + 200, textY);
            font.setColor(Color.WHITE);
        }
        
        // 説明
        font.getData().setScale(1.5f);
        font.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
        float descY = textY - 50;
        
        // 説明文を複数行に分割（長すぎる場合）
        String description = itemData.description;
        float maxWidth = detailWidth - 40;
        GlyphLayout descLayout = new GlyphLayout(font, description);
        
        if (descLayout.width > maxWidth) {
            // 説明文が長い場合は折り返し処理（簡易版）
            String[] words = description.split("");
            StringBuilder line = new StringBuilder();
            float currentX = detailX + 20;
            
            for (String word : words) {
                String testLine = line.toString() + word;
                GlyphLayout testLayout = new GlyphLayout(font, testLine);
                if (testLayout.width > maxWidth && line.length() > 0) {
                    font.draw(batch, line.toString(), currentX, descY);
                    descY -= 25;
                    line = new StringBuilder(word);
                } else {
                    line.append(word);
                }
            }
            if (line.length() > 0) {
                font.draw(batch, line.toString(), currentX, descY);
            }
        } else {
            font.draw(batch, description, detailX + 20, descY);
        }
        
        // カテゴリとティア
        font.getData().setScale(1.2f);
        font.setColor(new Color(0.6f, 0.6f, 0.8f, 1f));
        descY -= 40;
        font.draw(batch, "Category: " + itemData.category, detailX + 20, descY);
        descY -= 25;
        font.draw(batch, "Tier: " + itemData.tier, detailX + 20, descY);
        descY -= 25;
        font.draw(batch, "Civ Level: " + itemData.getCivilizationLevel(), detailX + 20, descY);
        
        font.setColor(Color.WHITE);
    }
}
