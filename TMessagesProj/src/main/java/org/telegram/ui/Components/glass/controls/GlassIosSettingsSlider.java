package org.telegram.ui.Components.glass.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;
import java.util.Arrays;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSource;
import org.telegram.ui.Components.glass.controls.GlassDampedDragController;
import org.telegram.ui.Components.glass.controls.GlassDragGestureDetector;
import org.telegram.ui.Components.glass.engine.GlassBackdropSource;
import org.telegram.ui.Components.glass.engine.GlassCombinedBackdropSource;
import org.telegram.ui.Components.glass.engine.GlassEffectSpec;
import org.telegram.ui.Components.glass.engine.GlassHighlightSpec;
import org.telegram.ui.Components.glass.engine.GlassInnerShadowSpec;
import org.telegram.ui.Components.glass.engine.GlassLayerBackdrop;
import org.telegram.ui.Components.glass.engine.GlassScaledBackdropSource;
import org.telegram.ui.Components.glass.engine.GlassShadowSpec;
import org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout;
import org.telegram.ui.Components.glass.engine.TelegramBlurredBackgroundSourceAdapter;
import org.telegram.ui.Components.glass.shape.GlassContinuousPathFactory;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public class GlassIosSettingsSlider extends ViewGroup {
    private GlassBackdropSource baseBackdropSource;
    private boolean darkTheme;
    private float[] detentFractions;
    private boolean detentHapticsEnabled;
    private boolean didDrag;
    private final GlassDragGestureDetector dragDetector;
    private float explicitSourceX;
    private float explicitSourceY;
    private final Paint indicatorPaint;
    private int lastDetentIndex;
    private GlassDampedDragController motion;
    private float previousSnappedValue;
    private float rangeEnd;
    private float rangeStart;
    private final GlassScaledBackdropSource scaledTrack;
    private boolean showDetentIndicators;
    private boolean showStepIndicators;
    private View sourceCoordinateView;
    private int steps;
    private boolean tapCandidate;
    private float tapDownX;
    private float tapDownY;
    private final ThumbSurface thumb;
    private final int touchSlop;
    private final RectF trackBounds;
    private float trackCenterY;
    private final GlassLayerBackdrop trackLayer;
    private float trackLeft;
    private final Paint trackPaint;
    private final Path trackPath;
    private float trackWidth;
    private float value;
    private OnValueChangeListener valueChangeListener;
    private float virtualValue;
    private float visibilityThreshold;

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public interface OnValueChangeListener {
        void onValueChangeFinished(GlassIosSettingsSlider glassIosSettingsSlider, float f);

        void onValueChanged(GlassIosSettingsSlider glassIosSettingsSlider, float f, boolean z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public final class ThumbSurface extends GlassSurfaceFrameLayout {
        ThumbSurface(Context context) {
            super(context);
            setCapsule(true);
            setClickable(false);
        }

        void updateVisuals(float f, boolean z) {
            float clamp01 = z ? GlassControlUtils.clamp01(f) : 0.0f;
            float f2 = 1.0f - clamp01;
            setEffectSpec(GlassEffectSpec.lensOnly(GlassIosSettingsSlider.this.dp(8.0f) * f2, GlassIosSettingsSlider.this.dp(10.0f) * clamp01, GlassIosSettingsSlider.this.dp(14.0f) * clamp01, true));
            setTintColor(GlassControlUtils.multiplyAlpha(-1, f2));
            setHighlightSpec(GlassHighlightSpec.ambient(getResources().getDisplayMetrics().density, clamp01));
            setOuterShadowSpec(new GlassShadowSpec(GlassIosSettingsSlider.this.dp(6.0f), 0.0f, GlassIosSettingsSlider.this.dp(1.0f), z ? 637534208 : 218103808, 1.0f));
            setInnerShadowSpec(new GlassInnerShadowSpec(GlassIosSettingsSlider.this.dp(4.0f) * clamp01, 0.0f, GlassIosSettingsSlider.this.dp(4.0f) * clamp01, 637534208, clamp01));
        }
    }

    /* renamed from: $r8$lambda$_l8YiofxygJC1-ljUJypWh-f-S8  reason: not valid java name */
    public static /* synthetic */ void m1065$r8$lambda$_l8YiofxygJC1ljUJypWhfS8(GlassIosSettingsSlider glassIosSettingsSlider, GlassDampedDragController glassDampedDragController) {
        glassIosSettingsSlider.updateThumbState();
        glassIosSettingsSlider.invalidate();
    }

    public GlassIosSettingsSlider(Context context) {
        this(context, null);
    }

    public GlassIosSettingsSlider(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GlassIosSettingsSlider(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.trackPaint = new Paint(1);
        this.indicatorPaint = new Paint(1);
        this.trackPath = new Path();
        this.trackBounds = new RectF();
        GlassLayerBackdrop glassLayerBackdrop = new GlassLayerBackdrop();
        this.trackLayer = glassLayerBackdrop;
        this.scaledTrack = new GlassScaledBackdropSource(glassLayerBackdrop);
        this.rangeEnd = 1.0f;
        this.visibilityThreshold = 0.01f;
        this.detentFractions = new float[0];
        this.lastDetentIndex = -1;
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        setFocusable(true);
        setClickable(true);
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        ThumbSurface thumbSurface = new ThumbSurface(context);
        this.thumb = thumbSurface;
        addView(thumbSurface, new ViewGroup.LayoutParams(Math.round(dp(40.0f)), Math.round(dp(24.0f))));
        recreateMotion(false);
        this.dragDetector = new GlassDragGestureDetector(new GlassDragGestureDetector.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosSettingsSlider.1
            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDrag(float f, float f2, float f3, float f4) {
                if (GlassIosSettingsSlider.this.isEnabled()) {
                    if (f3 != 0.0f) {
                        GlassIosSettingsSlider.this.didDrag = true;
                    }
                    float rangeLength = GlassIosSettingsSlider.this.rangeLength() * (f3 / Math.max(1.0f, GlassIosSettingsSlider.this.trackWidth)) * (GlassControlUtils.isRtl(GlassIosSettingsSlider.this) ? -1.0f : 1.0f);
                    float f5 = GlassIosSettingsSlider.this.virtualValue;
                    GlassIosSettingsSlider glassIosSettingsSlider = GlassIosSettingsSlider.this;
                    glassIosSettingsSlider.virtualValue = glassIosSettingsSlider.clampToRange(glassIosSettingsSlider.virtualValue + rangeLength);
                    GlassIosSettingsSlider glassIosSettingsSlider2 = GlassIosSettingsSlider.this;
                    glassIosSettingsSlider2.performDetentHaptic(f5, glassIosSettingsSlider2.virtualValue);
                    GlassIosSettingsSlider glassIosSettingsSlider3 = GlassIosSettingsSlider.this;
                    float snapValue = glassIosSettingsSlider3.snapValue(glassIosSettingsSlider3.virtualValue);
                    if (Math.abs(GlassIosSettingsSlider.this.virtualValue - snapValue) >= (GlassIosSettingsSlider.this.rangeLength() / (GlassIosSettingsSlider.this.steps + 1.0f)) * 0.35f) {
                        snapValue = GlassIosSettingsSlider.this.virtualValue;
                    } else if (GlassIosSettingsSlider.this.steps > 0 && GlassIosSettingsSlider.this.previousSnappedValue != snapValue) {
                        GlassIosSettingsSlider.this.previousSnappedValue = snapValue;
                        GlassControlUtils.performSegmentTick(GlassIosSettingsSlider.this);
                    }
                    GlassIosSettingsSlider.this.motion.updateValue(snapValue);
                    GlassIosSettingsSlider.this.dispatchValue(snapValue, true);
                }
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragCancel() {
                GlassIosSettingsSlider.this.finishThumbGesture();
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragEnd(float f, float f2) {
                GlassIosSettingsSlider.this.finishThumbGesture();
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragStart(float f, float f2) {
                GlassIosSettingsSlider.this.didDrag = false;
                GlassIosSettingsSlider glassIosSettingsSlider = GlassIosSettingsSlider.this;
                glassIosSettingsSlider.virtualValue = glassIosSettingsSlider.motion.getTargetValue();
                GlassIosSettingsSlider.this.lastDetentIndex = -1;
                GlassIosSettingsSlider.this.motion.press();
                GlassIosSettingsSlider.this.thumb.setPressed(true);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float clampToRange(float f) {
        return GlassControlUtils.clamp(f, this.rangeStart, this.rangeEnd);
    }

    private void dispatchFinished(float f) {
        OnValueChangeListener onValueChangeListener = this.valueChangeListener;
        if (onValueChangeListener != null) {
            onValueChangeListener.onValueChangeFinished(this, f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchValue(float f, boolean z) {
        float clampToRange = clampToRange(f);
        this.value = clampToRange;
        OnValueChangeListener onValueChangeListener = this.valueChangeListener;
        if (onValueChangeListener != null) {
            onValueChangeListener.onValueChanged(this, clampToRange, z);
        }
        sendAccessibilityEvent(2048);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float dp(float f) {
        return GlassControlUtils.dp(this, f);
    }

    private void drawIndicator(Canvas canvas, float f, float f2, float f3) {
        int round;
        boolean z = Math.abs(f - 0.5f) < 1.0E-4f;
        if (this.darkTheme) {
            round = Math.round((z ? 0.38f : 0.26f) * 255.0f);
        } else {
            round = Math.round((z ? 0.3f : 0.2f) * 255.0f);
        }
        this.indicatorPaint.setColor(this.darkTheme ? Color.argb(round, 255, 255, 255) : Color.argb(round, 0, 0, 0));
        if (GlassControlUtils.isRtl(this)) {
            f = 1.0f - f;
        }
        canvas.drawCircle(this.trackLeft + f2 + (f3 * f), this.trackCenterY, dp(z ? 1.5f : 1.25f), this.indicatorPaint);
    }

    private void drawIndicators(Canvas canvas) {
        float[] fArr;
        int i;
        int i2;
        if ((!this.showStepIndicators || this.steps < 0) && (!this.showDetentIndicators || this.detentFractions.length == 0)) {
            return;
        }
        float dp = dp(1.5f);
        float max = Math.max(0.0f, this.trackWidth - (2.0f * dp));
        if (this.showStepIndicators) {
            int i3 = 0;
            while (true) {
                if (i3 >= this.steps + 2) {
                    break;
                }
                drawIndicator(canvas, i3 / (i2 + 1), dp, max);
                i3++;
            }
        }
        if (this.showDetentIndicators) {
            for (float f : this.detentFractions) {
                if (this.showStepIndicators) {
                    int i4 = 0;
                    while (true) {
                        if (i4 >= this.steps + 2) {
                            break;
                        } else if (Math.abs(f - (i4 / (i + 1))) < 1.0E-4f) {
                            break;
                        } else {
                            i4++;
                        }
                    }
                }
                drawIndicator(canvas, f, dp, max);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void drawTrackBars(Canvas canvas) {
        float f = isEnabled() ? 1.0f : 0.5f;
        float dp = dp(6.0f);
        RectF rectF = this.trackBounds;
        float f2 = this.trackLeft;
        float f3 = this.trackCenterY;
        float f4 = dp * 0.5f;
        rectF.set(f2, f3 - f4, this.trackWidth + f2, f3 + f4);
        int i = this.darkTheme ? 2136233048 : 507264067;
        this.trackPaint.setStyle(Paint.Style.FILL);
        this.trackPaint.setColor(GlassControlUtils.multiplyAlpha(i, f));
        GlassContinuousPathFactory.capsule(this.trackPath, this.trackBounds);
        canvas.drawPath(this.trackPath, this.trackPaint);
        float progress = this.trackWidth * this.motion.getProgress();
        if (progress > 0.0f) {
            this.trackPaint.setColor(GlassControlUtils.multiplyAlpha(this.darkTheme ? -16739841 : -16742145, f));
            if (GlassControlUtils.isRtl(this)) {
                RectF rectF2 = this.trackBounds;
                float f5 = this.trackLeft + this.trackWidth;
                rectF2.left = f5 - progress;
                rectF2.right = f5;
            } else {
                RectF rectF3 = this.trackBounds;
                float f6 = this.trackLeft;
                rectF3.left = f6;
                rectF3.right = f6 + progress;
            }
            GlassContinuousPathFactory.capsule(this.trackPath, this.trackBounds);
            canvas.drawPath(this.trackPath, this.trackPaint);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishThumbGesture() {
        this.thumb.setPressed(false);
        if (isEnabled() && this.didDrag) {
            float snapValue = snapValue(this.motion.getTargetValue());
            this.motion.animateToValue(snapValue);
            setInternalValue(snapValue);
            dispatchValue(snapValue, true);
            dispatchFinished(snapValue);
        } else {
            this.motion.release();
        }
        this.didDrag = false;
    }

    private float getThumbPhysicalX() {
        float progress = (-dp(20.0f)) + (this.trackWidth * this.motion.getProgress());
        if (progress >= (-dp(20.0f)) && progress <= dp(10.0f)) {
            progress = (progress + dp(20.0f)) / 3.0f;
        } else if (progress >= this.trackWidth - dp(50.0f) && progress <= this.trackWidth - dp(20.0f)) {
            progress = ((progress - (this.trackWidth - dp(50.0f))) / 3.0f) + (this.trackWidth - dp(50.0f));
        }
        if (GlassControlUtils.isRtl(this)) {
            progress = (this.trackWidth - dp(40.0f)) - progress;
        }
        return this.trackLeft + progress;
    }

    private boolean isInsideThumb(float f, float f2) {
        float thumbPhysicalX = getThumbPhysicalX();
        float dp = this.trackCenterY - dp(12.0f);
        return f >= thumbPhysicalX && f <= thumbPhysicalX + dp(40.0f) && f2 >= dp && f2 <= dp + dp(24.0f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void performDetentHaptic(float f, float f2) {
        if (!this.detentHapticsEnabled || this.detentFractions.length == 0) {
            return;
        }
        float max = Math.max(Math.abs(rangeLength()) * 0.0125f, 1.0E-4f);
        boolean z = f2 >= f;
        int i = 0;
        int i2 = -1;
        while (true) {
            if (i >= this.detentFractions.length) {
                i = i2;
                break;
            }
            float rangeLength = this.rangeStart + (rangeLength() * this.detentFractions[i]);
            boolean z2 = !z ? f <= rangeLength || f2 > rangeLength : f >= rangeLength || f2 < rangeLength;
            boolean z3 = Math.abs(f2 - rangeLength) <= max && Math.abs(f - rangeLength) > max;
            if (z2 || z3) {
                if (!z) {
                    break;
                }
                i2 = i;
            }
            i++;
        }
        if (i >= 0 && i != this.lastDetentIndex) {
            this.lastDetentIndex = i;
            GlassControlUtils.performSegmentTick(this);
        } else if (this.lastDetentIndex < 0 || Math.abs(f2 - (this.rangeStart + (rangeLength() * this.detentFractions[this.lastDetentIndex]))) <= max * 2.0f) {
        } else {
            this.lastDetentIndex = -1;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float rangeLength() {
        return this.rangeEnd - this.rangeStart;
    }

    private void rebuildThumbBackdrop() {
        ThumbSurface thumbSurface = this.thumb;
        GlassBackdropSource glassBackdropSource = this.baseBackdropSource;
        thumbSurface.setBackdropSource(glassBackdropSource == null ? this.scaledTrack : GlassCombinedBackdropSource.of(glassBackdropSource, this.scaledTrack));
    }

    private void recreateMotion(boolean z) {
        GlassDampedDragController glassDampedDragController = this.motion;
        if (glassDampedDragController != null) {
            glassDampedDragController.dispose();
        }
        this.motion = new GlassDampedDragController(clampToRange(this.value), this.rangeStart, this.rangeEnd, this.visibilityThreshold, 1.0f, 1.5f).setListener(new GlassDampedDragController.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosSettingsSlider$$ExternalSyntheticLambda1
            @Override // org.telegram.ui.Components.glass.controls.GlassDampedDragController.Listener
            public final void onAnimationChanged(GlassDampedDragController glassDampedDragController2) {
                GlassIosSettingsSlider.m1065$r8$lambda$_l8YiofxygJC1ljUJypWhfS8(GlassIosSettingsSlider.this, glassDampedDragController2);
            }
        });
        this.previousSnappedValue = snapValue(this.value);
        float f = this.value;
        this.virtualValue = f;
        if (z) {
            this.motion.animateToValue(f);
        }
    }

    private void setInternalValue(float f) {
        float clampToRange = clampToRange(f);
        this.value = clampToRange;
        this.virtualValue = clampToRange;
    }

    private void setValueFromAccessibility(float f) {
        float snapValue = snapValue(clampToRange(f));
        this.motion.animateToValue(snapValue);
        setInternalValue(snapValue);
        dispatchValue(snapValue, true);
        dispatchFinished(snapValue);
        sendAccessibilityEvent(4);
    }

    private void setValueFromTap(float f) {
        float clamp01 = GlassControlUtils.clamp01((f - this.trackLeft) / Math.max(1.0f, this.trackWidth));
        float snapValue = snapValue(clampToRange(GlassControlUtils.isRtl(this) ? this.rangeEnd - (rangeLength() * clamp01) : this.rangeStart + (rangeLength() * clamp01)));
        performDetentHaptic(this.value, snapValue);
        this.motion.animateToValue(snapValue);
        setInternalValue(snapValue);
        dispatchValue(snapValue, true);
        dispatchFinished(snapValue);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float snapValue(float f) {
        if (this.steps <= 0) {
            return clampToRange(f);
        }
        float rangeLength = rangeLength() / (this.steps + 1.0f);
        return clampToRange(this.rangeStart + (Math.round((f - this.rangeStart) / rangeLength) * rangeLength));
    }

    private void updateExplicitSourceOffset() {
        this.trackLayer.setOriginInSource(this.explicitSourceX, this.explicitSourceY);
        this.thumb.setSourceOffset(this.explicitSourceX + getThumbPhysicalX(), (this.explicitSourceY + this.trackCenterY) - dp(12.0f));
    }

    private void updateThumbState() {
        ThumbSurface thumbSurface = this.thumb;
        if (thumbSurface == null || this.motion == null || thumbSurface.getMeasuredWidth() == 0) {
            return;
        }
        float pressProgress = isEnabled() ? this.motion.getPressProgress() : 0.0f;
        float velocity = isEnabled() ? this.motion.getVelocity() / 10.0f : 0.0f;
        float clamp = GlassControlUtils.clamp(0.75f * velocity, -0.2f, 0.2f);
        float clamp2 = GlassControlUtils.clamp(velocity * 0.25f, -0.2f, 0.2f);
        ThumbSurface thumbSurface2 = this.thumb;
        thumbSurface2.setPivotX(thumbSurface2.getWidth() * 0.5f);
        ThumbSurface thumbSurface3 = this.thumb;
        thumbSurface3.setPivotY(thumbSurface3.getHeight() * 0.5f);
        this.thumb.setScaleX((isEnabled() ? this.motion.getScaleX() : 1.0f) / (1.0f - clamp));
        this.thumb.setScaleY((isEnabled() ? this.motion.getScaleY() : 1.0f) * (1.0f - clamp2));
        this.thumb.setTranslationX(getThumbPhysicalX());
        this.scaledTrack.setScale(GlassControlUtils.lerp(0.6666667f, 1.0f, pressProgress), GlassControlUtils.lerp(0.0f, 1.0f, pressProgress));
        this.thumb.updateVisuals(pressProgress, isEnabled());
        if (this.sourceCoordinateView == null) {
            updateExplicitSourceOffset();
        }
    }

    public int getSteps() {
        return this.steps;
    }

    public float getValue() {
        return this.value;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTrackBars(canvas);
        if (getWidth() > 0 && getHeight() > 0) {
            this.trackLayer.record(this, getWidth(), getHeight(), new GlassLayerBackdrop.Recorder() { // from class: org.telegram.ui.Components.glass.controls.GlassIosSettingsSlider$$ExternalSyntheticLambda0
                @Override // org.telegram.ui.Components.glass.engine.GlassLayerBackdrop.Recorder
                public final void draw(Canvas canvas2) {
                    GlassIosSettingsSlider.this.drawTrackBars(canvas2);
                }
            });
        }
        drawIndicators(canvas);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(SeekBar.class.getName());
        accessibilityEvent.setItemCount(this.steps + 2);
        accessibilityEvent.setCurrentItemIndex(Math.round(this.motion.getProgress() * (this.steps + 1)));
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(SeekBar.class.getName());
        accessibilityNodeInfo.setScrollable(isEnabled());
        int i = Build.VERSION.SDK_INT;
        accessibilityNodeInfo.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(1, this.rangeStart, this.rangeEnd, this.value));
        if (isEnabled()) {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
            if (i >= 24) {
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS);
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.trackLeft = getPaddingLeft();
        this.trackWidth = Math.max(1.0f, (getWidth() - getPaddingLeft()) - getPaddingRight());
        float paddingTop = getPaddingTop() + (((getHeight() - getPaddingTop()) - getPaddingBottom()) * 0.5f);
        this.trackCenterY = paddingTop;
        int round = Math.round(paddingTop - dp(12.0f));
        ThumbSurface thumbSurface = this.thumb;
        thumbSurface.layout(0, round, thumbSurface.getMeasuredWidth(), this.thumb.getMeasuredHeight() + round);
        updateThumbState();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        setMeasuredDimension(View.resolveSize(Math.round(dp(200.0f)) + getPaddingLeft() + getPaddingRight(), i), View.resolveSize(Math.round(dp(24.0f)) + getPaddingTop() + getPaddingBottom(), i2));
        this.thumb.measure(View.MeasureSpec.makeMeasureSpec(Math.round(dp(40.0f)), 1073741824), View.MeasureSpec.makeMeasureSpec(Math.round(dp(24.0f)), 1073741824));
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        requestLayout();
        updateThumbState();
        invalidate();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (isEnabled()) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                if (isInsideThumb(motionEvent.getX(), motionEvent.getY())) {
                    this.tapCandidate = false;
                    return this.dragDetector.onTouchEvent(motionEvent);
                }
                this.tapCandidate = true;
                this.tapDownX = motionEvent.getX();
                this.tapDownY = motionEvent.getY();
                setPressed(true);
                return true;
            } else if (actionMasked == 1) {
                if (this.dragDetector.isDragging()) {
                    return this.dragDetector.onTouchEvent(motionEvent);
                }
                if (this.tapCandidate) {
                    this.tapCandidate = false;
                    setPressed(false);
                    setValueFromTap(motionEvent.getX());
                    performClick();
                }
                return true;
            } else if (actionMasked != 2) {
                if (actionMasked != 3) {
                    return this.dragDetector.isDragging() && this.dragDetector.onTouchEvent(motionEvent);
                }
                if (this.dragDetector.isDragging()) {
                    this.dragDetector.onTouchEvent(motionEvent);
                }
                this.tapCandidate = false;
                setPressed(false);
                return true;
            } else if (this.dragDetector.isDragging()) {
                return this.dragDetector.onTouchEvent(motionEvent);
            } else {
                if (this.tapCandidate && (Math.abs(motionEvent.getX() - this.tapDownX) > this.touchSlop || Math.abs(motionEvent.getY() - this.tapDownY) > this.touchSlop)) {
                    this.tapCandidate = false;
                    setPressed(false);
                }
                return true;
            }
        }
        return false;
    }

    @Override // android.view.View
    public boolean performAccessibilityAction(int i, Bundle bundle) {
        if (isEnabled()) {
            float rangeLength = rangeLength() / (this.steps + 1.0f);
            if (i == 4096) {
                setValueFromAccessibility(this.value + rangeLength);
                return true;
            } else if (i == 8192) {
                setValueFromAccessibility(this.value - rangeLength);
                return true;
            } else if (Build.VERSION.SDK_INT < 24 || i != AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.getId() || bundle == null || !bundle.containsKey("android.view.accessibility.action.ARGUMENT_PROGRESS_VALUE")) {
                return super.performAccessibilityAction(i, bundle);
            } else {
                setValueFromAccessibility(bundle.getFloat("android.view.accessibility.action.ARGUMENT_PROGRESS_VALUE"));
                return true;
            }
        }
        return super.performAccessibilityAction(i, bundle);
    }

    @Override // android.view.View
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public void setBackdropSource(GlassBackdropSource glassBackdropSource) {
        this.baseBackdropSource = glassBackdropSource;
        rebuildThumbBackdrop();
    }

    public void setDarkTheme(boolean z) {
        this.darkTheme = z;
        invalidate();
    }

    public void setDetentFractions(float... fArr) {
        if (fArr == null || fArr.length == 0) {
            this.detentFractions = new float[0];
        } else {
            float[] copyOf = Arrays.copyOf(fArr, fArr.length);
            for (int i = 0; i < copyOf.length; i++) {
                copyOf[i] = GlassControlUtils.clamp01(copyOf[i]);
            }
            Arrays.sort(copyOf);
            int i2 = 0;
            for (float f : copyOf) {
                if (i2 == 0 || f != copyOf[i2 - 1]) {
                    copyOf[i2] = f;
                    i2++;
                }
            }
            this.detentFractions = Arrays.copyOf(copyOf, i2);
        }
        float[] fArr2 = this.detentFractions;
        this.showDetentIndicators = fArr2.length > 0;
        this.detentHapticsEnabled = fArr2.length > 0;
        this.lastDetentIndex = -1;
        invalidate();
    }

    public void setDetentHapticsEnabled(boolean z) {
        this.detentHapticsEnabled = z;
    }

    @Override // android.view.View
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.thumb.setEnabled(z);
        if (!z) {
            this.dragDetector.cancel();
            this.tapCandidate = false;
            this.motion.release();
        }
        updateThumbState();
        invalidate();
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.valueChangeListener = onValueChangeListener;
    }

    public void setShowDetentIndicators(boolean z) {
        this.showDetentIndicators = z;
        invalidate();
    }

    public void setShowStepIndicators(boolean z) {
        this.showStepIndicators = z;
        invalidate();
    }

    public void setSourceCoordinateView(View view) {
        this.sourceCoordinateView = view;
        this.thumb.setSourceCoordinateView(view);
        this.trackLayer.setCoordinateRoot(view);
        if (view == null) {
            updateExplicitSourceOffset();
        }
    }

    public void setSteps(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("steps must be >= 0");
        }
        this.steps = i;
        this.showStepIndicators = i > 0;
        this.previousSnappedValue = snapValue(this.value);
        invalidate();
    }

    public void setTelegramBackdropSource(BlurredBackgroundSource blurredBackgroundSource) {
        setBackdropSource(blurredBackgroundSource == null ? null : new TelegramBlurredBackgroundSourceAdapter(blurredBackgroundSource));
    }

    public void setValue(float f) {
        setValue(f, true);
    }

    public void setValue(float f, boolean z) {
        float clampToRange = clampToRange(f);
        setInternalValue(clampToRange);
        if (z) {
            this.motion.updateValue(clampToRange);
        } else {
            this.motion.snapTo(clampToRange);
        }
    }

    public void setValueRange(float f, float f2) {
        if (f2 <= f) {
            throw new IllegalArgumentException("value range must be increasing");
        }
        if (this.rangeStart == f && this.rangeEnd == f2) {
            return;
        }
        this.rangeStart = f;
        this.rangeEnd = f2;
        this.value = clampToRange(this.value);
        recreateMotion(false);
        invalidate();
        sendAccessibilityEvent(2048);
    }

    public void setVisibilityThreshold(float f) {
        if (f <= 0.0f) {
            throw new IllegalArgumentException("visibilityThreshold must be > 0");
        }
        if (this.visibilityThreshold == f) {
            return;
        }
        this.visibilityThreshold = f;
        recreateMotion(false);
    }
}
