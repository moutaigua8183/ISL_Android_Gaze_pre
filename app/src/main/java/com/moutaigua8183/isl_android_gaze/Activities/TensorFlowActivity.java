package com.moutaigua8183.isl_android_gaze.Activities;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moutaigua8183.isl_android_gaze.Handlers.CameraHandler;
import com.moutaigua8183.isl_android_gaze.Handlers.DotHandler;
import com.moutaigua8183.isl_android_gaze.Handlers.ImageProcessHandler;
import com.moutaigua8183.isl_android_gaze.Handlers.TensorFlowHandler;
import com.moutaigua8183.isl_android_gaze.Handlers.TimerHandler;
import com.moutaigua8183.isl_android_gaze.Handlers.VolleyHandler;
import com.moutaigua8183.isl_android_gaze.JNInterface.MobileGazeInterface;
import com.moutaigua8183.isl_android_gaze.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mou on 9/22/2017.
 */

public class TensorFlowActivity extends AppCompatActivity {

    private final String LOG_TAG = "TensorFlowActivity";
    private CameraHandler cameraHandler;
    private DotHandler dotHandler;
    private TextureView textureView;
    private FrameLayout view_dot_container;
    private FrameLayout view_dot_container_result;
    private TextView    result_board;
    private int[]       SCREEN_SIZE;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tensorflow);
        getSupportActionBar().hide();

        SCREEN_SIZE = fetchScreenSize();
        textureView = (TextureView) findViewById(R.id.activity_tensorflow_preview_textureview);
        // ensure texture fill the screen with a certain ratio
        int[] textureSize = SCREEN_SIZE;
        int expected_height = textureSize[0]*DataCollectionActivity.Image_Size.getHeight()/DataCollectionActivity.Image_Size.getWidth();
        if( expected_height< textureSize[1] ){
            textureSize[1] = expected_height;
        } else {
            textureSize[0] = textureSize[1]*DataCollectionActivity.Image_Size.getWidth()/DataCollectionActivity.Image_Size.getHeight();
        }
        textureView.setLayoutParams(new RelativeLayout.LayoutParams(textureSize[0], textureSize[1]));


        view_dot_container = (FrameLayout) findViewById(R.id.activity_tensorflow_layout_dotHolder_background);
        view_dot_container.bringToFront();
        view_dot_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result_board.setText("");
                Log.d(LOG_TAG, "pressed");
                cameraHandler.setCameraState(CameraHandler.CAMERA_STATE_STILL_CAPTURE);
                /**** test ***/
                MobileGazeInterface gazeInterface = new MobileGazeInterface();
                byte[] res = gazeInterface.getMsg(new byte[]{1,2});
                result_board.setText( res.toString() );
            }
        });
        view_dot_container_result = (FrameLayout) findViewById(R.id.activity_tensorflow_layout_dotHolder_result);
        view_dot_container_result.bringToFront();
        dotHandler = new DotHandler(this, fetchScreenSize());
        dotHandler.setDotHolderLayout(view_dot_container);
        dotHandler.showAllCandidateDots();

        result_board = (TextView) findViewById(R.id.activity_tensorflow_txtview_result);


    }


    @Override
    public void onResume() {
        super.onResume();
        cameraHandler = CameraHandler.getInstance(this, true);
        cameraHandler.setOnImageAvailableListenerForPrev(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireNextImage();
                if( cameraHandler.getCameraState()==CameraHandler.CAMERA_STATE_STILL_CAPTURE ) {
                    cameraHandler.setCameraState(CameraHandler.CAMERA_STATE_PREVIEW);
                    Log.d(LOG_TAG, "Take a picture");
//                    ByteBuffer buffer = image. getPlanes()[0]. getBuffer();
//                    byte[] bwImageBytes = new byte[buffer.capacity()];
//                    buffer.get(bwImageBytes);
//                    uploadImage(bwImageBytes);
                    TimerHandler.getInstance().reset();
                    TimerHandler.getInstance().tic();
                    ImageProcessHandler.YUVtoJPEGByte(image, TensorFlowActivity.this);
                    Log.d(LOG_TAG, String.valueOf(TimerHandler.getInstance().toc()));
                }
                image.close();
            }
        });
