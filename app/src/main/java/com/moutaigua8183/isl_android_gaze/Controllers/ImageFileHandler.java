package com.moutaigua8183.isl_android_gaze.Controllers;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.util.Log;
import android.util.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Mou on 9/12/2017.
 */

public class ImageFileHandler {

    private final String LOG_TAG = "Image_File_Handler";
    private final String FOLDER_NAME = "Android_Gaze_Data";
    private ImageReader imageReader;
    private int imageWidth;
    private int imageHeight;
    private int imageFormat;
    private int maxImages;
    private String imageName;
    private SavingCallback savingCallback;

    // interface for operations after pictures are saved
    public interface SavingCallback{
        void onSaved();
    }


    // default constructor
    public ImageFileHandler(){
        imageReader = null;
        imageHeight = -1;
        imageWidth = -1;
        imageFormat = -1;
        maxImages = 1;
        imageName = "";
        savingCallback = null;
    }

    public ImageFileHandler(int width, int height, int format, int _maxImages) {
        imageWidth = width;
        imageHeight = height;
        imageFormat = format;
        maxImages = _maxImages;
        imageName = "";
        savingCallback = null;
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
                    byte[] imageBytes = new byte[buffer.capacity()];
                    buffer.get(imageBytes);
                    saveImageByteIntoFile(imageBytes, imageName);
                    Log.d(LOG_TAG, "taken");
                    image.close();
                    if( null!=savingCallback ){
                        savingCallback.onSaved();
                    }
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

    public void setSavingCallback(SavingCallback _savingCallback){
        this.savingCallback = _savingCallback;
    }

    public void saveImageByteIntoFile(byte[] imageData, String file_name){
        if(file_name==null || file_name.isEmpty()){
            Log.d(LOG_TAG, "Invalid filename. Image is not saved");
            return;
        }
        File picFolderRoot = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), FOLDER_NAME);
        if (!picFolderRoot.exists()){
            if (!picFolderRoot.mkdirs()){
                Log.d("App", "failed to create directory");
            }
        }
        String file_name_sufix;
        switch (this.imageFormat){
            case ImageFormat.JPEG:
                file_name_sufix = ".jpg";
                break;
            default:
                file_name_sufix = ".jpg";
                break;
        }
        File picFile = new File(picFolderRoot.getPath() + File.separator + file_name + file_name_sufix);
        try {
            OutputStream output = new FileOutputStream(picFile);
            output.write(imageData);
            output.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception occurred while saving picture to external storage ", e);
        }
    }

    public void setImageSize(Size[] size){
//        imageWidth = null!=size ? size[0].getWidth() : 640 ;
//        imageHeight = null!=size ? size[0].getHeight() : 480 ;
        imageWidth = 640 ;
        imageHeight = 480 ;
    }

    public void setImageFormat(int imageFormat) {
        this.imageFormat = imageFormat;
    }

    public void setImageName(String image_name) {
        this.imageName = image_name;
    }

    public void deleteLastImage(){
        File picFolderRoot = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), FOLDER_NAME);
        String file_name_sufix;
        switch (this.imageFormat){
            case ImageFormat.JPEG:
                file_name_sufix = ".jpg";
                break;
            default:
                file_name_sufix = ".jpg";
                break;
        }
        File picFile = new File(picFolderRoot.getPath() + File.separator + imageName + file_name_sufix);
        if( picFile.exists() ){
            if( picFile.delete() ){
                Log.d(LOG_TAG, "Image " + imageName + " is deleted");
            }
        }
    }


}
