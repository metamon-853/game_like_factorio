package io.github.some_example_name.ui;

import io.github.some_example_name.entity.ItemData;
import io.github.some_example_name.manager.ItemDataLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

/**
 * アイテム図鑑UIを描画するクラス。
 */
public class ItemEncyclopediaUI {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    private int screenWidth;
    private int screenHeight;
    
    // UIのサイズと位置
    private float panelWidth = 800;
    private float panelHeight = 600;
    private float panelX;
    private float panelY;
    
    // アイテムスロットの設定
    private static final int SLOTS_PER_ROW = 8;
    private static final int MAX_ROWS = 10;
    private static final float SLOT_SIZE = 70;
    private static final float SLOT_PADDING = 10;
    
    // アイテム詳細表示
    private ItemData selectedItemData = null;
    
    // ホバー中のアイテム（ツールチップ表示用）
    private ItemData hoveredItemData = null;
    
    // スロット情報を保持（クリック判定用）
    private List<SlotInfo> slotInfos;
    
    // インベントリに戻るボタン
    private Button backButton;
    
    // スクロール位置
    private float scrollOffset = 0;
    private static final float SCROLL_SPEED = 20f;
    
    /**
     * スロット情報を保持する内部クラス
     */
    private static class SlotInfo {
        float x, y;
        ItemData itemData;
        
        SlotInfo(float x, float y, ItemData itemData) {
            this.x = x;
            this.y = y;
            this.itemData = itemData;
        }
    }
    
    public ItemEncyclopediaUI(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
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
        
        // インベントリに戻るボタンの位置を設定
        float buttonWidth = 200;
        float buttonHeight = 50;
        float buttonX = panelX + 20;
        float buttonY = panelY + panelHeight - buttonHeight - 20;
        backButton = new Button(buttonX, buttonY, buttonWidth, buttonHeight);
    }
    
    /**
     * マウスクリックを処理します。
     * @param screenX スクリーンX座標
     * @param screenY スクリーンY座標
     * @return 戻るボタンがクリックされた場合true
     */
    public boolean handleClick(int screenX, int screenY) {
        if (slotInfos == null) {
            return false;
        }
        
        // スクリーン座標をUI座標に変換（LibGDXはY座標が下から上）
        float uiY = screenHeight - screenY;
        
        // 戻るボタンのクリック判定
        if (backButton != null && backButton.contains(screenX, uiY)) {
            return true; // 戻るボタンがクリックされた
        }
        
        // アイテムスロットのクリック判定
        for (SlotInfo slot : slotInfos) {
            if (screenX >= slot.x && screenX <= slot.x + SLOT_SIZE &&
                uiY >= slot.y && uiY <= slot.y + SLOT_SIZE) {
                selectedItemData = slot.itemData;
                return false;
            }
        }
        
        // 詳細パネルの外側をクリックした場合は詳細を閉じる
        if (selectedItemData != null) {
            float detailX = panelX + panelWidth + 20;
            float detailY = panelY;
            float detailWidth = 400;
            float detailHeight = 400;
            
            if (!(screenX >= detailX && screenX <= detailX + detailWidth &&
                  uiY >= detailY && uiY <= detailY + detailHeight)) {
                selectedItemData = null;
            }
        }
        
        return false;
    }
    
    /**
     * スクロール処理を行います。
     */
    public void handleScroll(float amountY) {
        scrollOffset += amountY * SCROLL_SPEED;
        // スクロール範囲を制限（必要に応じて調整）
        scrollOffset = Math.max(0, scrollOffset);
    }
    
