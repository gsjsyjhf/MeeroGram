package org.telegram.ui.Components.glass.motion;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassSpringSpec {
    private final float dampingRatio;
    private final float stiffness;
    private final float velocityThreshold;
    private final float visibilityThreshold;
    public static final GlassSpringSpec VALUE = new GlassSpringSpec(1.0f, 1000.0f, 0.001f);
    public static final GlassSpringSpec VELOCITY = new GlassSpringSpec(0.5f, 300.0f, 0.01f);
    public static final GlassSpringSpec PRESS = new GlassSpringSpec(1.0f, 1000.0f, 0.001f);
    public static final GlassSpringSpec SCALE_X = new GlassSpringSpec(0.6f, 250.0f, 0.001f);
    public static final GlassSpringSpec SCALE_Y = new GlassSpringSpec(0.7f, 250.0f, 0.001f);
    public static final GlassSpringSpec INTERACTIVE_HIGHLIGHT = new GlassSpringSpec(0.5f, 300.0f, 0.001f);

    public GlassSpringSpec(float f, float f2, float f3) {
        this(f, f2, f3, 62.5f * f3);
    }

    public GlassSpringSpec(float f, float f2, float f3, float f4) {
        requireFinite("dampingRatio", f);
        requireFinite("stiffness", f2);
        requireFinite("visibilityThreshold", f3);
        requireFinite("velocityThreshold", f4);
        if (f < 0.0f) {
            throw new IllegalArgumentException("dampingRatio must be >= 0");
        }
        if (f2 <= 0.0f) {
            throw new IllegalArgumentException("stiffness must be > 0");
        }
        if (f3 <= 0.0f) {
            throw new IllegalArgumentException("visibilityThreshold must be > 0");
        }
        if (f4 <= 0.0f) {
            throw new IllegalArgumentException("velocityThreshold must be > 0");
        }
        this.dampingRatio = f;
        this.stiffness = f2;
        this.visibilityThreshold = f3;
        this.velocityThreshold = f4;
    }

    public static GlassSpringSpec forValue(float f) {
        return new GlassSpringSpec(1.0f, 1000.0f, f);
    }

    public static GlassSpringSpec forVelocity(float f) {
        return new GlassSpringSpec(0.5f, 300.0f, f * 10.0f);
    }

    private static void requireFinite(String str, float f) {
        if (Float.isNaN(f) || Float.isInfinite(f)) {
            throw new IllegalArgumentException(str + " must be finite");
        }
    }

    public float getDampingRatio() {
        return this.dampingRatio;
    }

    public float getStiffness() {
        return this.stiffness;
    }

    public float getVelocityThreshold() {
        return this.velocityThreshold;
    }

    public float getVisibilityThreshold() {
        return this.visibilityThreshold;
    }
}
