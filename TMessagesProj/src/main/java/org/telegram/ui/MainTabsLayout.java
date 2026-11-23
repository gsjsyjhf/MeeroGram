package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.lerp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import android.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedLinearLayout;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.glass.GlassTabView;
import org.telegram.ui.Components.blur3.BlurredBackgroundDrawableViewFactory;
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;
import org.telegram.ui.Components.blur3.drawable.color.impl.BlurredBackgroundProviderImpl;

import java.util.HashSet;
import java.util.Set;

import me.vkryl.android.animator.BoolAnimator;
import me.vkryl.android.animator.ListAnimator;
import me.vkryl.android.util.ClickHelper;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.core.ui.mainTabs.MeeroTabDragHelper;

@SuppressLint("ViewConstructor")
public class MainTabsLayout extends AnimatedLinearLayout {

    private final Theme.ResourcesProvider resourcesProvider;

    public MainTabsLayout(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
    }

    private static final float[] PASS_TEXT_SIZES_DP = {12f, 12f, 10f};
    private static final int[] PASS_PADDINGS_DP = {16, 8, 4};

    private int maxWidthPx;

    public void setMaxWidth(int maxWidthPx) {
        if (this.maxWidthPx != maxWidthPx) {
            this.maxWidthPx = maxWidthPx;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isForMainTabsCG) {
            onMeasureMainTabsCG(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int tabHeight = height - getPaddingTop() - getPaddingBottom();

        if (maxWidthPx > 0 && width > maxWidthPx) {
            width = maxWidthPx;
        }

        final int maxTotalWidthForTabs = width - getPaddingLeft() - getPaddingRight();
        final int minTotalWidthForTabs = Math.min(dp(320), maxTotalWidthForTabs);

        int chosenPass = PASS_TEXT_SIZES_DP.length - 1;
        float lastMeasuredTextSize = -1;
        for (int pass = 0; pass < PASS_TEXT_SIZES_DP.length; pass++) {
            if (PASS_TEXT_SIZES_DP[pass] != lastMeasuredTextSize) {
                measureTabTexts(PASS_TEXT_SIZES_DP[pass]);
                lastMeasuredTextSize = PASS_TEXT_SIZES_DP[pass];
            }
            final int padding = dp(PASS_PADDINGS_DP[pass]);
            float total = 0;
            for (int a = 0, N = getChildCount(); a < N; a++) {
                if (!isViewVisible(getChildAt(a))) continue;
                final float withMargin = tabsTextWidth[a] + padding * 2;
                total += withMargin;
            }
            final boolean fits = total <= maxTotalWidthForTabs;
            if (fits || pass == PASS_TEXT_SIZES_DP.length - 1) {
                chosenPass = pass;
                break;
            }
        }

        applyPassTextSize(chosenPass);

        final int tabPadding = dp(PASS_PADDINGS_DP[chosenPass]);
        final int maxTabTextWidthIfEq = (maxTotalWidthForTabs / Math.max(1, visibleChildCount)) - tabPadding * 2;

        float totalWidth = 0;
        int totalWeight = 0;
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            if (!isViewVisible(child)) {
                tabsTextWidth[a] = tabsTextWidthWithMargin[a] = 0;
                tabsWeight[a] = 0;
                continue;
            }

            tabsTextWidthWithMargin[a] = tabsTextWidth[a] + tabPadding * 2;
            tabsWeight[a] = tabsTextWidthWithMargin[a] > (maxTabTextWidthIfEq + tabPadding * 2) ? 0 : 1;

            totalWidth += tabsTextWidthWithMargin[a];
            totalWeight += tabsWeight[a];
        }

        if (totalWeight == 0) {
            for (int a = 0, N = getChildCount(); a < N; a++) {
                tabsWeight[a] = isViewVisible(getChildAt(a)) ? 1 : 0;
            }
            totalWeight = visibleChildCount;
        }

        if (totalWidth > maxTotalWidthForTabs) {
            final float m = maxTotalWidthForTabs / totalWidth;
            for (int a = 0, N = getChildCount(); a < N; a++) {
                tabsTextWidthWithMargin[a] *= m;
            }
        } else if (totalWidth < minTotalWidthForTabs) {
            final float growW = minTotalWidthForTabs - totalWidth;
            final float growP = growW / totalWeight;

            for (int a = 0, N = getChildCount(); a < N; a++) {
                tabsTextWidthWithMargin[a] += growP * tabsWeight[a];
            }
        }

        int l = 0;
        for (int a = 0, N = getChildCount(); a < N; a++) {
            if (!isViewVisible(getChildAt(a))) {
                continue;
            }

            tabsWidth[a] = Math.round(tabsTextWidthWithMargin[a]);
            tabsLeftPos[a] = l;
            l += tabsWidth[a];
        }
        setMeasuredDimension(l + getPaddingLeft() + getPaddingRight(), height);
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            child.measure(
                MeasureSpec.makeMeasureSpec(tabsWidth[a], MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(tabHeight, MeasureSpec.EXACTLY));
        }

        calculateTotalSizesAfterMeasure();
    }

    public interface Tab {
        float measureTextWidth();
        default float measureTextWidth(float textSizeDp) { return measureTextWidth(); }
        default void setTextSizeDp(float textSizeDp) {}
    }



    // fills tabsTextWidth[] and return visible child count;

    private float[] tabsTextWidth;
    private float[] tabsTextWidthWithMargin;
    private int[] tabsWeight;
    private int[] tabsWidth;

    private int[] tabsLeftPos;


    private int visibleChildCount;
    private int biggestTabTextWidth;

    private void measureTabTexts(float textSizeDp) {
        final int childCount = getChildCount();
        if (tabsTextWidth == null || tabsTextWidth.length < childCount) {
            tabsTextWidth = new float[childCount];
            tabsTextWidthWithMargin = new float[childCount];
            tabsWeight = new int[childCount];
            tabsLeftPos = new int[childCount];
            tabsWidth = new int[childCount];
        }

        float maxTabWidthF = 0;
        int index = 0;

        for (int a = 0; a < childCount; a++) {
            final View child = getChildAt(a);
            if (!isViewVisible(child)) {
                tabsTextWidth[a] = -1;
                continue;
            }

            final float tabWidth;
            if (child instanceof MainTabsLayout.Tab) {
                tabWidth = ((MainTabsLayout.Tab) child).measureTextWidth(textSizeDp);
            } else {
                tabWidth = 0;
            }

            tabsTextWidth[a] = tabWidth;
            maxTabWidthF = Math.max(maxTabWidthF, tabWidth);
            index++;
        }

        biggestTabTextWidth = (int) Math.ceil(maxTabWidthF);
        visibleChildCount = index;
    }

    private void applyPassTextSize(int pass) {
        final float textSizeDp = PASS_TEXT_SIZES_DP[pass];
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            if (child instanceof MainTabsLayout.Tab) {
                ((MainTabsLayout.Tab) child).setTextSizeDp(textSizeDp);
            }
        }
    }

