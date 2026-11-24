package org.telegram.ui.Components.glass.motion;

import android.view.Choreographer;
import org.telegram.ui.Components.glass.motion.GlassSpringSimulation;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassPointSpring implements Choreographer.FrameCallback {
    private Choreographer choreographer;
    private boolean frameScheduled;
    private boolean running;
    private final GlassSpringSimulation.State sampledX = new GlassSpringSimulation.State();
    private final GlassSpringSimulation.State sampledY = new GlassSpringSimulation.State();
    private GlassSpringSimulation simulationX;
    private GlassSpringSimulation simulationY;
    private GlassSpringSpec spec;
    private long startFrameNanos;
    private float targetX;
    private float targetY;
    private UpdateListener updateListener;
    private float velocityX;
    private float velocityY;
    private float x;
    private float y;

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public interface UpdateListener {
        void onSpringUpdate(GlassPointSpring glassPointSpring, float f, float f2, float f3, float f4);
    }

    public GlassPointSpring(float f, float f2, GlassSpringSpec glassSpringSpec) {
        requireFinite("initialX", f);
        requireFinite("initialY", f2);
        if (glassSpringSpec == null) {
            throw new NullPointerException("spec");
        }
        this.targetX = f;
        this.x = f;
        this.targetY = f2;
        this.y = f2;
        this.spec = glassSpringSpec;
    }

    private void beginSimulation(long j) {
        this.simulationX = new GlassSpringSimulation(this.spec, this.x, this.velocityX, this.targetX);
        this.simulationY = new GlassSpringSimulation(this.spec, this.y, this.velocityY, this.targetY);
        this.startFrameNanos = j;
        this.running = true;
        scheduleFrame();
    }

    private void dispatchEnd(boolean z) {
    }

    private void dispatchUpdate() {
        UpdateListener updateListener = this.updateListener;
        if (updateListener != null) {
            updateListener.onSpringUpdate(this, this.x, this.y, this.velocityX, this.velocityY);
        }
    }

    private boolean isAtEquilibrium() {
        return Math.abs(this.x - this.targetX) <= this.spec.getVisibilityThreshold() && Math.abs(this.y - this.targetY) <= this.spec.getVisibilityThreshold() && Math.abs(this.velocityX) <= this.spec.getVelocityThreshold() && Math.abs(this.velocityY) <= this.spec.getVelocityThreshold();
    }

    private static void requireFinite(String str, float f) {
        if (Float.isNaN(f) || Float.isInfinite(f)) {
            throw new IllegalArgumentException(str + " must be finite");
        }
    }

    private void sampleAt(long j) {
        if (!this.running || this.simulationX == null || this.simulationY == null) {
            return;
        }
        double max = Math.max(0L, j - this.startFrameNanos) * 1.0E-9d;
        this.simulationX.evaluate(max, this.sampledX);
        this.simulationY.evaluate(max, this.sampledY);
        GlassSpringSimulation.State state = this.sampledX;
        this.x = state.value;
        GlassSpringSimulation.State state2 = this.sampledY;
        this.y = state2.value;
        this.velocityX = state.velocity;
        this.velocityY = state2.velocity;
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

    public void animateTo(float f, float f2) {
        animateTo(f, f2, Float.NaN, Float.NaN);
    }

    public void animateTo(float f, float f2, float f3, float f4) {
        requireFinite("newTargetX", f);
        requireFinite("newTargetY", f2);
        long nanoTime = System.nanoTime();
        if (this.running) {
            sampleAt(nanoTime);
        }
        if (!Float.isNaN(f3)) {
            requireFinite("startVelocityX", f3);
            this.velocityX = f3;
        }
        if (!Float.isNaN(f4)) {
            requireFinite("startVelocityY", f4);
            this.velocityY = f4;
        }
        this.targetX = f;
        this.targetY = f2;
        if (!isAtEquilibrium()) {
            beginSimulation(nanoTime);
            return;
        }
        this.x = this.targetX;
        this.y = this.targetY;
        this.velocityX = 0.0f;
        this.velocityY = 0.0f;
        this.running = false;
        this.simulationX = null;
        this.simulationY = null;
        stopFrames();
        dispatchUpdate();
        dispatchEnd(false);
    }

    public void cancel() {
        if (this.running) {
            sampleAt(System.nanoTime());
            this.running = false;
            this.simulationX = null;
            this.simulationY = null;
            stopFrames();
            dispatchUpdate();
            dispatchEnd(true);
        }
    }

    @Override // android.view.Choreographer.FrameCallback
    public void doFrame(long j) {
        GlassSpringSimulation glassSpringSimulation;
        GlassSpringSimulation glassSpringSimulation2;
        this.frameScheduled = false;
        if (!this.running || (glassSpringSimulation = this.simulationX) == null || (glassSpringSimulation2 = this.simulationY) == null) {
            return;
        }
        sampleAt(j);
        dispatchUpdate();
        if (this.running && glassSpringSimulation == this.simulationX && glassSpringSimulation2 == this.simulationY) {
            if (!isAtEquilibrium()) {
                scheduleFrame();
                return;
            }
            this.x = this.targetX;
            this.y = this.targetY;
            this.velocityX = 0.0f;
            this.velocityY = 0.0f;
            this.running = false;
            this.simulationX = null;
            this.simulationY = null;
            dispatchUpdate();
            dispatchEnd(false);
        }
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public GlassPointSpring setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
        return this;
    }

    public void snapTo(float f, float f2) {
        requireFinite("newX", f);
        requireFinite("newY", f2);
        stopFrames();
        this.running = false;
        this.simulationX = null;
        this.simulationY = null;
        this.targetX = f;
        this.x = f;
        this.targetY = f2;
        this.y = f2;
        this.velocityX = 0.0f;
        this.velocityY = 0.0f;
        dispatchUpdate();
    }
}
