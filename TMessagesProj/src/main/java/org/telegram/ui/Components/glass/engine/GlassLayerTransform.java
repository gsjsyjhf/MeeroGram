package org.telegram.ui.Components.glass.engine;

import android.graphics.Canvas;
import android.graphics.Matrix;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassLayerTransform {
    public static final GlassLayerTransform IDENTITY = new GlassLayerTransform(1.0f, 1.0f, 0.0f);
    public final float rotationDegrees;
    public final float scaleX;
    public final float scaleY;

    public GlassLayerTransform(float f, float f2, float f3) {
        this.scaleX = f;
        this.scaleY = f2;
        this.rotationDegrees = f3;
    }

    public boolean concatInverse(Canvas canvas, Matrix matrix) {
        float f = this.scaleX;
        if (f != 0.0f) {
            float f2 = this.scaleY;
            if (f2 != 0.0f) {
                float f3 = this.rotationDegrees;
                if (f3 == 0.0f) {
                    canvas.scale(1.0f / f, 1.0f / f2, 0.0f, 0.0f);
                    return true;
                }
                double d = f3 * 0.017453292519943295d;
                float sin = (float) Math.sin(d);
                float cos = (float) Math.cos(d);
                float f4 = this.scaleX;
                float f5 = cos * f4;
                float f6 = this.scaleY;
                float f7 = sin * f6;
                float f8 = (-sin) * f4;
                float f9 = cos * f6;
                float f10 = (f5 * f9) - (f7 * f8);
                if (f10 == 0.0f) {
                    return false;
                }
                float f11 = 1.0f / f10;
                matrix.setValues(new float[]{f9 * f11, (-f7) * f11, 0.0f, (-f8) * f11, f5 * f11, 0.0f, 0.0f, 0.0f, 1.0f});
                canvas.concat(matrix);
                return true;
            }
        }
        return false;
    }
}
