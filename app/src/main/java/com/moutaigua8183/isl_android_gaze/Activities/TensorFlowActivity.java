package com.moutaigua8183.isl_android_gaze.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moutaigua8183.isl_android_gaze.Controllers.CameraHandler;
import com.moutaigua8183.isl_android_gaze.R;

import org.w3c.dom.Text;

/**
 * Created by Mou on 9/22/2017.
 */

public class TensorFlowActivity extends AppCompatActivity {

    private final String LOG_TAG = "ActivityTensorFlow";
    private CameraHandler cameraHandler;
    private TextureView textureView;
    private TextView    txt_result;
    private Button btn_detect;



    private static final int NUM_CLASSES = 1001;
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input:0";
    private static final String OUTPUT_NAME = "output:0";

    private static final String MODEL_FILE =                "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/imagenet_comp_graph_label_strings.txt";


    private final TensorFlowClassifier tensorflow = new TensorFlowClassifier();
    private TextView mResultText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tensorflow);
        getSupportActionBar().hide();


        textureView = (TextureView) findViewById(R.id.activity_tensorflow_preview_textureview);
        textureView.setLayoutParams(
                // switch the height and width deliberately for better preview effect,
                // but finally the image will be saved in the correct ratio
                new RelativeLayout.LayoutParams(
                        DataCollectionActivity.Image_Size.getHeight(),
                        DataCollectionActivity.Image_Size.getWidth()
                )
        );

        txt_result = (TextView) findViewById(R.id.activity_tensorflow_txt_result);

        btn_detect = (Button) findViewById(R.id.activity_tensorflow_btn_detect);
        btn_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }


    @Override
    public void onResume() {
        super.onResume();
        cameraHandler = CameraHandler.getInstance(this, DataCollectionActivity.Image_Size);
        cameraHandler.startPreview(textureView);
    }

    @Override
    public void onPause(){
        super.onPause();
        cameraHandler.stopPreview();
    }



}
