package com.activityrez.fulfillment.events;

import com.activityrez.fulfillment.models.SearchEntry;

/**
 * Created by hiro on 9/2/2014.
 */
public class DialogEvent {
    public String  sale_id = "0";

    public DialogEvent(String id){
        this.sale_id =id;
    }
}
