package org.telegram.ui.Components.blur3.source;

import android.graphics.Canvas;
import android.graphics.RecordingCanvas;
import android.graphics.RectF;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.os.Build;
import java.util.Iterator;
import java.util.List;
import me.vkryl.core.reference.ReferenceList;


import org.telegram.ui.Components.blur3.DownscaleScrollableNoiseSuppressor;
import org.telegram.ui.Components.blur3.RenderNodeWithHash;
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawableRenderNode;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public class BlurredBackgroundSourceRenderNode implements BlurredBackgroundSource {
    private final BlurredBackgroundSource fallbackSource;
    private boolean inRecording;
    private boolean noClip;
    private Runnable onDrawablesRelativePositionChangeListener;
    private RecordingCanvas recordingCanvas;
    private RenderNodeWithHash renderNodeWithHash;
    private DownscaleScrollableNoiseSuppressor scrollableNoiseSuppressor;
    private int scrollableNoiseSuppressorIndex;
    public BlurredBackgroundSource underSource;
    private final ReferenceList drawables = new ReferenceList();
    private final RenderNode renderNode = new RenderNode((String) null);

    public BlurredBackgroundSourceRenderNode(BlurredBackgroundSource blurredBackgroundSource) {
        this.fallbackSource = blurredBackgroundSource;
    }

    public RecordingCanvas beginRecording(int i, int i2) {
        RecordingCanvas beginRecording;
        if (this.inRecording) {
            throw new IllegalStateException();
        }
        this.inRecording = true;
        this.renderNode.setPosition(0, 0, i, i2);
        beginRecording = this.renderNode.beginRecording(i, i2);
        this.recordingCanvas = beginRecording;
        return beginRecording;
    }

    @Override // org.telegram.ui.Components.blur3.source.BlurredBackgroundSource
    public BlurredBackgroundDrawable createDrawable() {
        BlurredBackgroundDrawableRenderNode blurredBackgroundDrawableRenderNode = new BlurredBackgroundDrawableRenderNode(this);
        this.drawables.add(blurredBackgroundDrawableRenderNode);
        return blurredBackgroundDrawableRenderNode;
    }

    @Override // org.telegram.ui.Components.blur3.source.BlurredBackgroundSource
    public void dispatchOnDrawablesRelativePositionChange() {
        Runnable runnable = this.onDrawablesRelativePositionChangeListener;
        if (runnable != null) {
            runnable.run();
        }
    }

    @Override // org.telegram.ui.Components.blur3.source.BlurredBackgroundSource
    public void draw(Canvas canvas, float f, float f2, float f3, float f4) {
        DownscaleScrollableNoiseSuppressor downscaleScrollableNoiseSuppressor;
        if (!canvas.isHardwareAccelerated()) {
            BlurredBackgroundSource blurredBackgroundSource = this.fallbackSource;
            if (blurredBackgroundSource != null) {
                blurredBackgroundSource.draw(canvas, f, f2, f3, f4);
            }
        } else if (this.inRecording) {
            throw new IllegalStateException();
        } else {
            BlurredBackgroundSource blurredBackgroundSource2 = this.underSource;
            if (blurredBackgroundSource2 != null) {
                blurredBackgroundSource2.draw(canvas, f, f2, f3, f4);
            }
            canvas.save();
            if (!this.noClip) {
                canvas.clipRect(f, f2, f3, f4);
            }
            if (Build.VERSION.SDK_INT < 31 || (downscaleScrollableNoiseSuppressor = this.scrollableNoiseSuppressor) == null) {
                canvas.drawRenderNode(this.renderNode);
            } else {
                downscaleScrollableNoiseSuppressor.drawInline(canvas, this.scrollableNoiseSuppressorIndex);
            }
            canvas.restore();
        }
    }

    public void endRecording() {
        if (!this.inRecording) {
            throw new IllegalStateException();
        }
        this.renderNode.endRecording();
        this.inRecording = false;
        this.recordingCanvas = null;
    }

    public BlurredBackgroundSource getFallbackSource() {
        return this.fallbackSource;
    }

    public int getVisiblePositions(List list, int i, int i2) {
        RectF rectF;
        Iterator it = this.drawables.iterator();
        int i3 = 0;
        while (it.hasNext()) {
            BlurredBackgroundDrawableRenderNode blurredBackgroundDrawableRenderNode = (BlurredBackgroundDrawableRenderNode) it.next();
            if (blurredBackgroundDrawableRenderNode.hasDisplayList() && blurredBackgroundDrawableRenderNode.getAlpha() > 0 && !blurredBackgroundDrawableRenderNode.getPaddedBounds().isEmpty()) {
                if (i < list.size()) {
                    rectF = (RectF) list.get(i);
                } else {
                    rectF = new RectF();
                    list.add(rectF);
                }
                blurredBackgroundDrawableRenderNode.getPositionRelativeSource(rectF);
                float f = -i2;
                rectF.inset(f, f);
                i++;
                i3++;
            }
        }
        return i3;
    }

    public boolean inRecording() {
        return this.inRecording;
    }

    public void invalidateDisplayListForDrawables() {
        Iterator it = this.drawables.iterator();
        while (it.hasNext()) {
            ((BlurredBackgroundDrawableRenderNode) it.next()).invalidateDisplayList();
        }
    }

    public boolean isRecordingCanvas(Canvas canvas) {
        return canvas != null && canvas == this.recordingCanvas;
    }

    public boolean needUpdateDisplayList(int i, int i2) {
        return (this.renderNode.hasDisplayList() && this.renderNode.getWidth() == i && this.renderNode.getHeight() == i2) ? false : true;
    }

    public void noClip() {
        this.noClip = true;
    }

    public void setBlur(float f) {
        this.renderNode.setRenderEffect(f > 0.0f ? RenderEffect.createBlurEffect(f, f, Shader.TileMode.CLAMP) : null);
    }

    public void setBlur(float f, RenderEffect renderEffect) {
        RenderEffect createChainEffect;
        RenderNode renderNode = this.renderNode;
        createChainEffect = RenderEffect.createChainEffect(RenderEffect.createBlurEffect(f, f, Shader.TileMode.CLAMP), renderEffect);
        renderNode.setRenderEffect(createChainEffect);
    }

    public void setOnDrawablesRelativePositionChangeListener(Runnable runnable) {
        this.onDrawablesRelativePositionChangeListener = runnable;
    }

    public void setScrollableNoiseSuppressor(DownscaleScrollableNoiseSuppressor downscaleScrollableNoiseSuppressor, int i) {
        this.scrollableNoiseSuppressor = downscaleScrollableNoiseSuppressor;
        this.scrollableNoiseSuppressorIndex = i;
    }

    public void setSize(int i, int i2) {
        this.renderNode.setPosition(0, 0, i, i2);
    }

    public void setUnderSource(BlurredBackgroundSource blurredBackgroundSource) {
        this.underSource = blurredBackgroundSource;
    }

    public void setupRenderer(RenderNodeWithHash.Renderer renderer) {
        if (this.renderNodeWithHash == null) {
            this.renderNodeWithHash = new RenderNodeWithHash(this.renderNode, renderer);
        }
    }

    public void updateDisplayListIfNeeded() {
        this.renderNodeWithHash.updateDisplayListIfNeeded();
    }
}
