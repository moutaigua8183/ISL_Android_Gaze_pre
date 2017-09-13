package com.moutaigua8183.isl_android_gaze.Controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.support.v4.content.pm.ActivityInfoCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mou on 9/12/2017.
 */

public class CameraHandler {

    private final String LOG_TAG = "Camera_Handler";

    private static CameraHandler myInstance;
    private Context ctxt;
    private CameraManager cameraManager;
    private CameraDevice frontCamera;
    private StreamConfigurationMap frontCameraStreamConfigurationMap;
    private CameraDevice.StateCallback stateCallback;

    // private constructor
    private CameraHandler(Context context) {
        this.ctxt = context;
        cameraManager = (CameraManager) ctxt.getSystemService(Context.CAMERA_SERVICE);
        frontCamera = null;
        frontCameraStreamConfigurationMap = null;
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
                    initFrontCameraStreamConfigurationMap();
                } else {
                    Log.d(LOG_TAG, "Can\'t open camera because of no permission");
                    Toast.makeText(this.ctxt, "Can't open camera because of no permission", Toast.LENGTH_SHORT);
                }
            } else {
                if ( PermissionChecker.checkCallingOrSelfPermission(this.ctxt, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED
                        && PermissionChecker.checkCallingOrSelfPermission(this.ctxt, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    this.cameraManager.openCamera(frontCameraId, this.stateCallback, null);
                    initFrontCameraStreamConfigurationMap();
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

    public void takePicture(@NonNull ImageReader imageReader, int imageFormat){
        if ( null==frontCamera) {
            Log.e(LOG_TAG, "No front camera");
            return;
        }
        try {
            Size[] imageSize = frontCameraStreamConfigurationMap.getOutputSizes(imageFormat);
            final boolean jpegSizesNotEmpty = imageSize != null && 0 < imageSize.length;
            int width = jpegSizesNotEmpty ? imageSize[0].getWidth() : 640;
            int height = jpegSizesNotEmpty ? imageSize[0].getHeight() : 480;
            //final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            final List<Surface> outputSurfaces = new ArrayList<>();
            outputSurfaces.add(imageReader.getSurface());
            final CaptureRequest.Builder captureBuilder = frontCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_HDR);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation());
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
            frontCamera.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                session.capture(captureBuilder.build(), captureListener, null);
                            } catch (CameraAccessException e) {
                                Log.e(LOG_TAG, " exception occurred while accessing " + frontCamera.getId(), e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        }
                    }
                    , null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public Size[] getFrontCameraPictureSize(int format){
        return frontCameraStreamConfigurationMap.getOutputSizes(format);
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

    private void initFrontCameraStreamConfigurationMap(){
        try {
            CameraCharacteristics characteristics = this.cameraManager.getCameraCharacteristics(frontCamera.getId());
            frontCameraStreamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        } catch (CameraAccessException e) {
            Log.d(LOG_TAG, "Fail to get front camera StreamConfigurationMap");
            e.printStackTrace();
        }
    }

    private int getOrientation(){
        int rotation = ((Activity)this.ctxt).getWindowManager().getDefaultDisplay().getRotation();
        SparseIntArray ORIENTATIONS = new SparseIntArray();
        ORIENTATIONS.append(Surface.ROTATION_0, 270);
        ORIENTATIONS.append(Surface.ROTATION_90, 180);
        ORIENTATIONS.append(Surface.ROTATION_180, 90);
        ORIENTATIONS.append(Surface.ROTATION_270, 0);
        return ORIENTATIONS.get(rotation);
    }




}
