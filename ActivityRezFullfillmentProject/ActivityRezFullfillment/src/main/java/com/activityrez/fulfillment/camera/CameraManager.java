package com.activityrez.fulfillment.camera;

/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.activityrez.fulfillment.ARContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.zxing.PlanarYUVLuminanceSource;

import java.io.IOException;
import java.util.List;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
@Singleton
public final class CameraManager {
    private static final String TAG = CameraManager.class.getSimpleName();

    private boolean hasSurface = false;
    private SurfaceHolder surface;

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
    private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080

    private final CameraConfigurationManager configManager;
    private CaptureActivityHandler handler;

    private Camera camera;
    private AutoFocusManager autoFocusManager;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;
    private int requestedFramingRectWidth;
    private int requestedFramingRectHeight;

    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private final PreviewCallback previewCallback;

    @Inject
    public CameraManager() {
        this.configManager = new CameraConfigurationManager(ARContainer.context);
        previewCallback = new PreviewCallback(configManager);
    }

    public void init(SurfaceHolder surfaceHolder){
        //Log.i("lifecycle","init called");
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if(camera != null) {
            //Log.w("CAMERA ACTIVITY", "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        surface = surfaceHolder;
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //Log.i("lifecycle","surface created");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                //Log.i("lifecycle","surface changed");
                framingRect = null;
                framingRectInPreview = null;
                if(hasSurface) return;
                hasSurface = true;
                try {
                    openDriver();
                    if (handler == null)
                        handler = new CaptureActivityHandler();
                } catch (IOException ioe) {
                   // Log.w(TAG, ioe);
                } catch (RuntimeException e) {
                   // Log.w(TAG, "Unexpected error initializing camera", e);
                }

                AmbientLightManager alm = new AmbientLightManager();
                alm.start();

                startPreview();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //Log.i("lifecycle","surface destroyed");

                if (camera != null) {
                    camera.stopPreview();
                    camera.setPreviewCallback(null);
                    camera.release();
                    camera = null;
                }

                hasSurface = false;
                stopPreview();
                closeDriver();
            }
        });
    }

    public void rotate(){
        if(camera == null)
            return;

        Camera.Parameters parameters = camera.getParameters();
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0,info);

        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size cs = sizes.get(0);
        int width = cs.width;
        int height = cs.height;

        int rotation = ((WindowManager)ARContainer.context.getSystemService(ARContainer.context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch(info.orientation){
            case 90:
                rotation = (rotation + Surface.ROTATION_90)%4;
                break;
            case 180:
                rotation = (rotation + Surface.ROTATION_180)%4;
                break;
            case 270:
                rotation = (rotation + Surface.ROTATION_270)%4;
                break;
        }
/*
        //if(isActive)
        //    stopPreview();
        if(rotation == Surface.ROTATION_90){
            camera.setDisplayOrientation(90);
            parameters.setPreviewSize(height, width);
        } else if(rotation == Surface.ROTATION_0){
//            camera.setDisplayOrientation(0);
            camera.setDisplayOrientation(180);
            parameters.setPreviewSize(width, height);
        } else if(rotation == Surface.ROTATION_270){
            camera.setDisplayOrientation(270);
            parameters.setPreviewSize(width, height);
        } else if(rotation == Surface.ROTATION_180){
//            camera.setDisplayOrientation(180);
            camera.setDisplayOrientation(0);
            parameters.setPreviewSize(height, width);
        }
*/
        camera.setParameters(parameters);
        //if(isActive)
        //    startPreview();
    }

    public Handler getHandler(){ return this.handler; }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @throws IOException Indicates the camera driver failed to open.
     */
    public synchronized void openDriver() throws IOException {
        Camera theCamera = camera;
        if (theCamera == null) {
            theCamera = OpenCameraInterface.open();
            if (theCamera == null) {
                throw new IOException();
            }
            camera = theCamera;
            rotate();
        }
        theCamera.setPreviewDisplay(surface);

        if (!initialized) {
            initialized = true;
            configManager.initFromCameraParameters(theCamera);
            if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
                setManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight);
                requestedFramingRectWidth = 0;
                requestedFramingRectHeight = 0;
            }
        }

        Camera.CameraInfo beans = new Camera.CameraInfo();
        Camera.getCameraInfo(0,beans);

        Camera.Parameters parameters = theCamera.getParameters();
        String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save these, temporarily
        try {
            configManager.setDesiredCameraParameters(theCamera, false);
        } catch (RuntimeException re) {
            // Driver failed
            //Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
            //Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
            // Reset:
            if (parametersFlattened != null) {
                parameters = theCamera.getParameters();
                parameters.unflatten(parametersFlattened);
                try {
                    theCamera.setParameters(parameters);
                    configManager.setDesiredCameraParameters(theCamera, true);
                } catch (RuntimeException re2) {
                    // Well, darn. Give up
                    //Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
                }
            }
        }

    }

    public synchronized boolean isOpen() {
        return camera != null;
    }

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        if (camera != null) {
            camera.release();
            camera = null;
            // Make sure to clear these each time we close the camera, so that any scanning rect
            // requested by intent is forgotten.
            framingRect = null;
            framingRectInPreview = null;
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public synchronized void startPreview() {
        //Log.i("lifecycle","preview started [" + (camera == null?"null":"not null") + "]");
        if(camera == null) return;
        if(!previewing){

            camera.startPreview();
            previewing = true;

            autoFocusManager = new AutoFocusManager(ARContainer.context, camera);
        }
        if(handler != null){
            handler.full_retard();
        }
        //Log.i("preview","start called [" + (handler == null?"without":"with") + " handle]");
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopPreview() {
        //Log.i("lifecycle","preview stopped [" + (camera == null?"null":"not null") + "]");
        if (autoFocusManager != null) {
            autoFocusManager.stop();
            autoFocusManager = null;
        }
        if(camera != null && previewing) {
            camera.stopPreview();
            previewing = false;
        }
        //Log.i("preview","stop called");
    }

    public synchronized void setTorch(boolean newSetting) {
        if (newSetting != configManager.getTorchState(camera)) {
            if (camera != null) {
                if (autoFocusManager != null) {
                    autoFocusManager.stop();
                }
                configManager.setTorch(camera, newSetting);
                if (autoFocusManager != null) {
                    autoFocusManager.start();
                }
            }
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    public synchronized void requestPreviewFrame(Handler handler, int message) {
        Camera theCamera = camera;
        if (theCamera != null && previewing) {
            previewCallback.setHandler(handler, message);
            theCamera.setOneShotPreviewCallback(previewCallback);
        }
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public synchronized Rect getFramingRect() {
        if (framingRect == null) {
            if (camera == null) {
                return null;
            }
            //Point screenResolution = configManager.getScreenResolution();
            Rect r = surface.getSurfaceFrame();
            Point screenResolution = new Point(r.width(),r.height());

            int width = findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
            int height = findDesiredDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);

            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            //Log.d(TAG, "Calculated framing rect: " + framingRect);
        }
        return framingRect;
    }

    private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
        int dim = 5 * resolution / 8; // Target 5/8 of each dimension
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     */
    public synchronized Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect framingRect = getFramingRect();
            if (framingRect == null) {
                return null;
            }
            Rect rect = new Rect(framingRect);
            Point cameraResolution = configManager.getCameraResolution();
            Rect r = surface.getSurfaceFrame();
            Point screenResolution = new Point(r.width(),r.height());
            if (cameraResolution == null || screenResolution == null) {
                // Called early, before init even finished
                return null;
            }
            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }

    /**
     * Allows third party apps to specify the scanning rectangle dimensions, rather than determine
     * them automatically based on screen resolution.
     *
     * @param width The width in pixels to scan.
     * @param height The height in pixels to scan.
     */
    public synchronized void setManualFramingRect(int width, int height) {
        if (initialized) {
            Point screenResolution = configManager.getScreenResolution();
            if (width > screenResolution.x) {
                width = screenResolution.x;
            }
            if (height > screenResolution.y) {
                height = screenResolution.y;
            }
            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            //Log.d(TAG, "Calculated manual framing rect: " + framingRect);
            framingRectInPreview = null;
        } else {
            requestedFramingRectWidth = width;
            requestedFramingRectHeight = height;
        }
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview();
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), false);
    }

    public void grab(Camera.PictureCallback callback){
        //Log.i("lifecycle","grab called [" + (camera == null?"no":"with") + " camera]");
        final Camera.PictureCallback _callback = callback;
        if(camera == null){
            surface.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    camera.takePicture(null,null,_callback);
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    if (camera != null) {
                        camera.stopPreview();
                        camera.setPreviewCallback(null);
                        camera.release();
                        camera = null;
                    }
                }

            });
        } else {
            camera.takePicture(null,_callback,null);
        }
    }

}
