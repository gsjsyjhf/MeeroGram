package org.telegram.ui.Components.glass.controls;

import android.os.SystemClock;
import android.view.Choreographer;
import org.telegram.ui.Components.glass.motion.GlassFloatSpring;
import org.telegram.ui.Components.glass.motion.GlassSpringSpec;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassDampedDragController implements Choreographer.FrameCallback {
    private final float initialScale;
    private Listener listener;
    private final GlassFloatSpring pressSpring;
    private final float pressedScale;
    private long previousSampleTimeMs;
    private final float rangeEnd;
    private final float rangeLength;
    private final float rangeStart;
    private boolean releaseFrameScheduled;
    private boolean releaseRequested;
    private boolean releasedForRequest;
    private final GlassFloatSpring scaleXSpring;
    private final GlassFloatSpring scaleYSpring;
    private boolean userVelocityUpdateActive;
    private final GlassFloatSpring valueSpring;
    private boolean velocityOnlyDuringUserUpdates;
    private final GlassFloatSpring velocitySpring;
    private final GlassVelocityTracker1D velocityTracker = new GlassVelocityTracker1D();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public interface Listener {
        void onAnimationChanged(GlassDampedDragController glassDampedDragController);
    }

    public static /* synthetic */ void $r8$lambda$9C9CLKVushCc0tMNngQP0PxmxFk(GlassDampedDragController glassDampedDragController, GlassFloatSpring glassFloatSpring, boolean z, float f, float f2) {
        if (z) {
            glassDampedDragController.getClass();
        } else if (glassDampedDragController.releaseRequested) {
            glassDampedDragController.scheduleReleaseFrame();
        }
    }

    public static /* synthetic */ void $r8$lambda$MWn6dwU2LWkWkSHYBr2qd_S6dWI(GlassDampedDragController glassDampedDragController, GlassFloatSpring glassFloatSpring, float f, float f2) {
        if (!glassDampedDragController.velocityOnlyDuringUserUpdates || glassDampedDragController.userVelocityUpdateActive) {
            glassDampedDragController.updateNormalizedVelocity(f);
        }
        glassDampedDragController.dispatchChanged();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public GlassDampedDragController(float f, float f2, float f3, float f4, float f5, float f6) {
        if (f3 <= f2) {
            throw new IllegalArgumentException("value range must be increasing");
        }
        this.rangeStart = f2;
        this.rangeEnd = f3;
        this.rangeLength = f3 - f2;
        this.initialScale = f5;
        this.pressedScale = f6;
        float clamp = clamp(f, f2, f3);
        GlassFloatSpring glassFloatSpring = new GlassFloatSpring(clamp, GlassSpringSpec.forValue(f4));
        this.valueSpring = glassFloatSpring;
        GlassFloatSpring glassFloatSpring2 = new GlassFloatSpring(0.0f, GlassSpringSpec.forVelocity(f4));
        this.velocitySpring = glassFloatSpring2;
        GlassFloatSpring glassFloatSpring3 = new GlassFloatSpring(0.0f, GlassSpringSpec.PRESS);
        this.pressSpring = glassFloatSpring3;
        GlassFloatSpring glassFloatSpring4 = new GlassFloatSpring(f5, GlassSpringSpec.SCALE_X);
        this.scaleXSpring = glassFloatSpring4;
        GlassFloatSpring glassFloatSpring5 = new GlassFloatSpring(f5, GlassSpringSpec.SCALE_Y);
        this.scaleYSpring = glassFloatSpring5;
        GlassFloatSpring.UpdateListener updateListener = new GlassFloatSpring.UpdateListener() { // from class: org.telegram.ui.Components.glass.controls.GlassDampedDragController$$ExternalSyntheticLambda0
            @Override // org.telegram.ui.Components.glass.motion.GlassFloatSpring.UpdateListener
            public final void onSpringUpdate(GlassFloatSpring glassFloatSpring6, float f7, float f8) {
                GlassDampedDragController.this.dispatchChanged();
            }
        };
        glassFloatSpring.setUpdateListener(new GlassFloatSpring.UpdateListener() { // from class: org.telegram.ui.Components.glass.controls.GlassDampedDragController$$ExternalSyntheticLambda1
            @Override // org.telegram.ui.Components.glass.motion.GlassFloatSpring.UpdateListener
            public final void onSpringUpdate(GlassFloatSpring glassFloatSpring6, float f7, float f8) {
                GlassDampedDragController.$r8$lambda$MWn6dwU2LWkWkSHYBr2qd_S6dWI(GlassDampedDragController.this, glassFloatSpring6, f7, f8);
            }
        });
        glassFloatSpring2.setUpdateListener(updateListener);
        glassFloatSpring3.setUpdateListener(updateListener);
        glassFloatSpring4.setUpdateListener(updateListener);
        glassFloatSpring5.setUpdateListener(updateListener);
        glassFloatSpring.setEndListener(new GlassFloatSpring.EndListener() { // from class: org.telegram.ui.Components.glass.controls.GlassDampedDragController$$ExternalSyntheticLambda2
            @Override // org.telegram.ui.Components.glass.motion.GlassFloatSpring.EndListener
            public final void onSpringEnd(GlassFloatSpring glassFloatSpring6, boolean z, float f7, float f8) {
                GlassDampedDragController.$r8$lambda$9C9CLKVushCc0tMNngQP0PxmxFk(GlassDampedDragController.this, glassFloatSpring6, z, f7, f8);
            }
        });
        resetVelocityTracking(clamp);
    }

    private void cancelReleaseRequest() {
        this.releaseRequested = false;
        this.releasedForRequest = false;
        if (this.releaseFrameScheduled) {
            Choreographer.getInstance().removeFrameCallback(this);
            this.releaseFrameScheduled = false;
        }
    }

    private static float clamp(float f, float f2, float f3) {
        return Math.max(f2, Math.min(f3, f));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchChanged() {
        Listener listener = this.listener;
        if (listener != null) {
            listener.onAnimationChanged(this);
        }
    }

    private void releaseNow() {
        this.releaseRequested = false;
        this.releasedForRequest = true;
        this.pressSpring.animateTo(0.0f);
        this.scaleXSpring.animateTo(this.initialScale);
        this.scaleYSpring.animateTo(this.initialScale);
    }

    private void resetVelocityTracking(float f) {
        long uptimeMillis = SystemClock.uptimeMillis();
        this.velocityTracker.resetTracking();
        this.previousSampleTimeMs = uptimeMillis;
        this.velocityTracker.addDataPoint(uptimeMillis, f);
    }

    private void scheduleReleaseFrame() {
        if (this.releaseFrameScheduled) {
            return;
        }
        this.releaseFrameScheduled = true;
        Choreographer.getInstance().postFrameCallback(this);
    }

    private void updateNormalizedVelocity(float f) {
        long uptimeMillis = SystemClock.uptimeMillis();
        if (uptimeMillis > this.previousSampleTimeMs) {
            this.velocityTracker.addDataPoint(uptimeMillis, f);
            this.velocitySpring.animateTo(this.velocityTracker.calculateVelocity() / this.rangeLength);
            this.previousSampleTimeMs = uptimeMillis;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void animateToValue(float f) {
        if (this.velocityOnlyDuringUserUpdates) {
            this.userVelocityUpdateActive = false;
        }
        press();
        this.valueSpring.animateTo(clamp(f, this.rangeStart, this.rangeEnd));
        if (this.velocitySpring.getValue() != 0.0f) {
            this.velocitySpring.animateTo(0.0f);
        }
        release();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void dispose() {
        cancelReleaseRequest();
        this.userVelocityUpdateActive = false;
        this.valueSpring.cancel();
        this.velocitySpring.cancel();
        this.pressSpring.cancel();
        this.scaleXSpring.cancel();
        this.scaleYSpring.cancel();
        this.listener = null;
    }

    @Override // android.view.Choreographer.FrameCallback
    public void doFrame(long j) {
        this.releaseFrameScheduled = false;
        if (!this.releaseRequested || this.releasedForRequest) {
            return;
        }
        float f = this.rangeLength * 0.025f;
        if (!this.valueSpring.isRunning() || Math.abs(this.valueSpring.getValue() - this.valueSpring.getTargetValue()) < f) {
            releaseNow();
        } else {
            scheduleReleaseFrame();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getPressProgress() {
        return clamp(this.pressSpring.getValue(), 0.0f, 1.0f);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getProgress() {
        return clamp((getValue() - this.rangeStart) / this.rangeLength, 0.0f, 1.0f);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getScaleX() {
        return this.scaleXSpring.getValue();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getScaleY() {
        return this.scaleYSpring.getValue();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getTargetValue() {
        return this.valueSpring.getTargetValue();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getValue() {
        return this.valueSpring.getValue();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getVelocity() {
        return this.velocitySpring.getValue();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void press() {
        cancelReleaseRequest();
        if (this.velocityOnlyDuringUserUpdates) {
            this.userVelocityUpdateActive = false;
        }
        resetVelocityTracking(this.valueSpring.getValue());
        this.pressSpring.animateTo(1.0f);
        this.scaleXSpring.animateTo(this.pressedScale);
        this.scaleYSpring.animateTo(this.pressedScale);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void release() {
        if (this.velocityOnlyDuringUserUpdates) {
            this.userVelocityUpdateActive = false;
        }
        this.releaseRequested = true;
        this.releasedForRequest = false;
        scheduleReleaseFrame();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public GlassDampedDragController setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void snapTo(float f) {
        cancelReleaseRequest();
        if (this.velocityOnlyDuringUserUpdates) {
            this.userVelocityUpdateActive = false;
        }
        float clamp = clamp(f, this.rangeStart, this.rangeEnd);
        this.valueSpring.snapTo(clamp);
        resetVelocityTracking(clamp);
        this.velocitySpring.snapTo(0.0f);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public GlassDampedDragController trackVelocityOnlyDuringUserUpdates() {
        this.velocityOnlyDuringUserUpdates = true;
        this.userVelocityUpdateActive = false;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateValue(float f) {
        if (this.velocityOnlyDuringUserUpdates) {
            this.userVelocityUpdateActive = true;
        }
        this.valueSpring.animateTo(clamp(f, this.rangeStart, this.rangeEnd));
    }
}
