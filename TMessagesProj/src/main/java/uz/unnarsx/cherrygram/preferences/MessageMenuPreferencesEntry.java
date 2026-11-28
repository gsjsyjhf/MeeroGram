/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import static uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper.applyNewSpan;
import static uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper.applyProSpan;

import android.os.Build;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.chats.CGMessageMenuInjector;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramMessagesConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.donates.DonatesManager;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class MessageMenuPreferencesEntry extends BaseCGPreferencesEntry {


    private final int messageMenuItemsRow = 7;
    private final int messageMenuItemsCompactViewRow = 8;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("message_menu_preferences_screen");
        return getString(R.string.CP_MessageMenu);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Message_Menu;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        boolean requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
        boolean requireDonate2 = !DonatesManager.INSTANCE.checkAllDonatedAccounts() && !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();

        // MeeroGram R14.38: the message-menu switches (iOS message list + PRO, native
        // blur, open by long press, whole album, remember keyboard state) are gone -
        // the iOS menu behaviour is now built in, so there is nothing left to toggle
        // here. Only the menu contents and the compact layout stay.
        items.add(UItem.asHeader(getString(R.string.LocalMiscellaneousCache)));
        items.add(UItem.asButton(messageMenuItemsRow, R.drawable.msg_list, getString(R.string.CP_MessageMenuItems))
                .slug("menuItems")
        );
        items.add(
                SettingsHelper.asSwitchCG
                        (
                            messageMenuItemsCompactViewRow,
                            applyProSpan(getString(R.string.CP_MessageMenuCompactLayout), getResourceProvider()),
                            getString(R.string.CP_MessageMenuCompactLayout_Desc) + "\n\n" + getString(R.string.CP_MessageMenuCompactLayout_Dot)
                        )
                .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgMenuItemsCompactView())
                .setLocked(requireDonate2)
                .slug("compactMsgMenu")
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        boolean requireDonate;

        if (item.id == messageMenuItemsCompactViewRow) {
            requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccounts() && !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
        } else {
            requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
        }

        var holder = listView.findViewHolderForAdapterPosition(position);
        if (holder == null || !listView.adapter.isEnabled(holder)) {
            return;
        }
        if (requireDonate && item.id != messageMenuItemsRow) {
            AndroidUtilities.shakeViewSpring(view);
            BotWebViewVibrationEffect.APP_ERROR.vibrate();
            showDonateBulletin();
            return;
        }

        if (item.id == messageMenuItemsRow) {
            CGMessageMenuInjector.INSTANCE.showMessageMenuItemsConfigurator(this);
        } else if (item.id == messageMenuItemsCompactViewRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgMenuItemsCompactView(!CherrygramMessagesConfig.INSTANCE.getMsgMenuItemsCompactView());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgMenuItemsCompactView());
        }
    }

}
