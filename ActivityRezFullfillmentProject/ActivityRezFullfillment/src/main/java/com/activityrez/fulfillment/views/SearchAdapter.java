package com.activityrez.fulfillment.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.models.Ticket;

import java.util.ArrayList;

/**
 * Created by alex on 11/7/13.
 */
public class SearchAdapter extends BaseAdapter {
    private ArrayList<Ticket> results;
    private ArrayList<SearchResultView> views;
    private LayoutInflater inflater;

    public SearchAdapter(ArrayList<Ticket> r){
        results = r;
        views = new ArrayList<SearchResultView>();
        for(int ni = 0; ni < r.size(); ni++) {
            views.add(null);
        }
        inflater = (LayoutInflater)ARContainer.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public Object getItem(int i) {
        return results.get(i);
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        SearchResultView obj;

        if( view == null ) {
            view = inflater.inflate(R.layout.search_result, null, false);
        }
        if( views.get(i) == null ) {
            obj = new SearchResultView( view, results.get(i) );
            views.set(i,obj);
        } else {
            views.get(i).setView(view);
        }

        return view;
    }

}
