package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ゲームのセーブ/ロード機能を管理するクラス。
 */
public class SaveGameManager {
    private static final String GAME_NAME = "game_like_factorio";
    private static final String SAVE_FILE_PREFIX = "savegame_";
    private static final String SAVE_FILE_EXTENSION = ".json";
    
    /**
     * セーブデータの保存先ディレクトリを取得します。
     * Steamゲームとして標準的な場所（Documents/My Games/[ゲーム名]）を使用します。
     * @return セーブディレクトリのFileHandle
     */
    public FileHandle getSaveDirectory() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            String documentsPath;
            
            if (osName.contains("win")) {
                documentsPath = System.getenv("USERPROFILE");
                if (documentsPath == null) {
                    documentsPath = System.getProperty("user.home");
                }
                documentsPath += "\\Documents\\My Games\\" + GAME_NAME;
            } else if (osName.contains("mac")) {
                documentsPath = System.getProperty("user.home") + "/Documents/My Games/" + GAME_NAME;
            } else {
                documentsPath = System.getProperty("user.home") + "/Documents/My Games/" + GAME_NAME;
            }
            
            FileHandle saveDir = Gdx.files.absolute(documentsPath);
            
            if (!saveDir.exists()) {
                saveDir.mkdirs();
                Gdx.app.log("SaveGame", "Created save directory at " + saveDir.file().getAbsolutePath());
            }
            
