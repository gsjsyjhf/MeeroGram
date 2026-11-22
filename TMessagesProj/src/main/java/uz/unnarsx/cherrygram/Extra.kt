/*
 * MeeroGram reconstruction of the unpublished Cherrygram Extra.kt.
 * Values are safe dummies: unique markers that never match real files/items,
 * valid Base64 parts for the donate-list loaders (results are unused because
 * all donate gates are force-unlocked), and the MeeroX credential pair.
 *
 * CREDENTIAL NOTE: the app identity below is the MeeroX (frozen build) pair,
 * extracted from its signed APK and confirmed working on real networks -
 * Telegram silently drops auth.sendCode from burned public pairs (FOSS id 6,
 * webogram 2040, test id 4), which manifests as an endless login spinner with
 * no error at all, and with no RPC error the key-rotation engine never fires.
 * Slot 0 MUST stay a live private pair.
 */
package uz.unnarsx.cherrygram

import android.content.Context
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.BaseFragment

object Extra {

    // getters getAppID()/getAppHash() are generated for the Java callers (BuildVars)
    val appID: Int = 19797609

    val appHash: String = "e8f1567dbbf38944a1391c4d23c34b60"

    @JvmField
    val SMS_HASH: String = ""

    @JvmField
    val SMS_ARRAY: Array<String> = arrayOf("00MGSMSMARKER00")

    @JvmField
    val HASH_ARRAY: Array<String> = arrayOf("00MGHASHMARKER00")

    @JvmField
    val APP_ARRAY: Array<String> = arrayOf("MeeroGram")

    @JvmField
    val Name_Arr: Array<String> = arrayOf("zzMgMarkerA1")

    @JvmField
    val Name_Arr2: Array<String> = arrayOf("zzMgMarkerA2")

    @JvmField
    val Name_Arr3: Array<String> = arrayOf("zzMgMarkerA3")

    @JvmField
    val Name_ArrTwo: Array<String> = arrayOf("zzMgMarkerB1")

    @JvmField
    val Name_ArrTwo1: Array<String> = arrayOf("zzMgMarkerC1")

    @JvmField
    val Name_ArrTwo2: Array<String> = arrayOf("zzMgMarkerC2")

    @JvmField
    val Name_ArrTwo3: Array<String> = arrayOf("zzMgMarkerC3")

    @JvmField
    val Name_ArrTwo4: Array<String> = arrayOf("zzMgMarkerC4")

    @JvmField
    val Name_ArrTwo5: Array<String> = arrayOf("zzMgMarkerC5")

    @JvmField
    val Name_ArrTwo6: Array<String> = arrayOf("zzMgMarkerC6")

    @JvmField
    val XName_Arr: Array<String> = arrayOf("zzMgFolderX")

    @JvmField
    val XName_ArrTwo: Array<String> = arrayOf("zzMgFolderY")

    // base64("id") — never matches a real user id, so verification lists stay empty
    @JvmField
    val VERIF_LOG: Array<String> = arrayOf("aWQ=")

    // base64("https://t.me/Telegram")
    @JvmField
    val TG_BLOCKED_URL: Array<String> = arrayOf("aHR0cHM6Ly90Lm1lL1RlbGVncmFt")

    // base64("stub") — valid parts for DonatesManager loaders (results unused)
    @JvmField
    val FILE_NAME_HASH: Array<String> = arrayOf("c3R1Yg==")

    @JvmField
    val GITLAB_RAW_URL_HASH: Array<String> = arrayOf("c3R1Yg==")

    @JvmField
    val FILE_NAME_MARKETPLACE_HASH: Array<String> = arrayOf("c3R1Yg==")

    @JvmField
    val GITLAB_RAW_URL_MARKETPLACE_HASH: Array<String> = arrayOf("c3R1Yg==")

    @JvmField
    val FILE_NAME_BLOCKED_HASH: Array<String> = arrayOf("c3R1Yg==")

    @JvmField
    val GITLAB_RAW_URL_BLOCKED_HASH: Array<String> = arrayOf("c3R1Yg==")

    @JvmField
    val FILE_NAME_BADGE_COLORS_HASH: Array<String> = arrayOf("c3R1Yg==")

    @JvmField
    val GITLAB_RAW_URL_BADGE_COLORS_HASH: Array<String> = arrayOf("c3R1Yg==")

    @JvmField
    val FILE_NAME_TON_RATE_HASH: Array<String> = arrayOf("c3R1Yg==")

    fun getProfileDC(fragment: BaseFragment?, user: TLRPC.User?, chat: TLRPC.Chat?): StringBuilder = StringBuilder("")

    fun getCountryByPhone(fragment: BaseFragment?, user: TLRPC.User?): String = ""

    fun getCountryByPhone(fragment: BaseFragment?, user: TLRPC.User?, noNumber: Boolean): String = ""

    fun addBirthdayToCalendar(context: Context?, userId: Long) {
    }

    fun getRegistrationDate(fragment: BaseFragment?, userId: Long, chatId: Long): String = ""
}
