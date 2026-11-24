package org.telegram.ui.Components.glass.engine;

import android.graphics.BlendMode;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RecordingCanvas;
import android.graphics.RenderNode;
import android.os.Build;




/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassShadowRenderer {
    private final Paint clearPaint;
    private Object innerShadowNode;
    private final Paint shadowPaint = new Paint(1);

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    private static final class Api29Paint {
        static void configureClear(Paint paint) {
            BlendMode blendMode;
            blendMode = BlendMode.CLEAR;
            paint.setBlendMode(blendMode);
        }
    }

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    private static final class Api31InnerShadow {
        static Object draw(Object obj, Canvas canvas, Path path, int i, int i2, GlassInnerShadowSpec glassInnerShadowSpec, Paint paint, Paint paint2) {
            RecordingCanvas beginRecording;
            RenderNode m = (obj instanceof RenderNode) ? ((RenderNode) obj) : new RenderNode("MRTelegramGlassInnerShadow");
            m.setPosition(0, 0, i, i2);
            m.setAlpha(glassInnerShadowSpec.alpha);
            float f = glassInnerShadowSpec.radius;
            m.setRenderEffect(f > 0.0f ? RenderEffect.createBlurEffect(f, f, Shader.TileMode.DECAL) : null);
            beginRecording = m.beginRecording(i, i2);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(glassInnerShadowSpec.color);
            paint.setAlpha(Color.alpha(glassInnerShadowSpec.color));
            paint.setMaskFilter(null);
            beginRecording.drawPath(path, paint);
            beginRecording.save();
            beginRecording.translate(glassInnerShadowSpec.offsetX, glassInnerShadowSpec.offsetY);
            beginRecording.drawPath(path, paint2);
            beginRecording.restore();
            m.endRecording();
            canvas.drawRenderNode(m);
            return m;
        }
    }

    public GlassShadowRenderer() {
        Paint paint = new Paint(1);
        this.clearPaint = paint;
        if (Build.VERSION.SDK_INT >= 29) {
            Api29Paint.configureClear(paint);
        } else {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
    }

    public void drawInner(Canvas canvas, Path path, int i, int i2, GlassInnerShadowSpec glassInnerShadowSpec) {
        Canvas canvas2;
        if (glassInnerShadowSpec == null || glassInnerShadowSpec.alpha <= 0.0f || Color.alpha(glassInnerShadowSpec.color) == 0) {
            return;
        }
        canvas.save();
        canvas.clipPath(path);
        if (Build.VERSION.SDK_INT < 31 || !canvas.isHardwareAccelerated()) {
            canvas2 = canvas;
        } else {
            canvas2 = canvas;
            this.innerShadowNode = Api31InnerShadow.draw(this.innerShadowNode, canvas2, path, i, i2, glassInnerShadowSpec, this.shadowPaint, this.clearPaint);
        }
        canvas2.restore();
    }

    public void drawOuter(Canvas canvas, Path path, float f, float f2, GlassShadowSpec glassShadowSpec) {
        if (glassShadowSpec == null || glassShadowSpec.alpha <= 0.0f || Color.alpha(glassShadowSpec.color) == 0) {
            return;
        }
        float f3 = glassShadowSpec.radius * 2.0f;
        float f4 = -f3;
        int saveLayer = canvas.saveLayer(f4, f4, f + f3 + Math.max(0.0f, glassShadowSpec.offsetX), f2 + f3 + Math.max(0.0f, glassShadowSpec.offsetY), null);
        this.shadowPaint.setStyle(Paint.Style.FILL);
        this.shadowPaint.setColor(glassShadowSpec.color);
        this.shadowPaint.setAlpha(Math.round(Color.alpha(glassShadowSpec.color) * glassShadowSpec.alpha));
        this.shadowPaint.setMaskFilter(glassShadowSpec.radius > 0.0f ? new BlurMaskFilter(glassShadowSpec.radius, BlurMaskFilter.Blur.NORMAL) : null);
        canvas.save();
        canvas.translate(glassShadowSpec.offsetX, glassShadowSpec.offsetY);
        canvas.drawPath(path, this.shadowPaint);
        canvas.translate(-glassShadowSpec.offsetX, -glassShadowSpec.offsetY);
        canvas.drawPath(path, this.clearPaint);
        canvas.restore();
        canvas.restoreToCount(saveLayer);
        this.shadowPaint.setMaskFilter(null);
    }
}
