package com.activityrez.fulfillment.views;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.events.QRCodePointFound;
import com.google.inject.Inject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.activityrez.fulfillment.camera.CameraManager;
import com.squareup.otto.Subscribe;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {
    @Inject CameraManager cameraManager;

    private static final int CURRENT_POINT_OPACITY = 0x10;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 30;

    private final Paint paint;
    private final int maskColor;
    private final int resultPointColor;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;

    private int rotation = 0;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()){
            rotation = ((WindowManager)ARContainer.context.getSystemService(ARContainer.context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();

            RoboGuice.getInjector(ARContainer.context).injectMembers(this);
            ARContainer.bus.register(this);
        }

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        possibleResultPoints = new ArrayList<ResultPoint>(5);
        lastPossibleResultPoints = null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }

        Rect frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }

        int line_width = 30;
        int line_length = 50;

        paint.setColor(maskColor);
        //top left corner
        canvas.drawRect(frame.left - line_width,frame.top - line_width,frame.left+line_length,frame.top,paint);
        canvas.drawRect(frame.left - line_width,frame.top,frame.left,frame.top+line_length,paint);

        //top right corner
        canvas.drawRect(frame.right-line_length,frame.top-line_width,frame.right+line_width,frame.top,paint);
        canvas.drawRect(frame.right,frame.top,frame.right+line_width,frame.top+line_length,paint);

        //bottom right corner
        canvas.drawRect(frame.right-line_length,frame.bottom+line_width,frame.right+line_width,frame.bottom,paint);
        canvas.drawRect(frame.right,frame.bottom,frame.right+line_width,frame.bottom-line_length,paint);

        //bottom left corner
        canvas.drawRect(frame.left - line_width,frame.bottom + line_width,frame.left+line_length,frame.bottom,paint);
        canvas.drawRect(frame.left - line_width,frame.bottom,frame.left,frame.bottom-line_length,paint);

        float scaleX = frame.width() / (float) previewFrame.width();
        float scaleY = frame.height() / (float) previewFrame.height();

        List<ResultPoint> currentPossible = possibleResultPoints;
        List<ResultPoint> currentLast = lastPossibleResultPoints;
        int frameLeft = frame.left;
        int frameTop = frame.top;

        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new ArrayList<ResultPoint>(5);
            lastPossibleResultPoints = currentPossible;
            paint.setAlpha(CURRENT_POINT_OPACITY);
            paint.setColor(resultPointColor);
            switch(rotation){
                case Surface.ROTATION_0:
                    canvas.rotate(90f, canvas.getWidth()/2f, canvas.getHeight()/2f);
                    break;
                case Surface.ROTATION_180:
                    canvas.rotate(270f, canvas.getWidth()/2f, canvas.getHeight()/2f);
                    break;
                case Surface.ROTATION_270:
                    canvas.rotate(180f, canvas.getWidth()/2f, canvas.getHeight()/2f);
                    break;
            }
            synchronized (currentPossible) {
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            POINT_SIZE, paint);
                }
            }
        }
        if (currentLast != null) {
            paint.setAlpha(CURRENT_POINT_OPACITY / 3);
            paint.setColor(resultPointColor);
            synchronized (currentLast) {
                float radius = POINT_SIZE * 0.8f;
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            radius, paint);
                }
            }
        }
    }

    @Subscribe public void onFoundPoint(QRCodePointFound point){
        List<ResultPoint> points = possibleResultPoints;

        synchronized (points) {
            points.add(point.point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
            invalidate();
        }
    }
}
