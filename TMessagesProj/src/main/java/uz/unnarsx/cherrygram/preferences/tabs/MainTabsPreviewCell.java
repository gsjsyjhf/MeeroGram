package uz.unnarsx.cherrygram.preferences.tabs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import org.telegram.ui.Components.glass.GlassTabView;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Collections;

import uz.unnarsx.cherrygram.core.ui.mainTabs.MainTabsManager;

/**
 * MeeroGram R11.3: real live preview of the bottom-tabs organizer (replaces
 * the R11 empty placeholder). Renders the actual GlassTabView set built by
 * MainTabsManager, lets the user tap a tab to enable/disable it (Chats is
 * always on) and, in edit mode, drag tabs horizontally to reorder them.
 * Every change is reported through OnTabsChangedListener; the entry persists
 * it via MainTabsManager.saveTabs.
 */
public class MainTabsPreviewCell extends LinearLayout {

    public interface OnTabsChangedListener {
        void onTabsChanged();
    }

    private ArrayList<MainTabsManager.Tab> tabs;
    private Theme.ResourcesProvider resourcesProvider;
    private int currentAccount;
    private boolean editMode;
    private OnTabsChangedListener listener;

    private final ArrayList<GlassTabView> views = new ArrayList<>();

    // drag-to-reorder state
    private int dragIndex = -1;
    private float dragDownX;
    private boolean dragArmed;

    public MainTabsPreviewCell(Context context) {
        super(context);
        init();
    }

    public MainTabsPreviewCell(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setWillNotDraw(false);
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public void setOnTabsChangedListener(OnTabsChangedListener listener) {
        this.listener = listener;
    }

    public void setTabs(ArrayList<MainTabsManager.Tab> tabs, Context context, Theme.ResourcesProvider resourcesProvider, int currentAccount, boolean animated, boolean attached) {
        this.tabs = tabs;
        this.resourcesProvider = resourcesProvider;
        this.currentAccount = currentAccount;
        rebuild();
    }

    private void rebuild() {
        removeAllViews();
        views.clear();
        if (tabs == null) {
            return;
        }
        int firstEnabled = -1;
        for (int i = 0; i < tabs.size(); i++) {
            final MainTabsManager.Tab t = tabs.get(i);
            if (t.enabled && firstEnabled == -1 && t.type != MainTabsManager.TabType.SEARCH) {
                firstEnabled = i;
            }
        }
        for (int i = 0; i < tabs.size(); i++) {
            final MainTabsManager.Tab t = tabs.get(i);
            final GlassTabView v = MainTabsManager.INSTANCE.createTabView(
                    getContext(), resourcesProvider, currentAccount, t.type, true, true);
            v.setSelected(i == firstEnabled, false);
            v.setAlpha(t.enabled ? 1f : 0.35f);
            v.setOnClickListener(vv -> {
                if (!editMode) {
                    return;
                }
                final int idx = views.indexOf(vv);
                if (idx < 0) {
                    return;
                }
                final MainTabsManager.Tab tab = tabs.get(idx);
                // Chats is always on (MainTabsManager.loadTabs forces it).
                if (tab.type == MainTabsManager.TabType.CHATS) {
                    return;
                }
                tab.enabled = !tab.enabled;
                vv.setAlpha(tab.enabled ? 1f : 0.35f);
                notifyChanged();
            });
            addView(v, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
            views.add(v);
        }
    }

    private void notifyChanged() {
        if (listener != null) {
            listener.onTabsChanged();
        }
    }

    // ---- drag-to-reorder (edit mode only) ----

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!editMode || tabs == null || views.isEmpty()) {
            return super.onTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX();
                dragIndex = -1;
                for (int i = 0; i < views.size(); i++) {
                    final View child = views.get(i);
                    if (x >= child.getLeft() && x <= child.getRight()) {
                        dragIndex = i;
                        break;
                    }
                }
                dragDownX = x;
                dragArmed = dragIndex >= 0;
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!dragArmed || dragIndex < 0) {
                    return true;
                }
                final GlassTabView dragged = views.get(dragIndex);
                float dx = event.getX() - dragDownX;
                dragged.setTranslationX(dx);
                // swap when the dragged center crosses the neighbor's center
                final int dir = dx > 0 ? 1 : -1;
                final int to = dragIndex + dir;
                if (to >= 0 && to < views.size()) {
                    final View neighbor = views.get(to);
                    final float draggedCenter = dragged.getLeft() + dragged.getWidth() / 2f + dx;
                    final float neighborCenter = neighbor.getLeft() + neighbor.getWidth() / 2f;
                    if ((dir > 0 && draggedCenter > neighborCenter)
                            || (dir < 0 && draggedCenter < neighborCenter)) {
                        Collections.swap(tabs, dragIndex, to);
                        Collections.swap(views, dragIndex, to);
                        removeView(dragged);
                        addView(dragged, to);
                        // keep the dragged tab visually continuous under the finger
                        dragDownX += dir * dragged.getWidth();
                        dx = event.getX() - dragDownX;
                        dragged.setTranslationX(dx);
                        dragIndex = to;
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (dragIndex >= 0) {
                    final GlassTabView dragged = views.get(dragIndex);
                    dragged.animate().translationX(0f).setDuration(180).start();
                    dragIndex = -1;
                    dragArmed = false;
                    notifyChanged();
                }
                return true;
            }
        }
        return true;
    }
}
