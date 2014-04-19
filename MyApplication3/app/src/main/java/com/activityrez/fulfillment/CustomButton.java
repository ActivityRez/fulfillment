package com.activityrez.fulfillment;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by alex on 10/14/13.
 */
public class CustomButton extends Button {
    public CustomButton(Context context) {
        super(context);
        setTypeface(ARContainer.Fonts.MAIN);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(ARContainer.Fonts.MAIN);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(ARContainer.Fonts.MAIN);
    }
}
