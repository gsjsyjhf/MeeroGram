package org.telegram.ui.Components.glass.controls;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import org.telegram.ui.Components.glass.shape.GlassContinuousPathFactory;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
final class GlassContainerRim {
    private boolean cachedDark;
    private float cachedDensity;
    private final Paint paint = new Paint(1);
    private final Paint catchlightPaint = new Paint(1);
    private final Path path = new Path();
    private final Path catchlightPath = new Path();
    private final RectF bounds = new RectF();
    private int cachedWidth = -1;
    private int cachedHeight = -1;

    /* JADX INFO: Access modifiers changed from: package-private */
    public void draw(Canvas canvas, int i, int i2, float f, boolean z) {
        float f2;
        float f3;
        int i3;
        int[] iArr;
        if (i <= 0 || i2 <= 0) {
            return;
        }
        if (i != this.cachedWidth || i2 != this.cachedHeight || f != this.cachedDensity || z != this.cachedDark) {
            this.cachedWidth = i;
            this.cachedHeight = i2;
            this.cachedDensity = f;
            this.cachedDark = z;
            Paint paint = this.paint;
            Paint.Style style = Paint.Style.STROKE;
            paint.setStyle(style);
            float f4 = (z ? 0.9f : 0.6f) * f;
            float f5 = f4 * 0.5f;
            float f6 = i;
            float f7 = i2;
            this.bounds.set(f5, f5, f6 - f5, f7 - f5);
            GlassContinuousPathFactory.capsule(this.path, this.bounds);
            this.paint.setStrokeWidth(f4);
            this.paint.setColor(-1);
            if (z) {
                f3 = 0.5f;
                f2 = f7;
                this.paint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, f2, new int[]{889192447, 184549375, 117440511, 184549375, 956301311}, new float[]{0.0f, 0.25f, 0.5f, 0.75f, 1.0f}, Shader.TileMode.CLAMP));
            } else {
                f2 = f7;
                f3 = 0.5f;
                this.paint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, f2, new int[]{301989888, 469762048, 771751936, 469762048, 503316480}, new float[]{0.0f, 0.25f, 0.5f, 0.75f, 1.0f}, Shader.TileMode.CLAMP));
            }
            float f8 = (z ? 0.8f : 0.45f) * f;
            float f9 = f4 + (f8 * f3);
            this.bounds.set(f9, f9, f6 - f9, f2 - f9);
            GlassContinuousPathFactory.capsule(this.catchlightPath, this.bounds);
            this.catchlightPaint.setStyle(style);
            this.catchlightPaint.setStrokeWidth(f8);
            this.catchlightPaint.setColor(-1);
            Paint paint2 = this.catchlightPaint;
            if (z) {
                i3 = 3;
                iArr = new int[]{318767103, 16777215, 285212671};
            } else {
                i3 = 3;
                iArr = new int[]{1358954495, 16777215, 1090519039};
            }
            float[] fArr = new float[i3];
            // fill-array-data instruction
            fArr[0] = 0.0f;
            fArr[1] = 0.5f;
            fArr[2] = 1.0f;
            paint2.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, f2, iArr, fArr, Shader.TileMode.CLAMP));
        }
        canvas.drawPath(this.path, this.paint);
        canvas.drawPath(this.catchlightPath, this.catchlightPaint);
    }
}
