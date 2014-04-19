package com.activityrez.fulfillment.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Created by alex on 10/30/13.
 */
public class SlidingRelative extends RelativeLayout {
    protected int mXFraction = 0;
    protected int mHeight;
    protected int mWidth;
    protected int mSelfWidth;

    public SlidingRelative(Context context){
        super(context);
    }
    public SlidingRelative(Context context, AttributeSet attr){
        super(context,attr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mSelfWidth = w;

        Display d = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics sizer = new DisplayMetrics();
        d.getMetrics(sizer);

        mHeight = sizer.heightPixels;
        mWidth = sizer.widthPixels;
    }

    public float getXFraction(){ return mXFraction; }
    public void setXFraction(int xf){
        mXFraction = xf;
        if(mWidth == 0){
            setX(0);
        } else if(xf > mWidth - mSelfWidth){
            setX(mWidth-mSelfWidth);
        } else {
            setX(xf);
        }
    }
}
