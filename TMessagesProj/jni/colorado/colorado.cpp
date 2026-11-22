// MeeroGram stub: upstream colorado native module is not published.
#include "colorado/colorado.h"

int videoOnJNILoad(JavaVM *vm, JNIEnv *env) {
    (void)vm; (void)env;
    return JNI_TRUE;
}
