package uz.unnarsx.cherrygram.chats.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import org.telegram.ui.ActionBar.Theme;

/**
 * MeeroGram reconstruction of the unpublished Cherrygram reactions-menu glass
 * background. Draws a plain themed rounded rect so the reactions popup keeps a
 * visible background. With the stub MessageMenuHelper (allowNewMessageMenu ==
 * false) this class is never instantiated; it exists to keep the tree compiling.
 */
public class MessageMenuReactionsGlass {

    private final Theme.ResourcesProvider resourcesProvider;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MessageMenuReactionsGlass(Theme.ResourcesProvider resourcesProvider) {
        this.resourcesProvider = resourcesProvider;
    }

    public void draw(Canvas canvas, RectF rect, float radius, int alpha, boolean isWindow) {
        try {
            paint.setColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground, resourcesProvider));
            paint.setAlpha(alpha);
            canvas.drawRoundRect(rect, radius, radius, paint);
        } catch (Exception ignored) {
        }
    }
}
