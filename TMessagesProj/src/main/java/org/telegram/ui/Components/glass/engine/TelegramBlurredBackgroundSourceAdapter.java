package org.telegram.ui.Components.glass.engine;

import android.graphics.Canvas;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSource;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class TelegramBlurredBackgroundSourceAdapter implements GlassBackdropSource {
    private final BlurredBackgroundSource source;

    public TelegramBlurredBackgroundSourceAdapter(BlurredBackgroundSource blurredBackgroundSource) {
        this.source = blurredBackgroundSource;
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassBackdropSource
    public void draw(Canvas canvas, float f, float f2, float f3, float f4) {
        this.source.draw(canvas, f, f2, f3, f4);
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassBackdropSource
    public void onSurfacePositionChanged() {
        this.source.dispatchOnDrawablesRelativePositionChange();
    }
}
