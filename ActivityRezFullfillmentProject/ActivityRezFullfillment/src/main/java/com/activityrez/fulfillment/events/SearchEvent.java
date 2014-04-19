package com.activityrez.fulfillment.events;

import com.activityrez.fulfillment.core.Model;
import com.activityrez.fulfillment.models.SearchEntry;

import org.json.JSONObject;

/**
 * Created by alex on 11/13/13.
 */
public class SearchEvent {
    public final SearchEntry data;

    public SearchEvent(SearchEntry m){
        this.data = m;
    }

}
