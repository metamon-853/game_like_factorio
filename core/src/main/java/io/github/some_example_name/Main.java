package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Player player;
    private ItemManager itemManager;
    
    // ポーズ状態
    private boolean isPaused;
    
    // グリッドのサイズ
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 15;
    
    // 画面サイズ
    private int screenWidth;
    private int screenHeight;
    
    @Override
    public void create() {
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
    }

    @Override
    public void render() {
        // 画面をクリア
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        
        // ESCキーでポーズ/再開を切り替え
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused;
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
        }
        
        // グリッドを描画（Lineモード）
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawGrid();
        shapeRenderer.end();
        
        // その他の描画（Filledモード）
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // アイテムを描画
        itemManager.render(shapeRenderer);
        
        // プレイヤーを描画
        player.render(shapeRenderer);
        
        shapeRenderer.end();
        
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
        
        // 方向キーで移動
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            player.move(0, 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            player.move(0, -1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            player.move(-1, 0);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
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
     * ポーズメニューを描画します。
     */
    private void drawPauseMenu() {
        // 半透明の背景を描画
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        
        // テキストを描画
        batch.begin();
        
        // "PAUSED" テキストを中央に表示
        String pausedText = "PAUSED";
        GlyphLayout pausedLayout = new GlyphLayout(font, pausedText);
        float pausedX = (screenWidth - pausedLayout.width) / 2;
        float pausedY = screenHeight / 2 + 40;
        font.draw(batch, pausedText, pausedX, pausedY);
        
        // 操作説明を表示
        font.getData().setScale(1.2f);
        String instructionText = "Press ESC to resume";
        GlyphLayout instructionLayout = new GlyphLayout(font, instructionText);
        float instructionX = (screenWidth - instructionLayout.width) / 2;
        float instructionY = screenHeight / 2 - 40;
        font.draw(batch, instructionText, instructionX, instructionY);
        font.getData().setScale(2.0f); // 元に戻す
        
        batch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
