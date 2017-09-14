package com.moutaigua8183.isl_android_gaze.Controllers;

import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Date;

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
    private byte[] imageBytes;

    public ImageFileHandler(){
        imageReader = null;
        imageHeight = -1;
        imageWidth = -1;
        imageFormat = -1;
        maxImages = 1;
        imageBytes = null;
    }

    public ImageFileHandler(int width, int height, int format, int _maxImages) {
        imageWidth = width;
        imageHeight = height;
        imageFormat = format;
        maxImages = _maxImages;
        imageBytes = null;
        imageReader = ImageReader.newInstance(imageWidth, imageHeight, imageFormat, maxImages);
    }


    public void instantiateImageReader(){
        if( -1!=imageWidth && -1!=imageHeight && -1!=imageFormat ) {
            imageReader = ImageReader.newInstance(imageWidth, imageHeight, imageFormat, maxImages);
            ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    imageBytes = new byte[buffer.capacity()];
                    buffer.get(imageBytes);
                    saveImageByteIntoFile(imageBytes);
                    image.close();
                }
            };
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
        }
    }

    public ImageReader getImageReader() {
        if( null==imageReader ){
            Log.d(LOG_TAG, "ImageReader is not instantiated");
        }
        return imageReader;
    }

    private void saveImageByteIntoFile(byte[] imageData){
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        File picFolderRoot = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Data");
        if (!picFolderRoot.exists()){
            if (!picFolderRoot.mkdirs()){
                Log.d("App", "failed to create directory");
            }
        }
        File picFile = new File(picFolderRoot.getPath() + File.separator + currentDateTimeString + ".jpg");
        try {
            OutputStream output = new FileOutputStream(picFile);
            output.write(imageData);
            output.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception occurred while saving picture to external storage ", e);
        }
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
