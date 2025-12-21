package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import io.github.some_example_name.entity.TerrainTile;
import java.util.HashMap;
import java.util.Map;

/**
 * サウンド効果を管理するクラス。
 */
public class SoundManager implements Disposable {
    private Sound hoverSound;
    private Sound collectSound;
    private Sound footstepSound; // 後方互換性のため残す（デフォルト用）
    private Map<TerrainTile.TerrainType, Sound> footstepSounds;
    private Sound craftSound;
    private SoundSettings soundSettings;
    private boolean isInitialized = false;
    
    // 音の再生クールダウン（連続再生を防ぐ）
    private long lastHoverSoundTime = 0;
    private long lastCollectSoundTime = 0;
    private long lastFootstepSoundTime = 0;
    private long lastCraftSoundTime = 0;
    private static final long HOVER_SOUND_COOLDOWN_MS = 50; // 50ミリ秒のクールダウン
    private static final long COLLECT_SOUND_COOLDOWN_MS = 100; // 100ミリ秒のクールダウン
    private static final long FOOTSTEP_SOUND_COOLDOWN_MS = 150; // 150ミリ秒のクールダウン（足音の間隔）
    private static final long CRAFT_SOUND_COOLDOWN_MS = 120; // 120ミリ秒のクールダウン
    
    /**
     * SoundManagerを初期化します。
     * @param soundSettings サウンド設定
     */
    public SoundManager(SoundSettings soundSettings) {
        this.soundSettings = soundSettings;
        initializeAudio();
    }
    
    /**
     * オーディオを初期化します。
     */
    private void initializeAudio() {
        try {
            // プログラムで生成した音声データをWAVファイルとして作成し、Soundオブジェクトとして読み込む
            hoverSound = createHoverSound();
            if (hoverSound == null) {
                Gdx.app.error("SoundManager", "Failed to create hover sound");
            }
            collectSound = createCollectSound();
            if (collectSound == null) {
                Gdx.app.error("SoundManager", "Failed to create collect sound");
            }
            
            // 各タイルタイプ用の足音を初期化
            footstepSounds = new HashMap<>();
            for (TerrainTile.TerrainType type : TerrainTile.TerrainType.values()) {
                Sound sound = createFootstepSoundForTerrain(type);
                if (sound != null) {
                    footstepSounds.put(type, sound);
                } else {
                    Gdx.app.error("SoundManager", "Failed to create footstep sound for " + type);
                }
            }
            
            // 後方互換性のため、デフォルトの足音も作成（GRASS用）
            footstepSound = footstepSounds.get(TerrainTile.TerrainType.GRASS);
            if (footstepSound == null) {
                Gdx.app.error("SoundManager", "Failed to create default footstep sound");
            }
            
            craftSound = createCraftSound();
            if (craftSound == null) {
                Gdx.app.error("SoundManager", "Failed to create craft sound");
            }
            isInitialized = true;
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to initialize audio", e);
            isInitialized = false;
        }
    }
    
    /**
     * ホバー音をプログラムで生成してSoundオブジェクトとして返します。
     * Soundオブジェクトは非同期で再生されるため、パフォーマンスが良いです。
     */
    private Sound createHoverSound() {
        int sampleRate = 44100;
        float frequency = 800.0f; // 800Hzの高めの音（心地よい音）
        float duration = 0.08f; // 80ミリ秒の短い音
        int numSamples = (int)(sampleRate * duration);
        
        // WAVファイルのヘッダーサイズ
        int dataSize = numSamples * 2; // 16bit = 2 bytes per sample
        int fileSize = 36 + dataSize;
        
        // WAVファイルのバイト配列を作成
        byte[] wavData = new byte[44 + dataSize];
        int offset = 0;
        
        // RIFFヘッダー
        writeString(wavData, offset, "RIFF"); offset += 4;
        writeInt(wavData, offset, fileSize); offset += 4;
        writeString(wavData, offset, "WAVE"); offset += 4;
        
        // fmtチャンク
        writeString(wavData, offset, "fmt "); offset += 4;
        writeInt(wavData, offset, 16); // fmtチャンクサイズ
        offset += 4;
        writeShort(wavData, offset, (short)1); // オーディオフォーマット（PCM）
        offset += 2;
        writeShort(wavData, offset, (short)1); // チャンネル数（モノラル）
        offset += 2;
        writeInt(wavData, offset, sampleRate); // サンプルレート
        offset += 4;
        writeInt(wavData, offset, sampleRate * 2); // バイトレート
        offset += 4;
        writeShort(wavData, offset, (short)2); // ブロックアライメント
        offset += 2;
        writeShort(wavData, offset, (short)16); // ビット深度
        offset += 2;
        
        // dataチャンク
        writeString(wavData, offset, "data"); offset += 4;
        writeInt(wavData, offset, dataSize); offset += 4;
        
        // 音声サンプルを生成
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            // サイン波を生成
            double sample = Math.sin(2 * Math.PI * frequency * time);
            
            // エンベロープを適用（フェードイン・フェードアウト）
            double envelope;
            if (i < numSamples * 0.1) {
                // フェードイン（最初の10%）
                envelope = i / (numSamples * 0.1);
            } else if (i > numSamples * 0.7) {
                // フェードアウト（最後の30%）
                envelope = (numSamples - i) / (numSamples * 0.3);
            } else {
                envelope = 1.0;
            }
            
            // 音量を調整（0.3で少し小さめに）
            sample *= envelope * 0.3;
            
            // 16bit PCMとして書き込み（リトルエンディアン）
            short sampleValue = (short)(sample * Short.MAX_VALUE);
            wavData[offset++] = (byte)(sampleValue & 0xFF);
            wavData[offset++] = (byte)((sampleValue >> 8) & 0xFF);
        }
        
