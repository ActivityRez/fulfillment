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
    private ArrayList<Boolean> cviews;
    private LayoutInflater inflater;

    public SearchAdapter(ArrayList<Ticket> r){
        results = r;
        Log.i("results...","size "+results.size());
        views = new ArrayList<SearchResultView>();
        cviews = new ArrayList<Boolean>();
        for(int ni = 0; ni < r.size(); ni++) {
            views.add(null);
            cviews.add(false);
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
        View v = view;
        if( v == null ){
            v  = inflater.inflate(R.layout.search_result, null, false);
        }
        SearchResultView obj = new SearchResultView( v, i, results.get(i), cviews );
        views.set( i, obj);
        v = views.get(i).getView();

        Log.i("test","here "+obj);
        return v;
    }


}
