package com.moutaigua8183.isl_android_gaze.Activities;

import android.content.res.AssetManager;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLInt8;
import com.moutaigua8183.isl_android_gaze.Handlers.CameraHandler;
import com.moutaigua8183.isl_android_gaze.Handlers.DotHandler;
import com.moutaigua8183.isl_android_gaze.Handlers.ImageProcessHandler;
import com.moutaigua8183.isl_android_gaze.Handlers.TensorFlowHandler;
import com.moutaigua8183.isl_android_gaze.R;

import org.ujmp.core.Matrix;
import org.ujmp.core.util.Base64;
import org.ujmp.jmatio.ImportMatrixMAT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by Mou on 9/22/2017.
 */

public class TensorFlowActivity extends AppCompatActivity {

    private final String LOG_TAG = "TensorFlowActivity";
    private CameraHandler cameraHandler;
    private DotHandler dotHandler;
    private TextureView textureView;
    private FrameLayout view_dot_container;
    private TextView    result_board;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tensorflow);
        getSupportActionBar().hide();

        textureView = (TextureView) findViewById(R.id.activity_tensorflow_preview_textureview);
        // ensure texture fill the screen with a certain ratio
        int[] textureSize = fetchScreenSize();
        int expected_height = textureSize[0]*DataCollectionActivity.Image_Size.getHeight()/DataCollectionActivity.Image_Size.getWidth();
        if( expected_height< textureSize[1] ){
            textureSize[1] = expected_height;
        } else {
            textureSize[0] = textureSize[1]*DataCollectionActivity.Image_Size.getWidth()/DataCollectionActivity.Image_Size.getHeight();
        }
        textureView.setLayoutParams(new RelativeLayout.LayoutParams(textureSize[0], textureSize[1]));

        view_dot_container = (FrameLayout) findViewById(R.id.activity_tensorflow_layout_dotHolder);
        view_dot_container.bringToFront();
        view_dot_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result_board.setText("");
                Log.d(LOG_TAG, "pressed");
            }
        });
        dotHandler = new DotHandler(this, fetchScreenSize());
        dotHandler.setDotHolderLayout(view_dot_container);
        dotHandler.showAllCandidateDots();

        result_board = (TextView) findViewById(R.id.activity_tensorflow_txtview_result);


    }


    @Override
    public void onResume() {
        super.onResume();
        cameraHandler = CameraHandler.getInstance(this);
        cameraHandler.startPreview(textureView);
    }

    @Override
    public void onPause(){
        super.onPause();
        cameraHandler.stopPreview();
    }


    private int[][][][] readDataFromMatFile(int pic_num){
        int[][][][] image = new int[pic_num][36][60][3];
        try{
            AssetManager assetManager = getResources().getAssets();
            InputStream inputStream = assetManager.open("eye_left_all.dat");
            byte data[] = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            for(int colorLayer=0; colorLayer<3; ++colorLayer) {
                for (int col = 0; col < 60; ++col) {
                    for (int row = 0; row < 36; ++row) {
                        for(int pic = 0; pic < pic_num; ++pic) {
                            image[pic][row][col][colorLayer] = (int) data[pic + pic_num * row + pic_num * 36 * col + pic_num * 36 * 60 * colorLayer];
                        }
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }

    private int[][] readResultFromMatFile(){
        int[][] image = new int[425][2];
        try{
            AssetManager assetManager = getResources().getAssets();
            InputStream inputStream = assetManager.open("gaze_result.dat");
            byte data[] = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            for(int col=0; col<2; ++col) {
                for (int row = 0; row < 425; ++row) {
                    image[row][col] = ((data[row*2 + 1 + 850*col]<<8)& 0xFFFF) | data[row*2 + 850*col] & 0x00FF;
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }

    private int[] fetchScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }




    /***************** Test *******************/
    private String testPrecision(){
        int pic_num = 425;
        int[][] gaze_result = readResultFromMatFile();
        int[][][][] rawImages = readDataFromMatFile(pic_num);
        long timestamp_start = System.currentTimeMillis();
        float[][][][] processedImages = ImageProcessHandler.getStandardizedNormalDistribution(rawImages);
        float[] para = TensorFlowHandler.getInstance(TensorFlowActivity.this).getEstimatedLocation(processedImages);
        long timestamp_end = System.currentTimeMillis();
        int[] ScreenSize = fetchScreenSize();
        double err_x = 0;
        double err_y = 0;
        double err_d = 0;
        for(int i =0; i<pic_num; ++i){
            int esti_x = Math.round(para[2*i] * ScreenSize[0]);
            int esti_y = Math.round(para[2*i + 1] * ScreenSize[1]);
            err_x += Math.abs(gaze_result[i][0] - esti_x);
            err_y += Math.abs(gaze_result[i][1] - esti_y);
            err_d += Math.abs( Math.sqrt( (gaze_result[i][0] - esti_x)*(gaze_result[i][0] - esti_x)+(gaze_result[i][1] - esti_y)*(gaze_result[i][1] - esti_y) ) );
        }
        String res_report = "# of Images = " + pic_num
                            + "\nTotal Time  = " + (timestamp_end - timestamp_start)/1000 + "." + (timestamp_end - timestamp_start)%1000 + " sec"
                            + "\nTime of Each = " + (timestamp_end-timestamp_start)/pic_num + " ms"
                            + "\nErr_X = " + String.format("%.2f", err_x/pic_num) + " px"
                            + "\nErr_Y = " + String.format("%.2f", err_y/pic_num) + " px"
                            + "\nErr_D = " + String.format("%.2f", err_d/pic_num) + " px" ;
        Log.d(LOG_TAG,"\n" + res_report);
        return res_report;
    }


}
