package tw.nekomimi.nekogram;

/**
 * MeeroGram 1.1.1 port of the MeeroX api-key engine shell (libmeerocore).
 *
 * Only the credential-pool bindings survive the port: the lock core, vault
 * seed and automation engines stay MeeroX-only, so the v183 static block's
 * MeeroVaultSeed dependency is deliberately dropped here - loading the lib
 * must not depend on classes that do not exist in this tree.
 *
 * Name-pinned: JNI bindings are name-mangled, so this class must never be
 * renamed (kept by proguard-rules.pro).
 */
public final class MeeroCore {

    private MeeroCore() {
    }

    private static volatile boolean ready;

    static {
        boolean ok = false;
        try {
            System.loadLibrary("meerocore");
            ok = true;
        } catch (Throwable ignored) {
        }
        ready = ok;
    }

    public static boolean ready() {
        return ready;
    }

    /* ---------------- api-key engine ---------------- */
    public static native void nInstallKey0(int id, String hash);

    public static native int nKeyCount();

    public static native int nKeyId(int index);

    public static native String nKeyHash(int index);

    public static native int nKeyAdvance(int current);

    public static native boolean nIsKeyError(String errorText);
}
