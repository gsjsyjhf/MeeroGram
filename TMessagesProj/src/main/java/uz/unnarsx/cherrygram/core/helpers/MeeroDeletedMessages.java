/*
 * MeeroGram: local capture of deleted messages and edit history.
 *
 * Telegram forgets a message the moment the server deletes it. This helper
 * keeps a small rolling cache of message texts that were actually loaded by
 * the UI (via NotificationCenter.messagesDidLoad / didReceivedNewMessages)
 * and, when the delete or edit update arrives, snapshots the text into a
 * per-account log that the "الرسائل المحذوفة" screen can display.
 *
 * Everything here is local, best-effort and wrapped in try/catch so a failure
 * can never break the update pipeline.
 */

package uz.unnarsx.cherrygram.core.helpers;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseArray;

import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import uz.unnarsx.cherrygram.core.configs.MeeroDeletedConfig;

public final class MeeroDeletedMessages {

    public static final int MAX_ENTRIES = 250;
    private static final int MAX_CACHE = 4000;

    public static final class Entry {
        public long dialogId;
        public int mid;
        public long date;
        public boolean edit;
        public String text;
        public String newText;
    }

    private static final MeeroDeletedMessages INSTANCE = new MeeroDeletedMessages();

    private final ConcurrentHashMap<Integer, LinkedHashMap<Long, String>> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ArrayList<Entry>> entries = new ConcurrentHashMap<>();
    private final SparseArray<Object> registered = new SparseArray<>();

    private MeeroDeletedMessages() {
    }

    public static MeeroDeletedMessages getInstance() {
        return INSTANCE;
    }

    private static long cacheKey(long dialogId, int mid) {
        return (dialogId << 32) ^ (mid & 0xffffffffL);
    }

    /** Registers the notification observer (idempotent, one per account). */
    public void ensureInit(int account) {
        try {
            if (registered.get(account) != null) {
                return;
            }
            registered.put(account, Boolean.TRUE);
            NotificationCenter.getInstance(account).addObserver(this, NotificationCenter.messagesDidLoad);
            NotificationCenter.getInstance(account).addObserver(this, NotificationCenter.didReceiveNewMessages);
            load(account);
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    private LinkedHashMap<Long, String> cacheFor(int account) {
        LinkedHashMap<Long, String> map = cache.get(account);
        if (map == null) {
            map = new LinkedHashMap<Long, String>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, String> eldest) {
                    return size() > MAX_CACHE;
                }
            };
            cache.put(account, map);
        }
        return map;
    }

    private ArrayList<Entry> entriesFor(int account) {
        ArrayList<Entry> list = entries.get(account);
        if (list == null) {
            list = new ArrayList<>();
            entries.put(account, list);
        }
        return list;
    }

