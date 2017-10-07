package com.moutaigua8183.isl_android_gaze.Handlers;

import android.util.Log;

/**
 * Created by Mou on 10/1/2017.
 */

public class ImageProcessHandler {

    private final String LOG_TAG = "ImageProcessHandler";
    public static int EYE_MODEL_INPUTSIZE_ROWS      = 36;
    public static int EYE_MODEL_INPUTSIZE_COLUMNS   = 60;
    public static int EYE_MODEL_INPUTSIZE_COLORS    = 3;


    public static float[][][][] getStandardizedNormalDistribution(int[][][][] rawImages){
        int dim0 = rawImages.length;
        int dim1 = rawImages[0].length;
        int dim2 = rawImages[0][0].length;
        int dim3 = rawImages[0][0][0].length;
        float[][][][] postImage = new float[dim0][dim1][dim2][dim3];
        float[] postArray = new float[dim0*dim1*dim2*dim3];
        int[] eachPreArray = new int[dim1*dim2*dim3];
        for(int d0 = 0; d0 < dim0; ++d0) {
            // 3D array --> row array
            for (int d1 = 0; d1 < dim1; ++d1) {
                for (int d2 = 0; d2 < dim2; ++d2) {
                    for (int d3 = 0; d3 < dim3; ++d3) {
                        eachPreArray[d1 * dim2 * dim3 + d2 * dim3 + d3] = rawImages[d0][d1][d2][d3];
                    }
                }
            }
            // calculate
            double mean = 0;
            for(int i = 0; i < eachPreArray.length; i++) {
                mean += eachPreArray[i];
            }
            mean /= eachPreArray.length;
            double stdev = 0;
            for(int i = 0; i < eachPreArray.length; i++) {
                stdev += (eachPreArray[i] - mean) * (eachPreArray[i] - mean);
            }
            stdev = Math.sqrt( stdev/(eachPreArray.length-1) );
            // preArray --> postArray
            for(int i = 0; i < eachPreArray.length; i++) {
                postArray[d0 * dim1 * dim2 * dim3 + i] = (float)((eachPreArray[i] - mean)/stdev);
            }
            // postArray --> postImage
            for (int d1 = 0; d1 < dim1; ++d1) {
                for (int d2 = 0; d2 < dim2; ++d2) {
                    for (int d3 = 0; d3 < dim3; ++d3) {
                        postImage[d0][d1][d2][d3] = postArray[d0 * dim1 * dim2 * dim3 + d1 * dim2 * dim3 + d2 * dim3 + d3];
                    }
                }
            }
        }
        return postImage;
    }


    public static float[] toTensorFlowEyeModelArray(float[][][][] image){
        int pic_num = image.length;
        float[] input_array = new float[ pic_num * EYE_MODEL_INPUTSIZE_ROWS * EYE_MODEL_INPUTSIZE_COLUMNS * EYE_MODEL_INPUTSIZE_COLORS];
        for(int pic = 0; pic < pic_num; ++pic) {
            for (int row = 0; row < EYE_MODEL_INPUTSIZE_ROWS; ++row) {
                for (int col = 0; col < EYE_MODEL_INPUTSIZE_COLUMNS; ++col) {
                    for (int colorLayer = 0; colorLayer < EYE_MODEL_INPUTSIZE_COLORS; ++colorLayer) {
                        input_array[    pic * EYE_MODEL_INPUTSIZE_ROWS * EYE_MODEL_INPUTSIZE_COLUMNS * EYE_MODEL_INPUTSIZE_COLORS
                                        + row * EYE_MODEL_INPUTSIZE_COLUMNS * EYE_MODEL_INPUTSIZE_COLORS
                                        + col * EYE_MODEL_INPUTSIZE_COLORS
                                        + colorLayer ] = image[pic][row][col][colorLayer];
                    }
                }
            }
        }
        return input_array;
    }




}
