package com.activityrez.fulfillment.models;

import com.activityrez.fulfillment.core.Model;

/**
 * Created by hiro on 7/26/2014.
 */
public class DateRange extends Model {
    protected int id = 0;
    protected String choice = "";

    public DateRange( int _id, String _choice )
    {
        id = _id;
        choice = _choice;
    }
    public String toString()
    {
        return( choice );
    }

}