package com.activityrez.fulfillment.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.activityrez.fulfillment.R;

/**
 * Created by alex on 10/31/13.
 */
public class WelcomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.splash,container,false);
        return v;
    }
}
