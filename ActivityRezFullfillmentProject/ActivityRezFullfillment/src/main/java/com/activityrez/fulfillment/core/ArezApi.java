package com.activityrez.fulfillment.core;

import org.json.JSONException;
import org.json.JSONObject;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.AuthModule;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.Volley;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import roboguice.RoboGuice;

/**
 * Created by alex on 10/16/13.
 */
@Singleton
public class ArezApi {
    @Inject AuthModule auth;

    private static final String BASE_URL = "https://devhiro.activityrez.com/ar-core/api/";
    private static final String OLD_BASE_URL = "https://staging.activityrez.com/wp-content/plugins/flash-api/wsrv.php";
    private RequestQueue queue;

    @Inject public ArezApi(){
        RoboGuice.getInjector(ARContainer.context).injectMembers(this);
        queue = Volley.newRequestQueue(ARContainer.context);
    }

    public Request request(String url,JSONObject params, Listener<JSONObject> on_success, ErrorListener on_error){
        return request(Method.POST, url, params, on_success, on_error);
    }
    public Request request(int method, String url,JSONObject params, Listener<JSONObject> on_success, ErrorListener on_error){
        final Listener<JSONObject> _callback = on_success;
        final String token = auth.getToken();

        if(params == null)
            params = new JSONObject();

        try {
            if(!params.has("token"))
                params.put("token",token);
        } catch(JSONException e){
            return null;
        }

        PHPJsonRequest req = new PHPJsonRequest(method, BASE_URL + url, params, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject s) {
            try {
                if (s.has("token") && s.getString("token") != token) {
                    auth.setToken(s.getString("token"));
                }
            } catch (JSONException e) {
            }
            if (_callback == null) return;
            _callback.onResponse(s);
            }
        }, on_error);
        req.setShouldCache(false);

        queue.add(req);
        return req;
    }
    public void oldRequest(String service, String action, JSONObject params, Listener<JSONObject> on_success, ErrorListener on_error){
        oldRequest(Method.GET,service,action,params,on_success,on_error);
    }
    public void oldRequest(int method, String service, String action, JSONObject params, Listener<JSONObject> on_success, ErrorListener on_error){
        final Listener<JSONObject> _callback = on_success;
        final String token = auth.getToken();

        if(params == null)
            params = new JSONObject();

        try{
            if(params.has("token"))
                params.remove("token");
            if(!params.has("nonce"))
                params.put("nonce",token);
            if(!params.has("service"))
                params.put("service",service);
            if(!params.has("action"))
                params.put("action",action);
        } catch(JSONException e){}

        PHPJsonRequest req = new PHPJsonRequest(method, OLD_BASE_URL, params, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject s){
                try {
                    if(s.has("nonce") && s.getString("nonce") != token){
                        auth.setToken(s.getString("nonce"));
                    }
                } catch(JSONException e){}
                if(_callback == null) return;
                _callback.onResponse(s);
            }
        }, on_error);
        queue.add(req);
    }
}


