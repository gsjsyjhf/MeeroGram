/*
 * MeeroGram: "الرسائل المحذوفة" (deleted messages) settings screen.
 * Its own dedicated section, opened from the Ghost mode screen.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
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
    /* R14.16: AyuGram's message-saving options, same order/semantics. */
    private final int deletedReactionsRow = 5;
    private final int deletedBotsRow = 6;
    private final int deletedMediaRow = 7;
    private final int deletedColorRow = 8;
    private final int deletedIconRow = 9;
    private final int deletedStorageRow = 10;

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
        items.add(
                SettingsHelper.asSwitchCG(deletedReactionsRow, getString(R.string.MG_Deleted_Reactions), getString(R.string.MG_Deleted_Reactions_Desc))
                        .setChecked(MeeroDeletedConfig.INSTANCE.getSaveReactions())
        );
        items.add(
                SettingsHelper.asSwitchCG(deletedBotsRow, getString(R.string.MG_Deleted_Bots), getString(R.string.MG_Deleted_Bots_Desc))
                        .setChecked(MeeroDeletedConfig.INSTANCE.getSaveBots())
        );
        items.add(
                SettingsHelper.asSwitchCG(deletedMediaRow, getString(R.string.MG_Deleted_Media), getString(R.string.MG_Deleted_Media_Desc))
                        .setChecked(MeeroDeletedConfig.INSTANCE.getSaveMedia())
        );
        items.add(UItem.asButton(deletedColorRow, getString(R.string.MG_Deleted_Color), getString(R.string.MG_Deleted_Color_Value)));
        items.add(UItem.asButton(deletedIconRow, getString(R.string.MG_Deleted_Icon), getString(R.string.MG_Deleted_Icon_Value)));
        // MeeroGram R14.20: the log is a real SQLite database now (AyuGram-style schema).
        items.add(UItem.asButton(deletedStorageRow, getString(R.string.MG_Deleted_Storage), getString(R.string.MG_Deleted_Storage_Value)));
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
        } else if (item.id == deletedReactionsRow) {
            MeeroDeletedConfig.INSTANCE.setSaveReactions(!MeeroDeletedConfig.INSTANCE.getSaveReactions());
            updateRows(true);
        } else if (item.id == deletedBotsRow) {
            MeeroDeletedConfig.INSTANCE.setSaveBots(!MeeroDeletedConfig.INSTANCE.getSaveBots());
            updateRows(true);
        } else if (item.id == deletedMediaRow) {
            MeeroDeletedConfig.INSTANCE.setSaveMedia(!MeeroDeletedConfig.INSTANCE.getSaveMedia());
            updateRows(true);
        } else if (item.id == deletedColorRow) {
            showColorPicker();
        } else if (item.id == deletedIconRow) {
            showIconPicker();
        } else if (item.id == deletedStorageRow) {
            showStorageInfo();
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

    /** Storage details: where the SQLite database lives and how big it is. */
    private void showStorageInfo() {
        if (getParentActivity() == null) {
            return;
        }
        final int account = UserConfig.selectedAccount;
        String info;
        try {
            java.io.File file = MeeroDeletedMessages.dbFile();
            final long size = file.exists() ? file.length() : 0;
            info = getString(R.string.MG_Deleted_Storage) + ": SQLite\n"
                    + MeeroDeletedMessages.dbPath() + "\n"
                    + "(" + android.text.format.Formatter.formatShortFileSize(getParentActivity(), size) + ")\n"
                    + LocaleControllerString(MeeroDeletedMessages.countFor(account));
        } catch (Throwable t) {
            info = MeeroDeletedMessages.dbPath();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(getString(R.string.MG_Deleted_Storage));
        builder.setMessage(info);
        builder.setPositiveButton(getString(R.string.OK), null);
        showDialog(builder.create());
    }

    /** Builds a vector-drawable marker span for a recovered entry row. */
    private android.text.style.ImageSpan meeroIconSpan(int resId, int color) {
        try {
            android.graphics.drawable.Drawable drawable = getParentActivity() != null
                    ? getParentActivity().getResources().getDrawable(resId, getParentActivity().getTheme())
                    : null;
            if (drawable == null) {
                drawable = androidx.core.content.ContextCompat.getDrawable(
                        org.telegram.messenger.ApplicationLoader.applicationContext, resId);
            }
            int size = AndroidUtilities.dp(14);
            drawable.setBounds(0, 0, size, size);
            drawable.setColorFilter(color != 0 ? color : Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2),
                    android.graphics.PorterDuff.Mode.SRC_IN);
            return new android.text.style.ImageSpan(drawable, android.text.style.ImageSpan.ALIGN_BOTTOM);
        } catch (Throwable t) {
            return null;
        }
    }

    /** Drawable marker + label, used by the icon picker so no emoji is shown anywhere. */
    private CharSequence meeroIconLabel(int resId, String label, int color) {
        SpannableString ss = new SpannableString("  " + label);
        android.text.style.ImageSpan span = meeroIconSpan(resId, color);
        if (span != null) {
            ss.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    /** AyuGram-style colour picker for recovered entries. */
    private void showColorPicker() {
        if (getParentActivity() == null) {
            return;
        }
        final CharSequence[] names = new CharSequence[]{
                getString(R.string.MG_Deleted_Color_Default),
                "\u0623\u062d\u0645\u0631", "\u0628\u0631\u062a\u0642\u0627\u0644\u064a",
                "\u0628\u0646\u0641\u0633\u062c\u064a", "\u0623\u0632\u0631\u0642", "\u0623\u062e\u0636\u0631"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(getString(R.string.MG_Deleted_Color));
        builder.setItems(names, (dialog, which) -> {
            MeeroDeletedConfig.INSTANCE.setEntryColor(which == 0 ? 0 : MeeroDeletedConfig.INSTANCE.getENTRY_COLORS()[which - 1]);
            updateRows(true);
        });
        builder.setNegativeButton(getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    /** AyuGram-style marker drawn in front of a recovered message. */
    private void showIconPicker() {
        if (getParentActivity() == null) {
            return;
        }
        final int color = MeeroDeletedConfig.INSTANCE.getEntryColor();
        final CharSequence[] names = new CharSequence[]{
                getString(R.string.MG_Deleted_Icon_None),
                meeroIconLabel(org.telegram.messenger.R.drawable.meero_ic_deleted, getString(R.string.MG_Deleted_Icon_Trash), color),
                meeroIconLabel(org.telegram.messenger.R.drawable.meero_ic_edited, getString(R.string.MG_Deleted_Icon_Pencil), color),
                meeroIconLabel(org.telegram.messenger.R.drawable.meero_ic_eye_off, getString(R.string.MG_Deleted_Icon_Eye), color),
                meeroIconLabel(org.telegram.messenger.R.drawable.meero_ic_alert, getString(R.string.MG_Deleted_Icon_Alert), color)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(getString(R.string.MG_Deleted_Icon));
        builder.setItems(names, (dialog, which) -> {
            MeeroDeletedConfig.INSTANCE.setEntryIcon(which);
            updateRows(true);
        });
        builder.setNegativeButton(getString(R.string.Cancel), null);
        showDialog(builder.create());
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
        final CharSequence[] items = new CharSequence[list.size()];
        final int iconIndex = Math.max(0, Math.min(MeeroDeletedConfig.INSTANCE.getEntryIcon(), MeeroDeletedConfig.INSTANCE.getENTRY_ICON_DRAWABLES().length - 1));
        final int iconRes = MeeroDeletedConfig.INSTANCE.getENTRY_ICON_DRAWABLES()[iconIndex];
        final int color = MeeroDeletedConfig.INSTANCE.getEntryColor();
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
            SpannableStringBuilder body = new SpannableStringBuilder();
            if (iconRes != 0) {
                // R14.21: vector marker (ImageSpan) instead of an emoji glyph.
                SpannableString marker = new SpannableString("  ");
                marker.setSpan(meeroIconSpan(iconRes, color), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                body.append(marker).append(' ');
            }
            if (entry.edit) {
                String newText = entry.newText == null ? "" : entry.newText.replace('\n', ' ');
                body.append(text).append("  \u2192  ").append(newText);
            } else {
                body.append(text);
                if (entry.mediaLabel != null) {
                    body.append(' ').append(entry.mediaLabel);
                }
                if (entry.reactions != null) {
                    body.append("   ").append(entry.reactions);
                }
            }
            if (color != 0) {
                body.setSpan(new ForegroundColorSpan(color), 0, body.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            items[a] = TextUtils.concat("[" + when + "] " + peer + "\n", body);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(getString(R.string.MG_Deleted_Header));
        builder.setItems(items, (dialog, which) -> {
        });
        builder.setPositiveButton(getString(R.string.OK), null);
        showDialog(builder.create());
    }
}
