#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_moutaigua8183_isl_1android_1gaze_Activities_MainActivity_getMsg(JNIEnv *env,
                                                                         jobject instance) {

    // TODO

    return (*env)->NewStringUTF(env, "Hello World");
}