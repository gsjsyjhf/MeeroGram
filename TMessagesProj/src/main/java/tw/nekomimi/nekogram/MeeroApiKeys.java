package tw.nekomimi.nekogram;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;

/**
 * MeeroGram port of MeeroX: a pool of Telegram API credentials with
 * automatic failover.
 *
 * Telegram rates each api_id, and the limits differ from one to the next.
 * The pooled ids live natively (libmeerocore key engine); Java keeps only
 * the rotation STATE (which slot is active) in local prefs and asks the
 * native engine one question at a time. When the lib somehow cannot load,
 * behaviour degrades to the single build-provided credential with no
 * rotation.
 *
 * Slot 0 is the build credential (BuildVars.APP_ID / APP_HASH - resolved
 * from the Cherrygram Extra layer); slots 1..4 are the harvested pool,
 * XOR-obfuscated inside the .so. A user-supplied key from NekoXConfig
 * always wins - this only governs which built-in credential is used when
 * there is no custom one.
 */
public class MeeroApiKeys {

    private static final String PREFS = "meerox_api";
    private static final String KEY_INDEX = "api_index";
    /**
     * Bumped whenever the pool changes. A saved index points at a position
     * in the old list, so rebuilding the table must reset the saved choice
     * once.
     */
    private static final String KEY_GENERATION = "api_generation";
    private static final int GENERATION = 1;

    private static int index = -1;

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static int count() {
        if (MeeroCore.ready()) {
            try {
                final int n = MeeroCore.nKeyCount();
                if (n > 0) return n;
            } catch (Throwable ignored) {
            }
        }
        return 1; // degraded: only the build key
    }

    private static void ensureInstalled() {
        if (MeeroCore.ready()) {
            try {
                /* slot 0 of the native table is the build key, unknown to C */
                MeeroCore.nInstallKey0(BuildVars.APP_ID, BuildVars.APP_HASH);
            } catch (Throwable ignored) {
            }
        }
    }

    private static int index() {
        if (index < 0) {
            try {
                final SharedPreferences p = prefs();
                if (p.getInt(KEY_GENERATION, 0) != GENERATION) {
                    index = 0;
                    p.edit().putInt(KEY_INDEX, 0).putInt(KEY_GENERATION, GENERATION).apply();
                } else {
                    index = p.getInt(KEY_INDEX, 0);
                }
            } catch (Throwable e) {
                index = 0;
            }
            if (index < 0 || index >= count()) {
                index = 0;
            }
        }
        return index;
    }

    public static int currentId() {
        ensureInstalled();
        if (MeeroCore.ready()) {
            try {
                return MeeroCore.nKeyId(index());
            } catch (Throwable ignored) {
            }
        }
        return BuildVars.APP_ID;
    }

    public static String currentHash() {
        ensureInstalled();
        if (MeeroCore.ready()) {
            try {
                final String h = MeeroCore.nKeyHash(index());
                if (h != null) return h;
            } catch (Throwable ignored) {
            }
        }
        return BuildVars.APP_HASH;
    }

    /**
     * Moves to the next usable credential (decision made natively).
     *
     * @return true when another one was available
     */
    public static boolean advance() {
        ensureInstalled();
        if (!MeeroCore.ready()) {
            return false;
        }
        final int next;
        try {
            next = MeeroCore.nKeyAdvance(index());
        } catch (Throwable t) {
            return false;
        }
        if (next < 0) {
            return false;
        }
        index = next;
        try {
            prefs().edit().putInt(KEY_INDEX, index).putInt(KEY_GENERATION, GENERATION).apply();
        } catch (Throwable ignore) {
        }
        FileLog.d("MeeroGram: switching to api id " + currentId());
        return true;
    }

    /** Goes back to the first credential, for a manual reset. */
    public static void reset() {
        index = 0;
        try {
            prefs().edit().putInt(KEY_INDEX, 0).putInt(KEY_GENERATION, GENERATION).apply();
        } catch (Throwable ignore) {
        }
    }

    /**
     * Whether an auth.sendCode failure blames the credential rather than the
     * phone number (marker set lives natively).
     */
    public static boolean isKeyError(String errorText) {
        if (errorText == null) {
            return false;
        }
        if (MeeroCore.ready()) {
            try {
                return MeeroCore.nIsKeyError(errorText);
            } catch (Throwable ignored) {
            }
        }
        return errorText.contains("API_ID_INVALID")
                || errorText.contains("API_ID_PUBLISHED_FLOOD")
                || errorText.contains("API_ID_RESTRICTED")
                || errorText.contains("AUTH_KEY_DUPLICATED")
                || errorText.contains("CONNECTION_API_ID_INVALID");
    }

    /** A short description of the active key, for the settings screen. */
    public static String describe() {
        return "api_id " + currentId() + " (" + (index() + 1) + "/" + count() + ")";
    }
}
