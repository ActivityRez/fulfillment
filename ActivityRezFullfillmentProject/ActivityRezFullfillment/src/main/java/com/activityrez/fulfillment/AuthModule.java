package com.activityrez.fulfillment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.activityrez.fulfillment.models.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;

/**
 * Created by alex on 10/21/13.
 */

@Singleton
public class AuthModule {
    @Inject ARContainer app;

    private User user = new User();
    private String token;
    private SharedPreferences sp;

    @Inject
    public AuthModule(){}

    public String getToken(){
        if(sp == null)
            sp = app.getSharedPreferences("arez",Context.MODE_PRIVATE);
        if(token == null)
            token = sp.getString("token","NEW");
        return token;
    }
    public void setToken(String s){
        if(sp == null)
            sp = app.getSharedPreferences("arez",Context.MODE_PRIVATE);
        if(token != null && token == s) return;

        token = s;
        SharedPreferences.Editor e = sp.edit();
        e.putString("token",s);
        e.commit();
    }
    public User getUser(){ return user; }
    public void setUser(User u){
        user.hydrate((HashMap<String,Object>)u.out());
    }
}