    @Override
    protected void setChildVisibilityFactor(View view, float factor) {
        final float s = lerp(0.7f, 1f, factor);
        view.setAlpha(factor);
        view.setScaleX(s);
        view.setScaleY(s);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        checkVisualWidth();
    }

    @Override
    protected void onItemsChanged() {
        super.onItemsChanged();
        checkVisualWidth();
    }

    private void checkVisualWidth() {
        for (int a = 0, N = getEntriesCount(); a < N; a++) {
            final ListAnimator.Entry<Holder> entry = getEntry(a);
            final float width = entry.getRectF().width();
            ((GlassTabView) entry.item.view).setVisualWidth(width);
        }
    }








    public void setTabSelected(View tab, boolean animated) {
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            if (child instanceof GlassTabView) {
                ((GlassTabView) child).setSelected(child == tab, animated);
            }
        }
        /** Meerogram liquid pill start */
        meeroUpdatePillTarget(tab, animated);
        /** Meerogram liquid pill finish */
    }

    private View findSelectedTab() {
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            if (child.getVisibility() != View.VISIBLE) {
                continue;
            }

            if (child instanceof GlassTabView) {
                if (((GlassTabView) child).isTabSelected()) {
                    return child;
                }
            }
        }
        return null;
    }

    private final Runnable restoreDrawSelector = () -> setSkipDrawSelector(false);

    private boolean drawCustomSelector;
    private void setSkipDrawSelector(boolean skipDrawSelector) {
        drawCustomSelector = skipDrawSelector;
        if (drawCustomSelector) {
            selectorPaint.setColor(Theme.multAlpha(Theme.getColor(Theme.key_glass_tabSelected, resourcesProvider), 0.09f));
        }
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            if (child.getVisibility() != View.VISIBLE) {
                continue;
            }

            if (child instanceof GlassTabView) {
                ((GlassTabView) child).setSkipDrawSelector(skipDrawSelector);
            }
        }
        invalidate();
    }







    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        /** Meerogram liquid pill start */
        if (meeroPillMode) {
            meeroTickPill(canvas);
            super.dispatchDraw(canvas);
            return;
        }
        /** Meerogram liquid pill finish */
        if (drawCustomSelector) {
            final float x = animatedLongSelectedViewCenterX + animatedLongSelectedViewOffsetX;
            final float sWidth = getInterpolatedWidthByX(x, this);
            final float sHeight = getHeight() - getPaddingTop() - getPaddingBottom();

            canvas.drawRoundRect(
                    x - sWidth / 2f, (getHeight() - sHeight) / 2f,
                    x + sWidth / 2f, (getHeight() + sHeight) / 2f,
                    sHeight / 2f, sHeight / 2f, selectorPaint);
        }

        super.dispatchDraw(canvas);
    }


    final Paint selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final SpringAnimation scaleX = new SpringAnimation(this, DynamicAnimation.SCALE_X, 1f);
    final SpringAnimation scaleY = new SpringAnimation(this, DynamicAnimation.SCALE_Y, 1f);

    final SpringAnimation selectedTabPositionOffsetX = new SpringAnimation(this, new FloatPropertyCompat<MainTabsLayout>("selectedTabPositionOffsetX") {
        @Override
        public float getValue(MainTabsLayout object) {
            return object.animatedLongSelectedViewOffsetX;
        }

        @Override
        public void setValue(MainTabsLayout object, float value) {
            object.animatedLongSelectedViewOffsetX = value;
            object.invalidate();
        }
    });
    final SpringAnimation selectedTabPositionX = new SpringAnimation(this, new FloatPropertyCompat<MainTabsLayout>("selectedTabPositionX") {
        @Override
        public float getValue(MainTabsLayout object) {
            return object.animatedLongSelectedViewCenterX;
        }

        @Override
        public void setValue(MainTabsLayout object, float value) {
            object.animatedLongSelectedViewCenterX = value;
            object.invalidate();
        }
    });

    {
        selectedTabPositionOffsetX.setSpring(new SpringForce(1)
            .setStiffness(SpringForce.STIFFNESS_MEDIUM)
            .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY));
        scaleX.setSpring(new SpringForce(1f)
            .setStiffness(250)
            .setDampingRatio(0.25f));
        scaleY.setSpring(new SpringForce(1f)
            .setStiffness(250)
            .setDampingRatio(0.25f));
        selectedTabPositionX.setSpring(new SpringForce(1f)
            .setStiffness(SpringForce.STIFFNESS_MEDIUM)
            .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY));
    }

    private float animatedLongSelectedViewCenterX;
    private float animatedLongSelectedViewOffsetX;

    private boolean isInLongPress;
    private float lastLongSelectedViewCenterX;
    private float lastLongSelectedViewWidth;
    private View lastLongSelectedView;




    public static View findChildUnder(ViewGroup parent, float x, float y) {
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            View child = parent.getChildAt(i);

            if (child.getVisibility() != View.VISIBLE) continue;

            if (x >= child.getLeft() && x <= child.getRight()
                    && y >= child.getTop() && y <= child.getBottom()) {
                return child;
            }
        }
        return null;
    }

    private void checkLongMove(float x_, float y, boolean start, boolean end) {
        final float x = clampXToChildrenCenters(x_, this);
        final View found = findNearestVisibleChildByX(x, this);
        if (start) {
            View selected = findSelectedTab();
            if (selected != null) {
                animatedLongSelectedViewCenterX = selected.getX() + selected.getWidth() / 2f;
                animatedLongSelectedViewOffsetX = animatedLongSelectedViewCenterX - x;
                selectedTabPositionOffsetX.animateToFinalPosition(0);
                if (selected != found && found != null) {
                    found.performClick();
                }
            }
            selectedTabPositionX.cancel();
        }

        if (!end) {
            animatedLongSelectedViewCenterX = x;
            invalidate();
        }

        if (found != null) {
            lastLongSelectedView = found;
            setTabSelected(found, true);

            if (end) {
                final float vw = found.getWidth();
                final float cx = found.getX() + vw / 2f;
                if (lastLongSelectedViewWidth != vw || lastLongSelectedViewCenterX != cx) {
                    selectedTabPositionX.animateToFinalPosition(cx);
                }
            }
        }
    }

    private final Set<View> tabsWithIgnoreClick = new HashSet<>();
    public void addTabToIgnoreClick(View v) {
        tabsWithIgnoreClick.add(v);
    }

    private final BoolAnimator animatorIsScaled = new BoolAnimator(0, (a, factor, c, g) -> {
        setScaleX(lerp(1, 1.019f, factor));
        setScaleY(lerp(1, 1.019f, factor));
    }, CubicBezierInterpolator.EASE_OUT_QUINT, 380);

    private final ClickHelper clickHelper = new ClickHelper(new ClickHelper.Delegate() {
        @Override
        public boolean needClickAt(View view, float x, float y) {
            lastLongSelectedView = null;
            final View found = findChildUnder(MainTabsLayout.this, x, y);
            return found != null && !tabsWithIgnoreClick.contains(found);
        }

        @Override
        public void onClickAt(View view, float x, float y) {
        }

        @Override
        public boolean needLongPress(float x, float y) {
            return true;
        }

        @Override
        public boolean needCancelTouchBySlopMove() {
            return false;
        }


        @Override
        public boolean onLongPressRequestedAt(View view, float x, float y) {
            checkPivot(view, x, y);
            isInLongPress = true;
            AndroidUtilities.cancelRunOnUIThread(restoreDrawSelector);
            setSkipDrawSelector(true);
            checkLongMove(x, y, true, false);
            invalidate();
            longTouchStart();
            return true;
        }

        @Override
        public void onLongPressMove(View view, MotionEvent e, float x, float y, float startX, float startY) {
            checkPivot(view, x, y);
            checkLongMove(x, y, false, false);
            invalidate();
        }

        @Override
        public long getLongPressDuration() {
            return ClickHelper.Delegate.super.getLongPressDuration() * 750 / 1000;
        }

        @Override
        public void onLongPressFinish(View view, float x, float y) {
            checkPivot(view, x, y);
            checkLongMove(x, y, false, true);
            isInLongPress = false;
            AndroidUtilities.runOnUIThread(restoreDrawSelector, 450);
            if (lastLongSelectedView != null) {
                lastLongSelectedView.performClick();
            }
            lastLongSelectedView = null;
            invalidate();
            longTouchEnd();
        }

        @Override
        public void onLongPressCancelled(View view, float x, float y) {
            checkPivot(view, x, y);
            checkLongMove(x, y, false, true);
            isInLongPress = false;
            AndroidUtilities.runOnUIThread(restoreDrawSelector, 450);
            lastLongSelectedView = null;
            invalidate();
            longTouchEnd();
        }

        private void longTouchStart() {
            animatorIsScaled.setValue(true, true);

            /*
            if (!scaleX.isRunning()) {
                scaleX.setStartVelocity(-0.45f);
                scaleY.setStartVelocity(-0.45f);
            }
            scaleX.animateToFinalPosition(1.012f);
            scaleY.animateToFinalPosition(1.012f);
            */
        }

        private void longTouchEnd() {
            animatorIsScaled.setValue(false, true);

            /*
            if (!scaleX.isRunning()) {
                scaleX.setStartVelocity(0.25f);
                scaleY.setStartVelocity(0.25f);
            }
            scaleX.animateToFinalPosition(1f);
            scaleY.animateToFinalPosition(1f);
            */
        }
    });

    @Override
    public void setScaleY(float scaleY) {
        super.setScaleY(scaleY);
        checkLayerType();
    }

    @Override
    public void setScaleX(float scaleX) {
        super.setScaleX(scaleX);
        checkLayerType();
    }

    private void checkLayerType() {
        final int layerType = Math.abs(getScaleX() - 1f) < 0.0001f && Math.abs(getScaleY() - 1f) < 0.0001f ?
            View.LAYER_TYPE_NONE : View.LAYER_TYPE_HARDWARE;

        if (getLayerType() != layerType) {
            setLayerType(layerType, null);
            invalidate();
        }
    }


    private void checkPivot(View view, float x, float y) {
        float w = view.getWidth();
        float h = view.getHeight();

        if (w <= 0f || h <= 0f) {
            return;
        }

        float cx = w * 0.5f;
        float cy = h * 0.5f;

        float dx = x - cx;
        float dy = y - cy;

        float halfW = w * 0.5f;
        float halfH = h * 0.5f;

        float nx = dx / halfW;
        float ny = dy / halfH;

        float r = (float) Math.sqrt(nx * nx + ny * ny);

        float pivotX;
        float pivotY;

        if (r > 1e-4f) {
            float mappedR = 1.5f * r / (r + 0.5f);

            float scale = mappedR / r;
            pivotX = cx + dx * scale;
            pivotY = cy + dy * scale;
        } else {
            pivotX = cx;
            pivotY = cy;
        }

        pivotX = lerp(cx, pivotX, 1f);
        pivotY = lerp(cy, pivotY, 3f);

        view.setPivotX(pivotX);
        view.setPivotY(pivotY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (tabDragHelper != null) {
            // while the ClickHelper long-press drag owns the bar, the tab drag stands down
            tabDragHelper.onDispatchTouchEvent(ev, clickHelper.inLongPress());
            if (tabDragHelper.isDragging()) {
                // a horizontal drag across the bar took over the gesture:
                // cancel the pending ClickHelper long press so the two never fight
                clickHelper.cancel(this, ev.getX(), ev.getY());
                return super.dispatchTouchEvent(ev);
            }
            clickHelper.onTouchEvent(this, ev);
            return super.dispatchTouchEvent(ev);
        }
        clickHelper.onTouchEvent(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // once the horizontal drag is confirmed, children get ACTION_CANCEL
        // and the bar itself handles the rest of the gesture
        return tabDragHelper != null && tabDragHelper.isDragging();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // own the stream after interception (and for touches on the bar padding)
        if (tabDragHelper != null && tabDragHelper.isHandlingTouch()) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    /** Meerogram: drag-to-switch delegate, wired into MeeroTabDragHelper. */
    private final MeeroTabDragHelper.Delegate tabDragDelegate = new MeeroTabDragHelper.Delegate() {
        @Override
        public boolean isTabVisibleForDrag(View tab) {
            return tab.getVisibility() == View.VISIBLE && isViewVisible(tab);
        }

        @Override
        public void onTabDragPressChanged(View tab, boolean pressed) {
            if (pressed) {
                if (tabDragPressedView == tab) {
                    return;
                }
                releaseTabDragPress();
                tabDragPressedView = tab;
                if (tab != null) {
                    // gentle press feedback on ACTION_DOWN
                    tab.animate().scaleX(1.05f).scaleY(1.05f)
                        .setDuration(120).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).start();
                }
            } else {
                releaseTabDragPress();
            }
        }

        @Override
        public void onTabDragStarted() {
            lastTabDragTickIndex = -1;
            if (tabDragPressedView != null) {
                tabDragPressedView.animate().cancel();
            }
        }

        @Override
        public void onTabDragProgress(float position, float velocity) {
            applyTabDragVisual(position, velocity);
        }

        @Override
        public void onTabDragFinished(float position, float velocity, View nearestTab) {
            restoreTabDragVisual();
            tabDragPressedView = null;
            lastTabDragTickIndex = -1;
            if (nearestTab != null) {
                nearestTab.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                // snap: exact same code path as a normal tap — the tab's own
                // OnClickListener performs selectTab + viewPager.scrollToPosition
                nearestTab.performClick();
            }
        }

        @Override
        public void onTabDragCancelled() {
            restoreTabDragVisual();
            tabDragPressedView = null;
            lastTabDragTickIndex = -1;
        }
    };

    private void releaseTabDragPress() {
        if (tabDragPressedView != null) {
            tabDragPressedView.animate().scaleX(1f).scaleY(1f)
                .setDuration(150).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).start();
            tabDragPressedView = null;
        }
    }

    /**
     * The selection highlight of every visible tab follows the finger
     * proportionally (gestureSelectedOverride interpolation between adjacent
     * tabs, elongated in the drag direction by the finger velocity), plus a
     * gentle scale on the tab icons. Bar dimensions are never changed.
     */
    private void applyTabDragVisual(float position, float velocity) {
        final int count = tabDragHelper.getVisibleTabCount();
        if (count == 0) {
            return;
        }
        final float[] factors = tabDragHelper.computeTabFactors(position, velocity);
        for (int i = 0; i < count; i++) {
            final View child = tabDragHelper.getVisibleTab(i);
            if (!(child instanceof GlassTabView)) {
                continue;
            }
            final float factor = factors[i];
            ((GlassTabView) child).setGestureSelectedOverride(factor, true);
            final float s = lerp(1f, 1.05f, factor);
            child.setScaleX(s);
            child.setScaleY(s);
        }
        final int index = Math.round(position);
        if (index != lastTabDragTickIndex) {
            if (lastTabDragTickIndex != -1) {
                final int clamped = MathUtils.clamp(index, 0, count - 1);
                final View tickTab = tabDragHelper.getVisibleTab(clamped);
                if (tickTab != null) {
                    tickTab.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                }
            }
            lastTabDragTickIndex = index;
        }
        /** Meerogram liquid pill start */
        if (meeroPillMode) {
            meeroPillTarget = position;
            meeroPillDragging = true;
            final float v = MathUtils.clamp(velocity / 3000f, -0.2f, 0.2f);
            meeroPillScaleXTarget = 1f + v;
            meeroPillScaleYTarget = 1f - v * 0.4f;
        }
        /** Meerogram liquid pill finish */
        invalidate();
    }

    private void restoreTabDragVisual() {
        /** Meerogram liquid pill start */
        if (meeroPillMode) {
            meeroPillDragging = false;
            meeroPillScaleXTarget = 1f;
            meeroPillScaleYTarget = 1f;
            meeroUpdatePillTarget(findSelectedTab(), true);
        }
        /** Meerogram liquid pill finish */
        final int count = tabDragHelper != null ? tabDragHelper.getVisibleTabCount() : 0;
        for (int i = 0; i < count; i++) {
            final View child = tabDragHelper.getVisibleTab(i);
            if (!(child instanceof GlassTabView)) {
                continue;
            }
            ((GlassTabView) child).setGestureSelectedOverride(0f, false);
            child.animate().scaleX(1f).scaleY(1f)
                .setDuration(160).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).start();
        }
        invalidate();
    }
    /** Meerogram finish */


    private static float clampXToChildrenCenters(float x, ViewGroup parent) {
        if (parent == null || parent.getChildCount() == 0) {
            return x;
        }

        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        boolean found = false;

        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (view == null || view.getVisibility() != View.VISIBLE) {
                continue;
            }

            float centerX = view.getX() + view.getWidth() * 0.5f;

            if (centerX < min) min = centerX;
            if (centerX > max) max = centerX;

            found = true;
        }

        if (!found) {
            return x;
        }

        if (x < min) return min;
        if (x > max) return max;
        return x;
    }

    @Nullable
    private static View findNearestVisibleChildByX(float x, ViewGroup parent) {
        if (parent == null || parent.getChildCount() == 0) {
            return null;
        }

        View nearest = null;
        float nearestDistance = Float.MAX_VALUE;

        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (view == null || view.getVisibility() != View.VISIBLE) {
                continue;
            }

            float centerX = view.getX() + view.getWidth() * 0.5f;
            float distance = Math.abs(centerX - x);

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = view;
            }
        }

        return nearest;
    }

    private static float getInterpolatedWidthByX(float x, ViewGroup parent) {
        if (parent == null || parent.getChildCount() == 0) {
            return 0f;
        }

        View left = null;
        View right = null;

        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (view == null || view.getVisibility() != View.VISIBLE) {
                continue;
            }

            float centerX = view.getX() + view.getWidth() * 0.5f;

            if (centerX <= x && (left == null || centerX > getCenterX(left))) {
                left = view;
            }

            if (centerX >= x && (right == null || centerX < getCenterX(right))) {
                right = view;
            }
        }

        if (left == null && right == null) {
            return 0f;
        }

        if (left == null) {
            return right.getWidth();
        }

        if (right == null) {
            return left.getWidth();
        }

        float leftX = getCenterX(left);
        float rightX = getCenterX(right);

        if (left == right || leftX == rightX) {
            return left.getWidth();
        }

        float ratio = (x - leftX) / (rightX - leftX);
        return lerp(left.getWidth(), right.getWidth(), ratio);
    }

    private static float getCenterX(View v) {
        return v.getX() + v.getWidth() * 0.5f;
    }

    /** Cherrygram start */
    private boolean isForMainTabsCG = false;

    // Meerogram: horizontal drag-to-switch for the main bottom tab bar
    // (see uz.unnarsx.cherrygram.core.ui.mainTabs.MeeroTabDragHelper).
    // Only created for the main tabs bar; other usages of MainTabsLayout
    // (MediaActivity, StatisticActivity) keep their exact previous behavior.
    /** Meerogram liquid pill start */
    // YTXTube-style liquid glass pill: one frosted capsule slides behind the
    // tab icons, follows the finger while dragging (with a velocity squish)
    // and glides to the selected tab at rest. Same glass as the bar itself,
    // sized to Telegram's official bar dimensions.
    private BlurredBackgroundDrawableViewFactory meeroGlassFactory;
    private BlurredBackgroundDrawable meeroPillGlass;
    private boolean meeroPillMode;
    private boolean meeroPillInit;
    private boolean meeroPillDragging;
    private float meeroPillPosition;
    private float meeroPillTarget;
    private float meeroPillScaleX = 1f, meeroPillScaleY = 1f;
    private float meeroPillScaleXTarget = 1f, meeroPillScaleYTarget = 1f;
    private final RectF meeroPillRect = new RectF();
    private final Paint meeroPillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void setMeeroGlassFactory(BlurredBackgroundDrawableViewFactory factory) {
        meeroGlassFactory = factory;
        meeroPillMode = factory != null && isForMainTabsCG;
        if (meeroPillMode) {
            try {
                meeroPillGlass = factory.create(this, BlurredBackgroundProviderImpl.mainTabs(resourcesProvider));
            } catch (Throwable ignore) {
                meeroPillGlass = null;
            }
            for (int a = 0, N = getChildCount(); a < N; a++) {
                final View child = getChildAt(a);
                if (child instanceof GlassTabView) {
                    ((GlassTabView) child).setMeeroHideSelector(true);
                }
            }
            meeroPillInit = false;
            invalidate();
        } else {
            meeroPillGlass = null;
        }
    }

    private int meeroIndexOf(View tab) {
        int index = 0;
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            if (!(child instanceof GlassTabView) || child.getVisibility() != View.VISIBLE) {
                continue;
            }
            if (child == tab) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private View meeroTabAt(int index) {
        int i = 0;
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            if (!(child instanceof GlassTabView) || child.getVisibility() != View.VISIBLE) {
                continue;
            }
            if (i == index) {
                return child;
            }
            i++;
        }
        return null;
    }

    private int meeroVisibleTabCount() {
        int i = 0;
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            if (child instanceof GlassTabView && child.getVisibility() == View.VISIBLE) {
                i++;
            }
        }
        return i;
    }

    private void meeroUpdatePillTarget(View tab, boolean animated) {
        if (!meeroPillMode) {
            return;
        }
        final int index = tab == null ? -1 : meeroIndexOf(tab);
        if (index >= 0) {
            if (!meeroPillInit) {
                meeroPillPosition = index;
                meeroPillInit = true;
            }
            meeroPillTarget = index;
            for (int a = 0, N = getChildCount(); a < N; a++) {
                final View child = getChildAt(a);
                if (child instanceof GlassTabView) {
                    ((GlassTabView) child).setMeeroHideSelector(true);
                }
            }
        }
        invalidate();
    }

    private void meeroTickPill(Canvas canvas) {
        final int count = meeroVisibleTabCount();
        if (count == 0) {
            return;
        }
        if (!meeroPillInit) {
            final View selected = findSelectedTab();
            if (selected != null) {
                meeroUpdatePillTarget(selected, false);
            }
        }
        meeroPillPosition = MathUtils.clamp(meeroPillPosition, 0, count - 1);
        meeroPillTarget = MathUtils.clamp(meeroPillTarget, 0, count - 1);
        if (meeroPillDragging) {
            meeroPillPosition = meeroPillTarget;
        } else {
            meeroPillPosition += (meeroPillTarget - meeroPillPosition) * 0.35f;
            if (Math.abs(meeroPillTarget - meeroPillPosition) > 0.002f) {
                postInvalidateOnAnimation();
            }
        }
        meeroPillScaleX += (meeroPillScaleXTarget - meeroPillScaleX) * 0.35f;
        meeroPillScaleY += (meeroPillScaleYTarget - meeroPillScaleY) * 0.35f;

        final float p = meeroPillPosition;
        final int low = MathUtils.clamp((int) Math.floor(p), 0, count - 1);
        final int high = MathUtils.clamp((int) Math.ceil(p), 0, count - 1);
        final float t = p - low;
        final View lowTab = meeroTabAt(low);
        final View highTab = meeroTabAt(high);
        if (lowTab == null || highTab == null) {
            return;
        }
        final float left = lerp(lowTab.getLeft(), highTab.getLeft(), t);
        final float right = lerp(lowTab.getRight(), highTab.getRight(), t);
        final float innerTop = getPaddingTop() + dp(4);
        final float innerBottom = getHeight() - getPaddingBottom() - dp(4);
        meeroPillRect.set(left, innerTop, right, innerBottom);

        canvas.save();
        canvas.scale(meeroPillScaleX, meeroPillScaleY, meeroPillRect.centerX(), meeroPillRect.centerY());
        final float radius = meeroPillRect.height() / 2f;
        if (meeroPillGlass != null) {
            try {
                meeroPillGlass.setRadius(radius);
                meeroPillGlass.setBounds((int) meeroPillRect.left, (int) meeroPillRect.top, (int) meeroPillRect.right, (int) meeroPillRect.bottom);
                meeroPillGlass.draw(canvas);
            } catch (Throwable ignore) {
            }
        } else {
            meeroPillPaint.setStyle(Paint.Style.FILL);
            meeroPillPaint.setColor(Theme.multAlpha(Theme.getColor(Theme.key_glass_tabSelected, resourcesProvider), 0.09f));
            canvas.drawRoundRect(meeroPillRect, radius, radius, meeroPillPaint);
        }
        // Hairline highlight along the pill edge, like the reference glass.
        meeroPillPaint.setStyle(Paint.Style.STROKE);
        meeroPillPaint.setStrokeWidth(dp(1));
        meeroPillPaint.setColor(Theme.multAlpha(0xFFFFFFFF, 0.16f));
        canvas.drawRoundRect(meeroPillRect, radius, radius, meeroPillPaint);
        canvas.restore();
    }
    /** Meerogram liquid pill finish */

    private MeeroTabDragHelper tabDragHelper;
    private View tabDragPressedView;
    private int lastTabDragTickIndex = -1;

    public void setForMainTabsCG(boolean cherrygramMainTabs) {
        if (isForMainTabsCG != cherrygramMainTabs) {
            isForMainTabsCG = cherrygramMainTabs;
            if (cherrygramMainTabs && tabDragHelper == null) {
                tabDragHelper = new MeeroTabDragHelper(this, tabDragDelegate);
            }
            requestLayout();
        }
    }

    private void onMeasureMainTabsCG(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int tabHeight = height - getPaddingTop() - getPaddingBottom();
        final int tabWidth = dp(!CherrygramAppearanceConfig.INSTANCE.getShowMainTabsTitle() ? 12 : 24);

        if (maxWidthPx > 0 && width > maxWidthPx) {
            width = maxWidthPx;
        }

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int searchOffset = dp(DialogsActivity.MAIN_TABS_HEIGHT_WITH_MARGINS);
        int absoluteMaxLimit = screenWidth - searchOffset - dp(DialogsActivity.MAIN_TABS_MARGIN * 2);
        final int maxTotalWidthForTabs = Math.max(width, absoluteMaxLimit) - getPaddingLeft() - getPaddingRight();
        final int minTotalWidthForTabs = Math.min(dp(320) / 2, maxTotalWidthForTabs);

        int chosenPass = PASS_TEXT_SIZES_DP.length - 1;
        float lastMeasuredTextSize = -1;
        for (int pass = 0; pass < PASS_TEXT_SIZES_DP.length; pass++) {
            if (PASS_TEXT_SIZES_DP[pass] != lastMeasuredTextSize) {
                measureTabTexts(PASS_TEXT_SIZES_DP[pass]);
                lastMeasuredTextSize = PASS_TEXT_SIZES_DP[pass];
            }
            final int padding = dp(PASS_PADDINGS_DP[pass]);
            float total = 0;
            for (int a = 0, N = getChildCount(); a < N; a++) {
                if (!isViewVisible(getChildAt(a))) continue;
                final float withMargin = tabsTextWidth[a] + padding * 2;
                total += withMargin;
            }
            final boolean fits = total <= maxTotalWidthForTabs;
            if (fits || pass == PASS_TEXT_SIZES_DP.length - 1) {
                chosenPass = pass;
                break;
            }
        }

        applyPassTextSize(chosenPass);

        final int tabPadding = dp(PASS_PADDINGS_DP[chosenPass]);
        final int maxTabTextWidthIfEq = (maxTotalWidthForTabs / Math.max(1, visibleChildCount)) - tabPadding * 2;

        float totalWidth = 0;
        int totalWeight = 0;
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            if (!isViewVisible(child)) {
                tabsTextWidth[a] = tabsTextWidthWithMargin[a] = 0;
                tabsWeight[a] = 0;
                continue;
            }

            float baseTabWidth = tabWidth + dp(12) * 2;
            final float w = Math.max(tabsTextWidth[a], baseTabWidth);

            if (w > maxTabTextWidthIfEq) {
                tabsTextWidthWithMargin[a] = w + dp(13) * 2;
            } else {
                tabsTextWidthWithMargin[a] = w + dp(16) * 2;
            }

            tabsWeight[a] = 1;
            totalWidth += tabsTextWidthWithMargin[a];
            totalWeight += tabsWeight[a];
        }

        if (totalWeight == 0) {
            for (int a = 0, N = getChildCount(); a < N; a++) {
                tabsWeight[a] = isViewVisible(getChildAt(a)) ? 1 : 0;
            }
            totalWeight = visibleChildCount;
        }

        if (totalWidth > maxTotalWidthForTabs) {
            final float m = maxTotalWidthForTabs / totalWidth;
            for (int a = 0, N = getChildCount(); a < N; a++) {
                tabsTextWidthWithMargin[a] *= m;
            }
        } else if (totalWidth < minTotalWidthForTabs) {

            float targetWidth;

            if (visibleChildCount == 1) {
                targetWidth = totalWidth + tabWidth;
                targetWidth = Math.min(targetWidth, maxTotalWidthForTabs * 0.5f);
            } else {
                targetWidth = minTotalWidthForTabs;
            }

            final float growW = targetWidth - totalWidth;
            final float growP = growW / totalWeight;

            for (int a = 0, N = getChildCount(); a < N; a++) {
                tabsTextWidthWithMargin[a] += growP * tabsWeight[a];
            }
        }

        int l = 0;
        for (int a = 0, N = getChildCount(); a < N; a++) {
            if (!isViewVisible(getChildAt(a))) {
                continue;
            }

            tabsWidth[a] = Math.round(tabsTextWidthWithMargin[a]);
            tabsLeftPos[a] = l;
            l += tabsWidth[a];
        }
        setMeasuredDimension(l + getPaddingLeft() + getPaddingRight(), height);
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            child.measure(
                MeasureSpec.makeMeasureSpec(tabsWidth[a], MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(tabHeight, MeasureSpec.EXACTLY));
        }

        calculateTotalSizesAfterMeasure();
    }

    /** Cherrygram finish */

}