package io.github.some_example_name.ui;

import io.github.some_example_name.entity.ItemData;
import io.github.some_example_name.manager.ItemDataLoader;
import io.github.some_example_name.game.Inventory;
import io.github.some_example_name.game.CraftingSystem;
import io.github.some_example_name.system.SoundManager;

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
    private float panelWidth = 900;
    private float panelHeight = 750;
    private float panelX;
    private float panelY;
    
    // アイテムスロットの設定
    private static final int SLOTS_PER_ROW = 6;
    private static final int MAX_ROWS = 8;
    private static final float SLOT_SIZE = 105;
    private static final float SLOT_PADDING = 15;
    
    // アイテム詳細表示
    private ItemData selectedItemData = null;
    private int selectedItemCount = 0;
    
    // スロット情報を保持（クリック判定用）
    private List<SlotInfo> slotInfos;
    
    // アイテム図鑑ボタン
    private Button encyclopediaButton;
    
    // タブ（インベントリ/クラフト）
    private enum Tab { INVENTORY, CRAFTING }
    private Tab currentTab = Tab.INVENTORY;
    private Button inventoryTabButton;
    private Button craftingTabButton;
    
    // クラフトシステム
    private CraftingSystem craftingSystem;
    
    // アイテムデータローダー（詳細表示用）
    private ItemDataLoader itemDataLoader;
    
    // クラフト可能アイテムのスロット情報
    private List<SlotInfo> craftSlotInfos;
    
    // サウンドマネージャー
    private SoundManager soundManager;
    
    // 前回のホバー状態を記録（音の重複再生を防ぐため）
    private boolean lastEncyclopediaButtonHovered = false;
    private ItemData lastHoveredItem = null;
    
    /**
     * スロット情報を保持する内部クラス
     */
    private static class SlotInfo {
        float x, y;
        ItemData itemData;
        int count;
        
        SlotInfo(float x, float y, ItemData itemData, int count) {
            this.x = x;
            this.y = y;
            this.itemData = itemData;
            this.count = count;
        }
    }
    
    public InventoryUI(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                     OrthographicCamera uiCamera, int screenWidth, int screenHeight) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        // にじみ対策：描画座標を整数にスナップ
        this.font.setUseIntegerPositions(true);
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
        
        // アイテム図鑑ボタンの位置を設定
        float buttonWidth = 300;
        float buttonHeight = 75;
        float buttonX = panelX + panelWidth - buttonWidth - 20;
        float buttonY = panelY + panelHeight - buttonHeight - 20;
        encyclopediaButton = new Button(buttonX, buttonY, buttonWidth, buttonHeight);
        
        // タブボタンの位置を設定
        float tabWidth = 200;
        float tabHeight = 50;
        float tabY = panelY + panelHeight - tabHeight - 20;
        inventoryTabButton = new Button(panelX + 20, tabY, tabWidth, tabHeight);
        craftingTabButton = new Button(panelX + 20 + tabWidth + 10, tabY, tabWidth, tabHeight);
    }
    
    /**
     * クラフトシステムを設定します。
     */
    public void setCraftingSystem(CraftingSystem craftingSystem) {
        this.craftingSystem = craftingSystem;
    }
    
    /**
     * アイテムデータローダーを設定します。
     */
    public void setItemDataLoader(ItemDataLoader itemDataLoader) {
        this.itemDataLoader = itemDataLoader;
    }
    
    /**
     * マウスクリックを処理します。
     * @param screenX スクリーンX座標
     * @param screenY スクリーンY座標
     * @return アイテム図鑑ボタンがクリックされた場合は特殊値としてItemData.id=-1を返す、クラフトが実行された場合はItemData.id=-2を返す、それ以外はnull
     */
    public ItemData handleClick(int screenX, int screenY) {
        // スクリーン座標をUI座標に変換（LibGDXはY座標が下から上）
        float uiY = screenHeight - screenY;
        
        // タブボタンのクリック判定
        if (inventoryTabButton != null && inventoryTabButton.contains(screenX, uiY)) {
            currentTab = Tab.INVENTORY;
            return null;
        }
        if (craftingTabButton != null && craftingTabButton.contains(screenX, uiY)) {
            currentTab = Tab.CRAFTING;
            return null;
        }
        
        // クラフトタブでクラフト可能アイテムをクリック
        if (currentTab == Tab.CRAFTING && craftSlotInfos != null && craftingSystem != null) {
            for (SlotInfo slot : craftSlotInfos) {
                if (screenX >= slot.x && screenX <= slot.x + SLOT_SIZE &&
                    uiY >= slot.y && uiY <= slot.y + SLOT_SIZE) {
                    // クラフト実行
                    if (craftingSystem.craft(slot.itemData)) {
                        // クラフト成功のマーカーを返す
                        ItemData craftMarker = new ItemData();
                        craftMarker.id = -2; // 特殊値として-2を使用
                        return craftMarker;
                    }
                    break;
                }
            }
        }
        
        // アイテム図鑑ボタンのクリック判定
        if (encyclopediaButton != null && encyclopediaButton.contains(screenX, uiY)) {
            // 特殊なItemDataを返してアイテム図鑑ボタンがクリックされたことを示す
            ItemData encyclopediaMarker = new ItemData();
            encyclopediaMarker.id = -1; // 特殊値として-1を使用
            return encyclopediaMarker;
        }
        
        return null;
    }
    
    /**
     * マウスホバーを処理して、ホバー中のアイテムを検出します。
     */
    private void handleHover() {
        // マウスの位置を取得
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();
        float uiY = screenHeight - mouseY;
        
        // 詳細パネルの上にマウスがある場合は、ホバーを無視
        float detailX = panelX + panelWidth + 30;
        float detailY = panelY;
        float detailWidth = 600;
        float detailHeight = 450;
        
        if (mouseX >= detailX && mouseX <= detailX + detailWidth &&
            uiY >= detailY && uiY <= detailY + detailHeight) {
            // 詳細パネルの上にある場合は、現在の選択を維持
            return;
        }
        
        // アイテムスロットの上にマウスがあるかチェック
        selectedItemData = null;
        selectedItemCount = 0;
        
        List<SlotInfo> slotsToCheck = currentTab == Tab.INVENTORY ? slotInfos : craftSlotInfos;
        if (slotsToCheck != null) {
            for (SlotInfo slot : slotsToCheck) {
                if (mouseX >= slot.x && mouseX <= slot.x + SLOT_SIZE &&
                    uiY >= slot.y && uiY <= slot.y + SLOT_SIZE) {
                    selectedItemData = slot.itemData;
                    selectedItemCount = slot.count;
                    break;
                }
            }
        }
    }
    
    /**
     * インベントリUIを描画します。
     * @param inventory インベントリ
     * @param itemDataLoader アイテムデータローダー
     */
    /**
     * インベントリUIを描画します。
     * 
     * <p>このメソッドはbatchを開始し、終了します。
     * 呼び出し元でbatchを管理する必要はありません。</p>
     * 
     * @param inventory インベントリ
     * @param itemDataLoader アイテムデータローダー
     */
    public void render(Inventory inventory, ItemDataLoader itemDataLoader) {
        if (inventory == null) {
            return;
        }
        
        // batchが既に開始されているかチェック
        boolean batchWasActive = batch.isDrawing();
        
        // パネルの背景を描画
        if (batchWasActive) {
            batch.end();
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
        
        // タブボタンを描画
        renderTabs();
        
        // タイトル位置を計算（タイトルは表示しないが、レイアウト用に使用）
        float titleY = panelY + panelHeight - 45;
        
        // アイテム図鑑ボタンを描画
        if (encyclopediaButton != null) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            boolean isHovered = encyclopediaButton.contains(mouseX, mouseY);
            
            // ホバー状態が変わったときに音を再生
            if (isHovered && !lastEncyclopediaButtonHovered && soundManager != null) {
                soundManager.playHoverSound();
            }
            lastEncyclopediaButtonHovered = isHovered;
            
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (isHovered) {
                shapeRenderer.setColor(0.25f, 0.25f, 0.35f, 0.95f);
            } else {
                shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 0.95f);
            }
            shapeRenderer.rect(encyclopediaButton.x, encyclopediaButton.y, encyclopediaButton.width, encyclopediaButton.height);
            shapeRenderer.end();
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            if (isHovered) {
                shapeRenderer.setColor(0.8f, 0.8f, 1.0f, 1f);
            } else {
                shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
            }
            shapeRenderer.rect(encyclopediaButton.x, encyclopediaButton.y, encyclopediaButton.width, encyclopediaButton.height);
            shapeRenderer.end();
            
            batch.begin();
            font.getData().setScale(0.675f);
            font.setColor(isHovered ? new Color(0.9f, 0.9f, 1.0f, 1f) : Color.WHITE);
            String buttonText = "アイテム図鑑";
            GlyphLayout buttonLayout = new GlyphLayout(font, buttonText);
            float buttonTextX = encyclopediaButton.x + (encyclopediaButton.width - buttonLayout.width) / 2;
            float buttonTextY = encyclopediaButton.y + encyclopediaButton.height / 2 + buttonLayout.height / 2;
            font.draw(batch, buttonText, buttonTextX, buttonTextY);
            font.getData().setScale(0.825f);
        }
        
        // 現在のタブに応じて内容を描画
        if (currentTab == Tab.INVENTORY) {
            renderInventoryTab(inventory, itemDataLoader, titleY);
        } else {
            renderCraftingTab(inventory, itemDataLoader, titleY);
        }
        
        // マウスホバーを処理
        handleHover();
        
        // アイテムホバー音を再生
        if (selectedItemData != null && selectedItemData != lastHoveredItem && soundManager != null) {
            soundManager.playHoverSound();
        }
        lastHoveredItem = selectedItemData;
        
        // アイテム詳細パネルを描画
        if (selectedItemData != null) {
            renderItemDetail(selectedItemData, selectedItemCount);
        }
        
        font.getData().setScale(0.825f);
        
        // batchを終了（元々開始されていなかった場合は終了しない）
        // 注意: renderTabs()やrenderInventoryTab()などでbatch.end()が呼ばれる可能性があるため、
        // ここでbatchが開始されているかチェックする
        if (!batchWasActive && batch.isDrawing()) {
            batch.end();
        }
    }
    
    /**
     * タブボタンを描画します。
     * 注意: このメソッドはbatchが開始されていることを前提とし、内部でbatch.end()を呼び出します。
     */
    private void renderTabs() {
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        // batchが開始されていることを確認
        if (!batch.isDrawing()) {
            batch.begin();
            batch.setProjectionMatrix(uiCamera.combined);
        }
        
        batch.end();
        
        // インベントリタブ
        boolean inventoryHovered = inventoryTabButton.contains(mouseX, mouseY);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (currentTab == Tab.INVENTORY) {
            shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f);
        } else if (inventoryHovered) {
            shapeRenderer.setColor(0.25f, 0.25f, 0.35f, 1f);
        } else {
            shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 1f);
        }
        shapeRenderer.rect(inventoryTabButton.x, inventoryTabButton.y, inventoryTabButton.width, inventoryTabButton.height);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        shapeRenderer.rect(inventoryTabButton.x, inventoryTabButton.y, inventoryTabButton.width, inventoryTabButton.height);
        shapeRenderer.end();
        
        // クラフトタブ
        boolean craftingHovered = craftingTabButton.contains(mouseX, mouseY);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (currentTab == Tab.CRAFTING) {
            shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f);
        } else if (craftingHovered) {
            shapeRenderer.setColor(0.25f, 0.25f, 0.35f, 1f);
        } else {
            shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 1f);
        }
        shapeRenderer.rect(craftingTabButton.x, craftingTabButton.y, craftingTabButton.width, craftingTabButton.height);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        shapeRenderer.rect(craftingTabButton.x, craftingTabButton.y, craftingTabButton.width, craftingTabButton.height);
        shapeRenderer.end();
        
        batch.begin();
        font.getData().setScale(0.675f);
        font.setColor(Color.WHITE);
        
        String inventoryTabText = "インベントリ";
        GlyphLayout inventoryLayout = new GlyphLayout(font, inventoryTabText);
        float inventoryTextX = inventoryTabButton.x + (inventoryTabButton.width - inventoryLayout.width) / 2;
        float inventoryTextY = inventoryTabButton.y + inventoryTabButton.height / 2 + inventoryLayout.height / 2;
        font.draw(batch, inventoryTabText, inventoryTextX, inventoryTextY);
        
        String craftingTabText = "クラフト";
        GlyphLayout craftingLayout = new GlyphLayout(font, craftingTabText);
        float craftingTextX = craftingTabButton.x + (craftingTabButton.width - craftingLayout.width) / 2;
        float craftingTextY = craftingTabButton.y + craftingTabButton.height / 2 + craftingLayout.height / 2;
        font.draw(batch, craftingTabText, craftingTextX, craftingTextY);
        
        font.getData().setScale(0.825f);
    }
    
    /**
     * インベントリタブの内容を描画します。
     */
    private void renderInventoryTab(Inventory inventory, ItemDataLoader itemDataLoader, float titleY) {
        float startX = panelX + 30;
        float startY = titleY - 75;
        float currentY = startY;
        
        Map<Integer, Integer> items = inventory.getAllItems();
        int itemIndex = 0;
        
        // スロット情報をリセット
        slotInfos = new ArrayList<>();
        
        if (items.isEmpty()) {
            // 空のインベントリメッセージを表示
            font.getData().setScale(0.825f);
            font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
            String emptyText = "Inventory is empty";
            GlyphLayout emptyLayout = new GlyphLayout(font, emptyText);
            float emptyX = panelX + (panelWidth - emptyLayout.width) / 2;
            float emptyY = panelY + panelHeight / 2;
            font.draw(batch, emptyText, emptyX, emptyY);
            font.setColor(Color.WHITE);
        } else {
            for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
                if (itemIndex >= SLOTS_PER_ROW * MAX_ROWS) {
                    break; // 最大表示数を超えたら終了
                }
                
                int itemId = entry.getKey();
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
                slotInfos.add(new SlotInfo(slotX, slotY - SLOT_SIZE, itemData, count));
                
                // ホバー中のアイテムかどうかで色を変える
                boolean isHovered = selectedItemData != null && selectedItemData.id == itemId;
                
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
                
                // 数量を描画
                if (count > 1) {
                    String countText = "x" + count;
                    // 所持数は小さめにして、スロット内で隠れないようにする
                    float prevScaleX = font.getData().scaleX;
                    float prevScaleY = font.getData().scaleY;
                    font.getData().setScale(0.55f);
                    GlyphLayout countLayout = new GlyphLayout(font, countText);
                    float countX = slotX + SLOT_SIZE - countLayout.width - 8;
                    font.setColor(Color.YELLOW);
                    // にじみ防止のため整数座標で描画（下端ギリギリを避ける）
                    font.draw(batch, countText, Math.round(countX), Math.round(slotY - SLOT_SIZE + 22));
                    font.setColor(Color.WHITE);
                    font.getData().setScale(prevScaleX, prevScaleY);
                }
                
                itemIndex++;
            }
        }
    }
    
    /**
     * クラフトタブの内容を描画します。
     */
    private void renderCraftingTab(Inventory inventory, ItemDataLoader itemDataLoader, float titleY) {
        if (craftingSystem == null || itemDataLoader == null) {
            return;
        }
        
        float startX = panelX + 30;
        float startY = titleY - 75;
        float currentY = startY;
        
        // クラフト可能なアイテムを取得
        List<ItemData> craftableItems = new ArrayList<>();
        for (ItemData itemData : itemDataLoader.getAllItems()) {
            if (itemData.isCraftable()) {
                craftableItems.add(itemData);
            }
        }
        
        // スロット情報をリセット
        craftSlotInfos = new ArrayList<>();
        
        if (craftableItems.isEmpty()) {
            // クラフト可能なアイテムがないメッセージを表示
            font.getData().setScale(0.825f);
            font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
            String emptyText = "クラフト可能なアイテムがありません";
            GlyphLayout emptyLayout = new GlyphLayout(font, emptyText);
            float emptyX = panelX + (panelWidth - emptyLayout.width) / 2;
            float emptyY = panelY + panelHeight / 2;
            font.draw(batch, emptyText, emptyX, emptyY);
            font.setColor(Color.WHITE);
        } else {
            int itemIndex = 0;
            for (ItemData itemData : craftableItems) {
                if (itemIndex >= SLOTS_PER_ROW * MAX_ROWS) {
                    break;
                }
                
                // クラフト可能かチェック
                boolean canCraft = craftingSystem.canCraft(itemData);
                
                // スロットの位置を計算
                int row = itemIndex / SLOTS_PER_ROW;
                int col = itemIndex % SLOTS_PER_ROW;
                float slotX = startX + col * (SLOT_SIZE + SLOT_PADDING);
                float slotY = currentY - row * (SLOT_SIZE + SLOT_PADDING);
                
                // スロット情報を保存（クリック判定用）
                craftSlotInfos.add(new SlotInfo(slotX, slotY - SLOT_SIZE, itemData, 0));
                
                // ホバー中のアイテムかどうかで色を変える
                boolean isHovered = selectedItemData != null && selectedItemData.id == itemData.id;
                
                // スロットの背景を描画
                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                if (!canCraft) {
                    shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1f); // クラフト不可は暗く
                } else if (isHovered) {
                    shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f);
                } else {
                    shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
                }
                shapeRenderer.rect(slotX, slotY - SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
                shapeRenderer.end();
                
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                if (!canCraft) {
                    shapeRenderer.setColor(0.4f, 0.2f, 0.2f, 1f); // クラフト不可は赤っぽく
                } else if (isHovered) {
                    shapeRenderer.setColor(0.8f, 0.8f, 1.0f, 1f);
                } else {
                    shapeRenderer.setColor(0.5f, 0.5f, 0.7f, 1f);
                }
                shapeRenderer.rect(slotX, slotY - SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
                shapeRenderer.end();
                
                // アイテムの色で円を描画
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                Color itemColor = itemData.getColor();
                if (!canCraft) {
                    // クラフト不可の場合は色を暗く
                    shapeRenderer.setColor(itemColor.r * 0.5f, itemColor.g * 0.5f, itemColor.b * 0.5f, itemColor.a);
                } else {
                    shapeRenderer.setColor(itemColor);
                }
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
    }
    
    /**
     * アイテム詳細パネルを描画します。
     */
    private void renderItemDetail(ItemData itemData, int count) {
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
        
        // 数量
        if (count > 1) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "x" + count, textX + 300, textY);
            font.setColor(Color.WHITE);
        }
        
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
        
        // クラフト可能なアイテムの場合は素材情報を表示
        if (itemData.isCraftable()) {
            descY -= 50;
            font.setColor(new Color(0.9f, 0.9f, 0.7f, 1f));
            font.draw(batch, "必要な素材:", detailX + 30, descY);
            descY -= 30;
            
            Map<Integer, Integer> materials = itemData.getMaterials();
            for (Map.Entry<Integer, Integer> entry : materials.entrySet()) {
                int materialId = entry.getKey();
                int requiredAmount = entry.getValue();
                
                // 素材の名前を取得
                ItemData materialData = null;
                if (itemDataLoader != null) {
                    materialData = itemDataLoader.getItemData(materialId);
                }
                String materialName = materialData != null ? materialData.name : "アイテムID" + materialId;
                int currentAmount = craftingSystem != null ? craftingSystem.getItemCount(materialId) : 0;
                
                // 素材名と必要数を表示
                String materialText = materialName + ": " + currentAmount + "/" + requiredAmount;
                Color materialColor = currentAmount >= requiredAmount ? 
                    new Color(0.7f, 1.0f, 0.7f, 1f) : new Color(1.0f, 0.7f, 0.7f, 1f);
                font.setColor(materialColor);
                font.draw(batch, materialText, detailX + 30, descY);
                descY -= 25;
            }
        }
        
        font.setColor(Color.WHITE);
    }
}
