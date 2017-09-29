package com.moutaigua8183.isl_android_gaze.TensorFlow;

import android.content.Context;
import android.content.res.AssetManager;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by Mou on 9/28/2017.
 */


public class TensorFlowInference {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private static TensorFlowInference myInstance;
    private TensorFlowInferenceInterface tf;
    private static final String MODEL_FILE = "file:///android_asset/optimized_my_model.pb";
    private static final String INPUT_NODE = "input_image";
    private static final String[] OUTPUT_NODES = {"output_image"};
    private static final String OUTPUT_NODE = "y_";
    private static final long[] INPUT_SIZE = {1,3};
    private static final int OUTPUT_SIZE = 6;
    private static AssetManager assetManager;

    public static TensorFlowInference getInstance(final Context context) {
        if (myInstance == null)
        {
            myInstance = new TensorFlowInference(context);
        }
        return myInstance;
    }

    public TensorFlowInference(final Context context) {
        this.assetManager = context.getAssets();
        tf = new TensorFlowInferenceInterface();
        if( 0 != tf.initializeTensorFlow(assetManager, MODEL_FILE) ){
            throw new RuntimeException("TensorFlow initialization Fails in TensorFlowInference.Java");
        }
    }


    public float getResult(float a, float b){
        float[] res = new float[]{0};
        String[] outputNames = new String[]{"output"};
        tf.fillNodeFloat("x", new int[]{1, 1}, new float[]{a});
        tf.runInference(outputNames);
        tf.readNodeFloat(outputNames[0], res);
        return res[0];
    }

}
