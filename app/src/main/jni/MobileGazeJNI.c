#include <jni.h>
#include <stdio.h>

JNIEXPORT jbyteArray JNICALL
Java_com_moutaigua8183_isl_1android_1gaze_JNInterface_MobileGazeInterface_getMsg(JNIEnv *env,
                                                                                 jobject instance,
                                                                                 jbyteArray content_) {
    jbyte *contentPtr = (*env)->GetByteArrayElements(env, content_, NULL);
    jsize length = (*env)->GetArrayLength(env, content_);
    // TODO

    (*env)->ReleaseByteArrayElements(env, content_, contentPtr, 0);
}