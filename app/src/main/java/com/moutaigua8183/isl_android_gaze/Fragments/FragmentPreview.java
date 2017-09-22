package com.moutaigua8183.isl_android_gaze.Fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.moutaigua8183.isl_android_gaze.Activities.DataCollectionActivity;
import com.moutaigua8183.isl_android_gaze.Controllers.CameraHandler;
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
        public void onClick();
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
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onActionListener.onClick();
                    }
                }, 800);
            }
        });
        textureView.setLayoutParams(
                // switch the height and width deliberately for better preview effect,
                // but finally the image will be saved in the correct ratio
                new RelativeLayout.LayoutParams(
                        DataCollectionActivity.Image_Size.getHeight(),
                        DataCollectionActivity.Image_Size.getWidth()
                )
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraHandler = CameraHandler.getInstance(getActivity(), DataCollectionActivity.Image_Size);
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


}
