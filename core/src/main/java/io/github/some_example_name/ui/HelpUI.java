package io.github.some_example_name.ui;

import io.github.some_example_name.manager.LivestockDataLoader;

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

/**
 * ヘルプ/ガイドUIを描画するクラス。
 */
public class HelpUI {
    /**
     * ガイドの状態を表すenum
     */
    public enum GuideState {
        MENU,           // メニュー画面
        CONTROLS,       // 操作方法
        FARMING,        // 農業
        LIVESTOCK,      // 家畜
        TERRAIN,        // 地形
        OTHER_FEATURES, // その他の機能
        BUILDINGS,      // 建造物
        ENDING,         // エンディング
        GATHERING,      // 採集
        MINING          // 採掘
    }
    
    /**
     * サブメニューの状態を表すクラス
     */
    private static class SubMenuState {
        GuideState category;
        String selectedSection;
        
        SubMenuState(GuideState category) {
            this.category = category;
            this.selectedSection = null;
        }
    }
    
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    private int screenWidth;
    private int screenHeight;
    
    // 現在の状態
    private GuideState currentState = GuideState.MENU;
    private SubMenuState subMenuState = null; // サブメニューの状態（nullの場合はメインメニューまたはドキュメント表示）
    
    // UIのサイズと位置
    private float panelWidth = 1200;
    private float panelHeight = 900;
    private float panelX;
    private float panelY;
    
    // ウィンドウ（共通化）
    private UIWindow window;
    
    // ヘッダーエリア（タイトル用）
    private float headerY;
    private float headerHeight;
    
    // コンテンツエリア（タイトルとフッターの間）
    private float contentAreaY;
    private float contentAreaHeight;
    
    // フッターエリア（戻るボタン用）
    private float footerY;
    private float footerHeight;
    
    // スクロール位置
    private float scrollOffset = 0;
    private static final float SCROLL_SPEED = 30f;
    private float maxScrollOffset = 0;
    private float totalContentHeight = 0; // 実際のコンテンツの高さ

    // スクロールバーのドラッグ状態
    private boolean isDraggingScrollThumb = false;
    private float scrollThumbGrabOffsetY = 0f; // つまみ内で掴んだ位置（Y）
    
    // ボタン（UIButtonを使用）
    private UIButton backButton;
    private UIButton controlsButton;
    private UIButton farmingButton;
    private UIButton livestockButton;
    private UIButton terrainButton;
    private UIButton otherFeaturesButton;
    private UIButton buildingsButton;
    private UIButton endingButton;
    private UIButton gatheringButton;
    private UIButton miningButton;
    
    // サブメニューボタン（動的に生成）
    private List<UIButton> subMenuButtons = new ArrayList<UIButton>();
    private List<String> subMenuItems = new ArrayList<String>();
    
    // サウンドマネージャー
    private io.github.some_example_name.system.SoundManager soundManager;
    
