package com.moutaigua8183.isl_android_gaze.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.moutaigua8183.isl_android_gaze.Activities.DataCollectionActivity;
import com.moutaigua8183.isl_android_gaze.Controllers.CameraHandler;
import com.moutaigua8183.isl_android_gaze.Controllers.DotController;
import com.moutaigua8183.isl_android_gaze.Controllers.ImageFileHandler;
import com.moutaigua8183.isl_android_gaze.R;

/**
 * Created by mou on 9/16/17.
 */


public class FragmentDataCollect extends Fragment {


    private final String LOG_TAG = "FragmentDataCollect";
    private View dotHolderLayout;
    private CameraHandler cameraHandler;
    private DotController dotController;
    private int[] SCREEN_SIZE;
    private boolean isPicSaved = false;




    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dot_container, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dotHolderLayout = getActivity().findViewById(R.id.fragment_dot_container_layout_dotHolder);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraHandler = CameraHandler.getInstance(getActivity(), DataCollectionActivity.Image_Size);
        cameraHandler.setSavingCallback(new ImageFileHandler.SavingCallback() {
            @Override
            public void onSaved() {
                isPicSaved = true;
            }
        });
        cameraHandler.openFrontCamera();
        SCREEN_SIZE = fetchScreenSize();
        Log.d(LOG_TAG, "Width: " + SCREEN_SIZE[0] + "    Height: " + SCREEN_SIZE[1]);
        dotController = new DotController(getActivity(), SCREEN_SIZE);
        dotController.setDotHolderLayout((FrameLayout)dotHolderLayout);
        dotController.showNext();
        delayCapture(450);      //250ms for human eye reaction
    }


    @Override
    public void onPause(){
        super.onPause();
        if( isPicSaved ){
            cameraHandler.deleteLastPicture();
        }
        cameraHandler.closeFrontCamera();
    }


    public boolean isPicSaved(){
        return  isPicSaved;
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

    private int[] fetchScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new int[]{displayMetrics.heightPixels, displayMetrics.widthPixels};
    }


}
