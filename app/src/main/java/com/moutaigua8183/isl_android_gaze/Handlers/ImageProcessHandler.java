package com.moutaigua8183.isl_android_gaze.Handlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import com.moutaigua8183.isl_android_gaze.JNInterface.MobileGazeJniInterface;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

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

    public static float[][][] getStandardizedNormalDistribution(int[][][] rawImages){
        int dim0 = rawImages.length;
        int dim1 = rawImages[0].length;
        int dim2 = rawImages[0][0].length;
        float[][][] postImage = new float[dim0][dim1][dim2];
        float[] postArray = new float[dim0*dim1*dim2];
        int[] eachPreArray = new int[dim1*dim2];
        for(int d0 = 0; d0 < dim0; ++d0) {
            if( rawImages[d0][0][0]==0 ){
                for (int d1 = 0; d1 < dim1; ++d1) {
                    for (int d2 = 0; d2 < dim2; ++d2) {
                        postImage[d0][d1][d2] = rawImages[d0][d1][d2];
                    }
                }
                continue;
            }
            // 3D array --> row array
            for (int d1 = 0; d1 < dim1; ++d1) {
                for (int d2 = 0; d2 < dim2; ++d2) {
                    eachPreArray[d1 * dim2 + d2] = rawImages[d0][d1][d2];
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
                postArray[d0 * dim1 * dim2 + i] = (float)((eachPreArray[i] - mean)/stdev);
            }
            // postArray --> postImage
            for (int d1 = 0; d1 < dim1; ++d1) {
                for (int d2 = 0; d2 < dim2; ++d2) {
                    postImage[d0][d1][d2] = postArray[d0 * dim1 * dim2 + d1 * dim2 + d2];
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

    public static float[] toTensorFlowEyeModelArray(float[][][] image){
        int pic_num = image.length;
        int total_row = image[0].length;
        int total_col = image[0][0].length;
        float[] input_array = new float[ pic_num * total_col * total_row ];
        for(int pic = 0; pic < pic_num; ++pic) {
            for (int row = 0; row < total_row; ++row) {
                for (int col = 0; col < total_col; ++col) {
                    input_array[  pic * total_row * total_col
                            + row * total_col
                            + col ] = image[pic][row][col];
                }
            }
        }
        return input_array;
    }

    public static int[] getRotatedRGBImage(Image image){
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
        byte[] yBytes = new byte[yBuffer.capacity()];
        byte[] uBytes = new byte[uBuffer.capacity()];
        byte[] vBytes = new byte[vBuffer.capacity()];
        yBuffer.get(yBytes);
        uBuffer.get(uBytes);
        vBuffer.get(vBytes);
        int width = image.getWidth();
        int height = image.getHeight();
        MobileGazeJniInterface jniHandler = new MobileGazeJniInterface();
        int[] rgbIntArray = jniHandler.getRotatedRGBImage(yBytes, uBytes, vBytes, width, height);
        return rgbIntArray;
    }

    public static byte[] YUVtoJPEGByte(Image image, Context context) {
        byte[] nv21Bytes = YUV_420_888_to_NV21(image);
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21Bytes.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(image.getWidth()).setY(image.getHeight());
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

//        final Bitmap bmpout = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);

        in.copyFromUnchecked(nv21Bytes);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        Bitmap bmpout = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        out.copyTo(bmpout);
        FileOutputStream outFile = null;
        try {
            String base = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
            String imagePath = "/"+ base + "/Download/bitmap.jpg";
            outFile = new FileOutputStream(imagePath);
            bmpout.compress(Bitmap.CompressFormat.JPEG, 100, outFile); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    outFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byte[] jpgBytes = new byte[out.getBytesSize()];
        out.copyTo(jpgBytes);
        return jpgBytes;
    }

    public static byte[] YUV_420_888_to_NV21(Image image) {
        ByteBuffer yBuffer = image. getPlanes()[0]. getBuffer();
        ByteBuffer uBuffer = image. getPlanes()[1]. getBuffer();
        ByteBuffer vBuffer = image. getPlanes()[2]. getBuffer();
        int ySize = yBuffer. remaining();
        int uSize = uBuffer. remaining();
        int vSize = vBuffer. remaining();
        byte[] nv21 = new byte[ySize + uSize + vSize];
        byte[] uBytes = new byte[uSize];
        byte[] vBytes = new byte[vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        uBuffer.get(uBytes, 0, uSize);
        vBuffer.get(vBytes, 0, vSize);
        for(int i=0; i<uSize; i++){
            nv21[ySize + 2*i] = vBytes[i];
            nv21[ySize + 2*i + 1] = uBytes[i];
        }
//        yBuffer.get(nv21, 0, ySize);
//        for(int i=0; i<vSize; i++){
//            vBuffer.get(nv21, ySize + 2*i, 1);
//            uBuffer.get(nv21, ySize + 2*i + 1, 1);
//        }
        return nv21;
    }



}
