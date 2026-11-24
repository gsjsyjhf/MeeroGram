package org.telegram.ui.Components.glass.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Collections;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSource;
import org.telegram.ui.Components.glass.controls.GlassDampedDragController;
import org.telegram.ui.Components.glass.controls.GlassDragGestureDetector;
import org.telegram.ui.Components.glass.controls.GlassIosSettingsTabBar;
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
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public class GlassIosSettingsTabBar extends ViewGroup {
    private int accentColor;
    private GlassBackdropSource baseBackdropSource;
    private final Path containerPath;
    private final ContainerSurface containerSurface;
    private int currentIndex;
    private boolean darkTheme;
    private boolean didDrag;
    private boolean dispatchingSettledSelection;
    private final GlassDragGestureDetector dragDetector;
    private float explicitSourceX;
    private float explicitSourceY;
    private final Paint iconPaint;
    private Drawable[] icons;
    private final IndicatorSurface indicatorSurface;
    private final GlassInteractiveHighlightController interactiveHighlight;
    private final GlassInteractiveHighlightRenderer interactiveHighlightRenderer;
    private float itemWidth;
    private boolean liquidGlassEnabled;
    private GlassDampedDragController motion;
    private final GlassFloatSpring panelDragSpring;
    private GlassBackdropSource requestedBackdropSource;
    private float rowLeft;
    private float rowTop;
    private float rowWidth;
    private int selectedIndex;
    private OnSelectedIndexChangeListener selectedIndexChangeListener;
    private View sourceCoordinateView;
    private final Paint symbolPaint;
    private final ArrayList tabs;
    private final TabsExportSurface tabsExportSurface;
    private final GlassLayerBackdrop tabsLayer;
    private final Paint titlePaint;
    private final ArrayList touchViews;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public final class ContainerSurface extends GlassSurfaceFrameLayout {
        ContainerSurface(Context context) {
            super(context);
            setCapsule(true);
            setClickable(false);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
        public void onDrawGlassContent(Canvas canvas) {
            Canvas canvas2;
            GlassContinuousPathFactory.capsule(GlassIosSettingsTabBar.this.containerPath, getWidth(), getHeight());
            if (GlassIosSettingsTabBar.this.darkTheme) {
                canvas2 = canvas;
                GlassIosSettingsTabBar.this.interactiveHighlightRenderer.draw(canvas2, GlassIosSettingsTabBar.this.containerPath, getWidth(), getHeight(), GlassIosSettingsTabBar.this.interactiveHighlight);
            } else {
                canvas2 = canvas;
            }
            GlassIosSettingsTabBar.this.drawTabs(canvas2, GlassIosSettingsTabBar.this.darkTheme ? -1711276033 : -1728053248, 1.0f);
            if (GlassIosSettingsTabBar.this.liquidGlassEnabled || GlassIosSettingsTabBar.this.tabs.isEmpty()) {
                return;
            }
            GlassIosSettingsTabBar glassIosSettingsTabBar = GlassIosSettingsTabBar.this;
            float physicalItemLeft = glassIosSettingsTabBar.getPhysicalItemLeft(glassIosSettingsTabBar.selectedIndex);
            int save = canvas2.save();
            canvas2.clipRect(physicalItemLeft, 0.0f, GlassIosSettingsTabBar.this.itemWidth + physicalItemLeft, getHeight());
            GlassIosSettingsTabBar glassIosSettingsTabBar2 = GlassIosSettingsTabBar.this;
            glassIosSettingsTabBar2.drawTabs(canvas2, glassIosSettingsTabBar2.accentColor, 1.0f);
            canvas2.restoreToCount(save);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public final class IndicatorSurface extends GlassSurfaceFrameLayout {
        IndicatorSurface(Context context) {
            super(context);
            setCapsule(true);
            setClickable(false);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
        public void onDrawGlassContent(Canvas canvas) {
            float pressProgress = GlassIosSettingsTabBar.this.motion == null ? 0.0f : GlassIosSettingsTabBar.this.motion.getPressProgress();
            if (GlassIosSettingsTabBar.this.liquidGlassEnabled) {
                Paint paint = GlassIosSettingsTabBar.this.iconPaint;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(GlassControlUtils.multiplyAlpha(GlassIosSettingsTabBar.this.darkTheme ? -1 : -16777216, (1.0f - pressProgress) * 0.1f));
                canvas.drawRect(0.0f, 0.0f, getWidth(), getHeight(), paint);
                paint.setColor(GlassControlUtils.multiplyAlpha(-16777216, pressProgress * 0.03f));
                canvas.drawRect(0.0f, 0.0f, getWidth(), getHeight(), paint);
            }
        }

        void updateVisuals(float f) {
            float clamp01 = GlassControlUtils.clamp01(f);
            if (GlassIosSettingsTabBar.this.liquidGlassEnabled) {
                setEffectSpec(GlassEffectSpec.lensOnly(0.0f, GlassIosSettingsTabBar.this.dp(10.0f) * clamp01, GlassIosSettingsTabBar.this.dp(14.0f) * clamp01, true));
                setTintColor(0);
                float f2 = getResources().getDisplayMetrics().density;
                GlassHighlightSpec defaultHighlight = GlassHighlightSpec.defaultHighlight(f2);
                setHighlightSpec(new GlassHighlightSpec(defaultHighlight.width, defaultHighlight.blurRadius, clamp01, defaultHighlight.color, defaultHighlight.angleDegrees, defaultHighlight.falloff, defaultHighlight.style, defaultHighlight.additive));
                GlassShadowSpec defaultShadow = GlassShadowSpec.defaultShadow(f2);
                setOuterShadowSpec(new GlassShadowSpec(defaultShadow.radius, defaultShadow.offsetX, defaultShadow.offsetY, defaultShadow.color, clamp01));
                setInnerShadowSpec(new GlassInnerShadowSpec(GlassIosSettingsTabBar.this.dp(8.0f) * clamp01, 0.0f, GlassIosSettingsTabBar.this.dp(8.0f) * clamp01, 637534208, clamp01));
            } else {
                setEffectSpec(new GlassEffectSpec(0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, false));
                setTintColor(GlassIosSettingsTabBar.this.darkTheme ? 536870911 : 335544320);
                setHighlightSpec(null);
                setOuterShadowSpec(null);
                setInnerShadowSpec(null);
            }
            invalidate();
        }
    }

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public interface OnSelectedIndexChangeListener {
        void onSelectedIndexChanged(GlassIosSettingsTabBar glassIosSettingsTabBar, int i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public final class TabTouchView extends View {
        private final int index;

        TabTouchView(Context context, final int i) {
            super(context);
            this.index = i;
            setClickable(true);
            setFocusable(true);
            setContentDescription(((GlassIosSettingsTab) GlassIosSettingsTabBar.this.tabs.get(i)).title);
            setOnClickListener(new View.OnClickListener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosSettingsTabBar$TabTouchView$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    GlassIosSettingsTabBar.this.selectFromItem(i);
                }
            });
        }

        @Override // android.view.View
        public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
            super.onInitializeAccessibilityEvent(accessibilityEvent);
            accessibilityEvent.setClassName("android.app.ActionBar$Tab");
            accessibilityEvent.setItemCount(GlassIosSettingsTabBar.this.tabs.size());
            accessibilityEvent.setCurrentItemIndex(this.index);
            accessibilityEvent.setChecked(isSelected());
        }

        @Override // android.view.View
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
            accessibilityNodeInfo.setClassName("android.app.ActionBar$Tab");
            accessibilityNodeInfo.setSelected(isSelected());
            accessibilityNodeInfo.setClickable(isEnabled());
            accessibilityNodeInfo.setCollectionItemInfo(AccessibilityNodeInfo.CollectionItemInfo.obtain(0, 1, this.index, 1, false, isSelected()));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public final class TabsExportSurface extends GlassSurfaceFrameLayout {
        private final Path exportPath;

        TabsExportSurface(Context context) {
            super(context);
            this.exportPath = new Path();
            setCapsule(true);
            setClickable(false);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
        public void onDrawGlassContent(Canvas canvas) {
            GlassContinuousPathFactory.capsule(this.exportPath, getWidth(), getHeight());
            GlassIosSettingsTabBar.this.interactiveHighlightRenderer.draw(canvas, this.exportPath, getWidth(), getHeight(), GlassIosSettingsTabBar.this.interactiveHighlight, GlassIosSettingsTabBar.this.getAnimatedItemCenterInRow(), getHeight() * 0.5f);
            GlassIosSettingsTabBar.this.drawTabs(canvas, GlassControlUtils.multiplyAlpha(GlassIosSettingsTabBar.this.accentColor, 0.6f), GlassControlUtils.lerp(1.0f, 1.2f, GlassIosSettingsTabBar.this.motion == null ? 0.0f : GlassIosSettingsTabBar.this.motion.getPressProgress()), getHeight() * 0.5f);
        }

        void updateVisuals(float f) {
            float clamp01 = GlassControlUtils.clamp01(f);
            if (GlassIosSettingsTabBar.this.liquidGlassEnabled) {
                setEffectSpec(new GlassEffectSpec(0.0f, 1.0f, 1.5f, GlassIosSettingsTabBar.this.dp(8.0f), GlassIosSettingsTabBar.this.dp(24.0f) * clamp01, GlassIosSettingsTabBar.this.dp(24.0f) * clamp01, false, false));
                setTintColor(0);
                if (GlassIosSettingsTabBar.this.darkTheme) {
                    GlassHighlightSpec defaultHighlight = GlassHighlightSpec.defaultHighlight(getResources().getDisplayMetrics().density);
                    setHighlightSpec(new GlassHighlightSpec(defaultHighlight.width, defaultHighlight.blurRadius, clamp01, defaultHighlight.color, defaultHighlight.angleDegrees, defaultHighlight.falloff, defaultHighlight.style, defaultHighlight.additive));
                } else {
                    GlassHighlightSpec defaultHighlight2 = GlassHighlightSpec.defaultHighlight(getResources().getDisplayMetrics().density);
                    setHighlightSpec(new GlassHighlightSpec(defaultHighlight2.width, defaultHighlight2.blurRadius, clamp01 * 0.25f, defaultHighlight2.color, defaultHighlight2.angleDegrees, defaultHighlight2.falloff, defaultHighlight2.style, defaultHighlight2.additive));
                }
                setOuterShadowSpec(null);
            } else {
                setEffectSpec(new GlassEffectSpec(0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, false));
                setTintColor(0);
                setHighlightSpec(null);
                setOuterShadowSpec(null);
            }
            invalidate();
        }
    }

    public static /* synthetic */ void $r8$lambda$gowQgOUWss2ge0kcIUtmWq_dV7k(GlassIosSettingsTabBar glassIosSettingsTabBar, GlassInteractiveHighlightController glassInteractiveHighlightController) {
        glassIosSettingsTabBar.containerSurface.invalidate();
        glassIosSettingsTabBar.tabsExportSurface.invalidate();
        glassIosSettingsTabBar.invalidate();
    }

    public GlassIosSettingsTabBar(Context context) {
        this(context, null);
    }

    public GlassIosSettingsTabBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GlassIosSettingsTabBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.tabs = new ArrayList();
        this.touchViews = new ArrayList();
        this.tabsLayer = new GlassLayerBackdrop();
        GlassInteractiveHighlightController glassInteractiveHighlightController = new GlassInteractiveHighlightController();
        this.interactiveHighlight = glassInteractiveHighlightController;
        this.interactiveHighlightRenderer = new GlassInteractiveHighlightRenderer();
        GlassFloatSpring glassFloatSpring = new GlassFloatSpring(0.0f, new GlassSpringSpec(1.0f, 300.0f, 0.5f));
        this.panelDragSpring = glassFloatSpring;
        this.iconPaint = new Paint(1);
        Paint paint = new Paint(1);
        this.titlePaint = paint;
        Paint paint2 = new Paint(1);
        this.symbolPaint = paint2;
        this.containerPath = new Path();
        this.icons = new Drawable[0];
        this.liquidGlassEnabled = true;
        this.accentColor = -16745729;
        setClipChildren(false);
        setClipToPadding(false);
        setFocusable(true);
        Paint.Align align = Paint.Align.CENTER;
        paint.setTextAlign(align);
        Typeface typeface = Typeface.DEFAULT;
        paint.setTypeface(Typeface.create(typeface, 1));
        paint.setTextSize(sp(10.0f));
        paint.setLetterSpacing(0.05f);
        paint2.setTextAlign(align);
        paint2.setTypeface(Typeface.create(typeface, 1));
        ContainerSurface containerSurface = new ContainerSurface(context);
        this.containerSurface = containerSurface;
        TabsExportSurface tabsExportSurface = new TabsExportSurface(context);
        this.tabsExportSurface = tabsExportSurface;
        IndicatorSurface indicatorSurface = new IndicatorSurface(context);
        this.indicatorSurface = indicatorSurface;
        addView(containerSurface);
        addView(tabsExportSurface);
        addView(indicatorSurface);
        glassFloatSpring.setUpdateListener(new GlassFloatSpring.UpdateListener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosSettingsTabBar$$ExternalSyntheticLambda1
            @Override // org.telegram.ui.Components.glass.motion.GlassFloatSpring.UpdateListener
            public final void onSpringUpdate(GlassFloatSpring glassFloatSpring2, float f, float f2) {
                GlassIosSettingsTabBar.this.updateTransforms();
            }
        });
        glassInteractiveHighlightController.setListener(new GlassInteractiveHighlightController.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosSettingsTabBar$$ExternalSyntheticLambda2
            @Override // org.telegram.ui.Components.glass.motion.GlassInteractiveHighlightController.Listener
            public final void onHighlightChanged(GlassInteractiveHighlightController glassInteractiveHighlightController2) {
                GlassIosSettingsTabBar.$r8$lambda$gowQgOUWss2ge0kcIUtmWq_dV7k(GlassIosSettingsTabBar.this, glassInteractiveHighlightController2);
            }
        });
        this.dragDetector = new GlassDragGestureDetector(new GlassDragGestureDetector.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosSettingsTabBar.1
            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDrag(float f, float f2, float f3, float f4) {
                if (f3 != 0.0f) {
                    GlassIosSettingsTabBar.this.didDrag = true;
                }
                GlassIosSettingsTabBar.this.motion.updateValue(GlassControlUtils.clamp(GlassIosSettingsTabBar.this.motion.getTargetValue() + ((f3 / Math.max(1.0f, GlassIosSettingsTabBar.this.itemWidth + GlassIosSettingsTabBar.this.dp(0.0f))) * (GlassControlUtils.isRtl(GlassIosSettingsTabBar.this) ? -1.0f : 1.0f)), 0.0f, GlassIosSettingsTabBar.this.tabs.size() - 1.0f));
                GlassIosSettingsTabBar.this.panelDragSpring.snapTo(GlassIosSettingsTabBar.this.panelDragSpring.getValue() + f3);
                GlassIosSettingsTabBar.this.interactiveHighlight.setRenderPosition(GlassIosSettingsTabBar.this.getAnimatedItemCenterInRow(), GlassIosSettingsTabBar.this.dp(28.0f));
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragCancel() {
                GlassIosSettingsTabBar.this.finishDrag(false);
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragEnd(float f, float f2) {
                GlassIosSettingsTabBar.this.finishDrag(false);
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragStart(float f, float f2) {
                GlassIosSettingsTabBar.this.didDrag = false;
                GlassIosSettingsTabBar.this.motion.press();
                GlassIosSettingsTabBar.this.interactiveHighlight.press(GlassIosSettingsTabBar.this.getAnimatedItemCenterInRow(), GlassIosSettingsTabBar.this.dp(28.0f));
                GlassIosSettingsTabBar.this.indicatorSurface.setPressed(true);
            }
        });
        resolveAppearance();
    }

    private void applyRequestedBackdrop() {
        GlassBackdropSource glassBackdropSource = this.liquidGlassEnabled ? this.baseBackdropSource : null;
        this.containerSurface.setBackdropSource(glassBackdropSource);
        this.tabsExportSurface.setBackdropSource(glassBackdropSource);
        if (this.liquidGlassEnabled) {
            this.indicatorSurface.setBackdropSource(glassBackdropSource == null ? this.tabsLayer : GlassCombinedBackdropSource.of(glassBackdropSource, this.tabsLayer));
        } else {
            this.indicatorSurface.setBackdropSource(null);
        }
        View view = this.sourceCoordinateView;
        if (view != null) {
            this.containerSurface.setSourceCoordinateView(view);
            this.tabsExportSurface.setSourceCoordinateView(this.sourceCoordinateView);
            this.indicatorSurface.setSourceCoordinateView(this.sourceCoordinateView);
            this.tabsLayer.setCoordinateRoot(this.sourceCoordinateView);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float dp(float f) {
        return GlassControlUtils.dp(this, f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void drawTabs(Canvas canvas, int i, float f) {
        drawTabs(canvas, i, f, dp(28.0f));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void drawTabs(Canvas canvas, int i, float f, float f2) {
        float f3;
        float f4;
        float f5;
        float f6 = f2;
        if (this.tabs.isEmpty()) {
            return;
        }
        this.titlePaint.setColor(i);
        this.symbolPaint.setColor(i);
        float dp = dp(18.0f);
        float dp2 = dp(4.0f);
        float sp = sp(10.0f);
        float f7 = 0.5f;
        float f8 = f6 - (((dp + dp2) + sp) * 0.5f);
        float f9 = (dp * 0.5f) + f8;
        Paint.FontMetrics fontMetrics = this.titlePaint.getFontMetrics();
        float f10 = (((f8 + dp) + dp2) + (sp * 0.5f)) - ((fontMetrics.ascent + fontMetrics.descent) * 0.5f);
        int i2 = 0;
        while (i2 < this.tabs.size()) {
            float physicalItemLeft = getPhysicalItemLeft(i2) + (this.itemWidth * f7);
            int save = canvas.save();
            canvas.scale(f, f, physicalItemLeft, f6);
            Drawable drawable = this.icons[i2];
            GlassIosSettingsTab glassIosSettingsTab = (GlassIosSettingsTab) this.tabs.get(i2);
            if (drawable != null) {
                int intrinsicWidth = drawable.getIntrinsicWidth();
                int intrinsicHeight = drawable.getIntrinsicHeight();
                if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
                    f3 = 0.5f;
                    f4 = dp;
                    f5 = f4;
                } else {
                    float f11 = intrinsicWidth;
                    f3 = 0.5f;
                    float f12 = intrinsicHeight;
                    float min = Math.min(dp / f11, dp / f12);
                    f5 = f11 * min;
                    f4 = f12 * min;
                }
                int max = Math.max(1, Math.round(f5));
                int max2 = Math.max(1, Math.round(f4));
                int round = Math.round(physicalItemLeft - (max * f3));
                int round2 = Math.round(f9 - (max2 * f3));
                drawable.setBounds(round, round2, max + round, max2 + round2);
                drawable.setColorFilter(i, PorterDuff.Mode.SRC_IN);
                drawable.draw(canvas);
            } else if (!glassIosSettingsTab.symbol.isEmpty()) {
                this.symbolPaint.setTextSize(sp("i".equals(glassIosSettingsTab.symbol) ? 18.0f : 17.0f));
                Paint.FontMetrics fontMetrics2 = this.symbolPaint.getFontMetrics();
                canvas.drawText(glassIosSettingsTab.symbol, physicalItemLeft, f9 - ((fontMetrics2.ascent + fontMetrics2.descent) * 0.5f), this.symbolPaint);
            }
            canvas.drawText(TextUtils.ellipsize(glassIosSettingsTab.title, new TextPaint(this.titlePaint), Math.max(1.0f, this.itemWidth), TextUtils.TruncateAt.END).toString(), physicalItemLeft, f10, this.titlePaint);
            canvas.restoreToCount(save);
            i2++;
            f6 = f2;
            f7 = 0.5f;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishDrag(boolean z) {
        OnSelectedIndexChangeListener onSelectedIndexChangeListener;
        this.indicatorSurface.setPressed(false);
        this.interactiveHighlight.release();
        this.panelDragSpring.animateTo(0.0f);
        GlassDampedDragController glassDampedDragController = this.motion;
        if (glassDampedDragController == null) {
            return;
        }
        int max = Math.max(0, Math.min(Math.round(glassDampedDragController.getTargetValue()), this.tabs.size() - 1));
        boolean z2 = max != this.currentIndex;
        this.currentIndex = max;
        this.motion.animateToValue(max);
        if (z2 && (onSelectedIndexChangeListener = this.selectedIndexChangeListener) != null) {
            this.dispatchingSettledSelection = true;
            try {
                onSelectedIndexChangeListener.onSelectedIndexChanged(this, max);
            } finally {
                this.dispatchingSettledSelection = false;
            }
        } else if (this.selectedIndexChangeListener == null) {
            this.selectedIndex = max;
            updateAccessibilitySelection();
        }
        if (this.didDrag && this.selectedIndexChangeListener == null) {
            sendAccessibilityEvent(4);
        }
        this.didDrag = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float getAnimatedItemCenterInRow() {
        return getIndicatorPhysicalLeft() + (this.itemWidth * 0.5f) + getPanelOffset();
    }

    private float getIndicatorPhysicalLeft() {
        GlassDampedDragController glassDampedDragController = this.motion;
        return glassDampedDragController == null ? getPhysicalItemLeft(this.selectedIndex) : getPhysicalItemLeft(glassDampedDragController.getValue());
    }

    private float getLogicalItemLeft(float f) {
        return dp(4.0f) + ((this.itemWidth + dp(0.0f)) * f);
    }

    private float getPanelOffset() {
        if (this.rowWidth <= 0.0f) {
            return 0.0f;
        }
        float clamp = GlassControlUtils.clamp(this.panelDragSpring.getValue() / this.rowWidth, -1.0f, 1.0f);
        return dp(4.0f) * Math.signum(clamp) * GlassControlUtils.easeOut(Math.abs(clamp));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float getPhysicalItemLeft(float f) {
        float logicalItemLeft = getLogicalItemLeft(f);
        return GlassControlUtils.isRtl(this) ? (this.rowWidth - logicalItemLeft) - this.itemWidth : logicalItemLeft;
    }

    private void invalidateSurfaces() {
        this.containerSurface.invalidate();
        this.indicatorSurface.invalidate();
        invalidate();
    }

    private void recreateMotion() {
        GlassDampedDragController glassDampedDragController = this.motion;
        if (glassDampedDragController != null) {
            glassDampedDragController.dispose();
        }
        if (this.tabs.isEmpty()) {
            this.motion = null;
            return;
        }
        this.motion = new GlassDampedDragController(this.selectedIndex, 0.0f, Math.max(1.0f, this.tabs.size() - 1.0f), 0.001f, 1.0f, 1.3928572f).trackVelocityOnlyDuringUserUpdates().setListener(new GlassDampedDragController.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosSettingsTabBar$$ExternalSyntheticLambda0
            @Override // org.telegram.ui.Components.glass.controls.GlassDampedDragController.Listener
            public final void onAnimationChanged(GlassDampedDragController glassDampedDragController2) {
                GlassIosSettingsTabBar.this.updateTransforms();
            }
        });
        if (this.tabs.size() == 1) {
            this.motion.snapTo(0.0f);
        }
    }

    private void resolveAppearance() {
        float f = getResources().getDisplayMetrics().density;
        if (this.liquidGlassEnabled) {
            this.containerSurface.setEffectSpec(GlassEffectSpec.liquid(dp(2.0f), dp(24.0f), dp(24.0f)));
            this.containerSurface.setTintColor(this.darkTheme ? -1728053248 : -1711276033);
            this.containerSurface.setHighlightSpec(this.darkTheme ? GlassHighlightSpec.defaultHighlight(f) : null);
            this.containerSurface.setOuterShadowSpec(GlassShadowSpec.defaultShadow(f));
        } else {
            this.containerSurface.setEffectSpec(new GlassEffectSpec(0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, false));
            this.containerSurface.setTintColor(this.darkTheme ? -14935010 : -855305);
            this.containerSurface.setHighlightSpec(null);
            this.containerSurface.setOuterShadowSpec(null);
        }
        IndicatorSurface indicatorSurface = this.indicatorSurface;
        GlassDampedDragController glassDampedDragController = this.motion;
        indicatorSurface.updateVisuals(glassDampedDragController == null ? 0.0f : glassDampedDragController.getPressProgress());
        TabsExportSurface tabsExportSurface = this.tabsExportSurface;
        GlassDampedDragController glassDampedDragController2 = this.motion;
        tabsExportSurface.updateVisuals(glassDampedDragController2 != null ? glassDampedDragController2.getPressProgress() : 0.0f);
        invalidateSurfaces();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void selectFromItem(int i) {
        if (isEnabled()) {
            OnSelectedIndexChangeListener onSelectedIndexChangeListener = this.selectedIndexChangeListener;
            if (onSelectedIndexChangeListener != null) {
                onSelectedIndexChangeListener.onSelectedIndexChanged(this, i);
            } else {
                setSelectedIndex(i, true);
            }
        }
    }

    private float sp(float f) {
        return f * getResources().getDisplayMetrics().scaledDensity;
    }

    private void updateAccessibilitySelection() {
        int i = 0;
        while (i < this.touchViews.size()) {
            ((TabTouchView) this.touchViews.get(i)).setSelected(i == this.selectedIndex);
            i++;
        }
    }

    private void updateExplicitSourceOffsets() {
        float dp = this.rowTop + dp(0.0f);
        this.containerSurface.setSourceOffset(this.explicitSourceX + this.rowLeft + getPanelOffset(), this.explicitSourceY + this.rowTop);
        this.tabsExportSurface.setSourceOffset(this.explicitSourceX + this.rowLeft + getPanelOffset(), this.explicitSourceY + dp);
        this.tabsLayer.setOriginInSource(this.explicitSourceX + this.rowLeft + getPanelOffset(), this.explicitSourceY + dp);
        this.indicatorSurface.setSourceOffset(this.explicitSourceX + this.rowLeft + getIndicatorPhysicalLeft() + getPanelOffset() + (this.indicatorSurface.getPivotX() * (1.0f - this.indicatorSurface.getScaleX())), this.explicitSourceY + dp + (this.indicatorSurface.getPivotY() * (1.0f - this.indicatorSurface.getScaleY())));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTransforms() {
        if (this.motion == null || this.containerSurface.getMeasuredWidth() == 0) {
            return;
        }
        float pressProgress = this.motion.getPressProgress();
        float panelOffset = getPanelOffset();
        ContainerSurface containerSurface = this.containerSurface;
        containerSurface.setPivotX(containerSurface.getWidth() * 0.5f);
        ContainerSurface containerSurface2 = this.containerSurface;
        containerSurface2.setPivotY(containerSurface2.getHeight() * 0.5f);
        float dp = this.liquidGlassEnabled ? ((dp(16.0f) / Math.max(1.0f, this.containerSurface.getWidth())) * pressProgress) + 1.0f : 1.0f;
        this.containerSurface.setScaleX(dp);
        this.containerSurface.setScaleY(dp);
        this.containerSurface.setTranslationX(panelOffset);
        this.tabsExportSurface.setTranslationX(panelOffset);
        IndicatorSurface indicatorSurface = this.indicatorSurface;
        indicatorSurface.setPivotX(indicatorSurface.getWidth() * 0.5f);
        IndicatorSurface indicatorSurface2 = this.indicatorSurface;
        indicatorSurface2.setPivotY(indicatorSurface2.getHeight() * 0.5f);
        if (this.liquidGlassEnabled) {
            float velocity = this.motion.getVelocity() / 10.0f;
            float clamp = GlassControlUtils.clamp(0.75f * velocity, -0.2f, 0.2f);
            float clamp2 = GlassControlUtils.clamp(velocity * 0.25f, -0.2f, 0.2f);
            this.indicatorSurface.setScaleX(this.motion.getScaleX() / (1.0f - clamp));
            this.indicatorSurface.setScaleY(((((this.motion.getScaleY() - 1.0f) * 0.5357143f) / 0.3928572f) + 0.85714287f) * (1.0f - clamp2));
        } else {
            this.indicatorSurface.setScaleX(1.0f);
            this.indicatorSurface.setScaleY(0.85714287f);
        }
        this.indicatorSurface.setTranslationX(this.rowLeft + getIndicatorPhysicalLeft() + panelOffset);
        this.indicatorSurface.updateVisuals(pressProgress);
        this.tabsExportSurface.updateVisuals(pressProgress);
        if (pressProgress > 0.0f) {
            this.interactiveHighlight.setRenderPosition(getAnimatedItemCenterInRow(), dp(28.0f));
        }
        this.containerSurface.invalidate();
        this.indicatorSurface.invalidate();
        invalidate();
        if (this.sourceCoordinateView == null) {
            updateExplicitSourceOffsets();
        }
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        TabsExportSurface tabsExportSurface = this.tabsExportSurface;
        if (view == tabsExportSurface) {
            if (!this.liquidGlassEnabled || tabsExportSurface.getWidth() <= 0 || this.tabsExportSurface.getHeight() <= 0) {
                return true;
            }
            GlassLayerBackdrop glassLayerBackdrop = this.tabsLayer;
            TabsExportSurface tabsExportSurface2 = this.tabsExportSurface;
            int width = tabsExportSurface2.getWidth();
            int height = this.tabsExportSurface.getHeight();
            final TabsExportSurface tabsExportSurface3 = this.tabsExportSurface;
            Objects.requireNonNull(tabsExportSurface3);
            glassLayerBackdrop.record(tabsExportSurface2, width, height, new GlassLayerBackdrop.Recorder() { // from class: org.telegram.ui.Components.glass.controls.GlassIosSettingsTabBar$$ExternalSyntheticLambda3
                @Override // org.telegram.ui.Components.glass.engine.GlassLayerBackdrop.Recorder
                public final void draw(Canvas canvas2) {
                    TabsExportSurface.this.draw(canvas2);
                }
            });
            return true;
        }
        return super.drawChild(canvas, view, j);
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public List<GlassIosSettingsTab> getTabs() {
        return Collections.unmodifiableList(this.tabs);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setCollectionInfo(AccessibilityNodeInfo.CollectionInfo.obtain(1, this.tabs.size(), false, 1));
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled() || this.tabs.isEmpty()) {
            return false;
        }
        if (motionEvent.getActionMasked() == 0) {
            float indicatorPhysicalLeft = getIndicatorPhysicalLeft() + getPanelOffset();
            float dp = this.rowTop + dp(4.0f);
            float f = indicatorPhysicalLeft + this.rowLeft;
            return motionEvent.getX() >= f && motionEvent.getX() <= f + this.itemWidth && motionEvent.getY() >= dp && motionEvent.getY() <= dp + dp(48.0f);
        }
        return this.dragDetector.isDragging();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.rowLeft = getPaddingLeft() + Math.max(0.0f, (((getWidth() - getPaddingLeft()) - getPaddingRight()) - this.rowWidth) * 0.5f);
        this.rowTop = getPaddingTop() + Math.max(0.0f, (((getHeight() - getPaddingTop()) - getPaddingBottom()) - dp(56.0f)) * 0.5f);
        int round = Math.round(this.rowLeft);
        int round2 = Math.round(this.rowTop);
        int round3 = Math.round(this.rowTop + dp(0.0f));
        ContainerSurface containerSurface = this.containerSurface;
        containerSurface.layout(round, round2, containerSurface.getMeasuredWidth() + round, this.containerSurface.getMeasuredHeight() + round2);
        TabsExportSurface tabsExportSurface = this.tabsExportSurface;
        tabsExportSurface.layout(round, round3, tabsExportSurface.getMeasuredWidth() + round, this.tabsExportSurface.getMeasuredHeight() + round3);
        IndicatorSurface indicatorSurface = this.indicatorSurface;
        indicatorSurface.layout(0, round3, indicatorSurface.getMeasuredWidth(), this.indicatorSurface.getMeasuredHeight() + round3);
        for (int i5 = 0; i5 < this.touchViews.size(); i5++) {
            int round4 = Math.round(this.rowLeft + getPhysicalItemLeft(i5));
            int round5 = Math.round(this.rowTop + dp(4.0f));
            ((TabTouchView) this.touchViews.get(i5)).layout(round4, round5, Math.round(this.itemWidth) + round4, Math.round(dp(48.0f)) + round5);
        }
        updateTransforms();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int resolveSize;
        int max = Math.max(1, this.tabs.size());
        float f = max;
        float f2 = max - 1;
        setMeasuredDimension(View.resolveSize(Math.round(dp((f * 90.0f) + 8.0f + (f2 * 0.0f))) + getPaddingLeft() + getPaddingRight(), i), View.resolveSize(Math.round(dp(72.0f)) + getPaddingTop() + getPaddingBottom(), i2));
        float max2 = Math.max(1.0f, (resolveSize - getPaddingLeft()) - getPaddingRight());
        float dp = dp(4.0f) * 2.0f;
        float dp2 = dp(0.0f) * f2;
        float min = Math.min(dp(90.0f), Math.max(1.0f, ((max2 - dp) - dp2) / f));
        this.itemWidth = min;
        float max3 = Math.max(1.0f, dp + (min * f) + dp2);
        this.rowWidth = max3;
        this.containerSurface.measure(View.MeasureSpec.makeMeasureSpec(Math.round(max3), 1073741824), View.MeasureSpec.makeMeasureSpec(Math.round(dp(56.0f)), 1073741824));
        this.tabsExportSurface.measure(View.MeasureSpec.makeMeasureSpec(Math.round(this.rowWidth), 1073741824), View.MeasureSpec.makeMeasureSpec(Math.round(dp(56.0f)), 1073741824));
        this.indicatorSurface.measure(View.MeasureSpec.makeMeasureSpec(Math.round(this.itemWidth), 1073741824), View.MeasureSpec.makeMeasureSpec(Math.round(dp(56.0f)), 1073741824));
        ArrayList arrayList = this.touchViews;
        int size = arrayList.size();
        int i3 = 0;
        while (i3 < size) {
            Object obj = arrayList.get(i3);
            i3++;
            ((TabTouchView) obj).measure(View.MeasureSpec.makeMeasureSpec(Math.round(this.itemWidth), 1073741824), View.MeasureSpec.makeMeasureSpec(Math.round(dp(48.0f)), 1073741824));
        }
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        requestLayout();
        updateTransforms();
        invalidateSurfaces();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled() || this.tabs.isEmpty()) {
            return false;
        }
        motionEvent.getActionMasked();
        return this.dragDetector.onTouchEvent(motionEvent);
    }

    public void setAccentColor(int i) {
        this.accentColor = i;
        invalidateSurfaces();
    }

    public void setBackdropSource(GlassBackdropSource glassBackdropSource) {
        this.requestedBackdropSource = glassBackdropSource;
        this.baseBackdropSource = glassBackdropSource;
        applyRequestedBackdrop();
    }

    public void setDarkTheme(boolean z) {
        if (this.darkTheme == z) {
            return;
        }
        this.darkTheme = z;
        int i = this.accentColor;
        if (i == -16745729 || i == -16087809) {
            this.accentColor = z ? -16087809 : -16745729;
        }
        resolveAppearance();
    }

    @Override // android.view.View
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.containerSurface.setEnabled(z);
        this.tabsExportSurface.setEnabled(z);
        this.indicatorSurface.setEnabled(z);
        ArrayList arrayList = this.touchViews;
        int size = arrayList.size();
        int i = 0;
        while (i < size) {
            Object obj = arrayList.get(i);
            i++;
            ((TabTouchView) obj).setEnabled(z);
        }
        if (!z && this.motion != null) {
            this.dragDetector.cancel();
            this.motion.release();
        }
        invalidateSurfaces();
    }

    public void setLiquidGlassEnabled(boolean z) {
        if (this.liquidGlassEnabled == z) {
            return;
        }
        this.liquidGlassEnabled = z;
        applyRequestedBackdrop();
        resolveAppearance();
    }

    public void setOnSelectedIndexChangeListener(OnSelectedIndexChangeListener onSelectedIndexChangeListener) {
        this.selectedIndexChangeListener = onSelectedIndexChangeListener;
    }

    public void setSelectedIndex(int i) {
        setSelectedIndex(i, true);
    }

    public void setSelectedIndex(int i, boolean z) {
        boolean z2 = false;
        if (this.tabs.isEmpty()) {
            this.selectedIndex = 0;
            this.currentIndex = 0;
            return;
        }
        int max = Math.max(0, Math.min(i, this.tabs.size() - 1));
        this.selectedIndex = max;
        this.currentIndex = max;
        GlassDampedDragController glassDampedDragController = this.motion;
        if (glassDampedDragController != null) {
            if (this.dispatchingSettledSelection && Math.abs(glassDampedDragController.getTargetValue() - max) < 0.001f) {
                z2 = true;
            }
            if (!z) {
                this.motion.snapTo(max);
            } else if (!z2) {
                this.motion.animateToValue(max);
            }
        }
        updateAccessibilitySelection();
        updateTransforms();
    }

    public void setSourceCoordinateView(View view) {
        this.sourceCoordinateView = view;
        this.containerSurface.setSourceCoordinateView(view);
        this.tabsExportSurface.setSourceCoordinateView(view);
        this.indicatorSurface.setSourceCoordinateView(view);
        this.tabsLayer.setCoordinateRoot(view);
        if (view == null) {
            updateExplicitSourceOffsets();
        }
    }

    public void setTabs(List<GlassIosSettingsTab> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("GlassIosSettingsTabBar requires at least one tab");
        }
        ArrayList arrayList = this.touchViews;
        int size = arrayList.size();
        int i = 0;
        while (i < size) {
            Object obj = arrayList.get(i);
            i++;
            removeView((TabTouchView) obj);
        }
        this.tabs.clear();
        this.tabs.addAll(list);
        this.touchViews.clear();
        this.icons = new Drawable[this.tabs.size()];
        for (int i2 = 0; i2 < this.tabs.size(); i2++) {
            GlassIosSettingsTab glassIosSettingsTab = (GlassIosSettingsTab) this.tabs.get(i2);
            if (glassIosSettingsTab.iconResId != 0) {
                this.icons[i2] = getResources().getDrawable(glassIosSettingsTab.iconResId).mutate();
            }
            TabTouchView tabTouchView = new TabTouchView(getContext(), i2);
            this.touchViews.add(tabTouchView);
            addView(tabTouchView);
        }
        int min = Math.min(this.selectedIndex, this.tabs.size() - 1);
        this.selectedIndex = min;
        this.currentIndex = min;
        recreateMotion();
        requestLayout();
        updateAccessibilitySelection();
    }

    public void setTelegramBackdropSource(BlurredBackgroundSource blurredBackgroundSource) {
        if (blurredBackgroundSource == null) {
            setBackdropSource(null);
        } else {
            setBackdropSource(new TelegramBlurredBackgroundSourceAdapter(blurredBackgroundSource));
        }
    }
}
