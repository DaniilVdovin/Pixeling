package com.daniilvdovin.pixelit.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;

import java.nio.ByteBuffer;

public class SegmentMl {
    public static Bitmap resultBitmap;
    static SelfieSegmenterOptions options;
    static Segmenter segmenter;
    public static void Activate(){
        options = new SelfieSegmenterOptions.Builder()
                        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                        .build();
        segmenter = Segmentation.getClient(options);
    }
    public static Bitmap getResult(Context context, Bitmap bitmap){
        Activate();
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Task<SegmentationMask> result =
                segmenter.process(image)
                        .addOnSuccessListener(
                                mask -> {
                                    // Task completed successfully
                                    // ...
                                    ByteBuffer maskb = mask.getBuffer();
                                    int maskWidth = mask.getWidth();
                                    int maskHeight = mask.getHeight();
                                    Log.e("ML",maskWidth+"x"+maskHeight);
                                    resultBitmap = Bitmap.createBitmap(bitmap);
                                    for (int y = 0; y < maskHeight; y++) {
                                        for (int x = 0; x < maskWidth; x++) {
                                            // Gets the confidence of the (x,y) pixel in the mask being in the foreground.
                                            float foregroundConfidence = maskb.getFloat();
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                float al = (foregroundConfidence<1f?1f:0f);
                                                resultBitmap.setPixel(x,y,Color.argb(al,1f,1f,1f));
                                            }
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                e -> {
                                    // Task failed with an exception
                                    // ...
                                });
        if(resultBitmap!=null) {
            Log.e("ML","Return Success");
            return resultBitmap;
        }else{
            Log.e("ML","Return Failure");
            return bitmap;
        }
    }
    public static float clamp(float val, float min, float max) {
        float result = val > max ? max : val < min ? min : val;
        return result;
    }
}