    private static boolean enabled() {
        try {
            return MeeroDeletedConfig.INSTANCE.isEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    private void cacheObjects(int account, List<MessageObject> objects) {
        if (objects == null || objects.isEmpty() || !enabled()) {
            return;
        }
        try {
            LinkedHashMap<Long, String> map = cacheFor(account);
            synchronized (map) {
                for (int a = 0; a < objects.size(); a++) {
                    MessageObject object = objects.get(a);
                    if (object == null || object.messageOwner == null) {
                        continue;
                    }
                    String text = plainText(object);
                    if (TextUtils.isEmpty(text)) {
                        continue;
                    }
                    map.put(cacheKey(object.getDialogId(), object.getId()), text);
                }
            }
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    private static String plainText(MessageObject object) {
        try {
            CharSequence cs = object.messageText;
            if (cs == null) {
                cs = object.caption;
            }
            return cs == null ? null : cs.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    private static String plainText(TLRPC.Message message) {
        try {
            if (message == null) {
                return null;
            }
            if (!TextUtils.isEmpty(message.message)) {
                return message.message;
            }
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    private void addEntry(int account, Entry entry) {
        try {
            ArrayList<Entry> list = entriesFor(account);
            synchronized (list) {
                list.add(0, entry);
                while (list.size() > MAX_ENTRIES) {
                    list.remove(list.size() - 1);
                }
            }
            save(account);
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    /** Deletion of messages inside a known dialog (channels / supergroups). */
    public void captureDeleted(int account, long dialogId, ArrayList<Integer> mids) {
        if (mids == null || mids.isEmpty() || !enabled()) {
            return;
        }
        try {
            ensureInit(account);
            LinkedHashMap<Long, String> map = cacheFor(account);
            for (int a = 0; a < mids.size(); a++) {
                Integer mid = mids.get(a);
                if (mid == null) {
                    continue;
                }
                String text;
                synchronized (map) {
                    text = map.remove(cacheKey(dialogId, mid));
                }
                if (TextUtils.isEmpty(text)) {
                    continue;
                }
                Entry entry = new Entry();
                entry.dialogId = dialogId;
                entry.mid = mid;
                entry.date = System.currentTimeMillis();
                entry.text = text;
                addEntry(account, entry);
            }
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    /**
     * Deletion without a known dialog id (private chats / basic groups):
     * the ids are looked up in the rolling cache.
     */
    public void captureDeletedUnknownDialog(int account, ArrayList<Integer> mids) {
        if (mids == null || mids.isEmpty() || !enabled()) {
            return;
        }
        try {
            ensureInit(account);
            LinkedHashMap<Long, String> map = cacheFor(account);
            for (int a = 0; a < mids.size(); a++) {
                Integer mid = mids.get(a);
                if (mid == null) {
                    continue;
                }
                long foundKey = Long.MIN_VALUE;
                String text = null;
                synchronized (map) {
                    for (Iterator<Map.Entry<Long, String>> it = map.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<Long, String> e = it.next();
                        if (((int) (e.getKey() & 0xffffffffL)) == (int) mid) {
                            foundKey = e.getKey();
                            text = e.getValue();
                            it.remove();
                            break;
                        }
                    }
                }
                if (foundKey == Long.MIN_VALUE || TextUtils.isEmpty(text)) {
                    continue;
                }
                Entry entry = new Entry();
                entry.dialogId = foundKey >> 32;
                entry.mid = mid;
                entry.date = System.currentTimeMillis();
                entry.text = text;
                addEntry(account, entry);
            }
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    /** Old version of an edited message (new text is kept from the update). */
    public void captureEdit(int account, long dialogId, TLRPC.Message message) {
        try {
            if (message == null || !MeeroDeletedConfig.INSTANCE.getKeepEditsHistory()) {
                return;
            }
            ensureInit(account);
            LinkedHashMap<Long, String> map = cacheFor(account);
            String oldText;
            synchronized (map) {
                oldText = map.get(cacheKey(dialogId, message.id));
            }
            String newText = plainText(message);
            if (TextUtils.isEmpty(oldText)) {
                oldText = "";
            }
            if (TextUtils.isEmpty(newText)) {
                newText = "";
            }
            if (oldText.equals(newText)) {
                return;
            }
            synchronized (map) {
                map.put(cacheKey(dialogId, message.id), newText);
            }
            Entry entry = new Entry();
            entry.dialogId = dialogId;
            entry.mid = message.id;
            entry.date = System.currentTimeMillis();
            entry.edit = true;
            entry.text = oldText;
            entry.newText = newText;
            addEntry(account, entry);
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    public ArrayList<Entry> getEntries(int account, boolean editsOnly) {
        ArrayList<Entry> result = new ArrayList<>();
        try {
            ArrayList<Entry> list = entriesFor(account);
            synchronized (list) {
                for (int a = 0; a < list.size(); a++) {
                    Entry entry = list.get(a);
                    if (editsOnly && !entry.edit) {
                        continue;
                    }
                    if (!editsOnly && entry.edit) {
                        continue;
                    }
                    result.add(entry);
                }
            }
        } catch (Throwable t) {
            FileLog.e(t);
        }
        return result;
    }

    public void clear(int account) {
        try {
            ArrayList<Entry> list = entriesFor(account);
            synchronized (list) {
                list.clear();
            }
            save(account);
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    // ---- persistence -------------------------------------------------------

    private static SharedPreferences prefs(int account) {
        return org.telegram.messenger.ApplicationLoader.applicationContext
                .getSharedPreferences("meerogram_deleted_log_" + account, android.app.Activity.MODE_PRIVATE);
    }

    private static String enc(String value) {
        if (value == null) {
            return "";
        }
        try {
            return Base64.encodeToString(value.getBytes("UTF-8"), Base64.NO_WRAP);
        } catch (Throwable t) {
            return "";
        }
    }

    private static String dec(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        try {
            byte[] bytes = Base64.decode(value, Base64.NO_WRAP);
            return new String(bytes, "UTF-8");
        } catch (Throwable t) {
            return "";
        }
    }

    private void save(int account) {
        try {
            StringBuilder sb = new StringBuilder();
            ArrayList<Entry> list = entriesFor(account);
            synchronized (list) {
                for (int a = 0; a < list.size(); a++) {
                    Entry entry = list.get(a);
                    sb.append(entry.dialogId).append(';')
                            .append(entry.mid).append(';')
                            .append(entry.date).append(';')
                            .append(entry.edit ? 1 : 0).append(';')
                            .append(enc(entry.text)).append(';')
                            .append(enc(entry.newText)).append('\n');
                }
            }
            prefs(account).edit().putString("log", sb.toString()).apply();
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    private void load(int account) {
        try {
            String raw = prefs(account).getString("log", "");
            if (TextUtils.isEmpty(raw)) {
                return;
            }
            ArrayList<Entry> list = entriesFor(account);
            String[] lines = raw.split("\n");
            synchronized (list) {
                list.clear();
                for (int a = 0; a < lines.length && list.size() < MAX_ENTRIES; a++) {
                    String line = lines[a];
                    if (TextUtils.isEmpty(line)) {
                        continue;
                    }
                    String[] parts = line.split(";", -1);
                    if (parts.length < 6) {
                        continue;
                    }
                    try {
                        Entry entry = new Entry();
                        entry.dialogId = Long.parseLong(parts[0]);
                        entry.mid = Integer.parseInt(parts[1]);
                        entry.date = Long.parseLong(parts[2]);
                        entry.edit = "1".equals(parts[3]);
                        entry.text = dec(parts[4]);
                        entry.newText = dec(parts[5]);
                        list.add(entry);
                    } catch (Throwable ignore) {
                    }
                }
            }
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    // ---- notifications ----------------------------------------------------

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        try {
            if (id == NotificationCenter.messagesDidLoad) {
                if (args.length > 2 && args[2] instanceof List) {
                    //noinspection unchecked
                    cacheObjects(account, (List<MessageObject>) args[2]);
                }
            } else if (id == NotificationCenter.didReceiveNewMessages) {
                if (args.length > 1 && args[1] instanceof List) {
                    //noinspection unchecked
                    cacheObjects(account, (List<MessageObject>) args[1]);
                }
            }
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    /** Convenience for the settings screen: "N saved messages". */
    public static int countFor(int account) {
        try {
            List<Entry> list = INSTANCE.getEntries(account, false);
            return list == null ? 0 : list.size();
        } catch (Throwable t) {
            return 0;
        }
    }

    public static CharSequence dialogTitle(long dialogId) {
        try {
            return org.telegram.messenger.MessagesController.getInstance(
                    org.telegram.messenger.UserConfig.selectedAccount
            ).getPeerName(dialogId, true);
        } catch (Throwable t) {
            return String.valueOf(dialogId);
        }
    }
}
