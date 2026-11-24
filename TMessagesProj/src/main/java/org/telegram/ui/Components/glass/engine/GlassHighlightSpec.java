package org.telegram.ui.Components.glass.engine;

import android.graphics.Color;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassHighlightSpec {
    public final boolean additive;
    public final float alpha;
    public final float angleDegrees;
    public final float blurRadius;
    public final int color;
    public final float falloff;
    public final Style style;
    public final float width;

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public enum Style {
        DEFAULT,
        AMBIENT,
        PLAIN
    }

    public GlassHighlightSpec(float f, float f2, float f3, int i, float f4, float f5, Style style, boolean z) {
        this.width = Math.max(0.0f, f);
        this.blurRadius = Math.max(0.0f, f2);
        this.alpha = clamp01(f3);
        this.color = i;
        this.angleDegrees = f4;
        this.falloff = Math.max(0.0f, f5);
        this.style = style == null ? Style.DEFAULT : style;
        this.additive = z;
    }

    public static GlassHighlightSpec ambient(float f, float f2) {
        return new GlassHighlightSpec(0.33333334f * f, 0.16666667f * f, f2, Color.argb(Math.round(96.9f), 255, 255, 255), 45.0f, 1.0f, Style.AMBIENT, false);
    }

    private static float clamp01(float f) {
        return Math.max(0.0f, Math.min(1.0f, f));
    }

    public static GlassHighlightSpec defaultHighlight(float f) {
        return new GlassHighlightSpec(0.5f * f, 0.25f * f, 1.0f, Color.argb(128, 255, 255, 255), 45.0f, 1.0f, Style.DEFAULT, true);
    }
}
