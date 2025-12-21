package io.github.some_example_name.ui;

import io.github.some_example_name.entity.ItemData;
import io.github.some_example_name.manager.ItemDataLoader;
import io.github.some_example_name.system.SoundManager;

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
    private float panelWidth = 1200;
    private float panelHeight = 900;
    private float panelX;
    private float panelY;
    
    // アイテムスロットの設定
    private static final int SLOTS_PER_ROW = 8;
    private static final int MAX_ROWS = 10;
    private static final float SLOT_SIZE = 105;
    private static final float SLOT_PADDING = 15;
    
    // アイテム詳細表示
    private ItemData selectedItemData = null;
    
    // スロット情報を保持（クリック判定用）
    private List<SlotInfo> slotInfos;
    
    // インベントリに戻るボタン
    private Button backButton;
    
    // スクロール位置
    private float scrollOffset = 0;
    private static final float SCROLL_SPEED = 20f;
    
    // サウンドマネージャー
    private SoundManager soundManager;
    
    // 前回のホバー状態を記録（音の重複再生を防ぐため）
    private boolean lastBackButtonHovered = false;
    private ItemData lastHoveredItem = null;
    
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
     * サウンドマネージャーを設定します。
     */
    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
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
        float buttonWidth = 300;
        float buttonHeight = 75;
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
            float detailX = panelX + panelWidth + 30;
            float detailY = panelY;
            float detailWidth = 600;
            float detailHeight = 450;
            
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
     * ホバー処理を行います。
     */
    private void handleHover() {
        if (slotInfos == null) {
            selectedItemData = null;
            return;
        }
        
        // マウスの位置を取得
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        // 詳細パネルの上にマウスがある場合は、ホバーを無視
        float detailX = panelX + panelWidth + 30;
        float detailY = panelY;
        float detailWidth = 600;
        float detailHeight = 450;
        
        boolean mouseOnDetailPanel = mouseX >= detailX && mouseX <= detailX + detailWidth &&
                                    mouseY >= detailY && mouseY <= detailY + detailHeight;
        
        // 詳細パネルの上にマウスがある場合は何もしない
        if (mouseOnDetailPanel) {
            return;
        }
        
        // ホバー中のアイテムをリセット
        selectedItemData = null;
        
        // スロット情報をチェックしてホバー中のアイテムを検出
        for (SlotInfo slot : slotInfos) {
            if (mouseX >= slot.x && mouseX <= slot.x + SLOT_SIZE &&
                mouseY >= slot.y && mouseY <= slot.y + SLOT_SIZE) {
                selectedItemData = slot.itemData;
                break;
            }
        }
    }
    
    /**
     * アイテム図鑑UIを描画します。
     * @param itemDataLoader アイテムデータローダー
     */
    /**
     * アイテム図鑑UIを描画します。
     * 
     * <p>このメソッドはbatchを開始し、終了します。
     * 呼び出し元でbatchを管理する必要はありません。</p>
     * 
     * @param itemDataLoader アイテムデータローダー
     */
    public void render(ItemDataLoader itemDataLoader) {
        if (itemDataLoader == null) {
            return;
        }
        
        // batchが既に開始されているかチェック
        boolean batchWasActive = batch.isDrawing();
        
        // パネルの背景を描画
        if (batchWasActive) {
        // batchを終了（元々開始されていなかった場合は終了しない）
        if (!batchWasActive) {
            batch.end();
        }
    }
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
        
        // batchを開始（元々開始されていた場合は再度開始）
        batch.begin();
        batch.setProjectionMatrix(uiCamera.combined);
        
        font.getData().setScale(0.825f);
        font.setColor(Color.WHITE);
        
        // タイトルを描画
        String title = "アイテム図鑑";
        GlyphLayout titleLayout = new GlyphLayout(font, title);
        float titleX = panelX + (panelWidth - titleLayout.width) / 2;
        float titleY = panelY + panelHeight - 45;
        font.draw(batch, title, titleX, titleY);
        
        // 戻るボタンを描画
        if (backButton != null) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            boolean isHovered = backButton.contains(mouseX, mouseY);
            
            // ホバー状態が変わったときに音を再生
            if (isHovered && !lastBackButtonHovered && soundManager != null) {
                soundManager.playHoverSound();
            }
            lastBackButtonHovered = isHovered;
            
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
            font.getData().setScale(0.675f);
            font.setColor(isHovered ? new Color(0.9f, 0.9f, 1.0f, 1f) : Color.WHITE);
            String backText = "インベントリに戻る";
            GlyphLayout backLayout = new GlyphLayout(font, backText);
            float backTextX = backButton.x + (backButton.width - backLayout.width) / 2;
            float backTextY = backButton.y + backButton.height / 2 + backLayout.height / 2;
            font.draw(batch, backText, backTextX, backTextY);
        }
        
        // アイテムリストを描画
        float startX = panelX + 30;
        float startY = titleY - 120;
        float currentY = startY - scrollOffset;
        
        Array<ItemData> allItems = itemDataLoader.getAllItems();
        
        // スロット情報をリセット
        slotInfos = new ArrayList<>();
        
        if (allItems.size == 0) {
            // 空のメッセージを表示
            font.getData().setScale(0.825f);
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
                
                // ホバー中のアイテムかどうかで色を変える
                boolean isHovered = selectedItemData != null && selectedItemData.id == itemData.id;
                
                // スロットの背景を描画
                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                if (isHovered) {
                    shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f); // ホバー時は少し明るく
                } else {
                    shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
                }
                shapeRenderer.rect(slotX, slotY - SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
                shapeRenderer.end();
                
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                if (isHovered) {
                    shapeRenderer.setColor(0.8f, 0.8f, 1.0f, 1f); // ホバー時は明るい枠線
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
        
        // ホバー処理を実行（slotInfosが設定された後）
        handleHover();
        
        // アイテムホバー音を再生
        if (selectedItemData != null && selectedItemData != lastHoveredItem && soundManager != null) {
            soundManager.playHoverSound();
        }
        lastHoveredItem = selectedItemData;
        
        // 閉じるヒントを描画
        font.getData().setScale(0.6375f);
        font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        String hint = "Hover item for details | Press E to close";
        GlyphLayout hintLayout = new GlyphLayout(font, hint);
        float hintX = panelX + (panelWidth - hintLayout.width) / 2;
        font.draw(batch, hint, hintX, panelY + 30);
        font.setColor(Color.WHITE);
        
        // アイテム詳細パネルを描画（ホバーまたはクリックで選択されたアイテム）
        if (selectedItemData != null) {
            renderItemDetail(selectedItemData);
        }
        
        font.getData().setScale(0.825f);
    }
    
    /**
     * アイテム詳細パネルを描画します。
     */
    private void renderItemDetail(ItemData itemData) {
        float detailX = panelX + panelWidth + 30;
        float detailY = panelY;
        float detailWidth = 600;
        float detailHeight = 450;
        
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
        float iconSize = 90;
        float iconX = detailX + 30;
        float iconY = detailY + detailHeight - 120;
        shapeRenderer.circle(iconX + iconSize / 2, iconY + iconSize / 2, iconSize / 2);
        shapeRenderer.end();
        
        batch.begin();
        batch.setProjectionMatrix(uiCamera.combined);
        
        font.getData().setScale(0.825f);
        font.setColor(Color.WHITE);
        
        // アイテム名
        float textX = detailX + 150;
        float textY = detailY + detailHeight - 60;
        font.draw(batch, itemData.name, textX, textY);
        
        // 説明
        font.getData().setScale(0.6375f);
        font.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
        float descY = textY - 75;
        
        // 説明文を複数行に分割（長すぎる場合）
        String description = itemData.description;
        float maxWidth = detailWidth - 60;
        GlyphLayout descLayout = new GlyphLayout(font, description);
        
        if (descLayout.width > maxWidth) {
            // 説明文が長い場合は折り返し処理（簡易版）
            String[] words = description.split("");
            StringBuilder line = new StringBuilder();
            float currentX = detailX + 30;
            
            for (String word : words) {
                String testLine = line.toString() + word;
                GlyphLayout testLayout = new GlyphLayout(font, testLine);
                if (testLayout.width > maxWidth && line.length() > 0) {
                    font.draw(batch, line.toString(), currentX, descY);
                    descY -= 37.5f;
                    line = new StringBuilder(word);
                } else {
                    line.append(word);
                }
            }
            if (line.length() > 0) {
                font.draw(batch, line.toString(), currentX, descY);
            }
        } else {
            font.draw(batch, description, detailX + 30, descY);
        }
        
        font.setColor(Color.WHITE);
    }
}
