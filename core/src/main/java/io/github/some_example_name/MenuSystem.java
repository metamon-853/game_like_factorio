package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;

/**
 * メニューシステムを管理するクラス。
 * ポーズメニュー、サウンドメニュー、セーブ/ロードメニューなどを処理します。
 */
public class MenuSystem {
    public enum MenuState {
        MAIN_MENU,
        SOUND_MENU,
        SAVE_MENU,
        LOAD_MENU,
        QUIT_CONFIRM
    }
    
    // コールバックインターフェース
    public interface MenuCallbacks {
        void onSaveGame(String saveName);
        void onLoadGame(String saveName);
        void onToggleGrid();
        void onQuit();
        boolean isGridVisible();
        float getCameraZoom();
        void setPaused(boolean paused);
    }
    
    private MenuState currentMenuState = MenuState.MAIN_MENU;
    private boolean isDraggingSlider = false;
    
    private UIRenderer uiRenderer;
    private SaveGameManager saveGameManager;
    private SoundSettings soundSettings;
    private TextInputHandler textInputHandler;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    private int screenWidth;
    private int screenHeight;
    private MenuCallbacks callbacks;
    
    public MenuSystem(UIRenderer uiRenderer, SaveGameManager saveGameManager, 
                     SoundSettings soundSettings, TextInputHandler textInputHandler,
                     ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                     OrthographicCamera uiCamera, int screenWidth, int screenHeight,
                     MenuCallbacks callbacks) {
        this.uiRenderer = uiRenderer;
        this.saveGameManager = saveGameManager;
        this.soundSettings = soundSettings;
        this.textInputHandler = textInputHandler;
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.uiCamera = uiCamera;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.callbacks = callbacks;
    }
    
    /**
     * 画面サイズを更新します。
     */
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
    
    /**
     * 現在のメニュー状態を取得します。
     */
    public MenuState getCurrentMenuState() {
        return currentMenuState;
    }
    
    /**
     * メニュー状態を設定します。
     */
    public void setCurrentMenuState(MenuState state) {
        this.currentMenuState = state;
    }
    
    /**
     * ESCキーでメニューを閉じる処理を行います。
     * @return メニューを閉じた場合true
     */
    public boolean handleEscapeKey() {
        if (textInputHandler.isTextInputActive()) {
            textInputHandler.setTextInputActive(false);
            return false;
        } else if (currentMenuState == MenuState.SOUND_MENU || 
                  currentMenuState == MenuState.SAVE_MENU || 
                  currentMenuState == MenuState.LOAD_MENU ||
                  currentMenuState == MenuState.QUIT_CONFIRM) {
            currentMenuState = MenuState.MAIN_MENU;
            return false;
        } else {
            callbacks.setPaused(false);
            currentMenuState = MenuState.MAIN_MENU;
            return true;
        }
    }
    
    /**
     * メニューのクリックとドラッグを処理します。
     */
    public void handleMenuInput() {
        if (currentMenuState == MenuState.MAIN_MENU) {
            handlePauseMenuClick();
        } else if (currentMenuState == MenuState.SOUND_MENU) {
            handleSoundMenuClick();
            handleSoundMenuDrag();
        } else if (currentMenuState == MenuState.SAVE_MENU) {
            handleSaveMenuClick();
        } else if (currentMenuState == MenuState.LOAD_MENU) {
            handleLoadMenuClick();
        } else if (currentMenuState == MenuState.QUIT_CONFIRM) {
            handleQuitConfirmClick();
        }
    }
    
    /**
     * メニューを描画します。
     */
    public void render() {
        if (currentMenuState == MenuState.MAIN_MENU) {
            drawPauseMenu();
        } else if (currentMenuState == MenuState.SOUND_MENU) {
            drawSoundMenu();
        } else if (currentMenuState == MenuState.SAVE_MENU) {
            drawSaveMenu();
        } else if (currentMenuState == MenuState.LOAD_MENU) {
            drawLoadMenu();
        } else if (currentMenuState == MenuState.QUIT_CONFIRM) {
            drawQuitConfirmDialog();
        }
    }
    
    /**
     * ポーズメニューのマウスクリックを処理します。
     */
    private void handlePauseMenuClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            
            float buttonWidth = 320;
            float buttonHeight = 65;
            float centerX = screenWidth / 2;
            float centerY = screenHeight / 2;
            float buttonSpacing = 80;
            
