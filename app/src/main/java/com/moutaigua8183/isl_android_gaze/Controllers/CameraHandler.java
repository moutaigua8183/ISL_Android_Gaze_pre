package com.moutaigua8183.isl_android_gaze.Controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Mou on 9/12/2017.
 */

public class CameraHandler {

    private final String LOG_TAG = "Camera_Handler";
    private final int IMAGE_FORMAT = ImageFormat.JPEG;      //if changed, modify capureBuilder.set(Orientation, ) accordingly

    private static CameraHandler myInstance;
    private Context ctxt;
    // background capture
    private CameraManager cameraManager;
    private ImageFileHandler imageFileHandler;
    private CameraDevice frontCamera;
    private StreamConfigurationMap frontCameraStreamConfigurationMap;
    private CameraDevice.StateCallback stateCallback;
    // preview
    private Camera camera;
    private boolean isPreview = false;
    private boolean isConfigured = false;


    // private constructor
    private CameraHandler(Context context) {
        this.ctxt = context;
        cameraManager = (CameraManager) ctxt.getSystemService(Context.CAMERA_SERVICE);
        imageFileHandler = new ImageFileHandler();
        frontCamera = null;
        frontCameraStreamConfigurationMap = null;
        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                //when camera is open, initilize imageFileHandler for saving the pic
                Log.d(LOG_TAG, "Camera " + camera.getId() + " is opened");
                frontCamera = camera;
                initFrontCameraStreamConfigurationMap();
                imageFileHandler.setImageFormat(IMAGE_FORMAT);
                imageFileHandler.setImageSize( getFrontCameraPictureSize(IMAGE_FORMAT) );
                imageFileHandler.instantiateImageReader();
            }

            @Override
            public void onClosed(@NonNull CameraDevice camera) {
                Log.d(LOG_TAG, "Camera " + camera.getId() + " is closed");
                frontCamera = null;
                frontCameraStreamConfigurationMap = null;
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.d(LOG_TAG, "Camera " + camera.getId() + " is disconnected");
                camera.close();
                frontCamera = null;
                frontCameraStreamConfigurationMap = null;
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                Log.d(LOG_TAG, "Camera " + camera.getId() + " can\'t be opened with the error number " + error);
                frontCamera = null;
                frontCameraStreamConfigurationMap = null;
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
                    this.cameraManager.openCamera(frontCameraId, this.stateCallback, new Handler());
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

    public void takePicture(Point point){
        if ( null==frontCamera) {
            Log.d(LOG_TAG, "No front camera");
            return;
        }
        if ( null==imageFileHandler.getImageReader() ) {
            Log.d(LOG_TAG, "ImageReader is not ready, can\'t take picture.");
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String picName = timestamp + "_" + point.x + "_" + point.y;
        Log.d(LOG_TAG, picName);
        imageFileHandler.setImageName(picName);
        try {
            final List<Surface> outputSurfaces = new ArrayList<>();
            outputSurfaces.add(imageFileHandler.getImageReader().getSurface());
            final CaptureRequest.Builder captureBuilder = frontCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageFileHandler.getImageReader().getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_HDR);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation());
            frontCamera.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                session.capture(captureBuilder.build(), null, null);
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

    public void setSavingCallback(ImageFileHandler.SavingCallback savingCallback){
        imageFileHandler.setSavingCallback(savingCallback);
    }

    public void deteleLastPicture(){
        this.imageFileHandler.deleteLastImage();
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

    private Size[] getFrontCameraPictureSize(int format){
        return frontCameraStreamConfigurationMap.getOutputSizes(format);
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
