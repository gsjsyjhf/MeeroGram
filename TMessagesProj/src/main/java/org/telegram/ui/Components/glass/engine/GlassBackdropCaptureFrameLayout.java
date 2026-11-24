package org.telegram.ui.Components.glass.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import org.telegram.ui.Components.glass.engine.GlassLayerBackdrop;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public class GlassBackdropCaptureFrameLayout extends FrameLayout {
    private final GlassLayerBackdrop exportedBackdrop;

    private void drawSuperInternal(Canvas canvas) {
        super.draw(canvas);
    }

    public GlassBackdropCaptureFrameLayout(Context context) {
        this(context, null);
    }

    public GlassBackdropCaptureFrameLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GlassBackdropCaptureFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.exportedBackdrop = new GlassLayerBackdrop();
        setWillNotDraw(false);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
        recordExportedBackdrop();
    }

    public GlassLayerBackdrop getExportedBackdrop() {
        return this.exportedBackdrop;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        this.exportedBackdrop.clear();
        super.onDetachedFromWindow();
    }

    public void recordExportedBackdrop() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        this.exportedBackdrop.record(this, getWidth(), getHeight(), new GlassLayerBackdrop.Recorder() { // from class: org.telegram.ui.Components.glass.engine.GlassBackdropCaptureFrameLayout$$ExternalSyntheticLambda0
            @Override // org.telegram.ui.Components.glass.engine.GlassLayerBackdrop.Recorder
            public final void draw(Canvas canvas) {
                drawSuperInternal(canvas);
            }
        });
    }

    public void setBackdropCoordinateRoot(View view) {
        this.exportedBackdrop.setCoordinateRoot(view);
    }
}
