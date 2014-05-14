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
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

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
    private RelativeLayout mainLayout;
    InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        inputMethodManager = (InputMethodManager) ARContainer.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mainLayout = (RelativeLayout) findViewById(R.id.activity_wrapper);

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

        searchfrag = (SearchFragment) getFragmentManager().findFragmentById((R.id.search_stuff));

    }

    @Override
    protected void onPause() {
        super.onPause();

        if( searchfrag != null ) {
            getFragmentManager().beginTransaction().remove(searchfrag).commit();
            searchfrag = null;
            Log.i("called","destroyed SearchFragment");
        }

        ARContainer.bus.unregister(this);

        if(capfrag != null){
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(capfrag);
            ft.commit();
            capfrag = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        return true;
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
        Log.i("old state",state.toString());
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
                ObjectAnimator da = ObjectAnimator.ofInt(dv, "xFraction", width, 0);
                da.setInterpolator(inter);
                bs.with(da);
            } else if(n.state == NavStatus.State.SEARCHING){
                View dv = fm.findFragmentById(R.id.search_stuff).getView();
                ObjectAnimator da = ObjectAnimator.ofInt(dv, "xFraction", width, 0);
                da.setInterpolator(inter);
                bs.with(da);
            } else if(n.state == NavStatus.State.DEFAULT){
                //move to default
            }
        } else if(n.state == NavStatus.State.SCANNING){
            //move stuff to the right
            View cv = fm.findFragmentById(R.id.cap_fragment).getView();
            cv.setKeepScreenOn(true);
            View nv = fm.findFragmentById(R.id.nav_fragment).getView();

            InputMethodManager mgr = (InputMethodManager) ARContainer.context.getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(cv.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

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
                Log.i("Login","after log out?");
                View dv = fm.findFragmentById(R.id.the_splasher).getView();
                ObjectAnimator da = ObjectAnimator.ofInt(dv, "xFraction", 0, width);
                da.setInterpolator(inter);
                bs.with(da);
            } else if(state == NavStatus.State.SEARCHING){

                EditText nam = (EditText)nv.findViewById(R.id.name_search);
                EditText ph = (EditText)nv.findViewById(R.id.phone_search);
                EditText em = (EditText)nv.findViewById(R.id.email_search);
                EditText cr = (EditText)nv.findViewById(R.id.credit_search);
                EditText sid = (EditText)nv.findViewById(R.id.sale_id_search);
                nam.getEditableText().clear();
                ph.getEditableText().clear();
                em.getEditableText().clear();
                cr.getEditableText().clear();
                sid.getEditableText().clear();

                View dv = fm.findFragmentById(R.id.search_stuff).getView();
                ObjectAnimator da = ObjectAnimator.ofInt(dv, "xFraction", 0, width);
                da.setInterpolator(inter);
                bs.with(da);
            }

            cameraManager.startPreview();
        } else if(n.state == NavStatus.State.DEFAULT){
            if(state == NavStatus.State.LOGIN){
                //hide login
            } else if(state == NavStatus.State.SEARCHING){
                View sv = fm.findFragmentById(R.id.search_stuff).getView();
                //hide sv
            }
            View dv = fm.findFragmentById(R.id.the_splasher).getView();
            //show default
        } else if(n.state == NavStatus.State.SEARCHING){
            if(state == NavStatus.State.DEFAULT){
                //hide default
            } else if(state == NavStatus.State.LOGIN){
                //hide login
            }
            //show searching
        } else if(n.state == NavStatus.State.LOGIN){

            //replace and move stuff to the right
            View sv = fm.findFragmentById(R.id.the_splasher).getView();
            sv.setKeepScreenOn(true);
            View nv = fm.findFragmentById(R.id.nav_fragment).getView();

            ObjectAnimator ca = ObjectAnimator.ofInt(nv, "xFraction", -width, 0);
            ObjectAnimator na = ObjectAnimator.ofInt(sv, "xFraction", 0, width);
            ca.setInterpolator(inter);
            na.setInterpolator(inter);
            bs = as.play(ca).with(na);

            if(state == NavStatus.State.SEARCHING){
                EditText nam = (EditText)nv.findViewById(R.id.name_search);
                EditText ph = (EditText)nv.findViewById(R.id.phone_search);
                EditText em = (EditText)nv.findViewById(R.id.email_search);
                EditText cr = (EditText)nv.findViewById(R.id.credit_search);
                EditText sid = (EditText)nv.findViewById(R.id.sale_id_search);
                nam.getEditableText().clear();
                ph.getEditableText().clear();
                em.getEditableText().clear();
                cr.getEditableText().clear();
                sid.getEditableText().clear();
            }

            View dv = fm.findFragmentById(R.id.the_splasher).getView();
            ObjectAnimator da = ObjectAnimator.ofInt(dv, "xFraction", -width, 0);
            da.setInterpolator(inter);
            bs.with(da);
        }

        as.setDuration(getResources().getInteger(R.integer.slide_speed));
        as.setInterpolator(inter);
        as.start();

        state = n.state;
    }
}
