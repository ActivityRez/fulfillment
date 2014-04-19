package com.activityrez.fulfillment.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.CustomText;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.core.ArezApi;
import com.activityrez.fulfillment.core.ViewModel;
import com.activityrez.fulfillment.events.NavStatus;
import com.activityrez.fulfillment.events.SearchEvent;
import com.activityrez.fulfillment.models.*;
import com.activityrez.fulfillment.models.SearchEntry;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Observable;
import org.json.JSONObject;

/**
 * Created by alex on 10/16/13.
 */
public class SearchResultView extends ViewModel {
    @Inject ArezApi api;
    private boolean isOpen;
    private int last_checked = 0;
    private LayoutInflater inflater;
    InputMethodManager inputMethodManager;

    public SearchResultView(View v, final int i, final Ticket m, final ArrayList<Boolean> cviews){
        super(v,m);

        CustomText text = (CustomText) v.findViewById(R.id.guestName);
        text.setText(((String) m.get("first_name")+(String)" "+(String) m.get("last_name")).trim());
        CustomText type = (CustomText) v.findViewById(R.id.guest_title);
        type.setText((String) m.get("guest_type"));
        CustomText act = (CustomText) v.findViewById(R.id.activity_name);
        act.setText((String) m.get("activity_name"));
        CustomText vouch = (CustomText) v.findViewById(R.id.voucher_id);
        vouch.setText(m.get("sale_id").toString()+"-"+m.get("activity_id").toString());
        CustomText notes = (CustomText) v.findViewById(R.id.guest_notes);
        notes.setText((String)m.get("comments"));

        inputMethodManager = (InputMethodManager) ARContainer.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        final int ck = (Integer)(m.get("checkin_status"));
        final CheckBox check = (CheckBox) v.findViewById(R.id.checkbox1);
        final ImageButton cb = (ImageButton) v.findViewById(R.id.comment_button);
        final View cv = v.findViewById(R.id.guest_notes);

        v.findViewById(R.id.result_wrap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        v.findViewById(R.id.sale_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    com.activityrez.fulfillment.models.SearchEntry m = new SearchEntry();
                    m.set("sale_id",getModel().get("sale_id"));
                    ARContainer.bus.post(new SearchEvent(m));
                } catch(Exception e){}
            }
        });

        if( ((String)m.get("comments")).length() > 0 ){
            if( !cviews.get(i) ) {
                cb.setImageResource(R.drawable.comment_a);
                cv.setVisibility(View.GONE);
            } else {
                cb.setImageResource(R.drawable.comment_s);
                cv.setVisibility(View.VISIBLE);
            }
        } else {
            cb.setImageResource(R.drawable.comment_d);
            cv.setVisibility(View.GONE);
        }
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if( ((String)m.get("comments")).length() > 0 ) {
                if( !cviews.get(i) ) {
                    cviews.set(i, true);
                    cb.setImageResource(R.drawable.comment_s);
                    cv.setVisibility(View.VISIBLE);
                } else {
                    cviews.set(i, false);
                    cb.setImageResource(R.drawable.comment_a);
                    cv.setVisibility(View.GONE);
                }
            }
            }
        });

        if( ck ==1 )
            check.setChecked(true);
        else
            check.setChecked(false);
        check.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // NOTE!! the original checkin_status ... 0: not yet, 1: checked in, 2: no shown (NOT true/false)
                if( ((CheckBox) v.findViewById(R.id.checkbox1)).isChecked() ) {
                    last_checked = 0;
                    getModel().set("checkin_status", 1);
                } else { // will see "no shown" sooner/later
                    last_checked = 1;
                    getModel().set("checkin_status", 0);
                }
            }
         });
    }

    @Override
    public void update(Observable observable, Object data) {
        View v = getView();
        try{
            ( (BaseAdapter) ( (ListView)v.getParent() ).getAdapter() ).notifyDataSetChanged();
        } catch(Exception e) {}
    }


}
