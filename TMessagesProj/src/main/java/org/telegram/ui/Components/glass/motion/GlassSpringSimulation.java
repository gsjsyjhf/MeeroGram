package org.telegram.ui.Components.glass.motion;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassSpringSimulation {
    private final float initialValue;
    private final float initialVelocity;
    private final GlassSpringSpec spec;
    private final float targetValue;

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public static final class State {
        public float value;
        public float velocity;

        public State set(float f, float f2) {
            this.value = f;
            this.velocity = f2;
            return this;
        }
    }

    public GlassSpringSimulation(GlassSpringSpec glassSpringSpec, float f, float f2, float f3) {
        if (glassSpringSpec == null) {
            throw new NullPointerException("spec");
        }
        requireFinite("initialValue", f);
        requireFinite("initialVelocity", f2);
        requireFinite("targetValue", f3);
        this.spec = glassSpringSpec;
        this.initialValue = f;
        this.initialVelocity = f2;
        this.targetValue = f3;
    }

    private static void requireFinite(String str, float f) {
        if (Float.isNaN(f) || Float.isInfinite(f)) {
            throw new IllegalArgumentException(str + " must be finite");
        }
    }

    public State evaluate(double d, State state) {
        double d2;
        double d3;
        State state2 = state != null ? state : new State();
        if (d <= 0.0d) {
            return state2.set(this.initialValue, this.initialVelocity);
        }
        double d4 = this.initialValue - this.targetValue;
        double d5 = this.initialVelocity;
        double sqrt = Math.sqrt(this.spec.getStiffness());
        double dampingRatio = this.spec.getDampingRatio();
        if (dampingRatio < 0.9999d) {
            double sqrt2 = Math.sqrt(1.0d - (dampingRatio * dampingRatio)) * sqrt;
            double d6 = (d5 + ((dampingRatio * sqrt) * d4)) / sqrt2;
            double d7 = sqrt2 * d;
            double cos = Math.cos(d7);
            double sin = Math.sin(d7);
            double d8 = (-dampingRatio) * sqrt;
            double exp = Math.exp(d8 * d);
            double d9 = (d4 * cos) + (d6 * sin);
            d3 = this.targetValue + (exp * d9);
            d2 = exp * (((d8 * d9) - ((d4 * sqrt2) * sin)) + (d6 * sqrt2 * cos));
        } else if (dampingRatio > 1.0001d) {
            double sqrt3 = Math.sqrt((dampingRatio * dampingRatio) - 1.0d);
            double d10 = -sqrt;
            double d11 = (dampingRatio - sqrt3) * d10;
            double d12 = d10 * (dampingRatio + sqrt3);
            double d13 = (d5 - (d12 * d4)) / (d11 - d12);
            double d14 = d4 - d13;
            double exp2 = Math.exp(d11 * d);
            double exp3 = Math.exp(d12 * d);
            d2 = (d13 * d11 * exp2) + (d14 * d12 * exp3);
            d3 = this.targetValue + (d13 * exp2) + (d14 * exp3);
        } else {
            double d15 = d5 + (sqrt * d4);
            double exp4 = Math.exp((-sqrt) * d);
            double d16 = d4 + (d15 * d);
            d2 = exp4 * (d15 - (sqrt * d16));
            d3 = this.targetValue + (exp4 * d16);
        }
        return state2.set((float) d3, (float) d2);
    }

    public boolean isAtEquilibrium(float f, float f2) {
        return Math.abs(f - this.targetValue) <= this.spec.getVisibilityThreshold() && Math.abs(f2) <= this.spec.getVelocityThreshold();
    }
}
