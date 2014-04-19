package com.activityrez.fulfillment;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by alex on 10/14/13.
 */
public class CustomText extends TextView {
    public CustomText(Context context) {
        super(context);
        setTypeface(ARContainer.Fonts.MAIN);
    }

    public CustomText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(ARContainer.Fonts.MAIN);
    }

    public CustomText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(ARContainer.Fonts.MAIN);
    }
}
