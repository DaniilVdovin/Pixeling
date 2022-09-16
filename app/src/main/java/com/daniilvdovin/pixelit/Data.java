package com.daniilvdovin.pixelit;

import android.graphics.Bitmap;
import android.net.Uri;

//Static data class
public class Data {
    //Dots on image
    public static boolean _isDots = false;
    //Grid on image
    public static boolean _isGrid = false;
    //To grayscale
    public static boolean _isGray = false;
    //Filter image (blurring)
    public static boolean _isFilter = false;
    //Scan color by pixels
    public static boolean _isScanColor = false;
    //Debug logs
    public static boolean _isDebug = false;
    //Upscale image on Scale Size
    public static  int ScaleSize = 30;
    //Pixel size PixelRate/PixelRate
    public static int PixelRate = 48;
    //Original image
    public static Bitmap image;
    //Image in progress
    public static Bitmap image_processed;
    //Image path after crop
    public static Uri imageUri;
    //Image path after crop / name image after save ("Pixel_"+image_name+".jpg")
    public static String image_name;
}