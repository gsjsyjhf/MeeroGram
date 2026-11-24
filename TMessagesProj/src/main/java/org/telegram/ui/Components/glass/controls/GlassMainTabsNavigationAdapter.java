package org.telegram.ui.Components.glass.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import j$.util.Objects;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSource;
import org.telegram.ui.Components.glass.GlassTabView;
import org.telegram.ui.Components.glass.controls.GlassDampedDragController;
import org.telegram.ui.Components.glass.controls.GlassMainTabsNavigationAdapter;
import org.telegram.ui.Components.glass.engine.GlassBackdropSource;
import org.telegram.ui.Components.glass.engine.GlassCombinedBackdropSource;
import org.telegram.ui.Components.glass.engine.GlassEffectSpec;
import org.telegram.ui.Components.glass.engine.GlassHighlightSpec;
import org.telegram.ui.Components.glass.engine.GlassInnerShadowSpec;
import org.telegram.ui.Components.glass.engine.GlassInteractiveHighlightRenderer;
import org.telegram.ui.Components.glass.engine.GlassLayerBackdrop;
import org.telegram.ui.Components.glass.engine.GlassShadowSpec;
import org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout;
import org.telegram.ui.Components.glass.engine.TelegramBlurredBackgroundSourceAdapter;
import org.telegram.ui.Components.glass.motion.GlassFloatSpring;
import org.telegram.ui.Components.glass.motion.GlassInteractiveHighlightController;
import org.telegram.ui.Components.glass.motion.GlassSpringSpec;
import org.telegram.ui.Components.glass.shape.GlassContinuousPathFactory;
import org.telegram.ui.MainTabsLayout;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassMainTabsNavigationAdapter extends FrameLayout {
    private int accentIconColor;
    private int accentTextColor;
    private boolean active;
    private final Matrix adapterToRootMatrix;
    private boolean appliedAppearanceDark;
    private float appliedAppearanceDensity;
    private float appliedOpticalProgress;
    private final ContainerSurface containerSurface;
    private boolean contentInvalidationPosted;
    private final Runnable contentInvalidationRunnable;
    private boolean darkTheme;
    private boolean externalInteractionDragged;
    private float externalInteractionPosition;
    private int externalInteractionStartIndex;
    private boolean externalPressed;
    private final RectF firstTabBounds;
    private boolean hasSelectionProgress;
    private final RectF indicatorBounds;
    private final IndicatorSurface indicatorSurface;
    private final GlassInteractiveHighlightController interactiveHighlight;
    private final GlassInteractiveHighlightRenderer interactiveHighlightRenderer;
    private final RectF layoutBounds;
    private GlassDampedDragController motion;
    private int motionVisibleTabCount;
    private int normalIconColor;
    private int normalTextColor;
    private MainTabsLayout observedTabsLayout;
    private final GlassFloatSpring panelDragSpring;
    private RawBackdropProvider rawBackdropProvider;
    private GlassBackdropSource rawBackdropSource;
    private final Matrix rootToAdapterMatrix;
    private final RectF secondTabBounds;
    private int selectedIndex;
    private float selectionProgressFraction;
    private GlassTabView selectionProgressFrom;
    private GlassTabView selectionProgressTo;
    private View sourceCoordinateView;
    private final Matrix tabToAdapterMatrix;
    private final TabsExportSurface tabsExportSurface;
    private final GlassLayerBackdrop tabsLayer;
    private MainTabsLayout tabsLayout;
    private final Matrix viewToRootMatrix;
    private final MainTabsLayout.VisualContentListener visualContentListener;
    private final MainTabsLayout.VisualGeometryListener visualGeometryListener;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public final class ContainerSurface extends GlassNavigationContainerSurface {
        ContainerSurface(Context context) {
            super(context);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.telegram.ui.Components.glass.controls.GlassNavigationContainerSurface, org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
        public void onDrawGlassContent(Canvas canvas) {
            super.onDrawGlassContent(canvas);
            GlassMainTabsNavigationAdapter glassMainTabsNavigationAdapter = GlassMainTabsNavigationAdapter.this;
            glassMainTabsNavigationAdapter.drawTabsFromSurface(canvas, this, glassMainTabsNavigationAdapter.normalIconColor, GlassMainTabsNavigationAdapter.this.normalTextColor, 1.0f, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public final class IndicatorSurface extends GlassSurfaceFrameLayout {
        private final Matrix restingShapeMatrix;
        private float shapePress;
        private float shapeScaleX;
        private float shapeScaleY;

        IndicatorSurface(Context context) {
            super(context);
            this.restingShapeMatrix = new Matrix();
            this.shapePress = 1.0f;
            this.shapeScaleX = 1.0f;
            this.shapeScaleY = 1.0f;
            setCapsule(true);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
        public void buildGlassPath(Path path, int i, int i2) {
            super.buildGlassPath(path, i, i2);
            if (this.shapePress >= 1.0f) {
                return;
            }
            float f = i * this.shapeScaleX;
            float f2 = i2 * this.shapeScaleY;
            GlassContinuousPathFactory.roundedRect(path, f, f2, Math.min(f, f2) * GlassControlUtils.lerp(0.5f, 0.5f, this.shapePress));
            this.restingShapeMatrix.setScale(1.0f / this.shapeScaleX, 1.0f / this.shapeScaleY);
            path.transform(this.restingShapeMatrix);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
        public void onDrawGlassContent(Canvas canvas) {
            float opticalProgress = GlassMainTabsNavigationAdapter.this.getOpticalProgress(GlassMainTabsNavigationAdapter.this.getPressProgress());
            canvas.drawColor(GlassControlUtils.multiplyAlpha(GlassMainTabsNavigationAdapter.this.darkTheme ? -1 : -16777216, (GlassMainTabsNavigationAdapter.this.darkTheme ? 0.1f : 0.06f) * (1.0f - opticalProgress)));
            canvas.drawColor(GlassControlUtils.multiplyAlpha(-16777216, opticalProgress * 0.03f));
        }

        void updateRestingShape(float f) {
            float lerp = GlassControlUtils.lerp(Math.max(0.001f, getScaleX()), 1.0f, f);
            float lerp2 = GlassControlUtils.lerp(Math.max(0.001f, getScaleY()), 1.0f, f);
            if (this.shapePress == f && this.shapeScaleX == lerp && this.shapeScaleY == lerp2) {
                return;
            }
            this.shapePress = f;
            this.shapeScaleX = lerp;
            this.shapeScaleY = lerp2;
            invalidateGlassPath();
        }
    }

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public interface RawBackdropProvider {
        BlurredBackgroundSource acquireRawSource();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public final class TabsExportSurface extends GlassSurfaceFrameLayout {
        private final Path exportPath;

        TabsExportSurface(Context context) {
            super(context);
            this.exportPath = new Path();
            setCapsule(true);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
        public void onDrawGlassContent(Canvas canvas) {
            GlassContinuousPathFactory.capsule(this.exportPath, getWidth(), getHeight());
            float pressProgress = GlassMainTabsNavigationAdapter.this.getPressProgress();
            GlassMainTabsNavigationAdapter.this.interactiveHighlightRenderer.draw(canvas, this.exportPath, getWidth(), getHeight(), GlassMainTabsNavigationAdapter.this.interactiveHighlight, GlassMainTabsNavigationAdapter.this.getAnimatedItemCenterInContainer(), getHeight() * 0.5f, GlassMainTabsNavigationAdapter.this.getOpticalProgress(pressProgress));
            float lerp = GlassControlUtils.lerp(1.0f, 1.2f, pressProgress);
            GlassMainTabsNavigationAdapter.this.drawTabsFromSurface(canvas, this, GlassMainTabsNavigationAdapter.this.accentIconColor, GlassMainTabsNavigationAdapter.this.accentTextColor, lerp, true);
        }
    }

    public static /* synthetic */ void $r8$lambda$oy0edqoMshp1DD2QgI8G7pu6aQk(GlassMainTabsNavigationAdapter glassMainTabsNavigationAdapter, GlassInteractiveHighlightController glassInteractiveHighlightController) {
        if (glassMainTabsNavigationAdapter.active) {
            glassMainTabsNavigationAdapter.invalidateGlassSurfaces();
        }
    }

    public static /* synthetic */ void $r8$lambda$rTxyDirJ7MFHMrgqlThShebnoDQ(GlassMainTabsNavigationAdapter glassMainTabsNavigationAdapter, GlassDampedDragController glassDampedDragController) {
        if (glassMainTabsNavigationAdapter.active) {
            glassMainTabsNavigationAdapter.updateIndicatorGeometry();
            glassMainTabsNavigationAdapter.updateIndicatorTransform();
            glassMainTabsNavigationAdapter.invalidateGlassSurfaces();
        }
    }

    public static /* synthetic */ void $r8$lambda$xVXwmdyFuTGED3WBEiVDFNnuKr4(GlassMainTabsNavigationAdapter glassMainTabsNavigationAdapter, GlassFloatSpring glassFloatSpring, float f, float f2) {
        if (glassMainTabsNavigationAdapter.active) {
            glassMainTabsNavigationAdapter.updateIndicatorTransform();
            glassMainTabsNavigationAdapter.invalidateGlassSurfaces();
        }
    }

    public GlassMainTabsNavigationAdapter(Context context) {
        this(context, null);
    }

    public GlassMainTabsNavigationAdapter(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GlassMainTabsNavigationAdapter(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.tabsLayer = new GlassLayerBackdrop();
        GlassInteractiveHighlightController glassInteractiveHighlightController = new GlassInteractiveHighlightController();
        this.interactiveHighlight = glassInteractiveHighlightController;
        this.interactiveHighlightRenderer = new GlassInteractiveHighlightRenderer();
        GlassFloatSpring glassFloatSpring = new GlassFloatSpring(0.0f, new GlassSpringSpec(1.0f, 300.0f, 0.5f));
        this.panelDragSpring = glassFloatSpring;
        this.tabToAdapterMatrix = new Matrix();
        this.viewToRootMatrix = new Matrix();
        this.adapterToRootMatrix = new Matrix();
        this.rootToAdapterMatrix = new Matrix();
        this.layoutBounds = new RectF();
        this.firstTabBounds = new RectF();
        this.secondTabBounds = new RectF();
        this.indicatorBounds = new RectF();
        this.visualGeometryListener = new MainTabsLayout.VisualGeometryListener() { // from class: org.telegram.ui.Components.glass.controls.GlassMainTabsNavigationAdapter$$ExternalSyntheticLambda1
            public final void onVisualGeometryChanged() {
                GlassMainTabsNavigationAdapter.this.onTabsVisualGeometryChanged();
            }
        };
        this.visualContentListener = new MainTabsLayout.VisualContentListener() { // from class: org.telegram.ui.Components.glass.controls.GlassMainTabsNavigationAdapter$$ExternalSyntheticLambda2
            public final void onVisualContentInvalidated() {
                GlassMainTabsNavigationAdapter.this.onTabsVisualContentInvalidated();
            }
        };
        this.contentInvalidationRunnable = new Runnable() { // from class: org.telegram.ui.Components.glass.controls.GlassMainTabsNavigationAdapter$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                GlassMainTabsNavigationAdapter.this.dispatchPendingContentInvalidation();
            }
        };
        this.normalIconColor = -16777216;
        this.normalTextColor = -16777216;
        this.accentIconColor = -16745729;
        this.accentTextColor = -16745729;
        this.appliedOpticalProgress = Float.NaN;
        this.appliedAppearanceDensity = Float.NaN;
        setClipChildren(false);
        setClipToPadding(false);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(4);
        ContainerSurface containerSurface = new ContainerSurface(context);
        this.containerSurface = containerSurface;
        TabsExportSurface tabsExportSurface = new TabsExportSurface(context);
        this.tabsExportSurface = tabsExportSurface;
        IndicatorSurface indicatorSurface = new IndicatorSurface(context);
        this.indicatorSurface = indicatorSurface;
        preparePassiveSurface(containerSurface);
        preparePassiveSurface(tabsExportSurface);
        preparePassiveSurface(indicatorSurface);
        addView(containerSurface);
        addView(tabsExportSurface);
        addView(indicatorSurface);
        clearIndicatorBounds();
        glassFloatSpring.setUpdateListener(new GlassFloatSpring.UpdateListener() { // from class: org.telegram.ui.Components.glass.controls.GlassMainTabsNavigationAdapter$$ExternalSyntheticLambda4
            @Override // org.telegram.ui.Components.glass.motion.GlassFloatSpring.UpdateListener
            public final void onSpringUpdate(GlassFloatSpring glassFloatSpring2, float f, float f2) {
                GlassMainTabsNavigationAdapter.$r8$lambda$xVXwmdyFuTGED3WBEiVDFNnuKr4(GlassMainTabsNavigationAdapter.this, glassFloatSpring2, f, f2);
            }
        });
        glassInteractiveHighlightController.setListener(new GlassInteractiveHighlightController.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassMainTabsNavigationAdapter$$ExternalSyntheticLambda5
            @Override // org.telegram.ui.Components.glass.motion.GlassInteractiveHighlightController.Listener
            public final void onHighlightChanged(GlassInteractiveHighlightController glassInteractiveHighlightController2) {
                GlassMainTabsNavigationAdapter.$r8$lambda$oy0edqoMshp1DD2QgI8G7pu6aQk(GlassMainTabsNavigationAdapter.this, glassInteractiveHighlightController2);
            }
        });
        applyStaticAppearance();
        super.setVisibility(8);
    }

    private static void appendMatrixToRoot(View view, Matrix matrix) {
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            View view2 = (View) parent;
            appendMatrixToRoot(view2, matrix);
            matrix.preTranslate(-view2.getScrollX(), -view2.getScrollY());
        }
        matrix.preTranslate(view.getLeft(), view.getTop());
        Matrix matrix2 = view.getMatrix();
        if (matrix2.isIdentity()) {
            return;
        }
        matrix.preConcat(matrix2);
    }

    private void applyDynamicAppearance() {
        float opticalProgress = getOpticalProgress(getPressProgress());
        float f = getResources().getDisplayMetrics().density;
        if (this.appliedOpticalProgress == opticalProgress && this.appliedAppearanceDensity == f && this.appliedAppearanceDark == this.darkTheme) {
            return;
        }
        this.appliedOpticalProgress = opticalProgress;
        this.appliedAppearanceDensity = f;
        boolean z = this.darkTheme;
        this.appliedAppearanceDark = z;
        this.tabsExportSurface.setTintColor(GlassControlUtils.multiplyAlpha(GlassNavigationContainerSurface.tintForTheme(z), 1.0f - opticalProgress));
        this.tabsExportSurface.setEffectSpec(new GlassEffectSpec(0.0f, 1.0f, 1.5f, dp(8.0f), dp(24.0f) * opticalProgress, dp(24.0f) * opticalProgress, false, false));
        GlassHighlightSpec defaultHighlight = GlassHighlightSpec.defaultHighlight(f);
        this.tabsExportSurface.setHighlightSpec(new GlassHighlightSpec(defaultHighlight.width, defaultHighlight.blurRadius, opticalProgress * (this.darkTheme ? 1.0f : 0.25f), defaultHighlight.color, defaultHighlight.angleDegrees, defaultHighlight.falloff, defaultHighlight.style, defaultHighlight.additive));
        this.tabsExportSurface.setOuterShadowSpec(null);
        this.indicatorSurface.setEffectSpec(GlassEffectSpec.lensOnly(0.0f, dp(10.0f) * opticalProgress, dp(14.0f) * opticalProgress, true));
        this.indicatorSurface.setTintColor(0);
        this.indicatorSurface.setHighlightSpec(new GlassHighlightSpec(defaultHighlight.width, defaultHighlight.blurRadius, opticalProgress, defaultHighlight.color, defaultHighlight.angleDegrees, defaultHighlight.falloff, defaultHighlight.style, defaultHighlight.additive));
        GlassShadowSpec defaultShadow = GlassShadowSpec.defaultShadow(f);
        this.indicatorSurface.setOuterShadowSpec(new GlassShadowSpec(defaultShadow.radius, defaultShadow.offsetX, defaultShadow.offsetY, defaultShadow.color, opticalProgress));
        this.indicatorSurface.setInnerShadowSpec(new GlassInnerShadowSpec(dp(8.0f) * opticalProgress, 0.0f, dp(8.0f) * opticalProgress, 637534208, opticalProgress));
    }

    private void applyStaticAppearance() {
        this.containerSurface.setDarkTheme(this.darkTheme);
        this.tabsExportSurface.setInnerShadowSpec(null);
        applyDynamicAppearance();
    }

    private void bindBackdrop(GlassBackdropSource glassBackdropSource, View view) {
        this.containerSurface.setBackdropSource(glassBackdropSource);
        this.tabsExportSurface.setBackdropSource(glassBackdropSource);
        this.indicatorSurface.setBackdropSource(GlassCombinedBackdropSource.of(glassBackdropSource, this.tabsLayer));
        this.containerSurface.setSourceCoordinateView(view);
        this.tabsExportSurface.setSourceCoordinateView(view);
        this.indicatorSurface.setSourceCoordinateView(view);
        this.tabsLayer.setCoordinateRoot(view);
    }

    private void cancelPendingContentInvalidation() {
        removeCallbacks(this.contentInvalidationRunnable);
        this.contentInvalidationPosted = false;
    }

    private int clampIndex(int i) {
        return Math.max(0, Math.min(i, Math.max(0, getVisibleTabCount() - 1)));
    }

    private float clampPosition(float f) {
        return (Float.isNaN(f) || Float.isInfinite(f)) ? this.selectedIndex : GlassControlUtils.clamp(f, 0.0f, Math.max(0.0f, getVisibleTabCount() - 1.0f));
    }

    private void clearIndicatorBounds() {
        this.indicatorBounds.setEmpty();
        this.indicatorSurface.setVisibility(4);
    }

    private void clearSelectionProgressInternal(boolean z) {
        if (!this.hasSelectionProgress && this.selectionProgressFrom == null && this.selectionProgressTo == null) {
            return;
        }
        this.hasSelectionProgress = false;
        this.selectionProgressFrom = null;
        this.selectionProgressTo = null;
        this.selectionProgressFraction = 0.0f;
        if (z && this.active) {
            updateIndicatorGeometry();
            invalidateGlassSurfaces();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchPendingContentInvalidation() {
        try {
            if (this.active) {
                invalidateGlassSurfaces();
            }
        } finally {
            this.contentInvalidationPosted = false;
        }
    }

    private void disposeMotion() {
        GlassDampedDragController glassDampedDragController = this.motion;
        if (glassDampedDragController != null) {
            glassDampedDragController.dispose();
            this.motion = null;
        }
        this.motionVisibleTabCount = 0;
    }

    private float dp(float f) {
        return GlassControlUtils.dp(this, f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void drawTabsFromSurface(Canvas canvas, View view, int i, int i2, float f, boolean z) {
        int save = canvas.save();
        canvas.translate(-view.getLeft(), -view.getTop());
        drawTabsInAdapterCoordinates(canvas, i, i2, f, z);
        canvas.restoreToCount(save);
    }

    private void drawTabsInAdapterCoordinates(Canvas canvas, int i, int i2, float f, boolean z) {
        Canvas canvas2;
        int i3;
        int i4;
        float f2;
        boolean z2;
        int save;
        MainTabsLayout mainTabsLayout = this.tabsLayout;
        if (mainTabsLayout == null) {
            return;
        }
        mainTabsLayout.beginExternalNavigationMirrorDraw();
        int i5 = 0;
        while (i5 < mainTabsLayout.getChildCount()) {
            try {
                View childAt = mainTabsLayout.getChildAt(i5);
                if ((childAt instanceof GlassTabView) && childAt.getVisibility() == 0 && childAt.getAlpha() > 0.0f && resolveViewToAdapterMatrix(childAt, this.tabToAdapterMatrix)) {
                    int round = Math.round(GlassControlUtils.clamp01(childAt.getAlpha()) * 255.0f);
                    if (round < 255) {
                        canvas2 = canvas;
                        save = canvas2.saveLayerAlpha(0.0f, 0.0f, getWidth(), getHeight(), round);
                    } else {
                        canvas2 = canvas;
                        save = canvas2.save();
                    }
                    canvas2.concat(this.tabToAdapterMatrix);
                    i3 = i;
                    i4 = i2;
                    f2 = f;
                    z2 = z;
                    ((GlassTabView) childAt).drawNavigationLayer(canvas2, i3, i4, f2, z2);
                    canvas2.restoreToCount(save);
                    i5++;
                    canvas = canvas2;
                    i = i3;
                    i2 = i4;
                    f = f2;
                    z = z2;
                }
                canvas2 = canvas;
                i3 = i;
                i4 = i2;
                f2 = f;
                z2 = z;
                i5++;
                canvas = canvas2;
                i = i3;
                i2 = i4;
                f = f2;
                z = z2;
            } catch (Throwable th) {
                mainTabsLayout.endExternalNavigationMirrorDraw();
                throw th;
            }
        }
        mainTabsLayout.endExternalNavigationMirrorDraw();
    }

    private void ensureMotion() {
        int visibleTabCount = getVisibleTabCount();
        if (this.motion == null || this.motionVisibleTabCount != visibleTabCount) {
            recreateMotion(this.selectedIndex);
        }
    }

    private int findVisibleOrdinal(GlassTabView glassTabView) {
        MainTabsLayout mainTabsLayout = this.tabsLayout;
        if (mainTabsLayout != null && glassTabView != null) {
            int i = 0;
            for (int i2 = 0; i2 < mainTabsLayout.getChildCount(); i2++) {
                View childAt = mainTabsLayout.getChildAt(i2);
                if ((childAt instanceof GlassTabView) && mainTabsLayout.isViewVisible(childAt)) {
                    if (childAt == glassTabView) {
                        return i;
                    }
                    i++;
                }
            }
        }
        return -1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float getAnimatedItemCenterInContainer() {
        return (this.indicatorBounds.centerX() - this.containerSurface.getLeft()) + getPanelOffset();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float getOpticalProgress(float f) {
        return GlassControlUtils.easeOut(f);
    }

    private float getPanelOffset() {
        float width = this.containerSurface.getWidth();
        if (width <= 0.0f) {
            return 0.0f;
        }
        float clamp = GlassControlUtils.clamp(this.panelDragSpring.getValue() / width, -1.0f, 1.0f);
        return dp(4.0f) * Math.signum(clamp) * GlassControlUtils.easeOut(Math.abs(clamp));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float getPressProgress() {
        GlassDampedDragController glassDampedDragController = this.motion;
        if (glassDampedDragController == null) {
            return 0.0f;
        }
        return GlassControlUtils.clamp01(glassDampedDragController.getPressProgress());
    }

    private float getVisibleOrdinalSpan(float f) {
        int visibleTabCount = getVisibleTabCount();
        if (visibleTabCount <= 1) {
            return 0.0f;
        }
        int min = Math.min(visibleTabCount - 2, Math.max(0, (int) Math.floor(GlassControlUtils.clamp(f, 0.0f, visibleTabCount - 1.0f))));
        int i = min + 1;
        GlassTabView visibleTabAt = getVisibleTabAt(min);
        GlassTabView visibleTabAt2 = getVisibleTabAt(i);
        if (visibleTabAt == null || visibleTabAt2 == null) {
            return 0.0f;
        }
        return (visibleTabAt2.getX() + (visibleTabAt2.getNavigationVisualWidth() * 0.5f)) - (visibleTabAt.getX() + (visibleTabAt.getNavigationVisualWidth() * 0.5f));
    }

    private GlassTabView getVisibleTabAt(int i) {
        MainTabsLayout mainTabsLayout = this.tabsLayout;
        if (mainTabsLayout == null) {
            return null;
        }
        int i2 = 0;
        for (int i3 = 0; i3 < mainTabsLayout.getChildCount(); i3++) {
            View childAt = mainTabsLayout.getChildAt(i3);
            if ((childAt instanceof GlassTabView) && mainTabsLayout.isViewVisible(childAt)) {
                if (i2 == i) {
                    return (GlassTabView) childAt;
                }
                i2++;
            }
        }
        return null;
    }

    private int getVisibleTabCount() {
        MainTabsLayout mainTabsLayout = this.tabsLayout;
        if (mainTabsLayout == null) {
            return 0;
        }
        int i = 0;
        for (int i2 = 0; i2 < mainTabsLayout.getChildCount(); i2++) {
            View childAt = mainTabsLayout.getChildAt(i2);
            if ((childAt instanceof GlassTabView) && mainTabsLayout.isViewVisible(childAt)) {
                i++;
            }
        }
        return i;
    }

    private static void interpolateBounds(RectF rectF, RectF rectF2, float f, RectF rectF3) {
        rectF3.set(GlassControlUtils.lerp(rectF.left, rectF2.left, f), GlassControlUtils.lerp(rectF.top, rectF2.top, f), GlassControlUtils.lerp(rectF.right, rectF2.right, f), GlassControlUtils.lerp(rectF.bottom, rectF2.bottom, f));
    }

    private void invalidateGlassSurfaces() {
        this.containerSurface.invalidateGlass();
        this.tabsExportSurface.invalidateGlass();
        this.indicatorSurface.invalidateGlass();
        invalidate();
    }

    private boolean isConfiguredVisibleTab(GlassTabView glassTabView) {
        if (this.tabsLayout == null || glassTabView == null) {
            return false;
        }
        MainTabsLayout parent = glassTabView.getParent();
        MainTabsLayout mainTabsLayout = this.tabsLayout;
        return parent == mainTabsLayout && mainTabsLayout.isViewVisible(glassTabView);
    }

    private boolean mapViewBounds(View view, RectF rectF) {
        if (view == null || view.getWidth() <= 0 || view.getHeight() <= 0 || !resolveViewToAdapterMatrix(view, this.tabToAdapterMatrix)) {
            return false;
        }
        float navigationVisualWidth = view instanceof GlassTabView ? ((GlassTabView) view).getNavigationVisualWidth() : view.getWidth();
        if (navigationVisualWidth > 0.0f && view.getHeight() > 0) {
            rectF.set(0.0f, 0.0f, navigationVisualWidth, view.getHeight());
            this.tabToAdapterMatrix.mapRect(rectF);
            return true;
        }
        return false;
    }

    private static void measureAndLayout(View view, RectF rectF) {
        int round = Math.round(rectF.left);
        int round2 = Math.round(rectF.top);
        int max = Math.max(round, Math.round(rectF.right));
        int max2 = Math.max(round2, Math.round(rectF.bottom));
        int i = max - round;
        int i2 = max2 - round2;
        if (view.getMeasuredWidth() != i || view.getMeasuredHeight() != i2) {
            view.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(i2, 1073741824));
        }
        view.layout(round, round2, max, max2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onTabsVisualContentInvalidated() {
        if (!this.active || this.contentInvalidationPosted) {
            return;
        }
        this.contentInvalidationPosted = true;
        postOnAnimation(this.contentInvalidationRunnable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onTabsVisualGeometryChanged() {
        if (this.active) {
            ensureMotion();
            updateContainerGeometry();
            updateIndicatorGeometry();
            updateIndicatorTransform();
            invalidateGlassSurfaces();
        }
    }

    private static void preparePassiveSurface(View view) {
        view.setClickable(false);
        view.setFocusable(false);
        view.setImportantForAccessibility(2);
    }

    private void recreateMotion(float f) {
        disposeMotion();
        int visibleTabCount = getVisibleTabCount();
        this.motionVisibleTabCount = visibleTabCount;
        if (visibleTabCount <= 0) {
            return;
        }
        float f2 = visibleTabCount - 1.0f;
        GlassDampedDragController listener = new GlassDampedDragController(GlassControlUtils.clamp(f, 0.0f, Math.max(0.0f, f2)), 0.0f, Math.max(1.0f, f2), 0.001f, 1.0f, 1.3928572f).trackVelocityOnlyDuringUserUpdates().setListener(new GlassDampedDragController.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassMainTabsNavigationAdapter$$ExternalSyntheticLambda0
            @Override // org.telegram.ui.Components.glass.controls.GlassDampedDragController.Listener
            public final void onAnimationChanged(GlassDampedDragController glassDampedDragController) {
                GlassMainTabsNavigationAdapter.$r8$lambda$rTxyDirJ7MFHMrgqlThShebnoDQ(GlassMainTabsNavigationAdapter.this, glassDampedDragController);
            }
        });
        this.motion = listener;
        if (visibleTabCount == 1) {
            listener.snapTo(0.0f);
        }
    }

    private void registerTabsListeners() {
        MainTabsLayout mainTabsLayout = this.tabsLayout;
        if (!this.active || mainTabsLayout == null || this.observedTabsLayout == mainTabsLayout) {
            return;
        }
        unregisterTabsListeners();
        this.observedTabsLayout = mainTabsLayout;
        mainTabsLayout.addVisualGeometryListener(this.visualGeometryListener);
        mainTabsLayout.addVisualContentListener(this.visualContentListener);
    }

    private boolean resolveIndicatorBounds(float f, RectF rectF) {
        if (this.hasSelectionProgress && isConfiguredVisibleTab(this.selectionProgressFrom) && isConfiguredVisibleTab(this.selectionProgressTo) && mapViewBounds(this.selectionProgressFrom, this.firstTabBounds) && mapViewBounds(this.selectionProgressTo, this.secondTabBounds)) {
            float f2 = this.selectionProgressFraction;
            if (this.motion != null) {
                int findVisibleOrdinal = findVisibleOrdinal(this.selectionProgressFrom);
                int findVisibleOrdinal2 = findVisibleOrdinal(this.selectionProgressTo);
                int i = findVisibleOrdinal2 - findVisibleOrdinal;
                if (findVisibleOrdinal >= 0 && findVisibleOrdinal2 >= 0 && i != 0) {
                    f2 = GlassControlUtils.clamp01((this.motion.getValue() - findVisibleOrdinal) / i);
                }
            }
            interpolateBounds(this.firstTabBounds, this.secondTabBounds, f2, rectF);
            return rectF.width() > 0.0f && rectF.height() > 0.0f;
        }
        int visibleTabCount = getVisibleTabCount();
        if (visibleTabCount == 0) {
            return false;
        }
        float clamp = GlassControlUtils.clamp(f, 0.0f, visibleTabCount - 1.0f);
        int floor = (int) Math.floor(clamp);
        int min = Math.min(visibleTabCount - 1, floor + 1);
        GlassTabView visibleTabAt = getVisibleTabAt(floor);
        GlassTabView visibleTabAt2 = getVisibleTabAt(min);
        if (mapViewBounds(visibleTabAt, this.firstTabBounds)) {
            if (!mapViewBounds(visibleTabAt2, this.secondTabBounds)) {
                this.secondTabBounds.set(this.firstTabBounds);
            }
            interpolateBounds(this.firstTabBounds, this.secondTabBounds, clamp - floor, rectF);
            return rectF.width() > 0.0f && rectF.height() > 0.0f;
        }
        return false;
    }

    private boolean resolveViewToAdapterMatrix(View view, Matrix matrix) {
        this.viewToRootMatrix.reset();
        appendMatrixToRoot(view, this.viewToRootMatrix);
        this.adapterToRootMatrix.reset();
        appendMatrixToRoot(this, this.adapterToRootMatrix);
        if (this.adapterToRootMatrix.invert(this.rootToAdapterMatrix)) {
            matrix.setConcat(this.rootToAdapterMatrix, this.viewToRootMatrix);
            return true;
        }
        return false;
    }

    private void unbindBackdrop() {
        this.containerSurface.setBackdropSource(null);
        this.tabsExportSurface.setBackdropSource(null);
        this.indicatorSurface.setBackdropSource(null);
        this.containerSurface.setSourceCoordinateView(null);
        this.tabsExportSurface.setSourceCoordinateView(null);
        this.indicatorSurface.setSourceCoordinateView(null);
        this.tabsLayer.setCoordinateRoot(null);
        this.tabsLayer.clear();
        this.rawBackdropSource = null;
    }

    private void unregisterTabsListeners() {
        MainTabsLayout mainTabsLayout = this.observedTabsLayout;
        if (mainTabsLayout == null) {
            return;
        }
        mainTabsLayout.removeVisualGeometryListener(this.visualGeometryListener);
        this.observedTabsLayout.removeVisualContentListener(this.visualContentListener);
        this.observedTabsLayout = null;
    }

    private void updateContainerGeometry() {
        if (!mapViewBounds(this.tabsLayout, this.layoutBounds)) {
            this.layoutBounds.set(0.0f, 0.0f, getWidth(), getHeight());
        }
        float dp = dp(8.0f);
        float f = 2.0f * dp;
        if (this.layoutBounds.width() > f && this.layoutBounds.height() > f) {
            this.layoutBounds.inset(dp, dp);
        }
        measureAndLayout(this.containerSurface, this.layoutBounds);
        measureAndLayout(this.tabsExportSurface, this.layoutBounds);
    }

    private void updateIndicatorGeometry() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            this.indicatorSurface.setVisibility(4);
            return;
        }
        GlassDampedDragController glassDampedDragController = this.motion;
        if (!resolveIndicatorBounds(glassDampedDragController == null ? this.selectedIndex : glassDampedDragController.getValue(), this.indicatorBounds)) {
            this.indicatorSurface.setVisibility(4);
            return;
        }
        float centerY = this.indicatorBounds.centerY();
        float dp = dp(56.0f) * 0.5f;
        RectF rectF = this.indicatorBounds;
        rectF.top = centerY - dp;
        rectF.bottom = centerY + dp;
        this.indicatorSurface.setVisibility(0);
        int max = Math.max(1, Math.round(this.indicatorBounds.width()));
        int max2 = Math.max(1, Math.round(this.indicatorBounds.height()));
        if (this.indicatorSurface.getMeasuredWidth() != max || this.indicatorSurface.getMeasuredHeight() != max2) {
            this.indicatorSurface.measure(View.MeasureSpec.makeMeasureSpec(max, 1073741824), View.MeasureSpec.makeMeasureSpec(max2, 1073741824));
        }
        if (this.indicatorSurface.getLeft() != 0 || this.indicatorSurface.getTop() != 0 || this.indicatorSurface.getWidth() != max || this.indicatorSurface.getHeight() != max2) {
            this.indicatorSurface.layout(0, 0, max, max2);
        }
        updateIndicatorPosition();
    }

    private void updateIndicatorPosition() {
        this.indicatorSurface.setTranslationX((this.indicatorBounds.centerX() - (this.indicatorSurface.getWidth() * 0.5f)) + getPanelOffset());
        this.indicatorSurface.setTranslationY(this.indicatorBounds.centerY() - (this.indicatorSurface.getHeight() * 0.5f));
    }

    private void updateIndicatorTransform() {
        float pressProgress = getPressProgress();
        float panelOffset = getPanelOffset();
        ContainerSurface containerSurface = this.containerSurface;
        containerSurface.setPivotX(containerSurface.getWidth() * 0.5f);
        ContainerSurface containerSurface2 = this.containerSurface;
        containerSurface2.setPivotY(containerSurface2.getHeight() * 0.5f);
        float dp = ((dp(16.0f) / Math.max(1.0f, this.containerSurface.getWidth())) * pressProgress) + 1.0f;
        this.containerSurface.setScaleX(dp);
        this.containerSurface.setScaleY(dp);
        this.containerSurface.setTranslationX(panelOffset);
        this.tabsExportSurface.setTranslationX(panelOffset);
        IndicatorSurface indicatorSurface = this.indicatorSurface;
        indicatorSurface.setPivotX(indicatorSurface.getWidth() * 0.5f);
        IndicatorSurface indicatorSurface2 = this.indicatorSurface;
        indicatorSurface2.setPivotY(indicatorSurface2.getHeight() * 0.5f);
        float max = this.indicatorSurface.getWidth() > 0 ? Math.max(dp(52.0f), (this.containerSurface.getWidth() / Math.max(1, getVisibleTabCount())) - dp(4.0f)) / this.indicatorSurface.getWidth() : 1.0f;
        float f = (1.0f - pressProgress) * 0.035714287f;
        GlassDampedDragController glassDampedDragController = this.motion;
        if (glassDampedDragController != null) {
            float velocity = glassDampedDragController.getVelocity() / 10.0f;
            float clamp = GlassControlUtils.clamp(0.75f * velocity, -0.2f, 0.2f);
            float clamp2 = GlassControlUtils.clamp(velocity * 0.25f, -0.2f, 0.2f);
            this.indicatorSurface.setScaleX((this.motion.getScaleX() / (1.0f - clamp)) * GlassControlUtils.lerp(max, 1.0f, pressProgress));
            this.indicatorSurface.setScaleY(((((this.motion.getScaleY() - 1.0f) * 0.5357143f) / 0.3928572f) + 0.85714287f + f) * (1.0f - clamp2));
        } else {
            this.indicatorSurface.setScaleX(max);
            this.indicatorSurface.setScaleY(0.89285713f);
        }
        updateIndicatorPosition();
        this.indicatorSurface.updateRestingShape(pressProgress);
        applyDynamicAppearance();
        if (pressProgress > 0.0f) {
            this.interactiveHighlight.setRenderPosition(getAnimatedItemCenterInContainer(), this.containerSurface.getHeight() * 0.5f);
        }
    }

    public boolean activate() {
        if (this.active) {
            return true;
        }
        super.setVisibility(8);
        MainTabsLayout mainTabsLayout = this.tabsLayout;
        View view = this.sourceCoordinateView;
        RawBackdropProvider rawBackdropProvider = this.rawBackdropProvider;
        if (Build.VERSION.SDK_INT >= 31 && mainTabsLayout != null && view != null && rawBackdropProvider != null && isAttachedToWindow() && mainTabsLayout.isAttachedToWindow() && view.isAttachedToWindow() && isHardwareAccelerated() && mainTabsLayout.isHardwareAccelerated() && view.isHardwareAccelerated()) {
            try {
                BlurredBackgroundSource acquireRawSource = rawBackdropProvider.acquireRawSource();
                if (acquireRawSource == null) {
                    return false;
                }
                TelegramBlurredBackgroundSourceAdapter telegramBlurredBackgroundSourceAdapter = new TelegramBlurredBackgroundSourceAdapter(acquireRawSource);
                this.rawBackdropSource = telegramBlurredBackgroundSourceAdapter;
                bindBackdrop(telegramBlurredBackgroundSourceAdapter, view);
                this.active = true;
                int clampIndex = clampIndex(this.selectedIndex);
                this.selectedIndex = clampIndex;
                this.externalPressed = false;
                this.externalInteractionDragged = false;
                this.externalInteractionStartIndex = clampIndex;
                float f = clampIndex;
                this.externalInteractionPosition = f;
                recreateMotion(f);
                this.panelDragSpring.snapTo(0.0f);
                applyStaticAppearance();
                registerTabsListeners();
                super.setVisibility(0);
                requestLayout();
                invalidateGlassSurfaces();
                return true;
            } catch (Throwable unused) {
            }
        }
        return false;
    }

    public void beginExternalInteraction() {
        if (this.active) {
            clearSelectionProgressInternal(false);
            ensureMotion();
            if (this.motion == null) {
                return;
            }
            this.panelDragSpring.cancel();
            this.externalInteractionDragged = false;
            this.externalInteractionStartIndex = this.selectedIndex;
            this.externalInteractionPosition = this.motion.getTargetValue();
            setPressedState(true);
            this.interactiveHighlight.press(getAnimatedItemCenterInContainer(), this.containerSurface.getHeight() * 0.5f);
            this.indicatorSurface.setPressed(true);
        }
    }

    public void clearSelectionProgress() {
        clearSelectionProgressInternal(true);
    }

    public void configure(MainTabsLayout mainTabsLayout, View view, RawBackdropProvider rawBackdropProvider) {
        if (this.active) {
            deactivate();
        }
        if (this.tabsLayout != mainTabsLayout) {
            unregisterTabsListeners();
        }
        this.tabsLayout = mainTabsLayout;
        this.sourceCoordinateView = view;
        this.rawBackdropProvider = rawBackdropProvider;
        clearSelectionProgressInternal(false);
        clearIndicatorBounds();
        int clampIndex = clampIndex(this.selectedIndex);
        this.selectedIndex = clampIndex;
        this.externalPressed = false;
        this.externalInteractionDragged = false;
        this.externalInteractionStartIndex = clampIndex;
        this.externalInteractionPosition = clampIndex;
        disposeMotion();
        this.panelDragSpring.snapTo(0.0f);
        super.setVisibility(8);
    }

    public void deactivate() {
        this.active = false;
        this.externalPressed = false;
        this.externalInteractionDragged = false;
        cancelPendingContentInvalidation();
        unregisterTabsListeners();
        super.setVisibility(8);
        disposeMotion();
        this.panelDragSpring.cancel();
        this.panelDragSpring.snapTo(0.0f);
        this.interactiveHighlight.release();
        this.indicatorSurface.setPressed(false);
        unbindBackdrop();
        clearIndicatorBounds();
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    public void dragExternalInteractionBy(float f) {
        if (!this.active || !this.externalPressed || this.motion == null || Float.isNaN(f) || Float.isInfinite(f) || f == 0.0f) {
            return;
        }
        clearSelectionProgressInternal(false);
        float visibleOrdinalSpan = getVisibleOrdinalSpan(this.externalInteractionPosition);
        if (Math.abs(visibleOrdinalSpan) > 0.5f) {
            float clampPosition = clampPosition(this.externalInteractionPosition + (f / visibleOrdinalSpan));
            this.externalInteractionPosition = clampPosition;
            this.motion.updateValue(clampPosition);
            this.externalInteractionDragged = true;
        }
        GlassFloatSpring glassFloatSpring = this.panelDragSpring;
        glassFloatSpring.snapTo(glassFloatSpring.getValue() + f);
        this.interactiveHighlight.setRenderPosition(getAnimatedItemCenterInContainer(), this.containerSurface.getHeight() * 0.5f);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        if (this.active) {
            super.draw(canvas);
        }
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        TabsExportSurface tabsExportSurface = this.tabsExportSurface;
        if (view == tabsExportSurface) {
            if (!this.active || tabsExportSurface.getWidth() <= 0 || this.tabsExportSurface.getHeight() <= 0) {
                return true;
            }
            GlassLayerBackdrop glassLayerBackdrop = this.tabsLayer;
            TabsExportSurface tabsExportSurface2 = this.tabsExportSurface;
            int width = tabsExportSurface2.getWidth();
            int height = this.tabsExportSurface.getHeight();
            final TabsExportSurface tabsExportSurface3 = this.tabsExportSurface;
            Objects.requireNonNull(tabsExportSurface3);
            glassLayerBackdrop.record(tabsExportSurface2, width, height, new GlassLayerBackdrop.Recorder() { // from class: org.telegram.ui.Components.glass.controls.GlassMainTabsNavigationAdapter$$ExternalSyntheticLambda6
                @Override // org.telegram.ui.Components.glass.engine.GlassLayerBackdrop.Recorder
                public final void draw(Canvas canvas2) {
                    GlassMainTabsNavigationAdapter.TabsExportSurface.this.draw(canvas2);
                }
            });
            return true;
        }
        return super.drawChild(canvas, view, j);
    }

    public int endExternalInteraction(boolean z) {
        if (!this.active || this.motion == null) {
            this.externalPressed = false;
            this.externalInteractionDragged = false;
            this.panelDragSpring.cancel();
            this.panelDragSpring.snapTo(0.0f);
            this.indicatorSurface.setPressed(false);
            this.interactiveHighlight.release();
            return -1;
        }
        int clampIndex = z ? clampIndex(this.externalInteractionStartIndex) : clampIndex(Math.round(this.externalInteractionPosition));
        boolean z2 = (z || !this.externalInteractionDragged || clampIndex == this.externalInteractionStartIndex) ? false : true;
        this.externalPressed = false;
        this.externalInteractionDragged = false;
        float f = clampIndex;
        this.externalInteractionPosition = f;
        this.selectedIndex = clampIndex;
        this.indicatorSurface.setPressed(false);
        this.interactiveHighlight.release();
        this.motion.animateToValue(f);
        this.panelDragSpring.animateTo(0.0f);
        if (z2) {
            return clampIndex;
        }
        return -1;
    }

    public boolean isActive() {
        return this.active;
    }

    public void onBackdropFrameAvailable() {
        if (this.active) {
            invalidateGlassSurfaces();
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        deactivate();
        super.onDetachedFromWindow();
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        updateContainerGeometry();
        updateIndicatorGeometry();
        updateIndicatorTransform();
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int resolveSize = View.resolveSize(getSuggestedMinimumWidth(), i);
        int resolveSize2 = View.resolveSize(getSuggestedMinimumHeight(), i2);
        setMeasuredDimension(resolveSize, resolveSize2);
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max(0, resolveSize), 1073741824);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(Math.max(0, resolveSize2), 1073741824);
        this.containerSurface.measure(makeMeasureSpec, makeMeasureSpec2);
        this.tabsExportSurface.measure(makeMeasureSpec, makeMeasureSpec2);
        this.indicatorSurface.measure(makeMeasureSpec, makeMeasureSpec2);
    }

    public void onTabsGeometryOrContentChanged() {
        if (this.active) {
            requestLayout();
            updateIndicatorGeometry();
            invalidateGlassSurfaces();
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    public void release() {
        deactivate();
        unregisterTabsListeners();
        clearSelectionProgressInternal(false);
        clearIndicatorBounds();
        this.tabsLayout = null;
        this.sourceCoordinateView = null;
        this.rawBackdropProvider = null;
    }

    @Override // android.view.View, android.view.accessibility.AccessibilityEventSource
    public void sendAccessibilityEventUnchecked(AccessibilityEvent accessibilityEvent) {
    }

    public void setPressedState(boolean z) {
        GlassDampedDragController glassDampedDragController;
        if (!this.active || (glassDampedDragController = this.motion) == null) {
            this.externalPressed = false;
        } else if (this.externalPressed == z) {
        } else {
            this.externalPressed = z;
            if (z) {
                glassDampedDragController.press();
            } else {
                glassDampedDragController.release();
            }
        }
    }

    public void setSelectedIndex(int i, boolean z) {
        boolean z2 = this.hasSelectionProgress;
        boolean z3 = false;
        clearSelectionProgressInternal(false);
        this.selectedIndex = clampIndex(i);
        if (this.active) {
            ensureMotion();
            GlassDampedDragController glassDampedDragController = this.motion;
            if (glassDampedDragController == null) {
                return;
            }
            if (!z) {
                glassDampedDragController.snapTo(this.selectedIndex);
            } else if (this.externalPressed) {
                if (this.externalInteractionDragged) {
                    return;
                }
                float f = this.selectedIndex;
                this.externalInteractionPosition = f;
                glassDampedDragController.updateValue(f);
            } else {
                boolean z4 = Math.abs(glassDampedDragController.getTargetValue() - ((float) this.selectedIndex)) >= 0.001f;
                if (z2 && Math.abs(this.motion.getValue() - this.selectedIndex) >= 0.001f) {
                    z3 = true;
                }
                if (z4 || z3) {
                    this.motion.animateToValue(this.selectedIndex);
                }
            }
        }
    }

    public void setSelectionProgress(GlassTabView glassTabView, GlassTabView glassTabView2, float f) {
        if (this.externalPressed) {
            return;
        }
        if (!isConfiguredVisibleTab(glassTabView) || !isConfiguredVisibleTab(glassTabView2)) {
            clearSelectionProgress();
            return;
        }
        this.selectionProgressFrom = glassTabView;
        this.selectionProgressTo = glassTabView2;
        this.selectionProgressFraction = GlassControlUtils.clamp01((Float.isNaN(f) || Float.isInfinite(f)) ? 0.0f : 0.0f);
        this.hasSelectionProgress = true;
        if (this.active) {
            ensureMotion();
            int findVisibleOrdinal = findVisibleOrdinal(glassTabView);
            int findVisibleOrdinal2 = findVisibleOrdinal(glassTabView2);
            if (this.motion == null || findVisibleOrdinal < 0 || findVisibleOrdinal2 < 0) {
                updateIndicatorGeometry();
                invalidateGlassSurfaces();
                return;
            }
            float lerp = GlassControlUtils.lerp(findVisibleOrdinal, findVisibleOrdinal2, this.selectionProgressFraction);
            this.selectedIndex = clampIndex(Math.round(lerp));
            this.motion.updateValue(lerp);
        }
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        if (i == 0) {
            super.setVisibility(this.active ? 0 : 8);
        } else if (this.active) {
            deactivate();
        } else {
            super.setVisibility(8);
        }
    }

    public void updateAppearance(boolean z, int i, int i2, int i3, int i4) {
        this.darkTheme = z;
        this.normalIconColor = i;
        this.normalTextColor = i2;
        this.accentIconColor = i3;
        this.accentTextColor = i4;
        applyStaticAppearance();
        if (this.active) {
            invalidateGlassSurfaces();
        }
    }
}
