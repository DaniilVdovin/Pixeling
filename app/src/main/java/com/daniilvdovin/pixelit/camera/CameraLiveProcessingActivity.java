package com.daniilvdovin.pixelit.camera;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;

import com.daniilvdovin.pixelit.MainActivity;
import com.daniilvdovin.pixelit.R;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraFilter;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraLiveProcessingActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    public PreviewView previewView;
    Executor executor = new Executor() {
        @Override
        public void execute(Runnable runnable) {
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_live_processing);
        previewView = findViewById(R.id.previewView);
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        previewView.setScaleType(PreviewView.ScaleType.FIT_START);

        previewView.setOnClickListener(v->{
            ImageCapture imageCapture =
                    null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(this.getDisplay().getRotation())
                        .build();
            }
            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(new File(Environment.getExternalStorageDirectory()+"/File.jpg")).build();
            if(imageCapture!=null)
            imageCapture.takePicture(outputFileOptions, executor,
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                            // insert your code here.
                        }
                        @Override
                        public void onError(ImageCaptureException error) {
                            // insert your code here.
                        }
                    }
            );
        });

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
            }
        }, ContextCompat.getMainExecutor(this));
    }
    @SuppressLint("RestrictedApi")
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .addCameraFilter(cameraInfos -> {
                    List<CameraInfo> cameraInfoList = new ArrayList<>();
                    for (CameraInfo ci:cameraInfos) {
                        if(ci.getImplementationType().equals(CameraInfo.IMPLEMENTATION_TYPE_CAMERA2)) {
                            cameraInfoList.add(ci);
                        }
                    }
                    return cameraInfoList;
                })
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(executor, imageProxy -> {
            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();
            Log.e("IMAGE","Size:"+image.getWidth()+"x"+image.getHeight()+" format:"+image.getFormat());
            imageProxy.close();
        });
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector,imageAnalysis, preview);
    }
}