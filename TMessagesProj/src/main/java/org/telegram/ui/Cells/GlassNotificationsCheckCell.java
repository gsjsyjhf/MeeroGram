package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSource;
import org.telegram.ui.Components.glass.controls.GlassIosSettingsSwitch;
import org.telegram.ui.Components.glass.engine.GlassBackdropSource;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes4.dex */
public class GlassNotificationsCheckCell extends NotificationsCheckCell {
    private OnCheckedChangeListener checkedChangeListener;
    private int glassSettingId;
    private final GlassIosSettingsSwitch glassSwitch;
    private final Theme.ResourcesProvider resourcesProvider;

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes4.dex */
    public interface OnCheckedChangeListener {
        void onCheckedChanged(GlassNotificationsCheckCell glassNotificationsCheckCell, boolean z);
    }

    public GlassNotificationsCheckCell(Context context, int i, int i2, boolean z, Theme.ResourcesProvider resourcesProvider) {
        super(context, i, i2, z, resourcesProvider);
        this.glassSettingId = Integer.MIN_VALUE;
        this.resourcesProvider = resourcesProvider;
        setDrawLine(false);
        getCheckBox().setVisibility(8);
        GlassIosSettingsSwitch glassIosSettingsSwitch = new GlassIosSettingsSwitch(context);
        this.glassSwitch = glassIosSettingsSwitch;
        glassIosSettingsSwitch.setImportantForAccessibility(2);
        glassIosSettingsSwitch.setOnCheckedChangeListener(new GlassIosSettingsSwitch.OnCheckedChangeListener() { // from class: org.telegram.ui.Cells.GlassNotificationsCheckCell$$ExternalSyntheticLambda0
            public final void onCheckedChanged(GlassIosSettingsSwitch glassIosSettingsSwitch2, boolean z2) {
                GlassNotificationsCheckCell.this.onGlassCheckedChanged(z2);
            }
        });
        addView((View) glassIosSettingsSwitch, (ViewGroup.LayoutParams) createGlassLayoutParams());
        setClipChildren(false);
        setClipToPadding(false);
        updateGlassTheme();
    }

    public GlassNotificationsCheckCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        this(context, 21, 70, false, resourcesProvider);
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

    public int getGlassSettingId() {
        return this.glassSettingId;
    }

    @Override // org.telegram.ui.Cells.NotificationsCheckCell
    public boolean isChecked() {
        return this.glassSwitch.isChecked();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateGlassTheme();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        this.glassSwitch.setBackdropSource((GlassBackdropSource) null);
        this.glassSwitch.setSourceCoordinateView((View) null);
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        if (this.glassSwitch != null) {
            updateGlassLayoutDirection();
            this.glassSwitch.setLayoutParams(createGlassLayoutParams());
        }
    }

    @Override // org.telegram.ui.Cells.NotificationsCheckCell
    public void setChecked(boolean z) {
        super.setChecked(z);
        this.glassSwitch.setCheckedFromHost(z, isAttachedToWindow());
    }

    @Override // org.telegram.ui.Cells.NotificationsCheckCell
    public void setChecked(boolean z, int i) {
        super.setChecked(z, i);
        this.glassSwitch.setCheckedFromHost(z, isAttachedToWindow());
    }

    @Override // android.view.View
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        getCheckBox().setEnabled(z);
        GlassIosSettingsSwitch glassIosSettingsSwitch = this.glassSwitch;
        if (glassIosSettingsSwitch != null) {
            glassIosSettingsSwitch.setEnabled(z);
        }
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

    @Override // org.telegram.ui.Cells.NotificationsCheckCell
    public void setTextAndValueAndIconAndCheck(CharSequence charSequence, CharSequence charSequence2, int i, boolean z, int i2, boolean z2, boolean z3, boolean z4) {
        super.setTextAndValueAndIconAndCheck(charSequence, charSequence2, i, z, i2, z2, z3, z4);
        getCheckBox().setVisibility(8);
        this.glassSwitch.setCheckedFromHost(z, z4 && isAttachedToWindow());
    }
}
