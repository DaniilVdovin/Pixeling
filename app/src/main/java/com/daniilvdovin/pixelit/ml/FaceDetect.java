package com.daniilvdovin.pixelit.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;

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
    static FaceDetectorOptions options;
    static FaceDetector detector;
    public static void Activate(){
        options=new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .enableTracking()
                        .build();
        detector = FaceDetection.getClient(options);
    }
    public static Bitmap getResult(Context context, Bitmap bitmap){
        Activate();
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
    static void processFaceList(List<Face> faces, Bitmap bitmap) {
        if(faces.size()==0)return;
        Log.e("ML","Faces: "+faces.size());
        for (Face face : faces) {
            //TODO: See only 1 face
            if(face==null)break;
            if(face.getAllContours().size()==0)break;
            Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(tempBitmap);
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL_AND_STROKE);
            p.setColor(Color.WHITE);
            List<PointF> faceOval = Objects.requireNonNull(face.getContour(FaceContour.FACE)).getPoints();
            Path polyFace = new Path();
            PointF pfs = faceOval.get(0);
            polyFace.moveTo(pfs.x,pfs.y);
            for (int i = 0; i < faceOval.size(); i++) {
                PointF pff = faceOval.get(i);
                polyFace.lineTo(pff.x,pff.y);
            }
            polyFace.lineTo(pfs.x,pfs.y);
            canvas.drawPath(polyFace,p);
            resultBitmap = tempBitmap;
            Log.e("ML","Face processed Success");
        }
    }
}
