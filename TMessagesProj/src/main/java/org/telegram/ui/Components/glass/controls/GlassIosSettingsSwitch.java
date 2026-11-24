package org.telegram.ui.Components.glass.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Checkable;
import android.widget.Switch;
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
public class GlassIosSettingsSwitch extends ViewGroup implements Checkable {
    private float accumulatedDragX;
    private GlassBackdropSource baseBackdropSource;
    private boolean checked;
    private OnCheckedChangeListener checkedChangeListener;
    private boolean darkTheme;
    private boolean didDrag;
    private float explicitSourceX;
    private float explicitSourceY;
    private final GlassDragGestureDetector gestureDetector;
    private final GlassDampedDragController motion;
    private boolean parentGestureCaptured;
    private final GlassScaledBackdropSource scaledTrack;
    private View sourceCoordinateView;
    private boolean suppressNextCheckedHaptic;
    private boolean suppressPerformClickToggle;
    private final ThumbSurface thumb;
    private final float touchSlop;
    private final GlassLayerBackdrop trackLayer;
    private float trackLeft;
    private final Paint trackPaint;
    private final Path trackPath;
    private float trackTop;
    private float virtualFraction;

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public interface OnCheckedChangeListener {
        void onCheckedChanged(GlassIosSettingsSwitch glassIosSettingsSwitch, boolean z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public final class ThumbSurface extends GlassSurfaceFrameLayout {
        ThumbSurface(Context context) {
            super(context);
            setCapsule(true);
            setClickable(false);
            setDuplicateParentStateEnabled(true);
        }

        void updateVisuals(float f, boolean z) {
            float clamp01 = GlassControlUtils.clamp01(f);
            float f2 = 1.0f - clamp01;
            setEffectSpec(GlassEffectSpec.lensOnly(GlassIosSettingsSwitch.this.dp(8.0f) * f2, GlassIosSettingsSwitch.this.dp(5.0f) * clamp01, GlassIosSettingsSwitch.this.dp(10.0f) * clamp01, true));
            setTintColor(GlassControlUtils.multiplyAlpha(-1, f2));
            setHighlightSpec(GlassHighlightSpec.ambient(getResources().getDisplayMetrics().density, clamp01));
            setOuterShadowSpec(new GlassShadowSpec(GlassIosSettingsSwitch.this.dp(4.0f), 0.0f, GlassIosSettingsSwitch.this.dp(4.0f) / 6.0f, 218103808, 1.0f));
            setInnerShadowSpec(new GlassInnerShadowSpec(GlassIosSettingsSwitch.this.dp(4.0f) * clamp01, 0.0f, GlassIosSettingsSwitch.this.dp(4.0f) * clamp01, 637534208, clamp01));
        }
    }

    public static /* synthetic */ void $r8$lambda$kMZ0v3HcdaLlfQ12mwWbhRVWnfA(GlassIosSettingsSwitch glassIosSettingsSwitch, GlassDampedDragController glassDampedDragController) {
        glassIosSettingsSwitch.updateThumbState();
        glassIosSettingsSwitch.invalidate();
    }

    public GlassIosSettingsSwitch(Context context) {
        this(context, null);
    }

    public GlassIosSettingsSwitch(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GlassIosSettingsSwitch(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.trackPaint = new Paint(1);
        this.trackPath = new Path();
        GlassLayerBackdrop glassLayerBackdrop = new GlassLayerBackdrop();
        this.trackLayer = glassLayerBackdrop;
        this.scaledTrack = new GlassScaledBackdropSource(glassLayerBackdrop);
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        setFocusable(true);
        setClickable(true);
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        ThumbSurface thumbSurface = new ThumbSurface(context);
        this.thumb = thumbSurface;
        addView(thumbSurface, new ViewGroup.LayoutParams(Math.round(dp(40.0f)), Math.round(dp(24.0f))));
        this.motion = new GlassDampedDragController(0.0f, 0.0f, 1.0f, 0.001f, 1.0f, 1.5f).setListener(new GlassDampedDragController.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosSettingsSwitch$$ExternalSyntheticLambda1
            @Override // org.telegram.ui.Components.glass.controls.GlassDampedDragController.Listener
            public final void onAnimationChanged(GlassDampedDragController glassDampedDragController) {
                GlassIosSettingsSwitch.$r8$lambda$kMZ0v3HcdaLlfQ12mwWbhRVWnfA(GlassIosSettingsSwitch.this, glassDampedDragController);
            }
        });
        this.gestureDetector = new GlassDragGestureDetector(new GlassDragGestureDetector.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosSettingsSwitch.1
            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDrag(float f, float f2, float f3, float f4) {
                GlassIosSettingsSwitch.access$116(GlassIosSettingsSwitch.this, f3);
                boolean z = !GlassIosSettingsSwitch.this.didDrag && Math.abs(GlassIosSettingsSwitch.this.accumulatedDragX) > GlassIosSettingsSwitch.this.touchSlop;
                if (z) {
                    GlassIosSettingsSwitch.this.didDrag = true;
                }
                if (GlassIosSettingsSwitch.this.didDrag) {
                    if (z) {
                        f3 = GlassIosSettingsSwitch.this.accumulatedDragX;
                    }
                    boolean z2 = GlassIosSettingsSwitch.this.virtualFraction >= 0.5f;
                    float max = f3 / Math.max(1.0f, GlassIosSettingsSwitch.this.dp(20.0f));
                    if (GlassControlUtils.isRtl(GlassIosSettingsSwitch.this)) {
                        max = -max;
                    }
                    GlassIosSettingsSwitch glassIosSettingsSwitch = GlassIosSettingsSwitch.this;
                    glassIosSettingsSwitch.virtualFraction = GlassControlUtils.clamp01(glassIosSettingsSwitch.virtualFraction + max);
                    boolean z3 = GlassIosSettingsSwitch.this.virtualFraction >= 0.5f;
                    if (GlassIosSettingsSwitch.this.didDrag && z2 != z3) {
                        GlassControlUtils.performSegmentTick(GlassIosSettingsSwitch.this);
                    }
                    GlassIosSettingsSwitch.this.motion.updateValue(GlassIosSettingsSwitch.this.virtualFraction);
                }
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragCancel() {
                GlassIosSettingsSwitch.this.finishGesture(true);
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragEnd(float f, float f2) {
                GlassIosSettingsSwitch.this.finishGesture(false);
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragStart(float f, float f2) {
                GlassIosSettingsSwitch.this.didDrag = false;
                GlassIosSettingsSwitch.this.accumulatedDragX = 0.0f;
                GlassIosSettingsSwitch glassIosSettingsSwitch = GlassIosSettingsSwitch.this;
                glassIosSettingsSwitch.virtualFraction = glassIosSettingsSwitch.motion.getTargetValue();
                GlassIosSettingsSwitch.this.motion.press();
                GlassIosSettingsSwitch.this.thumb.setPressed(true);
            }
        });
        resolveAppearance();
    }

    static /* synthetic */ float access$116(GlassIosSettingsSwitch glassIosSettingsSwitch, float f) {
        float f2 = glassIosSettingsSwitch.accumulatedDragX + f;
        glassIosSettingsSwitch.accumulatedDragX = f2;
        return f2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float dp(float f) {
        return GlassControlUtils.dp(this, f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void drawTrack(Canvas canvas) {
        int lerpColor = GlassControlUtils.lerpColor(1417641855, this.darkTheme ? -13577896 : -13318311, this.motion.getValue());
        if (!isEnabled()) {
            lerpColor = GlassControlUtils.multiplyAlpha(lerpColor, 0.33f);
        }
        this.trackPaint.setColor(lerpColor);
        this.trackPaint.setStyle(Paint.Style.FILL);
        Path path = this.trackPath;
        float f = this.trackLeft;
        GlassContinuousPathFactory.capsule(path, new RectF(f, this.trackTop, dp(64.0f) + f, this.trackTop + dp(28.0f)));
        canvas.drawPath(this.trackPath, this.trackPaint);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishGesture(boolean z) {
        this.thumb.setPressed(false);
        if (z) {
            this.motion.animateToValue(this.checked ? 1.0f : 0.0f);
            this.didDrag = false;
            return;
        }
        boolean z2 = !this.didDrag ? this.checked : this.motion.getTargetValue() < 0.5f;
        boolean z3 = this.didDrag;
        this.didDrag = false;
        setCheckedFromUser(z2, z3);
        if (z3) {
            return;
        }
        this.suppressPerformClickToggle = true;
        performClick();
        this.suppressPerformClickToggle = false;
    }

    private float getThumbPhysicalX() {
        float dp = this.trackLeft + dp(2.0f) + (dp(20.0f) * this.motion.getValue());
        return GlassControlUtils.isRtl(this instanceof RenderNode) ? ((this.trackLeft + dp(64.0f)) - dp(40.0f)) - (dp - this.trackLeft) : dp;
    }

    private void rebuildThumbBackdrop() {
        ThumbSurface thumbSurface = this.thumb;
        GlassBackdropSource glassBackdropSource = this.baseBackdropSource;
        thumbSurface.setBackdropSource(glassBackdropSource == null ? this.scaledTrack : GlassCombinedBackdropSource.of(glassBackdropSource, this.scaledTrack));
    }

    private void resolveAppearance() {
        ThumbSurface thumbSurface = this.thumb;
        if (thumbSurface == null) {
            return;
        }
        thumbSurface.setAlpha(1.0f);
        updateThumbState();
        invalidate();
    }

    private void setChecked(boolean z, boolean z2, boolean z3) {
        if (this.checked == z) {
            if (this.motion.getTargetValue() == (z ? 1.0f : 0.0f)) {
                return;
            }
        }
        boolean z4 = this.checked != z;
        this.checked = z;
        float f = z ? 1.0f : 0.0f;
        this.virtualFraction = f;
        if (z2) {
            this.motion.animateToValue(f);
        } else {
            this.motion.snapTo(f);
            this.motion.release();
        }
        if (z4 && z3) {
            GlassControlUtils.performSegmentTick(this);
        }
        refreshDrawableState();
        sendAccessibilityEvent(2048);
    }

    private void setCheckedFromUser(boolean z, boolean z2) {
        OnCheckedChangeListener onCheckedChangeListener;
        boolean z3 = this.checked != z;
        this.checked = z;
        float f = z ? 1.0f : 0.0f;
        this.virtualFraction = f;
        this.motion.animateToValue(f);
        if (z3 && !z2) {
            GlassControlUtils.performSegmentTick(this);
        }
        if (z3 && (onCheckedChangeListener = this.checkedChangeListener) != null) {
            if (z2) {
                this.suppressNextCheckedHaptic = true;
            }
            onCheckedChangeListener.onCheckedChanged(this, z);
            this.suppressNextCheckedHaptic = false;
        }
        refreshDrawableState();
        sendAccessibilityEvent(1);
    }

    private void setParentGestureCaptured(boolean z) {
        if (this.parentGestureCaptured == z) {
            return;
        }
        this.parentGestureCaptured = z;
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(z);
        }
    }

    private void updateExplicitSourceOffset() {
        this.trackLayer.setOriginInSource(this.explicitSourceX, this.explicitSourceY);
        this.thumb.setSourceOffset(this.explicitSourceX + getThumbPhysicalX(), this.explicitSourceY + this.trackTop + dp(2.0f));
    }

    private void updateThumbState() {
        ThumbSurface thumbSurface = this.thumb;
        if (thumbSurface == null || thumbSurface.getMeasuredWidth() == 0) {
            return;
        }
        float velocity = this.motion.getVelocity() / 50.0f;
        float clamp = GlassControlUtils.clamp(velocity * 0.75f, -0.2f, 0.2f);
        float clamp2 = GlassControlUtils.clamp(velocity * 0.25f, -0.2f, 0.2f);
        ThumbSurface thumbSurface2 = this.thumb;
        thumbSurface2.setPivotX(thumbSurface2.getWidth() * 0.5f);
        ThumbSurface thumbSurface3 = this.thumb;
        thumbSurface3.setPivotY(thumbSurface3.getHeight() * 0.5f);
        this.thumb.setScaleX(this.motion.getScaleX() / (1.0f - clamp));
        this.thumb.setScaleY(this.motion.getScaleY() * (1.0f - clamp2));
        this.thumb.setTranslationX(getThumbPhysicalX());
        float pressProgress = this.motion.getPressProgress();
        this.scaledTrack.setScale(GlassControlUtils.lerp(0.6666667f, 0.75f, pressProgress), GlassControlUtils.lerp(0.0f, 0.75f, pressProgress));
        this.thumb.updateVisuals(this.motion.getPressProgress(), isEnabled());
        if (this.sourceCoordinateView == null) {
            updateExplicitSourceOffset();
        }
    }

    @Override // android.widget.Checkable
    public boolean isChecked() {
        return this.checked;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 1);
        if (this.checked) {
            View.mergeDrawableStates(onCreateDrawableState, new int[]{16842912});
        }
        return onCreateDrawableState;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        this.gestureDetector.cancel();
        setParentGestureCaptured(false);
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTrack(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        this.trackLayer.record(this, getWidth(), getHeight(), new GlassLayerBackdrop.Recorder() { // from class: org.telegram.ui.Components.glass.controls.GlassIosSettingsSwitch$$ExternalSyntheticLambda0
            @Override // org.telegram.ui.Components.glass.engine.GlassLayerBackdrop.Recorder
            public final void draw(Canvas canvas2) {
                GlassIosSettingsSwitch.this.drawTrack(canvas2);
            }
        });
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(Switch.class.getName());
        accessibilityEvent.setChecked(this.checked);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(Switch.class.getName());
        accessibilityNodeInfo.setCheckable(true);
        accessibilityNodeInfo.setChecked(this.checked);
        accessibilityNodeInfo.setClickable(isEnabled());
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        float dp = dp(64.0f);
        float dp2 = dp(28.0f);
        this.trackLeft = getPaddingLeft() + Math.max(0.0f, (((getWidth() - getPaddingLeft()) - getPaddingRight()) - dp) * 0.5f);
        float paddingTop = getPaddingTop() + Math.max(0.0f, (((getHeight() - getPaddingTop()) - getPaddingBottom()) - dp2) * 0.5f);
        this.trackTop = paddingTop;
        int round = Math.round(paddingTop + dp(2.0f));
        ThumbSurface thumbSurface = this.thumb;
        thumbSurface.layout(0, round, thumbSurface.getMeasuredWidth(), this.thumb.getMeasuredHeight() + round);
        updateThumbState();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        setMeasuredDimension(View.resolveSize(Math.round(dp(64.0f)) + getPaddingLeft() + getPaddingRight(), i), View.resolveSize(Math.round(dp(28.0f)) + getPaddingTop() + getPaddingBottom(), i2));
        this.thumb.measure(View.MeasureSpec.makeMeasureSpec(Math.round(dp(40.0f)), 1073741824), View.MeasureSpec.makeMeasureSpec(Math.round(dp(24.0f)), 1073741824));
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        requestLayout();
        updateThumbState();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (isEnabled()) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                setParentGestureCaptured(true);
            }
            boolean onTouchEvent = this.gestureDetector.onTouchEvent(motionEvent);
            if (actionMasked == 1 || actionMasked == 3 || !this.gestureDetector.isDragging()) {
                setParentGestureCaptured(false);
                return onTouchEvent;
            }
            return onTouchEvent;
        }
        return false;
    }

    @Override // android.view.View
    public boolean performClick() {
        super.performClick();
        if (!this.suppressPerformClickToggle && isEnabled()) {
            setCheckedFromUser(!this.checked, false);
        }
        return true;
    }

    public void setBackdropSource(GlassBackdropSource glassBackdropSource) {
        this.baseBackdropSource = glassBackdropSource;
        rebuildThumbBackdrop();
    }

    @Override // android.widget.Checkable
    public void setChecked(boolean z) {
        setChecked(z, true);
    }

    public void setChecked(boolean z, boolean z2) {
        boolean z3 = this.suppressNextCheckedHaptic;
        boolean z4 = false;
        this.suppressNextCheckedHaptic = false;
        if (isAttachedToWindow() && !z3) {
            z4 = true;
        }
        setChecked(z, z2, z4);
    }

    public void setCheckedFromHost(boolean z, boolean z2) {
        this.suppressNextCheckedHaptic = false;
        setChecked(z, z2, false);
    }

    public void setDarkTheme(boolean z) {
        if (this.darkTheme == z) {
            return;
        }
        this.darkTheme = z;
        resolveAppearance();
    }

    @Override // android.view.View
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.thumb.setEnabled(z);
        if (!z) {
            this.gestureDetector.cancel();
            setParentGestureCaptured(false);
            this.motion.release();
        }
        resolveAppearance();
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.checkedChangeListener = onCheckedChangeListener;
    }

    public void setSourceCoordinateView(View view) {
        this.sourceCoordinateView = view;
        this.thumb.setSourceCoordinateView(view);
        this.trackLayer.setCoordinateRoot(view);
        if (view == null) {
            updateExplicitSourceOffset();
        }
    }

    public void setTelegramBackdropSource(BlurredBackgroundSource blurredBackgroundSource) {
        setBackdropSource(blurredBackgroundSource == null ? null : new TelegramBlurredBackgroundSourceAdapter(blurredBackgroundSource));
    }

    @Override // android.widget.Checkable
    public void toggle() {
        setCheckedFromUser(!this.checked, false);
    }
}
