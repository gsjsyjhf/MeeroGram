package tw.nekomimi.nekogram;

import android.content.SharedPreferences;
import android.graphics.Typeface;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * App-wide font selection for Meerogram (ported from frozen MeeroX).
 *
 * Bundled faces live in assets/fonts/, user-supplied .ttf/.otf files are
 * copied into files/meerox_fonts/. The chosen face is returned from
 * AndroidUtilities.getTypeface(), which every text view in the app resolves
 * through, so a change applies everywhere at once.
 */
public class MeeroFonts {

    /* v189 (batch 3C): the bundled-face table (ids, titles, asset paths) and
     * the user-file prefix now come from the sealed motion table (dom 'C');
     * the literals below only serve the no-lib fallback, byte-identical. */
    public static final String DEFAULT = "default";
    public static final String CUSTOM_PREFIX = initPrefix();

    private static volatile String[] fontTab;

    private static String[] fontTab() {
        // MeeroGram: the sealed native table of MeeroX is not shipped here;
        // the literal fallback table below is the source of truth.
        return null;
    }

    private static String initPrefix() {
        return "custom:";
    }

    private static final String PREFS = "meerox_fonts";
    private static final HashMap<String, Typeface> cache = new HashMap<>();

    private static String selected;
    private static boolean loaded;

    public static class Option {
        public final String id;
        public final String title;
        /** asset path, or null for the stock font / a user file */
        public final String asset;

