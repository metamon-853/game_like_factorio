package io.github.some_example_name.ui;

import io.github.some_example_name.entity.ItemData;
import io.github.some_example_name.manager.ItemDataLoader;
import io.github.some_example_name.game.CraftingSystem;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Map;

/**
 * アイテム詳細パネルを描画する共通クラス。
 */
public class ItemDetailPanel {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    
    // パネルの位置とサイズ
    private float detailX;
    private float detailY;
    private float detailWidth = 600;
    private float detailHeight = 450;
    
    // オプションのデータローダーとクラフトシステム（素材情報表示用）
    private ItemDataLoader itemDataLoader;
    private CraftingSystem craftingSystem;
    
    /**
     * ItemDetailPanelを作成します。
     * @param shapeRenderer ShapeRenderer
     * @param batch SpriteBatch
     * @param font BitmapFont
     * @param uiCamera OrthographicCamera
     */
    public ItemDetailPanel(ShapeRenderer shapeRenderer, SpriteBatch batch, 
                          BitmapFont font, OrthographicCamera uiCamera) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.uiCamera = uiCamera;
    }
    
    /**
     * パネルの位置を設定します。
     * @param x X座標
     * @param y Y座標
     */
    public void setPosition(float x, float y) {
        this.detailX = x;
        this.detailY = y;
    }
    
    /**
     * パネルのサイズを設定します。
     * @param width 幅
     * @param height 高さ
     */
    public void setSize(float width, float height) {
        this.detailWidth = width;
        this.detailHeight = height;
    }
    
    /**
     * アイテムデータローダーを設定します（素材情報表示用）。
     */
    public void setItemDataLoader(ItemDataLoader itemDataLoader) {
        this.itemDataLoader = itemDataLoader;
    }
    
    /**
     * クラフトシステムを設定します（素材情報表示用）。
     */
    public void setCraftingSystem(CraftingSystem craftingSystem) {
        this.craftingSystem = craftingSystem;
    }
    
    /**
     * アイテム詳細パネルを描画します。
     * @param itemData アイテムデータ
     * @param count 数量（オプション、0以下の場合は表示しない）
     */
    public void render(ItemData itemData, int count) {
        if (itemData == null) {
            return;
        }
        
        boolean batchWasActive = batch.isDrawing();
        if (batchWasActive) {
            batch.end();
        }
        
        // 詳細パネルの背景
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
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
        
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
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
        float lineSpacing = 37.5f;
        int lines = UITextHelper.drawWrappedText(batch, font, description, detailX + 30, descY, maxWidth, lineSpacing);
        descY -= lines * lineSpacing;
        
        // クラフト可能なアイテムの場合は素材情報を表示
        if (itemData.isCraftable() && itemDataLoader != null) {
            descY -= 50;
            font.setColor(new Color(0.9f, 0.9f, 0.7f, 1f));
            font.draw(batch, "必要な素材:", detailX + 30, descY);
            descY -= 30;
            
            Map<Integer, Integer> materials = itemData.getMaterials();
            for (Map.Entry<Integer, Integer> entry : materials.entrySet()) {
                int materialId = entry.getKey();
                int requiredAmount = entry.getValue();
                
                // 素材の名前を取得
                ItemData materialData = itemDataLoader.getItemData(materialId);
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
        
        // batchが元々開始されていなかった場合は終了する
        if (!batchWasActive) {
            batch.end();
        }
    }
    
    /**
     * アイテム詳細パネルを描画します（数量なし）。
     * @param itemData アイテムデータ
     */
    public void render(ItemData itemData) {
        render(itemData, 0);
    }
    
    /**
     * 指定された座標がパネル内にあるかどうかを判定します。
     * @param screenX 画面X座標
     * @param screenY 画面Y座標
     * @return パネル内にある場合true
     */
    public boolean contains(float screenX, float screenY) {
        return screenX >= detailX && screenX <= detailX + detailWidth &&
               screenY >= detailY && screenY <= detailY + detailHeight;
    }
}
