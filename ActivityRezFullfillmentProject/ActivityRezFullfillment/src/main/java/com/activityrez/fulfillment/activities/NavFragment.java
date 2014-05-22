package com.activityrez.fulfillment.activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.core.Model;
import com.activityrez.fulfillment.events.NavStatus;
import com.activityrez.fulfillment.events.QRCodeFound;
import com.activityrez.fulfillment.models.NavState;
import com.activityrez.fulfillment.views.NavView;
import com.squareup.otto.Subscribe;

/**
 * Created by alex on 10/30/13.
 */
public class NavFragment extends Fragment {
    private NavView navView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ARContainer.bus.register(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        ARContainer.bus.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.menubar,container);
        navView = new NavView(v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ARContainer.bus.register(this);
        NavState ns = (NavState) navView.getModel();
        ARContainer.bus.register(ns);
    }

    @Override
    public void onPause() {
        super.onPause();
        ARContainer.bus.unregister(this);
        NavState ns = (NavState) navView.getModel();
        ARContainer.bus.unregister(ns);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        navView = null;
    }

    @Subscribe public void onNavStatus(NavStatus n){
        if(navView == null) return;
        NavState ns = (NavState) navView.getModel();

        if(n.state == NavStatus.State.LOGIN){
            ((Model)ns.get("login")).set("show", true);
        }

        if(n.state == NavStatus.State.SCANNING){
            moveRight();
        } else if(ns.get("state") == NavStatus.State.SCANNING){
            moveLeft();
        }

        ns.set("state", n.state);
    }
    //@Subscribe public void onQRCodeFound(QRCodeFound)

    private void moveLeft(){
        if(navView == null) return;
        View v = navView.getView();

        ObjectAnimator na = ObjectAnimator.ofFloat(v, "xFraction", 1f, 0f);

        AnimatorSet s = new AnimatorSet();

        s.play(na);
        s.setDuration(ARContainer.context.getResources().getInteger(R.integer.slide_speed));
        s.start();
    }
    private void moveRight(){
        if(navView == null) return;
        View v = navView.getView();

        ObjectAnimator na = ObjectAnimator.ofFloat(v, "xFraction", 0f, 1f);

        AnimatorSet s = new AnimatorSet();

        s.play(na);
        s.setDuration(ARContainer.context.getResources().getInteger(R.integer.slide_speed));
        s.start();
    }
}
