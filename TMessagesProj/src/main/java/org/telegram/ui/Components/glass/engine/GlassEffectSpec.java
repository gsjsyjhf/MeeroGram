package org.telegram.ui.Components.glass.engine;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassEffectSpec {
    public final float blurRadius;
    public final float brightness;
    public final boolean chromaticAberration;
    public final float contrast;
    public final boolean depthEffect;
    public final float refractionAmount;
    public final float refractionHeight;
    public final float saturation;

    public GlassEffectSpec(float f, float f2, float f3, float f4, float f5, float f6, boolean z, boolean z2) {
        this.brightness = f;
        this.contrast = f2;
        this.saturation = f3;
        this.blurRadius = Math.max(0.0f, f4);
        this.refractionHeight = Math.max(0.0f, f5);
        this.refractionAmount = Math.max(0.0f, f6);
        this.depthEffect = z;
        this.chromaticAberration = z2;
    }

    public static GlassEffectSpec lensOnly(float f, float f2, float f3, boolean z) {
        return new GlassEffectSpec(0.0f, 1.0f, 1.0f, f, f2, f3, false, z);
    }

    public static GlassEffectSpec liquid(float f, float f2, float f3) {
        return new GlassEffectSpec(0.0f, 1.0f, 1.5f, f, f2, f3, false, false);
    }

    public boolean hasBlur() {
        return this.blurRadius > 0.0f;
    }

    public boolean hasColorControls() {
        return (this.brightness == 0.0f && this.contrast == 1.0f && this.saturation == 1.0f) ? false : true;
    }

    public boolean hasLens() {
        return this.refractionHeight > 0.0f && this.refractionAmount > 0.0f;
    }
}
