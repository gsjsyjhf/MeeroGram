package org.telegram.ui.Components.glass.controls;

import android.view.MotionEvent;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
final class GlassDragGestureDetector {
    private int activePointerId = -1;
    private boolean dragging;
    private final Listener listener;
    private float previousX;
    private float previousY;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public interface Listener {
        void onDrag(float f, float f2, float f3, float f4);

        void onDragCancel();

        void onDragEnd(float f, float f2);

        void onDragStart(float f, float f2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public GlassDragGestureDetector(Listener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        this.listener = listener;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void cancel() {
        if (this.dragging) {
            this.dragging = false;
            this.activePointerId = -1;
            this.listener.onDragCancel();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isDragging() {
        return this.dragging;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.activePointerId = motionEvent.getPointerId(0);
            this.previousX = motionEvent.getX(0);
            float y = motionEvent.getY(0);
            this.previousY = y;
            this.dragging = true;
            this.listener.onDragStart(this.previousX, y);
            this.listener.onDrag(this.previousX, this.previousY, 0.0f, 0.0f);
            return true;
        } else if (actionMasked == 1) {
            if (this.dragging) {
                int findPointerIndex = motionEvent.findPointerIndex(this.activePointerId);
                float x = findPointerIndex >= 0 ? motionEvent.getX(findPointerIndex) : this.previousX;
                float y2 = findPointerIndex >= 0 ? motionEvent.getY(findPointerIndex) : this.previousY;
                this.dragging = false;
                this.activePointerId = -1;
                this.listener.onDragEnd(x, y2);
                return true;
            }
            return false;
        } else if (actionMasked == 2) {
            if (this.dragging) {
                int findPointerIndex2 = motionEvent.findPointerIndex(this.activePointerId);
                if (findPointerIndex2 < 0) {
                    cancel();
                    return false;
                }
                float x2 = motionEvent.getX(findPointerIndex2);
                float y3 = motionEvent.getY(findPointerIndex2);
                this.listener.onDrag(x2, y3, x2 - this.previousX, y3 - this.previousY);
                this.previousX = x2;
                this.previousY = y3;
                return true;
            }
            return false;
        } else if (actionMasked == 3) {
            if (this.dragging) {
                cancel();
                return true;
            }
            return false;
        } else if (actionMasked != 6) {
            return this.dragging;
        } else {
            if (this.dragging) {
                int actionIndex = motionEvent.getActionIndex();
                if (motionEvent.getPointerId(actionIndex) == this.activePointerId) {
                    int i = actionIndex == 0 ? 1 : 0;
                    if (i < motionEvent.getPointerCount()) {
                        this.activePointerId = motionEvent.getPointerId(i);
                        this.previousX = motionEvent.getX(i);
                        this.previousY = motionEvent.getY(i);
                    } else {
                        cancel();
                    }
                }
                return true;
            }
            return false;
        }
    }
}
