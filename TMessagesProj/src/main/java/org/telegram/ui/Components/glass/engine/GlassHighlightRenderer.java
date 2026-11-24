package org.telegram.ui.Components.glass.engine;

import android.graphics.BlendMode;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RuntimeShader;
import android.os.Build;
import org.telegram.messenger.AndroidUtilities;

import org.telegram.messenger.R;
import org.telegram.ui.Components.glass.engine.GlassHighlightSpec;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassHighlightRenderer {
    private BlurMaskFilter blurMaskFilter;
    private int blurRadiusBits;
    private final Paint paint;
    private Object shaderCache;

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    private static final class Api33Impl {

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
        public static final class Cache {
            final RuntimeShader ambientShader;
            final RuntimeShader defaultShader;

            private Cache() {
                this.defaultShader = new RuntimeShader(AndroidUtilities.readRes(R.raw.mrtelegram_glass_highlight_default));
                this.ambientShader = new RuntimeShader(AndroidUtilities.readRes(R.raw.mrtelegram_glass_highlight_ambient));
            }
        }

        static RuntimeShader configure(Object obj, float f, float f2, GlassRadii glassRadii, GlassHighlightSpec glassHighlightSpec) {
            Cache cache = (Cache) obj;
            GlassRadii clamp = new GlassRadii(glassRadii.topLeft, glassRadii.topRight, glassRadii.bottomRight, glassRadii.bottomLeft).clamp(f, f2);
            RuntimeShader runtimeShader = glassHighlightSpec.style == GlassHighlightSpec.Style.AMBIENT ? cache.ambientShader : cache.defaultShader;
            runtimeShader.setFloatUniform("size", f, f2);
            runtimeShader.setFloatUniform("cornerRadii", clamp.topLeft, clamp.topRight, clamp.bottomRight, clamp.bottomLeft);
            runtimeShader.setFloatUniform("angle", (float) Math.toRadians(glassHighlightSpec.angleDegrees));
            runtimeShader.setFloatUniform("falloff", glassHighlightSpec.falloff);
            if (glassHighlightSpec.style == GlassHighlightSpec.Style.DEFAULT) {
                runtimeShader.setColorUniform("color", Color.rgb(Color.red(glassHighlightSpec.color), Color.green(glassHighlightSpec.color), Color.blue(glassHighlightSpec.color)));
            }
            return runtimeShader;
        }

        static Object createCache() {
            return new Cache();
        }
    }

    public GlassHighlightRenderer() {
        Paint paint = new Paint(1);
        this.paint = paint;
        paint.setStyle(Paint.Style.STROKE);
    }

    public void draw(Canvas canvas, Path path, float f, float f2, GlassRadii glassRadii, GlassHighlightSpec glassHighlightSpec) {
        BlendMode blendMode;
        if (glassHighlightSpec == null || glassHighlightSpec.width <= 0.0f || glassHighlightSpec.alpha <= 0.0f) {
            return;
        }
        this.paint.setStrokeWidth(((float) Math.ceil(Math.min(glassHighlightSpec.width, Math.min(f, f2) * 0.5f))) * 2.0f);
        this.paint.setColor(glassHighlightSpec.color);
        this.paint.setAlpha(Math.round(Color.alpha(glassHighlightSpec.color) * glassHighlightSpec.alpha));
        float f3 = glassHighlightSpec.blurRadius;
        if (f3 > 0.0f) {
            int floatToRawIntBits = Float.floatToRawIntBits(f3);
            if (this.blurMaskFilter == null || this.blurRadiusBits != floatToRawIntBits) {
                this.blurMaskFilter = new BlurMaskFilter(glassHighlightSpec.blurRadius, BlurMaskFilter.Blur.NORMAL);
                this.blurRadiusBits = floatToRawIntBits;
            }
            this.paint.setMaskFilter(this.blurMaskFilter);
        } else {
            this.paint.setMaskFilter(null);
        }
        int i = Build.VERSION.SDK_INT;
        if (i >= 29) {
            this.paint.setBlendMode(glassHighlightSpec.additive ? BlendMode.PLUS : BlendMode.SRC_OVER);
        } else {
            this.paint.setXfermode(glassHighlightSpec.additive ? new PorterDuffXfermode(PorterDuff.Mode.ADD) : null);
        }
        if (i < 33 || glassHighlightSpec.style == GlassHighlightSpec.Style.PLAIN) {
            this.paint.setShader(null);
        } else {
            if (this.shaderCache == null) {
                this.shaderCache = Api33Impl.createCache();
            }
            this.paint.setShader(Api33Impl.configure(this.shaderCache, f, f2, glassRadii, glassHighlightSpec));
        }
        canvas.drawPath(path, this.paint);
        this.paint.setShader(null);
        this.paint.setMaskFilter(null);
        this.paint.setXfermode(null);
        if (i >= 29) {
            Paint paint = this.paint;
            blendMode = BlendMode.SRC_OVER;
            paint.setBlendMode(blendMode);
        }
    }
}
