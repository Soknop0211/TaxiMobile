package com.fxn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.fxn.pix.R;


public class CropOverlayView extends View {

    /**
     * The Paint used to darken the surrounding areas outside the crop area.
     */
    private Paint mBackgroundPaint;

    public CropOverlayView(Context context) {
        super(context);
        init(context);
    }

    RectF rect = new RectF(0, 0, 100, 100);

    /**
     * Used for oval crop window shape or non-straight rotation drawing.
     */
    private Path mPath = new Path();

    private float clipRectRight = getResources().getDimension(R.dimen.clipRectRight);
    private float clipRectBottom = getResources().getDimension(R.dimen.clipRectBottom);
    private float clipRectTop = getResources().getDimension(R.dimen.clipRectTop);
    private float clipRectLeft = getResources().getDimension(R.dimen.clipRectLeft);

    private float rectCorner = getResources().getDimension(R.dimen.rectCorner);
    private float boundaryMerging = getResources().getDimension(R.dimen.boundaryMerging);
    private float boundaryLength = getResources().getDimension(R.dimen.boundaryLength);
    private float boundaryStroke = getResources().getDimension(R.dimen.boundaryStroke);

    private float rectInset = getResources().getDimension(R.dimen.rectInset);
    private float smallRectOffset = getResources().getDimension(R.dimen.smallRectOffset);

    private float circleRadius = getResources().getDimension(R.dimen.circleRadius);
    private float textOffset = getResources().getDimension(R.dimen.textOffset);
    private float textSize = getResources().getDimension(R.dimen.textSize);


    private void init(Context context) {
        mBackgroundPaint = getNewPaint(Color.argb(119, 0, 0, 0));

        mPath.lineTo(0, 100);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(getResources().getDimension(R.dimen.strokeWidth));
        paint.setTextSize(getResources().getDimension(R.dimen.textSize));

    }

    /**
     * Creates the Paint object for drawing.
     */
    private static Paint getNewPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        return paint;
    }

    public CropOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
    }

    public void cropZone(float left, float top, float right, float bottom, float x, float y) {

    }

    public float getWidthRatio() {
        return width / height;
    }

    float width = 0f;
    float height = 0f;

    private void drawTransparentArea(Canvas canvas) {
        width = canvas.getWidth();
        height = canvas.getHeight();

        //Ratio of width
//        float ratio = width / height;
        //Todo find other width size
        //Height x ratio => new width
        //Crop Width zone 90% of canvas
        //Crop Height zone width x 1.4f
        //Crop margin new width - cropWidth
        //Margin top Margin * 2

        float rectWidth = width * 0.90f;
        float rectHeight = rectWidth * 1.4f;
        float gapWidth = (float) (width * 0.1);
        float gapHeight = (float) (width * marginTop);
        mPath.addRoundRect(gapWidth, gapHeight, rectWidth, rectHeight, rectCorner, rectCorner, Path.Direction.CCW);
    }

    private Path bPath = new Path();

    private void drawBoundaries(Canvas canvas) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();

        float rectWidth = width * 0.90f;
        float rectHeight = rectWidth * 1.4f;
        float gapWidth = width - rectWidth;
        float gapHeight = (float) (width * marginTop);

        //Todo draw boundaries
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(boundaryStroke);
        paint.setAntiAlias(true);

        float length = boundaryLength;
        float corner = rectCorner;
        float left = gapWidth - boundaryMerging;
        float top = gapHeight - boundaryMerging;

        float right = rectWidth + boundaryMerging;
        float bottom = rectHeight + boundaryMerging;

        bPath.rewind();
        // Top-Left corner..
        bPath.moveTo(left, top + length);
        bPath.lineTo(left, top + corner);
        bPath.cubicTo(left, top + corner, left, top, left + corner, top);
        bPath.lineTo(left + length, top);

        // Top-Right corner..
        bPath.moveTo(right - length, top);
        bPath.lineTo(right - corner, top);
        bPath.cubicTo(right - corner, top, right, top, right, top + corner);
        bPath.lineTo(right, top + length);

        // Bottom-Right corner..
        bPath.moveTo(right, bottom - length);
        bPath.lineTo(right, bottom - corner);
        bPath.cubicTo(right, bottom - corner, right, bottom, right - corner, bottom);
        bPath.lineTo(right - length, bottom);

        // Bottom-Left corner..
        bPath.moveTo(left + length, bottom);
        bPath.lineTo(left + corner, bottom);
        bPath.cubicTo(left + corner, bottom, left, bottom, left, bottom - corner);
        bPath.lineTo(left, bottom - length);

        canvas.drawPath(bPath, paint);

    }

    private void drawBackground(Canvas canvas) {
        canvas.save();
        mPath.rewind();
        drawTransparentArea(canvas);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            canvas.clipPath(mPath, Region.Op.DIFFERENCE);
        } else {
            canvas.clipOutPath(mPath);
        }
        mPath.close();
        canvas.drawRect(0, 0, getWidth(), getHeight(), mBackgroundPaint);
        drawBoundaries(canvas);
        canvas.restore();


    }

    private Paint paint = new Paint();

    private void drawClippedRectangle(Canvas canvas) {
        // Set the boundaries of the clipping rectangle for the whole picture.
        canvas.clipRect(
                clipRectLeft, clipRectTop,
                clipRectRight, clipRectBottom
        );
        // Fill the canvas with white.
        // With the clipped rectangle, this only draws
        // inside the clipping rectangle.
        // The rest of the surface remains gray.
        canvas.drawColor(Color.WHITE);
        // Change the color to red and
        // draw a line inside the clipping rectangle.
        paint.setColor(Color.RED);
        canvas.drawLine(
                clipRectLeft, clipRectTop,
                clipRectRight, clipRectBottom, paint
        );
        // Set the color to green and
        // draw a circle inside the clipping rectangle.
        paint.setColor(Color.GREEN);
        canvas.drawCircle(
                circleRadius, clipRectBottom - circleRadius,
                circleRadius, paint
        );
        // Set the color to blue and draw text aligned with the right edge
        // of the clipping rectangle.
        paint.setColor(Color.BLUE);
        // Align the RIGHT side of the text with the origin.
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.RIGHT);
//        canvas.drawText(
//                context.getString(R.string.clipping),
//                clipRectRight, textOffset, paint);
    }

    private float marginTop;
    public void setMarginTop(float marginTopArg) {
        marginTop = marginTopArg;
    }
}
