package com.activityrez.fulfillment.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.AuthModule;
import com.activityrez.fulfillment.camera.CameraManager;
import com.activityrez.fulfillment.events.NavStatus;
import com.activityrez.fulfillment.events.QRCodeFound;

import com.google.inject.Inject;
import com.squareup.otto.Subscribe;

import java.util.Collection;
import java.util.Iterator;

import roboguice.activity.RoboActivity;

public class MainActivity extends RoboActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Inject AuthModule auth;
    @Inject CameraManager cameraManager;

    private NavStatus.State state = NavStatus.State.DEFAULT;

    private CaptureFragment capfrag;
    private SplashFragment splashfrag;
    private SearchFragment searchfrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        ARContainer.bus.register(this);

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Log.i(TAG, "someone pushed the back button.. that faggot");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ARContainer.bus.register(this);
        if((Integer)auth.getUser().get("id") == 0){
            ARContainer.bus.post(new NavStatus(NavStatus.State.LOGIN));
        } else if(state == NavStatus.State.DEFAULT){
            ARContainer.bus.post(new NavStatus(NavStatus.State.SCANNING));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ARContainer.bus.unregister(this);
        if(capfrag != null){
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(capfrag);
            ft.commit();
            capfrag = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("arez_state",state.ordinal());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ARContainer.bus.post(new NavStatus(NavStatus.State.values()[savedInstanceState.getInt("arez_state")]));
    }

    @Subscribe public void onQRCodeFound(QRCodeFound msg){
        cameraManager.stopPreview();
    }
    @Subscribe public void onNavStatus(NavStatus n){
        Log.i("loaded state",n.state.toString());
        if(state == n.state) return;

        FragmentManager fm = getFragmentManager();
        AnimatorSet as = new AnimatorSet();
        LinearInterpolator inter = new LinearInterpolator();
        AnimatorSet.Builder bs;

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics sizer = new DisplayMetrics();
        display.getMetrics(sizer);
        int width = sizer.widthPixels;


        if(state == NavStatus.State.SCANNING){
            cameraManager.stopPreview();
            //move stuff to the left
            View cv = fm.findFragmentById(R.id.cap_fragment).getView();
            cv.setKeepScreenOn(false);
            View nv = fm.findFragmentById(R.id.nav_fragment).getView();

            ObjectAnimator ca = ObjectAnimator.ofInt(cv,"xFraction",0,-width);
            ObjectAnimator na = ObjectAnimator.ofInt(nv, "xFraction", width, 0);

            ca.setInterpolator(inter);
            na.setInterpolator(inter);

            bs = as.play(ca).with(na);

            if(n.state == NavStatus.State.LOGIN){
                View dv = fm.findFragmentById(R.id.the_splasher).getView();
                ObjectAnimator da = ObjectAnimator.ofInt(dv, "xFraction", 0, width);
                da.setInterpolator(inter);
                bs.with(da);
            } else if(n.state == NavStatus.State.SEARCHING){
                View dv = fm.findFragmentById(R.id.search_stuff).getView();
                ObjectAnimator da = ObjectAnimator.ofInt(dv, "xFraction", width, 0);
                da.setInterpolator(inter);
                bs.with(da);
            }
        } else if(n.state == NavStatus.State.SCANNING){
            //move stuff to the right
            View cv = fm.findFragmentById(R.id.cap_fragment).getView();
            cv.setKeepScreenOn(true);
            View nv = fm.findFragmentById(R.id.nav_fragment).getView();

            ObjectAnimator ca = ObjectAnimator.ofInt(cv, "xFraction", -width, 0);
            ObjectAnimator na = ObjectAnimator.ofInt(nv, "xFraction", 0, width);

            ca.setInterpolator(inter);
            na.setInterpolator(inter);

            bs = as.play(ca).with(na);

            if(state == NavStatus.State.DEFAULT){
                View dv = fm.findFragmentById(R.id.the_splasher).getView();
                ObjectAnimator da = ObjectAnimator.ofInt(dv, "xFraction", 0, width);
                da.setInterpolator(inter);
                bs.with(da);

                dv = fm.findFragmentById(R.id.search_stuff).getView();
                da = ObjectAnimator.ofInt(dv, "xFraction", 0, width);
                da.setInterpolator(inter);
                bs.with(da);
            } else if(state == NavStatus.State.LOGIN){
                View dv = fm.findFragmentById(R.id.the_splasher).getView();
                ObjectAnimator da = ObjectAnimator.ofInt(dv, "xFraction", 0, width);
                da.setInterpolator(inter);
                bs.with(da);
            } else if(state == NavStatus.State.SEARCHING){
                View dv = fm.findFragmentById(R.id.search_stuff).getView();
                ObjectAnimator da = ObjectAnimator.ofInt(dv, "xFraction", 0, width);
                da.setInterpolator(inter);
                bs.with(da);
            }

            cameraManager.startPreview();
        }

        as.setDuration(getResources().getInteger(R.integer.slide_speed));
        as.setInterpolator(inter);
        as.start();

        state = n.state;
    }
}
