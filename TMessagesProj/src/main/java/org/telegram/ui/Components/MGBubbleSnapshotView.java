package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.FrameLayout;

import org.telegram.ui.Cells.ChatMessageCell;

/**
 * MeeroGram R14.39: a still image of the bubble the user pressed, placed inside the
 * message-menu popup so the stack reads exactly like iOS / MeeroX:
 *
 *     [ reaction bar ]
 *     [ bubble ]
 *     [ menu card ]
 *
 * Upstream keeps the bubble in the chat list and lets the menu window cover it, so
 * the three pieces can never be stacked. Capturing the bubble to a bitmap and
 * putting it in the popup is the only way to get that order.
 */
public class MGBubbleSnapshotView extends FrameLayout {

    private final Bitmap bitmap;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private final int bubbleLeft, bubbleTop;
    private final int bubbleWidth, bubbleHeight;

    private MGBubbleSnapshotView(Context context, Bitmap bitmap, int bubbleLeft, int bubbleTop, int w, int h) {
        super(context);
        this.bitmap = bitmap;
        this.bubbleLeft = bubbleLeft;
        this.bubbleTop = bubbleTop;
        this.bubbleWidth = w;
        this.bubbleHeight = h;
        setWillNotDraw(false);
    }

    /** Renders the bubble of {@code cell} into a bitmap, or null when it can't be captured. */
    public static MGBubbleSnapshotView capture(Context context, ChatMessageCell cell) {
        if (cell == null || cell.getMeasuredWidth() <= 0 || cell.getMeasuredHeight() <= 0) {
            return null;
        }
        int left, top, right, bottom;
        try {
            final int pad = cell.getPaddingTop();
            left = cell.getBackgroundDrawableLeft();
            right = cell.getBackgroundDrawableRight();
            top = pad + cell.getBackgroundDrawableTop();
            bottom = pad + cell.getBackgroundDrawableBottom();
        } catch (Throwable e) {
            return null;
        }
        if (right <= left || bottom <= top) {
            left = 0;
            top = cell.getPaddingTop();
            right = cell.getMeasuredWidth();
            bottom = cell.getMeasuredHeight();
        }
        try {
            final org.telegram.messenger.ImageReceiver avatar = cell.getAvatarImage();
            if (avatar != null) {
                final int aLeft = (int) avatar.getImageX();
                final int aRight = (int) avatar.getImageX2();
                final int aTop = (int) avatar.getImageY();
                final int aBottom = (int) avatar.getImageY2();
                if (aRight > aLeft && aBottom > aTop) {
                    left = Math.min(left, aLeft);
                    right = Math.max(right, aRight);
                    top = Math.min(top, aTop);
                    bottom = Math.max(bottom, aBottom);
                }
            }
        } catch (Throwable ignore) {
        }
        final int padX = dp(6), padY = dp(4);
        left = Math.max(0, left - padX);
        top = Math.max(0, top - padY);
        right = Math.min(cell.getMeasuredWidth(), right + padX);
        bottom = Math.min(cell.getMeasuredHeight(), bottom + padY);

        final int w = right - left;
        final int h = bottom - top;
        if (w <= 0 || h <= 0 || (long) w * h > 4096L * 4096L) {
            return null;
        }
        Bitmap bmp;
        try {
            bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        } catch (Throwable e) {
            return null;
        }
        final Canvas canvas = new Canvas(bmp);
        canvas.translate(-left, -top);
        try {
            cell.setInvalidatesParent(false);
            if (cell.drawBackgroundInParent()) {
                canvas.save();
                canvas.translate(0, cell.getPaddingTop());
                cell.drawBackgroundInternal(canvas, true);
                canvas.restore();
            }
            cell.draw(canvas);
            if (cell.hasNameLayout()) {
                canvas.save();
                cell.drawNamesLayout(canvas, 1f);
                canvas.restore();
            }
            if (cell.hasOutboundsContent()) {
                canvas.save();
                canvas.translate(0, cell.getPaddingTop());
                cell.drawOutboundsContent(canvas);
                canvas.restore();
            }
        } catch (Throwable e) {
            try {
                bmp.recycle();
            } catch (Throwable ignore) {
            }
            return null;
        } finally {
            try {
                cell.setInvalidatesParent(false);
            } catch (Throwable ignore) {
            }
        }
        return new MGBubbleSnapshotView(context, bmp, left, top, w, h);
    }

    public int getBubbleLeftInCell() {
        return bubbleLeft;
    }

    public int getBubbleTopInCell() {
        return bubbleTop;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(bubbleWidth, bubbleHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }

    public void release() {
        try {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        } catch (Throwable ignore) {
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }
}
