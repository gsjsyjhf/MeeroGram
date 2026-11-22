/*
 * MeeroGram reconstruction of the unpublished Cherrygram Extra.kt.
 * Values are safe dummies: unique markers that never match real files/items,
 * valid Base64 parts for the donate-list loaders (results are unused because
 * all donate gates are force-unlocked), and the public FOSS Telegram API pair.
 */
package uz.unnarsx.cherrygram

import android.content.Context
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.BaseFragment

object Extra {

    // getters getAppID()/getAppHash() are generated for the Java callers (BuildVars)
    val appID: Int = 6

    val appHash: String = "eb06d4abfb49dc3eeb1aeb98ae0f581e"

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
