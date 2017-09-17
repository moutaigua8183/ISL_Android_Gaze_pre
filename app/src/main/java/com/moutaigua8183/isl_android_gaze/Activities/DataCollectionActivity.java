package com.moutaigua8183.isl_android_gaze.Activities;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.moutaigua8183.isl_android_gaze.Controllers.CameraHandler;
import com.moutaigua8183.isl_android_gaze.Controllers.DotController;
import com.moutaigua8183.isl_android_gaze.Controllers.ImageFileHandler;
import com.moutaigua8183.isl_android_gaze.R;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    private boolean isPicSaved;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);
        getSupportActionBar().hide();

        isPicSaved = false;

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
                        cameraHandler.deteleLastPicture();
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



//        Button  btn = (Button) findViewById(R.id.button);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(LOG_TAG, "pressed");
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyMMddhhmmss");
//                String timestamp = sdf.format(new Date());
//                Point curPoint = dotController.getCurrPoint();
//                String picName = timestamp + "_" + curPoint.x + "_" + curPoint.y;
//                cameraHandler.takePicture(picName);
//                Log.d(LOG_TAG, picName);
//            }
//        });

    }







    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraHandler.closeFrontCamera();
    }


    /**
     * Press Back twice to exit
     */
    @Override
    public void onBackPressed() {
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
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
                String timestamp = sdf.format(new Date());
                Point curPoint = dotController.getCurrPoint();
                String picName = timestamp + "_" + curPoint.x + "_" + curPoint.y;
                cameraHandler.takePicture(picName);
                Log.d(LOG_TAG, picName);
            }
        }, delayLength);
    }

}
