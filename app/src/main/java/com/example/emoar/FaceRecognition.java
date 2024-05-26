package com.example.emoar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.emoar.databinding.ActivityFaceRecognitionBinding;
import com.example.emoar.ml.Facialemotionmodel;
import com.example.emoar.utils.BitmapUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class FaceRecognition extends AppCompatActivity {
    private static final String TAG = "FaceRecognition";
    private ActivityFaceRecognitionBinding binding;
    private PreviewView previewView;
    private ImageView back;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
    private ImageAnalysis imageAnalysis;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Facialemotionmodel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFaceRecognitionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        previewView = binding.previewView;
        back = binding.backbutton;

        back.setOnClickListener(view -> {
            finish();
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        binding.btnSwitchCam.setOnClickListener(v -> switchCamera());

        try {
            model = Facialemotionmodel.newInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        startCamera();
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build();
        FaceDetector detector = FaceDetection.getClient(options);

        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageProxy -> {
            processImageProxy(detector, imageProxy);
        });

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(FaceDetector detector, ImageProxy imageProxy) {
        @androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        if (planes.length > 0) {
            InputImage image = InputImage.fromMediaImage(Objects.requireNonNull(imageProxy.getImage()), imageProxy.getImageInfo().getRotationDegrees());
            detector.process(image)
                    .addOnSuccessListener(faces -> {
                        binding.arOverlayView.clear();
                        if (!faces.isEmpty()) {
                            Rect faceRect = faces.get(0).getBoundingBox();
                            String emotion = detectEmotion(faces.get(0), imageProxy);
                            FaceBoxOverlay.FaceBox faceBox = new FaceBoxOverlay.FaceBox(faceRect, emotion);
                            binding.arOverlayView.add(faceBox);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Face detection failed: " + e))
                    .addOnCompleteListener(task -> imageProxy.close());
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private String detectEmotion(Face face, ImageProxy imageProxy) {
        Bitmap bitmap = BitmapUtils.toBitmap(imageProxy.getImage());
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
            return emotionLabels[maxIndex];
        }
        return "Unknown";
    }

    private void switchCamera() {
        cameraSelector = (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
                ? CameraSelector.DEFAULT_BACK_CAMERA
                : CameraSelector.DEFAULT_FRONT_CAMERA;
        bindPreview();
    }
}
