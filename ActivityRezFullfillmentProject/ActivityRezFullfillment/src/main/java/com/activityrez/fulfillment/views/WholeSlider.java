package com.activityrez.fulfillment.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.camera.CameraManager;
import com.google.inject.Inject;

import roboguice.RoboGuice;

/**
 * Created by alex on 10/31/13.
 */
public class WholeSlider extends SlidingRelative {
    public WholeSlider(Context context){
        super(context);
    }

    public WholeSlider(Context context, AttributeSet attr){
        super(context,attr);
    }

    @Override
    public void setXFraction(int xf) {
        mXFraction = xf;
        setX((mWidth > 0) ? xf : 0);
    }
}
