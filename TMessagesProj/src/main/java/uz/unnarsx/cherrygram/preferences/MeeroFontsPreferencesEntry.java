/**
 * This is the source code of Meerogram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 *
 * MeeroGram R11.1: custom font picker (ported from frozen MeeroX
 * MeeroFontsActivity) in the Cherrygram preferences style.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.provider.OpenableColumns;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.io.InputStream;
import java.util.ArrayList;

import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;
import tw.nekomimi.nekogram.MeeroFonts;

public class MeeroFontsPreferencesEntry extends BaseCGPreferencesEntry {

    private static final int PICK_FONT = 4711;

    /** \u0647\u064a\u062c \u064a\u0635\u064a\u0631 \u0627\u0644\u062e\u0637 - the same sample on every row. */
    private static final String FONT_SAMPLE = "\u0647\u064a\u062c \u064a\u0635\u064a\u0631 \u0627\u0644\u062e\u0637";

    private final int fontsHeaderRow = 1;
    private int fontsStartRow = 2;
    private int fontsEndRow = 2;
    private int importRow = 2;

    private ArrayList<MeeroFonts.Option> options;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.MG_Fonts_Title);
    }

    @Override
    protected String getKey() {
        return null;
    }

    private void rebuildOptions() {
        options = MeeroFonts.getOptions();
        fontsStartRow = fontsHeaderRow + 1;
        fontsEndRow = fontsStartRow + options.size();
        importRow = fontsEndRow;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        rebuildOptions();
        items.add(UItem.asShadow("MeeroGram R14.13 \u00b7 build 7324\n" + getString(R.string.MG_Fonts_Desc)));
        for (int i = 0; i < options.size(); i++) {
            MeeroFonts.Option o = options.get(i);
            boolean selected = o.id.equals(MeeroFonts.getSelected());
            // R14.7: every row shows ONLY the sample sentence, in that row's own
            // face - the old font names are gone on request.
            UItem row = UItem.asButton(fontsStartRow + i, FONT_SAMPLE,
                    selected ? getString(R.string.MG_Fonts_Selected) : "")
                    .slug("meeroFontRow");
            // show the name rendered in the font itself
            try {
                row.object = MeeroFonts.previewOf(o.id);
            } catch (Throwable ignore) {
            }
            items.add(row);
        }
        items.add(UItem.asButton(importRow, getString(R.string.MG_Fonts_Import))
                .slug("meeroFontImport"));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id >= fontsStartRow && item.id < fontsEndRow) {
            MeeroFonts.Option o = options.get(item.id - fontsStartRow);
            MeeroFonts.setSelected(o.id);
            AndroidUtilities.clearTypefaceCache();
            updateRows(true);
            if (getParentLayout() != null) {
                getParentLayout().rebuildAllFragmentViews(true, true);
            }
        } else if (item.id == importRow) {
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                getParentActivity().startActivityForResult(intent, PICK_FONT);
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
    }

    @Override
    protected boolean handleItemLongClick(UniversalRecyclerView list, UItem item, View view, int position, float x, float y) {
        if (item.id >= fontsStartRow && item.id < fontsEndRow) {
            MeeroFonts.Option o = options.get(item.id - fontsStartRow);
            if (MeeroFonts.deleteCustom(o.id)) {
                AndroidUtilities.clearTypefaceCache();
                updateRows(true);
                BulletinFactory.of(this).createSimpleBulletin(getString(R.string.MG_Fonts_Deleted), o.title).show();
                return true;
            }
        }
        return super.handleItemLongClick(list, item, view, position, x, y);
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FONT && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String name = null;
            try {
                Cursor c = ApplicationLoader.applicationContext.getContentResolver().query(uri, null, null, null, null);
                if (c != null) {
                    try {
                        if (c.moveToFirst()) {
                            int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                            if (idx >= 0) {
                                name = c.getString(idx);
                            }
                        }
                    } finally {
                        c.close();
                    }
                }
            } catch (Throwable ignore) {
            }
            if (name == null) {
                name = uri.getLastPathSegment();
            }
            try {
                InputStream stream = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri);
                String id = MeeroFonts.importFont(stream, name);
                if (id != null) {
                    MeeroFonts.setSelected(id);
                    AndroidUtilities.clearTypefaceCache();
                    updateRows(true);
                    if (getParentLayout() != null) {
                        getParentLayout().rebuildAllFragmentViews(true, true);
                    }
                    BulletinFactory.of(this).createSimpleBulletin(getString(R.string.MG_Fonts_Imported), name).show();
                } else {
                    BulletinFactory.of(this).createSimpleBulletin(getString(R.string.MG_Fonts_Error), name).show();
                }
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
    }
}
