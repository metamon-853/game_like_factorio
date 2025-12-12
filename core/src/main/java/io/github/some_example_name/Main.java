package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
    
    // グリッドのサイズ
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 15;
    
    // ゲームの論理的な画面サイズ（ピクセル単位）
    private static final float VIEWPORT_WIDTH = GRID_WIDTH * Player.TILE_SIZE;
    private static final float VIEWPORT_HEIGHT = GRID_HEIGHT * Player.TILE_SIZE;
    
    // 画面サイズ
    private int screenWidth;
    private int screenHeight;
    
    @Override
    public void create() {
        // カメラとビューポートを初期化
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        viewport = new StretchViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
        
        // プレイヤーを画面中央に配置
        player = new Player(GRID_WIDTH / 2, GRID_HEIGHT / 2, GRID_WIDTH, GRID_HEIGHT);
        // アイテムマネージャーを初期化
        itemManager = new ItemManager(GRID_WIDTH, GRID_HEIGHT);
        // ポーズ状態を初期化
        isPaused = false;
        // 画面サイズを取得
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        
        // カメラをプレイヤーの初期位置に設定
        float playerCenterX = player.getPixelX() + Player.TILE_SIZE / 2;
        float playerCenterY = player.getPixelY() + Player.TILE_SIZE / 2;
        camera.position.set(playerCenterX, playerCenterY, 0);
        camera.update();
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
        
        // ポーズ中でない場合のみゲームを更新
        if (!isPaused) {
            // プレイヤーを更新
            float deltaTime = Gdx.graphics.getDeltaTime();
            player.update(deltaTime);
            
            // アイテムマネージャーを更新
            itemManager.update(deltaTime, player);
            
            // キーボード入力処理
            handleInput();
            
            // カメラをプレイヤーの位置に追従させる
            float playerCenterX = player.getPixelX() + Player.TILE_SIZE / 2;
            float playerCenterY = player.getPixelY() + Player.TILE_SIZE / 2;
            camera.position.set(playerCenterX, playerCenterY, 0);
        }
        
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
     * グリッドを描画します。
     */
    private void drawGrid() {
        shapeRenderer.setColor(Color.DARK_GRAY);
        
        int tileSize = Player.TILE_SIZE;
        
        // 縦線を描画
        for (int x = 0; x <= GRID_WIDTH; x++) {
            shapeRenderer.line(x * tileSize, 0, x * tileSize, GRID_HEIGHT * tileSize);
        }
        
        // 横線を描画
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            shapeRenderer.line(0, y * tileSize, GRID_WIDTH * tileSize, y * tileSize);
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
     * ポーズメニューを描画します。
     */
    private void drawPauseMenu() {
        // 半透明の背景を描画（画面座標系で）
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        
        // テキストを描画（画面座標系で）
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // "PAUSED" テキストを中央に表示（大きく）
        font.getData().setScale(3.0f);
        String pausedText = "PAUSED";
        GlyphLayout pausedLayout = new GlyphLayout(font, pausedText);
        float pausedX = (screenWidth - pausedLayout.width) / 2;
        float pausedY = screenHeight / 2 + 60;
        font.draw(batch, pausedText, pausedX, pausedY);
        
        // 操作説明を表示（大きく）
        font.getData().setScale(2.0f);
        String instructionText = "Press ESC to resume";
        GlyphLayout instructionLayout = new GlyphLayout(font, instructionText);
        float instructionX = (screenWidth - instructionLayout.width) / 2;
        float instructionY = screenHeight / 2 - 40;
        font.draw(batch, instructionText, instructionX, instructionY);
        
        // グリッド表示切り替えの説明
        String gridText = "Press G to toggle grid: " + (showGrid ? "ON" : "OFF");
        GlyphLayout gridLayout = new GlyphLayout(font, gridText);
        float gridX = (screenWidth - gridLayout.width) / 2;
        float gridY = screenHeight / 2 - 100;
        font.draw(batch, gridText, gridX, gridY);
        
        // ゲーム終了の説明
        String quitText = "Press Q to quit";
        GlyphLayout quitLayout = new GlyphLayout(font, quitText);
        float quitX = (screenWidth - quitLayout.width) / 2;
        float quitY = screenHeight / 2 - 160;
        font.draw(batch, quitText, quitX, quitY);
        
        font.getData().setScale(2.0f); // 元に戻す
        
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.update();
        
        // UI用カメラも更新
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
        screenWidth = width;
        screenHeight = height;
    }
    
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
