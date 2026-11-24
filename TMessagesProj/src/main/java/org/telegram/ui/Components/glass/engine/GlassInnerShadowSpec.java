package org.telegram.ui.Components.glass.engine;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassInnerShadowSpec {
    public final float alpha;
    public final int color;
    public final float offsetX;
    public final float offsetY;
    public final float radius;

    public GlassInnerShadowSpec(float f, float f2, float f3, int i, float f4) {
        this.radius = Math.max(0.0f, f);
        this.offsetX = f2;
        this.offsetY = f3;
        this.color = i;
        this.alpha = Math.max(0.0f, Math.min(1.0f, f4));
    }
}
