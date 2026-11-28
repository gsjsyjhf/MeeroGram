/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.StateSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.Gravity;
import android.view.SoundEffectConstants;

import androidx.annotation.Keep;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.BaseCell;
import org.telegram.ui.Components.glass.controls.GlassIosSettingsSwitch;

import me.vkryl.android.animator.BoolAnimator;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;

public class Switch extends FrameLayout implements Theme.Colorable {

    @Override
    public void updateColors() {
        colorSet = 0; // the ripple color is re-read from the theme on the next press
        if (glassSwitch != null) {
            glassSwitch.setDarkTheme(Theme.isCurrentThemeDark());
        }
    }

    /*
     * Meerogram R12 - hosted iOS 26 "Liquid Glass" switch.
     *
     * Until now the iOS 26 style was a hand-drawn approximation inside onDraw
     * (fixed 200ms ObjectAnimator, no inner shadow, no spring, no glass rim).
     * The original reference component (GlassIosSettingsSwitch) was already
     * shipped in this tree but only used by LiteModeSettingsActivity, so every
     * other screen kept the approximation. When the iOS 26 style is enabled we
     * now host the real component inside this view: identical geometry
     * (64x28dp track, 40x24dp thumb), identical spring physics, identical
     * glass rim/parallax/haptics - i.e. the exact reference switch - while
     * taps keep being routed to the enclosing cell exactly like before.
     */
    private GlassIosSettingsSwitch glassSwitch;

    private final class MeeroGlassSwitch extends GlassIosSettingsSwitch {
        MeeroGlassSwitch(Context context) {
            super(context);
        }

        @Override
        public boolean performClick() {
            // A tap must keep driving the enclosing cell (dialogs, dependent
            // rows, settings persistence), never toggle the view in isolation.
            if (routeClickToHost()) {
                playSoundEffect(SoundEffectConstants.CLICK);
                return true;
            }
            return super.performClick();
        }
    }

    private boolean routeClickToHost() {
        for (ViewParent parent = getParent(); parent instanceof View; parent = ((View) parent).getParent()) {
            View view = (View) parent;
            if (view instanceof androidx.recyclerview.widget.RecyclerView || view instanceof ListView) {
                return false;
            }
            if (view.callOnClick()) {
                return true;
            }
        }
        return false;
    }

    /**
     * R14.6 - the reason every switch in the settings screens was cosmetic only.
     *
     * Row clicks are dispatched by RecyclerListView itself (it owns an
     * OnItemClickListener and calls it from its own touch handling); the cells
     * have no OnClickListener at all. The old code walked the parents calling
     * callOnClick(), which always returned false on these screens, and then fell
     * back to flipping the view locally - so the switch moved, the preference was
     * never written, the feature kept running and the value came back on relaunch.
     *
     * Here we ask the enclosing list to run the real row click for our position,
     * exactly as if the row (not the switch) had been tapped.
     */
    private boolean meeroClickRow() {
        try {
            for (ViewParent p = getParent(); p instanceof View; p = ((View) p).getParent()) {
                final View v = (View) p;
                if (v instanceof ListView && !(v instanceof RecyclerListView)) {
                    return false;
                }
                if (v instanceof RecyclerListView) {
                    final RecyclerListView list = (RecyclerListView) v;
                    View child = this;
                    while (child.getParent() != list && child.getParent() instanceof View) {
                        child = (View) child.getParent();
                    }
                    if (child.getParent() != list) {
                        return false;
                    }
                    final int position = list.getChildAdapterPosition(child);
                    if (position == androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                        return false;
                    }
                    list.clickItem(child, position);
                    return true;
                }
            }
        } catch (Throwable ignore) {
        }
        return false;
    }

    private boolean hostGlassSwitch = true;

    /**
     * R12.3 - some screens (LiteMode / Power saving) already place their own
     * GlassIosSettingsSwitch next to this view. Hosting a second one there is
     * pointless and was the source of the crash on that screen, so they opt out.
     */
    public void setHostGlassSwitch(boolean host) {
        if (hostGlassSwitch == host) {
            return;
        }
        hostGlassSwitch = host;
        updateGlassMode();
    }

