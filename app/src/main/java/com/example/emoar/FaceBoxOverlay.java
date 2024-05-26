package com.example.emoar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class FaceBoxOverlay extends View {
    private final List<FaceBox> faceBoxes = new ArrayList<>();
    private final Paint paint;

    public FaceBoxOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setTextSize(48);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (FaceBox faceBox : faceBoxes) {
            faceBox.draw(canvas);
        }
    }

    public void clear() {
        faceBoxes.clear();
        postInvalidate();
    }

    public void add(FaceBox faceBox) {
        faceBoxes.add(faceBox);
        postInvalidate();
    }

    public static class FaceBox {
        private final Rect faceRect;
        private final String emotion;
        private final Paint paint, paintText;

        public FaceBox(Rect faceRect, String emotion) {
            this.faceRect = faceRect;
            this.emotion = emotion;

            this.paint = new Paint();
            this.paint.setColor(Color.BLUE);
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setStrokeWidth(6.0f);

            this.paintText = new Paint();
            this.paintText.setColor(Color.GREEN);
            this.paintText.setTextSize(40.0f);
            this.paintText.setTextAlign(Paint.Align.LEFT);
        }

        public void draw(Canvas canvas) {
            // Scale factors
            float scaleX = canvas.getWidth() / 480f;  // Assuming input image width is 480 pixels
            float scaleY = canvas.getHeight() / 640f; // Assuming input image height is 640 pixels

            // Adjust for mirrored coordinates
            float left = (canvas.getWidth() - faceRect.right * scaleX);
            float top = faceRect.top * scaleY;
            float right = (canvas.getWidth() - faceRect.left * scaleX);
            float bottom = faceRect.bottom * scaleY;

            RectF rectF = new RectF(left, top, right, bottom);
            canvas.drawRect(rectF, paint);
            canvas.drawText(emotion, rectF.left, rectF.top - 10, paintText);
        }
    }
}
