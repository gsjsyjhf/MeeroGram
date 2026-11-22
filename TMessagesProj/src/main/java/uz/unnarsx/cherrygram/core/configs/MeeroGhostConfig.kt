/*
 * MeeroGram: full ghost mode.
 *
 * When the master switch is on, presence / typing / read receipts are
 * silently dropped at the single network choke point (ConnectionsManager.
 * sendRequestInternal), so the server never learns that you opened the chat,
 * saw a message or are typing. Settings live in Meerogram Settings ->
 * "وضع الشبح" (Ghost mode).
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.tl.TL_account
import uz.unnarsx.cherrygram.preferences.boolean

object MeeroGhostConfig {

    val sharedPreferences: SharedPreferences =
        ApplicationLoader.applicationContext.getSharedPreferences("meerogram_ghost", Activity.MODE_PRIVATE)

    /** Master switch: full ghost mode. */
    var ghostMaster by sharedPreferences.boolean("MG_Ghost_Master", false)

    /** Never report being online (stay "last seen recently/offline"). */
    var ghostHideOnline by sharedPreferences.boolean("MG_Ghost_Online", true)

    /** Never send read receipts (no blue double checks for others). */
    var ghostHideReadReceipts by sharedPreferences.boolean("MG_Ghost_Read", true)

    /** Never send typing / recording / upload indicators. */
    var ghostHideTyping by sharedPreferences.boolean("MG_Ghost_Typing", true)

    /**
     * Called from ConnectionsManager.sendRequestInternal for every outgoing
     * request; returns true when the request must be silently dropped.
     */
    fun isGhostDropped(req: TLObject?): Boolean {
        if (req == null || !ghostMaster) {
            return false
        }
        return when (req) {
            is TL_account.updateStatus -> ghostHideOnline && !req.offline
            is TLRPC.TL_messages_setTyping -> ghostHideTyping
            is TLRPC.TL_messages_readHistory,
            is TLRPC.TL_messages_readMentions,
            is TLRPC.TL_messages_readMessageContents,
            is TLRPC.TL_messages_readDiscussion,
            is TLRPC.TL_messages_readReactions,
            is TLRPC.TL_channels_readHistory,
            is TLRPC.TL_channels_readMessageContents -> ghostHideReadReceipts
            else -> false
        }
    }
}
