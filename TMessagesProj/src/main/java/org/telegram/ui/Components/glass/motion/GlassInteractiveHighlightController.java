package org.telegram.ui.Components.glass.motion;

import org.telegram.ui.Components.glass.motion.GlassFloatSpring;
import org.telegram.ui.Components.glass.motion.GlassPointSpring;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassInteractiveHighlightController {
    private Listener listener;
    private final GlassPointSpring positionSpring;
    private final GlassFloatSpring pressProgressSpring;
    private boolean pressed;
    private float startX;
    private float startY;

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public interface Listener {
        void onHighlightChanged(GlassInteractiveHighlightController glassInteractiveHighlightController);
    }

    public GlassInteractiveHighlightController() {
        GlassFloatSpring glassFloatSpring = new GlassFloatSpring(0.0f, GlassSpringSpec.INTERACTIVE_HIGHLIGHT);
        this.pressProgressSpring = glassFloatSpring;
        GlassPointSpring glassPointSpring = new GlassPointSpring(0.0f, 0.0f, new GlassSpringSpec(0.5f, 300.0f, 0.5f));
        this.positionSpring = glassPointSpring;
        glassFloatSpring.setUpdateListener(new GlassFloatSpring.UpdateListener() { // from class: org.telegram.ui.Components.glass.motion.GlassInteractiveHighlightController$$ExternalSyntheticLambda0
            @Override // org.telegram.ui.Components.glass.motion.GlassFloatSpring.UpdateListener
            public final void onSpringUpdate(GlassFloatSpring glassFloatSpring2, float f, float f2) {
                GlassInteractiveHighlightController.this.dispatchChanged();
            }
        });
        glassPointSpring.setUpdateListener(new GlassPointSpring.UpdateListener() { // from class: org.telegram.ui.Components.glass.motion.GlassInteractiveHighlightController$$ExternalSyntheticLambda1
            @Override // org.telegram.ui.Components.glass.motion.GlassPointSpring.UpdateListener
            public final void onSpringUpdate(GlassPointSpring glassPointSpring2, float f, float f2, float f3, float f4) {
                GlassInteractiveHighlightController.this.dispatchChanged();
            }
        });
    }

    private static float clamp01(float f) {
        return Math.max(0.0f, Math.min(1.0f, f));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchChanged() {
        Listener listener = this.listener;
        if (listener != null) {
            listener.onHighlightChanged(this);
        }
    }

    private static void requireFinite(String str, float f) {
        if (Float.isNaN(f) || Float.isInfinite(f)) {
            throw new IllegalArgumentException(str + " must be finite");
        }
    }

    public void cancel() {
        release();
    }

    public void dispose() {
        this.pressed = false;
        this.pressProgressSpring.cancel();
        this.positionSpring.cancel();
        this.listener = null;
    }

    public float getGlowRadius(float f, float f2) {
        return Math.min(f, f2) * 1.5f;
    }

    public float getOffsetX() {
        return this.positionSpring.getX() - this.startX;
    }

    public float getOffsetY() {
        return this.positionSpring.getY() - this.startY;
    }

    public float getPositionX() {
        return this.positionSpring.getX();
    }

    public float getPositionY() {
        return this.positionSpring.getY();
    }

    public float getPressProgress() {
        return clamp01(this.pressProgressSpring.getValue());
    }

    public void move(float f, float f2) {
        if (this.pressed) {
            requireFinite("x", f);
            requireFinite("y", f2);
            this.positionSpring.snapTo(f, f2);
        }
    }

    public void press(float f, float f2) {
        requireFinite("x", f);
        requireFinite("y", f2);
        this.pressed = true;
        this.startX = f;
        this.startY = f2;
        this.positionSpring.snapTo(f, f2);
        this.pressProgressSpring.animateTo(1.0f);
    }

    public void release() {
        if (this.pressed || this.pressProgressSpring.getTargetValue() != 0.0f) {
            this.pressed = false;
            this.pressProgressSpring.animateTo(0.0f);
            this.positionSpring.animateTo(this.startX, this.startY);
        }
    }

    public GlassInteractiveHighlightController setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public void setRenderPosition(float f, float f2) {
        requireFinite("x", f);
        requireFinite("y", f2);
        if (this.positionSpring.getX() == f && this.positionSpring.getY() == f2) {
            return;
        }
        this.positionSpring.snapTo(f, f2);
    }
}