            return saveDir;
        } catch (Exception e) {
            Gdx.app.error("SaveGame", "Error getting save directory: " + e.getMessage());
            e.printStackTrace();
            FileHandle fallback = Gdx.files.external("Documents/My Games/" + GAME_NAME);
            fallback.mkdirs();
            Gdx.app.log("SaveGame", "Using fallback path: " + fallback.file().getAbsolutePath());
            return fallback;
        }
    }
    
    /**
     * セーブデータの保存先ファイルを取得します。
     * @param saveName セーブデータ名（nullの場合はデフォルト名）
     * @return セーブファイルのFileHandle
     */
    public FileHandle getSaveFileHandle(String saveName) {
        FileHandle saveDir = getSaveDirectory();
        String fileName;
        if (saveName == null || saveName.trim().isEmpty()) {
            fileName = SAVE_FILE_PREFIX + "default" + SAVE_FILE_EXTENSION;
        } else {
            String sanitizedName = saveName.replaceAll("[\\\\/:*?\"<>|]", "_");
            fileName = SAVE_FILE_PREFIX + sanitizedName + SAVE_FILE_EXTENSION;
        }
        return saveDir.child(fileName);
    }
    
    /**
     * 利用可能なセーブファイルのリストを取得します。
     * @return セーブファイル名のリスト（拡張子なし）
     */
    public List<String> getSaveFileList() {
        List<String> saveList = new ArrayList<>();
        try {
            FileHandle saveDir = getSaveDirectory();
            if (saveDir.exists() && saveDir.isDirectory()) {
                FileHandle[] files = saveDir.list();
                for (FileHandle file : files) {
                    String fileName = file.name();
                    if (fileName.startsWith(SAVE_FILE_PREFIX) && fileName.endsWith(SAVE_FILE_EXTENSION)) {
                        String saveName = fileName.substring(
                            SAVE_FILE_PREFIX.length(),
                            fileName.length() - SAVE_FILE_EXTENSION.length()
                        );
                        saveList.add(saveName);
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("SaveGame", "Error getting save file list: " + e.getMessage());
            e.printStackTrace();
        }
        saveList.sort(String::compareToIgnoreCase);
        return saveList;
    }
    
    /**
     * ゲームの状態をセーブします。
     * @param saveName セーブデータ名
     * @param player プレイヤー
     * @param itemManager アイテムマネージャー
     * @param showGrid グリッド表示フラグ
     * @param masterVolume マスターボリューム
     * @param isMuted ミュート状態
     * @param cameraZoom カメラズーム
     * @return セーブが成功した場合true
     */
    public boolean saveGame(String saveName, Player player, ItemManager itemManager,
                           boolean showGrid, float masterVolume, boolean isMuted, float cameraZoom) {
        try {
            GameSaveData saveData = new GameSaveData();
            
            saveData.playerTileX = player.getPlayerTileX();
            saveData.playerTileY = player.getPlayerTileY();
            
            saveData.collectedCount = itemManager.getCollectedCount();
            saveData.items = new ArrayList<>();
            for (Item item : itemManager.getItems()) {
                if (!item.isCollected()) {
                    GameSaveData.ItemData itemData = new GameSaveData.ItemData(
                        item.getTileX(),
                        item.getTileY(),
                        item.getType().name()
                    );
                    saveData.items.add(itemData);
                }
            }
            
            saveData.generatedChunks = new ArrayList<>(itemManager.getGeneratedChunks());
            
            saveData.showGrid = showGrid;
            saveData.masterVolume = masterVolume;
            saveData.isMuted = isMuted;
            saveData.cameraZoom = cameraZoom;
            saveData.civilizationLevel = itemManager.getCivilizationLevel().getLevel();
            
            Json json = new Json();
            String jsonString = json.prettyPrint(saveData);
            
            FileHandle saveFile = getSaveFileHandle(saveName);
            Gdx.app.log("SaveGame", "Writing to file: " + saveFile.file().getAbsolutePath());
            
            saveFile.writeString(jsonString, false);
            
            if (saveFile.exists()) {
                Gdx.app.log("SaveGame", "File successfully written. Size: " + saveFile.length() + " bytes");
                return true;
            } else {
                Gdx.app.error("SaveGame", "File was not created after write operation");
                return false;
            }
        } catch (Exception e) {
            Gdx.app.error("SaveGame", "Failed to save game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * ゲームの状態をロードします。
     * @param saveName セーブデータ名
     * @param player プレイヤー
     * @param itemManager アイテムマネージャー
     * @return ロードされたデータ（失敗時はnull）
     */
    public LoadResult loadGame(String saveName, Player player, ItemManager itemManager) {
        try {
            FileHandle saveFile = getSaveFileHandle(saveName);
            if (!saveFile.exists()) {
                Gdx.app.log("LoadGame", "Save file not found");
                return null;
            }
            
            String jsonString = saveFile.readString();
            Json json = new Json();
            GameSaveData saveData = json.fromJson(GameSaveData.class, jsonString);
            
            player.setPosition(saveData.playerTileX, saveData.playerTileY);
            
            itemManager.setCollectedCount(saveData.collectedCount);
            Array<Item> loadedItems = new Array<>();
            for (GameSaveData.ItemData itemData : saveData.items) {
                Item.ItemType type = Item.ItemType.valueOf(itemData.type);
                Item item = new Item(itemData.tileX, itemData.tileY, type);
                loadedItems.add(item);
            }
            itemManager.setItems(loadedItems);
            
            Set<String> chunks = new HashSet<>(saveData.generatedChunks);
            itemManager.setGeneratedChunks(chunks);
            
            // 文明レベルを復元
            if (saveData.civilizationLevel > 0) {
                itemManager.getCivilizationLevel().setLevel(saveData.civilizationLevel);
            }
            
            LoadResult result = new LoadResult();
            result.showGrid = saveData.showGrid;
            result.masterVolume = saveData.masterVolume;
            result.isMuted = saveData.isMuted;
            result.cameraZoom = saveData.cameraZoom;
            
            return result;
        } catch (Exception e) {
            Gdx.app.error("LoadGame", "Failed to load game: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * ロード結果を保持するクラス。
     */
    public static class LoadResult {
        public boolean showGrid;
        public float masterVolume;
        public boolean isMuted;
        public float cameraZoom;
    }
}
