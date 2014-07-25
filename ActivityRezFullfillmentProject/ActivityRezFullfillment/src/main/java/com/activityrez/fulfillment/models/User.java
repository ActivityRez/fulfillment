package com.activityrez.fulfillment.models;

import com.activityrez.fulfillment.core.Model;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by alex on 10/21/13.
 */
public class User extends Model {
    protected int id = 0;
    protected int company_id = 0;
    protected Company company;
    protected String name = "guest";
    protected String email;
    protected String status;
    protected Role role;

    public boolean can(String cap){
        if(role == null)
            return false;
        for(String c: role.capabilities){
            if(c == cap) return true;
        }
        return false;
    }
}
