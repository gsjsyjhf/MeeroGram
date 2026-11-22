/* MeeroGram 1.1.1 - API credential pool + failover engine.
 *
 * Port of the MeeroX meerocore.c key engine (v183 batch 2A / v199). The
 * pool table lives here rather than in Java: slot 0 is the build
 * credential, installed from Java on first use; slots 1..4 are the
 * harvested pool with XOR-obfuscated hash literals (key 0xA7). Rotation
 * decisions and duplicate-id skipping are native, so the table is not
 * visible in a plain Java decompile.
 *
 * Telegram rates each api_id; id 4 - the official Android client's - is
 * capped per device (first sign-in OK, second refused), and limited ids
 * answer API_ID_INVALID / API_ID_RESTRICTED / API_ID_PUBLISHED_FLOOD /
 * CONNECTION_API_ID_INVALID. Those markers trigger a rotate-and-retry in
 * LoginActivity. */

#include <jni.h>
#include <stdlib.h>
#include <string.h>

static char *mc_utf(JNIEnv *env, jstring s) {
    if (s == NULL) return NULL;
    const char *p = (*env)->GetStringUTFChars(env, s, NULL);
    if (p == NULL) return NULL;
    const size_t n = strlen(p) + 1;
    char *cp = malloc(n);
    if (cp != NULL) memcpy(cp, p, n);
    (*env)->ReleaseStringUTFChars(env, s, p);
    return cp;
}

/* UTF-8 -> real UTF-16, then NewString. NewStringUTF expects MODIFIED
 * UTF-8 and chokes on emoji; NewString(jchar*, units) has no such quirk.
 * Key hashes are ASCII, so this is belt-and-braces parity with MeeroX. */
static int mc_u16(const char *s, jchar **out) {
    *out = NULL;
    if (s == NULL) s = "";
    const unsigned char *p = (const unsigned char *) s;
    int units = 0;
    for (const unsigned char *q = p; *q; q++) {
        if ((*q & 0xC0) != 0x80) units += (*q >= 0xF0) ? 2 : 1;
    }
    jchar *b = malloc((size_t) (units + 1) * sizeof(jchar));
    if (b == NULL) return -1;
    int w = 0;
    while (*p) {
        uint32_t cp;
        int n;
        if (*p < 0x80) { cp = *p; n = 1; }
        else if ((*p & 0xE0) == 0xC0) { cp = ((uint32_t) (p[0] & 0x1F) << 6) | (uint32_t) (p[1] & 0x3F); n = 2; }
        else if ((*p & 0xF0) == 0xE0) { cp = ((uint32_t) (p[0] & 0x0F) << 12) | ((uint32_t) (p[1] & 0x3F) << 6) | (uint32_t) (p[2] & 0x3F); n = 3; }
        else if ((*p & 0xF8) == 0xF0) { cp = ((uint32_t) (p[0] & 0x07) << 18) | ((uint32_t) (p[1] & 0x3F) << 12) | ((uint32_t) (p[2] & 0x3F) << 6) | (uint32_t) (p[3] & 0x3F); n = 4; }
        else { cp = *p; n = 1; }
        if (cp > 0xFFFF) {
            uint32_t u = cp - 0x10000;
            b[w++] = (jchar) (0xD800 + (u >> 10));
            b[w++] = (jchar) (0xDC00 + (u & 0x3FF));
        } else {
            b[w++] = (jchar) cp;
        }
        p += n;
    }
    *out = b;
    return w;
}

static jstring mc_jstr(JNIEnv *env, const char *utf8) {
    jchar *b = NULL;
    int n = mc_u16(utf8, &b);
    if (n < 0) return NULL;
    jstring r = (*env)->NewString(env, b, (jsize) n);
    free(b);
    return r;
}

#define MC_CLASS(verb) Java_tw_nekomimi_nekogram_MeeroCore_##verb

/* ---- credential pool ------------------------------------------------ */

static const unsigned char XK = 0xA7;
static int mc_key_id0;
static char mc_key_hash0[64];
static int mc_key0_installed;

