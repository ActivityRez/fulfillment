package com.activityrez.fulfillment.models;

import android.util.Log;

import com.activityrez.fulfillment.core.Model;
import com.activityrez.fulfillment.events.AllIn;
import com.squareup.otto.Subscribe;

/**
 * Created by alex on 10/31/13.
 */
public class Ticket extends Model {
    protected int id = 0;
    protected int sale = 0;
    protected int voucher = 0;
    protected String name = "guest";
    protected String activity = "";
    protected String guest_type = "";
    protected String date = "";
    protected String time = "";
    protected String notes = "";
    protected boolean checkedIn = false;
    protected GuestOverview[] guest_info;

    @Subscribe public void onAllIn(AllIn a){
        set("checkedIn", true);
    }
}
