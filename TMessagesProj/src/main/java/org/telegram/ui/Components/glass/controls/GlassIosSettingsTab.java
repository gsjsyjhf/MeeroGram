package org.telegram.ui.Components.glass.controls;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
public final class GlassIosSettingsTab {
    public final int iconResId;
    public final String symbol;
    public final CharSequence title;

    public GlassIosSettingsTab(CharSequence charSequence, int i) {
        this(charSequence, i, "");
    }

    public GlassIosSettingsTab(CharSequence charSequence, int i, String str) {
        this.title = charSequence;
        this.iconResId = i;
        this.symbol = str == null ? "" : str;
    }
}
