package com.moutaigua8183.isl_android_gaze.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.moutaigua8183.isl_android_gaze.Controllers.CameraHandler;
import com.moutaigua8183.isl_android_gaze.Controllers.DotController;
import com.moutaigua8183.isl_android_gaze.Controllers.ImageFileHandler;
import com.moutaigua8183.isl_android_gaze.R;

/**
 * Created by mou on 9/16/17.
 */

public class DataCollectionActivity extends AppCompatActivity {


    private final String LOG_TAG = "DataCollectionActivity";
    private boolean doubleBackToExitPressedOnce = false;
    private View dotHolderLayout;
    private CameraHandler cameraHandler;
    private DotController dotController;
    private int[] SCREEN_SIZE;
    private boolean isPicSaved = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);
        getSupportActionBar().hide();

        dotHolderLayout = findViewById(R.id.activity_data_collection_layout_dotHolder);
        dotHolderLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if( !isPicSaved ){
                    return true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    Log.d(LOG_TAG, "pressed");
                    boolean is_click_on_left = motionEvent.getRawX() <= (SCREEN_SIZE[0]/2);
                    if( is_click_on_left && dotController.getCurrPointType()!=DotController.POINT_TYPE_LEFT
                            || !is_click_on_left && dotController.getCurrPointType()!=DotController.POINT_TYPE_RIGHT ) {
                        // invalid picture
                        cameraHandler.deleteLastPicture();
                        Log.d(LOG_TAG, "Invalid Picture");
                    }
                    dotController.showNext();
                    delayCapture(400);
                    isPicSaved = false;     // reset the status
                }
                return true;
            }
        });

        cameraHandler = CameraHandler.getInstance(this);
        cameraHandler.openFrontCamera();
        cameraHandler.setSavingCallback(new ImageFileHandler.SavingCallback() {
            @Override
            public void onSaved() {
                isPicSaved = true;
            }
        });

        SCREEN_SIZE = fetchScreenSize();
        dotController = new DotController(this, SCREEN_SIZE);
        dotController.setDotHolderLayout((FrameLayout)dotHolderLayout);
        dotController.showNext();
        delayCapture(400);

    }


    /**
     * When the collection encounter interference,
     * delete the last picture and exit the DataCollection
     * activity to guarantee the quality of training data
     */
    @Override
    protected void onPause() {
        super.onPause();
        if( isPicSaved ){
            cameraHandler.deleteLastPicture();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraHandler.deleteLastPicture();
        cameraHandler.closeFrontCamera();
    }

    /**
     * Press Back twice to exit unless a picture is being
     * saved. If so, try again after the picture is saved
     */
    @Override
    public void onBackPressed() {
        if( !isPicSaved ){
            return;
        }
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press Back again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 1500);
    }


    private int[] fetchScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new int[]{displayMetrics.heightPixels, displayMetrics.widthPixels};
    }

    private void delayCapture(int delayLength){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraHandler.takePicture(dotController.getCurrPoint());
            }
        }, delayLength);
    }


}
