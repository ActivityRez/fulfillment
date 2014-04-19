package com.activityrez.fulfillment.models;

import com.activityrez.fulfillment.core.Model;

/**
 * Created by alex on 10/21/13.
 */
public class Role extends Model {
    protected int id;
    protected int company_id;
    protected String title;
    protected String[] capabilities;
}
