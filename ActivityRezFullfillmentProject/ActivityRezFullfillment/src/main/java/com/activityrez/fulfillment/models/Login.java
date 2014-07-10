package com.activityrez.fulfillment.models;

import android.os.Handler;
import android.util.Log;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.AuthModule;
import com.activityrez.fulfillment.core.ArezApi;
import com.activityrez.fulfillment.core.Model;
import com.activityrez.fulfillment.events.NavStatus;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alex on 10/18/13.
 */
public class Login extends Model {
    @Inject AuthModule auth;
    @Inject ArezApi api;

    protected String username = "";
    protected String password = "";
    protected String error = "";
    protected boolean loading = false;
    protected boolean show = true;

    private Request req = null;

    public Login(){ this(null); }
    public Login(HashMap<String,Object> data){
        super(data);
        if((Integer)auth.getUser().get("id") > 0){
            set("show",false);
        }
    }

    public void login(){
        set("loading", true);
        set("error", "");

        ArrayList<String> params = new ArrayList<String>();
        params.add("username");
        params.add("password");

        req = api.request(Request.Method.GET, "login", (JSONObject) out(params, true), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    set("loading", false);
                    try {
                        //Log.i("got back",jsonObject.toString());
                        if (jsonObject.getInt("status") == -1) {
                            set("error", jsonObject.getString("msg"));
                            return;
                        }

                    set("username","");
                    set("show", false);
                    User u = new User();
                    u.hydrate(jsonObject.get("result"),true);
                    auth.setUser(u);
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ARContainer.bus.post(new NavStatus(NavStatus.State.SCANNING));
                        }
                    },1000);
                } catch (JSONException e) {
                }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    set("loading", false);
                }
            }
        );

        set("password","");
    }

    public void logout(){

        ArrayList<String> params = new ArrayList<String>();

        req = api.request(Request.Method.GET, "logout", (JSONObject) out(params, true), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        set("loading", false);
                        try {
                            //Log.i("logging out",jsonObject.toString());
                            if (jsonObject.getInt("status") == -1) {
                                set("error", jsonObject.getString("msg"));
                                return;
                            }
                            auth.setUser(new User());
                            set("show", true);
                        } catch (JSONException e) {
                            //Log.e("log out error",""+e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        set("loading", false);
                    }
                }
        );
    }
}