/* xor-obfuscated hash literals (each 32 chars + NUL) */
static const unsigned char H1[] = {0x94,0x94,0xc3,0x94,0x90,0x95,0x9e,0x91,0x95,0xc1,0xc6,0xc3,0xc5,0x97,0x96,0xc3,0xc1,0x93,0x90,0xc2,0x91,0xc4,0xc2,0xc2,0xc3,0x93,0xc2,0x94,0x94,0xc4,0xc3,0x91,0x00};
static const unsigned char H2[] = {0xc5,0x96,0x9f,0x93,0x93,0x96,0xc6,0x96,0xc1,0xc1,0x91,0x97,0x90,0xc2,0x96,0x97,0xc6,0x9e,0x9f,0x9e,0x9f,0x9e,0x96,0xc6,0x92,0x93,0x91,0x95,0xc2,0x91,0x95,0x90,0x00};
static const unsigned char H3[] = {0xc2,0xc5,0x97,0x91,0xc3,0x93,0xc6,0xc5,0xc1,0xc5,0x93,0x9e,0xc3,0xc4,0x94,0xc2,0xc2,0xc5,0x96,0xc6,0xc2,0xc5,0x9e,0x9f,0xc6,0xc2,0x97,0xc1,0x92,0x9f,0x96,0xc2,0x00};
static const unsigned char H4[] = {0x97,0x96,0x93,0xc5,0x94,0x92,0xc5,0x91,0x96,0x9f,0x93,0x96,0x97,0x97,0xc5,0x97,0x9f,0x92,0xc5,0x97,0xc3,0x97,0x92,0x90,0x95,0xc1,0x9e,0xc5,0x92,0x96,0x97,0x94,0x00};

static const int MC_IDS[4] = {11535358, 2040, 6, 4};
#define MC_KEY_N 5

static void mc_unxor(char *out, const unsigned char *enc) {
    int i = 0;
    while (enc[i]) { out[i] = (char) (enc[i] ^ XK); i++; }
    out[i] = 0;
}

JNIEXPORT void JNICALL MC_CLASS(nInstallKey0)(JNIEnv *env, jclass c, jint id, jstring hash) {
    (void) c;
    if (mc_key0_installed) return;
    char *h = mc_utf(env, hash);
    if (h != NULL) {
        strncpy(mc_key_hash0, h, sizeof(mc_key_hash0) - 1);
        mc_key_hash0[sizeof(mc_key_hash0) - 1] = 0;
        mc_key_id0 = id;
        mc_key0_installed = 1;
        memset(h, 0, strlen(h));
        free(h);
    }
}

JNIEXPORT jint JNICALL MC_CLASS(nKeyCount)(JNIEnv *env, jclass c) {
    (void) env; (void) c;
    return MC_KEY_N;
}

static jint mc_id_at(int idx) {
    if (idx == 0) return mc_key0_installed ? mc_key_id0 : 0;
    return MC_IDS[idx - 1];
}

JNIEXPORT jint JNICALL MC_CLASS(nKeyId)(JNIEnv *env, jclass c, jint idx) {
    (void) env; (void) c;
    if (idx < 0 || idx >= MC_KEY_N) return 0;
    return mc_id_at(idx);
}

JNIEXPORT jstring JNICALL MC_CLASS(nKeyHash)(JNIEnv *env, jclass c, jint idx) {
    (void) c;
    if (idx < 0 || idx >= MC_KEY_N) return NULL;
    char tmp[64];
    if (idx == 0) {
        if (!mc_key0_installed) return NULL;
        strcpy(tmp, mc_key_hash0);
    } else {
        const unsigned char *enc = H1;
        if (idx == 2) enc = H2; else if (idx == 3) enc = H3; else if (idx == 4) enc = H4;
        mc_unxor(tmp, enc);
    }
    jstring r = mc_jstr(env, tmp);
    memset(tmp, 0, sizeof(tmp));
    return r;
}

/* rotation: walk forward skipping ids that repeat an earlier slot. */
JNIEXPORT jint JNICALL MC_CLASS(nKeyAdvance)(JNIEnv *env, jclass c, jint cur) {
    (void) env; (void) c;
    int next = cur + 1;
    while (next < MC_KEY_N) {
        jint cand = mc_id_at(next);
        int dup = 0;
        for (int i = 0; i < next; i++) {
            if (mc_id_at(i) == cand) { dup = 1; break; }
        }
        if (!dup) return next;
        next++;
    }
    return -1;
}

/* which transport errors blame the credential (and so deserve a rotate) */
JNIEXPORT jboolean JNICALL MC_CLASS(nIsKeyError)(JNIEnv *env, jclass c, jstring err) {
    (void) c;
    char *e = mc_utf(env, err);
    if (e == NULL) return JNI_FALSE;
    static const char *MARKS[] = {
        "API_ID_INVALID", "API_ID_PUBLISHED_FLOOD", "API_ID_RESTRICTED",
        "AUTH_KEY_DUPLICATED", "CONNECTION_API_ID_INVALID"
    };
    jboolean hit = JNI_FALSE;
    for (int i = 0; i < 5; i++) {
        if (strstr(e, MARKS[i]) != NULL) { hit = JNI_TRUE; break; }
    }
    memset(e, 0, strlen(e));
    free(e);
    return hit;
}
