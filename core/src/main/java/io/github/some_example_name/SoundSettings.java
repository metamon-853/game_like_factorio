package io.github.some_example_name;

/**
 * サウンド設定を管理するクラス。
 */
public class SoundSettings {
    private float masterVolume = 1.0f;
    private boolean isMuted = false;
    
    /**
     * マスターボリュームを取得します。
     * @return 0.0f ～ 1.0f の範囲の音量値
     */
    public float getMasterVolume() {
        return masterVolume;
    }
    
    /**
     * マスターボリュームを設定します。
     * @param volume 0.0f ～ 1.0f の範囲の音量値
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateMasterVolume();
    }
    
    /**
     * ミュート状態を取得します。
     * @return ミュート中の場合true
     */
    public boolean isMuted() {
        return isMuted;
    }
    
    /**
     * ミュート状態を設定します。
     * @param muted ミュートする場合true
     */
    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }
    
    /**
     * マスターボリュームを更新します。
     * 将来的に音声を追加したときに、この音量設定が適用されます。
     */
    private void updateMasterVolume() {
        // LibGDXのマスターボリュームを設定
        // 将来的にSoundやMusicを追加したときに、この設定が適用されます
        // 個別のSoundやMusicオブジェクトに対してsetVolume()を呼び出す必要があります
    }
}
