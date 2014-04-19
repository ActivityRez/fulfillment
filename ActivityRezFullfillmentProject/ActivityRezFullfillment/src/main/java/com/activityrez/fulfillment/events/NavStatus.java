package com.activityrez.fulfillment.events;

import com.activityrez.fulfillment.models.NavState;

/**
 * Created by alex on 11/5/13.
 */
public class NavStatus {
    public enum State{SCANNING,SEARCHING,DEFAULT,LOGIN};
    public State state;

    public NavStatus(State s){ state = s; }
}
