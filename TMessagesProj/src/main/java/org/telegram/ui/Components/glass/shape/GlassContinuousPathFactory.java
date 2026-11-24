package org.telegram.ui.Components.glass.shape;

import android.graphics.Path;
import android.graphics.RectF;
import org.telegram.ui.Components.glass.shape.GlassG2ContinuityProfile;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public abstract class GlassContinuousPathFactory {
    private static final GlassG2ContinuityProfile PROFILE = GlassG2ContinuityProfile.ROUNDED_RECTANGLE;
    private static final GlassG2ContinuityProfile CAPSULE_PROFILE = GlassG2ContinuityProfile.CAPSULE;

    private static void arcTo(Path path, double d, double d2, double d3, double d4, double d5) {
        path.arcTo(new RectF((float) (d - d3), (float) (d2 - d3), (float) (d + d3), (float) (d2 + d3)), (float) Math.toDegrees(d4), (float) Math.toDegrees(d5));
    }

    private static void arcToWithScaledRadius(Path path, double d, double d2, double d3, double d4, double d5, double d6) {
        double d7 = d5 + (0.5d * d6);
        double d8 = 1.0d - d4;
        arcTo(path, d + (Math.cos(d7) * d3 * d8), d2 + (Math.sin(d7) * d3 * d8), d3 * d4, d5, d6);
    }

    private static GlassG2ContinuityProfile.CubicBezier bezier(double d, double d2, double d3, double d4) {
        return new GlassG2ContinuityProfile(d, d2, d3, d4).bezier();
    }

    public static Path capsule(Path path, float f, float f2) {
        Path prepare = prepare(path);
        createCapsule(prepare, f, f2);
        return prepare;
    }

    public static Path capsule(Path path, RectF rectF) {
        Path capsule = capsule(path, rectF.width(), rectF.height());
        capsule.offset(rectF.left, rectF.top);
        return capsule;
    }

    private static double clamp(double d, double d2, double d3) {
        return Math.max(d2, Math.min(d3, d));
    }

    private static void createCapsule(Path path, double d, double d2) {
        if (d > d2) {
            createHorizontalCapsule(path, d, d2);
        } else if (d < d2) {
            createVerticalCapsule(path, d, d2);
        } else {
            float f = (float) (d * 0.5d);
            path.addCircle(f, f, f, Path.Direction.CW);
        }
    }

    private static void createHorizontalCapsule(Path path, double d, double d2) {
        double d3 = d2 * 0.5d;
        GlassG2ContinuityProfile glassG2ContinuityProfile = CAPSULE_PROFILE;
        double clamp = clamp((((d * 0.5d) / d3) - 1.0d) / glassG2ContinuityProfile.extendedFraction, 0.0d, 1.0d);
        double d4 = glassG2ContinuityProfile.extendedFraction * clamp;
        double d5 = (-d3) * d4;
        double lerp = lerp(glassG2ContinuityProfile.bezierCurvatureScale, PROFILE.bezierCurvatureScale, clamp);
        double d6 = glassG2ContinuityProfile.arcFraction;
        GlassG2ContinuityProfile.CubicBezier multiply = bezier(d4, d6, lerp, 1.0d).multiply(d3);
        double d7 = d6 * 1.5707963267948966d;
        double d8 = (1.5707963267948966d - d7) * 0.5d;
        double d9 = (d7 + d8) * 2.0d;
        double d10 = d8 + 1.5707963267948966d;
        path.moveTo((float) ((Math.cos(d10) * d3) + d3), (float) (d3 + (Math.sin(d10) * d3)));
        arcTo(path, d3, d3, d3, d10, d9);
        GlassG2ContinuityProfile.Point point = multiply.p2;
        GlassG2ContinuityProfile.Point point2 = multiply.p1;
        path.cubicTo((float) (d3 - point.x), (float) (point.y + 0.0d), (float) (d3 - point2.x), (float) (point2.y + 0.0d), (float) (d3 - Math.max(multiply.p0.x, d5)), (float) (multiply.p0.y + 0.0d));
        double d11 = d - d3;
        path.lineTo((float) (d11 + d5), (float) 0.0d);
        GlassG2ContinuityProfile.Point point3 = multiply.p1;
        float f = (float) (point3.y + 0.0d);
        GlassG2ContinuityProfile.Point point4 = multiply.p2;
        float f2 = (float) (point4.x + d11);
        float f3 = (float) (point4.y + 0.0d);
        GlassG2ContinuityProfile.Point point5 = multiply.p3;
        path.cubicTo((float) (point3.x + d11), f, f2, f3, (float) (point5.x + d11), (float) (point5.y + 0.0d));
        arcTo(path, d11, d3, d3, -(1.5707963267948966d - d8), d9);
        GlassG2ContinuityProfile.Point point6 = multiply.p2;
        float f4 = (float) (d2 - point6.y);
        GlassG2ContinuityProfile.Point point7 = multiply.p1;
        path.cubicTo((float) (d11 + point6.x), f4, (float) (d11 + point7.x), (float) (d2 - point7.y), (float) (d11 + Math.max(multiply.p0.x, d5)), (float) (d2 - multiply.p0.y));
        path.lineTo((float) (d3 - d5), (float) d2);
        GlassG2ContinuityProfile.Point point8 = multiply.p1;
        float f5 = (float) (d3 - point8.x);
        float f6 = (float) (d2 - point8.y);
        GlassG2ContinuityProfile.Point point9 = multiply.p2;
        float f7 = (float) (d2 - point9.y);
        GlassG2ContinuityProfile.Point point10 = multiply.p3;
        path.cubicTo(f5, f6, (float) (d3 - point9.x), f7, (float) (d3 - point10.x), (float) (d2 - point10.y));
    }

    private static void createRoundedRectangle(Path path, double d, double d2, double d3, double d4, double d5, double d6) {
        double min = Math.min(d, d2) * 0.5d;
        double clamp = clamp(d3, 0.0d, min);
        double clamp2 = clamp(d4, 0.0d, min);
        double clamp3 = clamp(d5, 0.0d, min);
        double clamp4 = clamp(d6, 0.0d, min);
        double d7 = clamp + clamp2;
        if (d7 + clamp3 + clamp4 == 0.0d) {
            path.addRect(0.0f, 0.0f, (float) d, (float) d2, Path.Direction.CW);
        } else if ((d7 == d || clamp + clamp4 == d2) && clamp == clamp2 && clamp3 == clamp4 && clamp == clamp3) {
            createCapsule(path, d, d2);
        } else {
            createStandardRoundedRectangle(path, d, d2, clamp, clamp2, clamp3, clamp4);
        }
    }

    private static void createStandardRoundedRectangle(Path path, double d, double d2, double d3, double d4, double d5, double d6) {
        float f;
        Path path2;
        double d7;
        GlassG2ContinuityProfile.CubicBezier cubicBezier;
        GlassG2ContinuityProfile.CubicBezier cubicBezier2;
        GlassG2ContinuityProfile.CubicBezier cubicBezier3;
        GlassG2ContinuityProfile.CubicBezier cubicBezier4;
        float f2;
        GlassG2ContinuityProfile.CubicBezier cubicBezier5;
        double d8;
        double d9;
        double d10;
        GlassG2ContinuityProfile.CubicBezier cubicBezier6;
        double d11 = d * 0.5d;
        double d12 = d2 * 0.5d;
        GlassG2ContinuityProfile glassG2ContinuityProfile = PROFILE;
        double clamp = clamp(((d12 / d3) - 1.0d) / glassG2ContinuityProfile.extendedFraction, 0.0d, 1.0d);
        double clamp2 = clamp(((d11 / d3) - 1.0d) / glassG2ContinuityProfile.extendedFraction, 0.0d, 1.0d);
        double clamp3 = clamp(((d11 / d4) - 1.0d) / glassG2ContinuityProfile.extendedFraction, 0.0d, 1.0d);
        double clamp4 = clamp(((d12 / d4) - 1.0d) / glassG2ContinuityProfile.extendedFraction, 0.0d, 1.0d);
        double clamp5 = clamp(((d12 / d5) - 1.0d) / glassG2ContinuityProfile.extendedFraction, 0.0d, 1.0d);
        double clamp6 = clamp(((d11 / d5) - 1.0d) / glassG2ContinuityProfile.extendedFraction, 0.0d, 1.0d);
        double clamp7 = clamp(((d11 / d6) - 1.0d) / glassG2ContinuityProfile.extendedFraction, 0.0d, 1.0d);
        double clamp8 = clamp(((d12 / d6) - 1.0d) / glassG2ContinuityProfile.extendedFraction, 0.0d, 1.0d);
        double min = Math.min(clamp, clamp2);
        double min2 = Math.min(clamp3, clamp4);
        double min3 = Math.min(clamp5, clamp6);
        double min4 = Math.min(clamp7, clamp8);
        GlassG2ContinuityProfile glassG2ContinuityProfile2 = CAPSULE_PROFILE;
        double lerp = lerp(glassG2ContinuityProfile2.extendedFraction, glassG2ContinuityProfile.extendedFraction, min);
        double lerp2 = lerp(glassG2ContinuityProfile2.extendedFraction, glassG2ContinuityProfile.extendedFraction, min2);
        double lerp3 = lerp(glassG2ContinuityProfile2.extendedFraction, glassG2ContinuityProfile.extendedFraction, min3);
        double lerp4 = lerp(glassG2ContinuityProfile2.extendedFraction, glassG2ContinuityProfile.extendedFraction, min4);
        double d13 = lerp * clamp;
        double d14 = lerp * clamp2;
        double d15 = lerp2 * clamp3;
        double d16 = lerp2 * clamp4;
        double d17 = lerp3 * clamp5;
        double d18 = lerp3 * clamp6;
        double d19 = lerp4 * clamp7;
        double d20 = lerp4 * clamp8;
        double d21 = -d3;
        double d22 = d21 * d13;
        double d23 = d21 * d14;
        double d24 = -d4;
        double d25 = d24 * d15;
        double d26 = d24 * d16;
        double d27 = -d5;
        double d28 = d27 * d17;
        double d29 = d27 * d18;
        double d30 = -d6;
        double d31 = d30 * d19;
        double d32 = d30 * d20;
        double lerp5 = lerp(glassG2ContinuityProfile2.bezierCurvatureScale, glassG2ContinuityProfile.bezierCurvatureScale, clamp);
        double lerp6 = lerp(glassG2ContinuityProfile2.bezierCurvatureScale, glassG2ContinuityProfile.bezierCurvatureScale, clamp2);
        double lerp7 = lerp(glassG2ContinuityProfile2.bezierCurvatureScale, glassG2ContinuityProfile.bezierCurvatureScale, clamp3);
        double lerp8 = lerp(glassG2ContinuityProfile2.bezierCurvatureScale, glassG2ContinuityProfile.bezierCurvatureScale, clamp4);
        double lerp9 = lerp(glassG2ContinuityProfile2.bezierCurvatureScale, glassG2ContinuityProfile.bezierCurvatureScale, clamp5);
        double lerp10 = lerp(glassG2ContinuityProfile2.bezierCurvatureScale, glassG2ContinuityProfile.bezierCurvatureScale, clamp6);
        double lerp11 = lerp(glassG2ContinuityProfile2.bezierCurvatureScale, glassG2ContinuityProfile.bezierCurvatureScale, clamp7);
        double lerp12 = lerp(glassG2ContinuityProfile2.bezierCurvatureScale, glassG2ContinuityProfile.bezierCurvatureScale, clamp8);
        double lerp13 = lerp(glassG2ContinuityProfile2.arcFraction, glassG2ContinuityProfile.arcFraction, min);
        double lerp14 = lerp(glassG2ContinuityProfile2.arcFraction, glassG2ContinuityProfile.arcFraction, min2);
        double lerp15 = lerp(glassG2ContinuityProfile2.arcFraction, glassG2ContinuityProfile.arcFraction, min3);
        double lerp16 = lerp(glassG2ContinuityProfile2.arcFraction, glassG2ContinuityProfile.arcFraction, min4);
        double d33 = glassG2ContinuityProfile.arcCurvatureScale - 1.0d;
        double d34 = (min * d33) + 1.0d;
        double d35 = (min2 * d33) + 1.0d;
        double d36 = (min3 * d33) + 1.0d;
        double d37 = (d33 * min4) + 1.0d;
        GlassG2ContinuityProfile.CubicBezier bezier = bezier(d13, lerp13, lerp5, d34);
        GlassG2ContinuityProfile.CubicBezier bezier2 = bezier(d14, lerp13, lerp6, d34);
        GlassG2ContinuityProfile.CubicBezier bezier3 = bezier(d15, lerp14, lerp7, d35);
        GlassG2ContinuityProfile.CubicBezier bezier4 = bezier(d16, lerp14, lerp8, d35);
        GlassG2ContinuityProfile.CubicBezier bezier5 = bezier(d17, lerp15, lerp9, d36);
        GlassG2ContinuityProfile.CubicBezier bezier6 = bezier(d18, lerp15, lerp10, d36);
        GlassG2ContinuityProfile.CubicBezier bezier7 = bezier(d19, lerp16, lerp11, d37);
        GlassG2ContinuityProfile.CubicBezier bezier8 = bezier(d20, lerp16, lerp12, d37);
        float f3 = (float) 0.0d;
        float f4 = (float) (d3 - d22);
        path.moveTo(f3, f4);
        if (d3 > 0.0d) {
            GlassG2ContinuityProfile.Point point = bezier.p1;
            GlassG2ContinuityProfile.Point point2 = bezier.p2;
            GlassG2ContinuityProfile.Point point3 = bezier.p3;
            cubicBezier = bezier5;
            f = f4;
            d7 = d32;
            d9 = d26;
            d10 = d29;
            cubicBezier5 = bezier3;
            d8 = 0.0d;
            cubicBezier6 = bezier4;
            path.cubicTo((float) ((point.y * d3) + 0.0d), (float) (d3 - (point.x * d3)), (float) ((point2.y * d3) + 0.0d), (float) (d3 - (point2.x * d3)), (float) ((point3.y * d3) + 0.0d), (float) (d3 - (point3.x * d3)));
            cubicBezier2 = bezier6;
            cubicBezier4 = bezier8;
            f2 = f3;
            cubicBezier3 = bezier7;
            arcToWithScaledRadius(path, d3, d3, d3, 1.0d / d34, ((1.0d - lerp13) * 1.5707963267948966d * 0.5d) + 3.141592653589793d, lerp13 * 1.5707963267948966d);
            GlassG2ContinuityProfile.Point point4 = bezier2.p2;
            GlassG2ContinuityProfile.Point point5 = bezier2.p1;
            path2 = path;
            path2.cubicTo((float) (d3 - (point4.x * d3)), (float) ((point4.y * d3) + 0.0d), (float) (d3 - (point5.x * d3)), (float) ((point5.y * d3) + 0.0d), (float) (d3 - Math.max(bezier2.p0.x * d3, d23)), (float) ((bezier2.p0.y * d3) + 0.0d));
        } else {
            f = f4;
            path2 = path;
            d7 = d32;
            cubicBezier = bezier5;
            cubicBezier2 = bezier6;
            cubicBezier3 = bezier7;
            cubicBezier4 = bezier8;
            f2 = f3;
            cubicBezier5 = bezier3;
            d8 = 0.0d;
            d9 = d26;
            d10 = d29;
            cubicBezier6 = bezier4;
        }
        double d38 = d - d4;
        path2.lineTo((float) (d38 + d25), f2);
        if (d4 > d8) {
            GlassG2ContinuityProfile.CubicBezier cubicBezier7 = cubicBezier5;
            GlassG2ContinuityProfile.Point point6 = cubicBezier7.p1;
            float f5 = (float) ((point6.y * d4) + d8);
            GlassG2ContinuityProfile.Point point7 = cubicBezier7.p2;
            GlassG2ContinuityProfile.Point point8 = cubicBezier7.p3;
            path2.cubicTo((float) ((point6.x * d4) + d38), f5, (float) ((point7.x * d4) + d38), (float) ((point7.y * d4) + d8), (float) ((point8.x * d4) + d38), (float) ((point8.y * d4) + d8));
            arcToWithScaledRadius(path, d38, d4, d4, 1.0d / d35, (((1.0d - lerp14) * 1.5707963267948966d) * 0.5d) - 1.5707963267948966d, lerp14 * 1.5707963267948966d);
            GlassG2ContinuityProfile.CubicBezier cubicBezier8 = cubicBezier6;
            GlassG2ContinuityProfile.Point point9 = cubicBezier8.p2;
            float f6 = (float) (d4 - (point9.x * d4));
            GlassG2ContinuityProfile.Point point10 = cubicBezier8.p1;
            GlassG2ContinuityProfile.Point point11 = cubicBezier8.p0;
            path2 = path;
            path2.cubicTo((float) (d - (point9.y * d4)), f6, (float) (d - (point10.y * d4)), (float) (d4 - (point10.x * d4)), (float) (d - (point11.y * d4)), (float) (d4 - Math.max(point11.x * d4, d9)));
        }
        double d39 = d2 - d5;
        path2.lineTo((float) d, (float) (d39 + d28));
        if (d5 > d8) {
            GlassG2ContinuityProfile.CubicBezier cubicBezier9 = cubicBezier;
            GlassG2ContinuityProfile.Point point12 = cubicBezier9.p1;
            float f7 = (float) ((point12.x * d5) + d39);
            GlassG2ContinuityProfile.Point point13 = cubicBezier9.p2;
            GlassG2ContinuityProfile.Point point14 = cubicBezier9.p3;
            path2.cubicTo((float) (d - (point12.y * d5)), f7, (float) (d - (point13.y * d5)), (float) ((point13.x * d5) + d39), (float) (d - (point14.y * d5)), (float) ((point14.x * d5) + d39));
            double d40 = d - d5;
            arcToWithScaledRadius(path, d40, d39, d5, 1.0d / d36, (1.0d - lerp15) * 1.5707963267948966d * 0.5d, lerp15 * 1.5707963267948966d);
            GlassG2ContinuityProfile.CubicBezier cubicBezier10 = cubicBezier2;
            GlassG2ContinuityProfile.Point point15 = cubicBezier10.p2;
            float f8 = (float) (d2 - (point15.y * d5));
            GlassG2ContinuityProfile.Point point16 = cubicBezier10.p1;
            path2 = path;
            path2.cubicTo((float) ((point15.x * d5) + d40), f8, (float) ((point16.x * d5) + d40), (float) (d2 - (point16.y * d5)), (float) (d40 + Math.max(cubicBezier10.p0.x * d5, d10)), (float) (d2 - (cubicBezier10.p0.y * d5)));
        }
        path2.lineTo((float) (d6 - d31), (float) d2);
        if (d6 > d8) {
            GlassG2ContinuityProfile.CubicBezier cubicBezier11 = cubicBezier3;
            GlassG2ContinuityProfile.Point point17 = cubicBezier11.p1;
            float f9 = (float) (d2 - (point17.y * d6));
            GlassG2ContinuityProfile.Point point18 = cubicBezier11.p2;
            GlassG2ContinuityProfile.Point point19 = cubicBezier11.p3;
            path2.cubicTo((float) (d6 - (point17.x * d6)), f9, (float) (d6 - (point18.x * d6)), (float) (d2 - (point18.y * d6)), (float) (d6 - (point19.x * d6)), (float) (d2 - (point19.y * d6)));
            double d41 = d2 - d6;
            arcToWithScaledRadius(path, d6, d41, d6, 1.0d / d37, ((1.0d - lerp16) * 1.5707963267948966d * 0.5d) + 1.5707963267948966d, lerp16 * 1.5707963267948966d);
            GlassG2ContinuityProfile.CubicBezier cubicBezier12 = cubicBezier4;
            GlassG2ContinuityProfile.Point point20 = cubicBezier12.p2;
            float f10 = (float) ((point20.y * d6) + d8);
            float f11 = (float) ((point20.x * d6) + d41);
            GlassG2ContinuityProfile.Point point21 = cubicBezier12.p1;
            GlassG2ContinuityProfile.Point point22 = cubicBezier12.p0;
            path.cubicTo(f10, f11, (float) ((point21.y * d6) + d8), (float) ((point21.x * d6) + d41), (float) ((point22.y * d6) + d8), (float) (d41 + Math.max(point22.x * d6, d7)));
            path2 = path;
        }
        path2.lineTo(0.0f, f);
    }

    private static void createVerticalCapsule(Path path, double d, double d2) {
        double d3 = d * 0.5d;
        GlassG2ContinuityProfile glassG2ContinuityProfile = CAPSULE_PROFILE;
        double clamp = clamp((((d2 * 0.5d) / d3) - 1.0d) / glassG2ContinuityProfile.extendedFraction, 0.0d, 1.0d);
        double d4 = glassG2ContinuityProfile.extendedFraction * clamp;
        double d5 = (-d3) * d4;
        double lerp = lerp(glassG2ContinuityProfile.bezierCurvatureScale, PROFILE.bezierCurvatureScale, clamp);
        double d6 = glassG2ContinuityProfile.arcFraction;
        GlassG2ContinuityProfile.CubicBezier multiply = bezier(d4, d6, lerp, 1.0d).multiply(d3);
        double d7 = d6 * 1.5707963267948966d;
        double d8 = (1.5707963267948966d - d7) * 0.5d;
        double d9 = (d7 + d8) * 2.0d;
        float f = (float) (d3 - d5);
        path.moveTo((float) 0.0d, f);
        GlassG2ContinuityProfile.Point point = multiply.p1;
        float f2 = (float) (d3 - point.x);
        GlassG2ContinuityProfile.Point point2 = multiply.p2;
        GlassG2ContinuityProfile.Point point3 = multiply.p3;
        path.cubicTo((float) (point.y + 0.0d), f2, (float) (point2.y + 0.0d), (float) (d3 - point2.x), (float) (point3.y + 0.0d), (float) (d3 - point3.x));
        arcTo(path, d3, d3, d3, -(3.141592653589793d - d8), d9);
        GlassG2ContinuityProfile.Point point4 = multiply.p2;
        GlassG2ContinuityProfile.Point point5 = multiply.p1;
        GlassG2ContinuityProfile.Point point6 = multiply.p0;
        path.cubicTo((float) (d - point4.y), (float) (d3 - point4.x), (float) (d - point5.y), (float) (d3 - point5.x), (float) (d - point6.y), (float) (d3 - Math.max(point6.x, d5)));
        double d10 = d2 - d3;
        path.lineTo((float) d, (float) (d10 + d5));
        GlassG2ContinuityProfile.Point point7 = multiply.p1;
        float f3 = (float) (d10 + point7.x);
        GlassG2ContinuityProfile.Point point8 = multiply.p2;
        float f4 = (float) (d10 + point8.x);
        GlassG2ContinuityProfile.Point point9 = multiply.p3;
        path.cubicTo((float) (d - point7.y), f3, (float) (d - point8.y), f4, (float) (d - point9.y), (float) (d10 + point9.x));
        arcTo(path, d - d3, d10, d3, d8, d9);
        GlassG2ContinuityProfile.Point point10 = multiply.p2;
        GlassG2ContinuityProfile.Point point11 = multiply.p1;
        float f5 = (float) (point11.x + d10);
        GlassG2ContinuityProfile.Point point12 = multiply.p0;
        path.cubicTo((float) (point10.y + 0.0d), (float) (point10.x + d10), (float) (point11.y + 0.0d), f5, (float) (point12.y + 0.0d), (float) (d10 + Math.max(point12.x, d5)));
        path.lineTo(0.0f, f);
    }

    private static double lerp(double d, double d2, double d3) {
        return d + ((d2 - d) * d3);
    }

    private static Path prepare(Path path) {
        if (path == null) {
            path = new Path();
        } else {
            path.reset();
        }
        path.setFillType(Path.FillType.WINDING);
        return path;
    }

    public static Path roundedRect(Path path, float f, float f2, float f3) {
        return roundedRect(path, f, f2, f3, f3, f3, f3);
    }

    public static Path roundedRect(Path path, float f, float f2, float f3, float f4, float f5, float f6) {
        Path prepare = prepare(path);
        createRoundedRectangle(prepare, f, f2, f3, f4, f5, f6);
        return prepare;
    }
}
