/*
 * This file is a part of Meergram.
 * It is licensed under GNU GPL v. 2 or later.
 *
 * Meerogram R11.3: faithful View-world port of the YTXTube / Glasstube
 * liquid bottom bar physics (com.tensei.ytxtube.GlassMotion.kt +
 * GlassNavBar.kt). Same springs, same constants, same gesture semantics:
 *
 *  - DampedDragAnimation: value spring(1f, 1000f), velocity spring(0.5f, 300f),
 *    pressProgress spring(1f, 1000f), scaleX spring(0.6f, 250f),
 *    scaleY spring(0.7f, 250f); release waits until the value settles within
 *    2.5% of the range before relaxing the press.
 *  - Panel offset: 4dp * sign(fraction) * EaseOut(|fraction|), springing back
 *    with spring(1f, 300f, 0.5f).
 *  - InteractiveHighlight: white radial glow (Plus blend) that follows the
 *    moving pill with spring(0.5f, 300f).
 *
 * Springs are integrated semi-implicitly (k = stiffness,
 * c = 2 * dampingRatio * sqrt(stiffness)) on a fixed 1/120s substep, which
 * matches Compose's spring() for these specs.
 */
package uz.unnarsx.cherrygram.core.ui.mainTabs;

import android.os.SystemClock;

import androidx.annotation.NonNull;

public final class MeeroLiquidPhysics {

    public static final class Spring {
        public float value, velocity, target;
        public final float dampingRatio, stiffness, threshold;

        public Spring(float initial, float target, float dampingRatio, float stiffness, float threshold) {
            this.value = initial;
            this.target = target;
            this.dampingRatio = dampingRatio;
            this.stiffness = stiffness;
            this.threshold = threshold;
        }

        public void snapTo(float v) {
            value = v;
            velocity = 0f;
        }

        /** Semi-implicit Euler on a 1/120s substep. Returns true while still animating. */
        public boolean step(float dt) {
            if (settled()) {
                value = target;
                velocity = 0f;
                return false;
            }
            final float c = 2f * dampingRatio * (float) Math.sqrt(stiffness);
            final float maxStep = 1f / 120f;
            float remaining = dt;
            boolean settledNow = false;
            while (remaining > 0f && !settledNow) {
                final float h = Math.min(maxStep, remaining);
                remaining -= h;
                final float accel = -stiffness * (value - target) - c * velocity;
                velocity += accel * h;
                value += velocity * h;
                if (Math.abs(value - target) < threshold && Math.abs(velocity) < threshold * 400f) {
                    value = target;
                    velocity = 0f;
                    settledNow = true;
                }
            }
            return !settledNow;
        }

        public boolean settled() {
            return Math.abs(value - target) < threshold && Math.abs(velocity) < threshold * 400f;
        }
    }

    /** Compose EaseOut = cubic bezier(0, 0, 0.58, 1). */
    public static float easeOut(float t) {
        if (t <= 0f) return 0f;
        if (t >= 1f) return 1f;
        // Newton-Raphson on the parametric cubic bezier x(u), then y(u).
        final float x1 = 0.58f, y1 = 1f;
        float u = t;
        for (int i = 0; i < 8; i++) {
            final float x = 3f * u * (1f - u) * (1f - u) * 0f + 3f * u * u * (1f - u) * x1 + u * u * u;
            final float dx = 3f * (1f - u) * (1f - u) * 0f + 6f * u * (1f - u) * (x1 - 0f) + 3f * u * u * (1f - x1);
            if (Math.abs(dx) < 1e-6f) break;
            u -= (x - t) / dx;
            u = Math.max(0f, Math.min(1f, u));
        }
        return 3f * u * (1f - u) * (1f - u) * 0f + 3f * u * u * (1f - u) * y1 + u * u * u;
    }

    public final float valueRangeStart, valueRangeEnd;

    // spring(1f, 1000f, 0.001f)
    public final Spring value = new Spring(0f, 0f, 1f, 1000f, 0.001f);
    // spring(0.5f, 300f, 5f) - velocity chases the measured value velocity
    public final Spring velocity = new Spring(0f, 0f, 0.5f, 300f, 5f);
    // spring(1f, 1000f, 0.001f)
    public final Spring pressProgress = new Spring(0f, 0f, 1f, 1000f, 0.001f);
    // spring(0.6f, 250f, 0.001f)
    public final Spring scaleX = new Spring(1f, 1f, 0.6f, 250f, 0.001f);
    // spring(0.7f, 250f, 0.001f)
    public final Spring scaleY = new Spring(1f, 1f, 0.7f, 250f, 0.001f);

    /** Panel offset raw px; springs back to 0 with spring(1f, 300f, 0.5f). */
    public final Spring panelOffset = new Spring(0f, 0f, 1f, 300f, 0.5f);

