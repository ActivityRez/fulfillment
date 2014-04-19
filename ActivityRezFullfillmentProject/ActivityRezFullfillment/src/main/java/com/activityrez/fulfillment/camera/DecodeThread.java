package com.activityrez.fulfillment.camera;

/**
 * Created by alex on 10/28/13.
 */
import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.events.QRCodePointFound;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class DecodeThread extends Thread {
    public static final String BARCODE_BITMAP = "barcode_bitmap";
    public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";

    private final Map<DecodeHintType,Object> hints;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    private static ResultPoint mResult;
    private final Handler handle = new Handler(ARContainer.context.getMainLooper());
    private final Runnable runner = new Runnable() {
        @Override
        public void run() {
            ARContainer.bus.post(new QRCodePointFound(mResult));
        }
    };

    DecodeThread(Collection<BarcodeFormat> decodeFormats,
                 Map<DecodeHintType,?> baseHints,
                 String characterSet) {

        handlerInitLatch = new CountDownLatch(1);

        hints = new EnumMap<DecodeHintType,Object>(DecodeHintType.class);
        if (baseHints != null) {
            hints.putAll(baseHints);
        }

        // The prefs can't change while the thread is running, so pick them up once here.
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        if (characterSet != null) {
            hints.put(DecodeHintType.CHARACTER_SET, characterSet);
        }
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, new ResultPointCallback() {
            @Override
            public void foundPossibleResultPoint(ResultPoint resultPoint) {
                mResult = resultPoint;
                handle.post(runner);
            }
        });
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(hints);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}
