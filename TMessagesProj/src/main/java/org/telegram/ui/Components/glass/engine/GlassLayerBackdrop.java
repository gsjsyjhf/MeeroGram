package org.telegram.ui.Components.glass.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RecordingCanvas;
import android.graphics.RenderNode;
import android.os.Build;
import android.view.View;
import java.lang.ref.WeakReference;

import org.telegram.messenger.camera.CameraView$;

/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassLayerBackdrop implements GlassTransformAwareBackdropSource {
    private Object api29Layer;
    private WeakReference coordinateRoot;
    private boolean explicitOrigin;
    private boolean hasFrame;
    private int height;
    private float originX;
    private float originY;
    private WeakReference ownerView;
    private Bitmap softwareLayer;
    private int width;
    private final Matrix inverseMatrix = new Matrix();
    private final Matrix coordinateMatrix = new Matrix();
    private final float[] coordinatePoint = new float[2];
    private final int[] ownerLocation = new int[2];
    private final int[] rootLocation = new int[2];

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public static final class Api29Layer {

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
        public static final class State {
            final RenderNode node;

            private State() {
                this.node = new RenderNode("MRTelegramGlassLayerBackdrop");
            }
        }

        static void clear(Object obj) {
            ((State) obj).node.discardDisplayList();
        }

        static Object create() {
            return new State();
        }

        static void draw(Object obj, Canvas canvas) {
            State state = (State) obj;
            if (state.node).hasDisplayList() {
                canvas.drawRenderNode(state.node);
            }
        }

        static void record(Object obj, int i, int i2, Recorder recorder) {
            RecordingCanvas beginRecording;
            State state = (State) obj;
            state.node.setPosition(0, 0, i, i2);
            state.node.setClipToBounds(true);
            beginRecording = state.node.beginRecording(i, i2);
            recorder.draw(beginRecording);
            state.node.endRecording();
        }
    }

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public interface Recorder {
        void draw(Canvas canvas);
    }

    private void recordSoftware(int i, int i2, Recorder recorder) {
        Bitmap bitmap = this.softwareLayer;
        if (bitmap == null || bitmap.isRecycled() || this.softwareLayer.getWidth() != i || this.softwareLayer.getHeight() != i2) {
            recycleSoftwareLayer();
            this.softwareLayer = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(this.softwareLayer);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        recorder.draw(canvas);
    }

    private void recycleSoftwareLayer() {
        Bitmap bitmap = this.softwareLayer;
        if (bitmap != null) {
            bitmap.recycle();
            this.softwareLayer = null;
        }
    }

    private void updateOrigin() {
        if (this.explicitOrigin) {
            return;
        }
        WeakReference weakReference = this.ownerView;
        View view = weakReference == null ? null : (View) weakReference.get();
        if (view == null) {
            return;
        }
        view.getLocationInWindow(this.ownerLocation);
        WeakReference weakReference2 = this.coordinateRoot;
        View view2 = weakReference2 != null ? (View) weakReference2.get() : null;
        if (view2 == null) {
            int[] iArr = this.ownerLocation;
            this.originX = iArr[0];
            this.originY = iArr[1];
        } else if (GlassViewCoordinates.positionInAncestor(view, view2, this.coordinateMatrix, this.coordinatePoint)) {
            float[] fArr = this.coordinatePoint;
            this.originX = fArr[0];
            this.originY = fArr[1];
        } else {
            view2.getLocationInWindow(this.rootLocation);
            int[] iArr2 = this.ownerLocation;
            int i = iArr2[0];
            int[] iArr3 = this.rootLocation;
            this.originX = i - iArr3[0];
            this.originY = iArr2[1] - iArr3[1];
        }
    }

    public void clear() {
        Object obj;
        this.hasFrame = false;
        this.width = 0;
        this.height = 0;
        if (Build.VERSION.SDK_INT >= 29 && (obj = this.api29Layer) != null) {
            Api29Layer.clear(obj);
        }
        recycleSoftwareLayer();
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassBackdropSource
    public /* synthetic */ void draw(Canvas canvas, float f, float f2, float f3, float f4) {
        draw(canvas, f, f2, f3, f4, GlassLayerTransform.IDENTITY);
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassTransformAwareBackdropSource
    public void draw(Canvas canvas, float f, float f2, float f3, float f4, GlassLayerTransform glassLayerTransform) {
        if (this.hasFrame) {
            updateOrigin();
            int save = canvas.save();
            canvas.translate(f, f2);
            glassLayerTransform.concatInverse(canvas, this.inverseMatrix);
            canvas.translate(this.originX - f, this.originY - f2);
            if (Build.VERSION.SDK_INT < 29 || this.api29Layer == null || !canvas.isHardwareAccelerated()) {
                Bitmap bitmap = this.softwareLayer;
                if (bitmap != null && !bitmap.isRecycled()) {
                    canvas.drawBitmap(this.softwareLayer, 0.0f, 0.0f, (Paint) null);
                }
            } else {
                Api29Layer.draw(this.api29Layer, canvas);
            }
            canvas.restoreToCount(save);
        }
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassBackdropSource
    public void onSurfacePositionChanged() {
        updateOrigin();
    }

    public void record(View view, int i, int i2, Recorder recorder) {
        if (i <= 0 || i2 <= 0) {
            clear();
            return;
        }
        this.ownerView = new WeakReference(view);
        this.width = i;
        this.height = i2;
        updateOrigin();
        if (Build.VERSION.SDK_INT >= 29) {
            if (this.api29Layer == null) {
                this.api29Layer = Api29Layer.create();
            }
            Api29Layer.record(this.api29Layer, i, i2, recorder);
            recycleSoftwareLayer();
        } else {
            recordSoftware(i, i2, recorder);
        }
        this.hasFrame = true;
    }

    public void setCoordinateRoot(View view) {
        WeakReference weakReference = this.coordinateRoot;
        if ((weakReference == null ? null : (View) weakReference.get()) == view) {
            return;
        }
        this.coordinateRoot = view != null ? new WeakReference(view) : null;
        if (view != null) {
            this.explicitOrigin = false;
        }
        updateOrigin();
    }

    public void setOriginInSource(float f, float f2) {
        this.explicitOrigin = true;
        this.originX = f;
        this.originY = f2;
    }
}
