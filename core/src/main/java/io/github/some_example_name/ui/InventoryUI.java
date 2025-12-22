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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

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
    
    // ウィンドウ（共通化）
    private UIWindow window;
    
    // アイテムスロットの設定
    private static final int SLOTS_PER_ROW = 6;
    private static final int MAX_ROWS = 8;
    private static final float SLOT_SIZE = 105;
    private static final float SLOT_PADDING = 15;
    
    // アイテム詳細表示
    private ItemData selectedItemData = null;
    private int selectedItemCount = 0;
    
    // アイテム詳細パネル（共通化）
    private ItemDetailPanel itemDetailPanel;
    
    // スロット情報を保持（クリック判定用）
    private List<SlotInfo> slotInfos;
    
    // アイテム図鑑ボタン
    private UIButton encyclopediaButton;
    
    // タブ（インベントリ/クラフト）
    private enum Tab { INVENTORY, CRAFTING }
    private Tab currentTab = Tab.INVENTORY;
    private UIButton inventoryTabButton;
    private UIButton craftingTabButton;
    
    // クラフトシステム
    private CraftingSystem craftingSystem;
    
    // アイテムデータローダー（詳細表示用）
    private ItemDataLoader itemDataLoader;
    
    // クラフト可能アイテムのスロット情報
    private List<SlotInfo> craftSlotInfos = new ArrayList<>();
    
    // サウンドマネージャー
    private SoundManager soundManager;
    
    // 前回のホバー状態を記録（音の重複再生を防ぐため）
    private ItemData lastHoveredItem = null;
    
    // スクロールバー
    private UIScrollBar scrollBar;
    private float contentAreaY;
    private float contentAreaHeight;
    
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
        initializeWindow();
    }
    
    /**
     * ウィンドウを初期化します。
     */
    private void initializeWindow() {
        window = new UIWindow(panelX, panelY, panelWidth, panelHeight);
        window.setRenderResources(shapeRenderer, batch, font, uiCamera);
        
        // アイテム詳細パネルを初期化
        itemDetailPanel = new ItemDetailPanel(shapeRenderer, batch, font, uiCamera);
        itemDetailPanel.setPosition(panelX + panelWidth + 30, panelY);
        
        // スクロールバーを初期化
        scrollBar = new UIScrollBar(shapeRenderer, uiCamera, screenHeight);
        scrollBar.setScrollSpeed(20f);
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
        if (window != null) {
            window.x = panelX;
            window.y = panelY;
            window.width = panelWidth;
            window.height = panelHeight;
        }
    }
    
    /**
     * パネルの位置を更新します（画面中央に配置）。
     */
    private void updatePanelPosition() {
        panelX = (screenWidth - panelWidth) / 2;
        panelY = (screenHeight - panelHeight) / 2;
        
        // コンテンツエリアの設定
        float titleY = panelY + panelHeight - 45;
        float buttonAreaHeight = 75 + 20; // ボタンエリアの高さ
        contentAreaY = panelY + 20;
        contentAreaHeight = titleY - buttonAreaHeight - contentAreaY - 20;
        
        // スクロールバーのコンテンツエリアを設定
        if (scrollBar != null) {
            scrollBar.setContentArea(panelX + 30, contentAreaY, panelWidth - 60, contentAreaHeight);
        }
        
        // アイテム図鑑ボタンの位置を設定
        float buttonWidth = 300;
        float buttonHeight = 75;
        float buttonX = panelX + panelWidth - buttonWidth - 20;
        float buttonY = panelY + panelHeight - buttonHeight - 20;
        encyclopediaButton = new UIButton(buttonX, buttonY, buttonWidth, buttonHeight, "アイテム図鑑");
        encyclopediaButton.setRenderResources(shapeRenderer, batch, font, uiCamera);
        encyclopediaButton.setSoundManager(soundManager);
        
        // タブボタンの位置を設定
        float tabWidth = 200;
        float tabHeight = 50;
        float tabY = panelY + panelHeight - tabHeight - 20;
        inventoryTabButton = new UIButton(panelX + 20, tabY, tabWidth, tabHeight, "インベントリ");
        inventoryTabButton.setRenderResources(shapeRenderer, batch, font, uiCamera);
        inventoryTabButton.setSoundManager(soundManager);
        craftingTabButton = new UIButton(panelX + 20 + tabWidth + 10, tabY, tabWidth, tabHeight, "クラフト");
        craftingTabButton.setRenderResources(shapeRenderer, batch, font, uiCamera);
        craftingTabButton.setSoundManager(soundManager);
        
        // アイテム詳細パネルの位置を更新
        if (itemDetailPanel != null) {
            itemDetailPanel.setPosition(panelX + panelWidth + 30, panelY);
        }
    }
    
    /**
     * クラフトシステムを設定します。
     */
    public void setCraftingSystem(CraftingSystem craftingSystem) {
        this.craftingSystem = craftingSystem;
        if (itemDetailPanel != null) {
            itemDetailPanel.setCraftingSystem(craftingSystem);
        }
    }
    
    /**
     * アイテムデータローダーを設定します。
     */
    public void setItemDataLoader(ItemDataLoader itemDataLoader) {
        this.itemDataLoader = itemDataLoader;
        if (itemDetailPanel != null) {
            itemDetailPanel.setItemDataLoader(itemDataLoader);
        }
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
            // スクロール位置をリセット
            if (scrollBar != null) {
                scrollBar.resetScroll();
            }
            return null;
        }
        if (craftingTabButton != null && craftingTabButton.contains(screenX, uiY)) {
            currentTab = Tab.CRAFTING;
            // スクロール位置をリセット
            if (scrollBar != null) {
                scrollBar.resetScroll();
            }
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
        if (itemDetailPanel != null && itemDetailPanel.contains(mouseX, uiY)) {
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
        
        // ウィンドウを描画
        if (window != null) {
            window.render();
        }
        
        // batchが既に開始されているかチェック
        boolean batchWasActive = batch.isDrawing();
        if (!batchWasActive) {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
        }
        
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
            encyclopediaButton.updateAndRender(mouseX, mouseY);
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
        if (selectedItemData != null && itemDetailPanel != null) {
            itemDetailPanel.render(selectedItemData, selectedItemCount);
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
        
        // インベントリタブ（選択状態を反映）
        boolean inventoryHovered = inventoryTabButton.contains(mouseX, mouseY);
        if (currentTab == Tab.INVENTORY) {
            // 選択中のタブは特別な色で描画
            shapeRenderer.setProjectionMatrix(uiCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f);
            shapeRenderer.rect(inventoryTabButton.x, inventoryTabButton.y, inventoryTabButton.width, inventoryTabButton.height);
            shapeRenderer.end();
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
            shapeRenderer.rect(inventoryTabButton.x, inventoryTabButton.y, inventoryTabButton.width, inventoryTabButton.height);
            shapeRenderer.end();
            
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            font.getData().setScale(0.675f);
            font.setColor(Color.WHITE);
            String inventoryTabText = "インベントリ";
            GlyphLayout inventoryLayout = new GlyphLayout(font, inventoryTabText);
            float inventoryTextX = inventoryTabButton.x + (inventoryTabButton.width - inventoryLayout.width) / 2;
            float inventoryTextY = inventoryTabButton.y + inventoryTabButton.height / 2 + inventoryLayout.height / 2;
            font.draw(batch, inventoryTabText, inventoryTextX, inventoryTextY);
        } else {
            inventoryTabButton.updateAndRender(mouseX, mouseY);
        }
        
        // クラフトタブ（選択状態を反映）
        boolean craftingHovered = craftingTabButton.contains(mouseX, mouseY);
        if (currentTab == Tab.CRAFTING) {
            // 選択中のタブは特別な色で描画
            shapeRenderer.setProjectionMatrix(uiCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f);
            shapeRenderer.rect(craftingTabButton.x, craftingTabButton.y, craftingTabButton.width, craftingTabButton.height);
            shapeRenderer.end();
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
            shapeRenderer.rect(craftingTabButton.x, craftingTabButton.y, craftingTabButton.width, craftingTabButton.height);
            shapeRenderer.end();
            
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            font.getData().setScale(0.675f);
            font.setColor(Color.WHITE);
            String craftingTabText = "クラフト";
            GlyphLayout craftingLayout = new GlyphLayout(font, craftingTabText);
            float craftingTextX = craftingTabButton.x + (craftingTabButton.width - craftingLayout.width) / 2;
            float craftingTextY = craftingTabButton.y + craftingTabButton.height / 2 + craftingLayout.height / 2;
            font.draw(batch, craftingTabText, craftingTextX, craftingTextY);
        } else {
            craftingTabButton.updateAndRender(mouseX, mouseY);
        }
        
        font.getData().setScale(0.825f);
    }
    
    /**
     * スクロール処理を行います。
     */
    public void handleScroll(float amountY) {
        if (scrollBar != null) {
            scrollBar.handleScroll(amountY);
        }
    }
    
    /**
     * スクロールバーのドラッグ入力を処理します。
     */
    public void handleScrollBarDragInput() {
        if (scrollBar != null) {
            scrollBar.handleScrollBarDragInput();
        }
    }
    
    /**
     * インベントリタブの内容を描画します。
     */
    private void renderInventoryTab(Inventory inventory, ItemDataLoader itemDataLoader, float titleY) {
        // コンテンツの高さを計算
        float totalItems = inventory.getAllItems().size();
        float totalRows = (float)Math.ceil(totalItems / SLOTS_PER_ROW);
        float totalContentHeight = totalRows * (SLOT_SIZE + SLOT_PADDING) - SLOT_PADDING;
        
        if (scrollBar != null) {
            scrollBar.setTotalContentHeight(totalContentHeight);
        }
        
        float startX = panelX + 30;
        // HelpUIと同じロジック：コンテンツエリアの上部から開始し、scrollOffsetを加算
        // スロットは上から下に配置するので、最初のスロットのY座標を計算
        float scrollOffset = scrollBar != null ? scrollBar.getScrollOffset() : 0;
        float startY = contentAreaY + contentAreaHeight - 20 + scrollOffset;
        float currentY = startY;
        
        Map<Integer, Integer> items = inventory.getAllItems();
        int itemIndex = 0;
        
        // スロット情報をリセット
        slotInfos = new ArrayList<>();
        
        if (items.isEmpty()) {
            // 空のインベントリメッセージを表示
            UITextHelper.drawEmptyStateMessage(batch, font, "Inventory is empty", 
                                              panelX, panelY, panelWidth, panelHeight, null);
        } else {
            // クリッピング領域を設定（コンテンツエリアのみ描画）
            batch.flush();
            Rectangle scissors = new Rectangle();
            Rectangle clipBounds = new Rectangle(
                panelX + 30, contentAreaY, panelWidth - 60, contentAreaHeight
            );
            ScissorStack.calculateScissors(uiCamera, batch.getTransformMatrix(), clipBounds, scissors);
            boolean scissorsPushed = ScissorStack.pushScissors(scissors);
            
            for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
                
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
            
            // クリッピングを解除
            batch.flush();
            if (scissorsPushed) {
                ScissorStack.popScissors();
            }
        }
        
        // スクロール可能な場合、スクロールバーを描画
        if (scrollBar != null && scrollBar.getScrollOffset() >= 0) {
            scrollBar.render();
        }
    }
    
    /**
     * クラフトタブの内容を描画します。
     */
    private void renderCraftingTab(Inventory inventory, ItemDataLoader itemDataLoader, float titleY) {
        if (craftingSystem == null || itemDataLoader == null) {
            return;
        }
        
        // クラフト可能なアイテムを取得
        List<ItemData> craftableItems = new ArrayList<>();
        for (ItemData itemData : itemDataLoader.getAllItems()) {
            if (itemData.isCraftable()) {
                craftableItems.add(itemData);
            }
        }
        
        // コンテンツの高さを計算
        float totalItems = craftableItems.size();
        float totalRows = (float)Math.ceil(totalItems / SLOTS_PER_ROW);
        float totalContentHeight = totalRows * (SLOT_SIZE + SLOT_PADDING) - SLOT_PADDING;
        
        if (scrollBar != null) {
            scrollBar.setTotalContentHeight(totalContentHeight);
        }
        
        float startX = panelX + 30;
        // HelpUIと同じロジック：コンテンツエリアの上部から開始し、scrollOffsetを加算
        // スロットは上から下に配置するので、最初のスロットのY座標を計算
        float scrollOffset = scrollBar != null ? scrollBar.getScrollOffset() : 0;
        float startY = contentAreaY + contentAreaHeight - 20 + scrollOffset;
        float currentY = startY;
        
        // スロット情報をリセット
        craftSlotInfos = new ArrayList<>();
        
        if (craftableItems.isEmpty()) {
            // クラフト可能なアイテムがないメッセージを表示
            UITextHelper.drawEmptyStateMessage(batch, font, "クラフト可能なアイテムがありません", 
                                              panelX, panelY, panelWidth, panelHeight, null);
        } else {
            // クリッピング領域を設定（コンテンツエリアのみ描画）
            batch.flush();
            Rectangle scissors = new Rectangle();
            Rectangle clipBounds = new Rectangle(
                panelX + 30, contentAreaY, panelWidth - 60, contentAreaHeight
            );
            ScissorStack.calculateScissors(uiCamera, batch.getTransformMatrix(), clipBounds, scissors);
            boolean scissorsPushed = ScissorStack.pushScissors(scissors);
            
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
            
            // クリッピングを解除
            batch.flush();
            if (scissorsPushed) {
                ScissorStack.popScissors();
            }
        }
        
        // スクロール可能な場合、スクロールバーを描画
        if (scrollBar != null && scrollBar.getScrollOffset() >= 0) {
            scrollBar.render();
        }
    }
    
}
