package uz.unnarsx.cherrygram.preferences.tabs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.ui.mainTabs.MainTabsManager;

/**
 * MeeroGram reconstruction of the unpublished Cherrygram MainTabsPreviewCell
 * (live preview of the bottom-tabs organizer). Renders an empty placeholder;
 * the organizer itself keeps working through MainTabsManager.
 */
public class MainTabsPreviewCell extends FrameLayout {

    public interface OnTabsChangedListener {
        void onTabsChanged();
    }

    public MainTabsPreviewCell(Context context) {
        super(context);
    }

    public MainTabsPreviewCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEditMode(boolean editMode) {
    }

    public void setOnTabsChangedListener(OnTabsChangedListener listener) {
    }

    public void setTabs(ArrayList<MainTabsManager.Tab> tabs, Context context, Theme.ResourcesProvider resourcesProvider, int currentAccount, boolean animated, boolean attached) {
    }
}
