package com.example.calorieguard;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

public class OverlayDrawable extends Drawable {
    private Paint paint;
    private Context context;
    private int cornerSize;

    public OverlayDrawable(Context context) {
        this.paint = new Paint();
        this.paint.setColor(Color.WHITE);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(5); // Adjust the stroke width as needed
        this.context=context;

        // Get the width of the screen
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        cornerSize = display.getWidth()-500;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();

        // Draw the transparent overlay
        paint.setColor(Color.parseColor("#00000000")); // 100% transparent
        canvas.drawRect(bounds, paint);

        // Draw the four L-shaped corners with a solid white stroke
        int customColor = ContextCompat.getColor(context, R.color.transparentWhite);
        paint.setColor(customColor);
        paint.setStyle(Paint.Style.FILL);

        int left = (bounds.left + bounds.right - cornerSize) / 2;// Adjusted for center
        int top = (bounds.top + bounds.bottom - cornerSize) / 2;// Adjusted for center
        int bottom = (bounds.top + bounds.bottom + cornerSize) / 2; // Adjusted for center
        int right = (bounds.left + bounds.right + cornerSize) / 2; // Adjusted for center

// Draw the top-left corner
        canvas.drawRect(left, top, left + (int)(cornerSize/5), top + 20, paint);
        canvas.drawRect(left, top, left + 20, top + (int)(cornerSize/5), paint);

// Draw the top-right corner
        canvas.drawRect(right - (int)(cornerSize/5), top, right, top + 20, paint);
        canvas.drawRect(right - 20, top, right, top + (int)(cornerSize/5), paint);

// Draw the bottom-left corner
        canvas.drawRect(left, bottom - 20, left + (int)(cornerSize/5), bottom, paint);
        canvas.drawRect(left, bottom - (int)(cornerSize/5), left + 20, bottom, paint);

// Draw the bottom-right corner
        canvas.drawRect(right - (int)(cornerSize/5), bottom - 20, right, bottom, paint);
        canvas.drawRect(right - 20, bottom - (int)(cornerSize/5), right, bottom, paint);

    }

    @Override
    public void setAlpha(int alpha) {
        // Not needed for this example
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        // Not needed for this example
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN; // Not needed for this example
    }
}
