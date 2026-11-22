package uz.unnarsx.cherrygram.chats.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import org.telegram.messenger.MessageObject;
import org.telegram.ui.ActionBar.BaseFragment;

/**
 * MeeroGram reconstruction of the unpublished Cherrygram MessageMenuCompactView.
 * With the stub MessageMenuHelper the compact style is never enabled
 * (allowCompactStyle() == false), so this view is never instantiated; it exists
 * to keep the ChatActivity menu builder compiling.
 */
public class MessageMenuCompactView extends FrameLayout {

    public static boolean allowCompactStyle() {
        return false;
    }

    public MessageMenuCompactView(Context context) {
        super(context);
    }

    public MessageMenuCompactView(Context context, MessageObject messageObject, int optionsCount, boolean allowReply, boolean allowEdit, boolean allowForward, boolean allowCopy, boolean allowCopyPhoto, boolean allowCopyLink, boolean allowDelete) {
        super(context);
    }

    public MessageMenuCompactView(BaseFragment fragment, MessageObject messageObject, int optionsCount, boolean allowReply, boolean allowEdit, boolean allowForward, boolean allowCopy, boolean allowCopyPhoto, boolean allowCopyLink, boolean allowDelete) {
        super(fragment != null ? fragment.getParentActivity() : null);
    }

    public MessageMenuCompactView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
