package uz.unnarsx.cherrygram.core.ui.login;

/**
 * Meerogram: iOS 26 login styling metrics.
 *
 * Ported from MeeroX (13.1.1), where the same numbers live inside
 * CodeFieldContainer.java and MeeroIntro.java. The visual layer only -
 * login logic, network requests and account activation are untouched,
 * every call site falls back to the stock layout when the switch is off.
 *
 * Code entry (Telegram for iOS lays this screen out in CodeInputView.swift):
 *
 *   height       = compact ? 44.0 : 51.0
 *   itemSize     = floor(24.0 * height / 28.0)   // width follows height
 *   itemSpacing  = 15.0                          // when there is no prefix
 *   cornerRadius = 15.0                          (Android stock is 4dp)
 *   fontSize     = floor(13.0 * height / 28.0)
 *   borderWidth  = 1.0 + UIScreenPixel
 *
 * Intro (MeeroX's MeeroIntro sealed motion table, dom 'C'; the native
 * MeeroCore lookup was dropped - these are its byte-identical literal
 * fallbacks):
 *
 *   buttonLabel  = 17pt regular  (Android stock is 15pt)
 *   buttonHeight = 50pt          (SolidRoundedButtonNode, stock 48dp)
 *   headline     = 28pt semibold (AuthorizationSequenceSplashController)
 */
public final class MgIos26LoginStyle {

    private MgIos26LoginStyle() {
    }

    /** Master switch, backed by the "MG_Ios26Login" key (default true). */
    public static boolean enabled() {
        try {
            return uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig.INSTANCE.getIos26Login();
        } catch (Throwable ignore) {
            return false;
        }
    }

    /** iOS code-entry metrics. */
    public static final float CODE_HEIGHT_DP = 51f;
    public static final float CODE_HEIGHT_COMPACT_DP = 44f;
    public static final float CODE_WIDTH_RATIO = 24f / 28f;
    public static final float CODE_SPACING_DP = 15f;
    public static final float CODE_RADIUS_DP = 15f;
    public static final float CODE_FONT_RATIO = 13f / 28f;
    /** iOS's own threshold for switching to the compact layout. */
    public static final int IOS_COMPACT_WIDTH_DP = 375;
    public static final int IOS_COMPACT_COUNT = 5;

    /** iOS intro metrics. */
    public static final float INTRO_BUTTON_TEXT_DP = 17f;
    public static final float INTRO_BUTTON_HEIGHT_DP = 50f;
    public static final float INTRO_HEADLINE_DP = 28f;

    /** iOS drops to the shorter box on narrow screens or long codes. */
    public static boolean codeCompact(int count) {
        final int widthDp = (int) (org.telegram.messenger.AndroidUtilities.displaySize.x / org.telegram.messenger.AndroidUtilities.density);
        return widthDp <= 320 || (widthDp <= IOS_COMPACT_WIDTH_DP && count > IOS_COMPACT_COUNT);
    }

    public static float codeBoxHeight(int count) {
        return codeCompact(count) ? CODE_HEIGHT_COMPACT_DP : CODE_HEIGHT_DP;
    }
}
