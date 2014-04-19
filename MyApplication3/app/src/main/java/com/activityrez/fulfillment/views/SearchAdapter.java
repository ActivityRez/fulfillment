package com.activityrez.fulfillment.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.models.Ticket;

import java.util.ArrayList;

/**
 * Created by alex on 11/7/13.
 */
public class SearchAdapter extends BaseAdapter {
    private ArrayList<Ticket> results;
    private LayoutInflater inflater;

    public SearchAdapter(ArrayList<Ticket> r){
        results = r;
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
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View row = view;
        if(row == null){
            row  = inflater.inflate(R.layout.search_result,null,false);
            new SearchResultView(row,results.get(i));
        }

        return row;
    }
}
