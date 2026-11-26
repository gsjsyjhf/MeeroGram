/**
 * MeeroGram: "وضع الشبح" (Ghost mode) settings screen.
 * GPL v.2 or later, following the Cherrygram preferences style.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import android.view.View;

import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.MeeroGhostConfig;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class GhostModePreferencesEntry extends BaseCGPreferencesEntry {

    private final int ghostMasterRow = 1;
    private final int ghostOnlineRow = 2;
    private final int ghostReadRow = 3;
    private final int ghostTypingRow = 4;
    private final int ghostStoriesRow = 5;
    private final int ghostStoryAlertRow = 6;
    private final int deletedSectionRow = 7;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.MG_Ghost_Header);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Ghost;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asShadow("MeeroGram R14.11 \u00b7 build 7321\n" + getString(R.string.MG_Ghost_Desc)));
        items.add(
                SettingsHelper.asSwitchCG(ghostMasterRow, getString(R.string.MG_Ghost_Master))
                        .setChecked(MeeroGhostConfig.INSTANCE.getGhostMaster())
        );
        items.add(
                SettingsHelper.asSwitchCG(ghostOnlineRow, getString(R.string.MG_Ghost_Online), getString(R.string.MG_Ghost_Online_Desc))
                        .setChecked(MeeroGhostConfig.INSTANCE.getGhostHideOnline())
                        .setEnabled(MeeroGhostConfig.INSTANCE.getGhostMaster())
        );
        items.add(
                SettingsHelper.asSwitchCG(ghostReadRow, getString(R.string.MG_Ghost_Read), getString(R.string.MG_Ghost_Read_Desc))
                        .setChecked(MeeroGhostConfig.INSTANCE.getGhostHideReadReceipts())
                        .setEnabled(MeeroGhostConfig.INSTANCE.getGhostMaster())
        );
        items.add(
                SettingsHelper.asSwitchCG(ghostTypingRow, getString(R.string.MG_Ghost_Typing), getString(R.string.MG_Ghost_Typing_Desc))
                        .setChecked(MeeroGhostConfig.INSTANCE.getGhostHideTyping())
                        .setEnabled(MeeroGhostConfig.INSTANCE.getGhostMaster())
        );
        items.add(
                SettingsHelper.asSwitchCG(ghostStoriesRow, getString(R.string.MG_Ghost_Stories), getString(R.string.MG_Ghost_Stories_Desc))
                        .setChecked(MeeroGhostConfig.INSTANCE.getGhostHideStories())
                        .setEnabled(MeeroGhostConfig.INSTANCE.getGhostMaster())
        );
        // MeeroGram R14: explicit alert before watching a story.
        items.add(
                SettingsHelper.asSwitchCG(ghostStoryAlertRow, getString(R.string.MG_Ghost_StoryAlert), getString(R.string.MG_Ghost_StoryAlert_Desc))
                        .setChecked(MeeroGhostConfig.INSTANCE.getGhostStoryAlert())
        );
        items.add(UItem.asShadow(null));
        // MeeroGram R14: dedicated deleted-messages section, opened from here.
        items.add(
                UItem.asButton(deletedSectionRow, getString(R.string.MG_Deleted_Header), getString(R.string.MG_Deleted_Row_Desc))
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == ghostMasterRow) {
            MeeroGhostConfig.INSTANCE.setGhostMaster(!MeeroGhostConfig.INSTANCE.getGhostMaster());
            updateRows(true);
        } else if (item.id == ghostOnlineRow) {
            MeeroGhostConfig.INSTANCE.setGhostHideOnline(!MeeroGhostConfig.INSTANCE.getGhostHideOnline());
            updateRows(true);
        } else if (item.id == ghostReadRow) {
            MeeroGhostConfig.INSTANCE.setGhostHideReadReceipts(!MeeroGhostConfig.INSTANCE.getGhostHideReadReceipts());
            updateRows(true);
        } else if (item.id == ghostTypingRow) {
            MeeroGhostConfig.INSTANCE.setGhostHideTyping(!MeeroGhostConfig.INSTANCE.getGhostHideTyping());
            updateRows(true);
        } else if (item.id == ghostStoriesRow) {
            MeeroGhostConfig.INSTANCE.setGhostHideStories(!MeeroGhostConfig.INSTANCE.getGhostHideStories());
            updateRows(true);
        }  else if (item.id == ghostStoryAlertRow) {
            MeeroGhostConfig.INSTANCE.setGhostStoryAlert(!MeeroGhostConfig.INSTANCE.getGhostStoryAlert());
            updateRows(true);
        } else if (item.id == deletedSectionRow) {
            presentFragment(new MeeroDeletedPreferencesEntry());
        }
    }
}
