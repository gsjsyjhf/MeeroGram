package tw.nekomimi.nekogram;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

/**
 * MeeroGram port of the NekoXConfig credential accessors (minimal form -
 * only the API-selection surface that the login flow needs).
 *
 * The built-in credential now comes from a pool that rotates when Telegram
 * rejects a key, instead of being pinned to whatever the build was compiled
 * with. A user-supplied key still takes precedence.
 */
public class NekoXConfig {

    public static final int API_TYPE_DEFAULT = 0;
    public static final int API_TYPE_CUSTOM = 3;

    public static SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nekox_config", Context.MODE_PRIVATE);

    public static int customApi = preferences.getInt("custom_api", 0);
    public static int customAppId = preferences.getInt("custom_app_id", 0);
    public static String customAppHash = preferences.getString("custom_app_hash", "");

    public static int currentAppId() {
        return switch (customApi) {
            case API_TYPE_CUSTOM -> customAppId;
            default -> MeeroApiKeys.currentId();
        };
    }

    public static String currentAppHash() {
        return switch (customApi) {
            case API_TYPE_CUSTOM -> customAppHash;
            default -> MeeroApiKeys.currentHash();
        };
    }

    public static void saveCustomApi() {
        preferences.edit()
                .putInt("custom_api", customApi)
                .putInt("custom_app_id", customAppId)
                .putString("custom_app_hash", customAppHash)
                .apply();
    }
}
