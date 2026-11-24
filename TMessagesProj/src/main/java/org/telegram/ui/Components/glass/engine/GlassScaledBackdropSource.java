package org.telegram.ui.Components.glass.engine;

import android.graphics.Canvas;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassScaledBackdropSource implements GlassTransformAwareBackdropSource {
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private final GlassBackdropSource source;

    public GlassScaledBackdropSource(GlassBackdropSource glassBackdropSource) {
        this.source = glassBackdropSource;
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassBackdropSource
    public /* synthetic */ void draw(Canvas canvas, float f, float f2, float f3, float f4) {
        draw(canvas, f, f2, f3, f4, GlassLayerTransform.IDENTITY);
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassTransformAwareBackdropSource
    public void draw(Canvas canvas, float f, float f2, float f3, float f4, GlassLayerTransform glassLayerTransform) {
        int save = canvas.save();
        canvas.scale(this.scaleX, this.scaleY, ((f3 - f) * 0.5f) + f, ((f4 - f2) * 0.5f) + f2);
        GlassBackdropSources.draw(this.source, canvas, f, f2, f3, f4, glassLayerTransform);
        canvas.restoreToCount(save);
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassBackdropSource
    public void onSurfacePositionChanged() {
        this.source.onSurfacePositionChanged();
    }

    public void setScale(float f, float f2) {
        this.scaleX = f;
        this.scaleY = f2;
    }
}
