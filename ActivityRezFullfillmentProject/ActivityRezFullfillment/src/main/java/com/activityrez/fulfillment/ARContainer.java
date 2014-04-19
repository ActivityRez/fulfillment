package com.activityrez.fulfillment;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

import com.activityrez.fulfillment.core.WrapModule;
import com.google.inject.Inject;
import com.google.inject.util.Modules;
import com.squareup.otto.Bus;

import roboguice.RoboGuice;

import com.testflightapp.lib.TestFlight;

/**
 * Created by alex on 10/14/13.
 */

public class ARContainer extends Application {
    public static Context context;
    public static final Bus bus = new Bus();

    @Inject public ARContainer(){ super(); }

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE, Modules.override(RoboGuice.newDefaultRoboModule(this)).with(new WrapModule(this)));

        // typeface caching
        initializeTypefaces();

        TestFlight.takeOff(this,"44e8206b-a28f-4a62-9451-d6d48a5ff81d");
    }

    public static class Fonts {
        public static Typeface MAIN;
    }

    private void initializeTypefaces(){
        Fonts.MAIN   = Typeface.createFromAsset(getAssets(), "helvetica.otf");
    }
}
