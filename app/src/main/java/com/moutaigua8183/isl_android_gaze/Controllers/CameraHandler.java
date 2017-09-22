package com.moutaigua8183.isl_android_gaze.Controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.moutaigua8183.isl_android_gaze.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mou on 9/12/2017.
 */

public class CameraHandler {

    public final int STATUS_NONE = -1;
    public final int STATUS_PREVIEW = 1;
    public final int STATUS_DATA_COLLECTING = 2;
    private final String LOG_TAG = "Camera_Handler";
    private final int IMAGE_FORMAT = ImageFormat.JPEG;      //if changed, modify capureBuilder.set(Orientation, ) accordingly


    private static CameraHandler myInstance;
    private Context         ctxt;
    private CameraDevice    frontCamera;
    private Size            IMAGE_SIZE;
    private int             status;
    // background capture
    private CameraManager cameraManager;
    private ImageFileHandler imageFileHandler;
    private StreamConfigurationMap frontCameraStreamConfigurationMap;
    private CameraDevice.StateCallback stateCallback;
    private CaptureRequest.Builder  captureBuilder;


    // private constructor
    private CameraHandler(Context context, Size size) {
        this.ctxt = context;
        IMAGE_SIZE =size;
        cameraManager = (CameraManager) ctxt.getSystemService(Context.CAMERA_SERVICE);
        imageFileHandler = new ImageFileHandler();
        frontCamera = null;
        frontCameraStreamConfigurationMap = null;
        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                //when camera is open, initilize imageFileHandler for saving the pic
                Log.d(LOG_TAG, "Camera " + camera.getId() + " is opened");
                status = STATUS_DATA_COLLECTING;
                frontCamera = camera;
                initFrontCameraStreamConfigurationMap();
                imageFileHandler.setImageFormat(IMAGE_FORMAT);
                imageFileHandler.setImageSize(IMAGE_SIZE);
                imageFileHandler.instantiateImageReader();
            }

