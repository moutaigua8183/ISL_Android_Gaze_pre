package com.moutaigua8183.isl_android_gaze.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.moutaigua8183.isl_android_gaze.Handlers.ImageFileHandler;
import com.moutaigua8183.isl_android_gaze.Activities.DataCollectionActivity;
import com.moutaigua8183.isl_android_gaze.Handlers.CameraHandler;
import com.moutaigua8183.isl_android_gaze.Handlers.DotHandler;
import com.moutaigua8183.isl_android_gaze.R;

/**
 * Created by mou on 9/16/17.
 */


public class FragmentDataCollection extends Fragment {


    private final String LOG_TAG = "FragmentDataCollection";
    private View dotHolderLayout;
    private CameraHandler cameraHandler;
    private DotHandler  dotHandler;
    private int[] SCREEN_SIZE;
    private int firstSeveralDots = 0;
    private boolean isPicSaved = false;




    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_collection, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dotHolderLayout = getActivity().findViewById(R.id.fragment_data_collection_layout_dotHolder);
        dotHolderLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // first press is to start
                if(firstSeveralDots ==0){
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dotHandler.showNext();
                            delayCapture(400);
                        }
                    }, 500);
                    firstSeveralDots++;
                    return false;
                }
                if( !isPicSaved ){
                    return false;
                }
                // first 2 pictures will be deleted
                if( firstSeveralDots ==1 || firstSeveralDots ==2 ){
                    Log.d(LOG_TAG, String.valueOf(firstSeveralDots));
                    cameraHandler.deleteLastPicture();
                    firstSeveralDots++;
                    dotHandler.showNext();
                    delayCapture(400);
                    isPicSaved = false;
                }
                // for the future dots
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    Log.d(LOG_TAG, "pressed");
                    boolean is_click_on_left = motionEvent.getRawX() <= (SCREEN_SIZE[0]/2);
                    if( is_click_on_left && dotHandler.getCurrDotType()!= DotHandler.POINT_TYPE_LEFT
                            || !is_click_on_left && dotHandler.getCurrDotType()!= DotHandler.POINT_TYPE_RIGHT ) {
                        // invalid picture
                        cameraHandler.deleteLastPicture();
                        Log.d(LOG_TAG, "Invalid Picture");
                    }
                    dotHandler.showNext();
                    delayCapture(400);
                    isPicSaved = false;     // reset the status
                }
                return true;
            }
        });
        Toast.makeText(getActivity(), "Click anywhere to start\nFirst 2 dots don't count", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraHandler = CameraHandler.getInstance(getActivity(), false);
        cameraHandler.setImageSize(DataCollectionActivity.Image_Size);
        cameraHandler.setSavingCallback(new ImageFileHandler.SavingCallback() {
            @Override
            public void onSaved() {
                isPicSaved = true;
            }
        });
        cameraHandler.openFrontCameraForDataCollection();
        SCREEN_SIZE = fetchScreenSize();
        Log.d(LOG_TAG, "Width: " + SCREEN_SIZE[0] + "    Height: " + SCREEN_SIZE[1]);
        dotHandler = new DotHandler(getActivity(), SCREEN_SIZE);
        dotHandler.setDotHolderLayout((FrameLayout)dotHolderLayout);
    }


    @Override
    public void onPause(){
        super.onPause();
        if( isPicSaved ){
            cameraHandler.deleteLastPicture();
            firstSeveralDots = 0;
        }
        cameraHandler.closeFrontCameraForDataCollection();
    }


    public boolean isPicSaved(){
        return  isPicSaved;
    }

    private void delayCapture(int delayLength){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraHandler.takePicture(dotHandler.getCurrDot());
            }
        }, delayLength);
    }

    private int[] fetchScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }


}
