package org.telegram.ui.Components.glass.controls;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
abstract class GlassControlUtils {
    /* JADX INFO: Access modifiers changed from: package-private */
    public static float clamp(float f, float f2, float f3) {
        return Math.max(f2, Math.min(f3, f));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static float clamp01(float f) {
        return clamp(f, 0.0f, 1.0f);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static float dp(View view, float f) {
        return f * view.getResources().getDisplayMetrics().density;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static float easeOut(float f) {
        float clamp01 = clamp01(f);
        float f2 = 0.0f;
        float f3 = 1.0f;
        float f4 = clamp01;
        for (int i = 0; i < 12; i++) {
            f4 = (f2 + f3) * 0.5f;
            if (((1.0f - f4) * 3.0f * f4 * f4 * 0.58f) + (f4 * f4 * f4) < clamp01) {
                f2 = f4;
            } else {
                f3 = f4;
            }
        }
        return ((1.0f - f4) * 3.0f * f4 * f4) + (f4 * f4 * f4);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean isRtl(View view) {
        return view.getLayoutDirection() == 1;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static float lerp(float f, float f2, float f3) {
        return f + ((f2 - f) * f3);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int lerpColor(int i, int i2, float f) {
        float clamp01 = clamp01(f);
        double[] srgbToOklab = srgbToOklab(i);
        double[] srgbToOklab2 = srgbToOklab(i2);
        double d = srgbToOklab[0];
        double d2 = clamp01;
        double d3 = d + ((srgbToOklab2[0] - d) * d2);
        double d4 = srgbToOklab[1];
        double d5 = d4 + ((srgbToOklab2[1] - d4) * d2);
        double d6 = srgbToOklab[2];
        return oklabToSrgb(d3, d5, d6 + ((srgbToOklab2[2] - d6) * d2), Math.round(Color.alpha(i) + ((Color.alpha(i2) - Color.alpha(i)) * clamp01)));
    }

    private static double linearToSrgb(double d) {
        return d <= 0.0031308d ? d * 12.92d : (Math.pow(d, 0.4166666666666667d) * 1.055d) - 0.055d;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int multiplyAlpha(int i, float f) {
        return Color.argb(Math.round(Color.alpha(i) * clamp01(f)), Color.red(i), Color.green(i), Color.blue(i));
    }

    private static int oklabToSrgb(double d, double d2, double d3, int i) {
        double d4 = (0.3963377774d * d2) + d + (0.2158037573d * d3);
        double d5 = (d - (0.1055613458d * d2)) - (0.0638541728d * d3);
        double d6 = (d - (d2 * 0.0894841775d)) - (d3 * 1.291485548d);
        double d7 = d4 * d4 * d4;
        double d8 = d5 * d5 * d5;
        double d9 = d6 * d6 * d6;
        return Color.argb(Math.max(0, Math.min(255, i)), toByte(linearToSrgb(((4.0767416621d * d7) - (3.3077115913d * d8)) + (0.2309699292d * d9))), toByte(linearToSrgb((((-1.2684380046d) * d7) + (2.6097574011d * d8)) - (0.3413193965d * d9))), toByte(linearToSrgb(((d7 * (-0.0041960863d)) - (d8 * 0.7034186147d)) + (d9 * 1.707614701d))));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void performSegmentTick(View view) {
        view.performHapticFeedback(Build.VERSION.SDK_INT >= 34 ? 26 : 4);
    }

    private static double srgbToLinear(double d) {
        return d <= 0.04045d ? d / 12.92d : Math.pow((d + 0.055d) / 1.055d, 2.4d);
    }

    private static double[] srgbToOklab(int i) {
        double srgbToLinear = srgbToLinear(Color.red(i) / 255.0d);
        double srgbToLinear2 = srgbToLinear(Color.green(i) / 255.0d);
        double srgbToLinear3 = srgbToLinear(Color.blue(i) / 255.0d);
        double d = (0.4122214708d * srgbToLinear) + (0.5363325363d * srgbToLinear2) + (0.0514459929d * srgbToLinear3);
        double d2 = (srgbToLinear * 0.0883024619d) + (srgbToLinear2 * 0.2817188376d) + (srgbToLinear3 * 0.6299787005d);
        double cbrt = Math.cbrt(d);
        double cbrt2 = Math.cbrt((0.2119034982d * srgbToLinear) + (0.6806995451d * srgbToLinear2) + (0.1073969566d * srgbToLinear3));
        double cbrt3 = Math.cbrt(d2);
        return new double[]{((0.2104542553d * cbrt) + (0.793617785d * cbrt2)) - (0.0040720468d * cbrt3), ((1.9779984951d * cbrt) - (2.428592205d * cbrt2)) + (0.4505937099d * cbrt3), ((cbrt * 0.0259040371d) + (cbrt2 * 0.7827717662d)) - (cbrt3 * 0.808675766d)};
    }

    private static int toByte(double d) {
        return (int) Math.round(Math.max(0.0d, Math.min(1.0d, d)) * 255.0d);
    }
}
