package com.activityrez.fulfillment.core;

import android.content.Context;
import android.util.Log;

import com.activityrez.fulfillment.ARContainer;
import com.google.inject.Provider;

/**
 * Created by alex on 10/24/13.
 */
public class AppProvider implements Provider<ARContainer> {
    private ARContainer context;
    public AppProvider(Context c){
        context = (ARContainer) c;
    }
    @Override
    public ARContainer get() {
        return context;
    }
}
