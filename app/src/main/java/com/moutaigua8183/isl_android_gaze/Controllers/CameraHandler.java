package com.moutaigua8183.isl_android_gaze.Controllers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Mou on 9/12/2017.
 */

public class CameraHandler {

    private final String LOG_TAG = "Camera_Handler";

    private static CameraHandler myInstance;
    private Context ctxt;
    private CameraManager cameraManager;
    private CameraDevice frontCamera;
    private String frontCameraId;
    private CameraDevice.StateCallback stateCallback;

    // private constructor
    private CameraHandler(Context context) {
        this.ctxt = context;
        cameraManager = (CameraManager) ctxt.getSystemService(Context.CAMERA_SERVICE);
        frontCamera = null;
        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.d(LOG_TAG, "Camera " + camera.getId() + " is opened");
                frontCamera = camera;
            }

            @Override
            public void onClosed(@NonNull CameraDevice camera) {
                Log.d(LOG_TAG, "Camera " + camera.getId() + " is closed");
                frontCamera = null;
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.d(LOG_TAG, "Camera " + camera.getId() + " is disconnected");
                camera.close();
                frontCamera = null;
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                Log.d(LOG_TAG, "Camera " + camera.getId() + " can\'t be opened with the error number " + error);
                camera = null;
            }
        };
    }

    // to create a CameraHandler Singleton
    public static synchronized CameraHandler getInstance(Context context){
        if( null==myInstance ){
            myInstance = new CameraHandler(context);
        }
        return myInstance;
    }



    public void openFrontCamera(){
        Log.d(LOG_TAG, "Try to open local camera");
        try {
            String frontCameraId = getFrontCameraId();
            if(Build.VERSION.SDK_INT > 22) {
                if (this.ctxt.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && this.ctxt.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    this.cameraManager.openCamera(frontCameraId, this.stateCallback, null);
                } else {
                    Log.d(LOG_TAG, "Can\'t open camera because of no permission");
                    Toast.makeText(this.ctxt, "Can't open camera because of no permission", Toast.LENGTH_SHORT);
                }
            } else {
                if ( PermissionChecker.checkCallingOrSelfPermission(this.ctxt, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED
                        && PermissionChecker.checkCallingOrSelfPermission(this.ctxt, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    this.cameraManager.openCamera(frontCameraId, this.stateCallback, null);
                } else {
                    Log.d(LOG_TAG, "Can\'t open camera because of no permission");
                    Toast.makeText(this.ctxt, "Can't open camera because of no permission", Toast.LENGTH_SHORT);
                }
            }
        } catch (final CameraAccessException e) {
            Log.e(LOG_TAG, "exception occurred while opening camera with errors: ", e);
        }
    }

    public void closeFrontCamera(){
        Log.d(LOG_TAG, "Try to close front camera");
        if( null!=frontCamera ){
            frontCamera.close();
            Log.d(LOG_TAG, "Camera " + frontCamera.getId() + " is closed");
            frontCamera = null;
        }
    }

    public void takePicture(){

    }


    private String getFrontCameraId(){
        String frontCameraId = "unknown";
        try {
            for(final String cameraId : cameraManager.getCameraIdList() ){
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                int cameraOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if( cameraOrientation==CameraCharacteristics.LENS_FACING_FRONT ){
                    frontCameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d( LOG_TAG, "Front camera ID: " + frontCameraId );
        return frontCameraId;
    }




}