    public final float initialScale, pressedScale;
    private final float settleThreshold;

    private long lastFrameTime;
    private boolean pressed;
    private boolean waitingSettle;

    /** Value-velocity sampler (time, value) - mirrors GlassMotion VelocityTracker on the value. */
    private static final int SAMPLES = 6;
    private final long[] sampleTimes = new long[SAMPLES];
    private final float[] sampleValues = new float[SAMPLES];
    private int sampleCount;
    private float measuredValueVelocity;

    public MeeroLiquidPhysics(float initialValue, float rangeStart, float rangeEnd) {
        this(initialValue, rangeStart, rangeEnd, 1f, 78f / 56f);
    }

    public MeeroLiquidPhysics(float initialValue, float rangeStart, float rangeEnd, float initialScale, float pressedScale) {
        this.valueRangeStart = rangeStart;
        this.valueRangeEnd = rangeEnd;
        this.value.value = initialValue;
        this.value.target = initialValue;
        this.initialScale = initialScale;
        this.pressedScale = pressedScale;
        this.settleThreshold = (rangeEnd - rangeStart) * 0.025f;
    }

    /** Called with the frame delta (seconds) from the host view's tick. Returns true while any spring animates. */
    public boolean step(float dtSeconds) {
        final long now = SystemClock.elapsedRealtime();
        boolean animating = false;
        animating |= value.step(dtSeconds);
        animating |= velocity.step(dtSeconds);
        animating |= pressProgress.step(dtSeconds);
        animating |= scaleX.step(dtSeconds);
        animating |= scaleY.step(dtSeconds);
        animating |= panelOffset.step(dtSeconds);

        if (waitingSettle && Math.abs(value.value - value.target) < settleThreshold) {
            waitingSettle = false;
            if (!pressed) {
                relaxPress();
            }
        }
        lastFrameTime = now;
        return animating;
    }

    private void relaxPress() {
        pressProgress.target = 0f;
        scaleX.target = initialScale;
        scaleY.target = initialScale;
    }

    /** GlassMotion.press(): pressProgress -> 1, scales -> pressedScale. */
    public void press() {
        pressed = true;
        waitingSettle = false;
        pressProgress.target = 1f;
        scaleX.target = pressedScale;
        scaleY.target = pressedScale;
    }

    /** GlassMotion.release(): wait for the value to settle, then relax. */
    public void release() {
        pressed = false;
        if (Math.abs(value.value - value.target) < settleThreshold) {
            relaxPress();
        } else {
            waitingSettle = true;
        }
    }

    /** GlassMotion.updateValue(): retarget the value spring and sample velocity. */
    public void updateValue(float v) {
        final float clamped = Math.max(valueRangeStart, Math.min(valueRangeEnd, v));
        value.target = clamped;
        sampleValueVelocity();
    }

    /** GlassMotion.animateToValue(): press + animate + release under one gesture. */
    public void animateToValue(float v) {
        final float clamped = Math.max(valueRangeStart, Math.min(valueRangeEnd, v));
        value.target = clamped;
        press();
        if (velocity.value != 0f) {
            velocity.target = 0f;
        }
        release();
    }

    /** Immediate reposition (no animation) - used on first layout. */
    public void snapValue(float v) {
        value.snapTo(v);
        velocity.snapTo(0f);
        pressProgress.snapTo(0f);
        scaleX.snapTo(initialScale);
        scaleY.snapTo(initialScale);
        panelOffset.snapTo(0f);
        pressed = false;
        waitingSettle = false;
    }

    private void sampleValueVelocity() {
        final long now = SystemClock.elapsedRealtime();
        final int idx = sampleCount % SAMPLES;
        sampleTimes[idx] = now;
        sampleValues[idx] = value.value;
        sampleCount++;
        // use the oldest stored sample that is at least 40ms old
        long bestDt = -1;
        float bestValue = value.value;
        for (int i = 0; i < Math.min(sampleCount, SAMPLES); i++) {
            final long dt = now - sampleTimes[i];
            if (dt >= 40 && dt > bestDt) {
                bestDt = dt;
                bestValue = sampleValues[i];
            }
        }
        if (bestDt > 0) {
            measuredValueVelocity = (value.value - bestValue) / (bestDt / 1000f);
        }
        final float range = valueRangeEnd - valueRangeStart;
        velocity.target = range > 0f ? measuredValueVelocity / range : 0f;
    }

    /** Normalized velocity in tabs/second (GlassMotion.velocity). */
    public float getVelocity() {
        return velocity.value;
    }

    public boolean isPressed() {
        return pressed;
    }

    @NonNull
    @Override
    public String toString() {
        return "MeeroLiquidPhysics{value=" + value.value + ", press=" + pressProgress.value
                + ", sx=" + scaleX.value + ", sy=" + scaleY.value + "}";
    }
}
