package org.telegram.ui.Components.glass.engine;

import android.graphics.Color;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassShadowSpec {
    public final float alpha;
    public final int color;
    public final float offsetX;
    public final float offsetY;
    public final float radius;

    public GlassShadowSpec(float f, float f2, float f3, int i, float f4) {
        this.radius = Math.max(0.0f, f);
        this.offsetX = f2;
        this.offsetY = f3;
        this.color = i;
        this.alpha = Math.max(0.0f, Math.min(1.0f, f4));
    }

    public static GlassShadowSpec defaultShadow(float f) {
        float f2 = f * 24.0f;
        return new GlassShadowSpec(f2, 0.0f, f2 / 6.0f, Color.argb(26, 0, 0, 0), 1.0f);
    }
}
