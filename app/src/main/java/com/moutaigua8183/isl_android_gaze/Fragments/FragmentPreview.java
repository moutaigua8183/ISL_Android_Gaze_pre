package com.moutaigua8183.isl_android_gaze.Fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.moutaigua8183.isl_android_gaze.Activities.DataCollectionActivity;
import com.moutaigua8183.isl_android_gaze.Handlers.CameraHandler;
import com.moutaigua8183.isl_android_gaze.R;

/**
 * Created by mou on 9/16/17.
 */


public class FragmentPreview extends Fragment {


    private final String LOG_TAG = "FragmentPreview";
    private CameraHandler cameraHandler;
    private TextureView textureView;
    private OnActionListener onActionListener;


    public interface OnActionListener{
        void onClick();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textureView = (TextureView) getActivity().findViewById(R.id.fragment_preview_textureview);
        textureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionListener.onClick();
            }
        });
        // ensure texture fill the screen with a certain ratio
        int[] textureSize = fetchScreenSize();
        int expected_height = textureSize[0]*DataCollectionActivity.Image_Size.getHeight()/DataCollectionActivity.Image_Size.getWidth();
        if( expected_height< textureSize[1] ){
            textureSize[1] = expected_height;
        } else {
            textureSize[0] = textureSize[1]*DataCollectionActivity.Image_Size.getWidth()/DataCollectionActivity.Image_Size.getHeight();
        }
        textureView.setLayoutParams(new RelativeLayout.LayoutParams(textureSize[0], textureSize[1]));
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraHandler = CameraHandler.getInstance(getActivity(), false);
        cameraHandler.startPreview(textureView);
    }

    @Override
    public void onPause(){
        super.onPause();
        cameraHandler.stopPreview();
    }


    public void setOnActionListener(OnActionListener listener){
        this.onActionListener = listener;
    }

    private int[] fetchScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }


}
