package org.telegram.ui.Components.blur3.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.RenderNode;



import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.blur3.LiquidGlassEffect;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSource;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public class BlurredBackgroundDrawableRenderNode extends BlurredBackgroundDrawable {
    private ConversationGlassRim conversationRim;
    private LiquidGlassEffect liquidGlassEffect;
    private final Outline outline = new Outline();
    private final Rect outlineRect = new Rect();
    private final Paint paintShadow;
    private final Paint paintStrokeBottom;
    private final Paint paintStrokeTop;
    private final RenderNode renderNode;
    private final RenderNode renderNodeFill;
    private boolean renderNodeInvalidated;
    private final BlurredBackgroundSource source;

    public BlurredBackgroundDrawableRenderNode(BlurredBackgroundSource blurredBackgroundSource) {
        Paint paint = new Paint(1);
        this.paintShadow = paint;
        Paint paint2 = new Paint(1);
        this.paintStrokeTop = paint2;
        Paint paint3 = new Paint(1);
        this.paintStrokeBottom = paint3;
        RenderNode m = new RenderNode("BlurredNode");
        this.renderNode = m;
        this.renderNodeFill = new RenderNode("BlurredFill");
        m.setClipToOutline(true);
        m.setClipToBounds(true);
        this.source = blurredBackgroundSource;
        paint.setColor(0);
        Paint.Style style = Paint.Style.STROKE;
        paint2.setStyle(style);
        paint3.setStyle(style);
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x007d  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x0082  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0088  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x0099  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void updateDisplayList() {
        /*
            Method dump skipped, instructions count: 397
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawableRenderNode.updateDisplayList():void");
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        float alpha;
        if (this.boundProps.boundsWithPadding.isEmpty()) {
            return;
        }
        if (!canvas.isHardwareAccelerated()) {
            drawSource(canvas, this.source);
            return;
        }
        if (!this.renderNode).hasDisplayList() {
            this.source.dispatchOnDrawablesRelativePositionChange();
            updateDisplayList();
        } else if (this.renderNodeInvalidated) {
            updateDisplayList();
        }
        this.renderNodeInvalidated = false;
        int i = this.shadowColor;
        alpha = this.renderNode.getAlpha();
        int multAlpha = Theme.multAlpha(i, alpha * this.shadowAlpha);
        if (Color.alpha(multAlpha) != 0) {
            this.paintShadow.setShadowLayer(this.shadowLayerRadius, this.shadowLayerDx, this.shadowLayerDy, multAlpha);
            this.boundProps.drawShadows(canvas, this.paintShadow, this.inAppKeyboardOptimization);
        }
        canvas.save();
        Rect rect = this.boundProps.boundsWithPadding;
        canvas.translate(rect.left, rect.top);
        canvas.drawRenderNode(this.renderNode);
        canvas.restore();
    }

    @Override // org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable
    public BlurredBackgroundSource getSource() {
        return this.source;
    }

    public boolean hasDisplayList() {
        return this.renderNode.hasDisplayList();
    }

    public void invalidateDisplayList() {
        this.renderNodeInvalidated = true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable
    public void onBoundPropsChanged() {
        super.onBoundPropsChanged();
        this.paintStrokeTop.setStrokeWidth(this.boundProps.strokeWidthTop);
        this.paintStrokeBottom.setStrokeWidth(this.boundProps.strokeWidthBottom);
        this.outlineRect.set(0, 0, this.boundProps.boundsWithPadding.width(), this.boundProps.boundsWithPadding.height());
        BlurredBackgroundDrawable.getOutline(this.outline, this.outlineRect, this.boundProps.radii);
        this.outline.setAlpha(1.0f);
        if (this.boundProps.boundsWithPadding.isEmpty()) {
            return;
        }
        this.renderNodeFill.setPosition(0, 0, this.boundProps.boundsWithPadding.width(), this.boundProps.boundsWithPadding.height());
        this.renderNode.setPosition(0, 0, this.boundProps.boundsWithPadding.width(), this.boundProps.boundsWithPadding.height());
        this.renderNode.setOutline(this.outline);
        this.renderNodeInvalidated = true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable
    public void onSourceOffsetChange(float f, float f2) {
        super.onSourceOffsetChange(f, f2);
        this.renderNodeInvalidated = true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable
    public void onSourceRelativePositionChanged(RectF rectF) {
        super.onSourceRelativePositionChanged(rectF);
        this.source.dispatchOnDrawablesRelativePositionChange();
    }

    @Override // org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable, android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        int alpha = getAlpha();
        super.setAlpha(i);
        this.renderNode.setAlpha(i / 255.0f);
        this.renderNodeInvalidated = true;
        if (alpha != 0 || i <= 0) {
            return;
        }
        this.source.dispatchOnDrawablesRelativePositionChange();
    }

    @Override // org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable
    public BlurredBackgroundDrawable setClipToOutline(boolean z) {
        this.renderNode.setClipToOutline(z);
        return super.setClipToOutline(z);
    }

    public void setLiquidGlassEffectAllowed() {
        this.liquidGlassEffect = new LiquidGlassEffect(this.renderNodeFill);
    }

    @Override // org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable
    public void updateColors() {
        super.updateColors();
        this.paintShadow.setShadowLayer(this.shadowLayerRadius, this.shadowLayerDx, this.shadowLayerDy, this.shadowColor);
        this.paintStrokeTop.setColor(this.strokeColorTop);
        this.paintStrokeBottom.setColor(this.strokeColorBottom);
        this.renderNodeInvalidated = true;
    }
}
