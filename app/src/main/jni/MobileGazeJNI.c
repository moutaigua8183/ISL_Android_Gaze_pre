#include <jni.h>
#include <stdio.h>
#include "mou_jni_test.h"

JNIEXPORT jbyteArray JNICALL
Java_com_iai_mdf_JNInterface_MobileGazeJniInterface_getMsg(JNIEnv *env,
                                                                                 jobject instance,
                                                                                 jbyteArray content_) {
    jbyteArray *contentPtr = (*env)->GetByteArrayElements(env, content_, NULL);
    jsize length = (*env)->GetArrayLength(env, content_);
    // TODO

    (*env)->ReleaseByteArrayElements(env, content_, contentPtr, 0);
    return content_;
}

JNIEXPORT jintArray JNICALL
Java_com_iai_mdf_JNInterface_MobileGazeJniInterface_getRotatedRGBImage(
        JNIEnv *env, jobject instance, jbyteArray yBytes_, jbyteArray uBytes_, jbyteArray vBytes_, jint origWidth_, jint origHeight_) {
    int i = 0;
    int width = origWidth_;
    int height = origHeight_;
    int uvWidth = width / 2;
    int origX;
    int origY;
    int newI;
    int uvRow;
    int uvCol;
    jbyte *yBytes = (*env)->GetByteArrayElements(env, yBytes_, NULL);
    jbyte *uBytes = (*env)->GetByteArrayElements(env, uBytes_, NULL);
    jbyte *vBytes = (*env)->GetByteArrayElements(env, vBytes_, NULL);
    jsize yLength = (*env)->GetArrayLength(env, yBytes_);
    jsize uvLength = (*env)->GetArrayLength(env, uBytes_);

    // upsampling the U and V array
//    jbyteArray newUByteArray = (*env)->NewByteArray(env, yLength);
//    jbyte *newUBytes = (*env)->GetByteArrayElements(env, newUByteArray, NULL);
//    jbyteArray newVByteArray = (*env)->NewByteArray(env, yLength);
//    jbyte *newVBytes = (*env)->GetByteArrayElements(env, newVByteArray, NULL);
//    for(i=0; i < uvLength-1; i++){
//        int uvWidth = width / 2;
//        int uvCol = i % uvWidth;
//        int uvRow = i / uvWidth;
//        newUBytes[2*i] = *(uBytes + i);
//        newUBytes[2*i+1] = (*(uBytes + i) + *(uBytes + i + 1))/2;
//        newVBytes[2*i] = *(vBytes + i);
//        newVBytes[2*i+1] = (*(vBytes + i) + *(vBytes + i + 1))/2;
//    }
//    newUBytes[yLength-2] = *(uBytes + uvLength -1);
//    newUBytes[yLength-1] = *(uBytes + uvLength -1);
//    newVBytes[yLength-2] = *(vBytes + uvLength -1);
//    newVBytes[yLength-1] = *(vBytes + uvLength -1);

//    // YUV -> RGB  + rotation     with the size of U & V planes
//    jintArray rgbIntArray = (*env)->NewIntArray(env, uvLength);
//    jint *rgbInt = (*env)->GetIntArrayElements(env, rgbIntArray, NULL);
//    for(i = 0; i < uvLength; ++i) {
//        uvCol = (i % uvWidth) * 2;
//        uvRow = (i / uvWidth) * 2;
//        jint R = (char)*(yBytes + uvRow*width + uvCol) + 1.40200 * ((char)*(vBytes+i)-128);
//        jint G = (char)*(yBytes + uvRow*width + uvCol) - 0.34414 * ((char)*(uBytes+i)-128) - 0.71414 * ((char)*(vBytes+i)-128);
//        jint B = (char)*(yBytes + uvRow*width + uvCol) + 1.77200 * ((char)*(uBytes+i)-128);
//        R = (R > 255)? 255 : (R < 0)? 0 : R;
//        G = (G > 255)? 255 : (G < 0)? 0 : G;
//        B = (B > 255)? 255 : (B < 0)? 0 : B;
//        jint RGB = 0xff000000 | (R << 16) | (G << 8) | B;
//        origX = i % (width/2);
//        origY = i / (width/2);
//        newI = (height/2) * ((width/2) - 1 - origX) + origY;
//        rgbInt[newI] = RGB;
//    }
    // YUV -> RGB  + rotation      with the size of Y plane
    jintArray rgbIntArray = (*env)->NewIntArray(env, yLength);
    jint *rgbInt = (*env)->GetIntArrayElements(env, rgbIntArray, NULL);
    for(i = 0; i < yLength; ++i) {
        uvCol = (i % width) / 2;
        uvRow = i / width / 2;
        jint R = (char)*(yBytes + i) + 1.40200 * ((char)*(vBytes+uvRow*uvWidth+uvCol)-128);
        jint G = (char)*(yBytes + i) - 0.34414 * ((char)*(uBytes+uvRow*uvWidth+uvCol)-128) - 0.71414 * ((char)*(vBytes+uvRow*uvWidth+uvCol)-128);
        jint B = (char)*(yBytes + i) + 1.77200 * ((char)*(uBytes+uvRow*uvWidth+uvCol)-128);
        R = (R > 255)? 255 : (R < 0)? 0 : R;
        G = (G > 255)? 255 : (G < 0)? 0 : G;
        B = (B > 255)? 255 : (B < 0)? 0 : B;
        jint RGB = 0xff000000 | (R << 16) | (G << 8) | B;
        origX = i % width;
        origY = i / width;
        newI = height * (width - 1 - origX) + origY;
        rgbInt[newI] = RGB;
    }

    (*env)->ReleaseByteArrayElements(env, yBytes_, yBytes, 0);
    (*env)->ReleaseByteArrayElements(env, uBytes_, uBytes, 0);
    (*env)->ReleaseByteArrayElements(env, vBytes_, vBytes, 0);
//    (*env)->ReleaseByteArrayElements(env, newUByteArray, newUBytes, 0);
//    (*env)->ReleaseByteArrayElements(env, newVByteArray, newVBytes, 0);
    (*env)->ReleaseIntArrayElements(env, rgbIntArray, rgbInt, 0);
    return rgbIntArray;
}

JNIEXPORT jint JNICALL
Java_com_iai_mdf_JNInterface_MobileGazeJniInterface_getAdditionRes(
        JNIEnv *env, jobject instance, jint a, jint b) {

    return add(a, b);

}

JNIEXPORT jstring JNICALL
Java_com_iai_mdf_JNInterface_MobileGazeJniInterface_getWelcomeString(
        JNIEnv *env, jobject instance) {

    jstring res = welcome();


    return (*env)->NewStringUTF(env, res);
}