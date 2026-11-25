/*
 * MeeroGram: "الرسائل المحذوفة" (deleted messages) settings screen.
 * Its own dedicated section, opened from the Ghost mode screen.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import android.view.View;

import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.MeeroDeletedConfig;
import uz.unnarsx.cherrygram.core.helpers.MeeroDeletedMessages;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class MeeroDeletedPreferencesEntry extends BaseCGPreferencesEntry {

    private final int deletedKeepRow = 1;
    private final int deletedEditsRow = 2;
    private final int deletedViewRow = 3;
    private final int deletedClearRow = 4;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.MG_Deleted_Header);
    }

    @Override
    protected String getKey() {
        return "meero_deleted_messages";
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        final int account = UserConfig.selectedAccount;
        items.add(UItem.asShadow(getString(R.string.MG_Deleted_Intro)));
        items.add(
                SettingsHelper.asSwitchCG(deletedKeepRow, getString(R.string.MG_Deleted_Keep), getString(R.string.MG_Deleted_Keep_Desc))
                        .setChecked(MeeroDeletedConfig.INSTANCE.getKeepDeletedMessages())
        );
        items.add(
                SettingsHelper.asSwitchCG(deletedEditsRow, getString(R.string.MG_Deleted_Edits), getString(R.string.MG_Deleted_Edits_Desc))
                        .setChecked(MeeroDeletedConfig.INSTANCE.getKeepEditsHistory())
        );
        int count = MeeroDeletedMessages.countFor(account);
        items.add(
                UItem.asButton(deletedViewRow, getString(R.string.MG_Deleted_View),
                        count == 0 ? getString(R.string.MG_Deleted_Count_Zero) : LocaleControllerString(count))
        );
        if (count > 0) {
            items.add(
                    UItem.asButton(deletedClearRow, getString(R.string.MG_Deleted_Clear))
            );
        }
        items.add(UItem.asShadow(null));
    }

    private static String LocaleControllerString(int count) {
        try {
            return org.telegram.messenger.LocaleController.formatPluralString("Messages", count);
        } catch (Throwable t) {
            return String.valueOf(count);
        }
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == deletedKeepRow) {
            MeeroDeletedConfig.INSTANCE.setKeepDeletedMessages(!MeeroDeletedConfig.INSTANCE.getKeepDeletedMessages());
            if (MeeroDeletedConfig.INSTANCE.getKeepDeletedMessages()) {
                MeeroDeletedMessages.getInstance().ensureInit(UserConfig.selectedAccount);
            }
            updateRows(true);
        } else if (item.id == deletedEditsRow) {
            MeeroDeletedConfig.INSTANCE.setKeepEditsHistory(!MeeroDeletedConfig.INSTANCE.getKeepEditsHistory());
            if (MeeroDeletedConfig.INSTANCE.getKeepEditsHistory()) {
                MeeroDeletedMessages.getInstance().ensureInit(UserConfig.selectedAccount);
            }
            updateRows(true);
        } else if (item.id == deletedViewRow) {
            showDeletedMessagesLog();
        } else if (item.id == deletedClearRow) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(getString(R.string.MG_Deleted_Clear));
            builder.setMessage(getString(R.string.MG_Deleted_Clear_Confirm));
            builder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                MeeroDeletedMessages.getInstance().clear(UserConfig.selectedAccount);
                updateRows(true);
            });
            builder.setNegativeButton(getString(R.string.Cancel), null);
            showDialog(builder.create());
        }
    }

    /** Local log of deleted messages and old edit versions. */
    private void showDeletedMessagesLog() {
        if (getParentActivity() == null) {
            return;
        }
        final int account = UserConfig.selectedAccount;
        final ArrayList<MeeroDeletedMessages.Entry> list = new ArrayList<>();
        list.addAll(MeeroDeletedMessages.getInstance().getEntries(account, false));
        list.addAll(MeeroDeletedMessages.getInstance().getEntries(account, true));
        java.util.Collections.sort(list, (o1, o2) -> Long.compare(o2.date, o1.date));
        while (list.size() > 60) {
            list.remove(list.size() - 1);
        }
        if (list.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(getString(R.string.MG_Deleted_Header));
            builder.setMessage(getString(R.string.MG_Deleted_Empty));
            builder.setPositiveButton(getString(R.string.OK), null);
            showDialog(builder.create());
            return;
        }
        final String[] items = new String[list.size()];
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault());
        for (int a = 0; a < items.length; a++) {
            MeeroDeletedMessages.Entry entry = list.get(a);
            String peer;
            try {
                peer = org.telegram.messenger.MessagesController.getInstance(account).getPeerName(entry.dialogId, true);
            } catch (Throwable t) {
                peer = String.valueOf(entry.dialogId);
            }
            String when = format.format(new java.util.Date(entry.date));
            String text = entry.text == null ? "" : entry.text.replace('\n', ' ');
            if (entry.edit) {
                String newText = entry.newText == null ? "" : entry.newText.replace('\n', ' ');
                items[a] = "[" + when + "] " + peer + "\n" + text + "  \u2192  " + newText;
            } else {
                items[a] = "[" + when + "] " + peer + "\n" + text;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(getString(R.string.MG_Deleted_Header));
        builder.setItems(items, (dialog, which) -> {
        });
        builder.setPositiveButton(getString(R.string.OK), null);
        showDialog(builder.create());
    }
}
