package com.activityrez.fulfillment.events;

import com.activityrez.fulfillment.models.Ticket;

/**
 * Created by alex on 10/31/13.
 */
public class ValidTicket {
    public final Ticket ticket;
    public ValidTicket(Ticket ticket){
        this.ticket = ticket;
    }
}
