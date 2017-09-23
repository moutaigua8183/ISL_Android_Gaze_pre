package com.moutaigua8183.isl_android_gaze;



import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Trace;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * JNI wrapper class for the Tensorflow native code.
 */
public class TensorFlowClassifier implements Classifier {
    private static final String TAG = "TensorflowClassifier";

    // jni native methods.
    public native int initializeTensorFlow(
            AssetManager assetManager,
            String model,
            String labels,
            int numClasses,
            int inputSize,
            int imageMean,
            float imageStd,
            String inputName,
            String outputName);

    private native String classifyImageBmp(Bitmap bitmap);

    private native String classifyImageRgb(int[] output, int width, int height);

    static {
        System.loadLibrary("tensorflow_demo");
    }

    @Override
    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("Recognize");
        final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
        for (final String result : classifyImageBmp(bitmap).split("\n")) {
            Log.i(TAG, "Parsing [" + result + "]");

            // Clean up the string as needed
            final StringTokenizer st = new StringTokenizer(result);
            if (!st.hasMoreTokens()) {
                continue;
            }

            final String id = st.nextToken();
            final String confidenceString = st.nextToken();
            final float confidence = Float.parseFloat(confidenceString);

            final String title =
                    result.substring(id.length() + confidenceString.length() + 2, result.length());

            if (!title.isEmpty()) {
                recognitions.add(new Recognition(id, title, confidence, null));
            }
        }
        Trace.endSection();
        return recognitions;
    }

    @Override
    public void enableStatLogging(boolean debug) {

    }

    @Override
    public String getStatString() {
        return null;
    }

    @Override
    public void close() {}
}