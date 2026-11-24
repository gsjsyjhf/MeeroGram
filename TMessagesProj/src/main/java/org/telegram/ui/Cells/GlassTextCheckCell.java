package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.util.ArrayList;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Switch;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSource;
import org.telegram.ui.Components.glass.controls.GlassIosSettingsSwitch;
import org.telegram.ui.Components.glass.engine.GlassBackdropSource;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes4.dex */
public class GlassTextCheckCell extends TextCheckCell {
    private OnCheckedChangeListener checkedChangeListener;
    private int glassSettingId;
    private final GlassIosSettingsSwitch glassSwitch;
    private boolean hasLegacyIcon;
    private final Theme.ResourcesProvider resourcesProvider;
    private boolean showsCheck;
    private boolean useGlassSwitch;

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes4.dex */
    public interface OnCheckedChangeListener {
        void onCheckedChanged(GlassTextCheckCell glassTextCheckCell, boolean z);
    }

    public GlassTextCheckCell(Context context, int i, boolean z, Theme.ResourcesProvider resourcesProvider) {
        super(context, i, z, resourcesProvider);
        this.showsCheck = true;
        this.useGlassSwitch = true;
        this.glassSettingId = Integer.MIN_VALUE;
        this.resourcesProvider = resourcesProvider;
        GlassIosSettingsSwitch glassIosSettingsSwitch = new GlassIosSettingsSwitch(context);
        this.glassSwitch = glassIosSettingsSwitch;
        glassIosSettingsSwitch.setImportantForAccessibility(2);
        glassIosSettingsSwitch.setOnCheckedChangeListener(new GlassIosSettingsSwitch.OnCheckedChangeListener() { // from class: org.telegram.ui.Cells.GlassTextCheckCell$$ExternalSyntheticLambda0
            public final void onCheckedChanged(GlassIosSettingsSwitch glassIosSettingsSwitch2, boolean z2) {
                GlassTextCheckCell.this.onGlassCheckedChanged(z2);
            }
        });
        addView((View) glassIosSettingsSwitch, (ViewGroup.LayoutParams) createGlassLayoutParams());
        setClipChildren(false);
        setClipToPadding(false);
        updateGlassTheme();
        updateSwitchVisibility();
    }

