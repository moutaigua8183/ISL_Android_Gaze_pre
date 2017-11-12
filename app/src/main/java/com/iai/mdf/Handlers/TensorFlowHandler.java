package com.iai.mdf.Handlers;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Point;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by Mou on 9/28/2017.
 */


public class TensorFlowHandler {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private static TensorFlowHandler myInstance;
    private TensorFlowInferenceInterface tf;
    private static final String MODEL_FILE = "file:///android_asset/eye_state_v1_ft_VTTI_all_images.pb";
    private static final String INPUT_NODE = "eye"; //"input_image";
    private static final long[] INPUT_SIZE = {1,3};
    private static final String[] OUTPUT_NODES = {"eye_state0"};    //{"output"};
    private static final int OUTPUT_SIZE = 6;
    private static AssetManager assetManager;

    public static TensorFlowHandler getInstance(final Context context) {
        if (myInstance == null)
        {
            myInstance = new TensorFlowHandler(context);
        }
        return myInstance;
    }

    public TensorFlowHandler(final Context context) {
        this.assetManager = context.getAssets();
        tf = new TensorFlowInferenceInterface();
        if( 0 != tf.initializeTensorFlow(assetManager, MODEL_FILE) ){
            throw new RuntimeException("TensorFlow initialization Fails in TensorFlowHandler.Java");
        }
    }

    public Point getTestLocation(float[] array){
        tf.fillNodeFloat(INPUT_NODE, new int[]{1, 1}, array);
        tf.runInference(OUTPUT_NODES);
        // retrieve result from TensorFlow model
        float[] loc = new float[]{0,0};
        tf.readNodeFloat(OUTPUT_NODES[0], loc);
        Point res = new Point();
        res.set(  (int)loc[0],(int)loc[1] );
        return res;
    }

    public float[] getEstimatedLocation(float[][][][] images){
        float[] input_array = ImageProcessHandler.toTensorFlowEyeModelArray(images);
        tf.fillNodeFloat(
                INPUT_NODE,
                new int[]{
                        images.length,
                        ImageProcessHandler.EYE_MODEL_INPUTSIZE_ROWS,
                        ImageProcessHandler.EYE_MODEL_INPUTSIZE_COLUMNS,
                        ImageProcessHandler.EYE_MODEL_INPUTSIZE_COLORS},
                input_array
        );
        tf.runInference(OUTPUT_NODES);
        // retrieve result from TensorFlow model
        float[] loc = new float[ images.length * 2];
        tf.readNodeFloat(OUTPUT_NODES[0], loc);
        return loc;
    }

    public float[] getResultFromNewPbFile(float[][][] images){
        float[] input_array = ImageProcessHandler.toTensorFlowEyeModelArray(images);
        tf.fillNodeFloat(INPUT_NODE,
                new int[]{images.length, images[0].length, images[0][0].length, 1},
                input_array);
        tf.runInference(OUTPUT_NODES);
        // retrieve result from TensorFlow model
        float[] loc = new float[ images.length * 2];
        tf.readNodeFloat(OUTPUT_NODES[0], loc);
        return loc;
    }



}
