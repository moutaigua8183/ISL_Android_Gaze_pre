package com.moutaigua8183.isl_android_gaze.JNInterface;

/**
 * Created by Mou on 11/3/2017.
 */

public class MobileGazeInterface {

    static {
        System.loadLibrary("MobileGazeJNI");
    }


    private final String LOG_TAG = "MobileGazeInterface";

    public MobileGazeInterface(){

    }


    public native byte[] getMsg(byte[] content);


}
