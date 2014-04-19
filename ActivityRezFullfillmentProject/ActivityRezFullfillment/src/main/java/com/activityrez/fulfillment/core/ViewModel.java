package com.activityrez.fulfillment.core;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.activityrez.fulfillment.ARContainer;

import roboguice.RoboGuice;

/**
 * Created by alex on 10/16/13.
 */
public class ViewModel implements Observer {
    private View view;
    private Model model;

    public ViewModel(View v, Model m){
        RoboGuice.getInjector(ARContainer.context).injectMembers(this);

        view = v;
        model = m;

        model.addObserver(this);
    }
    public View getView(){ return view; }
    public Model getModel(){ return model; }

    @Override //this is where you connect fields to the view
    public void update(Observable observable, Object data) {}
}
