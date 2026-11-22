#pragma once
// MeeroGram stub: upstream secure_validator module is not published.
#include <jni.h>

namespace secure_validator {
void maybeForceDisconnectOrUpdate(JNIEnv *env, int instanceNum, int reason);
}
