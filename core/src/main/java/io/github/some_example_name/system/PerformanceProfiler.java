package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Map;

/**
 * パフォーマンスプロファイリングを行うクラス。
 * 
 * <p>このクラスは、ゲームの各処理の実行時間を計測し、
 * パフォーマンスのボトルネックを特定するために使用します。</p>
 * 
 * <p>使用例：</p>
 * <pre>
 * PerformanceProfiler profiler = PerformanceProfiler.getInstance();
 * profiler.startSection("render");
 * // 描画処理
 * profiler.endSection("render");
 * profiler.logResults();
 * </pre>
 * 
 * @author game_like_factorio
 * @version 1.0.0
 */
public class PerformanceProfiler {
    private static PerformanceProfiler instance;
    
    private Map<String, SectionData> sections;
    private Map<String, Long> sectionStartTimes;
    private boolean enabled;
    
    // フレーム統計
    private long frameCount;
    private long lastFrameTime;
    private float averageFrameTime;
    private float minFrameTime;
    private float maxFrameTime;
    
    private static final int STATS_UPDATE_INTERVAL = 60; // 60フレームごとに統計を更新
    
    /**
     * セクションデータを保持するクラス。
     */
    private static class SectionData {
        long totalTime; // 合計時間（ナノ秒）
        long callCount; // 呼び出し回数
        float averageTime; // 平均時間（ミリ秒）
        float minTime; // 最小時間（ミリ秒）
        float maxTime; // 最大時間（ミリ秒）
        
        SectionData() {
            this.totalTime = 0;
            this.callCount = 0;
            this.averageTime = 0f;
            this.minTime = Float.MAX_VALUE;
            this.maxTime = 0f;
        }
    }
    
    /**
     * PerformanceProfilerのシングルトンインスタンスを取得します。
     * @return PerformanceProfilerのインスタンス
     */
    public static PerformanceProfiler getInstance() {
        if (instance == null) {
            instance = new PerformanceProfiler();
        }
        return instance;
    }
    
    /**
     * PerformanceProfilerを初期化します。
     */
    private PerformanceProfiler() {
        this.sections = new HashMap<>();
        this.sectionStartTimes = new HashMap<>();
        this.enabled = false; // デフォルトでは無効（パフォーマンスオーバーヘッドを避ける）
        this.frameCount = 0;
        this.lastFrameTime = System.nanoTime();
        this.averageFrameTime = 0f;
        this.minFrameTime = Float.MAX_VALUE;
        this.maxFrameTime = 0f;
    }
    
    /**
     * プロファイリングを有効化/無効化します。
     * @param enabled 有効化する場合true
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            clear();
        }
    }
    
    /**
     * プロファイリングが有効かどうかを取得します。
     * @return 有効な場合true
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * セクションの計測を開始します。
     * @param sectionName セクション名
     */
    public void startSection(String sectionName) {
        if (!enabled) {
            return;
        }
        
        sectionStartTimes.put(sectionName, System.nanoTime());
    }
    
    /**
     * セクションの計測を終了します。
     * @param sectionName セクション名
     */
    public void endSection(String sectionName) {
        if (!enabled) {
            return;
        }
        
        Long startTime = sectionStartTimes.remove(sectionName);
        if (startTime == null) {
            Gdx.app.log("PerformanceProfiler", "Section not started: " + sectionName);
            return;
        }
        
        long duration = System.nanoTime() - startTime;
        float durationMs = duration / 1_000_000f; // ナノ秒からミリ秒に変換
        
        SectionData data = sections.get(sectionName);
        if (data == null) {
            data = new SectionData();
            sections.put(sectionName, data);
        }
        
        data.totalTime += duration;
        data.callCount++;
        data.averageTime = (data.totalTime / data.callCount) / 1_000_000f;
        data.minTime = Math.min(data.minTime, durationMs);
        data.maxTime = Math.max(data.maxTime, durationMs);
    }
    
    /**
     * フレームの計測を開始します。
     */
    public void startFrame() {
        if (!enabled) {
            return;
        }
        
        lastFrameTime = System.nanoTime();
    }
    
    /**
     * フレームの計測を終了します。
     */
    public void endFrame() {
        if (!enabled) {
            return;
        }
        
        long frameTime = System.nanoTime() - lastFrameTime;
        float frameTimeMs = frameTime / 1_000_000f;
        
        frameCount++;
        
        // 統計を更新
        if (frameCount % STATS_UPDATE_INTERVAL == 0) {
            averageFrameTime = frameTimeMs;
            minFrameTime = Math.min(minFrameTime, frameTimeMs);
            maxFrameTime = Math.max(maxFrameTime, frameTimeMs);
        }
    }
    
    /**
     * 結果をログに出力します。
     */
    public void logResults() {
        if (!enabled || sections.isEmpty()) {
            return;
        }
        
        Gdx.app.log("PerformanceProfiler", "=== Performance Results ===");
        Gdx.app.log("PerformanceProfiler", String.format("Frame: Avg=%.2fms, Min=%.2fms, Max=%.2fms, FPS=%.1f", 
            averageFrameTime, minFrameTime, maxFrameTime, 1000f / averageFrameTime));
        
        for (Map.Entry<String, SectionData> entry : sections.entrySet()) {
            SectionData data = entry.getValue();
            Gdx.app.log("PerformanceProfiler", String.format(
                "  %s: Avg=%.2fms, Min=%.2fms, Max=%.2fms, Calls=%d, Total=%.2fms",
                entry.getKey(), data.averageTime, data.minTime, data.maxTime, 
                data.callCount, data.totalTime / 1_000_000f));
        }
        
        Gdx.app.log("PerformanceProfiler", "========================");
    }
    
    /**
     * すべての統計をクリアします。
     */
    public void clear() {
        sections.clear();
        sectionStartTimes.clear();
        frameCount = 0;
        averageFrameTime = 0f;
        minFrameTime = Float.MAX_VALUE;
        maxFrameTime = 0f;
    }
    
    /**
     * 指定されたセクションの平均時間を取得します。
     * @param sectionName セクション名
     * @return 平均時間（ミリ秒）、セクションが存在しない場合は0
     */
    public float getAverageTime(String sectionName) {
        SectionData data = sections.get(sectionName);
        return data != null ? data.averageTime : 0f;
    }
    
    /**
     * 現在のFPSを取得します。
     * @return FPS（フレーム/秒）
     */
    public float getFPS() {
        if (averageFrameTime > 0) {
            return 1000f / averageFrameTime;
        }
        return 0f;
    }
}