        Option(String id, String title, String asset) {
            this.id = id;
            this.title = title;
            this.asset = asset;
        }
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, 0);
    }

    public static void load() {
        if (loaded) return;
        selected = prefs().getString("font", DEFAULT);
        loaded = true;
    }

    public static String getSelected() {
        load();
        return selected;
    }

    public static void setSelected(String id) {
        load();
        selected = id;
        prefs().edit().putString("font", id).apply();
        cache.clear();
    }

    public static File customDir() {
        File d = new File(ApplicationLoader.applicationContext.getFilesDir(), "meerox_fonts");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static ArrayList<Option> getOptions() {
        ArrayList<Option> list = new ArrayList<>();
        final String[] t = fontTab();
        if (t != null) {
            for (int i = 0; i < 30; i += 3) {
                list.add(new Option(t[i], t[i + 1], t[i + 2].isEmpty() ? null : t[i + 2]));
            }
        } else {
            list.add(new Option(DEFAULT, "\u0627\u0644\u0627\u0641\u062a\u0631\u0627\u0636\u064a", null));
            // R13.3: the bundled faces of the reference build (extracted from its APK).
            list.add(new Option("kg_iransans", "\u0625\u064a\u0631\u0627\u0646 \u0633\u0627\u0646\u0633", "fonts/iransans.ttf"));
            list.add(new Option("kg_iransans_bold", "\u0625\u064a\u0631\u0627\u0646 \u0633\u0627\u0646\u0633 \u0639\u0631\u064a\u0636", "fonts/iransans_bold.ttf"));
            list.add(new Option("kg_iransans_light", "\u0625\u064a\u0631\u0627\u0646 \u0633\u0627\u0646\u0633 \u0631\u0641\u064a\u0639", "fonts/iransans_light.ttf"));
            list.add(new Option("kg_iransans_dn", "\u0625\u064a\u0631\u0627\u0646 \u0633\u0627\u0646\u0633 DN", "fonts/iransans_dn.ttf"));
            list.add(new Option("kg_iransans_dn_bold", "\u0625\u064a\u0631\u0627\u0646 \u0633\u0627\u0646\u0633 DN \u0639\u0631\u064a\u0636", "fonts/iransans_dn_bold.ttf"));
            list.add(new Option("kg_iransans_dn_light", "\u0625\u064a\u0631\u0627\u0646 \u0633\u0627\u0646\u0633 DN \u0631\u0641\u064a\u0639", "fonts/iransans_dn_light.ttf"));
            list.add(new Option("kg_iranyekan", "\u0625\u064a\u0631\u0627\u0646 \u064a\u0643\u0627\u0646", "fonts/iranyekan.ttf"));
            list.add(new Option("kg_iranyekan_bold", "\u0625\u064a\u0631\u0627\u0646 \u064a\u0643\u0627\u0646 \u0639\u0631\u064a\u0636", "fonts/iranyekan_bold.ttf"));
            list.add(new Option("kg_iranyekan_light", "\u0625\u064a\u0631\u0627\u0646 \u064a\u0643\u0627\u0646 \u0631\u0641\u064a\u0639", "fonts/iranyekan_light.ttf"));
            list.add(new Option("kg_vazir", "\u0648\u0632\u064a\u0631", "fonts/vazir.ttf"));
            list.add(new Option("kg_vazir_bold", "\u0648\u0632\u064a\u0631 \u0639\u0631\u064a\u0636", "fonts/vazir_bold.ttf"));
            list.add(new Option("kg_vazir_light", "\u0648\u0632\u064a\u0631 \u0631\u0641\u064a\u0639", "fonts/vazir_light.ttf"));
            list.add(new Option("kg_titr", "\u062a\u062a\u0631", "fonts/titr.ttf"));
            list.add(new Option("kg_koodak", "\u0643\u0648\u062f\u0643", "fonts/koodak.ttf"));
            list.add(new Option("kg_morvarid", "\u0645\u0631\u0648\u0627\u0631\u064a\u062f", "fonts/morvarid.ttf"));
            list.add(new Option("kg_hama", "\u0647\u0645\u0627", "fonts/hama.ttf"));
            list.add(new Option("kg_afsaneh", "\u0623\u0641\u0633\u0627\u0646\u0629", "fonts/afsaneh.ttf"));
            list.add(new Option("kg_dastnevis", "\u062e\u0637 \u0627\u0644\u064a\u062f", "fonts/dastnevis.ttf"));
            list.add(new Option("kg_tv", "\u062a\u064a \u0641\u064a", "fonts/tv.ttf"));
            list.add(new Option("kg_riitalic", "\u0631\u0648\u0628\u0648\u062a\u0648 \u0645\u0627\u0626\u0644", "fonts/riitalic.ttf"));
            list.add(new Option("ios15", "\u0622\u064a \u0623\u0648 \u0625\u0633 15", "fonts/meerox_f8.ttf"));
            list.add(new Option("arabicui", "\u0639\u0631\u0628\u064a - \u0648\u0627\u062c\u0647\u0629 \u0627\u0644\u0646\u0635", "fonts/meerox_f1.ttf"));
            list.add(new Option("arabicuidisplay", "\u0639\u0631\u0628\u064a - \u0648\u0627\u062c\u0647\u0629 \u0627\u0644\u0639\u0631\u0636", "fonts/meerox_f6.ttf"));
            list.add(new Option("arefruqaa", "\u0639\u0627\u0631\u0641 \u0631\u0642\u0639\u0629", "fonts/meerox_f3.ttf"));
            list.add(new Option("gs45", "\u062c\u064a \u0625\u0633 45 \u0639\u0631\u0628\u064a", "fonts/meerox_f7.ttf"));
            list.add(new Option("cairo", "\u0627\u0644\u0642\u0627\u0647\u0631\u0629", "fonts/meero_cairo.ttf"));
            list.add(new Option("tajawal", "\u062a\u062c\u0648\u0627\u0644", "fonts/meero_tajawal.ttf"));
            list.add(new Option("almarai", "\u0627\u0644\u0645\u0631\u0627\u0639\u064a", "fonts/meero_almarai.ttf"));
            list.add(new Option("inter", "\u0625\u0646\u062a\u0631", "fonts/meero_inter.ttf"));
        }

        File[] files = customDir().listFiles();
        if (files != null) {
            for (File f : files) {
                String n = f.getName().toLowerCase();
                if (n.endsWith(".ttf") || n.endsWith(".otf")) {
                    list.add(new Option(CUSTOM_PREFIX + f.getName(), f.getName(), null));
                }
            }
        }
        return list;
    }

    public static String titleOf(String id) {
        for (Option o : getOptions()) {
            if (o.id.equals(id)) return o.title;
        }
        return "\u0627\u0644\u0627\u0641\u062a\u0631\u0627\u0636\u064a";
    }

    /** Copies a user-picked font into our directory. Returns its option id, or null. */
    public static String importFont(InputStream in, String displayName) {
        try {
            String safe = displayName.replaceAll("[^A-Za-z0-9._-]", "_");
            if (!safe.toLowerCase().endsWith(".ttf") && !safe.toLowerCase().endsWith(".otf")) {
                safe = safe + ".ttf";
            }
            File out = new File(customDir(), safe);
            try (FileOutputStream fos = new FileOutputStream(out)) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) > 0) fos.write(buf, 0, r);
            }
            // Reject anything Android cannot actually load, so a bad file
            // cannot leave the UI without a usable font.
            Typeface probe = Typeface.createFromFile(out);
            if (probe == null) {
                out.delete();
                return null;
            }
            return CUSTOM_PREFIX + out.getName();
        } catch (Throwable e) {
            FileLog.e(e);
            return null;
        }
    }

    public static boolean deleteCustom(String id) {
        if (id == null || !id.startsWith(CUSTOM_PREFIX)) return false;
        File f = new File(customDir(), id.substring(CUSTOM_PREFIX.length()));
        boolean ok = f.delete();
        if (ok && id.equals(getSelected())) setSelected(DEFAULT);
        cache.clear();
        return ok;
    }

    private static Typeface resolve(String id) {
        if (id == null || DEFAULT.equals(id)) return null;
        Typeface c = cache.get(id);
        if (c != null) return c;
        try {
            Typeface tf;
            if (id.startsWith(CUSTOM_PREFIX)) {
                File f = new File(customDir(), id.substring(CUSTOM_PREFIX.length()));
                if (!f.exists()) return null;
                tf = Typeface.createFromFile(f);
            } else {
                String asset = null;
                for (Option o : getOptions()) {
                    if (o.id.equals(id)) { asset = o.asset; break; }
                }
                if (asset == null) return null;
                tf = Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(), asset);
            }
            if (tf != null) cache.put(id, tf);
            return tf;
        } catch (Throwable e) {
            FileLog.e(e);
            return null;
        }
    }

    /** Preview face for a row in the picker. */
    public static Typeface previewOf(String id) {
        return resolve(id);
    }

    /**
     * The face the whole app should use, or null to keep Telegram's own.
     * Bold/italic are derived so headers and emphasis stay consistent.
     */
    public static Typeface appTypeface(boolean bold, boolean italic) {
        Typeface base = resolve(getSelected());
        if (base == null) return null;

        int style = Typeface.NORMAL;
        if (bold && italic) style = Typeface.BOLD_ITALIC;
        else if (bold) style = Typeface.BOLD;
        else if (italic) style = Typeface.ITALIC;

        if (style == Typeface.NORMAL) return base;

        String key = getSelected() + "#" + style;
        Typeface c = cache.get(key);
        if (c != null) return c;
        Typeface derived = Typeface.create(base, style);
        cache.put(key, derived);
        return derived;
    }

    public static void clearCache() {
        cache.clear();
    }
}