    /**
     * アイテム図鑑UIを描画します。
     * @param itemDataLoader アイテムデータローダー
     */
    public void render(ItemDataLoader itemDataLoader) {
        if (itemDataLoader == null) {
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
        
        font.getData().setScale(2.2f);
        font.setColor(Color.WHITE);
        
        // タイトルを描画
        String title = "アイテム図鑑";
        GlyphLayout titleLayout = new GlyphLayout(font, title);
        float titleX = panelX + (panelWidth - titleLayout.width) / 2;
        float titleY = panelY + panelHeight - 30;
        font.draw(batch, title, titleX, titleY);
        
        // 戻るボタンを描画
        if (backButton != null) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            boolean isHovered = backButton.contains(mouseX, mouseY);
            
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (isHovered) {
                shapeRenderer.setColor(0.25f, 0.25f, 0.35f, 0.95f);
            } else {
                shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 0.95f);
            }
            shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
            shapeRenderer.end();
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            if (isHovered) {
                shapeRenderer.setColor(0.8f, 0.8f, 1.0f, 1f);
            } else {
                shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
            }
            shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
            shapeRenderer.end();
            
            batch.begin();
            font.getData().setScale(1.8f);
            font.setColor(isHovered ? new Color(0.9f, 0.9f, 1.0f, 1f) : Color.WHITE);
            String backText = "インベントリに戻る";
            GlyphLayout backLayout = new GlyphLayout(font, backText);
            float backTextX = backButton.x + (backButton.width - backLayout.width) / 2;
            float backTextY = backButton.y + backButton.height / 2 + backLayout.height / 2;
            font.draw(batch, backText, backTextX, backTextY);
        }
        
        // マウス位置を取得（ホバー検出用）
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        // アイテムリストを描画
        float startX = panelX + 20;
        float startY = titleY - 80;
        float currentY = startY - scrollOffset;
        
        Array<ItemData> allItems = itemDataLoader.getAllItems();
        
        // スロット情報をリセット
        slotInfos = new ArrayList<>();
        
        // ホバー中のアイテムをリセット
        hoveredItemData = null;
        
        if (allItems.size == 0) {
            // 空のメッセージを表示
            font.getData().setScale(2.2f);
            font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
            String emptyText = "アイテムがありません";
            GlyphLayout emptyLayout = new GlyphLayout(font, emptyText);
            float emptyX = panelX + (panelWidth - emptyLayout.width) / 2;
            float emptyY = panelY + panelHeight / 2;
            font.draw(batch, emptyText, emptyX, emptyY);
            font.setColor(Color.WHITE);
        } else {
            int itemIndex = 0;
            for (ItemData itemData : allItems) {
                if (itemIndex >= SLOTS_PER_ROW * MAX_ROWS) {
                    break; // 最大表示数を超えたら終了
                }
                
                // スロットの位置を計算
                int row = itemIndex / SLOTS_PER_ROW;
                int col = itemIndex % SLOTS_PER_ROW;
                float slotX = startX + col * (SLOT_SIZE + SLOT_PADDING);
                float slotY = currentY - row * (SLOT_SIZE + SLOT_PADDING);
                
                // スロット情報を保存（クリック判定用）
                slotInfos.add(new SlotInfo(slotX, slotY - SLOT_SIZE, itemData));
                
                // ホバー判定
                boolean isHovered = mouseX >= slotX && mouseX <= slotX + SLOT_SIZE &&
                                   mouseY >= slotY - SLOT_SIZE && mouseY <= slotY;
                
                if (isHovered) {
                    hoveredItemData = itemData;
                }
                
                // 選択されているアイテムかどうかで色を変える
                boolean isSelected = selectedItemData != null && selectedItemData.id == itemData.id;
                
                // スロットの背景を描画
                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                if (isSelected) {
                    shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f); // 選択時は少し明るく
                } else if (isHovered) {
                    shapeRenderer.setColor(0.25f, 0.25f, 0.4f, 1f); // ホバー時は少し明るく
                } else {
                    shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
                }
                shapeRenderer.rect(slotX, slotY - SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
                shapeRenderer.end();
                
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                if (isSelected) {
                    shapeRenderer.setColor(0.8f, 0.8f, 1.0f, 1f); // 選択時は明るい枠線
                } else if (isHovered) {
                    shapeRenderer.setColor(0.7f, 0.7f, 0.9f, 1f); // ホバー時は明るい枠線
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
                
                itemIndex++;
            }
        }
        
        // 閉じるヒントを描画
        font.getData().setScale(1.7f);
        font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        String hint = "アイテムにホバーで説明表示 | クリックで詳細表示 | ESCで閉じる";
        GlyphLayout hintLayout = new GlyphLayout(font, hint);
        float hintX = panelX + (panelWidth - hintLayout.width) / 2;
        font.draw(batch, hint, hintX, panelY + 20);
        font.setColor(Color.WHITE);
        
        // アイテム詳細パネルを描画
        if (selectedItemData != null) {
            renderItemDetail(selectedItemData);
        }
        
        // ホバー中のアイテムのツールチップを描画
        if (hoveredItemData != null && selectedItemData == null) {
            renderTooltip(hoveredItemData, mouseX, mouseY);
        }
        
        font.getData().setScale(2.2f);
    }
    
