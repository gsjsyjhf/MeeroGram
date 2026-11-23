/*
 * This file is a part of Meergram.
 * It is licensed under GNU GPL v. 2 or later.
 *
 * Meerogram: horizontal drag-to-switch for the glass bottom tab bar.
 *
 * Modeled after the liquid bottom bar of GlassTube / YTXTube
 * (com.tensei.ytxtube.GlassNavBar.kt + DampedDragAnimation in GlassMotion.kt):
 * the finger's horizontal movement is mapped to a fractional tab position,
 * the selection highlight follows it proportionally between adjacent tabs,
 * and on release the bar snaps to the nearest tab and opens it through the
 * exact same code path as a normal tap (the tab's own OnClickListener).
 *
 * Only horizontal drags are consumed: a vertical movement never becomes a
 * tab drag, so vertical scrolling of the content above the bar is never
 * blocked. Positions are computed from the actual visual x coordinates of
 * the tab views, which makes the mapping automatically correct under RTL
 * layout direction (dragging toward the visually-next tab selects it).
 */
package uz.unnarsx.cherrygram.core.ui.mainTabs;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import java.util.ArrayList;

public class MeeroTabDragHelper {

    public interface Delegate {
        /** Whether the tab takes part in the drag (visible and not hidden by add/remove animations). */
        boolean isTabVisibleForDrag(View tab);

        /** A gentle press feedback on the tab under the finger (ACTION_DOWN), released on up/cancel. */
        void onTabDragPressChanged(@Nullable View tab, boolean pressed);

        /** The horizontal drag was confirmed (past touch slop); children are about to be intercepted. */
        void onTabDragStarted();

        /**
         * The finger moved: position is the fractional tab index interpolated between adjacent
         * tabs, velocity is the finger velocity in px/s, xSinceStart is the raw pointer offset
         * from the drag start and xDelta the raw movement since the previous event
         * (R11.3: the liquid physics accumulates target += dx / tabWidth exactly like
         * YTXTube DampedDragAnimation.onDrag).
         */
        void onTabDragProgress(float position, float velocity, float xSinceStart, float xDelta);

        /** The finger was released: snap to nearestTab and open it through the normal tap code path. */
        void onTabDragFinished(float position, float velocity, @Nullable View nearestTab);

        /** The gesture was cancelled (another gesture took over, second finger, system cancel). */
        void onTabDragCancelled();
    }

    private static final float MAX_VELOCITY_STRETCH = 0.45f;
    private static final float VELOCITY_STRETCH_DIVIDER = 7000f;

    private final ViewGroup tabBar;
    private final Delegate delegate;
    private final int touchSlop;
    /** -1 under RTL so the highlight smear always follows the visual drag direction. */
    private final float rtlSign;

    private VelocityTracker velocityTracker;
    private boolean tracking;
    private boolean dragging;

    private float downX, downY;
    private float dragStartX, dragLastX;

    private final ArrayList<View> visibleTabs = new ArrayList<>();
    private float[] cachedFactors;

    public MeeroTabDragHelper(ViewGroup tabBar, Delegate delegate) {
        this.tabBar = tabBar;
        this.delegate = delegate;
        this.touchSlop = ViewConfiguration.get(tabBar.getContext()).getScaledTouchSlop();
        this.rtlSign = tabBar.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? -1f : 1f;
    }

    public boolean isDragging() {
        return dragging;
    }

    /** True while a finger is down on the bar and this helper owns (or is deciding about) the gesture. */
    public boolean isHandlingTouch() {
        return tracking || dragging;
    }

