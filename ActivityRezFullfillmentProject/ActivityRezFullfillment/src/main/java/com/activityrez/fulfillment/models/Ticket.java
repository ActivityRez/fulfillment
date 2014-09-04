package com.activityrez.fulfillment.models;

import android.util.Log;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.core.ArezApi;
import com.activityrez.fulfillment.core.Model;
import com.activityrez.fulfillment.events.AllIn;
import com.activityrez.fulfillment.events.InternetError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.inject.Inject;
import com.squareup.otto.Subscribe;

import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * Created by alex on 10/31/13.
 */
public class Ticket extends Model {
    @Inject ArezApi api;

    protected int id = 0;
    protected int sale_id = 0;
    protected int activity_id = 0;
    protected int root_activity_id = 0;
    protected String activity_name = "test";
    protected String first_name = "guest";
    protected String last_name = "";
    protected String lead_first_name = "lead guest";
    protected String lead_last_name = "";
    protected String guest_type = "";
    protected String activity_date = "";
    protected String activity_time = "";
    protected String activity_timezone_abbreviation = "";
    protected String comments = "";
    protected boolean comment_visible = false;
    protected int checkin_status = 0;
    protected String due = "";
    protected GuestOverview[] guest_info;

    public void set(String field, Object val){
        if(field == "checkin_status" && checkin_status != (Integer)val){

            JSONObject params = new JSONObject();
            try {
                params.put("id",get("id"));
                params.put("checkin_status",val);
                api.request("ticket/status", params, null, new Response.ErrorListener() {
                       @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                ARContainer.bus.post(new InternetError());
                       }
                });
            } catch(Exception e){
                ARContainer.bus.post(new InternetError());
            }
        }
        try {
            Field f = this.getClass().getDeclaredField(field);
            f.setAccessible(true);

            if(f.get(this) == val) return;

            f.set(this,val);

        } catch(NoSuchFieldException e){
            Log.e("model","field [" + field + "] does not exist");
        } catch(IllegalAccessException e){
            Log.e("model","field [" + field + "] cannot be accessed");
        }
    }
}
