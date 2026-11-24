package org.telegram.ui.Components.glass.controls;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.telegram.messenger.ImageReceiver$;
import org.telegram.ui.Components.glass.controls.GlassDragGestureDetector;
import org.telegram.ui.Components.glass.engine.GlassBackdropSource;
import org.telegram.ui.Components.glass.engine.GlassEffectSpec;
import org.telegram.ui.Components.glass.engine.GlassHighlightSpec;
import org.telegram.ui.Components.glass.engine.GlassInteractiveHighlightRenderer;
import org.telegram.ui.Components.glass.engine.GlassShadowSpec;
import org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout;
import org.telegram.ui.Components.glass.motion.GlassInteractiveHighlightController;
import org.telegram.ui.Components.glass.shape.GlassContinuousPathFactory;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public abstract class GlassIosLiquidButton extends GlassSurfaceFrameLayout {
    private int accentColor;
    private final Paint borderPaint;
    private final int contentGap;
    private boolean darkTheme;
    private int fallbackBorderColor;
    private final GlassDragGestureDetector gestureDetector;
    private RippleDrawable indicationDrawable;
    private final GlassInteractiveHighlightController interactiveHighlight;
    private final GlassInteractiveHighlightRenderer interactiveHighlightRenderer;
    private boolean liquidGlassEnabled;
    private final Path localPath;
    private GlassBackdropSource requestedBackdropSource;
    private int resolvedContentColor;
    private int resolvedSurfaceColor;
    private Style style;
    private final Paint surfacePaint;

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    private static final class Api29Blend {
        static void drawProminentHue(Canvas canvas, Path path, Paint paint, int i) {
            BlendMode blendMode;
            BlendMode blendMode2;
            paint.setColor(i);
            paint.setAlpha(255);
            blendMode = BlendMode.HUE;
            ImageReceiver$.ExternalSyntheticApiModelOutline1.m(paint, blendMode);
            canvas.drawPath(path, paint);
            blendMode2 = BlendMode.SRC_OVER;
            ImageReceiver$.ExternalSyntheticApiModelOutline1.m(paint, blendMode2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public static final class CapsuleMaskDrawable extends Drawable {
        private final Paint paint;
        private final Path path;

        CapsuleMaskDrawable() {
            Paint paint = new Paint(1);
            this.paint = paint;
            this.path = new Path();
            paint.setColor(-1);
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            GlassContinuousPathFactory.capsule(this.path, getBounds().width(), getBounds().height());
            int save = canvas.save();
            canvas.translate(getBounds().left, getBounds().top);
            canvas.drawPath(this.path, this.paint);
            canvas.restoreToCount(save);
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
            this.paint.setAlpha(i);
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.paint.setColorFilter(colorFilter);
        }
    }

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public enum Style {
        GLASS,
        PROMINENT
    }

    public GlassIosLiquidButton(Context context) {
        this(context, null);
    }

    public GlassIosLiquidButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GlassIosLiquidButton(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.surfacePaint = new Paint(1);
        this.borderPaint = new Paint(1);
        this.localPath = new Path();
        GlassInteractiveHighlightController glassInteractiveHighlightController = new GlassInteractiveHighlightController();
        this.interactiveHighlight = glassInteractiveHighlightController;
        this.interactiveHighlightRenderer = new GlassInteractiveHighlightRenderer();
        this.style = Style.GLASS;
        this.liquidGlassEnabled = true;
        this.accentColor = Color.rgb(0, 122, 255);
        this.resolvedContentColor = -16777216;
        this.resolvedSurfaceColor = 0;
        this.fallbackBorderColor = 0;
        setCapsule(true);
        setClickable(true);
        setFocusable(true);
        setMinimumHeight(Math.round(dp(48.0f)));
        setPadding(Math.round(dp(16.0f)), Math.round(dp(10.0f)), Math.round(dp(16.0f)), Math.round(dp(10.0f)));
        this.contentGap = Math.round(dp(8.0f));
        installMaterialIndication(context);
        glassInteractiveHighlightController.setListener(new GlassInteractiveHighlightController.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosLiquidButton$$ExternalSyntheticLambda0
            @Override // org.telegram.ui.Components.glass.motion.GlassInteractiveHighlightController.Listener
            public final void onHighlightChanged(GlassInteractiveHighlightController glassInteractiveHighlightController2) {
                GlassIosLiquidButton.this.updateButtonTransform();
            }
        });
        this.gestureDetector = new GlassDragGestureDetector(new GlassDragGestureDetector.Listener() { // from class: org.telegram.ui.Components.glass.controls.GlassIosLiquidButton.1
            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDrag(float f, float f2, float f3, float f4) {
                GlassIosLiquidButton.this.interactiveHighlight.move(f, f2);
                GlassIosLiquidButton.this.invalidate();
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragCancel() {
                GlassIosLiquidButton.this.interactiveHighlight.cancel();
                GlassIosLiquidButton.this.invalidate();
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragEnd(float f, float f2) {
                GlassIosLiquidButton.this.interactiveHighlight.release();
                GlassIosLiquidButton.this.invalidate();
            }

            @Override // org.telegram.ui.Components.glass.controls.GlassDragGestureDetector.Listener
            public void onDragStart(float f, float f2) {
                GlassIosLiquidButton.this.interactiveHighlight.press(f, f2);
                GlassIosLiquidButton.this.invalidate();
            }
        });
        resolveAppearance();
    }

    private float dp(float f) {
        return GlassControlUtils.dp(this, f);
    }

    private void installMaterialIndication(Context context) {
        TypedValue typedValue = new TypedValue();
        int i = this.darkTheme ? 872415231 : 520093696;
        if (context.getTheme().resolveAttribute(16843820, typedValue, true)) {
            i = typedValue.data;
        }
        RippleDrawable rippleDrawable = new RippleDrawable(ColorStateList.valueOf(i), null, new CapsuleMaskDrawable());
        this.indicationDrawable = rippleDrawable;
        rippleDrawable.setCallback(this);
        this.indicationDrawable.setState(getDrawableState());
    }

    private void resolveAppearance() {
        float f = getResources().getDisplayMetrics().density;
        boolean z = this.darkTheme;
        int i = z ? 1290529781 : 1279016003;
        int i2 = z ? 1534621824 : 863533184;
        int i3 = z ? -1893983202 : 2063597567;
        int i4 = z ? 704643071 : 520093696;
        Style style = this.style;
        Style style2 = Style.GLASS;
        if (style == style2) {
            if (isEnabled()) {
                i2 = this.liquidGlassEnabled ? i3 : this.darkTheme ? 762738304 : 343176320;
            }
            this.resolvedSurfaceColor = i2;
            if (isEnabled()) {
                i = this.darkTheme ? -1 : -16777216;
            }
            this.resolvedContentColor = i;
            this.fallbackBorderColor = i4;
        } else {
            if (isEnabled()) {
                i2 = this.liquidGlassEnabled ? 0 : this.accentColor;
            }
            this.resolvedSurfaceColor = i2;
            if (isEnabled()) {
                i = -855638017;
            }
            this.resolvedContentColor = i;
            if (isEnabled()) {
                i4 = 956301311;
            }
            this.fallbackBorderColor = i4;
        }
        setTintColor(0);
        setEffectSpec(this.liquidGlassEnabled ? GlassEffectSpec.liquid(dp(10.0f), dp(12.0f), dp(24.0f)) : new GlassEffectSpec(0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, false));
        if (this.liquidGlassEnabled) {
            setOuterShadowSpec(GlassShadowSpec.defaultShadow(f));
            if (this.darkTheme || this.style != style2) {
                setHighlightSpec(GlassHighlightSpec.defaultHighlight(f));
            } else {
                setHighlightSpec(new GlassHighlightSpec(f * 0.25f, f * 0.125f, 1.0f, Integer.MIN_VALUE, 45.0f, 1.0f, GlassHighlightSpec.Style.DEFAULT, false));
            }
        } else {
            setHighlightSpec(null);
            setOuterShadowSpec(null);
        }
        applyContentColorToChildren();
        updateButtonTransform();
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateButtonTransform() {
        float pressProgress = this.interactiveHighlight.getPressProgress();
        float height = getHeight();
        float width = getWidth();
        if (!this.liquidGlassEnabled || !isEnabled() || width <= 0.0f || height <= 0.0f) {
            setTranslationX(0.0f);
            setTranslationY(0.0f);
            setScaleX(1.0f);
            setScaleY(1.0f);
            invalidate();
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
        invalidate();
    }

    public void applyContentColorToChildren() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof TextView) {
                ((TextView) childAt).setTextColor(this.resolvedContentColor);
            } else if (childAt instanceof ImageView) {
                ((ImageView) childAt).setColorFilter(this.resolvedContentColor);
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        RippleDrawable rippleDrawable = this.indicationDrawable;
        if (rippleDrawable == null || !rippleDrawable.isStateful()) {
            return;
        }
        this.indicationDrawable.setState(getDrawableState());
    }

    public int getResolvedContentColor() {
        return this.resolvedContentColor;
    }

    public Style getStyle() {
        return this.style;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        RippleDrawable rippleDrawable = this.indicationDrawable;
        if (rippleDrawable != null) {
            rippleDrawable.jumpToCurrentState();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
    public void onDrawGlassContent(Canvas canvas) {
        super.onDrawGlassContent(canvas);
        GlassContinuousPathFactory.capsule(this.localPath, getWidth(), getHeight());
        if (this.style == Style.PROMINENT && isEnabled() && this.liquidGlassEnabled) {
            this.surfacePaint.setColor(this.accentColor);
            this.surfacePaint.setAlpha(255);
            if (Build.VERSION.SDK_INT >= 29) {
                Api29Blend.drawProminentHue(canvas, this.localPath, this.surfacePaint, this.accentColor);
            }
            this.surfacePaint.setColor(GlassControlUtils.multiplyAlpha(this.accentColor, 0.75f));
            canvas.drawPath(this.localPath, this.surfacePaint);
        } else if (Color.alpha(this.resolvedSurfaceColor) != 0) {
            this.surfacePaint.setColor(this.resolvedSurfaceColor);
            this.surfacePaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(this.localPath, this.surfacePaint);
        }
        if (this.liquidGlassEnabled) {
            if (isEnabled()) {
                this.interactiveHighlightRenderer.draw(canvas, this.localPath, getWidth(), getHeight(), this.interactiveHighlight);
                return;
            }
            return;
        }
        this.borderPaint.setStyle(Paint.Style.STROKE);
        this.borderPaint.setStrokeWidth(dp(0.75f));
        this.borderPaint.setColor(this.fallbackBorderColor);
        canvas.drawPath(this.localPath, this.borderPaint);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
    public void onDrawGlassForeground(Canvas canvas) {
        super.onDrawGlassForeground(canvas);
        RippleDrawable rippleDrawable = this.indicationDrawable;
        if (rippleDrawable != null) {
            rippleDrawable.draw(canvas);
        }
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5 = 0;
        int i6 = 0;
        for (int i7 = 0; i7 < getChildCount(); i7++) {
            View childAt = getChildAt(i7);
            if (childAt.getVisibility() != 8) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) childAt.getLayoutParams();
                i6 += childAt.getMeasuredWidth() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
                i5++;
            }
        }
        if (i5 > 1) {
            i6 += this.contentGap * (i5 - 1);
        }
        int paddingLeft = getPaddingLeft();
        int width = getWidth() - getPaddingRight();
        int i8 = ((width - paddingLeft) - i6) / 2;
        int max = paddingLeft + Math.max(0, i8);
        boolean isRtl = GlassControlUtils.isRtl(this);
        if (isRtl) {
            max = width - Math.max(0, i8);
        }
        for (int i9 = 0; i9 < getChildCount(); i9++) {
            View childAt2 = getChildAt(i9);
            if (childAt2.getVisibility() != 8) {
                ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) childAt2.getLayoutParams();
                int paddingTop = getPaddingTop() + Math.max(0, (((((getHeight() - getPaddingTop()) - getPaddingBottom()) - childAt2.getMeasuredHeight()) - marginLayoutParams2.topMargin) - marginLayoutParams2.bottomMargin) / 2) + marginLayoutParams2.topMargin;
                if (isRtl) {
                    int measuredWidth = max - (marginLayoutParams2.rightMargin + childAt2.getMeasuredWidth());
                    childAt2.layout(measuredWidth, paddingTop, childAt2.getMeasuredWidth() + measuredWidth, childAt2.getMeasuredHeight() + paddingTop);
                    max = measuredWidth - (marginLayoutParams2.leftMargin + this.contentGap);
                } else {
                    int i10 = max + marginLayoutParams2.leftMargin;
                    childAt2.layout(i10, paddingTop, childAt2.getMeasuredWidth() + i10, childAt2.getMeasuredHeight() + paddingTop);
                    max = i10 + childAt2.getMeasuredWidth() + marginLayoutParams2.rightMargin + this.contentGap;
                }
            }
        }
        updateButtonTransform();
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        for (int i6 = 0; i6 < getChildCount(); i6++) {
            View childAt = getChildAt(i6);
            if (childAt.getVisibility() != 8) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) childAt.getLayoutParams();
                childAt.measure(ViewGroup.getChildMeasureSpec(i, getPaddingLeft() + getPaddingRight() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin, marginLayoutParams.width), ViewGroup.getChildMeasureSpec(i2, getPaddingTop() + getPaddingBottom() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin, marginLayoutParams.height));
                i4 += childAt.getMeasuredWidth() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
                i5 = Math.max(i5, childAt.getMeasuredHeight() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin);
                i3++;
            }
        }
        if (i3 > 1) {
            i4 += this.contentGap * (i3 - 1);
        }
        setMeasuredDimension(View.resolveSizeAndState(getPaddingLeft() + i4 + getPaddingRight(), i, 0), View.resolveSizeAndState(Math.max(Math.round(dp(48.0f)), getPaddingTop() + i5 + getPaddingBottom()), i2, 0));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout, android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        RippleDrawable rippleDrawable = this.indicationDrawable;
        if (rippleDrawable != null) {
            rippleDrawable.setBounds(0, 0, i, i2);
        }
        updateButtonTransform();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (isEnabled()) {
            RippleDrawable rippleDrawable = this.indicationDrawable;
            if (rippleDrawable != null) {
                rippleDrawable.setHotspot(motionEvent.getX(), motionEvent.getY());
            }
            this.gestureDetector.onTouchEvent(motionEvent);
            return super.onTouchEvent(motionEvent);
        }
        return false;
    }

    @Override // android.view.ViewGroup
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        applyContentColorToChildren();
        requestLayout();
    }

    @Override // android.view.ViewGroup
    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        requestLayout();
    }

    public void setAccentColor(int i) {
        if (this.accentColor == i) {
            return;
        }
        this.accentColor = i;
        resolveAppearance();
    }

    @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
    public void setBackdropSource(GlassBackdropSource glassBackdropSource) {
        this.requestedBackdropSource = glassBackdropSource;
        if (!this.liquidGlassEnabled) {
            glassBackdropSource = null;
        }
        super.setBackdropSource(glassBackdropSource);
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
        if (!z) {
            this.gestureDetector.cancel();
            this.interactiveHighlight.cancel();
        }
        resolveAppearance();
    }

    public void setLiquidGlassEnabled(boolean z) {
        if (this.liquidGlassEnabled == z) {
            return;
        }
        this.liquidGlassEnabled = z;
        super.setBackdropSource(z ? this.requestedBackdropSource : null);
        resolveAppearance();
    }

    public void setStyle(Style style) {
        if (this.style == style) {
            return;
        }
        this.style = style;
        resolveAppearance();
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        return drawable == this.indicationDrawable || super.verifyDrawable(drawable);
    }
}
