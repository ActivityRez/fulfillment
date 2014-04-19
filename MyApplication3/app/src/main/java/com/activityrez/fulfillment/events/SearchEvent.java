package com.activityrez.fulfillment.events;

import com.activityrez.fulfillment.core.Model;

import org.json.JSONObject;

/**
 * Created by alex on 11/13/13.
 */
public class SearchEvent extends Model {
    protected int sale_id;
    protected int ticket_id;
    protected String name;
    protected String phone;
    protected String email;
    protected String cc_number;

    public SearchEvent(){
        this(null);
    }

    public SearchEvent(JSONObject data){
        super();
        if(data == null) return;
        hydrate(data, true);
    }
}
