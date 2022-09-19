package com.daniilvdovin.pixelit.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class FaceDetect {

    static Bitmap resultBitmap;
    public static Bitmap getResult(Context context, Bitmap bitmap){
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.15f)
                        .enableTracking()
                        .build();
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        FaceDetector detector = FaceDetection.getClient(options);
        Task<List<Face>> result =
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
    static void processFaceList(List<Face> faces,Bitmap bitmap) {
        for (Face face : faces) {
            Rect bounds = face.getBoundingBox();
            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees
            Bitmap tempBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
            Canvas canvas = new Canvas(tempBitmap);
            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.GREEN);
            canvas.drawRect(bounds, p);

            List<PointF> faceOvel = Objects.requireNonNull(face.getContour(FaceContour.FACE)).getPoints();
            for (int i = 1; i < faceOvel.size(); i++) {
                PointF pfs = faceOvel.get(i-1);
                PointF pff = faceOvel.get(i);
                canvas.drawLine(pfs.x,pfs.y,pff.x,pff.y,p);
            }
            canvas.drawLine(faceOvel.get(0).x,faceOvel.get(0).y,faceOvel.get(faceOvel.size()-1).x,faceOvel.get(faceOvel.size()-1).y,p);
            // If face tracking was enabled:
            if (face.getTrackingId() != null) {
                int id = face.getTrackingId();
            }

            Log.e("ML","Face processed Success");
            resultBitmap = tempBitmap;
        }
    }
}
