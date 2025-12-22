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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
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
    
    // ウィンドウ（共通化）
    private UIWindow window;
    
    // アイテムスロットの設定
    private static final int SLOTS_PER_ROW = 8;
    private static final int MAX_ROWS = 10;
    private static final float SLOT_SIZE = 105;
    private static final float SLOT_PADDING = 15;
    
    // アイテム詳細表示
    private ItemData selectedItemData = null;
    
    // アイテム詳細パネル（共通化）
    private ItemDetailPanel itemDetailPanel;
    
    // スロット情報を保持（クリック判定用）
    private List<SlotInfo> slotInfos;
    
    // インベントリに戻るボタン
    private UIButton backButton;
    
    // スクロールバー
    private UIScrollBar scrollBar;
    private float contentAreaY;
    private float contentAreaHeight;
    
    // サウンドマネージャー
    private SoundManager soundManager;
    
    // 前回のホバー状態を記録（音の重複再生を防ぐため）
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
        initializeWindow();
    }
    
    /**
     * ウィンドウを初期化します。
     */
    private void initializeWindow() {
        window = new UIWindow(panelX, panelY, panelWidth, panelHeight);
        window.setRenderResources(shapeRenderer, batch, font, uiCamera);
        window.setTitle("アイテム図鑑");
        window.setTitleFontSize(0.825f);
        
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
        
        // インベントリに戻るボタンの位置を設定
        float buttonWidth = 300;
        float buttonHeight = 75;
        float buttonX = panelX + 20;
        float buttonY = panelY + panelHeight - buttonHeight - 20;
        backButton = new UIButton(buttonX, buttonY, buttonWidth, buttonHeight, "インベントリに戻る");
        backButton.setRenderResources(shapeRenderer, batch, font, uiCamera);
        backButton.setSoundManager(soundManager);
        
        // アイテム詳細パネルの位置を更新
        if (itemDetailPanel != null) {
            itemDetailPanel.setPosition(panelX + panelWidth + 30, panelY);
        }
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
        if (selectedItemData != null && itemDetailPanel != null) {
            if (!itemDetailPanel.contains(screenX, uiY)) {
                selectedItemData = null;
            }
        }
        
        return false;
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
        if (itemDetailPanel != null && itemDetailPanel.contains(mouseX, mouseY)) {
            // 詳細パネルの上にマウスがある場合は何もしない
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
        
        // batchが既に開始されているかチェック（UIWindowがbatchを開始する可能性がある）
        boolean batchWasActive = batch.isDrawing();
        
        // ウィンドウを描画（タイトルも含む）
        if (window != null) {
            window.render(true);
        }
        
        // batchが既に開始されているか再チェック（UIWindowがbatchを開始/終了した可能性がある）
        if (!batch.isDrawing()) {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
        }
        
        font.getData().setScale(0.825f);
        font.setColor(Color.WHITE);
        
        // 戻るボタンを描画
        if (backButton != null) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            backButton.updateAndRender(mouseX, mouseY);
        }
        
        // アイテムリストを描画
        float startX = panelX + 30;
        
        Array<ItemData> allItems = itemDataLoader.getAllItems();
        
        // コンテンツの高さを計算
        float totalItems = allItems.size;
        float totalRows = (float)Math.ceil(totalItems / SLOTS_PER_ROW);
        float totalContentHeight = totalRows * (SLOT_SIZE + SLOT_PADDING) - SLOT_PADDING;
        
        if (scrollBar != null) {
            scrollBar.setTotalContentHeight(totalContentHeight);
        }
        
        // HelpUIと同じロジック：コンテンツエリアの上部から開始し、scrollOffsetを加算
        // スロットは上から下に配置するので、最初のスロットのY座標を計算
        float scrollOffset = scrollBar != null ? scrollBar.getScrollOffset() : 0;
        float startY = contentAreaY + contentAreaHeight - 20 + scrollOffset;
        float currentY = startY;
        
        // スロット情報をリセット
        slotInfos = new ArrayList<>();
        
        if (allItems.size == 0) {
            // 空のメッセージを表示
            UITextHelper.drawEmptyStateMessage(batch, font, "アイテムがありません", 
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
            for (ItemData itemData : allItems) {
                
                // スロットの位置を計算
                int row = itemIndex / SLOTS_PER_ROW;
                int col = itemIndex % SLOTS_PER_ROW;
                float slotX = startX + col * (SLOT_SIZE + SLOT_PADDING);
                float slotY = currentY - row * (SLOT_SIZE + SLOT_PADDING);
                
                // スロット情報を保存（クリック判定用）
                slotInfos.add(new SlotInfo(slotX, slotY - SLOT_SIZE, itemData));
                
                // ホバー中のアイテムかどうかで色を変える
                boolean isHovered = selectedItemData != null && selectedItemData.id == itemData.id;
                
                // スロットの背景を描画（batchを一時的に終了してShapeRendererを使用）
                batch.flush();
                batch.end();
                
                shapeRenderer.setProjectionMatrix(uiCamera.combined);
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
                
                // batchを再開
                batch.setProjectionMatrix(uiCamera.combined);
                batch.begin();
                
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
        
        // ホバー処理を実行（slotInfosが設定された後）
        handleHover();
        
        // アイテムホバー音を再生
        if (selectedItemData != null && selectedItemData != lastHoveredItem && soundManager != null) {
            soundManager.playHoverSound();
        }
        lastHoveredItem = selectedItemData;
        
        // アイテム詳細パネルを描画（ホバーまたはクリックで選択されたアイテム）
        if (selectedItemData != null && itemDetailPanel != null) {
            itemDetailPanel.render(selectedItemData);
        }
        
        font.getData().setScale(0.825f);
        
        // batchが元々開始されていなかった場合は終了する
        // （UIWindowが開始した場合は、UIWindowで終了を管理）
        if (!batchWasActive && batch.isDrawing()) {
            batch.end();
        }
    }
    
}