            float gridButtonY = centerY + buttonSpacing - 20;
            Button gridButton = new Button(centerX - buttonWidth / 2, gridButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            float saveButtonY = centerY - 20;
            Button saveButton = new Button(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            float loadButtonY = centerY - buttonSpacing - 20;
            Button loadButton = new Button(centerX - buttonWidth / 2, loadButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            float soundButtonY = centerY - buttonSpacing * 2 - 20;
            Button soundButton = new Button(centerX - buttonWidth / 2, soundButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            float quitButtonY = centerY - buttonSpacing * 3 - 20;
            Button quitButton = new Button(centerX - buttonWidth / 2, quitButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            if (gridButton.contains(mouseX, mouseY)) {
                callbacks.onToggleGrid();
            } else if (saveButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.SAVE_MENU;
            } else if (loadButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.LOAD_MENU;
            } else if (soundButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.SOUND_MENU;
            } else if (quitButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.QUIT_CONFIRM;
            }
        }
    }
    
    /**
     * ポーズメニューを描画します。
     */
    private void drawPauseMenu() {
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        float buttonWidth = 320;
        float buttonHeight = 65;
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2;
        float buttonSpacing = 80;
        
        float gridButtonY = centerY + buttonSpacing - 20;
        Button gridButton = new Button(centerX - buttonWidth / 2, gridButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, gridButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Grid: " + (callbacks.isGridVisible() ? "ON" : "OFF"), gridButton.contains(mouseX, mouseY));
        
        float saveButtonY = centerY - 20;
        Button saveButton = new Button(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Save Game", saveButton.contains(mouseX, mouseY));
        
        float loadButtonY = centerY - buttonSpacing - 20;
        Button loadButton = new Button(centerX - buttonWidth / 2, loadButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, loadButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Load Game", loadButton.contains(mouseX, mouseY));
        
        float soundButtonY = centerY - buttonSpacing * 2 - 20;
        Button soundButton = new Button(centerX - buttonWidth / 2, soundButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, soundButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Sound", soundButton.contains(mouseX, mouseY));
        
        float quitButtonY = centerY - buttonSpacing * 3 - 20;
        Button quitButton = new Button(centerX - buttonWidth / 2, quitButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, quitButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Quit Game", quitButton.contains(mouseX, mouseY));
        
        font.getData().setScale(2.0f);
        batch.end();
    }
    
    /**
     * サウンドメニューのマウスクリックを処理します。
     */
    private void handleSoundMenuClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            
            float buttonWidth = 320;
            float buttonHeight = 65;
            float centerX = screenWidth / 2;
            float centerY = screenHeight / 2;
            
            float backButtonY = centerY - 200;
            Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            float sliderWidth = 400;
            float sliderHeight = 20;
            float sliderX = centerX - sliderWidth / 2;
            float sliderY = centerY - sliderHeight / 2;
            Button sliderArea = new Button(sliderX, sliderY, sliderWidth, sliderHeight);
            
            if (backButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.MAIN_MENU;
            } else if (sliderArea.contains(mouseX, mouseY)) {
                updateVolumeFromSliderPosition(mouseX, sliderX, sliderWidth);
                isDraggingSlider = true;
            }
        }
        
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            isDraggingSlider = false;
        }
    }
    
    /**
     * サウンドメニューのマウスドラッグを処理します。
     */
    private void handleSoundMenuDrag() {
        if (isDraggingSlider && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float centerX = screenWidth / 2;
            float sliderWidth = 400;
            float sliderX = centerX - sliderWidth / 2;
            
            updateVolumeFromSliderPosition(mouseX, sliderX, sliderWidth);
        }
    }
    
    /**
     * スライダーの位置から音量を更新します。
     */
    private void updateVolumeFromSliderPosition(float mouseX, float sliderX, float sliderWidth) {
        float relativeX = Math.max(0, Math.min(sliderWidth, mouseX - sliderX));
        float newVolume = relativeX / sliderWidth;
        
        if (soundSettings.isMuted() && newVolume > 0) {
            soundSettings.setMuted(false);
        }
        
        soundSettings.setMasterVolume(newVolume);
    }
    
    /**
     * サウンドメニューを描画します。
     */
    private void drawSoundMenu() {
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        font.getData().setScale(3.0f);
        font.setColor(Color.WHITE);
        String titleText = "SOUND SETTINGS";
        GlyphLayout titleLayout = new GlyphLayout(font, titleText);
        float titleX = (screenWidth - titleLayout.width) / 2;
        float titleY = screenHeight / 2 + 150;
        font.draw(batch, titleText, titleX, titleY);
        
        float buttonWidth = 320;
        float buttonHeight = 65;
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2;
        
        uiRenderer.drawVolumeSlider(centerX, centerY, soundSettings.getMasterVolume(), soundSettings.isMuted());
        
        font.getData().setScale(2.0f);
        font.setColor(soundSettings.isMuted() ? Color.RED : Color.WHITE);
        String volumeText = "Volume: " + (int)(soundSettings.getMasterVolume() * 100) + "%" + (soundSettings.isMuted() ? " (MUTED)" : "");
        GlyphLayout volumeLayout = new GlyphLayout(font, volumeText);
        float volumeTextX = centerX - volumeLayout.width / 2;
        float volumeTextY = centerY + 50;
        font.draw(batch, volumeText, volumeTextX, volumeTextY);
        
        float backButtonY = centerY - 200;
        Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Back", backButton.contains(mouseX, mouseY));
        
        font.getData().setScale(2.0f);
        batch.end();
    }
    
    /**
     * セーブメニューのマウスクリックを処理します。
     */
    private void handleSaveMenuClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            
            float centerX = screenWidth / 2;
            float centerY = screenHeight / 2;
            
            float inputFieldWidth = 500;
            float inputFieldHeight = 60;
            float inputFieldX = centerX - inputFieldWidth / 2;
            float inputFieldY = centerY + 30;
            Button inputField = new Button(inputFieldX, inputFieldY, inputFieldWidth, inputFieldHeight);
            
            float buttonWidth = 280;
            float buttonHeight = 65;
            float buttonSpacing = 90;
            
            float saveButtonY = centerY - 100;
            Button saveButton = new Button(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            float backButtonY = centerY - buttonSpacing - 100;
            Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            
            if (inputField.contains(mouseX, mouseY)) {
                textInputHandler.setTextInputActive(true);
                if (textInputHandler.getInputText().length() == 0) {
                    textInputHandler.setCurrentInputLabel("Save Name");
                }
            } else if (saveButton.contains(mouseX, mouseY) && textInputHandler.getInputText().length() > 0 && !textInputHandler.isTextInputActive()) {
                String text = textInputHandler.getInputText().trim();
                if (!text.isEmpty() && text.length() <= textInputHandler.getMaxInputLength()) {
                    callbacks.onSaveGame(text);
                }
                textInputHandler.setTextInputActive(false);
            } else if (backButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.MAIN_MENU;
                textInputHandler.setTextInputActive(false);
            } else if (!inputField.contains(mouseX, mouseY)) {
                if (textInputHandler.isTextInputActive()) {
                    textInputHandler.setTextInputActive(false);
                }
            }
        }
    }
    
    /**
     * ロードメニューのマウスクリックを処理します。
     */
    private void handleLoadMenuClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            
            float buttonWidth = 400;
            float buttonHeight = 50;
            float centerX = screenWidth / 2;
            float startY = screenHeight / 2 + 150;
            float buttonSpacing = 60;
            
            List<String> saveList = saveGameManager.getSaveFileList();
            
            for (int i = 0; i < saveList.size() && i < 10; i++) {
                float buttonY = startY - i * buttonSpacing;
                Button saveButton = new Button(centerX - buttonWidth / 2, buttonY - buttonHeight / 2, buttonWidth, buttonHeight);
                if (saveButton.contains(mouseX, mouseY)) {
                    String saveName = saveList.get(i);
                    callbacks.onLoadGame(saveName);
                    break;
                }
            }
            
            float backButtonY = screenHeight / 2 - 200;
            Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
            if (backButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.MAIN_MENU;
            }
        }
    }
    
    /**
     * セーブメニューを描画します。
     */
    private void drawSaveMenu() {
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2;
        
        float dialogWidth = 600;
        float dialogHeight = 450;
        float dialogX = (screenWidth - dialogWidth) / 2;
        float dialogY = (screenHeight - dialogHeight) / 2;
        
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
        shapeRenderer.rect(dialogX, dialogY, dialogWidth, dialogHeight);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        shapeRenderer.rect(dialogX, dialogY, dialogWidth, dialogHeight);
        shapeRenderer.end();
        
        float inputFieldWidth = 500;
        float inputFieldHeight = 60;
        float inputFieldX = centerX - inputFieldWidth / 2;
        float inputFieldY = centerY + 30;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (textInputHandler.isTextInputActive()) {
            shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
        } else {
            shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 1f);
        }
        shapeRenderer.rect(inputFieldX, inputFieldY, inputFieldWidth, inputFieldHeight);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (textInputHandler.isTextInputActive()) {
            shapeRenderer.setColor(0.8f, 0.8f, 1.0f, 1f);
            shapeRenderer.rect(inputFieldX - 2, inputFieldY - 2, inputFieldWidth + 4, inputFieldHeight + 4);
            shapeRenderer.rect(inputFieldX - 1, inputFieldY - 1, inputFieldWidth + 2, inputFieldHeight + 2);
        } else {
            shapeRenderer.setColor(0.5f, 0.5f, 0.7f, 1f);
        }
        shapeRenderer.rect(inputFieldX, inputFieldY, inputFieldWidth, inputFieldHeight);
        shapeRenderer.end();
        
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        font.getData().setScale(3.5f);
        font.setColor(Color.WHITE);
        String titleText = "SAVE GAME";
        GlyphLayout titleLayout = new GlyphLayout(font, titleText);
        float titleX = centerX - titleLayout.width / 2;
        float titleY = dialogY + dialogHeight - 50;
        font.draw(batch, titleText, titleX, titleY);
        
        font.getData().setScale(2.2f);
        font.setColor(Color.WHITE);
        String inputLabel = "Save Name:";
        GlyphLayout labelLayout = new GlyphLayout(font, inputLabel);
        float labelX = centerX - labelLayout.width / 2;
        float labelY = inputFieldY + inputFieldHeight + 20;
        font.draw(batch, inputLabel, labelX, labelY);
        
        font.getData().setScale(2.0f);
        String inputText = textInputHandler.getInputText();
        String displayText = textInputHandler.isTextInputActive() ? inputText + "_" : 
                            (inputText.length() > 0 ? inputText : "Enter save name...");
        font.setColor(textInputHandler.isTextInputActive() ? Color.YELLOW : (inputText.length() > 0 ? Color.WHITE : Color.GRAY));
        GlyphLayout textLayout = new GlyphLayout(font, displayText);
        float textX = inputFieldX + 15;
        float textY = inputFieldY + inputFieldHeight / 2 + textLayout.height / 2;
        font.draw(batch, displayText, textX, textY);
        
        font.getData().setScale(1.5f);
        font.setColor(Color.LIGHT_GRAY);
        String charCountText = inputText.length() + " / " + textInputHandler.getMaxInputLength();
        GlyphLayout charCountLayout = new GlyphLayout(font, charCountText);
        float charCountX = inputFieldX + inputFieldWidth - charCountLayout.width - 15;
        float charCountY = inputFieldY - 5;
        font.draw(batch, charCountText, charCountX, charCountY);
        
        float buttonWidth = 280;
        float buttonHeight = 65;
        float buttonSpacing = 90;
        
        float saveButtonY = centerY - 100;
        Button saveButton = new Button(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        String saveButtonText = textInputHandler.isTextInputActive() ? "Press Enter to Save" : 
                               (inputText.length() > 0 ? "Save Game" : "Enter Name First");
        uiRenderer.drawButton(centerX - buttonWidth / 2, saveButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   saveButtonText, saveButton.contains(mouseX, mouseY) && inputText.length() > 0);
        
        float backButtonY = centerY - buttonSpacing - 100;
        Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Back", backButton.contains(mouseX, mouseY));
        
        List<String> saveList = saveGameManager.getSaveFileList();
        if (!saveList.isEmpty() && saveList.size() <= 5) {
            font.getData().setScale(1.3f);
            font.setColor(Color.LIGHT_GRAY);
            String existingText = "Existing saves: " + saveList.size();
            GlyphLayout existingLayout = new GlyphLayout(font, existingText);
            float existingX = centerX - existingLayout.width / 2;
            float existingY = backButtonY - buttonHeight / 2 - 25;
            font.draw(batch, existingText, existingX, existingY);
        }
        
        font.getData().setScale(2.0f);
        batch.end();
    }
    
    /**
     * ロードメニューを描画します。
     */
    private void drawLoadMenu() {
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        font.getData().setScale(3.0f);
        font.setColor(Color.WHITE);
        String titleText = "LOAD GAME";
        GlyphLayout titleLayout = new GlyphLayout(font, titleText);
        float titleX = (screenWidth - titleLayout.width) / 2;
        float titleY = screenHeight / 2 + 200;
        font.draw(batch, titleText, titleX, titleY);
        
        float buttonWidth = 400;
        float buttonHeight = 50;
        float centerX = screenWidth / 2;
        float startY = screenHeight / 2 + 150;
        float buttonSpacing = 60;
        
        List<String> saveList = saveGameManager.getSaveFileList();
        
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        if (saveList.isEmpty()) {
            String noSaveText = "No save files found";
            GlyphLayout noSaveLayout = new GlyphLayout(font, noSaveText);
            float noSaveX = centerX - noSaveLayout.width / 2;
            float noSaveY = screenHeight / 2;
            font.draw(batch, noSaveText, noSaveX, noSaveY);
        } else {
            for (int i = 0; i < saveList.size() && i < 10; i++) {
                float buttonY = startY - i * buttonSpacing;
                String saveName = saveList.get(i);
                Button saveButton = new Button(centerX - buttonWidth / 2, buttonY - buttonHeight / 2, buttonWidth, buttonHeight);
                uiRenderer.drawButton(centerX - buttonWidth / 2, buttonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                          saveName, saveButton.contains(mouseX, mouseY));
            }
        }
        
        float backButtonY = screenHeight / 2 - 200;
        Button backButton = new Button(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight);
        uiRenderer.drawButton(centerX - buttonWidth / 2, backButtonY - buttonHeight / 2, buttonWidth, buttonHeight, 
                   "Back", backButton.contains(mouseX, mouseY));
        
        font.getData().setScale(2.0f);
        batch.end();
    }
    
    /**
     * 終了確認ダイアログのマウスクリックを処理します。
     */
    private void handleQuitConfirmClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();
            
            float buttonWidth = 200;
            float buttonHeight = 65;
            float centerX = screenWidth / 2;
            float centerY = screenHeight / 2 - 50;
            float buttonSpacing = 120;
            
            float yesButtonX = centerX - buttonSpacing - buttonWidth / 2;
            float yesButtonY = centerY - buttonHeight / 2;
            Button yesButton = new Button(yesButtonX, yesButtonY, buttonWidth, buttonHeight);
            
            float noButtonX = centerX + buttonSpacing - buttonWidth / 2;
            float noButtonY = centerY - buttonHeight / 2;
            Button noButton = new Button(noButtonX, noButtonY, buttonWidth, buttonHeight);
            
            if (yesButton.contains(mouseX, mouseY)) {
                callbacks.onQuit();
            } else if (noButton.contains(mouseX, mouseY)) {
                currentMenuState = MenuState.MAIN_MENU;
            }
        }
    }
    
