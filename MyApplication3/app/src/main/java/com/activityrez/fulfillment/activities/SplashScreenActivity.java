package com.activityrez.fulfillment.activities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.AuthModule;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.core.ArezApi;
import com.activityrez.fulfillment.models.User;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.transform.ErrorListener;

import roboguice.RoboGuice;
import roboguice.activity.RoboSplashActivity;
import roboguice.inject.InjectResource;

/**
 * Created by alex on 10/29/13.
 */
public class SplashScreenActivity extends Activity {
    @Inject ArezApi api;
    @Inject AuthModule auth;

    protected int minDisplayMs = (int) (1.5 * 1000);

    public SplashScreenActivity(){
        super();
        RoboGuice.getInjector(ARContainer.context).injectMembers(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if no camera... bail
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            setContentView(R.layout.no_camera);
            return;
        }

        boolean hasConnection = false;
        NetworkInfo[] infos = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getAllNetworkInfo();
        for(NetworkInfo info:infos){
            NetworkInfo.State state = info.getState();
            if(state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING)
                hasConnection = true;
        }

        //if no network connection... bail
        if(!hasConnection){
            setContentView(R.layout.no_network);
            return;
        }

        setContentView(R.layout.splashscreen);

        if(auth.getToken() == "NEW"){
            done();
            return;
        }
        api.request("user", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getInt("status") != -1) {
                        Log.i("beans",jsonObject.get("result").toString());
                        User u = new User();
                        u.hydrate(jsonObject.get("result"),true);
                        auth.setUser(u);
                    }
                } catch (JSONException e) {

                }

                done();
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                done();
            }
        });
    }

    private void done(){
        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);

        startActivity(intent);
        finish();
    }
}
