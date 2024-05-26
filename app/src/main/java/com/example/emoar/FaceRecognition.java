package com.example.emoar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.emoar.ml.Facialemotionmodel;
import com.example.emoar.utils.BitmapUtils;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceRecognition extends AppCompatActivity {
    private static final String TAG = "FaceRecognition";
    private PreviewView previewView;
    private AROverlayView arOverlayView;
    private Button btnSwitchCam;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private Facialemotionmodel model;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);

        previewView = findViewById(R.id.previewView);
        btnSwitchCam = findViewById(R.id.btnSwitchCam);
        arOverlayView = findViewById(R.id.arOverlayView);
        back=findViewById(R.id.backbutton);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        try {
            model = Facialemotionmodel.newInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        btnSwitchCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        startCamera();
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                processImage(imageProxy);
                imageProxy.close();
            }
        });

        cameraProvider.unbindAll();
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImage(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            Bitmap bitmap = BitmapUtils.toBitmap(mediaImage);
            if (bitmap != null) {
                // Convert to grayscale
                bitmap = BitmapUtils.convertToGrayscale(bitmap);

                // Resize bitmap to 48x48
                bitmap = Bitmap.createScaledBitmap(bitmap, 48, 48, true);

                // Convert bitmap to ByteBuffer
                ByteBuffer byteBuffer = BitmapUtils.convertBitmapToByteBuffer(bitmap);

                // Prepare input tensor buffer
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 48, 48, 1}, DataType.FLOAT32);
                inputFeature0.loadBuffer(byteBuffer);

                // Run model inference
                Facialemotionmodel.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                // Get the output array
                float[] outputArray = outputFeature0.getFloatArray();

                // Define the emotion labels
                String[] emotionLabels = {"Angry", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral"};

                // Find the emotion with the highest confidence
                int maxIndex = 0;
                float maxConfidence = outputArray[0];
                for (int i = 1; i < outputArray.length; i++) {
                    if (outputArray[i] > maxConfidence) {
                        maxConfidence = outputArray[i];
                        maxIndex = i;
                    }
                }

                // Get the corresponding emotion label
                String detectedEmotion = emotionLabels[maxIndex];
                Log.d(TAG, "Detected Emotion: " + detectedEmotion);

                // Update the AROverlayView with the detected emotion
                runOnUiThread(() -> arOverlayView.setDetectedEmotion(detectedEmotion));

                // Display the detected emotion
                runOnUiThread(() -> Toast.makeText(FaceRecognition.this, "Detected Emotion: " + detectedEmotion, Toast.LENGTH_SHORT).show());
            } else {
                Log.e(TAG, "Bitmap conversion failed");
            }
        } else {
            Log.e(TAG, "Media image is null");
        }
        imageProxy.close();
    }

    private void switchCamera() {
        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        } else {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        }
        startCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (model != null) {
            model.close();
        }
    }
}
