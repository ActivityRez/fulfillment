package com.activityrez.fulfillment.models;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.camera.CameraManager;
import com.activityrez.fulfillment.core.ArezApi;
import com.activityrez.fulfillment.core.Model;
import com.activityrez.fulfillment.events.NavStatus;
import com.activityrez.fulfillment.events.QRCodeFound;
import com.activityrez.fulfillment.events.QRCodePointFound;
import com.activityrez.fulfillment.events.ValidTicket;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.inject.Inject;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by alex on 10/31/13.
 */
public class NavState extends Model {
    @Inject ArezApi api;
    @Inject CameraManager cameraManager;

    protected Login login;
    protected SearchEntry search;
    protected JSONObject scan;
    protected boolean scanError = false;
    protected Ticket ticket;
    protected NavStatus.State state = NavStatus.State.LOGIN;

    public NavState(HashMap<String,Object> data){
        super(data);
        login = new Login();
        search = new SearchEntry();
        ARContainer.bus.register(this);
    }

    @Subscribe public void onQRCodeFound(QRCodeFound msg){
        final long[] successPattern = {0,300};
        final long[] errorPattern = {0,100,30,100};
        final Vibrator v = (Vibrator) ARContainer.context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(300);

        try {
            ticket = null;
            scanError = false;
            set("scan",new JSONObject(msg.result));
            JSONObject params = new JSONObject();
            params.put("id",scan.getInt("ticket"));
            api.request(Request.Method.GET,"ticket",params,new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                try {
                    if(jsonObject.getInt("status") == -1){
                        v.vibrate(errorPattern,-1);
                        invalidTicket();
                        return;
                    }
                    ticket = new Ticket();
                    ticket.hydrate(jsonObject.get("result"),true);
                    scan = null;

                    v.vibrate(successPattern,-1);
                    ARContainer.bus.post(new ValidTicket(ticket));
                    ARContainer.bus.post(new NavStatus(NavStatus.State.SEARCHING));
                } catch(Exception e){
                    v.vibrate(errorPattern,-1);
                    invalidTicket();
                }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    scan = null;
                    v.vibrate(errorPattern,-1);
                    invalidTicket();
                }
            });
        } catch(JSONException e){
            v.vibrate(errorPattern,-1);
            invalidTicket();
        }
    }
    private void invalidTicket(){
        scan = null;
        set("scanError",true);

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                set("scanError",false);
                cameraManager.startPreview();
            }
        },1000);
    }
}
