package com.daniilvdovin.pixelit;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.daniilvdovin.pixelit.colorize.PixelData;

import java.util.List;

//Static data class
public class Data {
    //User changed
    //Dots on image
    public static boolean _isDots = false;
    //Grid on image
    public static boolean _isGrid = false;
    //To grayscale
    public static boolean _isGray = false;
    //Filter image (blurring)
    public static boolean _isFilter = false;
    //ML Face detected
    public static boolean _isML_FaceDetected = false;
    //ML Segment detected
    public static boolean _isML_SegmentDetected = false;
    //System parameters
    //Scan color by pixels
    public static boolean _isScanColor = false;
    //Google Ads switch
    public static boolean _isGoogleAds = true;
    //Google Play Services
    public static boolean _isGooglePlayServices = false;
    //Huawei AppGallery Ads
    public static boolean _isAppGalleryAds = false;
    //Google Ads Debug Device
    public static boolean _isGoogleAds_DebugDevice =false;
    //Debug logs
    public static boolean _isDebug =false;
    //Can crop when load image
    public static boolean _isCanCrop = false;
    //Gallery open now
    public static boolean _isGalleryOpen = false;
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
    //Image path after save
    public static Uri imageAfterSave;
    //Image path after crop / name image after save ("Pixel_"+image_name+".jpg")
    public static String image_name;
    //Color array
    public static List<PixelData> colors;
    //Thread processing image
    public static AsyncTask<Bitmap, Integer, Bitmap> processing;
}
