/**
 * This is the source code of Meerogram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 *
 * YouTube-style floating glass action cluster for the dialogs screen:
 * a glass pill holding two circular buttons (scan QR, compose new message),
 * plus a smaller separate glass pill ("Edit") that opens the tab organizer.
 * The glass backgrounds reuse the fragment's blur3 liquid-glass factory and
 * follow the theme through the supplied Theme.ResourcesProvider.
 */

package uz.unnarsx.cherrygram.core.ui.floating;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.blur3.BlurredBackgroundDrawableViewFactory;
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;
import org.telegram.ui.Components.blur3.drawable.color.impl.BlurredBackgroundProviderImpl;

public class MeeroFloatingActionsView extends FrameLayout implements Theme.Colorable {

    private static final int SIZE_ICON_BUTTON = 44;
    private static final int SIZE_PILL = 56;
    private static final int SIZE_EDIT_PILL = 36;

    private final Theme.ResourcesProvider resourcesProvider;

    private final LinearLayout actionsPill;
    private final FrameLayout editPill;
    private final ImageView scanButton;
    private final ImageView composeButton;
    private final TextView editTextView;

    private Drawable scanRipple;
    private Drawable composeRipple;

    private int lastIconColor;

    public MeeroFloatingActionsView(@NonNull Context context) {
        this(context, null, null);
    }

    public MeeroFloatingActionsView(@NonNull Context context, @Nullable Theme.ResourcesProvider resourcesProvider) {
        this(context, resourcesProvider, null);
    }

    public MeeroFloatingActionsView(@NonNull Context context, @Nullable Theme.ResourcesProvider resourcesProvider, @Nullable BlurredBackgroundDrawableViewFactory glassFactory) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        setClipChildren(false);
        setClipToPadding(false);

        final LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        addView(row, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        // Main glass pill with two circular icon buttons (like YouTube's floating cluster).
        actionsPill = new LinearLayout(context);
        actionsPill.setOrientation(LinearLayout.HORIZONTAL);
        actionsPill.setGravity(Gravity.CENTER_VERTICAL);
        actionsPill.setPadding(dp(6), 0, dp(6), 0);
        row.addView(actionsPill, LayoutHelper.createLinear(dp(SIZE_ICON_BUTTON * 2 + 12), dp(SIZE_PILL)));

        scanButton = createIconButton(context);
        scanButton.setImageResource(R.drawable.msg_qr_scan);
        scanButton.setContentDescription(LocaleController.getString(R.string.ScanQrCode));
        scanRipple = Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider));
        scanButton.setBackground(scanRipple);
        scanButton.setOnClickListener(v -> {
            if (scanListener != null) {
                scanListener.onClick(v);
            }
        });
        actionsPill.addView(scanButton, LayoutHelper.createLinear(SIZE_ICON_BUTTON, SIZE_ICON_BUTTON));

        composeButton = createIconButton(context);
        composeButton.setImageResource(R.drawable.msg_add);
        composeButton.setContentDescription(LocaleController.getString(R.string.NewMessageTitle));
        composeRipple = Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider));
        composeButton.setBackground(composeRipple);
        composeButton.setOnClickListener(v -> {
            if (composeListener != null) {
                composeListener.onClick(v);
            }
        });
        actionsPill.addView(composeButton, LayoutHelper.createLinear(SIZE_ICON_BUTTON, SIZE_ICON_BUTTON));

        // Separate smaller glass pill that opens the tab organizer ("Edit").
        editPill = new FrameLayout(context);
        editTextView = new TextView(context);
        editTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        editTextView.setSingleLine(true);
        editTextView.setTypeface(AndroidUtilities.bold());
        editTextView.setGravity(Gravity.CENTER);
        editTextView.setText(LocaleController.getString(R.string.Edit));
        editTextView.setContentDescription(LocaleController.getString(R.string.Edit));
        editPill.addView(editTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER, 14, 0, 14, 0));
        editPill.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onClick(v);
            }
        });
        row.addView(editPill, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, dp(SIZE_EDIT_PILL), Gravity.CENTER_VERTICAL, 6, 0, 0, 0));

        if (glassFactory != null) {
            final BlurredBackgroundDrawable pillGlass = glassFactory.create(actionsPill, BlurredBackgroundProviderImpl.mainTabs(resourcesProvider));
            pillGlass.setRadius(dp(SIZE_PILL / 2f));
            pillGlass.setPadding(dp(6));
            actionsPill.setBackground(pillGlass);

            final BlurredBackgroundDrawable editGlass = glassFactory.create(editPill, BlurredBackgroundProviderImpl.mainTabs(resourcesProvider));
            editGlass.setRadius(dp(SIZE_EDIT_PILL / 2f));
            editGlass.setPadding(dp(6));
            editPill.setBackground(editGlass);
        }

        updateColors();
    }

    private ImageView createIconButton(Context context) {
        final ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setPadding(dp(10), dp(10), dp(10), dp(10));
        ScaleStateListAnimator.apply(imageView);
        return imageView;
    }

    private View.OnClickListener scanListener;
    private View.OnClickListener composeListener;
    private View.OnClickListener editListener;

    public void setOnScanClickListener(View.OnClickListener listener) {
        scanListener = listener;
    }

    public void setOnComposeClickListener(View.OnClickListener listener) {
        composeListener = listener;
    }

    public void setOnEditClickListener(View.OnClickListener listener) {
        editListener = listener;
    }

    @Override
    public void updateColors() {
        final int color = Theme.getColor(Theme.key_glass_tabUnselected, resourcesProvider);
        if (color != lastIconColor) {
            lastIconColor = color;
            final PorterDuffColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
            scanButton.setColorFilter(filter);
            composeButton.setColorFilter(filter);
            editTextView.setTextColor(color);
        }
        final int selectorColor = Theme.getColor(Theme.key_listSelector, resourcesProvider);
        if (scanRipple != null) {
            Theme.setSelectorDrawableColor(scanRipple, selectorColor, false);
        }
        if (composeRipple != null) {
            Theme.setSelectorDrawableColor(composeRipple, selectorColor, false);
        }
    }
}
