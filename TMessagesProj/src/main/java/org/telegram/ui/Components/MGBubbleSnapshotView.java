package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

    private Bitmap bitmap;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private final int bubbleLeft, bubbleTop;
    private final int bubbleWidth, bubbleHeight;

    /** MeeroGram R14.41: tall messages shrink to this and scroll inside. */
    private int maxHeight;
    private float scroll;
    private float maxScroll;
    private float lastTouchY;
    private boolean dragging;
    private final int touchSlop = dp(6);

    private MGBubbleSnapshotView(Context context, Bitmap bitmap, int bubbleLeft, int bubbleTop, int w, int h) {
        super(context);
        this.bitmap = bitmap;
        this.bubbleLeft = bubbleLeft;
        this.bubbleTop = bubbleTop;
        this.bubbleWidth = w;
        this.bubbleHeight = h;
        setWillNotDraw(false);
    }

    /**
     * MeeroGram R14.43: the region of {@code cell} that holds the bubble, in cell
     * coordinates: {left, top, right, bottom}.
     */
    public static int[] bubbleBounds(ChatMessageCell cell) {
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
        return new int[]{
                Math.max(0, left - dp(4)),
                Math.max(0, top - dp(3)),
                Math.min(cell.getMeasuredWidth(), right + dp(4)),
                Math.min(cell.getMeasuredHeight(), bottom + dp(3))
        };
    }

    /** Replaces the picture once the exact pixels arrive from the window copy. */
    public void setBitmap(Bitmap value) {
        final Bitmap old = bitmap;
        bitmap = value;
        if (old != null && old != value && !old.isRecycled()) {
            try {
                old.recycle();
            } catch (Throwable ignore) {
            }
        }
        invalidate();
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
            final float mgOldAlpha = cell.getAlpha();
            final boolean mgAlphaForced = mgOldAlpha != 1f;
            if (mgAlphaForced) {
                cell.setAlpha(1f);
            }
            try {
                canvas.save();
                cell.drawBackground(canvas,
                        cell.getBackgroundDrawableLeft(),
                        cell.getPaddingTop() + cell.getBackgroundDrawableTop(),
                        cell.getBackgroundDrawableRight(),
                        cell.getPaddingTop() + cell.getBackgroundDrawableBottom(),
                        false, false, false, 0);
                canvas.restore();
            } catch (Throwable ignore) {
            }
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
            if (mgAlphaForced) {
                cell.setAlpha(mgOldAlpha);
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
        // MeeroGram R14.44: keep only what the cell actually painted. The capture rect is
        // the row, so without this the snapshot carried a strip of chat background beside
        // the bubble; trimming to the painted pixels leaves nothing but the bubble.
        int tl = 0, tt = 0, tr = w - 1, tb = h - 1;
        try {
            final int step = 2;
            int minX = w, minY = h, maxX = -1, maxY = -1;
            final int[] row = new int[w];
            for (int y = 0; y < h; y += step) {
                bmp.getPixels(row, 0, w, 0, y, w, 1);
                for (int x = 0; x < w; x += step) {
                    if ((row[x] >>> 24) > 8) {
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            if (maxX > minX && maxY > minY) {
                tl = minX;
                tt = minY;
                tr = Math.min(w - 1, maxX + step);
                tb = Math.min(h - 1, maxY + step);
            }
        } catch (Throwable ignore) {
        }
        if (tl > 0 || tt > 0 || tr < w - 1 || tb < h - 1) {
            try {
                final Bitmap cropped = Bitmap.createBitmap(bmp, tl, tt, tr - tl + 1, tb - tt + 1);
                bmp.recycle();
                left += tl;
                top += tt;
                bmp = cropped;
            } catch (Throwable ignore) {
            }
        }
        return new MGBubbleSnapshotView(context, bmp, left, top, bmp.getWidth(), bmp.getHeight());
    }

    public int getBubbleLeftInCell() {
        return bubbleLeft;
    }

    public int getBubbleTopInCell() {
        return bubbleTop;
    }

    /** Space the popup can spare; 0 = no limit. */
    public void setMaxHeight(int value) {
        if (maxHeight != value) {
            maxHeight = value;
            requestLayout();
            invalidate();
        }
    }

    public boolean isScrollable() {
        return maxScroll > 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int h = bubbleHeight;
        if (maxHeight > 0) {
            h = Math.min(h, maxHeight);
        }
        h = Math.max(dp(40), h);
        maxScroll = Math.max(0, bubbleHeight - h);
        if (scroll > maxScroll) {
            scroll = maxScroll;
        }
        setMeasuredDimension(bubbleWidth, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        canvas.save();
        canvas.translate(0, -scroll);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.restore();
        if (maxScroll > 0) {
            final float trackW = dp(3);
            final float inset = dp(3);
            final float h = getMeasuredHeight();
            final float thumbH = Math.max(dp(28), h * (h / (float) bubbleHeight));
            final float travel = Math.max(0, h - thumbH);
            final float y = travel * (scroll / maxScroll);
            final float x = getMeasuredWidth() - inset - trackW;
            paint.setColor(0x40000000);
            canvas.drawRoundRect(new RectF(x, y, x + trackW, y + thumbH), trackW / 2f, trackW / 2f, paint);
            paint.setColor(0x66FFFFFF);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (maxScroll <= 0) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                lastTouchY = event.getY();
                dragging = false;
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                final float dy = lastTouchY - event.getY();
                if (!dragging && Math.abs(dy) > touchSlop) {
                    dragging = true;
                    lastTouchY = event.getY();
                    final ViewGroup parent = (ViewGroup) getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                }
                if (dragging) {
                    scroll += dy;
                    if (scroll < 0) scroll = 0;
                    if (scroll > maxScroll) scroll = maxScroll;
                    lastTouchY = event.getY();
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                return true;
        }
        return true;
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