    private void updateGlassMode() {
        // MeeroGram R14.31: the hosted glass switch is off. In the settings rows it
        // came out as a square green block with an oversized capsule knob clipped on
        // the right (the blur backdrop source is not available inside dialogs, so the
        // fillet path never loaded and the thumb kept its 24dp height). This view's
        // own onDraw already paints the iOS26 capsule correctly, so the hosted copy
        // is what gets dropped, not the style.
        final boolean want = false;
        if (want && glassSwitch == null) {
            final MeeroGlassSwitch hosted;
            try {
                hosted = new MeeroGlassSwitch(getContext());
            } catch (Throwable t) {
                // never let the hosted switch take the app down
                return;
            }
            // R13: the layout direction is left untouched - the knob mirrors
            // itself in RTL, and meeroGlassTouch() uses the very same direction
            // to translate finger movement, so drag always matches the visual.
            hosted.setDarkTheme(Theme.isCurrentThemeDark());
            hosted.setEnabled(isEnabled());
            hosted.setCheckedFromHost(isChecked, false);
            hosted.setOnCheckedChangeListener((view, checked) -> {
                isChecked = checked;
                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChanged(Switch.this, checked);
                } else if (!routeClickToHost()) {
                    postInvalidate();
                }
            });
            glassSwitch = hosted;
            setClipChildren(false);
            setClipToPadding(false);
            addView(hosted, new FrameLayout.LayoutParams(AndroidUtilities.dp(64), AndroidUtilities.dp(28), Gravity.CENTER));
            invalidate();
        } else if (!want && glassSwitch != null) {
            removeView(glassSwitch);
            glassSwitch = null;
            invalidate();
        }
    }
    private final BoolAnimator animatorIconVisibility = new BoolAnimator(this, CubicBezierInterpolator.EASE_OUT_QUINT, 380L, true);

    private RectF rectF;

    private float progress;
    private ObjectAnimator checkAnimator;
    private ObjectAnimator iconAnimator;

    private boolean attachedToWindow;
    private boolean isChecked;
    private Paint paint;
    private Paint paint2;
    private Paint paint3;
    private Paint paint4;
    private Paint paint5;

    private int drawIconType;
    private float iconProgress = 1.0f;

    private OnCheckedChangeListener onCheckedChangeListener;

    private int trackColorKey = Theme.key_fill_RedNormal;
    private int trackCheckedColorKey = Theme.key_switch2TrackChecked;
    private int thumbColorKey = Theme.key_windowBackgroundWhite;
    private int thumbCheckedColorKey = Theme.key_windowBackgroundWhite;

    private Drawable iconDrawable;
    private int lastIconColor;

    private boolean drawRipple;
    private RippleDrawable rippleDrawable;
    private Paint ripplePaint;
    private int[] pressedState = new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed};
    private int colorSet;

    private boolean bitmapsCreated;
    private Bitmap[] overlayBitmap;
    private Canvas[] overlayCanvas;
    private Bitmap overlayMaskBitmap;
    private Canvas overlayMaskCanvas;
    private float overlayCx;
    private float overlayCy;
    private float overlayRad;
    private Paint overlayEraserPaint;
    private Paint overlayMaskPaint;

    private Theme.ResourcesProvider resourcesProvider;

    private int overrideColorProgress;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(Switch view, boolean isChecked);
    }

    public Switch(Context context) {
        this(context, null);
    }

    public Switch(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        rectF = new RectF();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint4 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint5 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeCap(Paint.Cap.ROUND);
        paint2.setStrokeWidth(AndroidUtilities.dp(2));

        setHapticFeedbackEnabled(!CherrygramChatsConfig.INSTANCE.getDisableVibration());
        setWillNotDraw(false);
        updateGlassMode();
    }

    @Keep
    public void setProgress(float value) {
        if (progress == value) {
            return;
        }
        progress = value;
        invalidate();
    }

    @Keep
    public float getProgress() {
        return progress;
    }

    @Keep
    public void setIconProgress(float value) {
        if (iconProgress == value) {
            return;
        }
        iconProgress = value;
        invalidate();
    }

    @Keep
    public float getIconProgress() {
        return iconProgress;
    }

    private void cancelCheckAnimator() {
        if (checkAnimator != null) {
            checkAnimator.cancel();
            checkAnimator = null;
        }
    }

    private void cancelIconAnimator() {
        if (iconAnimator != null) {
            iconAnimator.cancel();
            iconAnimator = null;
        }
    }

    public void setDrawIconType(int type) {
        drawIconType = type;
    }

    public void setDrawRipple(boolean value) {
        if (Build.VERSION.SDK_INT < 21 || value == drawRipple) {
            return;
        }
        drawRipple = value;

        if (rippleDrawable == null) {
            ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ripplePaint.setColor(0xffffffff);
            Drawable maskDrawable;
            if (Build.VERSION.SDK_INT >= 23) {
                maskDrawable = null;
            } else {
                maskDrawable = new Drawable() {
                    @Override
                    public void draw(Canvas canvas) {
                        android.graphics.Rect bounds = getBounds();
                        canvas.drawCircle(bounds.centerX(), bounds.centerY(), AndroidUtilities.dp(18), ripplePaint);
                    }

                    @Override
                    public void setAlpha(int alpha) {

                    }

                    @Override
                    public void setColorFilter(ColorFilter colorFilter) {

                    }

                    @Override
                    public int getOpacity() {
                        return PixelFormat.UNKNOWN;
                    }
                };
            }
            ColorStateList colorStateList = new ColorStateList(
                new int[][]{StateSet.WILD_CARD},
                new int[]{0}
            );
            rippleDrawable = new BaseCell.RippleDrawableSafe(colorStateList, null, maskDrawable);
            if (Build.VERSION.SDK_INT >= 23) {
                rippleDrawable.setRadius(AndroidUtilities.dp(18));
            }
            rippleDrawable.setCallback(this);
        }
        if (isChecked && colorSet != 2 || !isChecked && colorSet != 1) {
            int color = Theme.getColor(isChecked ? Theme.key_switchTrackBlueSelectorChecked : Theme.key_switchTrackBlueSelector, resourcesProvider);
            color = processColor(color);
            ColorStateList colorStateList = new ColorStateList(
                new int[][]{StateSet.WILD_CARD},
                new int[]{color}
            );
            rippleDrawable.setColor(colorStateList);
            colorSet = isChecked ? 2 : 1;
        }
        if (Build.VERSION.SDK_INT >= 28 && value) {
            rippleDrawable.setHotspot(isChecked ? 0 : AndroidUtilities.dp(100), AndroidUtilities.dp(18));
        }
        rippleDrawable.setState(value ? pressedState : StateSet.NOTHING);
        invalidate();
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || rippleDrawable != null && who == rippleDrawable;
    }

    protected int processColor(int color) {
        return color;
    }

    public void setColors(int track, int trackChecked, int thumb, int thumbChecked) {
        trackColorKey = track;
        trackCheckedColorKey = trackChecked;
        thumbColorKey = thumb;
        thumbCheckedColorKey = thumbChecked;
    }

    private void animateToCheckedState(boolean newCheckedState) {
        checkAnimator = ObjectAnimator.ofFloat(this, "progress", newCheckedState ? 1 : 0);
        checkAnimator.setDuration(200);

        Interpolator interpolator = CubicBezierInterpolator.EASE_OUT_QUINT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ValueAnimator.getDurationScale() >= 1f) {
            interpolator = CubicBezierInterpolator.EASE_OUT_BACK;
        }
        checkAnimator.setInterpolator(interpolator);

        checkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                checkAnimator = null;
            }
        });
        checkAnimator.start();
    }

    private void animateIcon(boolean newCheckedState) {
        iconAnimator = ObjectAnimator.ofFloat(this, "iconProgress", newCheckedState ? 1 : 0);
        iconAnimator.setDuration(200);
        iconAnimator.setInterpolator(Easings.easeInOutQuad);
        iconAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                iconAnimator = null;
            }
        });
        iconAnimator.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
        updateGlassMode();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        onCheckedChangeListener = listener;
    }

    public void setChecked(boolean checked, boolean animated) {
        setChecked(checked, drawIconType, animated);
    }

    public void setChecked(boolean checked, int iconType, boolean animated) {
        if (glassSwitch != null) {
            if (checked != isChecked) {
                isChecked = checked;
                glassSwitch.setCheckedFromHost(checked, animated);
                if (attachedToWindow && animated && !CherrygramChatsConfig.INSTANCE.getDisableVibration()) {
                    vibrateChecked();
                }
                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChanged(this, checked);
                }
            }
            setDrawIconType(iconType, animated);
            return;
        }
        if (checked != isChecked) {
            isChecked = checked;
            if (attachedToWindow && animated) {
                if (!CherrygramChatsConfig.INSTANCE.getDisableVibration()) vibrateChecked();
                animateToCheckedState(checked);
            } else {
                cancelCheckAnimator();
                setProgress(checked ? 1.0f : 0.0f);
            }
            // R11.4: Ios26CapsuleSwitch LaunchedEffect(checked) - droplet rises
            // on external changes too, settles after 250ms.
            meeroOnExternalToggle(attachedToWindow && animated);
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged(this, checked);
            }
        }
        setDrawIconType(iconType, animated);
    }

    public void setIcon(int icon) {
        if (icon != 0) {
            iconDrawable = getResources().getDrawable(icon).mutate();
            if (iconDrawable != null) {
                iconDrawable.setColorFilter(new PorterDuffColorFilter(lastIconColor = Theme.getColor(isChecked ? trackCheckedColorKey : trackColorKey, resourcesProvider), PorterDuff.Mode.MULTIPLY));
            }
        } else {
            iconDrawable = null;
        }
        invalidate();
    }

    public void setIconVisible(boolean visible, boolean animated) {
        animatorIconVisibility.setValue(visible, animated);
    }

    public void setDrawIconType(int iconType, boolean animated) {
        if (drawIconType != iconType) {
            drawIconType = iconType;
            if (attachedToWindow && animated) {
                animateIcon(iconType == 0);
            } else {
                cancelIconAnimator();
                setIconProgress(iconType == 0 ? 1.0f : 0.0f);
            }
        }
    }

    public boolean hasIcon() {
        return iconDrawable != null;
    }

    public boolean isChecked() {
        return glassSwitch != null ? glassSwitch.isChecked() : isChecked;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (glassSwitch != null) {
            glassSwitch.setEnabled(enabled);
        }
    }

    public void setOverrideColor(int override) {
        if (overrideColorProgress == override) {
            return;
        }
        if (overlayBitmap == null) {
            try {
                overlayBitmap = new Bitmap[2];
                overlayCanvas = new Canvas[2];
                for (int a = 0; a < 2; a++) {
                    overlayBitmap[a] = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                    overlayCanvas[a] = new Canvas(overlayBitmap[a]);
                }
                overlayMaskBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                overlayMaskCanvas = new Canvas(overlayMaskBitmap);

                overlayEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                overlayEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

                overlayMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                overlayMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                bitmapsCreated = true;
            } catch (Throwable e) {
                return;
            }
        }
        if (!bitmapsCreated) {
            return;
        }
        overrideColorProgress = override;
        overlayCx = 0;
        overlayCy = 0;
        overlayRad = 0;
        invalidate();
    }

    public void setOverrideColorProgress(float cx, float cy, float rad) {
        overlayCx = cx;
        overlayCy = cy;
        overlayRad = rad;
        invalidate();
    }

    /**
     * MeeroGram R14.30: whether the styled switches have the room they were drawn for.
     *
     * Both Cherrygram styles paint a knob that is taller than the stock switch view
     * (OneUI 22dp, iOS26 24dp against Telegram's 20dp row) and the iOS26 track also
     * needs its full 63dp width. In the compact rows used by the settings sheets the
     * view is only ~39x20dp, so the knob was being clipped into a white block and the
     * iOS26 capsule was squeezed under its own knob - the broken toggles in the
     * conversation-shortcuts sheet. Where there is no room we fall back to stock.
     */
    private boolean meeroCanDrawOneUi() {
        // R14.31: the OneUI track is painted from its own x to the view's right edge
        // and its knob (22dp across) is centred off a fixed 30.5dp track, so in a
        // 39dp-wide cell the knob ran into the view bounds and was clipped into a
        // white blob on a green rectangle. Both dimensions have to leave room.
        return CherrygramAppearanceConfig.INSTANCE.getOneUI_SwitchStyle()
                && getMeasuredWidth() >= AndroidUtilities.dp(45)
                && getMeasuredHeight() >= AndroidUtilities.dp(26);
    }

    private boolean meeroCanDrawIos26() {
        // R14.32: the capsule draws itself at whatever size the cell hands it (see
        // meeroScale), so the only requirement is a box big enough to see it. That is
        // what makes the shortcut sheet's rows - which lay the switch out smaller than
        // the settings rows - look like the same switch as everywhere else.
        return CherrygramAppearanceConfig.INSTANCE.getIos26_SwitchStyle()
                && getMeasuredWidth() >= AndroidUtilities.dp(20)
                && getMeasuredHeight() >= AndroidUtilities.dp(12);
    }

    /** Scale of the iOS26 capsule against its native 63x28dp / 37x24dp knob. */
    private float meeroScale = 1f;
    private float meeroKnobHalfW = AndroidUtilities.dp(18.5f);
    private float meeroKnobHalfH = AndroidUtilities.dp(12f);
    private int meeroTrackWPx, meeroTrackHPx;

    @Override
    protected void onDraw(Canvas canvas) {
        if (glassSwitch != null) {
            // the hosted GlassIosSettingsSwitch paints the whole switch
            return;
        }
        if (getVisibility() != VISIBLE) {
            return;
        }

        int x;
        float y;
        int tx;
        int width = AndroidUtilities.dp(30.5F);
        int thumb;
        int meeroTrackW = 0, meeroTrackH = 0;

        if (meeroCanDrawOneUi()) {
            thumb = AndroidUtilities.dp(17.5F);
            x = (getMeasuredWidth() - width) / 2;
            y = getMeasuredHeight() / 2 - thumb / 2;

            float start = x + thumb / 2f + AndroidUtilities.dp(1);
            float end = x + width - thumb / 2f + AndroidUtilities.dp(2);

            tx = (int) (start + (end - start) * progress);
        } else if (meeroCanDrawIos26()) {
            // R11.3: real iOS26 keyboard switch - 63x28dp track, 37x24dp
            // capsule knob, 2dp pad, 22dp travel (Ios26CapsuleSwitch).
            final int maxH = Math.max(1, getMeasuredHeight());
            meeroTrackW = Math.min(AndroidUtilities.dp(63), getMeasuredWidth());
            meeroTrackH = Math.min(AndroidUtilities.dp(28), maxH);
            meeroScale = Math.min(meeroTrackW / (float) AndroidUtilities.dp(63),
                    meeroTrackH / (float) AndroidUtilities.dp(28));
            thumb = Math.round(AndroidUtilities.dp(37) * meeroScale);
            meeroKnobHalfW = AndroidUtilities.dp(18.5f) * meeroScale;
            meeroKnobHalfH = AndroidUtilities.dp(12f) * meeroScale;
            meeroTrackWPx = meeroTrackW;
            meeroTrackHPx = meeroTrackH;
            x = (getMeasuredWidth() - meeroTrackW) / 2;
            y = getMeasuredHeight() / 2 - meeroTrackH / 2;

            float start = x + meeroKnobHalfW + AndroidUtilities.dp(2) * meeroScale;
            float end = x + meeroTrackW - meeroKnobHalfW - AndroidUtilities.dp(2) * meeroScale;

            tx = (int) (start + (end - start) * progress);
            if (meeroPointerActive) {
                // R11.4: the knob follows the finger (rubber-banded thumb x)
                tx = (int) (start + meeroThumbX);
            }
        } else {
            thumb = AndroidUtilities.dp(20);
            x = (getMeasuredWidth() - width) / 2;
            y = (getMeasuredHeight() - AndroidUtilities.dpf2(14)) / 2;

            tx = x + AndroidUtilities.dp(7) + (int) (AndroidUtilities.dp(17) * progress);
        }

        int ty = getMeasuredHeight() / 2;


        int color1;
        int color2;
        int color3;
        int color4;
        int color5;
        float colorProgress;
        int r1;
        int r2;
        int g1;
        int g2;
        int b1;
        int b2;
        int a1;
        int a2;
        int red;
        int green;
        int blue;
        int alpha;
        int color;

        for (int a = 0; a < 2; a++) {
            if (a == 1 && overrideColorProgress == 0) {
                continue;
            }
            Canvas canvasToDraw = a == 0 ? canvas : overlayCanvas[0];

            if (a == 1) {
                overlayBitmap[0].eraseColor(0);
                paint.setColor(0xff000000);
                overlayMaskCanvas.drawRect(0, 0, overlayMaskBitmap.getWidth(), overlayMaskBitmap.getHeight(), paint);
                overlayMaskCanvas.drawCircle(overlayCx - getX(), overlayCy - getY(), overlayRad, overlayEraserPaint);
            }
            if (overrideColorProgress == 1) {
                colorProgress = a == 0 ? 0 : 1;
            } else if (overrideColorProgress == 2) {
                colorProgress = a == 0 ? 1 : 0;
            } else {
                colorProgress = progress;
            }

            color1 = processColor(Theme.getColor(trackColorKey, resourcesProvider));
            color2 = processColor(Theme.getColor(trackCheckedColorKey, resourcesProvider));
            color3 = processColor(Theme.getColor(Theme.key_alwaysWhite, resourcesProvider));
            color4 = processColor(Theme.getColor(Theme.key_alwaysGray, resourcesProvider));
            color5 = processColor(Theme.getColor(Theme.key_alwaysGrayDarkTheme, resourcesProvider));

            if (a == 0 && iconDrawable != null && lastIconColor != (isChecked ? color2 : color1)) {
                iconDrawable.setColorFilter(new PorterDuffColorFilter(lastIconColor = (isChecked ? color2 : color1), PorterDuff.Mode.MULTIPLY));
            }

            r1 = Color.red(color1);
            r2 = Color.red(color2);
            g1 = Color.green(color1);
            g2 = Color.green(color2);
            b1 = Color.blue(color1);
            b2 = Color.blue(color2);
            a1 = Color.alpha(color1);
            a2 = Color.alpha(color2);

            red = (int) (r1 + (r2 - r1) * colorProgress);
            green = (int) (g1 + (g2 - g1) * colorProgress);
            blue = (int) (b1 + (b2 - b1) * colorProgress);
            alpha = (int) (a1 + (a2 - a1) * colorProgress);
            color = ((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
            paint.setColor(color);
            paint2.setColor(color);
            paint3.setColor(color3);
            paint4.setColor(color4);
            paint5.setColor(color5);

            if (meeroCanDrawOneUi()) {
                rectF.set(x, y, getMeasuredWidth(), getMeasuredHeight() / 2 + thumb / 2);
                if (!isChecked && Color.alpha(color) < 255) {
                    Paint grayPaint = Theme.isCurrentThemeDark() || Theme.isCurrentThemeNight() ? paint5 : paint4;
                    canvasToDraw.drawRoundRect(rectF, AndroidUtilities.dpf2(11), AndroidUtilities.dpf2(11), grayPaint);
                    canvasToDraw.drawCircle(tx, ty, AndroidUtilities.dpf2(11), grayPaint);
                }
                canvasToDraw.drawRoundRect(rectF, AndroidUtilities.dpf2(11), AndroidUtilities.dpf2(11), paint);
                canvasToDraw.drawCircle(tx, ty, AndroidUtilities.dpf2(11), paint);
            } else if (meeroCanDrawIos26()) {
                // R11.4: Ios26CapsuleSwitch track - fixed iOS palette (off
                // 0xFF787880 @38% dark / 24% light, on IosGreen 0xFF34C759),
                // inner specular rim white 16% (inset 1dp), iOS micro-indicators
                // (white bar when on, grey ring when off) visible through the
                // glass droplet. The blend factor follows the finger while
                // dragging (thumbX / travel), colorProgress otherwise.
                final int meeroX = (getMeasuredWidth() - meeroTrackW) / 2;
                final int meeroY = getMeasuredHeight() / 2 - meeroTrackH / 2;
                final boolean meeroDark = Theme.isCurrentThemeDark() || Theme.isCurrentThemeNight();
                final float meeroColorT = meeroPointerActive
                        ? Math.max(0f, Math.min(1f, meeroThumbX / meeroMaxTravel()))
                        : colorProgress;
                if (a == 0 && overrideColorProgress == 0) {
                    paint.setColor(meeroBlend(
                            Theme.multAlpha(0xFF787880, meeroDark ? 0x61 : 0x3D),
                            0xFF34C759, meeroColorT));
                }
                rectF.set(meeroX, meeroY, meeroX + meeroTrackW, meeroY + meeroTrackH);
                canvasToDraw.drawRoundRect(rectF, meeroTrackHPx / 2f, meeroTrackHPx / 2f, paint);
                Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                rimPaint.setStyle(Paint.Style.STROKE);
                rimPaint.setStrokeWidth(Math.max(1, AndroidUtilities.dp(1)));
                rimPaint.setColor(0x29FFFFFF);
                rectF.set(meeroX + 1, meeroY + 1, meeroX + meeroTrackW - 1, meeroY + meeroTrackH - 1);
                canvasToDraw.drawRoundRect(rectF, Math.max(0f, meeroTrackHPx / 2f - 1f), Math.max(0f, meeroTrackHPx / 2f - 1f), rimPaint);
                // R11.5: the iOS micro-indicators (on = white bar, off = grey
                // ring) are removed - users read them as stray "O"/"I" glyphs
                // on the switch. The track stays a clean iOS capsule.
                rimPaint.setStyle(Paint.Style.FILL);
            } else {
                rectF.set(x, y, x + width, y + AndroidUtilities.dpf2(14));
                canvasToDraw.drawRoundRect(rectF, AndroidUtilities.dpf2(7), AndroidUtilities.dpf2(7), paint);
                canvasToDraw.drawCircle(tx, ty, AndroidUtilities.dpf2(10), paint);
            }

            if (a == 0 && rippleDrawable != null) {
                rippleDrawable.setBounds(tx - AndroidUtilities.dp(18), ty - AndroidUtilities.dp(18), tx + AndroidUtilities.dp(18), ty + AndroidUtilities.dp(18));
                rippleDrawable.draw(canvasToDraw);
            } else if (a == 1) {
                canvasToDraw.drawBitmap(overlayMaskBitmap, 0, 0, overlayMaskPaint);
            }
        }
        if (overrideColorProgress != 0) {
            canvas.drawBitmap(overlayBitmap[0], 0, 0, null);
        }

        for (int a = 0; a < 2; a++) {
            if (a == 1 && overrideColorProgress == 0) {
                continue;
            }
            Canvas canvasToDraw = a == 0 ? canvas : overlayCanvas[1];

            if (a == 1) {
                overlayBitmap[1].eraseColor(0);
            }
            if (overrideColorProgress == 1) {
                colorProgress = a == 0 ? 0 : 1;
            } else if (overrideColorProgress == 2) {
                colorProgress = a == 0 ? 1 : 0;
            } else {
                colorProgress = progress;
            }

            color1 = Theme.getColor(thumbColorKey, resourcesProvider);
            color2 = processColor(Theme.getColor(thumbCheckedColorKey, resourcesProvider));
            r1 = Color.red(color1);
            r2 = Color.red(color2);
            g1 = Color.green(color1);
            g2 = Color.green(color2);
            b1 = Color.blue(color1);
            b2 = Color.blue(color2);
            a1 = Color.alpha(color1);
            a2 = Color.alpha(color2);

            red = (int) (r1 + (r2 - r1) * colorProgress);
            green = (int) (g1 + (g2 - g1) * colorProgress);
            blue = (int) (b1 + (b2 - b1) * colorProgress);
            alpha = (int) (a1 + (a2 - a1) * colorProgress);
            paint.setColor(((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff));

            if (meeroCanDrawOneUi()) {
                canvasToDraw.drawCircle(tx, ty, AndroidUtilities.dp(9.5F), paint3);
            } else if (meeroCanDrawIos26()) {
                // R11.4: Ios26CapsuleSwitch knob - the Liquid Glass droplet rises
                // while the finger touches/drags (glassFrac) and on external
                // toggles, exactly like the uploaded keyboard source (native
                // style): swells (1+0.34g)*(1+0.46g) over the track edges,
                // translucent white (alpha 1-0.84g), liquid surface gradient,
                // upper specular arc, chromatic caustic rim, drop shadow.
                final boolean meeroDark = Theme.isCurrentThemeDark() || Theme.isCurrentThemeNight();
                final float g = meeroGlassFrac;
                final float swellX = (1f + 0.34f * g) * meeroStretchX;
                final float swellY = (1f + 0.46f * g) * meeroStretchY;
                final float hw = meeroKnobHalfW * swellX;
                final float hh = meeroKnobHalfH * swellY;
                if (g > 0.02f) {
                    canvas.save();
                    canvas.translate(tx, ty);
                    canvas.scale(swellX, swellY);
                    // soft drop shadow (elevation 2.5 + 6.5g dp, dark 55% / light 24%)
                    Paint shPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    final float shadowA = (meeroDark ? 0.40f : 0.18f) * Math.min(1f, 0.35f + 0.65f * g);
                    shPaint.setShader(new android.graphics.RadialGradient(0, AndroidUtilities.dp(1.5f),
                            meeroKnobHalfW * 1.03f,
                            new int[]{Theme.multAlpha(0xFF000000, (int) (255f * shadowA)), 0},
                            new float[]{0.55f, 1f}, android.graphics.Shader.TileMode.CLAMP));
                    canvasToDraw.drawCircle(0, AndroidUtilities.dp(1.5f), meeroKnobHalfW * 1.03f, shPaint);
                    // solid white fading out as the droplet takes over
                    Paint knobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    knobPaint.setColor(paint3.getColor());
                    knobPaint.setAlpha((int) (255f * (1f - 0.84f * g)));
                    android.graphics.RectF knobRect = new android.graphics.RectF(-meeroKnobHalfW, -meeroKnobHalfH, meeroKnobHalfW, meeroKnobHalfH);
                    canvasToDraw.drawRoundRect(knobRect, meeroKnobHalfH, meeroKnobHalfH, knobPaint);
                    // liquid surface gradient (native style: white tints)
                    Paint dropPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    dropPaint.setShader(new android.graphics.LinearGradient(0, -hh, 0, hh,
                            new int[]{
                                    Theme.multAlpha(0xFFFFFFFF, (int) (255f * (meeroDark ? 0.28f : 0.38f) * g)),
                                    Theme.multAlpha(0xFFFFFFFF, (int) (255f * 0.08f * g)),
                                    Theme.multAlpha(0xFFFFFFFF, (int) (255f * (meeroDark ? 0.20f : 0.28f) * g)),
                            },
                            new float[]{0f, 0.48f, 1f}, android.graphics.Shader.TileMode.CLAMP));
                    canvasToDraw.drawRoundRect(knobRect, meeroKnobHalfH, meeroKnobHalfH, dropPaint);
                    // upper specular liquid highlight arc
                    Paint hiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    hiPaint.setShader(new android.graphics.LinearGradient(0, -hh, 0, 0f,
                            new int[]{Theme.multAlpha(0xFFFFFFFF, (int) (255f * 0.90f * g)), Theme.multAlpha(0xFFFFFFFF, (int) (255f * 0.05f * g))},
                            new float[]{0f, 1f}, android.graphics.Shader.TileMode.CLAMP));
                    android.graphics.RectF hiRect = new android.graphics.RectF(-meeroKnobHalfW * 0.84f, -meeroKnobHalfH * 0.92f, meeroKnobHalfW * 0.84f, -hh * 0.06f);
                    canvasToDraw.drawRoundRect(hiRect, meeroKnobHalfH * 0.875f, meeroKnobHalfH * 0.875f, hiPaint);
                    // chromatic caustic rim (cyan -> white -> green)
                    Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    rimPaint.setStyle(Paint.Style.STROKE);
                    rimPaint.setStrokeWidth(Math.max(1, AndroidUtilities.dp(1.25f)));
                    rimPaint.setShader(new android.graphics.LinearGradient(-hw, 0, hw, 0,
                            new int[]{
                                    Theme.multAlpha(0xFF00B4FF, (int) (255f * 0.28f * g)),
                                    Theme.multAlpha(0xFFFFFFFF, (int) (255f * 0.92f * g)),
                                    Theme.multAlpha(0xFF34C759, (int) (255f * 0.30f * g)),
                            },
                            new float[]{0f, 0.5f, 1f}, android.graphics.Shader.TileMode.CLAMP));
                    canvasToDraw.drawRoundRect(knobRect, meeroKnobHalfH, meeroKnobHalfH, rimPaint);
                    canvas.restore();
                } else {
                    android.graphics.RectF knobRect = new android.graphics.RectF(tx - meeroKnobHalfW, ty - meeroKnobHalfH, tx + meeroKnobHalfW, ty + meeroKnobHalfH);
                    canvasToDraw.drawRoundRect(knobRect, meeroKnobHalfH, meeroKnobHalfH, paint3);
                    Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    rimPaint.setStyle(Paint.Style.STROKE);
                    rimPaint.setStrokeWidth(Math.max(1, AndroidUtilities.dp(0.8f)));
                    rimPaint.setColor(0xF5FFFFFF);
                    canvasToDraw.drawRoundRect(knobRect, meeroKnobHalfH, meeroKnobHalfH, rimPaint);
                }
            } else {
                canvasToDraw.drawCircle(tx, ty, AndroidUtilities.dp(8), paint);
            }

            if (a == 0) {
                if (iconDrawable != null) {
                    final float factor = animatorIconVisibility.getFloatValue();
                    if (factor > 0) {
                        final boolean needScale = factor < 1;
                        if (needScale) {
                            canvas.save();
                            canvas.scale(factor, factor, tx, ty);
                        }
                        iconDrawable.setBounds(tx - iconDrawable.getIntrinsicWidth() / 2, ty - iconDrawable.getIntrinsicHeight() / 2, tx + iconDrawable.getIntrinsicWidth() / 2, ty + iconDrawable.getIntrinsicHeight() / 2);
                        iconDrawable.draw(canvasToDraw);
                        if (needScale) {
                            canvas.restore();
                        }
                    }
                } else if (drawIconType == 1) {
                    tx -= AndroidUtilities.dp(10.8f) - AndroidUtilities.dp(1.3f) * progress;
                    ty -= AndroidUtilities.dp(8.5f) - AndroidUtilities.dp(0.5f) * progress;
                    int startX2 = (int) AndroidUtilities.dpf2(4.6f) + tx;
                    int startY2 = (int) (AndroidUtilities.dpf2(9.5f) + ty);
                    int endX2 = startX2 + AndroidUtilities.dp(2);
                    int endY2 = startY2 + AndroidUtilities.dp(2);

                    int startX = (int) AndroidUtilities.dpf2(7.5f) + tx;
                    int startY = (int) AndroidUtilities.dpf2(5.4f) + ty;
                    int endX = startX + AndroidUtilities.dp(7);
                    int endY = startY + AndroidUtilities.dp(7);

                    startX = (int) (startX + (startX2 - startX) * progress);
                    startY = (int) (startY + (startY2 - startY) * progress);
                    endX = (int) (endX + (endX2 - endX) * progress);
                    endY = (int) (endY + (endY2 - endY) * progress);
                    canvasToDraw.drawLine(startX, startY, endX, endY, paint2);

                    startX = (int) AndroidUtilities.dpf2(7.5f) + tx;
                    startY = (int) AndroidUtilities.dpf2(12.5f) + ty;
                    endX = startX + AndroidUtilities.dp(7);
                    endY = startY - AndroidUtilities.dp(7);
                    canvasToDraw.drawLine(startX, startY, endX, endY, paint2);
                } else if (drawIconType == 2 || iconAnimator != null) {
                    paint2.setAlpha((int) (255 * (1.0f - iconProgress)));
                    canvasToDraw.drawLine(tx, ty, tx, ty - AndroidUtilities.dp(5), paint2);
                    canvasToDraw.save();
                    canvasToDraw.rotate(-90 * iconProgress, tx, ty);
                    canvasToDraw.drawLine(tx, ty, tx + AndroidUtilities.dp(4), ty, paint2);
                    canvasToDraw.restore();
                }
            }
            if (a == 1) {
                canvasToDraw.drawBitmap(overlayMaskBitmap, 0, 0, overlayMaskPaint);
            }
        }
        if (overrideColorProgress != 0) {
            canvas.drawBitmap(overlayBitmap[1], 0, 0, null);
        }
    }

    // ================= R11.4: Ios26CapsuleSwitch finger interaction =================
    // Exact pointer semantics from the uploaded keyboard source
    // (ui/Ios26Components.kt Ios26CapsuleSwitch pointerInput).
    private boolean meeroPointerActive;
    private boolean meeroMoved;
    private boolean meeroDragPreviewOn;
    private float meeroStartPointerX;
    private float meeroLastPointerX;
    private float meeroThumbX;
    private float meeroStretchX = 1f, meeroStretchY = 1f;
    private float meeroGlassFrac;
    private ObjectAnimator meeroGlassAnimator;
    private ObjectAnimator meeroStretchXAnimator;
    private ObjectAnimator meeroStretchYAnimator;
    private Runnable meeroGlassSettle;

    @Keep
    public void setMeeroGlassFrac(float v) {
        meeroGlassFrac = v;
        invalidate();
    }

    @Keep
    public float getMeeroGlassFrac() {
        return meeroGlassFrac;
    }

    @Keep
    public void setMeeroStretchX(float v) {
        meeroStretchX = v;
        invalidate();
    }

    @Keep
    public float getMeeroStretchX() {
        return meeroStretchX;
    }

    @Keep
    public void setMeeroStretchY(float v) {
        meeroStretchY = v;
        invalidate();
    }

    @Keep
    public float getMeeroStretchY() {
        return meeroStretchY;
    }

    private void meeroGlassTo(float target, long duration) {
        if (meeroGlassAnimator != null) {
            meeroGlassAnimator.cancel();
            meeroGlassAnimator = null;
        }
        meeroGlassAnimator = ObjectAnimator.ofFloat(this, "meeroGlassFrac", target);
        meeroGlassAnimator.setDuration(duration);
        meeroGlassAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        meeroGlassAnimator.start();
    }

    private void meeroStretchTo(float sx, float sy, long duration) {
        if (meeroStretchXAnimator != null) {
            meeroStretchXAnimator.cancel();
        }
        if (meeroStretchYAnimator != null) {
            meeroStretchYAnimator.cancel();
        }
        meeroStretchXAnimator = ObjectAnimator.ofFloat(this, "meeroStretchX", sx);
        meeroStretchYAnimator = ObjectAnimator.ofFloat(this, "meeroStretchY", sy);
        meeroStretchXAnimator.setDuration(duration);
        meeroStretchYAnimator.setDuration(duration);
        meeroStretchXAnimator.start();
        meeroStretchYAnimator.start();
    }

    /** 63dp track, 37dp thumb, 2dp pad each side -> 22dp travel (Ios26CapsuleSwitch). */
    private float meeroMaxTravel() {
        final float trackW = Math.min(AndroidUtilities.dp(63), getMeasuredWidth());
        final float trackH = Math.min(AndroidUtilities.dp(28), Math.max(1, getMeasuredHeight()));
        final float scale = Math.min(trackW / (float) AndroidUtilities.dp(63), trackH / (float) AndroidUtilities.dp(28));
        return Math.max(AndroidUtilities.dp(1), trackW - AndroidUtilities.dp(37) * scale - AndroidUtilities.dp(4) * scale);
    }

    private static int meeroBlend(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        final int r = (int) (Color.red(c1) + (Color.red(c2) - Color.red(c1)) * t);
        final int g = (int) (Color.green(c1) + (Color.green(c2) - Color.green(c1)) * t);
        final int b = (int) (Color.blue(c1) + (Color.blue(c2) - Color.blue(c1)) * t);
        final int a = (int) (Color.alpha(c1) + (Color.alpha(c2) - Color.alpha(c1)) * t);
        return Color.argb(a, r, g, b);
    }

    /** External toggle (cell click or programmatic): droplet rises then settles. */
    private void meeroOnExternalToggle(boolean animated) {
        if (!CherrygramAppearanceConfig.INSTANCE.getIos26_SwitchStyle()) {
            return;
        }
        if (!animated || !attachedToWindow) {
            if (!meeroPointerActive) {
                meeroGlassTo(0f, 120);
                meeroStretchTo(1f, 1f, 200);
            }
            return;
        }
        meeroGlassTo(1f, 110);
        meeroStretchTo(1.06f, 0.98f, 90);
        if (meeroGlassSettle != null) {
            removeCallbacks(meeroGlassSettle);
        }
        meeroGlassSettle = () -> {
            if (!meeroPointerActive) {
                meeroStretchTo(1f, 1f, 250);
                meeroGlassTo(0f, 200);
            }
        };
        postDelayed(meeroGlassSettle, 250);
    }

    /*
     * R12.2 - drag fix.
     *
     * The hosted GlassIosSettingsSwitch owns its own gesture detector, but as a
     * child of this injected FrameLayout it never became the touch target: the
     * host took the gesture and the knob could not be dragged (tapping the row
     * still toggled, which made the switch look "dead"). We now intercept the
     * gesture here and forward every event to the hosted switch with translated
     * coordinates, so its reference drag/spring/haptics run exactly as in the
     * MR build.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (glassSwitch != null) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    /*
     * R13 - drag fix.
     *
     * R12.2 forwarded the raw MotionEvents into the hosted switch, which made
     * the knob only follow the finger after a horizontal mirror that depends on
     * the resolved layout direction (in Arabic it came out inverted: drag right,
     * knob goes left). Switch now tracks the finger itself - same slop, same
     * rubber band and same 50% commit threshold as the reference component -
     * and only pushes the resulting progress into it, so the knob always travels
     * with the finger and the glass look/spring/haptics stay 100% the reference.
     */
    private boolean meeroGlassTouch(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                meeroPointerActive = true;
                meeroMoved = false;
                meeroStartPointerX = meeroLastPointerX = event.getX();
                meeroThumbX = isChecked ? AndroidUtilities.dp(20) : 0f;
                // R13.2: without this the thumb stays the flat resting pill and
                // only flashes glass on tap - the drag must run the same press
                // lifecycle as the reference gesture detector.
                glassSwitch.hostDragBegin();
                final ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!meeroPointerActive) {
                    return true;
                }
                final float travel = Math.max(1f, AndroidUtilities.dp(20));
                final float dir = glassSwitch.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? -1f : 1f;
                final float dx = (event.getX() - meeroStartPointerX) * dir;
                if (!meeroMoved && Math.abs(dx) > AndroidUtilities.dpf2(4f)) {
                    meeroMoved = true;
                }
                if (meeroMoved) {
                    float next = (isChecked ? travel : 0f) + dx;
                    if (next < 0f) {
                        next *= 0.18f;
                    }
                    if (next > travel) {
                        next = travel + (next - travel) * 0.18f;
                    }
                    meeroThumbX = next;
                    glassSwitch.dragTo(next / travel);
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (!meeroPointerActive) {
                    return true;
                }
                meeroPointerActive = false;
                glassSwitch.hostDragEnd();
                final boolean wasMoved = meeroMoved;
                final boolean wasCancelled = event.getActionMasked() == MotionEvent.ACTION_CANCEL;
                meeroMoved = false;
                final float travel = Math.max(1f, AndroidUtilities.dp(20));
                final boolean nextChecked = wasCancelled
                        ? isChecked
                        : (!wasMoved ? !isChecked : meeroThumbX >= travel * 0.5f);
                if (!wasCancelled && nextChecked != isChecked) {
                    // R14.6: let the list run the real row click (this is what
                    // actually stores the preference); only if that is impossible
                    // fall back to the old ancestor walk.
                    if (meeroClickRow()) {
                        glassSwitch.setCheckedFromHost(isChecked, true);
                        return true;
                    }
                    // route through the enclosing cell so every screen keeps its
                    // exact behaviour (dialogs, dependent rows, persistence)
                    boolean handled = false;
                    for (ViewParent q = getParent(); q instanceof View; q = ((View) q).getParent()) {
                        final View v = (View) q;
                        if (v instanceof androidx.recyclerview.widget.RecyclerView || v instanceof android.widget.ListView) {
                            break;
                        }
                        if (v.callOnClick()) {
                            handled = true;
                            break;
                        }
                    }
                    if (!handled) {
                        setChecked(!isChecked, true);
                    }
                    glassSwitch.setCheckedFromHost(nextChecked, true);
                } else {
                    glassSwitch.setCheckedFromHost(isChecked, true);
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (glassSwitch != null) {
            return meeroGlassTouch(event);
        }
        if (!CherrygramAppearanceConfig.INSTANCE.getIos26_SwitchStyle() || getVisibility() != VISIBLE || !isEnabled()) {
            return super.onTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                meeroPointerActive = true;
                meeroMoved = false;
                meeroStartPointerX = meeroLastPointerX = event.getX();
                meeroThumbX = isChecked ? meeroMaxTravel() : 0f;
                meeroDragPreviewOn = isChecked;
                meeroGlassTo(1f, 120);
                meeroStretchTo(1.02f, 1.02f, 80);
                if (meeroGlassSettle != null) {
                    removeCallbacks(meeroGlassSettle);
                }
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!meeroPointerActive) {
                    break;
                }
                final float dx = event.getX() - meeroStartPointerX;
                if (!meeroMoved && Math.abs(dx) > AndroidUtilities.dpf2(4f)) {
                    meeroMoved = true;
                }
                if (meeroMoved) {
                    final float max = meeroMaxTravel();
                    float next = (isChecked ? max : 0f) + dx;
                    if (next < 0f) {
                        next *= 0.18f;
                    }
                    if (next > max) {
                        next = max + (next - max) * 0.18f;
                    }
                    meeroThumbX = next;
                    meeroDragPreviewOn = meeroThumbX >= max * 0.5f;
                    final float vx = event.getX() - meeroLastPointerX;
                    final float sx = 1f + Math.min(Math.abs(vx) * 0.012f, 0.14f);
                    meeroStretchX = sx;
                    meeroStretchY = (float) (1f / Math.sqrt(sx));
                    meeroLastPointerX = event.getX();
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (!meeroPointerActive) {
                    break;
                }
                meeroPointerActive = false;
                final boolean wasMoved = meeroMoved;
                final boolean wasCancelled = event.getActionMasked() == MotionEvent.ACTION_CANCEL;
                meeroMoved = false;
                // R11.5 exact Ios26CapsuleSwitch semantics: a tap (no move)
                // TOGGLES (!latestChecked); a drag commits at the 50% mark;
                // a system-cancelled gesture never toggles - it glides home.
                final float max = meeroMaxTravel();
                final boolean nextChecked = wasCancelled || !wasMoved
                        ? (wasCancelled ? isChecked : !isChecked)
                        : meeroThumbX >= max * 0.5f;
                if (wasMoved) {
                    // hand the knob over from the finger position: snap the
                    // progress to the visual drag point, then let the progress
                    // animator glide it to the decided state (spring-back or
                    // commit) - the knob never teleports back to its rest.
                    cancelCheckAnimator();
                    setProgress(Math.max(0f, Math.min(1f, meeroThumbX / max)));
                }
                meeroStretchTo(1f, 1f, 260);
                if (meeroGlassSettle != null) {
                    removeCallbacks(meeroGlassSettle);
                }
                meeroGlassSettle = () -> {
                    if (!meeroPointerActive) {
                        meeroGlassTo(0f, 200);
                    }
                };
                postDelayed(meeroGlassSettle, wasMoved ? 130 : 250);
                if (!wasCancelled && (!wasMoved || nextChecked != isChecked) && meeroClickRow()) {
                    // R14.6: the list handled the real row click itself.
                    invalidate();
                    return true;
                } else if (!wasCancelled && (!wasMoved || nextChecked != isChecked)) {
                    // route through the enclosing cell's click handler so every
                    // screen keeps its exact behavior (dialogs, dependencies...)
                    // R11.8b: the direct parent can be a plain grouped-card
                    // container without any click listener (iOS26 settings
                    // cards) - performClick on it was a silent no-op and taps
                    // stayed dead. Walk up to the NEAREST ancestor that really
                    // has a click listener, calling it there; never cross a
                    // scrolling parent. callOnClick() is the public equivalent
                    // of the @hide hasOnClickListener(): it fires only when a
                    // listener exists and reports whether it ran.
                    boolean handled = false;
                    for (ViewParent q = getParent(); q instanceof View; q = ((View) q).getParent()) {
                        final View v = (View) q;
                        if (v instanceof androidx.recyclerview.widget.RecyclerView || v instanceof android.widget.ListView) {
                            break;
                        }
                        if (v.callOnClick()) {
                            handled = true;
                            break;
                        }
                    }
                    if (!handled) {
                        // no reachable cell listener: still give the finger
                        // feedback instead of a dead tap
                        setChecked(!isChecked, true);
                    }
                } else if (wasMoved) {
                    // drag released without committing: glide back to the
                    // unchanged state from the finger position
                    animateToCheckedState(isChecked);
                }
                invalidate();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.Switch");
        info.setCheckable(true);
        info.setChecked(isChecked);
        //info.setContentDescription(isChecked ? LocaleController.getString(R.string.NotificationsOn) : LocaleController.getString(R.string.NotificationsOff));
    }

    private void vibrateChecked() {
        try {
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        } catch (Exception ignore) {}
    }
}
