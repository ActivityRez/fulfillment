package com.activityrez.fulfillment.camera;

/**
 * Created by alex on 10/28/13.
 */
import android.hardware.Camera;
import android.util.Log;

public final class OpenCameraInterface {

    private static final String TAG = OpenCameraInterface.class.getName();

    private OpenCameraInterface() {
    }

    /**
     * Opens a rear-facing camera with {@link Camera#open(int)}, if one exists, or opens camera 0.
     */
    public static Camera open() {

        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            //Log.w(TAG, "No cameras!");
            return null;
        }

        int index = 0;
        while (index < numCameras) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(index, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                break;
            }
            index++;
        }

        Camera camera;
        if (index < numCameras) {
            //Log.i(TAG, "Opening camera #" + index);
            camera = Camera.open(index);
        } else {
            //Log.i(TAG, "No camera facing back; returning camera #0");
            camera = Camera.open(0);
        }

        return camera;
    }

}
