package org.telegram.ui.Components.glass.shape;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassG2ContinuityProfile {
    public final double arcCurvatureScale;
    public final double arcFraction;
    public final double bezierCurvatureScale;
    private CubicBezier cachedBezier;
    public final double extendedFraction;
    public static final GlassG2ContinuityProfile ROUNDED_RECTANGLE = new GlassG2ContinuityProfile(0.5286651d, 0.5555555555555556d, 1.0732051d, 1.0732051d);
    public static final GlassG2ContinuityProfile CAPSULE = new GlassG2ContinuityProfile(0.396498825d, 0.0d, 1.0d, 1.0d);
    public static final GlassG2ContinuityProfile G1_EQUIVALENT = new GlassG2ContinuityProfile(0.0d, 1.0d, 1.0d, 1.0d);

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public static final class CubicBezier {
        final Point p0;
        final Point p1;
        final Point p2;
        final Point p3;

        CubicBezier(Point point, Point point2, Point point3, Point point4) {
            this.p0 = point;
            this.p1 = point2;
            this.p2 = point3;
            this.p3 = point4;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public CubicBezier multiply(double d) {
            return new CubicBezier(this.p0.multiply(d), this.p1.multiply(d), this.p2.multiply(d), this.p3.multiply(d));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public static final class Point {
        final double x;
        final double y;

        Point(double d, double d2) {
            this.x = d;
            this.y = d2;
        }

        Point add(Point point) {
            return new Point(this.x + point.x, this.y + point.y);
        }

        Point multiply(double d) {
            return new Point(this.x * d, this.y * d);
        }

        Point subtract(Point point) {
            return new Point(this.x - point.x, this.y - point.y);
        }
    }

    public GlassG2ContinuityProfile(double d, double d2, double d3, double d4) {
        this.extendedFraction = d;
        this.arcFraction = d2;
        this.bezierCurvatureScale = d3;
        this.arcCurvatureScale = d4;
    }

    private CubicBezier createBaseBezier() {
        double d = (1.5707963267948966d - (this.arcFraction * 1.5707963267948966d)) * 0.5d;
        double sin = Math.sin(d);
        double cos = Math.cos(d);
        if (this.bezierCurvatureScale == 1.0d && this.arcCurvatureScale == 1.0d) {
            double d2 = cos + 1.0d;
            double d3 = sin / d2;
            return new CubicBezier(new Point(-this.extendedFraction, 0.0d), new Point((1.0d - (1.5d / d2)) * d3, 0.0d), new Point(d3, 0.0d), new Point(sin, 1.0d - cos));
        }
        double d4 = 1.0d / this.arcCurvatureScale;
        double sqrt = 1.0d / Math.sqrt(2.0d);
        return generateG2ContinuousBezierWithZeroStartCurvature(new Point(-this.extendedFraction, 0.0d), new Point(0.0d, 1.0d).add(new Point(sqrt, -sqrt).multiply(1.0d - d4)).add(new Point(sin, -cos).multiply(d4)), new Point(1.0d, 0.0d), new Point(cos, sin), this.bezierCurvatureScale);
    }

    private static CubicBezier generateG2ContinuousBezierWithZeroStartCurvature(Point point, Point point2, Point point3, Point point4, double d) {
        double d2 = point3.x;
        double d3 = point4.y;
        double d4 = point3.y;
        double d5 = point4.x;
        double d6 = (d2 * d3) - (d4 * d5);
        double d7 = point2.x - point.x;
        double d8 = point2.y - point.y;
        double d9 = ((-d8) * d2) + (d4 * d7);
        double d10 = ((-((d8 * d5) - (d3 * d7))) / d6) - ((((((1.5d * d) * d9) * d9) / d6) / d6) / d6);
        double d11 = (-d9) / d6;
        return new CubicBezier(point, point.add(new Point(Math.max(d10 * d2, 0.0d), Math.max(d10 * point3.y, 0.0d))), point2.subtract(new Point(Math.max(point4.x * d11, 0.0d), Math.max(d11 * point4.y, 0.0d))), point2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public CubicBezier bezier() {
        CubicBezier cubicBezier = this.cachedBezier;
        if (cubicBezier == null) {
            CubicBezier createBaseBezier = createBaseBezier();
            this.cachedBezier = createBaseBezier;
            return createBaseBezier;
        }
        return cubicBezier;
    }
}
