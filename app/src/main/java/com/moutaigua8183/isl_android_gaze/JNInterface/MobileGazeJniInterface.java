package com.moutaigua8183.isl_android_gaze.JNInterface;

/**
 * Created by Mou on 11/3/2017.
 */

public class MobileGazeJniInterface {

    static {
        System.loadLibrary("MobileGazeJNI");
    }


    private final String LOG_TAG = "MobileGazeJniInterface";

    public MobileGazeJniInterface(){

    }



    public native byte[] getMsg(byte[] content);

    public native int[] getRotatedRGBImage(byte[] yBytes, byte[] uBytes, byte[] vBytes, int origWidth, int origHeight);

    public native int getAdditionRes(int a, int b);

    public native String getWelcomeString();




}
