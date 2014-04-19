package com.activityrez.fulfillment.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.activityrez.fulfillment.ARContainer;

/**
 * Created by alex on 11/7/13.
 */
public class LeftSlider extends SlidingRelative {
    public LeftSlider(Context context){
        super(context);
    }

    public LeftSlider(Context context, AttributeSet attr){
        super(context,attr);
    }

    @Override
    public void setXFraction(int xf) {
        Display display = ((WindowManager)ARContainer.context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics sizer = new DisplayMetrics();
        display.getMetrics(sizer);

        if(mWidth == 0){
            setX(0);
        } else if(xf > -(280/ sizer.densityDpi)){
            setX(0);
        } else {
            setX(xf);
        }
        mXFraction = xf;
    }
}
