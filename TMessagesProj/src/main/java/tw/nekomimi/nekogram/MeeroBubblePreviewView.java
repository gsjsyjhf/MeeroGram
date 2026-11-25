package tw.nekomimi.nekogram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

/**
 * MeeroGram R14: a real preview of the selected bubble style.
 *
 * It draws with MeeroBubbleStyles.drawPreview(...), which uses exactly the same
 * path helpers as the real chat bubbles, so the preview can never drift away
 * from what the chat shows.
 */
public class MeeroBubblePreviewView extends View {

    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Theme.ResourcesProvider resourcesProvider;

    public MeeroBubblePreviewView(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        textPaint.setTextSize(AndroidUtilities.dp(14));
        timePaint.setTextSize(AndroidUtilities.dp(10));
    }

    private int color(int key) {
        try {
            return resourcesProvider != null ? Theme.getColor(key, resourcesProvider) : Theme.getColor(key);
        } catch (Throwable t) {
            return 0xff3390ec;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int style = MeeroBubbleStyles.current();
        final float w = getWidth();
        final float h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        final float dp = AndroidUtilities.density;
        final float bubbleH = 34 * dp;
        final float margin = 14 * dp;
        final float top = (h - bubbleH) / 2f;

        final float inL = margin;
        final float inR = margin + Math.min(w * 0.52f, 190 * dp);
        final float outR = w - margin;
        final float outL = outR - Math.min(w * 0.40f, 150 * dp);

        // incoming bubble
        MeeroBubbleStyles.drawPreview(canvas, style, true, inL, top, inR, top + bubbleH, color(Theme.key_chat_inBubble));
        // outgoing bubble
        MeeroBubbleStyles.drawPreview(canvas, style, false, outL, top, outR, top + bubbleH, color(Theme.key_chat_outBubble));

        textPaint.setColor(color(Theme.key_chat_messageTextIn));
        canvas.drawText("\u0645\u0631\u062d\u0628\u0627\u064b", inL + 14 * dp, top + bubbleH / 2f + 5 * dp, textPaint);

        textPaint.setColor(color(Theme.key_chat_messageTextOut));
        canvas.drawText("\u0623\u0647\u0644\u0627\u064b \u0628\u0643", outL + 14 * dp, top + bubbleH / 2f + 5 * dp, textPaint);
    }
}