    public HelpUI(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                 OrthographicCamera uiCamera, int screenWidth, int screenHeight) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.uiCamera = uiCamera;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        updatePanelPosition();
        initializeButtonResources();
    }
    
    /**
     * ボタンとウィンドウの描画リソースを初期化します。
     */
    private void initializeButtonResources() {
        // ウィンドウを作成
        window = new UIWindow(panelX, panelY, panelWidth, panelHeight);
        window.setRenderResources(shapeRenderer, batch, font, uiCamera);
        window.setHeader(headerY, headerHeight);
        window.setTitle("ゲームガイド");
        window.setTitleFontSize(1.0f);
    }
    
    /**
     * ボタンに描画リソースを設定します。
     */
    private void setupButtonResources(UIButton button) {
        if (button != null) {
            button.setRenderResources(shapeRenderer, batch, font, uiCamera);
            button.setSoundManager(soundManager);
        }
    }
    
    /**
     * サウンドマネージャーを設定します。
     */
    public void setSoundManager(io.github.some_example_name.system.SoundManager soundManager) {
        this.soundManager = soundManager;
        // すべてのボタンにサウンドマネージャーを設定
        if (backButton != null) backButton.setSoundManager(soundManager);
        if (controlsButton != null) controlsButton.setSoundManager(soundManager);
        if (gatheringButton != null) gatheringButton.setSoundManager(soundManager);
        if (miningButton != null) miningButton.setSoundManager(soundManager);
        if (terrainButton != null) terrainButton.setSoundManager(soundManager);
        if (farmingButton != null) farmingButton.setSoundManager(soundManager);
        if (livestockButton != null) livestockButton.setSoundManager(soundManager);
        if (buildingsButton != null) buildingsButton.setSoundManager(soundManager);
        if (otherFeaturesButton != null) otherFeaturesButton.setSoundManager(soundManager);
        if (endingButton != null) endingButton.setSoundManager(soundManager);
        for (UIButton button : subMenuButtons) {
            button.setSoundManager(soundManager);
        }
    }
    
    /**
     * スクロール位置をリセットします。
     */
    public void resetScroll() {
        scrollOffset = 0; // 最上部から開始
    }
    
    /**
     * ヘルプ画面が開かれたときに呼び出されます。
     */
    public void onOpen() {
        resetScroll();
        currentState = GuideState.MENU; // メニュー画面に戻る
        subMenuState = null;
    }
    
    /**
     * サブメニューを開きます。
     * @param category カテゴリ
     */
    private void openSubMenu(GuideState category) {
        subMenuState = new SubMenuState(category);
        currentState = category;
        resetScroll();
        
        // サブメニュー項目を取得（livestockDataLoaderはrenderメソッドで取得）
        // ここではnullを渡し、実際の取得はrenderメソッドで行う
        subMenuItems = new ArrayList<String>();
        subMenuButtons.clear();
    }
    
    /**
     * パネルの位置を更新します（画面中央に配置）。
     */
    private void updatePanelPosition() {
        panelX = (screenWidth - panelWidth) / 2;
        panelY = (screenHeight - panelHeight) / 2;
        
        // エリアの設定
        float bodyPadding = 30;
        float buttonHeight = 75;
        float footerPadding = 20;
        
        // ヘッダーエリアの設定（上部）
        headerHeight = 80;
        headerY = panelY + panelHeight - headerHeight;
        
        // フッターエリアの設定（ウィンドウ下端に接地）
        // LibGDXではY=0が下部、Y=screenHeightが上部なので、
        // panelYが下部、panelY + panelHeightが上部
        footerHeight = buttonHeight + footerPadding * 2;
        footerY = panelY; // フッター下辺 = ウィンドウ下辺
        
        // コンテンツエリアの設定（ガイド表示用、ヘッダーとフッターの間）
        contentAreaY = footerY + footerHeight + bodyPadding;
        contentAreaHeight = headerY - contentAreaY - bodyPadding;
        
        // 戻るボタンの位置を設定（フッターエリア内に配置、下部）
        float buttonWidth = 300;
        float buttonX = panelX + 20;
        float buttonY = footerY + footerPadding;
        backButton = new UIButton(buttonX, buttonY, buttonWidth, buttonHeight, "戻る");
        setupButtonResources(backButton);
        
        // メニューボタンの位置を設定（ボディエリア内に配置）
        float menuButtonWidth = 500;
        float menuButtonHeight = 80;
        float menuButtonSpacing = 20;
        float menuStartX = panelX + (panelWidth - menuButtonWidth) / 2;
        // ボタンをヘッダーの下から配置（上から下へ）
        float menuStartY = headerY - bodyPadding - menuButtonHeight;
        
        // ボタンの順番: 操作方法 → 採集 → 採掘 → 地形 → 農業 → 家畜 → 建造物 → その他の機能 → エンディング
        controlsButton = new UIButton(menuStartX, menuStartY, menuButtonWidth, menuButtonHeight, "操作方法");
        gatheringButton = new UIButton(menuStartX, menuStartY - (menuButtonHeight + menuButtonSpacing), menuButtonWidth, menuButtonHeight, "採集");
        miningButton = new UIButton(menuStartX, menuStartY - (menuButtonHeight + menuButtonSpacing) * 2, menuButtonWidth, menuButtonHeight, "採掘");
        terrainButton = new UIButton(menuStartX, menuStartY - (menuButtonHeight + menuButtonSpacing) * 3, menuButtonWidth, menuButtonHeight, "地形");
        farmingButton = new UIButton(menuStartX, menuStartY - (menuButtonHeight + menuButtonSpacing) * 4, menuButtonWidth, menuButtonHeight, "農業");
        livestockButton = new UIButton(menuStartX, menuStartY - (menuButtonHeight + menuButtonSpacing) * 5, menuButtonWidth, menuButtonHeight, "家畜");
        buildingsButton = new UIButton(menuStartX, menuStartY - (menuButtonHeight + menuButtonSpacing) * 6, menuButtonWidth, menuButtonHeight, "建造物");
        otherFeaturesButton = new UIButton(menuStartX, menuStartY - (menuButtonHeight + menuButtonSpacing) * 7, menuButtonWidth, menuButtonHeight, "その他の機能");
        endingButton = new UIButton(menuStartX, menuStartY - (menuButtonHeight + menuButtonSpacing) * 8, menuButtonWidth, menuButtonHeight, "エンディング");
        
        // すべてのボタンにリソースを設定
        setupButtonResources(controlsButton);
        setupButtonResources(gatheringButton);
        setupButtonResources(miningButton);
        setupButtonResources(terrainButton);
        setupButtonResources(farmingButton);
        setupButtonResources(livestockButton);
        setupButtonResources(buildingsButton);
        setupButtonResources(otherFeaturesButton);
        setupButtonResources(endingButton);
    }
    
    /**
     * マウスクリックを処理します。
     * @param screenX スクリーンX座標（LibGDXの座標系、左上が原点）
     * @param screenY スクリーンY座標（LibGDXの座標系、左上が原点）
     * @return 戻るボタンがクリックされた場合true（ゲームを終了する必要がある場合）
     */
    public boolean handleClick(int screenX, int screenY) {
        // スクリーン座標をUI座標に変換（LibGDXはY座標が下から上）
        float uiY = screenHeight - screenY;
        
        if (currentState == GuideState.MENU) {
            // メニュー画面の場合、ボタンの位置をスクロールオフセットに応じて更新
            float menuButtonHeight = 80;
            float menuButtonSpacing = 20;
            float menuStartY = headerY - 30 - menuButtonHeight;
            
            // ボタンの順番: 操作方法 → 採集 → 採掘 → 地形 → 農業 → 家畜 → 建造物 → その他の機能 → エンディング
            
            // ボタンの位置を更新してクリック判定
            if (controlsButton != null) {
                controlsButton.y = menuStartY + scrollOffset;
                if (controlsButton.contains((float)screenX, uiY)) {
                    openSubMenu(GuideState.CONTROLS);
                    return false;
                }
            }
            if (gatheringButton != null) {
                gatheringButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) + scrollOffset;
                if (gatheringButton.contains((float)screenX, uiY)) {
                    openSubMenu(GuideState.GATHERING);
                    return false;
                }
            }
            if (miningButton != null) {
                miningButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 2 + scrollOffset;
                if (miningButton.contains((float)screenX, uiY)) {
                    openSubMenu(GuideState.MINING);
                    return false;
                }
            }
            if (terrainButton != null) {
                terrainButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 3 + scrollOffset;
                if (terrainButton.contains((float)screenX, uiY)) {
                    openSubMenu(GuideState.TERRAIN);
                    return false;
                }
            }
            if (farmingButton != null) {
                farmingButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 4 + scrollOffset;
                if (farmingButton.contains((float)screenX, uiY)) {
                    openSubMenu(GuideState.FARMING);
                    return false;
                }
            }
            if (livestockButton != null) {
                livestockButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 5 + scrollOffset;
                if (livestockButton.contains((float)screenX, uiY)) {
                    openSubMenu(GuideState.LIVESTOCK);
                    return false;
                }
            }
            if (buildingsButton != null) {
                buildingsButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 6 + scrollOffset;
                if (buildingsButton.contains((float)screenX, uiY)) {
                    openSubMenu(GuideState.BUILDINGS);
                    return false;
                }
            }
            if (otherFeaturesButton != null) {
                otherFeaturesButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 7 + scrollOffset;
                if (otherFeaturesButton.contains((float)screenX, uiY)) {
                    openSubMenu(GuideState.OTHER_FEATURES);
                    return false;
                }
            }
            if (endingButton != null) {
                endingButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 8 + scrollOffset;
                if (endingButton.contains((float)screenX, uiY)) {
                    openSubMenu(GuideState.ENDING);
                    return false;
                }
            }
            // メニュー画面での戻るボタンのクリック判定（ゲームガイドを閉じる）
            if (backButton != null && backButton.contains((float)screenX, uiY)) {
                return true; // ゲームガイドを閉じる
            }
        } else if (subMenuState != null && subMenuState.selectedSection == null) {
            // サブメニュー画面の場合
            float menuButtonHeight = 80;
            float menuButtonSpacing = 20;
            float menuStartY = headerY - 30 - menuButtonHeight;
            
            for (int i = 0; i < subMenuButtons.size(); i++) {
                UIButton button = subMenuButtons.get(i);
                button.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * i + scrollOffset;
                if (button.contains((float)screenX, uiY)) {
                    subMenuState.selectedSection = subMenuItems.get(i);
                    resetScroll();
                    return false;
                }
            }
            
            // サブメニュー画面での戻るボタンのクリック判定
            if (backButton != null && backButton.contains((float)screenX, uiY)) {
                subMenuState = null;
                currentState = GuideState.MENU;
                resetScroll();
                return false;
            }
        } else {
            // ドキュメント表示画面での戻るボタンのクリック判定
            if (backButton != null && backButton.contains((float)screenX, uiY)) {
                if (subMenuState != null) {
                    // サブメニューに戻る
                    subMenuState.selectedSection = null;
                } else {
                    // メインメニューに戻る
                    currentState = GuideState.MENU;
                }
                resetScroll();
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * スクロール処理を行います。
     * @param amountY スクロール量（LibGDXでは amountY > 0 が上スクロール、amountY < 0 が下スクロール）
     */
    public void handleScroll(float amountY) {
        // amountY > 0 のとき（上スクロール）は scrollOffset を増やす（コンテンツを上にスクロール、下のコンテンツが見える）
        // amountY < 0 のとき（下スクロール）は scrollOffset を減らす（コンテンツを下にスクロール、上のコンテンツが見える）
        scrollOffset += amountY * SCROLL_SPEED;
        // スクロール範囲を制限
        scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));
    }

    /**
     * スクロールバー（つまみ）のドラッグ入力を処理します。
     * MenuSystem側の毎フレーム入力処理から呼び出してください。
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
            // drawScrollBar() と同じマッピング：
            // thumbY = scrollBarY + (scrollBarHeight - thumbHeight) * (1 - scrollRatio)
            // => scrollRatio = 1 - (thumbY - scrollBarY) / trackRange
            float scrollRatio = 1f - ((newThumbY - m.scrollBarY) / trackRange);
            scrollOffset = scrollRatio * maxScrollOffset;
            scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));
        }
    }

    private static final class ScrollBarMetrics {
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

        m.scrollBarWidth = 10f;
        m.scrollBarX = panelX + panelWidth - m.scrollBarWidth - 8f;
        m.scrollBarHeight = contentAreaHeight;
        m.scrollBarY = contentAreaY;

        float safeTotalContentHeight = Math.max(1f, totalContentHeight);
        m.thumbHeight = Math.max(30f, contentAreaHeight * (contentAreaHeight / safeTotalContentHeight));

        float scrollRatio = maxScrollOffset > 0 ? scrollOffset / maxScrollOffset : 0f;
        m.thumbY = m.scrollBarY + (m.scrollBarHeight - m.thumbHeight) * (1.0f - scrollRatio);

        m.thumbX = m.scrollBarX + 1f;
        m.thumbWidth = m.scrollBarWidth - 2f;

        return m;
    }
    
    /**
     * コンテンツの高さを計算してスクロール範囲を設定します。
     */
    private void calculateContentHeight(LivestockDataLoader livestockDataLoader) {
        if (currentState == GuideState.MENU) {
            // メニュー画面の場合は別メソッドで計算
            calculateMenuContentHeight();
            return;
        }
        
        float lineSpacing = 35f;
        float totalHeight = 0;
        
        switch (currentState) {
            case CONTROLS:
            case FARMING:
            case LIVESTOCK:
            case TERRAIN:
            case OTHER_FEATURES:
            case BUILDINGS:
            case ENDING:
            case GATHERING:
            case MINING:
                // Markdownファイルから読み込んだ要素の高さを計算
                List<GuideContentLoader.GuideElement> elements = GuideContentLoader.loadGuideContent(currentState, livestockDataLoader);
                totalHeight = calculateGuideElementsHeight(elements, lineSpacing);
                break;
            default:
                totalHeight = 0;
                break;
        }
        
        // 実際のコンテンツの高さを保存
        totalContentHeight = totalHeight;
        
        // 最大スクロールオフセットを計算
        // コンテンツが表示エリアより大きい場合、その差分だけスクロール可能
        maxScrollOffset = Math.max(0, totalHeight - contentAreaHeight + 40); // パディング分も考慮
    }
    
    /**
     * ヘルプUIを描画します。
     */
    public void render(LivestockDataLoader livestockDataLoader) {
        // ウィンドウを描画（タイトルも含む）
        if (window != null) {
            window.render(true);
        }
        
        // 状態に応じて描画
        if (currentState == GuideState.MENU) {
            renderMenu();
        } else if (subMenuState != null && subMenuState.selectedSection == null) {
            // サブメニュー項目がまだ取得されていない場合は取得
            if (subMenuItems.isEmpty()) {
                subMenuItems = GuideContentLoader.getSubMenuItems(currentState, livestockDataLoader);
                // サブメニューボタンを作成
                subMenuButtons.clear();
                float menuButtonWidth = 500;
                float menuButtonHeight = 80;
                float menuStartX = panelX + (panelWidth - menuButtonWidth) / 2;
                
                for (int i = 0; i < subMenuItems.size(); i++) {
                    float buttonY = headerY - 30 - menuButtonHeight - (menuButtonHeight + 20) * i;
                    UIButton button = new UIButton(menuStartX, buttonY, menuButtonWidth, menuButtonHeight, subMenuItems.get(i));
                    setupButtonResources(button);
                    subMenuButtons.add(button);
                }
                
                // サブメニューが1つしかない場合は、直接ドキュメントを表示
                if (subMenuItems.size() == 1) {
                    subMenuState.selectedSection = subMenuItems.get(0);
                }
            }
            renderSubMenu();
        } else {
            renderGuide(livestockDataLoader);
        }
    }
    
    /**
     * メニュー画面を描画します。
     */
    private void renderMenu() {
        // メニューのコンテンツの高さを計算
        calculateMenuContentHeight();
        
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        // batchが既に開始されているかチェック（UIWindowがbatchを開始している可能性がある）
        boolean batchWasActive = batch.isDrawing();
        if (!batchWasActive) {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
        }
        
        // クリッピング領域を設定（コンテンツエリアのみ描画）
        batch.flush();
        batch.flush();
        Rectangle scissors = new Rectangle();
        Rectangle clipBounds = new Rectangle(
            panelX, contentAreaY, panelWidth, contentAreaHeight
        );
        ScissorStack.calculateScissors(uiCamera, batch.getTransformMatrix(), clipBounds, scissors);
        boolean scissorsPushed = ScissorStack.pushScissors(scissors);
        
        // ボタンをスクロールオフセットに応じて描画
        float menuButtonHeight = 80;
        float menuButtonSpacing = 20;
        float menuStartY = headerY - 30 - menuButtonHeight;
        
        // ボタンの順番: 操作方法 → 採集 → 採掘 → 地形 → 農業 → 家畜 → 建造物 → その他の機能 → エンディング
        // ボタンの位置を更新して描画（UIButtonがホバー状態の管理と描画を自動的に処理）
        if (controlsButton != null) {
            controlsButton.y = menuStartY + scrollOffset;
            controlsButton.updateAndRender(mouseX, mouseY);
        }
        if (gatheringButton != null) {
            gatheringButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) + scrollOffset;
            gatheringButton.updateAndRender(mouseX, mouseY);
        }
        if (miningButton != null) {
            miningButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 2 + scrollOffset;
            miningButton.updateAndRender(mouseX, mouseY);
        }
        if (terrainButton != null) {
            terrainButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 3 + scrollOffset;
            terrainButton.updateAndRender(mouseX, mouseY);
        }
        if (farmingButton != null) {
            farmingButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 4 + scrollOffset;
            farmingButton.updateAndRender(mouseX, mouseY);
        }
        if (livestockButton != null) {
            livestockButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 5 + scrollOffset;
            livestockButton.updateAndRender(mouseX, mouseY);
        }
        if (buildingsButton != null) {
            buildingsButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 6 + scrollOffset;
            buildingsButton.updateAndRender(mouseX, mouseY);
        }
        if (otherFeaturesButton != null) {
            otherFeaturesButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 7 + scrollOffset;
            otherFeaturesButton.updateAndRender(mouseX, mouseY);
        }
        if (endingButton != null) {
            endingButton.y = menuStartY - (menuButtonHeight + menuButtonSpacing) * 8 + scrollOffset;
            endingButton.updateAndRender(mouseX, mouseY);
        }
        
        // クリッピングを解除
        batch.flush();
        if (scissorsPushed) {
            ScissorStack.popScissors();
        }
        batch.end();
        
        // スクロール可能な場合、スクロールバーを描画
        if (maxScrollOffset > 0) {
            drawScrollBar();
        }
        
        // 戻るボタンを描画（フッター）
        if (backButton != null) {
            backButton.updateAndRender(mouseX, mouseY);
        }
    }
    
    /**
     * サブメニュー画面を描画します。
     */
    private void renderSubMenu() {
        // サブメニューのコンテンツの高さを計算
        calculateSubMenuContentHeight();
        
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        // batchが既に開始されているかチェック（UIWindowがbatchを開始している可能性がある）
        boolean batchWasActive = batch.isDrawing();
        if (!batchWasActive) {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
        }
        
        // クリッピング領域を設定（コンテンツエリアのみ描画）
        batch.flush();
        Rectangle scissors = new Rectangle();
        Rectangle clipBounds = new Rectangle(
            panelX, contentAreaY, panelWidth, contentAreaHeight
        );
        ScissorStack.calculateScissors(uiCamera, batch.getTransformMatrix(), clipBounds, scissors);
        boolean scissorsPushed = ScissorStack.pushScissors(scissors);
        
        // ボタンをスクロールオフセットに応じて描画
        float menuButtonHeight = 80;
        float menuButtonSpacing = 20;
        float menuStartY = headerY - 30 - menuButtonHeight;
        
        for (int i = 0; i < subMenuButtons.size(); i++) {
            UIButton button = subMenuButtons.get(i);
            float buttonY = menuStartY - (menuButtonHeight + menuButtonSpacing) * i + scrollOffset;
            button.y = buttonY;
            button.updateAndRender(mouseX, mouseY);
        }
        
        // クリッピングを解除
        batch.flush();
        if (scissorsPushed) {
            ScissorStack.popScissors();
        }
        
        // batchが元々開始されていなかった場合は終了する
        // （UIWindowが開始した場合は、UIWindowで終了を管理）
        if (!batchWasActive) {
            batch.end();
        }
        
        // スクロール可能な場合、スクロールバーを描画
        if (maxScrollOffset > 0) {
            drawScrollBar();
        }
        
        // 戻るボタンを描画（フッター）
        if (backButton != null) {
            backButton.updateAndRender(mouseX, mouseY);
        }
    }
    
    /**
     * サブメニューのコンテンツの高さを計算します。
     */
    private void calculateSubMenuContentHeight() {
        float menuButtonHeight = 80;
        float menuButtonSpacing = 20;
        float totalButtons = subMenuItems.size();
        float totalHeight = totalButtons * menuButtonHeight + (totalButtons - 1) * menuButtonSpacing;
        
        totalContentHeight = totalHeight;
        maxScrollOffset = Math.max(0, totalHeight - contentAreaHeight + 40);
    }
    
    /**
     * メニューのコンテンツの高さを計算します。
     */
    private void calculateMenuContentHeight() {
        float menuButtonHeight = 80;
        float menuButtonSpacing = 20;
        float totalButtons = 9; // 操作方法、農業、家畜、地形、その他の機能、建造物、エンディング、採集、採掘
        float totalHeight = totalButtons * menuButtonHeight + (totalButtons - 1) * menuButtonSpacing;
        
        totalContentHeight = totalHeight;
        maxScrollOffset = Math.max(0, totalHeight - contentAreaHeight + 40);
    }
    
    /**
     * 各ガイド画面を描画します。
     */
    private void renderGuide(LivestockDataLoader livestockDataLoader) {
        // コンテンツの高さを計算
        calculateContentHeight(livestockDataLoader);
        
        // batchが既に開始されているかチェック（UIWindowがbatchを開始している可能性がある）
        boolean batchWasActive = batch.isDrawing();
        if (!batchWasActive) {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
        }
        
        // クリッピング領域を設定（コンテンツエリアのみ描画）
        batch.flush();
        Rectangle scissors = new Rectangle();
        Rectangle clipBounds = new Rectangle(
            panelX, contentAreaY, panelWidth, contentAreaHeight
        );
        ScissorStack.calculateScissors(uiCamera, batch.getTransformMatrix(), clipBounds, scissors);
        boolean scissorsPushed = ScissorStack.pushScissors(scissors);
        
        // コンテンツを描画
        float startX = panelX + 40;
        float startY = contentAreaY + contentAreaHeight - 20 + scrollOffset;
        float lineSpacing = 35f;
        
        font.getData().setScale(0.7f);
        font.setColor(Color.WHITE);
        
        // Markdownファイルからガイドコンテンツを読み込んで描画
        String sectionTitle = (subMenuState != null && subMenuState.selectedSection != null) 
            ? subMenuState.selectedSection : null;
        List<GuideContentLoader.GuideElement> elements = GuideContentLoader.loadGuideContent(currentState, livestockDataLoader, sectionTitle);
        renderGuideElements(batch, elements, startX, startY, lineSpacing);
        
        font.getData().setScale(0.825f);
        font.setColor(Color.WHITE);
        
        // クリッピングを解除
        batch.flush();
        if (scissorsPushed) {
            ScissorStack.popScissors();
        }
        
        // batchが元々開始されていなかった場合は終了する
        // （UIWindowが開始した場合は、UIWindowで終了を管理）
        if (!batchWasActive) {
            batch.end();
        }
        
        // スクロール可能な場合、スクロールバーを描画
        if (maxScrollOffset > 0) {
            drawScrollBar();
        }
        
        // 戻るボタンを描画（フッター）
        if (backButton != null) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            backButton.updateAndRender(mouseX, mouseY);
        }
    }
    
    
    /**
     * ガイド要素のリストを描画します。
     * @param batch SpriteBatch
     * @param elements ガイド要素のリスト
     * @param startX 開始X座標
     * @param startY 開始Y座標
     * @param lineSpacing 行間隔
     */
    private void renderGuideElements(SpriteBatch batch, List<GuideContentLoader.GuideElement> elements, 
                                     float startX, float startY, float lineSpacing) {
        float currentY = startY;
        
        for (GuideContentLoader.GuideElement element : elements) {
            switch (element.type) {
                case HEADING_1:
                    font.setColor(new Color(0.8f, 0.9f, 1.0f, 1f));
                    font.getData().setScale(0.75f);
                    String heading1Text = "【" + element.text + "】";
                    GlyphLayout heading1Layout = new GlyphLayout(font, heading1Text);
                    font.draw(batch, heading1Text, startX, currentY);
                    // 見出し1の実際の高さを考慮してcurrentYを更新
                    currentY -= Math.max(lineSpacing, heading1Layout.height + 10);
                    break;
                    
                case HEADING_2:
                    // 作物名に応じて色を変更
                    Color heading2Color = getHeading2Color(element.text);
                    font.setColor(heading2Color);
                    font.getData().setScale(0.75f);
                    GlyphLayout heading2Layout = new GlyphLayout(font, element.text);
                    font.draw(batch, element.text, startX, currentY);
                    // 見出し2の実際の高さを考慮してcurrentYを更新
                    currentY -= Math.max(lineSpacing, heading2Layout.height + 10);
                    break;
                    
                case HEADING_3:
                    font.setColor(new Color(0.7f, 0.8f, 0.95f, 1f));
                    font.getData().setScale(0.7f);
                    font.draw(batch, element.text, startX, currentY);
                    currentY -= lineSpacing * 0.9f;
                    break;
                    
                case HEADING_4:
                    font.setColor(new Color(0.65f, 0.75f, 0.9f, 1f));
                    font.getData().setScale(0.65f);
                    font.draw(batch, element.text, startX, currentY);
                    currentY -= lineSpacing * 0.85f;
                    break;
                    
                case HEADING_5:
                    font.setColor(new Color(0.6f, 0.7f, 0.85f, 1f));
                    font.getData().setScale(0.6f);
                    font.draw(batch, element.text, startX, currentY);
                    currentY -= lineSpacing * 0.8f;
                    break;
                    
                case HEADING_6:
                    font.setColor(new Color(0.55f, 0.65f, 0.8f, 1f));
                    font.getData().setScale(0.6f);
                    font.draw(batch, element.text, startX, currentY);
                    currentY -= lineSpacing * 0.8f;
                    break;
                    
                case LIST_ITEM:
                    font.getData().setScale(0.6f);
                    font.setColor(Color.WHITE);
                    float indentX = startX + 20 + (element.indentLevel * 20);
                    int listItemLines = drawTextLine(batch, "・" + element.text, indentX, currentY);
                    currentY -= lineSpacing * 0.8f * listItemLines;
                    break;
                    
                case TEXT:
                    font.getData().setScale(0.6f);
                    font.setColor(Color.WHITE);
                    int textLines = drawTextLine(batch, element.text, startX, currentY);
                    currentY -= lineSpacing * 0.8f * textLines;
                    break;
                    
                case SEPARATOR:
                    currentY -= lineSpacing * 0.5f; // 区切り線の前後にスペース
                    break;
            }
        }
    }
    
    /**
     * HEADING_2のテキストに応じた色を取得します。
     * 作物名の場合は専用の色を返します。
     * @param text 見出しテキスト
     * @return 色
     */
    private Color getHeading2Color(String text) {
        // 作物名をチェック
        if (text.contains("米") || text.contains("稲作")) {
            return new Color(0.2f, 0.4f, 0.7f, 1f); // 青色
        } else if (text.contains("麦") || text.contains("小麦")) {
            return new Color(0.9f, 0.8f, 0.3f, 1f); // 黄色
        } else if (text.contains("芋") || text.contains("サツマイモ")) {
            return new Color(0.8f, 0.5f, 0.2f, 1f); // オレンジ色
        }
        // デフォルトの色
        return new Color(0.8f, 0.9f, 1.0f, 1f);
    }
    
    /**
     * ガイド要素のリストの高さを計算します。
     * @param elements ガイド要素のリスト
     * @param lineSpacing 行間隔
     * @return 合計の高さ
     */
    private float calculateGuideElementsHeight(List<GuideContentLoader.GuideElement> elements, float lineSpacing) {
        float totalHeight = 0;
        float maxWidth = panelWidth - 80;
        
        for (GuideContentLoader.GuideElement element : elements) {
            switch (element.type) {
                case HEADING_1:
                case HEADING_2:
                    totalHeight += lineSpacing;
                    break;
                case HEADING_3:
                    totalHeight += lineSpacing * 0.9f;
                    break;
                case HEADING_4:
                    totalHeight += lineSpacing * 0.85f;
                    break;
                case HEADING_5:
                case HEADING_6:
                    totalHeight += lineSpacing * 0.8f;
                    break;
                case LIST_ITEM:
                    // 折り返しを考慮して高さを計算
                    font.getData().setScale(0.6f);
                    String listText = "・" + element.text;
                    GlyphLayout listLayout = new GlyphLayout(font, listText);
                    int listLines = (int)Math.ceil(listLayout.width / maxWidth);
                    if (listLines < 1) listLines = 1;
                    totalHeight += lineSpacing * 0.8f * listLines;
                    break;
                case TEXT:
                    // 折り返しを考慮して高さを計算
                    font.getData().setScale(0.6f);
                    GlyphLayout textLayout = new GlyphLayout(font, element.text);
                    int textLines = (int)Math.ceil(textLayout.width / maxWidth);
                    if (textLines < 1) textLines = 1;
                    totalHeight += lineSpacing * 0.8f * textLines;
                    break;
                case SEPARATOR:
                    totalHeight += lineSpacing * 0.5f;
                    break;
            }
        }
        
        return totalHeight;
    }
    
    /**
     * スクロールバーを描画します。
     */
    private void drawScrollBar() {
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
    
    /**
     * テキストを1行描画します（長い場合は折り返し）。
     * @param batch SpriteBatch
     * @param text 描画するテキスト
     * @param x 開始X座標
     * @param y 開始Y座標
     * @return 使用した行数（折り返し後の行数）
     */
    private int drawTextLine(SpriteBatch batch, String text, float x, float y) {
        float maxWidth = panelWidth - 80;
        GlyphLayout layout = new GlyphLayout(font, text);
        
        if (layout.width > maxWidth) {
            // 長い場合は折り返し処理（簡易版）
            String[] words = text.split("");
            StringBuilder line = new StringBuilder();
            float currentX = x;
            float currentY = y;
            int lineCount = 0;
            
            for (String word : words) {
                String testLine = line.toString() + word;
                GlyphLayout testLayout = new GlyphLayout(font, testLine);
                if (testLayout.width > maxWidth && line.length() > 0) {
                    font.draw(batch, line.toString(), currentX, currentY);
                    currentY -= font.getLineHeight() * 0.8f;
                    lineCount++;
                    line = new StringBuilder(word);
                } else {
                    line.append(word);
                }
            }
            if (line.length() > 0) {
                font.draw(batch, line.toString(), currentX, currentY);
                lineCount++;
            }
            return lineCount;
        } else {
            font.draw(batch, text, x, y);
            return 1;
        }
    }
}
