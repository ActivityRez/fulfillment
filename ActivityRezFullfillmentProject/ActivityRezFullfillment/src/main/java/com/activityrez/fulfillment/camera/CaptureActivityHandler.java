package com.activityrez.fulfillment.camera;

/**
 * Created by alex on 10/28/13.
 */
import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.events.QRCodeFound;
import com.google.inject.Inject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.activityrez.fulfillment.R;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Collection;
import java.util.Map;

import roboguice.RoboGuice;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

    private static final String TAG = CaptureActivityHandler.class.getSimpleName();

    private final DecodeThread decodeThread;
    private State state;
    @Inject private CameraManager cameraManager;

    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    public CaptureActivityHandler(){
        this(DecodeFormatManager.QR_CODE_FORMATS, null, null);
    }
    public CaptureActivityHandler(Collection<BarcodeFormat> decodeFormats,
                           Map<DecodeHintType,?> baseHints,
                           String characterSet) {

        RoboGuice.getInjector(ARContainer.context).injectMembers(this);

        decodeThread = new DecodeThread(decodeFormats, baseHints, characterSet);
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
    }

    @Override
    public void handleMessage(Message message) {
        switch(message.what){
            case R.id.restart_preview:
                restartPreviewAndDecode();
                break;
            case R.id.decode_succeeded:
                state = State.SUCCESS;
                ARContainer.bus.post(new QRCodeFound(message.obj.toString()));
                break;
            case R.id.decode_failed:
                state = State.PREVIEW;
                cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
                break;
            case R.id.return_scan_result:
                break;
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    public void full_retard(){
        state = State.PREVIEW;
        cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
    }

    public void restartPreviewAndDecode() {
        //Log.i(TAG,state == State.PREVIEW?"in preview":"not in preview");
        if (state != State.PREVIEW) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
        }
    }
}
