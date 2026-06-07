package edu.group10.monopolydeal.frontend.view;

import java.awt.Toolkit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Provides lightweight UI sound feedback using toolkit beeps.
 */
public class AudioFeedbackService {
    /** Background executor used for non-blocking beep playback. */
    private final ExecutorService soundExecutor = Executors.newSingleThreadExecutor(new SoundThreadFactory());
    private volatile double volume = 0.6;

    /** Updates the normalized volume level. */
    public void setVolume(double volume) {
        this.volume = clamp(volume, 0.0, 1.0);
    }

    /** Plays the sound used when selecting a card. */
    public void playCardSelect() {
        playBeeps(1, 0L);
    }

    /** Plays the sound used after a successful card action. */
    public void playCardPlay() {
        playBeeps(2, 90L);
    }

    /** Plays the short victory sound pattern. */
    public void playVictory() {
        playBeeps(4, 120L);
    }

    /** Plays a sequence of beeps on the background executor. */
    private void playBeeps(int count, long pauseMillis) {
        if (volume <= 0.001 || count <= 0) {
            return;
        }
        soundExecutor.execute(() -> {
            try {
                for (int i = 0; i < count; i++) {
                    Toolkit.getDefaultToolkit().beep();
                    if (i + 1 < count && pauseMillis > 0L) {
                        Thread.sleep(pauseMillis);
                    }
                }
            } catch (Exception exception) {
                Toolkit.getDefaultToolkit().beep();
            }
        });
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class SoundThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "audio-feedback");
            thread.setDaemon(true);
            return thread;
        }
    }
}
