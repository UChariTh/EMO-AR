package com.example.emoar.ar;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.emoar.R;
import com.example.emoar.databinding.ActivityGraphicLabelingBinding;
import com.example.emoar.utils.EmotionUpdateListener;
//import com.google.ar.core.Anchor;
//import com.google.ar.sceneform.AnchorNode;
//import com.google.ar.sceneform.Scene;
//import com.google.ar.sceneform.rendering.ViewRenderable;
//import com.google.ar.sceneform.ux.ArFragment;

public class GraphicLabelingActivity extends AppCompatActivity implements EmotionUpdateListener {
    ActivityGraphicLabelingBinding activityGraphicLabelingBinding;
//    private ArFragment arFragment;
    private String detectedEmotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityGraphicLabelingBinding = ActivityGraphicLabelingBinding.inflate(getLayoutInflater());
        setContentView(activityGraphicLabelingBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
//
//        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
//            Anchor anchor = hitResult.createAnchor();
//            if (detectedEmotion != null) {
//                add3DText(anchor, detectedEmotion);
//            }
//        });

        // Assume detectedEmotion is being updated from another part of your code
        detectedEmotion = "Happy"; // Placeholder for the detected emotion
    }

//    private void add3DText(Anchor anchor, String text) {
//        ViewRenderable.builder()
//                .setView(this, R.layout.text_3d_view)
//                .build()
//                .thenAccept(viewRenderable -> {
//                    viewRenderable.setShadowCaster(false);
//                    viewRenderable.setShadowReceiver(false);
//
//                    TextView textView = viewRenderable.getView().findViewById(R.id.textView);
//                    textView.setText(text);
//
//                    AnchorNode anchorNode = new AnchorNode(anchor);
//                    anchorNode.setRenderable(viewRenderable);
//                    arFragment.getArSceneView().getScene().addChild(anchorNode);
//                });
//    }

    // Method to update detected emotion from other parts of the code
    public void updateDetectedEmotion(String emotion) {
        this.detectedEmotion = emotion;
    }

    @Override
    public void onEmotionDetected(String emotion) {
        updateDetectedEmotion(emotion);
    }
}
