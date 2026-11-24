package org.telegram.ui.Components.glass.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.telegram.ui.Components.glass.controls.GlassDragGestureDetector;
import org.telegram.ui.Components.glass.engine.GlassBackdropSource;
import org.telegram.ui.Components.glass.engine.GlassEffectSpec;
import org.telegram.ui.Components.glass.engine.GlassInteractiveHighlightRenderer;
import org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout;
import org.telegram.ui.Components.glass.motion.GlassInteractiveHighlightController;
import org.telegram.ui.Components.glass.shape.GlassContinuousPathFactory;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassIosActionButton extends GlassSurfaceFrameLayout {
    private static final GlassEffectSpec NO_EFFECTS = new GlassEffectSpec(0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, false);
    private final GlassContainerRim containerRim;
    private boolean darkTheme;
    private final GlassDragGestureDetector gestureDetector;
    private boolean handlingTouch;
    private GlassInteractiveHighlightController interactiveHighlight;
    private final GlassInteractiveHighlightRenderer lightRenderer;
    private boolean liquidGlassEnabled;
    private boolean navigationMaterial;
    private boolean ready;
    private GlassBackdropSource requestedBackdrop;
    private final Paint surfacePaint;
    private final Path surfacePath;

    public GlassIosActionButton(Context context) {
        super(context);
        this.interactiveHighlight = new GlassInteractiveHighlightController();
        this.lightRenderer = new GlassInteractiveHighlightRenderer();
        this.surfacePaint = new Paint(1);
        this.surfacePath = new Path();
        this.containerRim = new GlassContainerRim();
        this.liquidGlassEnabled = true;
        setCapsule(true);
        setMinimumHeight(Math.round(dp(40.0f)));
        setMinimumWidth(Math.round(dp(64.0f)));
        setPadding(Math.round(dp(12.0f)), 0, Math.round(dp(12.0f)), 0);
        setClickable(true);
        setFocusable(true);
        this.interactiveHighlight.setListener(new GlassInteractiveHighlightController.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosActionButton$$ExternalSyntheticLambda1
            @Override // org.telegram.ui.Components.glass.motion.GlassInteractiveHighlightController.Listener
            public final void onHighlightChanged(GlassInteractiveHighlightController glassInteractiveHighlightController) {
                GlassIosActionButton.this.updatePressVisuals();
            }
        });
        this.gestureDetector = new GlassDragGestureDetector(new GlassDragGestureDetector.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosActionButton.1
            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDrag(float f, float f2, float f3, float f4) {
                GlassIosActionButton.this.interactiveHighlight.move(f, f2);
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragCancel() {
                GlassIosActionButton.this.interactiveHighlight.cancel();
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragEnd(float f, float f2) {
                GlassIosActionButton.this.interactiveHighlight.release();
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragStart(float f, float f2) {
                GlassIosActionButton.this.interactiveHighlight.press(f, f2);
            }
        });
        this.ready = true;
        resolveMaterial();
    }

    private float dp(float f) {
        return GlassControlUtils.dp(this, f);
    }

    private void rebuildPaths() {
        float width = getWidth();
        float height = getHeight();
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        GlassContinuousPathFactory.capsule(this.surfacePath, width, height);
        this.surfacePaint.setShader(this.darkTheme ? new LinearGradient(0.0f, 0.0f, 0.0f, height, new int[]{83886079, 50331647, 117440511}, new float[]{0.0f, 0.5f, 1.0f}, Shader.TileMode.CLAMP) : new LinearGradient(0.0f, 0.0f, 0.0f, height, 553648127, 0, Shader.TileMode.CLAMP));
    }

    private void resetPress() {
        this.interactiveHighlight.setListener(null);
        this.gestureDetector.cancel();
        this.interactiveHighlight.dispose();
        GlassInteractiveHighlightController glassInteractiveHighlightController = new GlassInteractiveHighlightController();
        this.interactiveHighlight = glassInteractiveHighlightController;
        glassInteractiveHighlightController.setListener(new GlassInteractiveHighlightController.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosActionButton$$ExternalSyntheticLambda0
            @Override // org.telegram.ui.Components.glass.motion.GlassInteractiveHighlightController.Listener
            public final void onHighlightChanged(GlassInteractiveHighlightController glassInteractiveHighlightController2) {
                GlassIosActionButton.this.updatePressVisuals();
            }
        });
        updatePressVisuals();
    }

    private void resolveMaterial() {
        boolean z = this.liquidGlassEnabled && this.requestedBackdrop != null;
        int i = -328964;
        if (!this.navigationMaterial) {
            if (z) {
                i = this.darkTheme ? -1389219278 : -1275068417;
            } else if (this.darkTheme) {
                i = -14474461;
            }
            setTintColor(i);
            setEffectSpec(z ? GlassEffectSpec.lensOnly(dp(6.0f), dp(10.0f), dp(16.0f), false) : NO_EFFECTS);
            setHighlightSpec(null);
            setInnerShadowSpec(null);
            setOuterShadowSpec(null);
            rebuildPaths();
            applyContentColorToChildren();
            invalidateGlass();
            return;
        }
        float f = getResources().getDisplayMetrics().density;
        if (z) {
            i = GlassNavigationContainerSurface.tintForTheme(this.darkTheme);
        } else if (this.darkTheme) {
            i = -14277080;
        }
        setTintColor(i);
        setEffectSpec(z ? GlassNavigationContainerSurface.effectForSize(f, getWidth(), getHeight()) : NO_EFFECTS);
        setHighlightSpec(GlassNavigationContainerSurface.highlightForTheme(this.darkTheme, f));
        setOuterShadowSpec(GlassNavigationContainerSurface.shadowForTheme(this.darkTheme, f));
        setInnerShadowSpec(null);
        rebuildPaths();
        applyContentColorToChildren();
        invalidateGlass();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePressVisuals() {
        float pressProgress = this.interactiveHighlight.getPressProgress();
        float height = getHeight();
        float width = getWidth();
        if (!this.liquidGlassEnabled || !isEnabled() || width <= 0.0f || height <= 0.0f) {
            setTranslationX(0.0f);
            setTranslationY(0.0f);
            setScaleX(1.0f);
            setScaleY(1.0f);
            invalidateGlass();
            return;
        }
        float dp = ((dp(4.0f) / height) * pressProgress) + 1.0f;
        float offsetX = this.interactiveHighlight.getOffsetX();
        float offsetY = this.interactiveHighlight.getOffsetY();
        float min = Math.min(width, height);
        float max = Math.max(width, height);
        float tanh = ((float) Math.tanh((offsetX * 0.05f) / min)) * min;
        float tanh2 = min * ((float) Math.tanh((0.05f * offsetY) / min));
        float dp2 = dp(4.0f) / height;
        double atan2 = (float) Math.atan2(offsetY, offsetX);
        float abs = (Math.abs((((float) Math.cos(atan2)) * offsetX) / max) * dp2 * Math.min(width / height, 1.0f)) + dp;
        setPivotX(width * 0.5f);
        setPivotY(height * 0.5f);
        setTranslationX(tanh);
        setTranslationY(tanh2);
        setScaleX(abs);
        setScaleY(dp + (dp2 * Math.abs((((float) Math.sin(atan2)) * offsetY) / max) * Math.min(height / width, 1.0f)));
        invalidateGlass();
    }

    public void applyContentColorToChildren() {
        int i = this.darkTheme ? -1 : -16777216;
        if (!isEnabled()) {
            i = GlassControlUtils.multiplyAlpha(i, 0.38f);
        }
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            View childAt = getChildAt(i2);
            if (childAt instanceof TextView) {
                ((TextView) childAt).setTextColor(i);
            } else if (childAt instanceof ImageView) {
                ((ImageView) childAt).setColorFilter(i);
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (isEnabled()) {
            this.handlingTouch = true;
            try {
                this.gestureDetector.onTouchEvent(motionEvent);
                boolean dispatchTouchEvent = super.dispatchTouchEvent(motionEvent);
                this.handlingTouch = false;
                if (motionEvent.getActionMasked() == 0 && !dispatchTouchEvent) {
                    this.gestureDetector.cancel();
                }
                return dispatchTouchEvent;
            } catch (Throwable th) {
                this.handlingTouch = false;
                throw th;
            }
        }
        return false;
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return isClickable() ? Button.class.getName() : super.getAccessibilityClassName();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        resetPress();
        super.onDetachedFromWindow();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
    public void onDrawGlassContent(Canvas canvas) {
        if (this.navigationMaterial) {
            return;
        }
        canvas.drawPath(this.surfacePath, this.surfacePaint);
        float max = Math.max(0.0f, Math.min(1.0f, this.interactiveHighlight.getPressProgress()));
        GlassInteractiveHighlightController glassInteractiveHighlightController = this.interactiveHighlight;
        this.lightRenderer.draw(canvas, this.surfacePath, getWidth(), getHeight(), glassInteractiveHighlightController, glassInteractiveHighlightController.getPositionX(), this.interactiveHighlight.getPositionY(), max * 0.18f);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
    public void onDrawGlassForeground(Canvas canvas) {
        this.containerRim.draw(canvas, getWidth(), getHeight(), getResources().getDisplayMetrics().density, this.darkTheme);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout, android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (this.ready) {
            if (this.navigationMaterial && this.liquidGlassEnabled && this.requestedBackdrop != null) {
                setEffectSpec(GlassNavigationContainerSurface.effectForSize(getResources().getDisplayMetrics().density, i, i2));
            }
            rebuildPaths();
            updatePressVisuals();
        }
    }

    @Override // android.view.ViewGroup
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        applyContentColorToChildren();
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
    public void setBackdropSource(GlassBackdropSource glassBackdropSource) {
        this.requestedBackdrop = glassBackdropSource;
        if (!this.liquidGlassEnabled) {
            glassBackdropSource = null;
        }
        super.setBackdropSource(glassBackdropSource);
        if (this.ready) {
            resolveMaterial();
        }
    }

    public void setDarkTheme(boolean z) {
        if (this.darkTheme != z) {
            this.darkTheme = z;
            resolveMaterial();
        }
    }

    @Override // android.view.View
    public void setEnabled(boolean z) {
        if (z == isEnabled()) {
            return;
        }
        super.setEnabled(z);
        if (this.ready) {
            if (!z) {
                resetPress();
            }
            applyContentColorToChildren();
        }
    }

    public void setLiquidGlassEnabled(boolean z) {
        if (this.liquidGlassEnabled == z) {
            return;
        }
        this.liquidGlassEnabled = z;
        if (!z) {
            resetPress();
        }
        super.setBackdropSource(z ? this.requestedBackdrop : null);
        resolveMaterial();
    }

    public void setNavigationMaterial(boolean z) {
        if (this.navigationMaterial != z) {
            this.navigationMaterial = z;
            resolveMaterial();
        }
    }

    @Override // android.view.View
    public void setPressed(boolean z) {
        super.setPressed(z);
        if (!this.ready || this.handlingTouch || this.gestureDetector.isDragging() || !isEnabled()) {
            return;
        }
        if (z) {
            this.interactiveHighlight.press(getWidth() * 0.5f, getHeight() * 0.5f);
        } else {
            this.interactiveHighlight.release();
        }
    }
}
