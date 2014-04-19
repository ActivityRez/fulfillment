package com.activityrez.fulfillment.models;

import android.util.Log;

import com.activityrez.fulfillment.core.ArezApi;
import com.activityrez.fulfillment.core.Model;
import com.activityrez.fulfillment.events.AllIn;
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
    protected String guest_type = "";
    protected String activity_date = "";
    protected String activity_time = "";
    protected String activity_timezone_abbreviation = "";
    protected String comments = "";
    protected int checkin_status = 0;
    protected GuestOverview[] guest_info;

    public void set(String field, Object val){
        if(field == "checkin_status" && checkin_status != (Integer)val){

            Log.i("update","go");

            JSONObject params = new JSONObject();
            try {
                params.put("id",get("id"));
                params.put("checkin_status",val);
                api.request("ticket/status",params,null,null);
            } catch(Exception e){}
        }
        try {
            Field f = this.getClass().getDeclaredField(field);
            f.setAccessible(true);

            if(f.get(this) == val) return;

            f.set(this,val);

            this.setChanged();
            this.notifyObservers();
        } catch(NoSuchFieldException e){
            Log.e("model","field [" + field + "] does not exist");
        } catch(IllegalAccessException e){
            Log.e("model","field [" + field + "] cannot be accessed");
        }
    }

    @Subscribe public void onAllIn(AllIn a){
        if((Integer)get("checkin_status")!= 0)
            return;
        set("checkin_status", 1);
    }

}
