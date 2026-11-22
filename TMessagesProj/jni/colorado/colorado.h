#pragma once
// MeeroGram stub: the upstream Cherrygram "colorado" native module is not
// published in any public tree. jni.c only needs this one symbol.
#include <jni.h>

int videoOnJNILoad(JavaVM *vm, JNIEnv *env);
