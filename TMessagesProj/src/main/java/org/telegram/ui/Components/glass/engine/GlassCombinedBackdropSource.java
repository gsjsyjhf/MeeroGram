package org.telegram.ui.Components.glass.engine;

import android.graphics.Canvas;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassCombinedBackdropSource implements GlassTransformAwareBackdropSource {
    private final GlassBackdropSource[] sources;

    public GlassCombinedBackdropSource(GlassBackdropSource glassBackdropSource, GlassBackdropSource glassBackdropSource2, GlassBackdropSource... glassBackdropSourceArr) {
        int length = glassBackdropSourceArr.length + 2;
        GlassBackdropSource[] glassBackdropSourceArr2 = new GlassBackdropSource[length];
        this.sources = glassBackdropSourceArr2;
        glassBackdropSourceArr2[0] = glassBackdropSource;
        glassBackdropSourceArr2[1] = glassBackdropSource2;
        System.arraycopy(glassBackdropSourceArr, 0, glassBackdropSourceArr2, 2, glassBackdropSourceArr.length);
        for (int i = 0; i < length; i++) {
            if (glassBackdropSourceArr2[i] == null) {
                throw new NullPointerException("Combined backdrop sources cannot be null");
            }
        }
    }

    public static GlassCombinedBackdropSource of(GlassBackdropSource glassBackdropSource, GlassBackdropSource glassBackdropSource2) {
        return new GlassCombinedBackdropSource(glassBackdropSource, glassBackdropSource2, new GlassBackdropSource[0]);
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassBackdropSource
    public /* synthetic */ void draw(Canvas canvas, float f, float f2, float f3, float f4) {
        draw(canvas, f, f2, f3, f4, GlassLayerTransform.IDENTITY);
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassTransformAwareBackdropSource
    public void draw(Canvas canvas, float f, float f2, float f3, float f4, GlassLayerTransform glassLayerTransform) {
        for (GlassBackdropSource glassBackdropSource : this.sources) {
            GlassBackdropSources.draw(glassBackdropSource, canvas, f, f2, f3, f4, glassLayerTransform);
        }
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassBackdropSource
    public void onSurfacePositionChanged() {
        for (GlassBackdropSource glassBackdropSource : this.sources) {
            glassBackdropSource.onSurfacePositionChanged();
        }
    }
}
