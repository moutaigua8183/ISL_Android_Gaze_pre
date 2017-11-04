#include<jni.h>
#include<string.h>

jstring Java_com_moutaigua8183_isl_android_gaze_MainActivity_getMsg(JNIEnv* env, jobject obj) {
    return (*env)->NewStringUTF(env, "Hello World");
}