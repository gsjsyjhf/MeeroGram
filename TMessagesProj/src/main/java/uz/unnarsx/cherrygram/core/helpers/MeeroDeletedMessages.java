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
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uz.unnarsx.cherrygram.core.configs.MeeroDeletedConfig;

public final class MeeroDeletedMessages implements NotificationCenter.NotificationCenterDelegate {

    /** MeeroGram R14.20: real storage, the whole history is kept in SQLite. */
    public static final int MAX_ENTRIES = 5000;
    private static final int MAX_CACHE = 4000;

    public static final class Entry {
        public long dialogId;
        public int mid;
        public long date;
        public boolean edit;
        public String text;
        public String newText;
        /* R14.16 (AyuGram parity) */
        public String mediaLabel;
        public String reactions;
        public boolean fromBot;
    }

    private static final MeeroDeletedMessages INSTANCE = new MeeroDeletedMessages();

    private final ConcurrentHashMap<Integer, LinkedHashMap<Long, String>> cache = new ConcurrentHashMap<>();
    /** cacheKey -> "bot|media|reactions" snapshot taken while the message was still loaded. */
    private final ConcurrentHashMap<Integer, LinkedHashMap<Long, String>> metaCache = new ConcurrentHashMap<>();
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

    private LinkedHashMap<Long, String> metaFor(int account) {
        LinkedHashMap<Long, String> map = metaCache.get(account);
        if (map == null) {
            map = new LinkedHashMap<Long, String>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, String> eldest) {
                    return size() > MAX_CACHE;
                }
            };
            metaCache.put(account, map);
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
                    String meta = describe(object);
                    if (TextUtils.isEmpty(text) && !(MeeroDeletedConfig.INSTANCE.getSaveMedia() && !TextUtils.isEmpty(meta))) {
                        continue;
                    }
                    long key = cacheKey(object.getDialogId(), object.getId());
                    if (!TextUtils.isEmpty(text)) {
                        map.put(key, text);
                    }
                    if (!TextUtils.isEmpty(meta)) {
                        synchronized (metaFor(account)) {
                            metaFor(account).put(key, meta);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    /**
     * AyuGram-style snapshot of everything we can still see while the message is loaded:
     * bot flag, media kind and the reactions that were attached to it.
     */
    private static String describe(MessageObject object) {
        try {
            StringBuilder sb = new StringBuilder();
            TLRPC.Message owner = object.messageOwner;
            if (owner != null && owner.from_id != null) {
                TLRPC.User user = org.telegram.messenger.MessagesController.getInstance(object.currentAccount)
                        .getUser(MessageObject.getPeerId(owner.from_id));
                if (user != null && user.bot) {
                    sb.append("bot");
                }
            }
            String media = mediaLabel(owner);
            if (media != null) {
                if (sb.length() > 0) sb.append('|');
                sb.append(media);
            }
            String reactions = reactionsLabel(owner);
            if (reactions != null) {
                if (sb.length() > 0) sb.append('|');
                sb.append(reactions);
            }
            return sb.toString();
        } catch (Throwable t) {
            return "";
        }
    }

    private static String reactionsLabel(TLRPC.Message owner) {
        try {
            if (owner == null || owner.reactions == null || owner.reactions.results == null) return null;
            StringBuilder sb = new StringBuilder();
            for (int a = 0; a < owner.reactions.results.size(); a++) {
                TLRPC.ReactionCount rc = owner.reactions.results.get(a);
                if (rc == null || rc.reaction instanceof TLRPC.TL_reactionEmpty) continue;
                String emoji = null;
                if (rc.reaction instanceof TLRPC.TL_reactionEmoji) {
                    emoji = ((TLRPC.TL_reactionEmoji) rc.reaction).emoticon;
                }
                if (TextUtils.isEmpty(emoji)) continue;
                if (sb.length() > 0) sb.append(' ');
                sb.append(emoji).append(rc.count);
            }
            return sb.length() == 0 ? null : sb.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    /** Human placeholder for a media-only post, AyuGram keeps the attachment itself. */
    private static String mediaLabel(TLRPC.Message owner) {
        try {
            if (owner == null || owner.media == null) return null;
            if (owner.media instanceof TLRPC.TL_messageMediaPhoto) return "\uD83D\uDCF7";
            if (owner.media instanceof TLRPC.TL_messageMediaDocument) {
                if (owner.media.round) return "\uD83C\uDFA4";
                if (owner.media.document != null) {
                    for (int a = 0; a < owner.media.document.attributes.size(); a++) {
                        TLRPC.DocumentAttribute attr = owner.media.document.attributes.get(a);
                        if (attr instanceof TLRPC.TL_documentAttributeSticker) return "\uD83D\uDCAC";
                        if (attr instanceof TLRPC.TL_documentAttributeAnimated) return "GIF";
                        if (attr instanceof TLRPC.TL_documentAttributeVideo) return "\uD83C\uDFA5";
                    }
                }
                return "\uD83D\uDCCE";
            }
            if (owner.media instanceof TLRPC.TL_messageMediaPoll) return "\uD83D\uDCCA";
            if (owner.media instanceof TLRPC.TL_messageMediaGeo) return "\uD83D\uDCCD";
            if (owner.media instanceof TLRPC.TL_messageMediaWebPage) return "\uD83D\uDD17";
            return "\uD83D\uDCCE";
        } catch (Throwable t) {
            return null;
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
                long key = cacheKey(dialogId, mid);
                String text;
                synchronized (map) {
                    text = map.remove(key);
                }
                String meta = takeMeta(account, key);
                Entry entry = buildEntry(dialogId, mid, text, meta);
                if (entry != null) {
                    addEntry(account, entry);
                }
            }
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    private String takeMeta(int account, long key) {
        try {
            LinkedHashMap<Long, String> meta = metaFor(account);
            synchronized (meta) {
                return meta.remove(key);
            }
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * R14.16 (AyuGram parity): turns the cached text + metadata snapshot into a log entry,
     * honouring the "save for bots" and "save media" switches.
     */
    private Entry buildEntry(long dialogId, int mid, String text, String meta) {
        String media = null;
        String reactions = null;
        boolean bot = false;
        if (!TextUtils.isEmpty(meta)) {
            String[] parts = meta.split("\\|");
            for (int a = 0; a < parts.length; a++) {
                String part = parts[a];
                if ("bot".equals(part)) {
                    bot = true;
                } else if (!TextUtils.isEmpty(part) && (reactions == null && part.length() > 0 && isEmojiReactions(part))) {
                    reactions = part;
                } else if (!TextUtils.isEmpty(part)) {
                    media = part;
                }
            }
        }
        if (bot && !MeeroDeletedConfig.INSTANCE.getSaveBots()) {
            return null;
        }
        if (!MeeroDeletedConfig.INSTANCE.getSaveMedia()) {
            media = null;
        }
        if (!MeeroDeletedConfig.INSTANCE.getSaveReactions()) {
            reactions = null;
        }
        if (TextUtils.isEmpty(text) && TextUtils.isEmpty(media)) {
            return null;
        }
        Entry entry = new Entry();
        entry.dialogId = dialogId;
        entry.mid = mid;
        entry.date = System.currentTimeMillis();
        entry.text = text == null ? "" : text;
        entry.mediaLabel = media;
        entry.reactions = reactions;
        entry.fromBot = bot;
        return entry;
    }

    /** Reactions snapshot always carries a count, media labels never do. */
    private static boolean isEmojiReactions(String part) {
        try {
            char last = part.charAt(part.length() - 1);
            return Character.isDigit(last);
        } catch (Throwable t) {
            return false;
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
                if (foundKey == Long.MIN_VALUE) {
                    continue;
                }
                String meta = takeMeta(account, foundKey);
                Entry entry = buildEntry(foundKey >> 32, mid, text, meta);
                if (entry != null) {
                    addEntry(account, entry);
                }
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

    // ---- persistence (SQLite, AyuGram-style) -------------------------------

    /**
     * MeeroGram R14.20: the log used to live in SharedPreferences as a base64 text
     * blob. It is now a real SQLite database - column layout mirrors AyuGram's
     * Room entities (userId/dialogId/topicId/groupedId/messageId/date/...), so no
     * ordering information is lost and the history is queryable.
     */
    private static final Object DB_LOCK = new Object();
    private static SQLiteDatabase database;
    private static ExecutorService storeQueue;

    public static File dbFile() {
        return new File(ApplicationLoader.getFilesDirFixed(), "meero_saved.sqlite");
    }

    public static String dbPath() {
        return dbFile().getAbsolutePath();
    }

    private static ExecutorService storeQueue() {
        if (storeQueue == null) {
            storeQueue = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "meero-deleted-store");
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            });
        }
        return storeQueue;
    }

    private static SQLiteDatabase db() throws Exception {
        synchronized (DB_LOCK) {
            if (database == null) {
                database = new SQLiteDatabase(dbFile().getPath());
                database.executeFast("CREATE TABLE IF NOT EXISTS meero_deleted_messages(" +
                        "fakeId INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, dialogId INTEGER, " +
                        "topicId INTEGER DEFAULT 0, groupedId INTEGER DEFAULT 0, messageId INTEGER, " +
                        "date INTEGER, edit INTEGER, text TEXT, newText TEXT, media TEXT, reactions TEXT, fromBot INTEGER)").stepThis().dispose();
                database.executeFast("CREATE INDEX IF NOT EXISTS meero_deleted_idx ON meero_deleted_messages(userId, dialogId, messageId)").stepThis().dispose();
                database.executeFast("CREATE INDEX IF NOT EXISTS meero_deleted_date_idx ON meero_deleted_messages(userId, date DESC)").stepThis().dispose();
            }
            return database;
        }
    }

    private static SharedPreferences prefs(int account) {
        return ApplicationLoader.applicationContext
                .getSharedPreferences("meerogram_deleted_log_" + account, android.app.Activity.MODE_PRIVATE);
    }

    private void save(int account) {
        try {
            final ArrayList<Entry> snapshot;
            ArrayList<Entry> list = entriesFor(account);
            synchronized (list) {
                snapshot = new ArrayList<>(list);
            }
            final int acc = account;
            storeQueue().execute(() -> writeRows(acc, snapshot));
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    private static void writeRows(int account, ArrayList<Entry> rows) {
        try {
            final SQLiteDatabase db = db();
            synchronized (DB_LOCK) {
                db.beginTransaction();
                try {
                    db.executeFast("DELETE FROM meero_deleted_messages WHERE userId = ?").stepThis().dispose();
                    SQLitePreparedStatement stmt = db.executeFast(
                            "INSERT INTO meero_deleted_messages (userId, dialogId, messageId, date, edit, text, newText, media, reactions, fromBot) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    for (int a = 0; a < rows.size(); a++) {
                        Entry e = rows.get(a);
                        stmt.requery();
                        stmt.bindInteger(1, account);
                        stmt.bindLong(2, e.dialogId);
                        stmt.bindInteger(3, e.mid);
                        stmt.bindLong(4, e.date);
                        stmt.bindInteger(5, e.edit ? 1 : 0);
                        bindString(stmt, 6, e.text);
                        bindString(stmt, 7, e.newText);
                        bindString(stmt, 8, e.mediaLabel);
                        bindString(stmt, 9, e.reactions);
                        stmt.bindInteger(10, e.fromBot ? 1 : 0);
                        stmt.step();
                    }
                    stmt.dispose();
                    db.commitTransaction();
                } catch (Throwable inner) {
                    FileLog.e(inner);
                    try {
                        db.executeFast("ROLLBACK").stepThis().dispose();
                    } catch (Throwable ignore) {
                    }
                }
            }
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    private static void bindString(SQLitePreparedStatement stmt, int index, String value) throws Exception {
        if (value == null) {
            stmt.bindNull(index);
        } else {
            stmt.bindString(index, value);
        }
    }

    private void load(int account) {
        try {
            ArrayList<Entry> list = entriesFor(account);
            ArrayList<Entry> loaded = new ArrayList<>();
            synchronized (DB_LOCK) {
                SQLiteCursor cursor = db().queryFinalized(
                        "SELECT dialogId, messageId, date, edit, text, newText, media, reactions, fromBot " +
                                "FROM meero_deleted_messages WHERE userId = ? ORDER BY date ASC LIMIT " + MAX_ENTRIES,
                        account);
                try {
                    while (cursor.next() && loaded.size() < MAX_ENTRIES) {
                        Entry entry = new Entry();
                        entry.dialogId = cursor.longValue(0);
                        entry.mid = cursor.intValue(1);
                        entry.date = cursor.longValue(2);
                        entry.edit = cursor.intValue(3) != 0;
                        entry.text = cursor.isNull(4) ? "" : cursor.stringValue(4);
                        entry.newText = cursor.isNull(5) ? "" : cursor.stringValue(5);
                        entry.mediaLabel = cursor.isNull(6) ? null : cursor.stringValue(6);
                        entry.reactions = cursor.isNull(7) ? null : cursor.stringValue(7);
                        entry.fromBot = cursor.intValue(8) != 0;
                        loaded.add(entry);
                    }
                } finally {
                    cursor.dispose();
                }
            }
            synchronized (list) {
                list.clear();
                list.addAll(loaded);
            }
            migrateLegacyLog(account);
        } catch (Throwable t) {
            FileLog.e(t);
            loadLegacyLog(account);
        }
    }

    /** One-shot import of the pre-R14.20 SharedPreferences blob. */
    private void migrateLegacyLog(int account) {
        try {
            SharedPreferences p = prefs(account);
            String raw = p.getString("log", "");
            if (TextUtils.isEmpty(raw)) {
                return;
            }
            loadLegacyLog(account);
            p.edit().remove("log").apply();
            save(account);
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    private void loadLegacyLog(int account) {
        try {
            String raw = prefs(account).getString("log", "");
            if (TextUtils.isEmpty(raw)) {
                return;
            }
            ArrayList<Entry> list = entriesFor(account);
            String[] lines = raw.split("\n");
            synchronized (list) {
                for (int a = 0; a < lines.length; a++) {
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
                while (list.size() > MAX_ENTRIES) {
                    list.remove(0);
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
