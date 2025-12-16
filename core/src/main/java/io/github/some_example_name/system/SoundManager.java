package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

/**
 * サウンド効果を管理するクラス。
 */
public class SoundManager implements Disposable {
    private Sound hoverSound;
    private Sound collectSound;
    private Sound footstepSound;
    private SoundSettings soundSettings;
    private boolean isInitialized = false;
    
    // 音の再生クールダウン（連続再生を防ぐ）
    private long lastHoverSoundTime = 0;
    private long lastCollectSoundTime = 0;
    private long lastFootstepSoundTime = 0;
    private static final long HOVER_SOUND_COOLDOWN_MS = 50; // 50ミリ秒のクールダウン
    private static final long COLLECT_SOUND_COOLDOWN_MS = 100; // 100ミリ秒のクールダウン
    private static final long FOOTSTEP_SOUND_COOLDOWN_MS = 150; // 150ミリ秒のクールダウン（足音の間隔）
    
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
            } else {
                Gdx.app.log("SoundManager", "Collect sound created successfully");
            }
            footstepSound = createFootstepSound();
            if (footstepSound == null) {
                Gdx.app.error("SoundManager", "Failed to create footstep sound");
            } else {
                Gdx.app.log("SoundManager", "Footstep sound created successfully");
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
        
        // メモリ内のFileHandleを作成（一時ファイルを作成しない）
        // 注意: LibGDXのSoundクラスはFileHandleを必要とするため、
        // メモリ内のデータを扱うには、一時ファイルを作成する必要があります。
        // ただし、初期化時に一度だけ作成されるため、実行時のパフォーマンスには影響しません。
        FileHandle tempFile = Gdx.files.local(".temp_hover_sound.wav");
        tempFile.writeBytes(wavData, false);
        
        Sound sound = Gdx.audio.newSound(tempFile);
        
        // Soundオブジェクトは非同期で再生されるため、パフォーマンスが良いです
        // 一時ファイルは残しておきます（Soundオブジェクトが参照している可能性があるため）
        
        return sound;
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
        
        // 一時ファイルとして保存してからSoundオブジェクトとして読み込む
        try {
            FileHandle tempFile = Gdx.files.local(".temp_collect_sound.wav");
            tempFile.writeBytes(wavData, false);
            
            if (!tempFile.exists()) {
                Gdx.app.error("SoundManager", "Failed to create collect sound file");
                return null;
            }
            
            Sound sound = Gdx.audio.newSound(tempFile);
            Gdx.app.log("SoundManager", "Collect sound file created: " + tempFile.path());
            return sound;
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to create collect sound", e);
            return null;
        }
    }
    
    /**
     * 足音をプログラムで生成してSoundオブジェクトとして返します。
     * 「トン」という短い音を生成します。
     */
    private Sound createFootstepSound() {
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
        
        // 音声サンプルを生成（リアルな足音の音）
        java.util.Random random = new java.util.Random(42); // 再現可能なランダム
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            
            // 低めの周波数で「トン」という足音らしい音
            double frequency1 = 100.0; // ベース周波数（低め）
            double frequency2 = 200.0; // ハーモニック
            double frequency3 = 400.0; // 高周波成分（控えめ）
            double sample1 = Math.sin(2 * Math.PI * frequency1 * time);
            double sample2 = Math.sin(2 * Math.PI * frequency2 * time) * 0.2;
            double sample3 = Math.sin(2 * Math.PI * frequency3 * time) * 0.05; // 高周波は非常に小さく
            
            // ノイズ成分を追加（地面を踏む音らしさを出す）
            double noise = (random.nextDouble() - 0.5) * 0.1; // 小さなノイズ
            
            double sample = (sample1 + sample2 + sample3 + noise) / 1.35; // 正規化
            
            // エンベロープを適用（急激なアタック、ゆっくりしたリリース）
            double envelope;
            if (i < numSamples * 0.05) {
                // 急激なフェードイン（最初の5%）
                envelope = i / (numSamples * 0.05);
            } else if (i > numSamples * 0.3) {
                // ゆっくりしたフェードアウト（最後の70%）
                envelope = (numSamples - i) / (numSamples * 0.7);
            } else {
                envelope = 1.0;
            }
            
            // 音量を調整（0.25で控えめに）
            sample *= envelope * 0.25;
            
            // 16bit PCMとして書き込み（リトルエンディアン）
            short sampleValue = (short)(sample * Short.MAX_VALUE);
            wavData[offset++] = (byte)(sampleValue & 0xFF);
            wavData[offset++] = (byte)((sampleValue >> 8) & 0xFF);
        }
        
        // 一時ファイルとして保存してからSoundオブジェクトとして読み込む
        try {
            FileHandle tempFile = Gdx.files.local(".temp_footstep_sound.wav");
            tempFile.writeBytes(wavData, false);
            
            if (!tempFile.exists()) {
                Gdx.app.error("SoundManager", "Failed to create footstep sound file");
                return null;
            }
            
            Gdx.app.log("SoundManager", "Footstep sound file created: " + tempFile.path() + ", size: " + tempFile.length());
            Sound sound = Gdx.audio.newSound(tempFile);
            if (sound == null) {
                Gdx.app.error("SoundManager", "Failed to load footstep sound from file");
                return null;
            }
            Gdx.app.log("SoundManager", "Footstep sound loaded successfully");
            return sound;
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to create footstep sound", e);
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
        if (!isInitialized) {
            Gdx.app.log("SoundManager", "playCollectSound: not initialized");
            return;
        }
        if (collectSound == null) {
            Gdx.app.error("SoundManager", "playCollectSound: collectSound is null");
            return;
        }
        if (soundSettings.isMuted()) {
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
            Gdx.app.log("SoundManager", "playCollectSound: played successfully");
        }
    }
    
    /**
     * 足音を再生します。
     */
    public void playFootstepSound() {
        if (!isInitialized) {
            Gdx.app.log("SoundManager", "playFootstepSound: not initialized");
            return;
        }
        if (footstepSound == null) {
            Gdx.app.error("SoundManager", "playFootstepSound: footstepSound is null");
            return;
        }
        if (soundSettings.isMuted()) {
            Gdx.app.log("SoundManager", "playFootstepSound: sound is muted");
            return;
        }
        
        // クールダウンをチェック（連続再生を防ぐ）
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFootstepSoundTime < FOOTSTEP_SOUND_COOLDOWN_MS) {
            return;
        }
        
        float volume = soundSettings.getMasterVolume();
        if (volume > 0) {
            float finalVolume = volume * 0.25f; // 足音は控えめに
            footstepSound.play(finalVolume);
            lastFootstepSoundTime = currentTime;
            Gdx.app.log("SoundManager", "playFootstepSound: played successfully, volume=" + finalVolume + ", masterVolume=" + volume);
        } else {
            Gdx.app.log("SoundManager", "playFootstepSound: volume is 0");
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
        isInitialized = false;
    }
}
