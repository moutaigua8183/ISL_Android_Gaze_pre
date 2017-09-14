package com.moutaigua8183.isl_android_gaze;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Button;

import com.moutaigua8183.isl_android_gaze.Controllers.CameraHandler;
import com.moutaigua8183.isl_android_gaze.Controllers.ImageFileHandler;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_ACCESS_CODE = 1;

    CameraHandler cameraHandler;
    ImageFileHandler imageFileHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        checkPermissions();


        cameraHandler = CameraHandler.getInstance(this);
        cameraHandler.openFrontCamera();



        imageFileHandler = new ImageFileHandler();
        imageFileHandler.setImageFormat(ImageFormat.JPEG);
        Size[] imageSize = cameraHandler.getFrontCameraPictureSize(ImageFormat.JPEG);
        imageFileHandler.setImageWidth( null!=imageSize ? imageSize[0].getWidth() : 640 );
        imageFileHandler.setImageHeight( null!=imageSize ? imageSize[0].getHeight() : 480 );
        imageFileHandler.instantiateImageReader();


        Button  btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHandler.takePicture( imageFileHandler.getImageReader() );
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraHandler.closeFrontCamera();
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_CODE: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkPermissions();
                }
            }
        }
    }

    /**
     * checking  permissions at Runtime.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        final String[] requiredPermissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
        };
        final List<String> neededPermissions = new ArrayList<>();
        for (final String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }
        if (!neededPermissions.isEmpty()) {
            requestPermissions(neededPermissions.toArray(new String[]{}),
                    MY_PERMISSIONS_REQUEST_ACCESS_CODE);
        }
    }



}
