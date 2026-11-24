package org.telegram.ui.Components.glass.motion;

import android.animation.ValueAnimator;
import android.os.Build;
import android.provider.Settings;
import android.view.Choreographer;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.Components.glass.motion.GlassSpringSimulation;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassFloatSpring implements Choreographer.FrameCallback {
    private Choreographer choreographer;
    private double elapsedSimulationSeconds;
    private EndListener endListener;
    private boolean frameScheduled;
    private long previousFrameNanos;
    private boolean running;
    private final GlassSpringSimulation.State sampledState = new GlassSpringSimulation.State();
    private GlassSpringSimulation simulation;
    private GlassSpringSpec spec;
    private float targetValue;
    private UpdateListener updateListener;
    private float value;
    private float velocity;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public static final class Api26Impl {
        static float getDurationScale() {
            float durationScale;
            durationScale = ValueAnimator.getDurationScale();
            return durationScale;
        }
    }

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public interface EndListener {
        void onSpringEnd(GlassFloatSpring glassFloatSpring, boolean z, float f, float f2);
    }

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public interface UpdateListener {
        void onSpringUpdate(GlassFloatSpring glassFloatSpring, float f, float f2);
    }

    public GlassFloatSpring(float f, GlassSpringSpec glassSpringSpec) {
        requireFinite("initialValue", f);
        if (glassSpringSpec == null) {
            throw new NullPointerException("spec");
        }
        this.value = f;
        this.targetValue = f;
        this.spec = glassSpringSpec;
    }

    private void beginSimulation(float f, long j) {
        this.simulation = new GlassSpringSimulation(this.spec, this.value, this.velocity, f);
        this.previousFrameNanos = j;
        this.elapsedSimulationSeconds = 0.0d;
        this.running = true;
        scheduleFrame();
    }

    private void dispatchEnd(boolean z) {
        EndListener endListener = this.endListener;
        if (endListener != null) {
            endListener.onSpringEnd(this, z, this.value, this.velocity);
        }
    }

    private void dispatchUpdate() {
        UpdateListener updateListener = this.updateListener;
        if (updateListener != null) {
            updateListener.onSpringUpdate(this, this.value, this.velocity);
        }
    }

    private static float getSystemDurationScale() {
        if (Build.VERSION.SDK_INT >= 26) {
            return sanitizeDurationScale(Api26Impl.getDurationScale());
        }
        try {
            if (ApplicationLoader.applicationContext != null) {
                return sanitizeDurationScale(Settings.Global.getFloat(ApplicationLoader.applicationContext.getContentResolver(), "animator_duration_scale", 1.0f));
            }
            return 1.0f;
        } catch (Throwable unused) {
            return 1.0f;
        }
    }

    private static void requireFinite(String str, float f) {
        if (Float.isNaN(f) || Float.isInfinite(f)) {
            throw new IllegalArgumentException(str + " must be finite");
        }
    }

    private void sampleAt(long j) {
        if (!this.running || this.simulation == null) {
            return;
        }
        long max = Math.max(0L, j - this.previousFrameNanos);
        this.previousFrameNanos = j;
        double max2 = this.elapsedSimulationSeconds + ((max * 1.0E-9d) / Math.max(1.0E-4f, getSystemDurationScale()));
        this.elapsedSimulationSeconds = max2;
        this.simulation.evaluate(max2, this.sampledState);
        GlassSpringSimulation.State state = this.sampledState;
        this.value = state.value;
        this.velocity = state.velocity;
    }

    private static float sanitizeDurationScale(float f) {
        if (Float.isNaN(f) || Float.isInfinite(f)) {
            return 1.0f;
        }
        return Math.max(0.0f, f);
    }

    private void scheduleFrame() {
        if (this.frameScheduled) {
            return;
        }
        if (this.choreographer == null) {
            this.choreographer = Choreographer.getInstance();
        }
        this.frameScheduled = true;
        this.choreographer.postFrameCallback(this);
    }

    private void stopFrames() {
        Choreographer choreographer;
        if (this.frameScheduled && (choreographer = this.choreographer) != null) {
            choreographer.removeFrameCallback(this);
        }
        this.frameScheduled = false;
    }

    public void animateTo(float f) {
        animateTo(f, Float.NaN);
    }

    public void animateTo(float f, float f2) {
        requireFinite("newTarget", f);
        long nanoTime = System.nanoTime();
        if (this.running) {
            sampleAt(nanoTime);
        }
        if (!Float.isNaN(f2)) {
            requireFinite("startVelocity", f2);
            this.velocity = f2;
        }
        this.targetValue = f;
        if (getSystemDurationScale() == 0.0f) {
            this.value = this.targetValue;
            this.velocity = 0.0f;
            this.simulation = null;
            this.running = false;
            stopFrames();
            dispatchUpdate();
            dispatchEnd(false);
        } else if (this.spec.getVisibilityThreshold() < Math.abs(this.value - this.targetValue) || this.spec.getVelocityThreshold() < Math.abs(this.velocity)) {
            beginSimulation(f, nanoTime);
        } else {
            this.value = this.targetValue;
            this.velocity = 0.0f;
            this.simulation = null;
            this.running = false;
            stopFrames();
            dispatchUpdate();
            dispatchEnd(false);
        }
    }

    public void cancel() {
        if (this.running) {
            sampleAt(System.nanoTime());
            this.running = false;
            this.simulation = null;
            stopFrames();
            dispatchUpdate();
            dispatchEnd(true);
        }
    }

    @Override // android.view.Choreographer.FrameCallback
    public void doFrame(long j) {
        this.frameScheduled = false;
        if (!this.running || this.simulation == null) {
            return;
        }
        if (getSystemDurationScale() == 0.0f) {
            skipToEnd();
            return;
        }
        GlassSpringSimulation glassSpringSimulation = this.simulation;
        sampleAt(j);
        dispatchUpdate();
        if (this.running && this.simulation == glassSpringSimulation) {
            if (!glassSpringSimulation.isAtEquilibrium(this.value, this.velocity)) {
                scheduleFrame();
                return;
            }
            this.value = this.targetValue;
            this.velocity = 0.0f;
            this.running = false;
            this.simulation = null;
            dispatchUpdate();
            dispatchEnd(false);
        }
    }

    public float getTargetValue() {
        return this.targetValue;
    }

    public float getValue() {
        return this.value;
    }

    public boolean isRunning() {
        return this.running;
    }

    public GlassFloatSpring setEndListener(EndListener endListener) {
        this.endListener = endListener;
        return this;
    }

    public GlassFloatSpring setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
        return this;
    }

    public void skipToEnd() {
        if (this.running || this.value != this.targetValue) {
            this.value = this.targetValue;
            this.velocity = 0.0f;
            this.running = false;
            this.simulation = null;
            stopFrames();
            dispatchUpdate();
            dispatchEnd(false);
        }
    }

    public void snapTo(float f) {
        requireFinite("newValue", f);
        stopFrames();
        this.simulation = null;
        this.running = false;
        this.value = f;
        this.targetValue = f;
        this.velocity = 0.0f;
        dispatchUpdate();
    }
}
