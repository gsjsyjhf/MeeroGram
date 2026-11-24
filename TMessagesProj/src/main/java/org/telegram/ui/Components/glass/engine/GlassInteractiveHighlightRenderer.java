package org.telegram.ui.Components.glass.engine;

import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RuntimeShader;
import android.os.Build;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver$;
import org.telegram.messenger.R;
import org.telegram.ui.Components.MotionBackgroundPaint$AgslImpl$RuntimeShaderState$$ExternalSyntheticApiModelOutline0;
import org.telegram.ui.Components.MotionBackgroundPaint$AgslImpl$RuntimeShaderState$$ExternalSyntheticApiModelOutline1;
import org.telegram.ui.Components.glass.motion.GlassInteractiveHighlightController;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassInteractiveHighlightRenderer {
    private Object runtimeShader;
    private final Paint washPaint = new Paint(1);
    private final Paint glowPaint = new Paint(1);

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    public static final class Api33Impl {
        static RuntimeShader configure(Object obj, float f, float f2, GlassInteractiveHighlightController glassInteractiveHighlightController, float f3, float f4) {
            RuntimeShader m = GlassInteractiveHighlightRenderer$Api33Impl$$ExternalSyntheticApiModelOutline0.m(obj);
            m.setFloatUniform("size", f, f2);
            m.setColorUniform("color", -1);
            m.setFloatUniform("radius", glassInteractiveHighlightController.getGlowRadius(f, f2));
            m.setFloatUniform("position", GlassInteractiveHighlightRenderer.clamp(f3, 0.0f, f), GlassInteractiveHighlightRenderer.clamp(f4, 0.0f, f2));
            return m;
        }

        static Object createShader() {
            MotionBackgroundPaint$AgslImpl$RuntimeShaderState$$ExternalSyntheticApiModelOutline1.m();
            return MotionBackgroundPaint$AgslImpl$RuntimeShaderState$$ExternalSyntheticApiModelOutline0.m(AndroidUtilities.readRes(R.raw.MRTelegram_glass_interactive_highlight));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static float clamp(float f, float f2, float f3) {
        return Math.max(f2, Math.min(f3, f));
    }

    private static void clearBlend(Paint paint) {
        BlendMode blendMode;
        paint.setXfermode(null);
        if (Build.VERSION.SDK_INT >= 29) {
            blendMode = BlendMode.SRC_OVER;
            ImageReceiver$.ExternalSyntheticApiModelOutline1.m(paint, blendMode);
        }
    }

    private static void setAdditive(Paint paint) {
        BlendMode blendMode;
        if (Build.VERSION.SDK_INT < 29) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
            return;
        }
        blendMode = BlendMode.PLUS;
        ImageReceiver$.ExternalSyntheticApiModelOutline1.m(paint, blendMode);
        paint.setXfermode(null);
    }

    public void draw(Canvas canvas, Path path, float f, float f2, GlassInteractiveHighlightController glassInteractiveHighlightController) {
        draw(canvas, path, f, f2, glassInteractiveHighlightController, glassInteractiveHighlightController.getPositionX(), glassInteractiveHighlightController.getPositionY());
    }

    public void draw(Canvas canvas, Path path, float f, float f2, GlassInteractiveHighlightController glassInteractiveHighlightController, float f3, float f4) {
        draw(canvas, path, f, f2, glassInteractiveHighlightController, f3, f4, glassInteractiveHighlightController.getPressProgress());
    }

    public void draw(Canvas canvas, Path path, float f, float f2, GlassInteractiveHighlightController glassInteractiveHighlightController, float f3, float f4, float f5) {
        float clamp = Float.isNaN(f5) ? 0.0f : clamp(f5, 0.0f, 1.0f);
        if (clamp <= 0.0f || f <= 0.0f || f2 <= 0.0f) {
            return;
        }
        canvas.save();
        canvas.clipPath(path);
        if (Build.VERSION.SDK_INT < 33 || !canvas.isHardwareAccelerated()) {
            setAdditive(this.washPaint);
            this.washPaint.setColor(-1);
            this.washPaint.setAlpha(Math.round(clamp * 0.25f * 255.0f));
            this.washPaint.setShader(null);
            canvas.drawRect(0.0f, 0.0f, f, f2, this.washPaint);
        } else {
            setAdditive(this.washPaint);
            this.washPaint.setColor(-1);
            this.washPaint.setAlpha(Math.round(0.08f * clamp * 255.0f));
            this.washPaint.setShader(null);
            canvas.drawRect(0.0f, 0.0f, f, f2, this.washPaint);
            if (this.runtimeShader == null) {
                this.runtimeShader = Api33Impl.createShader();
            }
            setAdditive(this.glowPaint);
            this.glowPaint.setAlpha(Math.round(clamp * 0.15f * 255.0f));
            this.glowPaint.setShader(Api33Impl.configure(this.runtimeShader, f, f2, glassInteractiveHighlightController, f3, f4));
            canvas.drawRect(0.0f, 0.0f, f, f2, this.glowPaint);
            this.glowPaint.setShader(null);
        }
        canvas.restore();
        clearBlend(this.washPaint);
        clearBlend(this.glowPaint);
    }
}