        // 音源ファイルをsoundsディレクトリに保存
        try {
            FileHandle soundFile = Gdx.files.local("sounds/hover_sound.wav");
            soundFile.writeBytes(wavData, false);
            
            if (!soundFile.exists()) {
                Gdx.app.error("SoundManager", "Failed to create hover sound file");
                return null;
            }
            
            Sound sound = Gdx.audio.newSound(soundFile);
            return sound;
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to create hover sound", e);
            return null;
        }
    }
    
    /**
     * アイテム取得音をプログラムで生成してSoundオブジェクトとして返します。
     * 心地よい「ピロン」という音を生成します。
     */
    private Sound createCollectSound() {
        int sampleRate = 44100;
        float duration = 0.15f; // 150ミリ秒の音
        int numSamples = (int)(sampleRate * duration);
        
        // WAVファイルのヘッダーサイズ
        int dataSize = numSamples * 2; // 16bit = 2 bytes per sample
        int fileSize = 36 + dataSize;
        
        // WAVファイルのバイト配列を作成
        byte[] wavData = new byte[44 + dataSize];
        int offset = 0;
        
        // RIFFヘッダー
        writeString(wavData, offset, "RIFF"); offset += 4;
        writeInt(wavData, offset, fileSize); offset += 4;
        writeString(wavData, offset, "WAVE"); offset += 4;
        
        // fmtチャンク
        writeString(wavData, offset, "fmt "); offset += 4;
        writeInt(wavData, offset, 16); // fmtチャンクサイズ
        offset += 4;
        writeShort(wavData, offset, (short)1); // オーディオフォーマット（PCM）
        offset += 2;
        writeShort(wavData, offset, (short)1); // チャンネル数（モノラル）
        offset += 2;
        writeInt(wavData, offset, sampleRate); // サンプルレート
        offset += 4;
        writeInt(wavData, offset, sampleRate * 2); // バイトレート
        offset += 4;
        writeShort(wavData, offset, (short)2); // ブロックアライメント
        offset += 2;
        writeShort(wavData, offset, (short)16); // ビット深度
        offset += 2;
        
        // dataチャンク
        writeString(wavData, offset, "data"); offset += 4;
        writeInt(wavData, offset, dataSize); offset += 4;
        
        // 音声サンプルを生成（2つの周波数を組み合わせて心地よい音に）
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            
            // メインの周波数（600Hz）とハーモニック（1200Hz）を組み合わせ
            double frequency1 = 600.0;
            double frequency2 = 1200.0;
            double sample1 = Math.sin(2 * Math.PI * frequency1 * time);
            double sample2 = Math.sin(2 * Math.PI * frequency2 * time) * 0.3; // ハーモニックは少し小さく
            
            double sample = (sample1 + sample2) / 1.3; // 正規化
            
            // エンベロープを適用（フェードイン・フェードアウト）
            double envelope;
            if (i < numSamples * 0.15) {
                // フェードイン（最初の15%）
                envelope = i / (numSamples * 0.15);
            } else if (i > numSamples * 0.6) {
                // フェードアウト（最後の40%）
                envelope = (numSamples - i) / (numSamples * 0.4);
            } else {
                envelope = 1.0;
            }
            
            // 音量を調整（0.4で少し大きめに）
            sample *= envelope * 0.4;
            
            // 16bit PCMとして書き込み（リトルエンディアン）
            short sampleValue = (short)(sample * Short.MAX_VALUE);
            wavData[offset++] = (byte)(sampleValue & 0xFF);
            wavData[offset++] = (byte)((sampleValue >> 8) & 0xFF);
        }
        
        // 音源ファイルをsoundsディレクトリに保存
        try {
            FileHandle soundFile = Gdx.files.local("sounds/collect_sound.wav");
            soundFile.writeBytes(wavData, false);
            
            if (!soundFile.exists()) {
                Gdx.app.error("SoundManager", "Failed to create collect sound file");
                return null;
            }
            
            Sound sound = Gdx.audio.newSound(soundFile);
            return sound;
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to create collect sound", e);
            return null;
        }
    }
    
    /**
     * 指定されたタイルタイプ用の足音をプログラムで生成してSoundオブジェクトとして返します。
     * @param terrainType タイルタイプ
     * @return 生成されたSoundオブジェクト
     */
    private Sound createFootstepSoundForTerrain(TerrainTile.TerrainType terrainType) {
        switch (terrainType) {
            case GRASS:
                return createGrassFootstepSound();
            case DIRT:
                return createDirtFootstepSound();
            case SAND:
                return createSandFootstepSound();
            case WATER:
                return createWaterFootstepSound();
            case STONE:
                return createStoneFootstepSound();
            case FOREST:
                return createForestFootstepSound();
            case PADDY:
                // 水田は水に近い音
                return createWaterFootstepSound();
            case FARMLAND:
                // 畑は土に近い音
                return createDirtFootstepSound();
            case MARSH:
                // 湿地は水と泥の混合音（水の音を使用）
                return createWaterFootstepSound();
            default:
                return createGrassFootstepSound(); // デフォルトは草
        }
    }
    
    /**
     * 草の足音をプログラムで生成してSoundオブジェクトとして返します。
     * 柔らかく軽い音を生成します。
     */
    private Sound createGrassFootstepSound() {
        int sampleRate = 44100;
        float duration = 0.1f; // 100ミリ秒の短い音
        int numSamples = (int)(sampleRate * duration);
        
        // WAVファイルのヘッダーサイズ
        int dataSize = numSamples * 2; // 16bit = 2 bytes per sample
        int fileSize = 36 + dataSize;
        
        // WAVファイルのバイト配列を作成
        byte[] wavData = new byte[44 + dataSize];
        int offset = 0;
        
        // RIFFヘッダー
        writeString(wavData, offset, "RIFF"); offset += 4;
        writeInt(wavData, offset, fileSize); offset += 4;
        writeString(wavData, offset, "WAVE"); offset += 4;
        
        // fmtチャンク
        writeString(wavData, offset, "fmt "); offset += 4;
        writeInt(wavData, offset, 16); // fmtチャンクサイズ
        offset += 4;
        writeShort(wavData, offset, (short)1); // オーディオフォーマット（PCM）
        offset += 2;
        writeShort(wavData, offset, (short)1); // チャンネル数（モノラル）
        offset += 2;
        writeInt(wavData, offset, sampleRate); // サンプルレート
        offset += 4;
        writeInt(wavData, offset, sampleRate * 2); // バイトレート
        offset += 4;
        writeShort(wavData, offset, (short)2); // ブロックアライメント
        offset += 2;
        writeShort(wavData, offset, (short)16); // ビット深度
        offset += 2;
        
        // dataチャンク
        writeString(wavData, offset, "data"); offset += 4;
        writeInt(wavData, offset, dataSize); offset += 4;
        
        // 音声サンプルを生成（柔らかく軽い草の足音）
        java.util.Random random = new java.util.Random(42); // 再現可能なランダム
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            
            // 柔らかい低周波数（草は柔らかいので音も柔らかく）
            double frequency1 = 80.0; // ベース周波数（低めで柔らかく）
            double frequency2 = 160.0; // ハーモニック（控えめ）
            double sample1 = Math.sin(2 * Math.PI * frequency1 * time);
            double sample2 = Math.sin(2 * Math.PI * frequency2 * time) * 0.15;
            
            // 小さなノイズ成分（草を踏む音）
            double noise = (random.nextDouble() - 0.5) * 0.08;
            
            double sample = (sample1 + sample2 + noise) / 1.23;
            
            // エンベロープを適用（柔らかいアタックとリリース）
            double envelope;
            if (i < numSamples * 0.1) {
                envelope = i / (numSamples * 0.1);
            } else if (i > numSamples * 0.4) {
                envelope = (numSamples - i) / (numSamples * 0.6);
            } else {
                envelope = 1.0;
            }
            
            sample *= envelope * 0.2; // 控えめな音量
            
            short sampleValue = (short)(sample * Short.MAX_VALUE);
            wavData[offset++] = (byte)(sampleValue & 0xFF);
            wavData[offset++] = (byte)((sampleValue >> 8) & 0xFF);
        }
        
        try {
            FileHandle soundFile = Gdx.files.local("sounds/footstep_grass.wav");
            soundFile.writeBytes(wavData, false);
            if (!soundFile.exists()) {
                Gdx.app.error("SoundManager", "Failed to create grass footstep sound file");
                return null;
            }
            return Gdx.audio.newSound(soundFile);
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to create grass footstep sound", e);
            return null;
        }
    }
    
    /**
     * 土の足音をプログラムで生成してSoundオブジェクトとして返します。
     * 中程度の重さの音を生成します。
     */
    private Sound createDirtFootstepSound() {
        int sampleRate = 44100;
        float duration = 0.12f; // 120ミリ秒
        int numSamples = (int)(sampleRate * duration);
        int dataSize = numSamples * 2;
        int fileSize = 36 + dataSize;
        byte[] wavData = new byte[44 + dataSize];
        int offset = 0;
        
        writeString(wavData, offset, "RIFF"); offset += 4;
        writeInt(wavData, offset, fileSize); offset += 4;
        writeString(wavData, offset, "WAVE"); offset += 4;
        writeString(wavData, offset, "fmt "); offset += 4;
        writeInt(wavData, offset, 16); offset += 4;
        writeShort(wavData, offset, (short)1); offset += 2;
        writeShort(wavData, offset, (short)1); offset += 2;
        writeInt(wavData, offset, sampleRate); offset += 4;
        writeInt(wavData, offset, sampleRate * 2); offset += 4;
        writeShort(wavData, offset, (short)2); offset += 2;
        writeShort(wavData, offset, (short)16); offset += 2;
        writeString(wavData, offset, "data"); offset += 4;
        writeInt(wavData, offset, dataSize); offset += 4;
        
        java.util.Random random = new java.util.Random(43);
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            
            // 中程度の周波数（土は少し重め）
            double frequency1 = 110.0;
            double frequency2 = 220.0;
            double frequency3 = 350.0;
            double sample1 = Math.sin(2 * Math.PI * frequency1 * time);
            double sample2 = Math.sin(2 * Math.PI * frequency2 * time) * 0.25;
            double sample3 = Math.sin(2 * Math.PI * frequency3 * time) * 0.08;
            
            // ノイズ成分（土を踏む音）
            double noise = (random.nextDouble() - 0.5) * 0.12;
            
            double sample = (sample1 + sample2 + sample3 + noise) / 1.45;
            
            double envelope;
            if (i < numSamples * 0.06) {
                envelope = i / (numSamples * 0.06);
            } else if (i > numSamples * 0.35) {
                envelope = (numSamples - i) / (numSamples * 0.65);
            } else {
                envelope = 1.0;
            }
            
            sample *= envelope * 0.28;
            
            short sampleValue = (short)(sample * Short.MAX_VALUE);
            wavData[offset++] = (byte)(sampleValue & 0xFF);
            wavData[offset++] = (byte)((sampleValue >> 8) & 0xFF);
        }
        
        try {
            FileHandle soundFile = Gdx.files.local("sounds/footstep_dirt.wav");
            soundFile.writeBytes(wavData, false);
            if (!soundFile.exists()) {
                Gdx.app.error("SoundManager", "Failed to create dirt footstep sound file");
                return null;
            }
            return Gdx.audio.newSound(soundFile);
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to create dirt footstep sound", e);
            return null;
        }
    }
    
    /**
     * 砂の足音をプログラムで生成してSoundオブジェクトとして返します。
     * サラサラした軽い音を生成します。
     */
    private Sound createSandFootstepSound() {
        int sampleRate = 44100;
        float duration = 0.11f; // 110ミリ秒
        int numSamples = (int)(sampleRate * duration);
        int dataSize = numSamples * 2;
        int fileSize = 36 + dataSize;
        byte[] wavData = new byte[44 + dataSize];
        int offset = 0;
        
        writeString(wavData, offset, "RIFF"); offset += 4;
        writeInt(wavData, offset, fileSize); offset += 4;
        writeString(wavData, offset, "WAVE"); offset += 4;
        writeString(wavData, offset, "fmt "); offset += 4;
        writeInt(wavData, offset, 16); offset += 4;
        writeShort(wavData, offset, (short)1); offset += 2;
        writeShort(wavData, offset, (short)1); offset += 2;
        writeInt(wavData, offset, sampleRate); offset += 4;
        writeInt(wavData, offset, sampleRate * 2); offset += 4;
        writeShort(wavData, offset, (short)2); offset += 2;
        writeShort(wavData, offset, (short)16); offset += 2;
        writeString(wavData, offset, "data"); offset += 4;
        writeInt(wavData, offset, dataSize); offset += 4;
        
        java.util.Random random = new java.util.Random(44);
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            
            // 高めの周波数でサラサラした音
            double frequency1 = 120.0;
            double frequency2 = 250.0;
            double frequency3 = 500.0; // 高周波成分を多く（サラサラ感）
            double sample1 = Math.sin(2 * Math.PI * frequency1 * time);
            double sample2 = Math.sin(2 * Math.PI * frequency2 * time) * 0.2;
            double sample3 = Math.sin(2 * Math.PI * frequency3 * time) * 0.15; // 高周波を強調
            
            // ノイズ成分を多く（砂のサラサラ感）
            double noise = (random.nextDouble() - 0.5) * 0.2;
            
            double sample = (sample1 + sample2 + sample3 + noise) / 1.55;
            
            double envelope;
            if (i < numSamples * 0.08) {
                envelope = i / (numSamples * 0.08);
            } else if (i > numSamples * 0.4) {
                envelope = (numSamples - i) / (numSamples * 0.6);
            } else {
                envelope = 1.0;
            }
            
            sample *= envelope * 0.22;
            
            short sampleValue = (short)(sample * Short.MAX_VALUE);
            wavData[offset++] = (byte)(sampleValue & 0xFF);
            wavData[offset++] = (byte)((sampleValue >> 8) & 0xFF);
        }
        
        try {
            FileHandle soundFile = Gdx.files.local("sounds/footstep_sand.wav");
            soundFile.writeBytes(wavData, false);
            if (!soundFile.exists()) {
                Gdx.app.error("SoundManager", "Failed to create sand footstep sound file");
                return null;
            }
            return Gdx.audio.newSound(soundFile);
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to create sand footstep sound", e);
            return null;
        }
    }
    
    /**
     * 水の足音をプログラムで生成してSoundオブジェクトとして返します。
     * 水を踏む音を生成します（実際には水は通過できないので使用されない可能性があります）。
     */
    private Sound createWaterFootstepSound() {
        int sampleRate = 44100;
        float duration = 0.13f; // 130ミリ秒
        int numSamples = (int)(sampleRate * duration);
        int dataSize = numSamples * 2;
        int fileSize = 36 + dataSize;
        byte[] wavData = new byte[44 + dataSize];
        int offset = 0;
        
        writeString(wavData, offset, "RIFF"); offset += 4;
        writeInt(wavData, offset, fileSize); offset += 4;
        writeString(wavData, offset, "WAVE"); offset += 4;
        writeString(wavData, offset, "fmt "); offset += 4;
        writeInt(wavData, offset, 16); offset += 4;
        writeShort(wavData, offset, (short)1); offset += 2;
        writeShort(wavData, offset, (short)1); offset += 2;
        writeInt(wavData, offset, sampleRate); offset += 4;
        writeInt(wavData, offset, sampleRate * 2); offset += 4;
        writeShort(wavData, offset, (short)2); offset += 2;
        writeShort(wavData, offset, (short)16); offset += 2;
        writeString(wavData, offset, "data"); offset += 4;
        writeInt(wavData, offset, dataSize); offset += 4;
        
        java.util.Random random = new java.util.Random(45);
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            
            // 水を踏む音（中〜高周波）
            double frequency1 = 150.0;
            double frequency2 = 300.0;
            double frequency3 = 600.0;
            double sample1 = Math.sin(2 * Math.PI * frequency1 * time);
            double sample2 = Math.sin(2 * Math.PI * frequency2 * time) * 0.3;
            double sample3 = Math.sin(2 * Math.PI * frequency3 * time) * 0.2;
            
            // 水の音らしさを出すためのノイズ
            double noise = (random.nextDouble() - 0.5) * 0.15;
            
            double sample = (sample1 + sample2 + sample3 + noise) / 1.65;
            
            double envelope;
            if (i < numSamples * 0.05) {
                envelope = i / (numSamples * 0.05);
            } else if (i > numSamples * 0.3) {
                envelope = (numSamples - i) / (numSamples * 0.7);
            } else {
                envelope = 1.0;
            }
            
            sample *= envelope * 0.25;
            
            short sampleValue = (short)(sample * Short.MAX_VALUE);
            wavData[offset++] = (byte)(sampleValue & 0xFF);
            wavData[offset++] = (byte)((sampleValue >> 8) & 0xFF);
        }
        
        try {
            FileHandle soundFile = Gdx.files.local("sounds/footstep_water.wav");
            soundFile.writeBytes(wavData, false);
            if (!soundFile.exists()) {
                Gdx.app.error("SoundManager", "Failed to create water footstep sound file");
                return null;
            }
            return Gdx.audio.newSound(soundFile);
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to create water footstep sound", e);
            return null;
        }
    }
    
    /**
     * 岩の足音をプログラムで生成してSoundオブジェクトとして返します。
     * 硬くカチカチした音を生成します。
     */
    private Sound createStoneFootstepSound() {
        int sampleRate = 44100;
        float duration = 0.1f; // 100ミリ秒
        int numSamples = (int)(sampleRate * duration);
        int dataSize = numSamples * 2;
        int fileSize = 36 + dataSize;
        byte[] wavData = new byte[44 + dataSize];
        int offset = 0;
        
        writeString(wavData, offset, "RIFF"); offset += 4;
        writeInt(wavData, offset, fileSize); offset += 4;
        writeString(wavData, offset, "WAVE"); offset += 4;
        writeString(wavData, offset, "fmt "); offset += 4;
        writeInt(wavData, offset, 16); offset += 4;
        writeShort(wavData, offset, (short)1); offset += 2;
        writeShort(wavData, offset, (short)1); offset += 2;
        writeInt(wavData, offset, sampleRate); offset += 4;
        writeInt(wavData, offset, sampleRate * 2); offset += 4;
        writeShort(wavData, offset, (short)2); offset += 2;
        writeShort(wavData, offset, (short)16); offset += 2;
        writeString(wavData, offset, "data"); offset += 4;
        writeInt(wavData, offset, dataSize); offset += 4;
        
        java.util.Random random = new java.util.Random(46);
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            
            // 硬い高周波成分（岩は硬いのでカチカチした音）
            double frequency1 = 130.0;
            double frequency2 = 260.0;
            double frequency3 = 520.0;
            double frequency4 = 800.0; // 高周波成分を追加
            double sample1 = Math.sin(2 * Math.PI * frequency1 * time);
            double sample2 = Math.sin(2 * Math.PI * frequency2 * time) * 0.3;
            double sample3 = Math.sin(2 * Math.PI * frequency3 * time) * 0.2;
            double sample4 = Math.sin(2 * Math.PI * frequency4 * time) * 0.1;
            
            // ノイズ成分（控えめ）
            double noise = (random.nextDouble() - 0.5) * 0.08;
            
            double sample = (sample1 + sample2 + sample3 + sample4 + noise) / 1.88;
            
            // 急激なアタックとリリース（硬い音）
            double envelope;
            if (i < numSamples * 0.03) {
                envelope = i / (numSamples * 0.03);
            } else if (i > numSamples * 0.25) {
                envelope = (numSamples - i) / (numSamples * 0.75);
            } else {
                envelope = 1.0;
            }
            
            sample *= envelope * 0.3; // 少し大きめの音量
            
            short sampleValue = (short)(sample * Short.MAX_VALUE);
            wavData[offset++] = (byte)(sampleValue & 0xFF);
            wavData[offset++] = (byte)((sampleValue >> 8) & 0xFF);
        }
        
        try {
            FileHandle soundFile = Gdx.files.local("sounds/footstep_stone.wav");
            soundFile.writeBytes(wavData, false);
            if (!soundFile.exists()) {
                Gdx.app.error("SoundManager", "Failed to create stone footstep sound file");
                return null;
            }
            return Gdx.audio.newSound(soundFile);
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to create stone footstep sound", e);
            return null;
        }
    }
    
    /**
     * 森の足音をプログラムで生成してSoundオブジェクトとして返します。
     * 草に似ているが少し重めで、葉っぱの音も含む音を生成します。
     */
    private Sound createForestFootstepSound() {
        int sampleRate = 44100;
        float duration = 0.12f; // 120ミリ秒
        int numSamples = (int)(sampleRate * duration);
        int dataSize = numSamples * 2;
        int fileSize = 36 + dataSize;
        byte[] wavData = new byte[44 + dataSize];
        int offset = 0;
        
        writeString(wavData, offset, "RIFF"); offset += 4;
        writeInt(wavData, offset, fileSize); offset += 4;
        writeString(wavData, offset, "WAVE"); offset += 4;
        writeString(wavData, offset, "fmt "); offset += 4;
        writeInt(wavData, offset, 16); offset += 4;
        writeShort(wavData, offset, (short)1); offset += 2;
        writeShort(wavData, offset, (short)1); offset += 2;
        writeInt(wavData, offset, sampleRate); offset += 4;
        writeInt(wavData, offset, sampleRate * 2); offset += 4;
        writeShort(wavData, offset, (short)2); offset += 2;
        writeShort(wavData, offset, (short)16); offset += 2;
        writeString(wavData, offset, "data"); offset += 4;
        writeInt(wavData, offset, dataSize); offset += 4;
        
        java.util.Random random = new java.util.Random(47);
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            
            // 草に似ているが少し重め
            double frequency1 = 90.0; // ベース周波数（草より少し低め）
            double frequency2 = 180.0;
            double frequency3 = 360.0;
            double frequency4 = 450.0; // 葉っぱの音らしさ（高周波）
            double sample1 = Math.sin(2 * Math.PI * frequency1 * time);
            double sample2 = Math.sin(2 * Math.PI * frequency2 * time) * 0.22;
            double sample3 = Math.sin(2 * Math.PI * frequency3 * time) * 0.12;
            double sample4 = Math.sin(2 * Math.PI * frequency4 * time) * 0.1; // 葉っぱの音
            
            // ノイズ成分（葉っぱや枝を踏む音）
            double noise = (random.nextDouble() - 0.5) * 0.12;
            
            double sample = (sample1 + sample2 + sample3 + sample4 + noise) / 1.56;
            
            double envelope;
            if (i < numSamples * 0.08) {
                envelope = i / (numSamples * 0.08);
            } else if (i > numSamples * 0.4) {
                envelope = (numSamples - i) / (numSamples * 0.6);
            } else {
                envelope = 1.0;
            }
            
            sample *= envelope * 0.24;
            
            short sampleValue = (short)(sample * Short.MAX_VALUE);
            wavData[offset++] = (byte)(sampleValue & 0xFF);
            wavData[offset++] = (byte)((sampleValue >> 8) & 0xFF);
        }
        
        try {
            FileHandle soundFile = Gdx.files.local("sounds/footstep_forest.wav");
            soundFile.writeBytes(wavData, false);
            if (!soundFile.exists()) {
                Gdx.app.error("SoundManager", "Failed to create forest footstep sound file");
                return null;
            }
            return Gdx.audio.newSound(soundFile);
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to create forest footstep sound", e);
            return null;
        }
    }

    /**
     * クラフト成功音をプログラムで生成してSoundオブジェクトとして返します。
     * 少し「キラッ」とする上昇音（短いアルペジオ風）を生成します。
     */
    private Sound createCraftSound() {
        int sampleRate = 44100;
        float duration = 0.18f; // 180ms
        int numSamples = (int)(sampleRate * duration);

        int dataSize = numSamples * 2;
        int fileSize = 36 + dataSize;

        byte[] wavData = new byte[44 + dataSize];
        int offset = 0;

        // RIFFヘッダー
        writeString(wavData, offset, "RIFF"); offset += 4;
        writeInt(wavData, offset, fileSize); offset += 4;
        writeString(wavData, offset, "WAVE"); offset += 4;

        // fmtチャンク
        writeString(wavData, offset, "fmt "); offset += 4;
        writeInt(wavData, offset, 16); offset += 4;
        writeShort(wavData, offset, (short)1); offset += 2; // PCM
        writeShort(wavData, offset, (short)1); offset += 2; // mono
        writeInt(wavData, offset, sampleRate); offset += 4;
        writeInt(wavData, offset, sampleRate * 2); offset += 4;
        writeShort(wavData, offset, (short)2); offset += 2;
        writeShort(wavData, offset, (short)16); offset += 2;

        // dataチャンク
        writeString(wavData, offset, "data"); offset += 4;
        writeInt(wavData, offset, dataSize); offset += 4;

        // 3段階の上昇（C-E-G相当の比率）を短く鳴らす
        double baseFreq = 660.0; // 少し高めで「クラフト感」
        double[] ratios = new double[] { 1.0, 1.2599, 1.4983 };
        for (int i = 0; i < numSamples; i++) {
            double t = i / (double) sampleRate;

            // セグメント（0..1）
            double p = i / (double) numSamples;
            int seg = (p < 0.35) ? 0 : (p < 0.7 ? 1 : 2);
            double freq = baseFreq * ratios[seg];

            double sample = Math.sin(2 * Math.PI * freq * t);
            // うっすら倍音
            sample += Math.sin(2 * Math.PI * freq * 2.0 * t) * 0.12;
            sample /= 1.12;

            // エンベロープ（急アタック＋短いリリース）
            double env;
            if (p < 0.08) env = p / 0.08;
            else if (p > 0.75) env = Math.max(0.0, (1.0 - p) / 0.25);
            else env = 1.0;

            // 少しキラっとさせるため後半をわずかに強調
            double sparkle = 0.9 + 0.2 * p;
            sample *= env * sparkle * 0.35;

            short v = (short)(sample * Short.MAX_VALUE);
            wavData[offset++] = (byte)(v & 0xFF);
            wavData[offset++] = (byte)((v >> 8) & 0xFF);
        }

        try {
            FileHandle soundFile = Gdx.files.local("sounds/craft_sound.wav");
            soundFile.writeBytes(wavData, false);
            if (!soundFile.exists()) {
                Gdx.app.error("SoundManager", "Failed to create craft sound file");
                return null;
            }
            return Gdx.audio.newSound(soundFile);
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to create craft sound", e);
            return null;
        }
    }
    
    /**
     * バイト配列に文字列を書き込みます。
     */
    private void writeString(byte[] data, int offset, String str) {
        byte[] bytes = str.getBytes();
        System.arraycopy(bytes, 0, data, offset, bytes.length);
    }
    
    /**
     * バイト配列にintをリトルエンディアンで書き込みます。
     */
    private void writeInt(byte[] data, int offset, int value) {
        data[offset] = (byte)(value & 0xFF);
        data[offset + 1] = (byte)((value >> 8) & 0xFF);
        data[offset + 2] = (byte)((value >> 16) & 0xFF);
        data[offset + 3] = (byte)((value >> 24) & 0xFF);
    }
    
    /**
     * バイト配列にshortをリトルエンディアンで書き込みます。
     */
    private void writeShort(byte[] data, int offset, short value) {
        data[offset] = (byte)(value & 0xFF);
        data[offset + 1] = (byte)((value >> 8) & 0xFF);
    }
    
    /**
     * ホバー音を再生します。
     */
    public void playHoverSound() {
        if (!isInitialized || hoverSound == null || soundSettings.isMuted()) {
            return;
        }
        
        // クールダウンをチェック（連続再生を防ぐ）
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHoverSoundTime < HOVER_SOUND_COOLDOWN_MS) {
            return;
        }
        
        float volume = soundSettings.getMasterVolume();
        if (volume > 0) {
            hoverSound.play(volume * 0.3f); // ホバー音は少し小さめに
            lastHoverSoundTime = currentTime;
        }
    }
    
    /**
     * アイテム取得音を再生します。
     */
    public void playCollectSound() {
        if (!isInitialized || collectSound == null || soundSettings.isMuted()) {
            return;
        }
        
        // クールダウンをチェック（連続再生を防ぐ）
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCollectSoundTime < COLLECT_SOUND_COOLDOWN_MS) {
            return;
        }
        
        float volume = soundSettings.getMasterVolume();
        if (volume > 0) {
            collectSound.play(volume * 0.5f); // 取得音は少し大きめに
            lastCollectSoundTime = currentTime;
        }
    }
    
    /**
     * 足音を再生します（後方互換性のため、デフォルトの足音を再生）。
     */
    public void playFootstepSound() {
        playFootstepSound(TerrainTile.TerrainType.GRASS); // デフォルトは草
    }
    
    /**
     * 指定されたタイルタイプに応じた足音を再生します。
     * @param terrainType タイルタイプ
     */
    public void playFootstepSound(TerrainTile.TerrainType terrainType) {
        if (!isInitialized || soundSettings.isMuted()) {
            return;
        }
        
        // クールダウンをチェック（連続再生を防ぐ）
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFootstepSoundTime < FOOTSTEP_SOUND_COOLDOWN_MS) {
            return;
        }
        
        // タイルタイプに応じた足音を取得
        Sound sound = footstepSounds != null ? footstepSounds.get(terrainType) : null;
        if (sound == null) {
            // フォールバック：デフォルトの足音を使用
            sound = footstepSound;
        }
        
        if (sound == null) {
            return;
        }
        
        float volume = soundSettings.getMasterVolume();
        if (volume > 0) {
            sound.play(volume * 0.25f); // 足音は控えめに
            lastFootstepSoundTime = currentTime;
        }
    }

    /**
     * クラフト成功音を再生します。
     */
    public void playCraftSound() {
        if (!isInitialized || craftSound == null || soundSettings.isMuted()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCraftSoundTime < CRAFT_SOUND_COOLDOWN_MS) {
            return;
        }

        float volume = soundSettings.getMasterVolume();
        if (volume > 0) {
            // 少し目立たせたいので取得音よりちょい強め
            craftSound.play(volume * 0.55f);
            lastCraftSoundTime = currentTime;
        }
    }
    
    /**
     * リソースを解放します。
     */
    @Override
    public void dispose() {
        if (hoverSound != null) {
            hoverSound.dispose();
            hoverSound = null;
        }
        if (collectSound != null) {
            collectSound.dispose();
            collectSound = null;
        }
        if (footstepSound != null) {
            footstepSound.dispose();
            footstepSound = null;
        }
        if (footstepSounds != null) {
            for (Sound sound : footstepSounds.values()) {
                if (sound != null) {
                    sound.dispose();
                }
            }
            footstepSounds.clear();
            footstepSounds = null;
        }
        if (craftSound != null) {
            craftSound.dispose();
            craftSound = null;
        }
        isInitialized = false;
    }
}
