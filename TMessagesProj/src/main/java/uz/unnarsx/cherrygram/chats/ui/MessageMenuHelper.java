package uz.unnarsx.cherrygram.chats.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBarPopupWindow.ActionBarPopupWindowLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ChatScrimPopupContainerLayout;
import org.telegram.ui.Components.ReactionsContainerLayout;

import uz.unnarsx.cherrygram.core.configs.CherrygramMessagesConfig;

/**
 * MeeroGram reconstruction of the unpublished Cherrygram MessageMenuHelper.
 * Safe defaults: the iOS-style glass menu visuals that depended on private
 * code are disabled, while the user-facing "open menu by long press" setting
 * keeps working through CherrygramMessagesConfig.
 */
public class MessageMenuHelper {

    private static final MessageMenuHelper[] Instance = new MessageMenuHelper[UserConfig.MAX_ACCOUNT_COUNT];

    public static MessageMenuHelper getInstance(int currentAccount) {
        MessageMenuHelper local = Instance[currentAccount];
        if (local == null) {
            synchronized (MessageMenuHelper.class) {
                local = Instance[currentAccount];
                if (local == null) {
                    local = Instance[currentAccount] = new MessageMenuHelper();
                }
            }
        }
        return local;
    }

    public static final int LONG_PRESS_DURATION = 350;

    public boolean allowNewMessageMenu() {
        return false;
    }

    public boolean allowNewMessageMenu(MessageObject messageObject) {
        return false;
    }

    public boolean showDivider() {
        return false;
    }

    public boolean showCustomDivider(boolean value) {
        return false;
    }

    public boolean openMenuByLongPress() {
        return CherrygramMessagesConfig.INSTANCE.getMsgMenuOpenByLongPress();
    }

    public boolean showGap() {
        return false;
    }

    public int getLongPressDuration() {
        return LONG_PRESS_DURATION;
    }

    public int getCloseAnimationDuration() {
        return 0;
    }

    public void drawCopyInChat(Canvas canvas, View view) {
    }

    public View createGap(Context context, Theme.ResourcesProvider resourcesProvider) {
        View gap = new View(context);
        gap.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuSeparator, resourcesProvider));
        return gap;
    }

    public static boolean isMenuOnRight(View view, MessageObject messageObject) {
        return false;
    }

    public static int getMessageMenuBackgroundColor(Theme.ResourcesProvider resourcesProvider) {
        return Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground, resourcesProvider);
    }

    public static int getMessageMenuGapColor(Theme.ResourcesProvider resourcesProvider) {
        return Theme.getColor(Theme.key_actionBarDefaultSubmenuSeparator, resourcesProvider);
    }

    public void activatePress(View view) {
    }

    public void activatePress(ChatMessageCell cell) {
    }

    public void releasePress(View view) {
    }

    public void releasePress(ChatMessageCell cell) {
    }

    public void setMessagePressed(View view, boolean pressed) {
    }

    public void setMessagePressed(ChatMessageCell cell, boolean pressed) {
    }

    public void applyPressScale(Canvas canvas, ChatMessageCell cell) {
    }

    public void hideMessageView(View view, ChatActivity chatActivity, ChatActivity.ChatActivityAdapter adapter, MessageObject message, Activity parentActivity, boolean animated) {
    }

    public void hideMessageView(View view, ChatActivity chatActivity, ChatActivity.ChatActivityAdapter adapter, MessageObject message, Activity parentActivity, boolean animated, boolean fast) {
    }

    public void runAfterMenuClosed(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    public boolean createMenu(ChatActivity chatActivity, View view, ChatScrimPopupContainerLayout scrimContainer, ReactionsContainerLayout reactionsLayout, MessageObject messageObject, ActionBarPopupWindowLayout popupLayout) {
        return false;
    }

    public static class MaxHeightLinearLayout extends LinearLayout {

        private int maxHeight = AndroidUtilities.dp(320);
        private int maxWidth = Integer.MAX_VALUE;

        public MaxHeightLinearLayout(Context context) {
            super(context);
            setOrientation(LinearLayout.VERTICAL);
        }

        public MaxHeightLinearLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            setOrientation(LinearLayout.VERTICAL);
        }

        public void setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            requestLayout();
        }

        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            requestLayout();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthSpec = widthMeasureSpec;
            if (maxWidth != Integer.MAX_VALUE && MeasureSpec.getSize(widthMeasureSpec) > maxWidth) {
                widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
            }
            super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST));
        }
    }
}
