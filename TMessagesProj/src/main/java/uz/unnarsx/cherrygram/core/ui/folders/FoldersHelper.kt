/*
 * MeeroGram reconstruction of the unpublished Cherrygram FoldersHelper.
 * The real implementation moves the folder tabs into a bottom glass panel;
 * that module is not published, so this stub keeps the standard top tabs and
 * returns safe defaults for every call site in GlassTabView/DialogsActivity.
 */
package uz.unnarsx.cherrygram.core.ui.folders

import android.content.Context
import android.graphics.RectF
import android.view.ViewGroup

import org.telegram.ui.Components.FilterTabsView
import org.telegram.ui.Components.blur3.BlurredBackgroundDrawableViewFactory
import org.telegram.ui.Components.blur3.DownscaleScrollableNoiseSuppressor
import org.telegram.ui.Components.blur3.capture.IBlur3Capture
import org.telegram.ui.DialogsActivity

object FoldersHelper {

    @JvmStatic
    fun moveFoldersToBottom(): Boolean = false

    @JvmStatic
    fun getFloatingButtonsOffset(filterTabsView: FilterTabsView?): Int = 0

    @JvmStatic
    fun setupFilterTabs(
        context: Context?,
        contentView: ViewGroup?,
        filterTabsView: FilterTabsView?,
        resourceProvider: org.telegram.ui.ActionBar.Theme.ResourcesProvider?,
        iBlur3FactoryLiquidGlass: BlurredBackgroundDrawableViewFactory?,
        iBlur3FactoryFade: BlurredBackgroundDrawableViewFactory?,
        inForwardMode: Boolean
    ) {
    }

    @JvmStatic
    fun blur3_InvalidateBlur(
        dialogsActivity: DialogsActivity?,
        iBlur3Capture: IBlur3Capture?,
        iBlur3PositionActionBar: RectF?,
        iBlur3PositionFolders: RectF?,
        iBlur3PositionMainTabs: RectF?,
        iBlur3Positions: ArrayList<RectF>?,
        scrollableViewNoiseSuppressor: DownscaleScrollableNoiseSuppressor?
    ) {
    }

    @JvmStatic
    fun updateFoldersOffset(dialogsActivity: DialogsActivity?, inForwardMode: Boolean) {
    }
}