    /**
     * 終了確認ダイアログを描画します。
     */
    private void drawQuitConfirmDialog() {
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
        float dialogWidth = 500;
        float dialogHeight = 250;
        float dialogX = (screenWidth - dialogWidth) / 2;
        float dialogY = (screenHeight - dialogHeight) / 2;
        shapeRenderer.rect(dialogX, dialogY, dialogWidth, dialogHeight);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
        shapeRenderer.rect(dialogX, dialogY, dialogWidth, dialogHeight);
        shapeRenderer.end();
        
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
        String messageText = "Quit Game?";
        GlyphLayout messageLayout = new GlyphLayout(font, messageText);
        float messageX = (screenWidth - messageLayout.width) / 2;
        float messageY = screenHeight / 2 + 50;
        font.draw(batch, messageText, messageX, messageY);
        
        float buttonWidth = 200;
        float buttonHeight = 65;
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2 - 50;
        float buttonSpacing = 120;
        
        float yesButtonX = centerX - buttonSpacing - buttonWidth / 2;
        float yesButtonY = centerY - buttonHeight / 2;
        Button yesButton = new Button(yesButtonX, yesButtonY, buttonWidth, buttonHeight);
        uiRenderer.drawButton(yesButtonX, yesButtonY, buttonWidth, buttonHeight, 
                   "Yes", yesButton.contains(mouseX, mouseY));
        
        float noButtonX = centerX + buttonSpacing - buttonWidth / 2;
        float noButtonY = centerY - buttonHeight / 2;
        Button noButton = new Button(noButtonX, noButtonY, buttonWidth, buttonHeight);
        uiRenderer.drawButton(noButtonX, noButtonY, buttonWidth, buttonHeight, 
                   "No", noButton.contains(mouseX, mouseY));
        
        font.getData().setScale(2.0f);
        batch.end();
    }
}