//        cameraHandler.startPreview(textureView);
    }

    @Override
    public void onPause(){
        super.onPause();
        cameraHandler.stopPreview();
    }


    private int[][][][] readUInt16DataFromMatFile(String filename, int pic_num){
        int[][][][] image = new int[pic_num][36][60][3];
        try{
            AssetManager assetManager = getResources().getAssets();
            InputStream inputStream = assetManager.open(filename);
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

    private int[][][] readEyeStateFromMatFile(String filename, int pic_num){
        int[][][] image = new int[pic_num][32][32];
        try{
            AssetManager assetManager = getResources().getAssets();
            InputStream inputStream = assetManager.open(filename);
            byte data[] = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            for (int col = 0; col < 32; ++col) {
                for (int row = 0; row < 32; ++row) {
                    for(int pic = 0; pic < pic_num; ++pic) {
                        image[pic][row][col] = data[pic + pic_num * row + pic_num * 32 * col ] & 0xFF;
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }

    private int[][] readResultFromMatFile(String fileName){
        int[][] image = new int[425][2];
        try{
            AssetManager assetManager = getResources().getAssets();
            InputStream inputStream = assetManager.open(fileName);
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

    private void uploadImage(byte[] imageBytes){
        VolleyHandler.getInstance(TensorFlowActivity.this).uploadFileToServer(
                "http://ec2-54-236-72-209.compute-1.amazonaws.com/api/v1.0/upload",
                imageBytes,
                "image",
                new com.android.volley.Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
//                        android.os.Debug.waitForDebugger();
                        Log.d(LOG_TAG, String.valueOf(TimerHandler.getInstance().toc()) );
                        try {
                            JSONObject jsonRes = response;
                            String message = jsonRes.getString("msg");
                            if( !message.equalsIgnoreCase("success") ){
                                Log.d("VolleyHandler", message);
                                result_board.setText(message);
                            } else {
                                if( !jsonRes.isNull("data")) {
                                    JSONObject jsonData = jsonRes.getJSONObject("data");
                                    double widthRatio = jsonData.getDouble("width");
                                    double heightRatio = jsonData.getDouble("height");
                                    int x = (int) (SCREEN_SIZE[0] * widthRatio);
                                    int y = (int) (SCREEN_SIZE[1] * heightRatio);
                                    dotHandler.showDot(x, y, view_dot_container_result);
                                }
                                String timer1 = jsonRes.getString("timer1").substring(0,5);
                                String timer2 = jsonRes.getString("timer2").substring(0,5);
                                String resStr = "Prep: " + timer1 + "sec\n Crop: " + timer2 + " sec";
                                Log.d(LOG_TAG, resStr);
                                result_board.setText(resStr);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }






    /***************** Test *******************/
    private String testPrecision(){
        int pic_num = 425;
        int[][] gaze_result = readResultFromMatFile("gaze_result.dat");
        int[][][][] rawImages = readUInt16DataFromMatFile("eye_left_all.dat", pic_num);
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

    private String testNewPbFile(){
        int pic_num = 1074;
        int[][][] pre_image = readEyeStateFromMatFile("eye_state_left.dat", pic_num);
        float[][][] post_image = ImageProcessHandler.getStandardizedNormalDistribution(pre_image);
        float[] para = TensorFlowHandler.getInstance(TensorFlowActivity.this).getResultFromNewPbFile(post_image);
        String content = "";
        for(int i=0; i<para.length/2; i++){
            content += String.valueOf(para[i*2]) + "   " + String.valueOf(para[i*2+1]) + "\n";
        }
        return content;
    }

    public void saveImageByteIntoFile(byte[] imageData, String file_name){
        if(file_name==null || file_name.isEmpty()){
            Log.d(LOG_TAG, "Invalid filename. Image is not saved");
            return;
        }
        String base = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
        String imagePath = "/"+ base + "/Download/"+file_name;
        File picFile = new File( imagePath );
        try {
            OutputStream output = new FileOutputStream(picFile);
            output.write(imageData);
            output.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception occurred while saving picture to external storage ", e);
        }
    }


}
