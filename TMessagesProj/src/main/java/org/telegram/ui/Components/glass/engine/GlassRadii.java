package org.telegram.ui.Components.glass.engine;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassRadii {
    public float bottomLeft;
    public float bottomRight;
    public float topLeft;
    public float topRight;

    public GlassRadii() {
    }

    public GlassRadii(float f, float f2, float f3, float f4) {
        set(f, f2, f3, f4);
    }

    public GlassRadii clamp(float f, float f2) {
        float max = Math.max(0.0f, Math.min(f, f2) * 0.5f);
        this.topLeft = Math.min(this.topLeft, max);
        this.topRight = Math.min(this.topRight, max);
        this.bottomRight = Math.min(this.bottomRight, max);
        this.bottomLeft = Math.min(this.bottomLeft, max);
        return this;
    }

    public GlassRadii set(float f) {
        return set(f, f, f, f);
    }

    public GlassRadii set(float f, float f2, float f3, float f4) {
        this.topLeft = Math.max(0.0f, f);
        this.topRight = Math.max(0.0f, f2);
        this.bottomRight = Math.max(0.0f, f3);
        this.bottomLeft = Math.max(0.0f, f4);
        return this;
    }
}