    /**
     * ホバー時のツールチップを描画します。
     */
    private void renderTooltip(ItemData itemData, float mouseX, float mouseY) {
        batch.end();
        
        // ツールチップのサイズと位置を計算
        float tooltipWidth = 300;
        float tooltipHeight = 150;
        
        // マウス位置に合わせてツールチップを配置（画面外に出ないように調整）
        float tooltipX = mouseX + 15;
        float tooltipY = mouseY + 15;
        
        // 画面右端を超える場合は左側に表示
        if (tooltipX + tooltipWidth > screenWidth) {
            tooltipX = mouseX - tooltipWidth - 15;
        }
        
        // 画面上端を超える場合は下側に表示
        if (tooltipY + tooltipHeight > screenHeight) {
            tooltipY = mouseY - tooltipHeight - 15;
        }
        
        // ツールチップの背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 0.95f);
        shapeRenderer.rect(tooltipX, tooltipY, tooltipWidth, tooltipHeight);
        shapeRenderer.end();
        
        // ツールチップの枠線
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.7f, 0.7f, 0.9f, 1f);
        shapeRenderer.rect(tooltipX, tooltipY, tooltipWidth, tooltipHeight);
        shapeRenderer.end();
        
        // アイテムの色で小さな円を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(itemData.getColor());
        float iconSize = 30;
        float iconX = tooltipX + 10;
        float iconY = tooltipY + tooltipHeight - 40;
        shapeRenderer.circle(iconX + iconSize / 2, iconY + iconSize / 2, iconSize / 2);
        shapeRenderer.end();
        
        batch.begin();
        batch.setProjectionMatrix(uiCamera.combined);
        
        // アイテム名
        font.getData().setScale(1.8f);
        font.setColor(Color.WHITE);
        float textX = tooltipX + 50;
        float textY = tooltipY + tooltipHeight - 20;
        font.draw(batch, itemData.name, textX, textY);
        
        // 説明（短縮版）
        font.getData().setScale(1.4f);
        font.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
        float descY = textY - 30;
        String description = itemData.description;
        
        // 説明が長すぎる場合は切り詰める
        float maxWidth = tooltipWidth - 60;
        GlyphLayout descLayout = new GlyphLayout(font, description);
        if (descLayout.width > maxWidth) {
            // 文字数を制限して「...」を追加
            String truncated = description;
            while (true) {
                GlyphLayout testLayout = new GlyphLayout(font, truncated + "...");
                if (testLayout.width <= maxWidth || truncated.length() <= 1) {
                    break;
                }
                truncated = truncated.substring(0, truncated.length() - 1);
            }
            description = truncated + "...";
        }
        
        font.draw(batch, description, tooltipX + 10, descY);
        
        // カテゴリとティア（小さく表示）
        font.getData().setScale(1.2f);
        font.setColor(new Color(0.6f, 0.6f, 0.8f, 1f));
        descY -= 25;
        font.draw(batch, itemData.category + " | ティア" + itemData.tier, tooltipX + 10, descY);
        
        font.setColor(Color.WHITE);
    }
    
    /**
     * アイテム詳細パネルを描画します。
     */
    private void renderItemDetail(ItemData itemData) {
        float detailX = panelX + panelWidth + 20;
        float detailY = panelY;
        float detailWidth = 400;
        float detailHeight = 400;
        
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
        
        font.getData().setScale(2.2f);
        font.setColor(Color.WHITE);
        
        // アイテム名
        float textX = detailX + 100;
        float textY = detailY + detailHeight - 40;
        font.draw(batch, itemData.name, textX, textY);
        
        // 説明
        font.getData().setScale(1.7f);
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
        font.getData().setScale(1.4f);
        font.setColor(new Color(0.6f, 0.6f, 0.8f, 1f));
        descY -= 40;
        font.draw(batch, "カテゴリ: " + itemData.category, detailX + 20, descY);
        descY -= 25;
        font.draw(batch, "ティア: " + itemData.tier, detailX + 20, descY);
        descY -= 25;
        font.draw(batch, "文明レベル: " + itemData.getCivilizationLevel(), detailX + 20, descY);
        
        font.setColor(Color.WHITE);
    }
}