            @Override
            public void onClosed(@NonNull CameraDevice camera) {
                Log.d(LOG_TAG, "Camera " + camera.getId() + " is closed");
                frontCamera = null;
                frontCameraStreamConfigurationMap = null;
                status = STATUS_NONE;
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.d(LOG_TAG, "Camera " + camera.getId() + " is disconnected");
                camera.close();
                frontCamera = null;
                frontCameraStreamConfigurationMap = null;
                status = STATUS_NONE;
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                Log.d(LOG_TAG, "Camera " + camera.getId() + " can\'t be opened with the error number " + error);
                frontCamera = null;
                frontCameraStreamConfigurationMap = null;
                status = STATUS_NONE;
            }
        };
        status = STATUS_NONE;
    }

    // to create a CameraHandler Singleton
    public static synchronized CameraHandler getInstance(Context context, Size size) {
        if (null == myInstance) {
            myInstance = new CameraHandler(context, size);
        }
        return myInstance;
    }


    public void openFrontCamera() {
        Log.d(LOG_TAG, "Try to open local camera");
        try {
            String frontCameraId = getFrontCameraId();
            if (Build.VERSION.SDK_INT > 22) {
                if (this.ctxt.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && this.ctxt.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    this.cameraManager.openCamera(frontCameraId, this.stateCallback, new Handler());
                } else {
                    Log.d(LOG_TAG, "Can\'t open camera because of no permission");
                    Toast.makeText(this.ctxt, "Can't open camera because of no permission", Toast.LENGTH_SHORT);
                }
            } else {
                if (PermissionChecker.checkCallingOrSelfPermission(this.ctxt, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
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

    public void closeFrontCamera() {
        Log.d(LOG_TAG, "Try to close front camera");
        if (null != frontCamera) {
            frontCamera.close();
            Log.d(LOG_TAG, "Camera " + frontCamera.getId() + " is closed");
            frontCamera = null;
        }
    }

    public void takePicture(Point point) {
        if (null == frontCamera) {
            Log.d(LOG_TAG, "No front camera");
            return;
        }
        if (null == imageFileHandler.getImageReader()) {
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
            captureBuilder = frontCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageFileHandler.getImageReader().getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            captureBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_60HZ);
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

    public void setSavingCallback(ImageFileHandler.SavingCallback savingCallback) {
        imageFileHandler.setSavingCallback(savingCallback);
    }

    public void deleteLastPicture() {
        this.imageFileHandler.deleteLastImage();
    }


    private String getFrontCameraId() {
        String frontCameraId = "unknown";
        try {
            for (final String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                int cameraOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "Front camera ID: " + frontCameraId);
        return frontCameraId;
    }

    private void initFrontCameraStreamConfigurationMap() {
        try {
            CameraCharacteristics characteristics = this.cameraManager.getCameraCharacteristics(frontCamera.getId());
            frontCameraStreamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        } catch (CameraAccessException e) {
            Log.d(LOG_TAG, "Fail to get front camera StreamConfigurationMap");
            e.printStackTrace();
        }
    }

    private Size[] getFrontCameraPictureSize(int format) {
        return frontCameraStreamConfigurationMap.getOutputSizes(format);
    }

    private int getOrientation() {
        int rotation = ((Activity) this.ctxt).getWindowManager().getDefaultDisplay().getRotation();
        SparseIntArray ORIENTATIONS = new SparseIntArray();
        ORIENTATIONS.append(Surface.ROTATION_0, 270);
        ORIENTATIONS.append(Surface.ROTATION_90, 180);
        ORIENTATIONS.append(Surface.ROTATION_180, 90);
        ORIENTATIONS.append(Surface.ROTATION_270, 0);
        return ORIENTATIONS.get(rotation);
    }


    /******************* Preview ********************/

    private CameraManager   cameraManagerForPrev;
    private TextureView     textureViewForPrev;
    private SurfaceTexture  surfaceTextureForPrev;
    private Size            imageSizeForPrev;
    private Handler         handlerForPrev;
    private CameraDevice    frontCameraForPrev;
    private Semaphore       cameraOpenCloseLockForPrev = new Semaphore(1);
    private CaptureRequest.Builder  captureBuilderForPrev;

    public void startPreview(TextureView textureView) {
        cameraManagerForPrev = (CameraManager) ((Activity) ctxt).getSystemService(Context.CAMERA_SERVICE);
        textureViewForPrev = textureView;
        textureViewForPrev.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                initCameraAndPreview(IMAGE_SIZE.getWidth(), IMAGE_SIZE.getHeight());
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    public void stopPreview(){
        Log.d(LOG_TAG, "Try to close front camera for preview");
        try {
            cameraOpenCloseLockForPrev.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            cameraOpenCloseLockForPrev.release();
        }
        if (null != frontCameraForPrev) {
            frontCameraForPrev.close();
            Log.d(LOG_TAG, "Camera " + frontCameraForPrev.getId() + " is closed");
            frontCamera = null;
        }
    }

    private void initCameraAndPreview(int width, int height) {
        Log.d(LOG_TAG, "init camera and preview");
        HandlerThread handlerThread = new HandlerThread("Gaze_DataCollection_Preview");
        handlerThread.start();
        handlerForPrev = new Handler(handlerThread.getLooper());
        try {
            String frontCameraId = getFrontCameraId();
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(frontCameraId);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            imageSizeForPrev = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), width, height);
            if (ActivityCompat.checkSelfPermission((Activity)ctxt, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG_TAG, "No permission for local camera");
                ((Activity) ctxt).finish();
                return;
            }
            if( !cameraOpenCloseLockForPrev.tryAcquire(2500, TimeUnit.MICROSECONDS) ){
                throw new RuntimeException("Time out waiting to lock opening");
            }
            cameraManagerForPrev.openCamera(frontCameraId, new CameraDevice.StateCallback() {

                @Override
                public void onOpened(CameraDevice camera) {
                    Log.d(LOG_TAG,"Front camera is opened.");
                    cameraOpenCloseLockForPrev.release();
                    frontCameraForPrev = camera;
                    createCameraCaptureSession();
                    status = STATUS_PREVIEW;
                }

                @Override
                public void onClosed(@NonNull CameraDevice camera) {
                    super.onClosed(camera);
                    frontCameraForPrev = null;
                    status = STATUS_NONE;
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    status = STATUS_NONE;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    frontCameraForPrev = camera;
                    status = STATUS_NONE;
                }
            }, handlerForPrev);
        } catch (CameraAccessException e) {
            Log.e(LOG_TAG, "open camera failed." + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createCameraCaptureSession(){
        Log.d(LOG_TAG,"createCameraCaptureSession");
        try {
            surfaceTextureForPrev = textureViewForPrev.getSurfaceTexture();
            surfaceTextureForPrev.setDefaultBufferSize(imageSizeForPrev.getWidth(), imageSizeForPrev.getHeight());
            Surface outputSurface = new Surface(surfaceTextureForPrev);
            captureBuilderForPrev = frontCameraForPrev.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureBuilderForPrev.addTarget(outputSurface);
            frontCameraForPrev.createCaptureSession(
                    Arrays.asList(outputSurface),
                    new CameraCaptureSession.StateCallback() {
                                @Override
                                public void onConfigured(CameraCaptureSession session) {
                                    try {
                                        captureBuilderForPrev.set(CaptureRequest.CONTROL_AF_MODE,
                                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                        captureBuilderForPrev.set(CaptureRequest.CONTROL_AE_MODE,
                                                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                        session.setRepeatingRequest(captureBuilderForPrev.build(), sessionCaptureCallbackForPrev, handlerForPrev);
                                        captureBuilderForPrev.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_FACE_PRIORITY);
                                        captureBuilderForPrev.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE);
                                        captureBuilderForPrev.set(CaptureRequest.JPEG_ORIENTATION, getOrientation());
                                    } catch (CameraAccessException e) {
                                        e.printStackTrace();
                                        Log.e("linc","set preview builder failed."+e.getMessage());
                                    }
                                }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, handlerForPrev);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback sessionCaptureCallbackForPrev =
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, CaptureRequest request,
                                               TotalCaptureResult result) {
                }
            };

    private Size getPreferredPreviewSize(Size[] mapSizes, int width, int height) {
        double EPSL = 0.00001;
        List<Size> collectorSizes = new ArrayList<>();
        // looking for the exact size or the onse with the exact ratio;
        double preferredRatio = (double) width / height;
        for(Size option : mapSizes) {
            if( width==option.getWidth() && height==option.getHeight() ){
                return option;
            }
            double curRatio = (double)option.getWidth()/option.getHeight();
            if( Math.abs(preferredRatio-curRatio) < EPSL) {
                collectorSizes.add(option);
            }
        }
        if( collectorSizes.size()==0 ){ // if no size with the exact ratio
            double minRatioDiff = 1000;
            Size bestOption = null;
            for(Size option : mapSizes) {
                double curRatio = (double)option.getWidth()/option.getHeight();
                if( Math.abs(curRatio-preferredRatio) < minRatioDiff ){
                    bestOption = option;
                }
            }
            return bestOption;
        }
        return Collections.min(collectorSizes, new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
            }
        });
    }



}
