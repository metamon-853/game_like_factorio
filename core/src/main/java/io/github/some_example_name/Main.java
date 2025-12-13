package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Player player;
    private ItemManager itemManager;
    
    // カメラとビューポート
    private OrthographicCamera camera;
    private Viewport viewport;
    
    // UI用のカメラ（画面座標系）
    private OrthographicCamera uiCamera;
    
    // ポーズ状態
    private boolean isPaused;
    
    // グリッド表示フラグ（デフォルトはオン）
    private boolean showGrid = true;
    
    // ゲームの論理的な画面サイズ（ピクセル単位）- 基準サイズ
    private static final float BASE_VIEWPORT_SIZE = 20 * Player.TILE_SIZE;
    
    // 画面サイズ
    private int screenWidth;
    private int screenHeight;
    
    // 現在のビューポートサイズ（正方形を保つため動的に調整）
    private float viewportWidth;
    private float viewportHeight;
    
    // ズーム関連
    private float cameraZoom = 1.0f; // 現在のズームレベル（1.0が基準）
    private static final float MIN_ZOOM = 0.3f; // 最小ズーム（縮小の限界）
    private static final float MAX_ZOOM = 3.0f; // 最大ズーム（拡大の限界）
    private static final float ZOOM_SPEED = 0.1f; // ズームの速度
    
    // ボタン関連
    private static class Button {
        float x, y, width, height;
        
        Button(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        boolean contains(float screenX, float screenY) {
            return screenX >= x && screenX <= x + width && screenY >= y && screenY <= y + height;
        }
    }
    
    @Override
    public void create() {
        // 画面サイズを取得
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        
        // 画面のアスペクト比を計算
        float screenAspect = (float)screenWidth / (float)screenHeight;
        
        // 正方形の升を保つため、画面のアスペクト比に応じてビューポートサイズを調整
        if (screenAspect > 1.0f) {
            // 横長の画面：高さを基準にして幅を調整
            viewportHeight = BASE_VIEWPORT_SIZE;
            viewportWidth = BASE_VIEWPORT_SIZE * screenAspect;
        } else {
            // 縦長の画面：幅を基準にして高さを調整
            viewportWidth = BASE_VIEWPORT_SIZE;
            viewportHeight = BASE_VIEWPORT_SIZE / screenAspect;
        }
        
        // カメラとビューポートを初期化
        camera = new OrthographicCamera();
        camera.setToOrtho(false, viewportWidth, viewportHeight);
        viewport = new StretchViewport(viewportWidth, viewportHeight, camera);
        viewport.update(screenWidth, screenHeight);
        camera.update();
        
        // UI用のカメラを初期化（画面座標系）
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();
        
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.0f); // フォントサイズを大きく
        font.setColor(Color.WHITE);
        
        // プレイヤーを原点に配置（無限マップなので任意の位置から開始可能）
        player = new Player(0, 0);
        // アイテムマネージャーを初期化（無限マップ対応）
        itemManager = new ItemManager();
        // ポーズ状態を初期化
        isPaused = false;
        
        // カメラをプレイヤーの初期位置に設定
        float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
        float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
        camera.position.set(playerCenterX, playerCenterY, 0);
        camera.zoom = cameraZoom;
        camera.update();
        
        // マウススクロール入力を処理するInputProcessorを設定
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                // スクロール量に応じてズームを変更
                // amountY > 0 は上スクロール（縮小）、amountY < 0 は下スクロール（拡大）
                float zoomChange = amountY * ZOOM_SPEED;
                cameraZoom += zoomChange;
                
                // ズームの範囲を制限
                cameraZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, cameraZoom));
                
                // カメラのズームを更新
                camera.zoom = cameraZoom;
                camera.update();
                
                return true; // イベントを処理したことを示す
            }
        });
    }

    @Override
    public void render() {
        // 画面サイズが変更された場合、ビューポートを更新
        if (screenWidth != Gdx.graphics.getWidth() || screenHeight != Gdx.graphics.getHeight()) {
            screenWidth = Gdx.graphics.getWidth();
            screenHeight = Gdx.graphics.getHeight();
            viewport.update(screenWidth, screenHeight);
            camera.update();
        }
        
        // ビューポートを適用
        viewport.apply();
        
        // 画面をクリア
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        
        // ESCキーでポーズ/再開を切り替え
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused;
        }
        
        // ポーズ中にGキーでグリッド表示を切り替え
        if (isPaused && Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            showGrid = !showGrid;
        }
        
        // ポーズ中にQキーでゲーム終了
        if (isPaused && Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            Gdx.app.exit();
        }
        
        // ポーズ中にマウスクリックを処理
        if (isPaused) {
            handlePauseMenuClick();
        }
        
        // ポーズ中でない場合のみゲームを更新
        if (!isPaused) {
            // プレイヤーを更新
            float deltaTime = Gdx.graphics.getDeltaTime();
            player.update(deltaTime);
            
            // アイテムマネージャーを更新（カメラの視野範囲を渡す）
            itemManager.update(deltaTime, player, camera);
            
            // キーボード入力処理
            handleInput();
            
            // カメラをプレイヤーの位置に追従させる
            float playerCenterX = player.getPixelX() + Player.PLAYER_TILE_SIZE / 2;
            float playerCenterY = player.getPixelY() + Player.PLAYER_TILE_SIZE / 2;
            camera.position.set(playerCenterX, playerCenterY, 0);
        }
        
        // カメラのズームを適用（スクロールで変更された可能性があるため）
        camera.zoom = cameraZoom;
        
        // カメラを更新
        camera.update();
        
        // カメラのプロジェクション行列を設定
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        
        // グリッドを描画（Lineモード）- 表示フラグがオンの場合のみ
        if (showGrid) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            drawGrid();
            shapeRenderer.end();
        }
        
        // その他の描画（Filledモード）
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // アイテムを描画
        itemManager.render(shapeRenderer);
        
        // プレイヤーを描画
        player.render(shapeRenderer);
        
        shapeRenderer.end();
        
        // UI情報を描画（取得アイテム数など）
        drawUI();
        
        // ポーズメニューを描画（テキスト表示用）
        if (isPaused) {
            drawPauseMenu();
        }
    }
    
    /**
     * キーボード入力を処理します。
     */
    private void handleInput() {
        // 移動中は新しい入力を無視
        if (player.isMoving()) {
            return;
        }
        
        // 各方向のキーが押されているかチェック
        boolean up = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        
        // 斜め移動を優先的にチェック
        if (up && right) {
            // 右上
            player.move(1, 1);
        } else if (up && left) {
            // 左上
            player.move(-1, 1);
        } else if (down && right) {
            // 右下
            player.move(1, -1);
        } else if (down && left) {
            // 左下
            player.move(-1, -1);
        } else if (up) {
            // 上
            player.move(0, 1);
        } else if (down) {
            // 下
            player.move(0, -1);
        } else if (left) {
            // 左
            player.move(-1, 0);
        } else if (right) {
            // 右
            player.move(1, 0);
        }
    }
    
    /**
     * グリッドを描画します（カメラの視野範囲に基づいて動的に生成）。
     * マップ升（太い線）とプレイヤー升（細い線）の両方を描画します。
     */
    private void drawGrid() {
        int mapTileSize = Player.MAP_TILE_SIZE;
        int playerTileSize = Player.PLAYER_TILE_SIZE;
        
        // カメラの視野範囲を計算（ズームを考慮）
        float actualViewportWidth = camera.viewportWidth * camera.zoom;
        float actualViewportHeight = camera.viewportHeight * camera.zoom;
        float cameraLeft = camera.position.x - actualViewportWidth / 2;
        float cameraRight = camera.position.x + actualViewportWidth / 2;
        float cameraBottom = camera.position.y - actualViewportHeight / 2;
        float cameraTop = camera.position.y + actualViewportHeight / 2;
        
        // マージンを追加して少し広めに描画（見切れを防ぐ）
        float margin = mapTileSize * 2;
        
        // まず、プレイヤー升の細かいグリッドを描画（薄い色）
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f); // 薄いグレー
        
        int startPlayerTileX = (int)Math.floor((cameraLeft - margin) / playerTileSize);
        int endPlayerTileX = (int)Math.ceil((cameraRight + margin) / playerTileSize);
        int startPlayerTileY = (int)Math.floor((cameraBottom - margin) / playerTileSize);
        int endPlayerTileY = (int)Math.ceil((cameraTop + margin) / playerTileSize);
        
        // プレイヤー升の縦線を描画
        for (int x = startPlayerTileX; x <= endPlayerTileX; x++) {
            float lineX = x * playerTileSize;
            shapeRenderer.line(lineX, startPlayerTileY * playerTileSize, lineX, endPlayerTileY * playerTileSize);
        }
        
        // プレイヤー升の横線を描画
        for (int y = startPlayerTileY; y <= endPlayerTileY; y++) {
            float lineY = y * playerTileSize;
            shapeRenderer.line(startPlayerTileX * playerTileSize, lineY, endPlayerTileX * playerTileSize, lineY);
        }
        
        // 次に、マップ升の太いグリッドを描画（濃い色）
        shapeRenderer.setColor(Color.DARK_GRAY);
        
        int startMapTileX = (int)Math.floor((cameraLeft - margin) / mapTileSize);
        int endMapTileX = (int)Math.ceil((cameraRight + margin) / mapTileSize);
        int startMapTileY = (int)Math.floor((cameraBottom - margin) / mapTileSize);
        int endMapTileY = (int)Math.ceil((cameraTop + margin) / mapTileSize);
        
        // マップ升の縦線を描画
        for (int x = startMapTileX; x <= endMapTileX; x++) {
            float lineX = x * mapTileSize;
            shapeRenderer.line(lineX, startMapTileY * mapTileSize, lineX, endMapTileY * mapTileSize);
        }
        
        // マップ升の横線を描画
        for (int y = startMapTileY; y <= endMapTileY; y++) {
            float lineY = y * mapTileSize;
            shapeRenderer.line(startMapTileX * mapTileSize, lineY, endMapTileX * mapTileSize, lineY);
        }
    }
    
    /**
     * UI情報（取得アイテム数など）を描画します。
     */
    private void drawUI() {
        // UIは画面座標系で描画（ちらつきを防ぐため）
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // フォントサイズをUI用に調整
        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
        
        // 画面右上の位置を計算（画面座標系）
        float padding = 20;
        float rightX = screenWidth - padding;
        float topY = screenHeight - padding;
        
        // 取得したアイテム数を表示（右上に配置）
        String itemText = "Items: " + itemManager.getCollectedCount();
        GlyphLayout itemLayout = new GlyphLayout(font, itemText);
        float itemX = rightX - itemLayout.width;
        font.draw(batch, itemText, itemX, topY);
        
        // 現在のアイテム数も表示（その下に配置）
        String currentItemText = "On Map: " + itemManager.getItemCount();
        GlyphLayout currentLayout = new GlyphLayout(font, currentItemText);
        float currentX = rightX - currentLayout.width;
        font.draw(batch, currentItemText, currentX, topY - itemLayout.height - 10);
        
        // フォントサイズを元に戻す
        font.getData().setScale(2.0f);
        
        batch.end();
    }
    
    /**
     * ポーズメニューのマウスクリックを処理します。
     */
    private void handlePauseMenuClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            // マウスの座標を取得（LibGDXの座標系はY軸が下から上なので変換が必要）
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            
            // ボタンの位置とサイズを計算（drawPauseMenuと同じ値を使用）
            float buttonWidth = 320;
            float buttonHeight = 65;
            float centerX = screenWidth / 2;
            float centerY = screenHeight / 2;
            float buttonSpacing = 80;
            
            // グリッド切り替えボタン
            float gridButtonY = centerY - 20;
            Button gridButton = new Button(centerX - buttonWidth / 2, gridButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            // ゲーム終了ボタン
            float quitButtonY = centerY - buttonSpacing - 20;
            Button quitButton = new Button(centerX - buttonWidth / 2, quitButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            // ボタンがクリックされたかを判定
            if (gridButton.contains(mouseX, mouseY)) {
                showGrid = !showGrid;
            } else if (quitButton.contains(mouseX, mouseY)) {
                Gdx.app.exit();
            }
        }
    }
    
    /**
     * ポーズメニューを描画します。
     */
    private void drawPauseMenu() {
        // テキストを描画（画面座標系で）
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // "PAUSED" テキストを中央に表示（大きく）
        font.getData().setScale(3.0f);
        font.setColor(Color.WHITE);
        String pausedText = "PAUSED";
        GlyphLayout pausedLayout = new GlyphLayout(font, pausedText);
        float pausedX = (screenWidth - pausedLayout.width) / 2;
        float pausedY = screenHeight / 2 + 100;
        font.draw(batch, pausedText, pausedX, pausedY);
        
        // 操作説明を表示
        font.getData().setScale(1.8f);
        String instructionText = "Press ESC to resume";
        GlyphLayout instructionLayout = new GlyphLayout(font, instructionText);
        float instructionX = (screenWidth - instructionLayout.width) / 2;
        float instructionY = screenHeight / 2 + 40;
        font.draw(batch, instructionText, instructionX, instructionY);
        
        // ボタンの位置とサイズを計算
        float buttonWidth = 320;
        float buttonHeight = 65;
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2;
        float buttonSpacing = 80;
        
        // グリッド切り替えボタンを描画
        float gridButtonY = centerY - 20;
        drawButton(centerX - buttonWidth / 2, gridButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Toggle Grid: " + (showGrid ? "ON" : "OFF"));
        
        // ゲーム終了ボタンを描画
        float quitButtonY = centerY - buttonSpacing - 20;
        drawButton(centerX - buttonWidth / 2, quitButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Quit Game");
        
        font.getData().setScale(2.0f); // 元に戻す
        
        batch.end();
    }
    
    /**
     * ボタンを描画します。
     * 注意: このメソッドはbatch.begin()が既に呼ばれている状態で呼び出す必要があります。
     */
    private void drawButton(float x, float y, float width, float height, String text) {
        // batchを一時的に終了してshapeRendererを使用
        batch.end();
        
        // ボタンの背景を描画
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 0.95f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        // ボタンの枠線を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        // batchを再開してテキストを描画
        batch.begin();
        font.getData().setScale(1.8f);
        font.setColor(Color.WHITE);
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = x + (width - layout.width) / 2;
        float textY = y + (height + layout.height) / 2;
        font.draw(batch, text, textX, textY);
    }

    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        
        // ExtendViewportは自動的にアスペクト比を調整します
        viewport.update(width, height);
        camera.update();
        
        // UI用カメラも更新
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
    }
    
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
