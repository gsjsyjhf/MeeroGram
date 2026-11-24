package org.telegram.ui.Components.glass.controls;

import android.content.Context;
import android.graphics.Canvas;
import org.telegram.ui.Components.glass.engine.GlassEffectSpec;
import org.telegram.ui.Components.glass.engine.GlassHighlightSpec;
import org.telegram.ui.Components.glass.engine.GlassShadowSpec;
import org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public class GlassNavigationContainerSurface extends GlassSurfaceFrameLayout {
    private float appearanceDensity;
    private boolean appearanceInitialized;
    private final GlassContainerRim containerRim;
    private boolean darkTheme;
    private boolean denseContentBackdrop;

    public GlassNavigationContainerSurface(Context context) {
        super(context);
        this.containerRim = new GlassContainerRim();
        setCapsule(true);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(2);
        setDarkTheme(false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static GlassEffectSpec effectForSize(float f, int i, int i2) {
        float f2 = 24.0f * f;
        return GlassEffectSpec.liquid(f * 2.0f, (i <= 0 || i2 <= 0) ? f2 : Math.min(f2, Math.min(i, i2) * 0.5f), f2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static GlassHighlightSpec highlightForTheme(boolean z, float f) {
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static GlassShadowSpec shadowForTheme(boolean z, float f) {
        return z ? GlassShadowSpec.defaultShadow(f) : new GlassShadowSpec(f * 1.5f, 0.0f, 0.0f, 134217728, 1.0f);
    }

    public static int tintForTheme(boolean z) {
        return z ? -1723842496 : -1862270977;
    }

    private void updateLensForSize(int i, int i2) {
        setEffectSpec(effectForSize(getResources().getDisplayMetrics().density, i, i2));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout
    public void onDrawGlassContent(Canvas canvas) {
        this.containerRim.draw(canvas, getWidth(), getHeight(), getResources().getDisplayMetrics().density, this.darkTheme);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Components.glass.engine.GlassSurfaceFrameLayout, android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        updateLensForSize(i, i2);
    }

    public final void setDarkTheme(boolean z) {
        float f = getResources().getDisplayMetrics().density;
        if (this.appearanceInitialized && this.darkTheme == z && this.appearanceDensity == f) {
            return;
        }
        this.appearanceInitialized = true;
        this.appearanceDensity = f;
        this.darkTheme = z;
        updateLensForSize(getWidth(), getHeight());
        setTintColor(this.denseContentBackdrop ? z ? -869257168 : -989855745 : tintForTheme(z));
        setHighlightSpec(highlightForTheme(z, f));
        setOuterShadowSpec(shadowForTheme(z, f));
        setInnerShadowSpec(null);
        invalidateGlass();
    }

    public final void setDenseContentBackdrop(boolean z) {
        if (this.denseContentBackdrop == z) {
            return;
        }
        this.denseContentBackdrop = z;
        this.appearanceInitialized = false;
        setDarkTheme(this.darkTheme);
    }
}
