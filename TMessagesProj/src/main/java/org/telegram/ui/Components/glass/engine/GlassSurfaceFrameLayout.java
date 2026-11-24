package org.telegram.ui.Components.glass.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RecordingCanvas;
import android.graphics.RenderNode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import java.lang.ref.WeakReference;

import org.telegram.ui.Components.blur3.source.BlurredBackgroundSource;
import org.telegram.ui.Components.glass.engine.GlassLayerBackdrop;
import org.telegram.ui.Components.glass.shape.GlassContinuousPathFactory;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public abstract class GlassSurfaceFrameLayout extends FrameLayout {
    private static final GlassEffectSpec NO_EFFECTS = new GlassEffectSpec(0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, false);
    private Object api31Backdrop;
    private GlassBackdropSource backdropSource;
    private float cachedLayerRotation;
    private float cachedLayerScaleX;
    private float cachedLayerScaleY;
    private boolean capsule;
    private final Matrix coordinateMatrix;
    private final float[] coordinatePoint;
    private GlassEffectSpec effectSpec;
    private final GlassRadii effectiveRadii;
    private GlassLayerTransform explicitBackdropLayerTransform;
    private GlassLayerBackdrop exportedBackdrop;
    private final Path glassPath;
    private final GlassHighlightRenderer highlightRenderer;
    private GlassHighlightSpec highlightSpec;
    private GlassInnerShadowSpec innerShadowSpec;
    private final int[] locationBuffer;
    private GlassShadowSpec outerShadowSpec;
    private boolean pathDirty;
    private final GlassRadii radii;
    private GlassLayerTransform resolvedBackdropLayerTransform;
    private float resolvedSourceOffsetX;
    private float resolvedSourceOffsetY;
    private final GlassShadowRenderer shadowRenderer;
    private WeakReference sourceCoordinateView;
    private final int[] sourceLocationBuffer;
    private float sourceOffsetX;
    private float sourceOffsetY;
    private final Paint tintPaint;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public static final class Api31Backdrop {

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
        public static final class State {
            final RenderNode node;
            final GlassRenderEffectPipeline pipeline;

            private State() {
                RenderNode m = new RenderNode("MRTelegramGlassBackdrop");
                this.node = m;
                this.pipeline = new GlassRenderEffectPipeline(m);
            }
        }

        static Object create() {
            State state = new State();
            state.node.setClipToBounds(true);
            return state;
        }

        static void draw(Object obj, Canvas canvas, GlassBackdropSource glassBackdropSource, float f, float f2, int i, int i2, GlassRadii glassRadii, GlassEffectSpec glassEffectSpec, GlassLayerTransform glassLayerTransform) {
            RecordingCanvas beginRecording;
            State state = (State) obj;
            float update = state.pipeline.update(i, i2, glassRadii, glassEffectSpec);
            int max = Math.max(0, (int) update);
            int i3 = max * 2;
            int i4 = -max;
            state.node.setPosition(i4, i4, i + max, max + i2);
            beginRecording = state.node.beginRecording(i + i3, i3 + i2);
            float f3 = f - update;
            float f4 = f2 - update;
            int save = beginRecording.save();
            beginRecording.translate(-f3, -f4);
            GlassBackdropSources.draw(glassBackdropSource, beginRecording, f3, f4, f + i + update, f2 + i2 + update, glassLayerTransform);
            beginRecording.restoreToCount(save);
            state.node.endRecording();
            canvas.drawRenderNode(state.node);
        }
    }

    public static /* synthetic */ void $r8$lambda$LahXSuA7fFzNRANpIgiC6MfpBzI(GlassSurfaceFrameLayout glassSurfaceFrameLayout, int i, int i2, Canvas canvas) {
        glassSurfaceFrameLayout.getClass();
        int save = canvas.save();
        canvas.clipPath(glassSurfaceFrameLayout.glassPath);
        glassSurfaceFrameLayout.drawBackdrop(canvas, i, i2);
        if (Color.alpha(glassSurfaceFrameLayout.tintPaint.getColor()) != 0) {
            canvas.drawPath(glassSurfaceFrameLayout.glassPath, glassSurfaceFrameLayout.tintPaint);
        }
        glassSurfaceFrameLayout.onDrawGlassContent(canvas);
        glassSurfaceFrameLayout.onDrawGlassForeground(canvas);
        canvas.restoreToCount(save);
    }

    public GlassSurfaceFrameLayout(Context context) {
        this(context, null);
    }

    public GlassSurfaceFrameLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GlassSurfaceFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.glassPath = new Path();
        this.radii = new GlassRadii();
        this.effectiveRadii = new GlassRadii();
        this.tintPaint = new Paint(1);
        this.highlightRenderer = new GlassHighlightRenderer();
        this.shadowRenderer = new GlassShadowRenderer();
        this.locationBuffer = new int[2];
        this.sourceLocationBuffer = new int[2];
        this.coordinateMatrix = new Matrix();
        this.coordinatePoint = new float[2];
        this.effectSpec = NO_EFFECTS;
        this.resolvedBackdropLayerTransform = GlassLayerTransform.IDENTITY;
        this.cachedLayerScaleX = 1.0f;
        this.cachedLayerScaleY = 1.0f;
        this.capsule = true;
        this.pathDirty = true;
        initialize();
    }

    private void drawBackdrop(Canvas canvas, int i, int i2) {
        GlassBackdropSource glassBackdropSource = this.backdropSource;
        if (glassBackdropSource == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < 31 || !canvas.isHardwareAccelerated()) {
            drawSourceDirect(canvas, glassBackdropSource, this.resolvedSourceOffsetX, this.resolvedSourceOffsetY, i, i2, resolveBackdropLayerTransform());
            return;
        }
        if (this.api31Backdrop == null) {
            this.api31Backdrop = Api31Backdrop.create();
        }
        Api31Backdrop.draw(this.api31Backdrop, canvas, glassBackdropSource, this.resolvedSourceOffsetX, this.resolvedSourceOffsetY, i, i2, this.effectiveRadii, this.effectSpec, resolveBackdropLayerTransform());
    }

    private static void drawSourceDirect(Canvas canvas, GlassBackdropSource glassBackdropSource, float f, float f2, float f3, float f4, GlassLayerTransform glassLayerTransform) {
        int save = canvas.save();
        canvas.translate(-f, -f2);
        GlassBackdropSources.draw(glassBackdropSource, canvas, f, f2, f3 + f, f4 + f2, glassLayerTransform);
        canvas.restoreToCount(save);
    }

    private void ensurePath(int i, int i2) {
        if (this.pathDirty) {
            this.pathDirty = false;
            buildGlassPath(this.glassPath, i, i2);
        }
    }

    private void initialize() {
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        this.tintPaint.setStyle(Paint.Style.FILL);
        this.tintPaint.setColor(0);
    }

    private void recordExportedBackdrop(final int i, final int i2) {
        GlassLayerBackdrop glassLayerBackdrop = this.exportedBackdrop;
        if (glassLayerBackdrop == null) {
            return;
        }
        glassLayerBackdrop.record(this, i, i2, new GlassLayerBackdrop.Recorder() { // from class: org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout$$ExternalSyntheticLambda0
            @Override // org.telegram.ui.Components.glass.engine.GlassLayerBackdrop.Recorder
            public final void draw(Canvas canvas) {
                GlassSurfaceFrameLayout.$r8$lambda$LahXSuA7fFzNRANpIgiC6MfpBzI(GlassSurfaceFrameLayout.this, i, i2, canvas);
            }
        });
    }

    private GlassLayerTransform resolveBackdropLayerTransform() {
        GlassLayerTransform glassLayerTransform = this.explicitBackdropLayerTransform;
        if (glassLayerTransform != null) {
            return glassLayerTransform;
        }
        float scaleX = getScaleX();
        float scaleY = getScaleY();
        float rotation = getRotation();
        if (scaleX == 1.0f && scaleY == 1.0f && rotation == 0.0f) {
            return GlassLayerTransform.IDENTITY;
        }
        if (scaleX != this.cachedLayerScaleX || scaleY != this.cachedLayerScaleY || rotation != this.cachedLayerRotation) {
            this.cachedLayerScaleX = scaleX;
            this.cachedLayerScaleY = scaleY;
            this.cachedLayerRotation = rotation;
            this.resolvedBackdropLayerTransform = new GlassLayerTransform(scaleX, scaleY, rotation);
        }
        return this.resolvedBackdropLayerTransform;
    }

    private void resolveSourceOffset() {
        WeakReference weakReference = this.sourceCoordinateView;
        View view = weakReference == null ? null : (View) weakReference.get();
        if (view == null) {
            if (this.sourceCoordinateView != null) {
                this.sourceCoordinateView = null;
            }
            updateResolvedSourceOffset(this.sourceOffsetX, this.sourceOffsetY);
        } else if (GlassViewCoordinates.positionInAncestor(this, view, this.coordinateMatrix, this.coordinatePoint)) {
            float[] fArr = this.coordinatePoint;
            updateResolvedSourceOffset(fArr[0], fArr[1]);
        } else {
            getLocationInWindow(this.locationBuffer);
            view.getLocationInWindow(this.sourceLocationBuffer);
            int[] iArr = this.locationBuffer;
            int i = iArr[0];
            int[] iArr2 = this.sourceLocationBuffer;
            updateResolvedSourceOffset(i - iArr2[0], iArr[1] - iArr2[1]);
        }
    }

    private void updateResolvedSourceOffset(float f, float f2) {
        if (this.resolvedSourceOffsetX == f && this.resolvedSourceOffsetY == f2) {
            return;
        }
        this.resolvedSourceOffsetX = f;
        this.resolvedSourceOffsetY = f2;
        GlassBackdropSource glassBackdropSource = this.backdropSource;
        if (glassBackdropSource != null) {
            glassBackdropSource.onSurfacePositionChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void buildGlassPath(Path path, int i, int i2) {
        if (this.capsule) {
            GlassContinuousPathFactory.capsule(path, i, i2);
            this.effectiveRadii.set(Math.min(i, i2) * 0.5f);
            return;
        }
        GlassRadii glassRadii = this.effectiveRadii;
        GlassRadii glassRadii2 = this.radii;
        float f = i;
        float f2 = i2;
        glassRadii.set(glassRadii2.topLeft, glassRadii2.topRight, glassRadii2.bottomRight, glassRadii2.bottomLeft).clamp(f, f2);
        GlassRadii glassRadii3 = this.effectiveRadii;
        GlassContinuousPathFactory.roundedRect(path, f, f2, glassRadii3.topLeft, glassRadii3.topRight, glassRadii3.bottomRight, glassRadii3.bottomLeft);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            super.dispatchDraw(canvas);
            return;
        }
        ensurePath(width, height);
        int save = canvas.save();
        canvas.clipPath(this.glassPath);
        super.dispatchDraw(canvas);
        onDrawGlassForeground(canvas);
        this.highlightRenderer.draw(canvas, this.glassPath, width, height, this.effectiveRadii, this.highlightSpec);
        this.shadowRenderer.drawInner(canvas, this.glassPath, width, height, this.innerShadowSpec);
        canvas.restoreToCount(save);
        recordExportedBackdrop(width, height);
    }

    public GlassBackdropSource getBackdropSource() {
        return this.backdropSource;
    }

    public GlassEffectSpec getEffectSpec() {
        return this.effectSpec;
    }

    public GlassLayerBackdrop getExportedBackdrop() {
        return this.exportedBackdrop;
    }

    public int getTintColor() {
        return this.tintPaint.getColor();
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return true;
    }

    public void invalidateGlass() {
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final void invalidateGlassPath() {
        this.pathDirty = true;
        invalidate();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        resolveSourceOffset();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            super.onDraw(canvas);
            return;
        }
        resolveSourceOffset();
        ensurePath(width, height);
        this.shadowRenderer.drawOuter(canvas, this.glassPath, width, height, this.outerShadowSpec);
        int save = canvas.save();
        canvas.clipPath(this.glassPath);
        drawBackdrop(canvas, width, height);
        if (Color.alpha(this.tintPaint.getColor()) != 0) {
            canvas.drawPath(this.glassPath, this.tintPaint);
        }
        super.onDraw(canvas);
        onDrawGlassContent(canvas);
        canvas.restoreToCount(save);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onDrawGlassContent(Canvas canvas) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onDrawGlassForeground(Canvas canvas) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.pathDirty = true;
        this.api31Backdrop = null;
    }

    public void setBackdropLayerTransform(GlassLayerTransform glassLayerTransform) {
        this.explicitBackdropLayerTransform = glassLayerTransform;
        invalidate();
    }

    public void setBackdropSource(GlassBackdropSource glassBackdropSource) {
        if (this.backdropSource == glassBackdropSource) {
            return;
        }
        this.backdropSource = glassBackdropSource;
        this.api31Backdrop = null;
        if (glassBackdropSource != null) {
            glassBackdropSource.onSurfacePositionChanged();
        }
        invalidate();
    }

    public void setCapsule(boolean z) {
        if (this.capsule == z) {
            return;
        }
        this.capsule = z;
        this.pathDirty = true;
        invalidate();
    }

    public void setEffectSpec(GlassEffectSpec glassEffectSpec) {
        this.effectSpec = glassEffectSpec;
        invalidate();
    }

    public void setExportedBackdrop(GlassLayerBackdrop glassLayerBackdrop) {
        if (this.exportedBackdrop == glassLayerBackdrop) {
            return;
        }
        this.exportedBackdrop = glassLayerBackdrop;
        if (glassLayerBackdrop != null) {
            WeakReference weakReference = this.sourceCoordinateView;
            glassLayerBackdrop.setCoordinateRoot(weakReference == null ? null : (View) weakReference.get());
        }
        invalidate();
    }

    public void setHighlightSpec(GlassHighlightSpec glassHighlightSpec) {
        this.highlightSpec = glassHighlightSpec;
        invalidate();
    }

    public void setInnerShadowSpec(GlassInnerShadowSpec glassInnerShadowSpec) {
        this.innerShadowSpec = glassInnerShadowSpec;
        invalidate();
    }

    public void setOuterShadowSpec(GlassShadowSpec glassShadowSpec) {
        this.outerShadowSpec = glassShadowSpec;
        invalidate();
    }

    public void setRadii(GlassRadii glassRadii) {
        this.capsule = false;
        this.radii.set(glassRadii.topLeft, glassRadii.topRight, glassRadii.bottomRight, glassRadii.bottomLeft);
        this.pathDirty = true;
        invalidate();
    }

    public void setSourceCoordinateView(View view) {
        WeakReference weakReference = this.sourceCoordinateView;
        if ((weakReference == null ? null : (View) weakReference.get()) == view) {
            return;
        }
        this.sourceCoordinateView = view != null ? new WeakReference(view) : null;
        GlassLayerBackdrop glassLayerBackdrop = this.exportedBackdrop;
        if (glassLayerBackdrop != null) {
            glassLayerBackdrop.setCoordinateRoot(view);
        }
        resolveSourceOffset();
        invalidate();
    }

    public void setSourceOffset(float f, float f2) {
        this.sourceOffsetX = f;
        this.sourceOffsetY = f2;
        if (this.sourceCoordinateView == null) {
            updateResolvedSourceOffset(f, f2);
        }
    }

    public void setTelegramBackdropSource(BlurredBackgroundSource blurredBackgroundSource) {
        if (blurredBackgroundSource == null) {
            setBackdropSource(null);
        } else {
            setBackdropSource(new TelegramBlurredBackgroundSourceAdapter(blurredBackgroundSource));
        }
    }

    public void setTintColor(int i) {
        if (this.tintPaint.getColor() == i) {
            return;
        }
        this.tintPaint.setColor(i);
        invalidate();
    }
}
