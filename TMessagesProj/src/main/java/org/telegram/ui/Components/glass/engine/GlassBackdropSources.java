package org.telegram.ui.Components.glass.engine;

import android.graphics.Canvas;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public abstract class GlassBackdropSources {
    public static void draw(GlassBackdropSource glassBackdropSource, Canvas canvas, float f, float f2, float f3, float f4, GlassLayerTransform glassLayerTransform) {
        if (glassBackdropSource instanceof GlassTransformAwareBackdropSource) {
            ((GlassTransformAwareBackdropSource) glassBackdropSource).draw(canvas, f, f2, f3, f4, glassLayerTransform);
        } else {
            glassBackdropSource.draw(canvas, f, f2, f3, f4);
        }
    }
}
