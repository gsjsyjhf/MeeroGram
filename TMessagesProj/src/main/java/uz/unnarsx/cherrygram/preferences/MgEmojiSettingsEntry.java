package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import tw.nekomimi.nekogram.helpers.remote.EmojiHelper;
import tw.nekomimi.nekogram.ui.cells.EmojiSetCell;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

/**
 * MeeroGram R11.2: emoji packs screen (the deferred half of R11 item 10),
 * rebuilt on the Cherrygram universal-adapter base instead of the MeeroX
 * NekoX scaffolding. Logic (download/apply/delete/import + progress) ports
 * the frozen MeeroX NekoEmojiSettingsActivity.
 */
public class MgEmojiSettingsEntry extends BaseCGPreferencesEntry implements EmojiHelper.EmojiPackLoadListener, EmojiHelper.EmojiPacksLoadedListener {

    private final int useSystemEmojiRow = 1;
    private final int packsHeaderRow = 2;
    private final int customHeaderRow = 3;
    private final int importRow = 4;

    private final ArrayList<EmojiHelper.EmojiPackBase> packCells = new ArrayList<>();

    private AlertDialog progressDialog;

    @Override
    public boolean onFragmentCreate() {
        EmojiHelper.getInstance().loadEmojisInfo(this);
        EmojiHelper.getInstance().addListener(this);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        EmojiHelper.getInstance().removeListener(this);
        super.onFragmentDestroy();
    }

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.MG_EmojiSetsTitle);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        packCells.clear();

        items.add(SettingsHelper.asSwitchCG(useSystemEmojiRow, getString(R.string.MG_EmojiUseSystem), getString(R.string.MG_EmojiUseSystemDesc))
                .setChecked(CherrygramCoreConfig.INSTANCE.getSystemEmoji())
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.MG_EmojiSets)));
        final ArrayList<EmojiHelper.EmojiPackInfo> emojiPacks = EmojiHelper.getInstance().getEmojiPacksInfo();
        if (emojiPacks.isEmpty()) {
            items.add(UItem.asShadow(getString(R.string.MG_EmojiLoading)));
        } else {
            for (EmojiHelper.EmojiPackInfo pack : emojiPacks) {
                packCells.add(makePackRow(items, pack, true));
            }
        }
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.MG_EmojiMySets)));
        final ArrayList<EmojiHelper.EmojiPackBase> customPacks = EmojiHelper.getInstance().getEmojiCustomPacksInfo();
        for (EmojiHelper.EmojiPackBase pack : customPacks) {
            packCells.add(makePackRow(items, pack, false));
        }
        items.add(UItem.asButton(importRow, 0, getString(R.string.MG_EmojiAddSet)));
        items.add(UItem.asShadow(getString(R.string.MG_EmojiHint)));
    }

    private EmojiHelper.EmojiPackBase makePackRow(ArrayList<UItem> items, EmojiHelper.EmojiPackBase pack, boolean remote) {
        EmojiSetCell cell = new EmojiSetCell(getContext(), true);
        cell.setChecked(!CherrygramCoreConfig.INSTANCE.getSystemEmoji() && pack.getPackId().equals(EmojiHelper.getInstance().getSelectedEmojiPackId()), false);
        cell.setData(pack, false, false);
        cell.setOnClickListener(v -> onPackClicked(cell, pack, remote));
        if (!remote) {
            cell.setOnLongClickListener(v -> {
                EmojiHelper.getInstance().cancelableDelete(this, pack, new EmojiHelper.OnBulletinAction() {
                    @Override
                    public void onPreStart() {
                        updateRows(true);
                    }

                    @Override
                    public void onUndo() {
                        updateRows(true);
                    }
                });
                return true;
            });
        }
        items.add(UItem.asCustom(cell));
        return pack;
    }

    private void onPackClicked(EmojiSetCell cell, EmojiHelper.EmojiPackBase pack, boolean remote) {
        if (CherrygramCoreConfig.INSTANCE.getSystemEmoji()) {
            CherrygramCoreConfig.INSTANCE.setSystemEmoji(false);
        }
        if (remote && pack instanceof EmojiHelper.EmojiPackInfo) {
            EmojiHelper.EmojiPackInfo info = (EmojiHelper.EmojiPackInfo) pack;
            boolean downloading = EmojiHelper.getInstance().isEmojiPackDownloading(info);
            boolean downloaded = EmojiHelper.getInstance().isPackDownloaded(info);
            boolean installed = EmojiHelper.getInstance().isPackInstalled(info);
            if (downloading || (downloaded && !installed)) {
                return;
            }
            if (installed || pack.getPackId().equals("default")) {
                EmojiHelper.getInstance().setEmojiPack(pack.getPackId());
                EmojiHelper.reloadEmoji();
                updateRows(true);
            } else {
                cell.setProgress(true, true);
                EmojiHelper.getInstance().downloadPack(info, false, false);
            }
        } else {
            if (cell.isChecked()) {
                return;
            }
            EmojiHelper.getInstance().setEmojiPack(pack.getPackId());
            EmojiHelper.reloadEmoji();
            updateRows(true);
        }
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == useSystemEmojiRow) {
            CherrygramCoreConfig.INSTANCE.setSystemEmoji(!CherrygramCoreConfig.INSTANCE.getSystemEmoji());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getSystemEmoji());
            EmojiHelper.reloadEmoji();
            updateRows(true);
        } else if (item.id == importRow) {
            startDocumentSelectActivity();
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return super.onLongClick(item, view, position, x, y);
    }

    // ---- SAF import (frozen MeeroX flow) ----

    protected void startDocumentSelectActivity() {
        try {
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            photoPickerIntent.setType("font/*");
            startActivityForResult(photoPickerIntent, 21);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private File copyFileToCache(Uri uri) {
        try (InputStream is = getParentActivity().getContentResolver().openInputStream(uri)) {
            String fileName = MediaController.getFileName(uri);
            File sharingDirectory = AndroidUtilities.getSharingDirectory();
            if (!sharingDirectory.exists() && !sharingDirectory.mkdirs()) {
                return null;
            }
            File dest = new File(sharingDirectory, fileName == null ? "Emoji.ttf" : fileName);
            AndroidUtilities.copyFile(is, dest);
            return dest;
        } catch (IOException e) {
            FileLog.e(e);
        }
        return null;
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode == 21) {
            if (data == null) {
                return;
            }
            progressDialog = new AlertDialog(getParentActivity(), 3);
            progressDialog.setCanCancel(false);
            progressDialog.showDelayed(300);

            Utilities.globalQueue.postRunnable(() -> {
                ArrayList<File> files = new ArrayList<>();
                if (data.getData() != null) {
                    File file = copyFileToCache(data.getData());
                    if (file != null && EmojiHelper.isValidEmojiPack(file)) {
                        files.add(file);
                    }
                } else if (data.getClipData() != null) {
                    ClipData clipData = data.getClipData();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        File file = copyFileToCache(clipData.getItemAt(i).getUri());
                        if (file != null && EmojiHelper.isValidEmojiPack(file)) {
                            files.add(file);
                        }
                    }
                }
                AndroidUtilities.runOnUIThread(() -> {
                    if (!files.isEmpty()) {
                        processFiles(files);
                    } else {
                        BulletinFactory.of(MgEmojiSettingsEntry.this).createErrorBulletin(getString(R.string.MG_EmojiInvalidPack));
                        dismissProgress();
                    }
                });
            });
        }
    }

    private void dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void processFiles(ArrayList<File> files) {
        if (files == null || files.isEmpty()) {
            dismissProgress();
            return;
        }
        if (progressDialog == null) {
            progressDialog = new AlertDialog(getParentActivity(), 3);
            progressDialog.setCanCancel(false);
            progressDialog.showDelayed(300);
        }
        Utilities.globalQueue.postRunnable(() -> {
            int count = 0;
            for (File file : files) {
                try {
                    if (EmojiHelper.getInstance().installEmoji(file) != null) {
                        count++;
                    }
                } catch (Exception e) {
                    FileLog.e("Emoji Font install failed", e);
                }
            }
            int finalCount = count;
            AndroidUtilities.runOnUIThread(() -> {
                dismissProgress();
                if (finalCount > 0) {
                    BulletinFactory.of(this).createSimpleBulletin(R.raw.done, getString(R.string.MG_EmojiInstalled));
                }
                updateRows(true);
            });
        });
    }

    // ---- EmojiHelper listeners ----

    @Override
    public void emojiPacksLoaded(String error) {
        if (error != null) {
            if (BulletinFactory.canShowBulletin(this)) {
                BulletinFactory.of(this).createErrorBulletin(error);
            }
        }
        updateRows(true);
    }

    @Override
    public void progressChanged(EmojiHelper.EmojiPackInfo pack, boolean finished, float progress, long bytesLoaded) {
        if (listView == null) {
            return;
        }
        for (int i = 0; i < listView.getChildCount(); i++) {
            View view = listView.getChildAt(i);
            if (view instanceof EmojiSetCell) {
                EmojiSetCell cell = (EmojiSetCell) view;
                if (cell.getPack() != null && cell.getPack().getPackId().equals(pack.getPackId())) {
                    if (!finished) {
                        if (progress == 100f) {
                            cell.checkDownloaded(true);
                        } else {
                            cell.setProgress(progress, bytesLoaded, true);
                        }
                    } else {
                        cell.checkDownloaded(true);
                    }
                }
            }
        }
    }
}
