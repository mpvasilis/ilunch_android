
package com.alexmodis.bestbeforeapp.ExpiryDateScannerUtil;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.alexmodis.bestbeforeapp.ExpiryDateScannerUtil.common.GraphicOverlay;
import com.google.firebase.ml.vision.text.FirebaseVisionText;


public class TextGraphic extends GraphicOverlay.Graphic {

    private static final int TEXT_COLOR = Color.WHITE;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;

    private final Paint rectPaint;
    private final Paint ftextPaint;
    private final FirebaseVisionText.Element ftext;

    TextGraphic(GraphicOverlay overlay, FirebaseVisionText.Element ftext) {
        super(overlay);

        this.ftext = ftext;

        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        ftextPaint = new Paint();
        ftextPaint.setColor(TEXT_COLOR);
        ftextPaint.setTextSize(TEXT_SIZE);
    }

    @Override
    public void draw(Canvas canvas) {
        if (ftext == null) {
            throw new IllegalStateException("Attempting to draw a null ftext.");
        }

        // Draws the bounding box around the ftextBlock.
        RectF rect = new RectF(ftext.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        canvas.drawRect(rect, rectPaint);

        canvas.drawText(ftext.getText(), rect.left, rect.bottom, ftextPaint);
    }

    public Rect getActualBounds(FirebaseVisionText.Element ftext) {
        Rect rect = new Rect(ftext.getBoundingBox());
        rect.left = (int) translateX(rect.left);
        rect.top = (int) translateY(rect.top);
        rect.right = (int) translateX(rect.right);
        rect.bottom = (int) translateY(rect.bottom);

        return rect;
    }
}
