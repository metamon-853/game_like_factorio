package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.utils.Disposable;

/**
 * サウンド効果を管理するクラス。
 */
public class SoundManager implements Disposable {
    private AudioDevice audioDevice;
    private SoundSettings soundSettings;
    private short[] hoverSoundSamples;
    private boolean isInitialized = false;
    
    /**
     * SoundManagerを初期化します。
     * @param soundSettings サウンド設定
     */
    public SoundManager(SoundSettings soundSettings) {
        this.soundSettings = soundSettings;
        initializeAudio();
    }
    
    /**
     * オーディオデバイスを初期化します。
     */
    private void initializeAudio() {
        try {
            // サンプルレート44.1kHz、モノラルでオーディオデバイスを作成
            audioDevice = Gdx.audio.newAudioDevice(44100, true);
            generateHoverSound();
            isInitialized = true;
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to initialize audio device", e);
            isInitialized = false;
        }
    }
    
    /**
     * ホバー音をプログラムで生成します。
     * 短い、心地よい「ピッ」という音を生成します。
     */
    private void generateHoverSound() {
        int sampleRate = 44100;
        float frequency = 800.0f; // 800Hzの高めの音（心地よい音）
        float duration = 0.08f; // 80ミリ秒の短い音
        int numSamples = (int)(sampleRate * duration);
        hoverSoundSamples = new short[numSamples];
        
        // エンベロープ（音量の変化）を適用して、より心地よい音にする
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
            
            hoverSoundSamples[i] = (short)(sample * Short.MAX_VALUE);
        }
    }
    
    /**
     * ホバー音を再生します。
     */
    public void playHoverSound() {
        if (isInitialized && audioDevice != null && hoverSoundSamples != null && !soundSettings.isMuted()) {
            float volume = soundSettings.getMasterVolume();
            if (volume > 0) {
                // 音量を適用したサンプルを作成
                short[] volumeAdjustedSamples = new short[hoverSoundSamples.length];
                for (int i = 0; i < hoverSoundSamples.length; i++) {
                    volumeAdjustedSamples[i] = (short)(hoverSoundSamples[i] * volume);
                }
                audioDevice.writeSamples(volumeAdjustedSamples, 0, volumeAdjustedSamples.length);
            }
        }
    }
    
    /**
     * リソースを解放します。
     */
    @Override
    public void dispose() {
        if (audioDevice != null) {
            audioDevice.dispose();
            audioDevice = null;
        }
        hoverSoundSamples = null;
        isInitialized = false;
    }
}
