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
import org.telegram.messenger.R
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int

object MeeroDeletedConfig {

    val sharedPreferences: SharedPreferences =
        ApplicationLoader.applicationContext.getSharedPreferences("meerogram_deleted", Activity.MODE_PRIVATE)

    /** Keep deleted messages in the local history list. */
    var keepDeletedMessages by sharedPreferences.boolean("MG_Deleted_Keep", false)

    /** Keep the previous text of edited messages. */
    var keepEditsHistory by sharedPreferences.boolean("MG_Deleted_Edits", false)

    /* ---- R14.16: AyuGram-style message-saving options ---- */

    /** Keep the reactions that were on the message when it was deleted. */
    var saveReactions by sharedPreferences.boolean("MG_Deleted_Reactions", true)

    /** Keep messages sent by bots (AyuGram's "save for bots"). */
    var saveBots by sharedPreferences.boolean("MG_Deleted_Bots", true)

    /** Keep a placeholder for media/captioned posts (photo, video, file, ...). */
    var saveMedia by sharedPreferences.boolean("MG_Deleted_Media", true)

    /** Row tint for recovered entries (0 = theme text colour). */
    var entryColor by sharedPreferences.int("MG_Deleted_Color", 0)

    /** Marker drawn in front of recovered entries (0 none, else index of MARKERS). */
    var entryIcon by sharedPreferences.int("MG_Deleted_Icon", 0)

    val ENTRY_COLORS = intArrayOf(0xFFE5484D.toInt(), 0xFFFF9500.toInt(), 0xFFAF52DE.toInt(),
            0xFF0A84FF.toInt(), 0xFF30D158.toInt())

    /**
     * MeeroGram R14.21: markers are real vector drawables (SVG-style paths), not emoji.
     * Index 0 = no marker.
     */
    val ENTRY_ICON_DRAWABLES = intArrayOf(
        0,
        R.drawable.meero_ic_deleted,
        R.drawable.meero_ic_edited,
        R.drawable.meero_ic_eye_off,
        R.drawable.meero_ic_alert
    )

    fun isEnabled(): Boolean = keepDeletedMessages || keepEditsHistory
}