    public GlassTextCheckCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        this(context, 21, false, resourcesProvider);
    }

    private FrameLayout.LayoutParams createGlassLayoutParams() {
        boolean z = LocaleController.isRTL;
        return LayoutHelper.createFrame(68, 36.0f, (z ? 3 : 5) | 16, z ? 8.5f : 0.0f, 0.0f, z ? 0.0f : 8.5f, 0.0f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onGlassCheckedChanged(boolean z) {
        super.setChecked(z);
        OnCheckedChangeListener onCheckedChangeListener = this.checkedChangeListener;
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChanged(this, z);
        }
    }

    private void updateGlassLayoutDirection() {
        this.glassSwitch.setLayoutDirection(LocaleController.isRTL ? 1 : 0);
    }

    private void updateGlassTheme() {
        GlassIosSettingsSwitch glassIosSettingsSwitch = this.glassSwitch;
        if (glassIosSettingsSwitch == null) {
            return;
        }
        Theme.ResourcesProvider resourcesProvider = this.resourcesProvider;
        glassIosSettingsSwitch.setDarkTheme(resourcesProvider != null ? resourcesProvider.isDark() : Theme.isCurrentThemeDark());
        updateGlassLayoutDirection();
    }

    private void updateSwitchVisibility() {
        if (this.glassSwitch == null) {
            return;
        }
        boolean isUsingGlassSwitch = isUsingGlassSwitch();
        int i = 8;
        this.glassSwitch.setVisibility(isUsingGlassSwitch ? 0 : 8);
        Switch checkBox = getCheckBox();
        if (this.showsCheck && !isUsingGlassSwitch) {
            i = 0;
        }
        checkBox.setVisibility(i);
    }

    public int getGlassSettingId() {
        return this.glassSettingId;
    }

    public GlassIosSettingsSwitch getGlassSwitch() {
        return this.glassSwitch;
    }

    @Override // org.telegram.ui.Cells.TextCheckCell
    public boolean isChecked() {
        return isUsingGlassSwitch() ? this.glassSwitch.isChecked() : super.isChecked();
    }

    public boolean isPointInsideGlassSwitch(float f, float f2) {
        return isUsingGlassSwitch() && f >= ((float) this.glassSwitch.getLeft()) && f <= ((float) this.glassSwitch.getRight()) && f2 >= ((float) this.glassSwitch.getTop()) && f2 <= ((float) this.glassSwitch.getBottom());
    }

    public boolean isUsingGlassSwitch() {
        return this.useGlassSwitch && !this.hasLegacyIcon && this.showsCheck;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Cells.TextCheckCell, android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateGlassTheme();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.telegram.ui.Cells.TextCheckCell, android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override // org.telegram.ui.Cells.TextCheckCell
    public void setCheckBoxIcon(int i) {
        super.setCheckBoxIcon(i);
        this.hasLegacyIcon = i != 0;
        updateSwitchVisibility();
    }

    @Override // org.telegram.ui.Cells.TextCheckCell
    public void setChecked(boolean z) {
        super.setChecked(z);
        this.glassSwitch.setCheckedFromHost(z, isAttachedToWindow());
    }

    @Override // org.telegram.ui.Cells.TextCheckCell, android.view.View
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        GlassIosSettingsSwitch glassIosSettingsSwitch = this.glassSwitch;
        if (glassIosSettingsSwitch != null) {
            glassIosSettingsSwitch.setEnabled(z);
        }
    }

    @Override // org.telegram.ui.Cells.TextCheckCell
    public void setEnabled(boolean z, ArrayList arrayList) {
        super.setEnabled(z, arrayList);
        this.glassSwitch.setEnabled(z);
        this.glassSwitch.setAlpha(1.0f);
    }

    public void setGlassBackdropSource(GlassBackdropSource glassBackdropSource) {
        this.glassSwitch.setBackdropSource(glassBackdropSource);
    }

    public void setGlassSettingId(int i) {
        this.glassSettingId = i;
    }

    public void setGlassSourceCoordinateView(View view) {
        this.glassSwitch.setSourceCoordinateView(view);
    }

    public void setOnGlassCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.checkedChangeListener = onCheckedChangeListener;
    }

    public void setTelegramBackdropSource(BlurredBackgroundSource blurredBackgroundSource) {
        this.glassSwitch.setTelegramBackdropSource(blurredBackgroundSource);
    }

    @Override // org.telegram.ui.Cells.TextCheckCell
    public void setTextAndCheck(CharSequence charSequence, boolean z, boolean z2) {
        super.setTextAndCheck(charSequence, z, z2);
        this.showsCheck = true;
        this.glassSwitch.setCheckedFromHost(z, false);
        updateSwitchVisibility();
    }

    @Override // org.telegram.ui.Cells.TextCheckCell
    public void setTextAndValueAndCheck(String str, String str2, boolean z, boolean z2, boolean z3) {
        super.setTextAndValueAndCheck(str, str2, z, z2, z3);
        this.showsCheck = true;
        this.glassSwitch.setCheckedFromHost(z, false);
        updateSwitchVisibility();
    }

    public void setUseGlassSwitch(boolean z) {
        if (this.useGlassSwitch == z) {
            return;
        }
        this.useGlassSwitch = z;
        updateSwitchVisibility();
    }

    @Override // org.telegram.ui.Cells.TextCheckCell
    public void updateRTL() {
        super.updateRTL();
        if (this.glassSwitch != null) {
            updateGlassLayoutDirection();
            this.glassSwitch.setLayoutParams(createGlassLayoutParams());
        }
    }
}