    /**
     * Feed the dispatch-level touch stream of the tab bar.
     * Must be called before the bar dispatches to children.
     *
     * @param externalGestureActive true while another gesture owns the bar
     *                              (e.g. the ClickHelper long-press drag) — the helper then stands down.
     */
    public void onDispatchTouchEvent(MotionEvent ev, boolean externalGestureActive) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (externalGestureActive) {
                    return;
                }
                tracking = true;
                dragging = false;
                downX = ev.getX();
                downY = ev.getY();
                dragStartX = ev.getX();
                dragLastX = ev.getX();
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(ev);
                rebuildVisibleTabs();
                delegate.onTabDragPressChanged(findTabUnder(downX, downY), true);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!tracking) {
                    break;
                }
                if (externalGestureActive) {
                    finishGesture(true);
                    break;
                }
                velocityTracker.addMovement(ev);
                if (!dragging) {
                    final float dx = ev.getX() - downX;
                    final float dy = ev.getY() - downY;
                    // horizontal-only detection: a vertical movement must keep scrolling the content
                    if (Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy)) {
                        dragging = true;
                        dragStartX = downX;
                        dragLastX = ev.getX();
                        delegate.onTabDragStarted();
                        delegate.onTabDragProgress(positionForX(ev.getX()), 0f,
                                ev.getX() - dragStartX, ev.getX() - dragLastX);
                        dragLastX = ev.getX();
                    } else if (Math.abs(dy) > touchSlop && Math.abs(dy) > Math.abs(dx)) {
                        // vertical gesture wins: not ours
                        finishGesture(false);
                        delegate.onTabDragPressChanged(null, false);
                    }
                } else {
                    velocityTracker.computeCurrentVelocity(1000);
                    final float xNow = ev.getX();
                    delegate.onTabDragProgress(positionForX(xNow), velocityTracker.getXVelocity(),
                            xNow - dragStartX, xNow - dragLastX);
                    dragLastX = xNow;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (!tracking) {
                    break;
                }
                velocityTracker.addMovement(ev);
                velocityTracker.computeCurrentVelocity(1000);
                final float velocity = velocityTracker.getXVelocity();
                final boolean wasDragging = dragging;
                finishGesture(false);
                if (wasDragging) {
                    final float position = positionForX(ev.getX());
                    delegate.onTabDragFinished(position, velocity, nearestVisibleTab(position));
                } else {
                    delegate.onTabDragPressChanged(null, false);
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                if (!tracking) {
                    break;
                }
                final boolean wasDragging = dragging;
                finishGesture(false);
                if (wasDragging) {
                    delegate.onTabDragCancelled();
                } else {
                    delegate.onTabDragPressChanged(null, false);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                // second finger: keep it simple and give the gesture up
                if (tracking) {
                    final boolean wasDragging = dragging;
                    finishGesture(false);
                    if (wasDragging) {
                        delegate.onTabDragCancelled();
                    } else {
                        delegate.onTabDragPressChanged(null, false);
                    }
                }
                break;
            }
        }
    }

    private void finishGesture(boolean cancelled) {
        tracking = false;
        dragging = false;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
        if (cancelled) {
            delegate.onTabDragCancelled();
        }
    }

    public int getVisibleTabCount() {
        return visibleTabs.size();
    }

    @Nullable
    public View getVisibleTab(int index) {
        if (index < 0 || index >= visibleTabs.size()) {
            return null;
        }
        return visibleTabs.get(index);
    }

    private void rebuildVisibleTabs() {
        visibleTabs.clear();
        for (int a = 0, N = tabBar.getChildCount(); a < N; a++) {
            final View child = tabBar.getChildAt(a);
            if (delegate.isTabVisibleForDrag(child)) {
                visibleTabs.add(child);
            }
        }
    }

    /**
     * Maps a visual x to a fractional index in the sequence of visible tabs:
     * the value interpolates between tab i and tab i+1 as the finger moves
     * between their visual centers. Automatically RTL-correct because it is
     * built from the live visual positions of the children.
     */
    public float positionForX(float x) {
        final int N = visibleTabs.size();
        if (N == 0) {
            return 0f;
        }
        int prevIndex = -1;
        float prevCenter = 0f;
        int nextIndex = -1;
        float nextCenter = 0f;
        for (int i = 0; i < N; i++) {
            final View child = visibleTabs.get(i);
            final float center = child.getX() + child.getWidth() / 2f;
            if (center <= x && (prevIndex == -1 || center >= prevCenter)) {
                prevIndex = i;
                prevCenter = center;
            }
            if (center >= x && nextIndex == -1) {
                nextIndex = i;
                nextCenter = center;
            }
        }
        if (prevIndex == -1) {
            return 0f;
        }
        if (nextIndex == -1) {
            return N - 1f;
        }
        if (prevIndex == nextIndex || nextCenter == prevCenter) {
            return prevIndex;
        }
        final float t = (x - prevCenter) / (nextCenter - prevCenter);
        return prevIndex + t;
    }

    @Nullable
    public View nearestVisibleTab(float position) {
        final int N = visibleTabs.size();
        if (N == 0) {
            return null;
        }
        int index = Math.round(position);
        if (index < 0) {
            index = 0;
        }
        if (index > N - 1) {
            index = N - 1;
        }
        return visibleTabs.get(index);
    }

    private View findTabUnder(float x, float y) {
        for (int a = tabBar.getChildCount() - 1; a >= 0; a--) {
            final View child = tabBar.getChildAt(a);
            if (!delegate.isTabVisibleForDrag(child)) {
                continue;
            }
            if (x >= child.getX() && x <= child.getX() + child.getWidth()
                    && y >= child.getY() && y <= child.getY() + child.getHeight()) {
                return child;
            }
        }
        return null;
    }

    /**
     * Selection visibility of one tab for the current drag: 1 at the finger position,
     * fading to 0 over the adjacent tabs. The highlight is elongated ("liquid" smear)
     * in the visual direction of the finger movement proportionally to its velocity.
     */
    public float computeTabFactor(int visibleIndex, float position, float velocity) {
        final float stretch = MathUtils.clamp(Math.abs(velocity) / VELOCITY_STRETCH_DIVIDER, 0f, MAX_VELOCITY_STRETCH);
        final float dir = velocity >= 0f ? rtlSign : -rtlSign;
        final float d = visibleIndex - position;
        final float dist = d * dir > 0f ? Math.abs(d) / (1f + stretch) : Math.abs(d);
        return MathUtils.clamp(1f - dist, 0f, 1f);
    }

    /** Factors for all visible tabs, aligned with getVisibleTab(i). Reuses an internal array. */
    public float[] computeTabFactors(float position, float velocity) {
        final int N = visibleTabs.size();
        if (cachedFactors == null || cachedFactors.length != N) {
            cachedFactors = new float[N];
        }
        for (int i = 0; i < N; i++) {
            cachedFactors[i] = computeTabFactor(i, position, velocity);
        }
        return cachedFactors;
    }
}
