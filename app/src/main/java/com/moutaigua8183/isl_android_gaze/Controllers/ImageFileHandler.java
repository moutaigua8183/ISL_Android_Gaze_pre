package com.moutaigua8183.isl_android_gaze.Controllers;

import android.media.ImageReader;
import android.util.Log;

/**
 * Created by Mou on 9/12/2017.
 */

public class ImageFileHandler {

    private final String LOG_TAG = "Image_File_Handler";
    private ImageReader imageReader;
    private int imageWidth;
    private int imageHeight;
    private int imageFormat;
    private int maxImages;

    public ImageFileHandler(){
        imageReader = null;
        imageHeight = -1;
        imageWidth = -1;
        imageFormat = -1;
        maxImages = 1;
    }

    public ImageFileHandler(int width, int height, int format, int _maxImages) {
        imageWidth = width;
        imageHeight = height;
        imageFormat = format;
        maxImages = _maxImages;
        imageReader = ImageReader.newInstance(imageWidth, imageHeight, imageFormat, maxImages);
    }


    public void instantiateImageReader(){
        if( -1!=imageWidth && -1!=imageHeight && -1!=imageFormat ) {
            imageReader = ImageReader.newInstance(imageWidth, imageHeight, imageFormat, maxImages);
        }
    }

    public ImageReader getImageReader() {
        if( null==imageReader ){
            Log.d(LOG_TAG, "ImageReader is not instantiated");
        }
        return imageReader;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(int imageFormat) {
        this.imageFormat = imageFormat;
    }
}
