package com.activityrez.fulfillment.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.core.ArezApi;
import com.activityrez.fulfillment.events.AllIn;
import com.activityrez.fulfillment.events.NavStatus;
import com.activityrez.fulfillment.events.SearchEvent;
import com.activityrez.fulfillment.events.ValidTicket;
import com.activityrez.fulfillment.models.Ticket;
import com.activityrez.fulfillment.views.SearchAdapter;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.inject.Inject;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import roboguice.RoboGuice;

/**
 * Created by alex on 11/5/13.
 */
public class SearchFragment extends Fragment {
    @Inject ArezApi api;
    private ArrayList<Ticket> results = new ArrayList<Ticket>();
    private NavStatus.State state = NavStatus.State.DEFAULT;
    private Ticket t;
    private View _v;
    private SearchAdapter s;

    public SearchFragment(){
        RoboGuice.getInjector(ARContainer.context).injectMembers(this);
        ARContainer.bus.register(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _v = inflater.inflate(R.layout.search_results, container, false);
        for(int ni = 0; ni < 10;ni++){
            Ticket _r = new Ticket();
            try {
                JSONObject j = new JSONObject("{\"id\":12,\"sale_id\":200,\"first_name\":\"BEANS McGEE\",\"activity_name\":\"jumpin your moms bones\"}");
                _r.hydrate(j,true);
                results.add(_r);
            } catch(Exception e){}
        }
        s = new SearchAdapter(results);
        ((ListView) _v.findViewById(R.id.listview)).setAdapter(s);
        return _v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ListView) _v.findViewById(R.id.listview)).setAdapter(null);

        results.clear();

        Log.i("called","destroyed SearchFragment Adapter");
    }

    @Subscribe public void onSearchEvent(SearchEvent se){
        Log.i("SearchEvent",""+se);

        _v.findViewById(R.id.search_error).setVisibility(View.GONE);

        if( results.size() > 0 ) results.clear();

        JSONObject params = new JSONObject();
        try {
            params.put("sale", se.data.get("sale_id"));
            params.put("phone", se.data.get("phone"));
            params.put("email", se.data.get("email"));
            params.put("guest", se.data.get("name"));
            params.put("showCXL", "false");
            Log.i("params", " " + params);

            api.request(Request.Method.GET, "ticket/search", params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject ret) {
                            try {
                                if (ret.getInt("status") == -1) {
                                    onError();
                                    return;
                                }
                                JSONArray list = (JSONArray) ret.get("results");
                                if( list.length()==0 ){
                                    onError();
                                    return;
                                }

                                for (int ni = 0; ni < list.length(); ni++) {

                                    Ticket _t = new Ticket();
                                    _t.hydrate(list.get(ni), true);
                                    results.add(_t);
                                }
                                s = new SearchAdapter(results);
                                ListView list_v = (ListView) _v.findViewById(R.id.listview);
                                list_v.setAdapter(s);
                                s.notifyDataSetChanged();
                                _v.findViewById(R.id.search_wrap).setVisibility(View.VISIBLE);

                                onSuccess();
                            } catch (Exception e) {
                                Log.e("ERRORS", e.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            onError();
                        }
                    }
            );
        } catch(Exception e){
            onError();
            Log.e("ERRORS", e.toString());
        }

    }
    @Subscribe public void onValidTicket(ValidTicket v){
        t = v.ticket;
    }
    @Subscribe public void onNavStateChange(NavStatus ns){

        if( state == ns.state ) return;
        state = ns.state;
        if( getView() == null ) return;

        _v.findViewById(R.id.search_error).setVisibility(View.GONE);

        if(results.size() > 0)
            results.clear();

        if(ns.state != NavStatus.State.SEARCHING){
            t = null;
        } else {
            s = new SearchAdapter(results);
            ((ListView) getView().findViewById(R.id.listview)).setAdapter(s);
        }
        if(ns.state == NavStatus.State.SEARCHING && t != null){
            JSONObject params = new JSONObject();
            try {
                params.put("sale", "" + t.get("sale_id"));
                params.put("activity", "" + t.get("root_activity_id"));
                params.put("showCXL", "false");
                Log.i("params"," "+params);
                api.request(Request.Method.GET, "ticket/search", params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject ret) {
                        try {
                            if(ret.getInt("status") == -1){
                                onError();
                                return;
                            }
                            JSONArray list = (JSONArray)ret.get("results");
                            if( list.length()==0 ){
                                onError();
                                return;
                            }
                            for(int ni = 0; ni < list.length(); ni++){
                                Ticket _t = new Ticket();
                                _t.hydrate(list.get(ni),true);
                                results.add(_t);
                            }
                            s = new SearchAdapter(results);

                            ListView list_v = (ListView) _v.findViewById(R.id.listview);
                            list_v.setAdapter(s);
                            s.notifyDataSetChanged();
                        } catch(Exception e){}
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        onError();
                    }
                });
            } catch(Exception e){
                onError();
            }
        }
    }

    @Subscribe public void onAllIn(AllIn a){
        Ticket m;
        Log.i("results size",""+results.size());
        for(int ni = 0; ni < results.size(); ni++) {
            m = results.get(ni);
            if( m == null || (Integer) m.get("checkin_status") != 0 ) continue;
            m.set("checkin_status", 1);
        }
        s.notifyDataSetChanged();
    }

    private void onSuccess(){
        getView().findViewById(R.id.search_error).setVisibility(View.GONE);
    }
    private void onError(){
        getView().findViewById(R.id.search_error).setVisibility(View.VISIBLE);
    }
}
