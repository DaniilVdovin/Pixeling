package com.daniilvdovin.pixelit.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import com.daniilvdovin.pixelit.MainActivity;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;
import java.util.Objects;

public class FaceDetect {

    public static Bitmap resultBitmap;
    static MainActivity context;
    static FaceDetectorOptions options;
    static FaceDetector detector;
    public static void Activate(){
        options=new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.15f)
                        .enableTracking()
                        .build();
        detector = FaceDetection.getClient(options);
    }
    public static Bitmap getResult(Context context, Bitmap bitmap){
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        detector.process(image)
                        .addOnSuccessListener(
                                faces -> {
                                    // Task completed successfully
                                    // ...
                                    Log.e("ML","Task Success");
                                    processFaceList(faces,bitmap);
                                })
                        .addOnFailureListener(
                                e -> {
                                    // Task failed with an exception
                                    // ...
                                    Log.e("ML","Task Failed");
                                });
        if(resultBitmap!=null) {
            Log.e("ML","Return Success");
            return resultBitmap;
        }else{
            Log.e("ML","Return Failure");
            return bitmap;
        }
    }
    static Bitmap processFaceList(List<Face> faces,Bitmap bitmap) {
        for (Face face : faces) {
            Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(tempBitmap);
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL_AND_STROKE);
            p.setColor(Color.WHITE);
            List<PointF> faceOvel = Objects.requireNonNull(face.getContour(FaceContour.FACE)).getPoints();
            Path polyFace = new Path();
            PointF pfs = faceOvel.get(0);
            polyFace.moveTo(pfs.x,pfs.y);
            for (int i = 0; i < faceOvel.size(); i++) {
                PointF pff = faceOvel.get(i);
                polyFace.lineTo(pff.x,pff.y);
            }
            polyFace.lineTo(pfs.x,pfs.y);
            canvas.drawPath(polyFace,p);
            Log.e("ML","Face processed Success");
            resultBitmap = tempBitmap;
        }
        return resultBitmap;
    }
}
