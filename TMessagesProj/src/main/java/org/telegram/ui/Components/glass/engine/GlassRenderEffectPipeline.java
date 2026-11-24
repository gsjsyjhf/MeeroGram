package org.telegram.ui.Components.glass.engine;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.RuntimeShader;
import android.graphics.Shader;
import android.os.Build;
import org.telegram.messenger.AndroidUtilities;

import org.telegram.messenger.R;
import org.telegram.messenger.utils.RenderNodeEffects$;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassRenderEffectPipeline {
    private float cachedBottomLeft;
    private float cachedBottomRight;
    private RenderEffect cachedEffect;
    private int cachedHeight;
    private float cachedPadding;
    private GlassEffectSpec cachedSpec;
    private float cachedTopLeft;
    private float cachedTopRight;
    private int cachedWidth;
    private Object runtimeShaderCache;
    private final RenderNode target;

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    private static final class Api33Impl {

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
        public static final class Cache {
            final RuntimeShader dispersion;
            final RuntimeShader refraction;

            private Cache() {
                this.refraction = new RuntimeShader(AndroidUtilities.readRes(R.raw.mrtelegram_glass_refraction));
                this.dispersion = new RuntimeShader(AndroidUtilities.readRes(R.raw.mrtelegram_glass_refraction_dispersion));
            }
        }

        static Object createCache() {
            return new Cache();
        }

        static RenderEffect createLens(Object obj, int i, int i2, float f, GlassRadii glassRadii, GlassEffectSpec glassEffectSpec) {
            RenderEffect createRuntimeShaderEffect;
            Cache cache = (Cache) obj;
            float f2 = i;
            float f3 = i2;
            GlassRadii clamp = new GlassRadii(glassRadii.topLeft, glassRadii.topRight, glassRadii.bottomRight, glassRadii.bottomLeft).clamp(f2, f3);
            RuntimeShader runtimeShader = glassEffectSpec.chromaticAberration ? cache.dispersion : cache.refraction;
            runtimeShader.setFloatUniform("size", f2, f3);
            float f4 = -f;
            runtimeShader.setFloatUniform("offset", f4, f4);
            runtimeShader.setFloatUniform("cornerRadii", clamp.topLeft, clamp.topRight, clamp.bottomRight, clamp.bottomLeft);
            runtimeShader.setFloatUniform("refractionHeight", glassEffectSpec.refractionHeight);
            runtimeShader.setFloatUniform("refractionAmount", -glassEffectSpec.refractionAmount);
            runtimeShader.setFloatUniform("depthEffect", glassEffectSpec.depthEffect ? 1.0f : 0.0f);
            if (glassEffectSpec.chromaticAberration) {
                runtimeShader.setFloatUniform("chromaticAberration", 1.0f);
            }
            createRuntimeShaderEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "content");
            return createRuntimeShaderEffect;
        }
    }

    public GlassRenderEffectPipeline(RenderNode renderNode) {
        this.target = renderNode;
    }

    private static ColorMatrix createColorControlsMatrix(float f, float f2, float f3) {
        float f4 = 1.0f - f3;
        float f5 = ((0.5f - (f2 * 0.5f)) + f) * 255.0f;
        float f6 = 0.213f * f4 * f2;
        float f7 = 0.715f * f4 * f2;
        float f8 = f4 * 0.072f * f2;
        float f9 = f2 * f3;
        return new ColorMatrix(new float[]{f6 + f9, f7, f8, 0.0f, f5, f6, f7 + f9, f8, 0.0f, f5, f6, f7, f9 + f8, 0.0f, f5, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    }

    private boolean matchesCachedInputs(int i, int i2, GlassRadii glassRadii, GlassEffectSpec glassEffectSpec) {
        if (this.cachedSpec != null && i == this.cachedWidth && i2 == this.cachedHeight && sameFloat(glassRadii.topLeft, this.cachedTopLeft) && sameFloat(glassRadii.topRight, this.cachedTopRight) && sameFloat(glassRadii.bottomRight, this.cachedBottomRight) && sameFloat(glassRadii.bottomLeft, this.cachedBottomLeft)) {
            GlassEffectSpec glassEffectSpec2 = this.cachedSpec;
            if (glassEffectSpec != glassEffectSpec2) {
                if (sameFloat(glassEffectSpec.brightness, glassEffectSpec2.brightness) && sameFloat(glassEffectSpec.contrast, this.cachedSpec.contrast) && sameFloat(glassEffectSpec.saturation, this.cachedSpec.saturation) && sameFloat(glassEffectSpec.blurRadius, this.cachedSpec.blurRadius) && sameFloat(glassEffectSpec.refractionHeight, this.cachedSpec.refractionHeight) && sameFloat(glassEffectSpec.refractionAmount, this.cachedSpec.refractionAmount)) {
                    boolean z = glassEffectSpec.depthEffect;
                    GlassEffectSpec glassEffectSpec3 = this.cachedSpec;
                    if (z == glassEffectSpec3.depthEffect && glassEffectSpec.chromaticAberration == glassEffectSpec3.chromaticAberration) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
        return false;
    }

    private static boolean sameFloat(float f, float f2) {
        return Float.floatToRawIntBits(f) == Float.floatToRawIntBits(f2);
    }

    public void clear() {
        this.cachedSpec = null;
        this.cachedEffect = null;
        this.cachedPadding = 0.0f;
        this.target.setRenderEffect((RenderEffect) null);
    }

    public float update(int i, int i2, GlassRadii glassRadii, GlassEffectSpec glassEffectSpec) {
        float f;
        int i3;
        int i4;
        GlassRadii glassRadii2;
        GlassEffectSpec glassEffectSpec2;
        if (i <= 0 || i2 <= 0 || glassRadii == null || glassEffectSpec == null) {
            clear();
            return 0.0f;
        } else if (matchesCachedInputs(i, i2, glassRadii, glassEffectSpec)) {
            this.target.setRenderEffect(this.cachedEffect);
            return this.cachedPadding;
        } else {
            RenderEffect m = glassEffectSpec.hasColorControls() ? RenderEffect.createColorFilterEffect(new ColorMatrixColorFilter(createColorControlsMatrix(glassEffectSpec.brightness, glassEffectSpec.contrast, glassEffectSpec.saturation))) : null;
            if (glassEffectSpec.hasBlur()) {
                f = m != null ? Math.max(0.0f, glassEffectSpec.blurRadius) : 0.0f;
                float f2 = glassEffectSpec.blurRadius;
                RenderEffect m2 = RenderEffect.createBlurEffect(f2, f2, Shader.TileMode.CLAMP);
                m = m == null ? m2 : RenderEffect.createChainEffect(m2, m);
            } else {
                f = 0.0f;
            }
            if (Build.VERSION.SDK_INT < 33 || !glassEffectSpec.hasLens()) {
                i3 = i;
                i4 = i2;
                glassRadii2 = glassRadii;
                glassEffectSpec2 = glassEffectSpec;
            } else {
                if (f > 0.0f) {
                    f = Math.max(f - glassEffectSpec.refractionHeight, 0.0f);
                }
                float f3 = f;
                if (this.runtimeShaderCache == null) {
                    this.runtimeShaderCache = Api33Impl.createCache();
                }
                i3 = i;
                i4 = i2;
                glassRadii2 = glassRadii;
                glassEffectSpec2 = glassEffectSpec;
                RenderEffect createLens = Api33Impl.createLens(this.runtimeShaderCache, i3, i4, f3, glassRadii2, glassEffectSpec2);
                m = m == null ? createLens : RenderEffect.createChainEffect(createLens, m);
                f = f3;
            }
            this.target.setRenderEffect(m);
            this.cachedEffect = m;
            this.cachedWidth = i3;
            this.cachedHeight = i4;
            this.cachedTopLeft = glassRadii2.topLeft;
            this.cachedTopRight = glassRadii2.topRight;
            this.cachedBottomRight = glassRadii2.bottomRight;
            this.cachedBottomLeft = glassRadii2.bottomLeft;
            this.cachedPadding = f;
            this.cachedSpec = glassEffectSpec2;
            return f;
        }
    }
}
