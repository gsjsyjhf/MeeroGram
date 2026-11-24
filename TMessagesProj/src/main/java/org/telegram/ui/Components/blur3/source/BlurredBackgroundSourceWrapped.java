package org.telegram.ui.Components.blur3.source;

import android.graphics.Canvas;
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSource;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public class BlurredBackgroundSourceWrapped implements BlurredBackgroundSource {
    private BlurredBackgroundSource sourceInternal;

    @Override // org.telegram.ui.Components.blur3.source.BlurredBackgroundSource
    public BlurredBackgroundDrawable createDrawable() {
        throw new UnsupportedOperationException("not wired in this build");
    }

    @Override // org.telegram.ui.Components.blur3.source.BlurredBackgroundSource
    public /* synthetic */ void dispatchOnDrawablesRelativePositionChange() {
        BlurredBackgroundSource.CC.$default$dispatchOnDrawablesRelativePositionChange(this);
    }

    @Override // org.telegram.ui.Components.blur3.source.BlurredBackgroundSource
    public void draw(Canvas canvas, float f, float f2, float f3, float f4) {
        BlurredBackgroundSource blurredBackgroundSource = this.sourceInternal;
        if (blurredBackgroundSource != null) {
            blurredBackgroundSource.draw(canvas, f, f2, f3, f4);
        }
    }

    public BlurredBackgroundSource getSource() {
        return this.sourceInternal;
    }

    public void setSource(BlurredBackgroundSource blurredBackgroundSource) {
        this.sourceInternal = blurredBackgroundSource;
    }
}
