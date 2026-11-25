/*
 * MeeroGram: deleted messages / edit history.
 *
 * Provides the "الرسائل المحذوفة" (deleted messages) section in Meerogram
 * settings. When enabled, deleted messages and old versions of edited
 * messages are captured locally (see MeeroDeletedMessages) so the user can
 * still read what was removed.
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.preferences.boolean

object MeeroDeletedConfig {

    val sharedPreferences: SharedPreferences =
        ApplicationLoader.applicationContext.getSharedPreferences("meerogram_deleted", Activity.MODE_PRIVATE)

    /** Keep deleted messages in the local history list. */
    var keepDeletedMessages by sharedPreferences.boolean("MG_Deleted_Keep", false)

    /** Keep the previous text of edited messages. */
    var keepEditsHistory by sharedPreferences.boolean("MG_Deleted_Edits", false)

    fun isEnabled(): Boolean = keepDeletedMessages || keepEditsHistory
}
