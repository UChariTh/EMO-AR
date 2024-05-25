package com.example.emoar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class AROverlayView extends View {
    private Paint paint;
    private String detectedEmotion = "";

    public AROverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AROverlayView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(100);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
    }

    public void setDetectedEmotion(String emotion) {
        this.detectedEmotion = emotion;
        invalidate(); // Redraw the view with the new text
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!detectedEmotion.isEmpty()) {
            canvas.drawText(detectedEmotion, 50, 150, paint);
        }
    }
}
