package com.activityrez.fulfillment.activities;

import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.camera.CameraManager;
import com.google.inject.Inject;

import roboguice.RoboGuice;

/**
 * Created by alex on 10/30/13.
 */
public class CaptureFragment extends Fragment {
    @Inject CameraManager cameraManager;
    View mV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(ARContainer.context).injectMembers(this);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraManager.startPreview();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mV = inflater.inflate(R.layout.capture,container,false);

        SurfaceView surfaceView = (SurfaceView) mV.findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        cameraManager.init(surfaceHolder);

        return mV;
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraManager.stopPreview();
    }
}
