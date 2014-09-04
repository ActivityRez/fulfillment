package com.activityrez.fulfillment.views;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.CustomText;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.core.ArezApi;
import com.activityrez.fulfillment.core.ViewModel;
import com.activityrez.fulfillment.events.AllIn;
import com.activityrez.fulfillment.events.CheckAllDialog;
import com.activityrez.fulfillment.events.DialogEvent;
import com.activityrez.fulfillment.events.SearchEvent;
import com.activityrez.fulfillment.models.*;
import com.activityrez.fulfillment.models.SearchEntry;
import com.google.inject.Inject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Observable;

/**
 * Created by alex on 10/16/13.
 */
public class SearchResultView extends ViewModel {
    @Inject ArezApi api;
    private boolean isOpen;
    private int last_checked = 0;
    private LayoutInflater inflater;
    InputMethodManager inputMethodManager;

    public SearchResultView(View v, Ticket m){
        super(v,m);
    }

    public void init() {
        View v = getView();
        final Ticket m = (Ticket) getModel();

        inputMethodManager = (InputMethodManager) ARContainer.context.getSystemService(Context.INPUT_METHOD_SERVICE);

        final CheckBox check = (CheckBox) v.findViewById(R.id.checkbox1);
        final ImageButton cb = (ImageButton) v.findViewById(R.id.comment_button);
        final ImageButton cl = (ImageButton) v.findViewById(R.id.clipboard);
        final View cv = v.findViewById(R.id.guest_notes);
        final View sh = v.findViewById(R.id.sale_hold);

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
                    com.activityrez.fulfillment.models.SearchEntry mo = new SearchEntry();
                    mo.set("sale_id",getModel().get("sale_id"));
                    ARContainer.bus.post(new SearchEvent(mo));
                } catch(Exception e){}
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check.isChecked()) {
                    getModel().set("checkin_status", 1);
                } else {
                    getModel().set("checkin_status", 0);
                }
            }
        });

        check.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ARContainer.bus.post(new CheckAllDialog());
                return true;
            }
        });

        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( ((String)m.get("comments")).length() > 0 ) {
                    if( !( (Boolean) (m.get("comment_visible")) ) ) {
                        getModel().set("comment_visible", true);
                        cb.setImageResource(R.drawable.comment_s);
                        cv.setVisibility(View.VISIBLE);
                    } else {
                        getModel().set("comment_visible", false);
                        cb.setImageResource(R.drawable.comment_a);
                        cv.setVisibility(View.GONE);
                    }
                }
            }
        });

        cl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager clipboard = (ClipboardManager) ARContainer.context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText( "label", m.get("sale_id").toString() );
                clipboard.setPrimaryClip(clip);

                ARContainer.bus.post(new DialogEvent( m.get("sale_id").toString()) );
            }
        });
    }

    @Override
    public void update(Observable observable, Object data) {
        View v = getView();
        Ticket m = (Ticket) getModel();

        CustomText text = (CustomText) v.findViewById(R.id.guestName);
        if (((String) m.get("first_name")).length() > 0 || ((String) m.get("last_name")).length() > 0)
            text.setText(((String) m.get("first_name")+(String)" "+(String) m.get("last_name")).trim());
        else
            text.setText(((String) m.get("lead_first_name")+(String)" "+(String) m.get("lead_last_name")).trim());
        CustomText type = (CustomText) v.findViewById(R.id.guest_title);
        type.setText((String) m.get("guest_type"));
        CustomText act = (CustomText) v.findViewById(R.id.activity_name);
        act.setText((String) m.get("activity_name"));
        CustomText vouch = (CustomText) v.findViewById(R.id.voucher_id);
        vouch.setText(m.get("sale_id").toString()+"-"+m.get("activity_id").toString());
        CustomText notes = (CustomText) v.findViewById(R.id.guest_notes);
        notes.setText((String)m.get("comments"));
        CheckBox check = (CheckBox) v.findViewById(R.id.checkbox1);
        int ck = (Integer)(m.get("checkin_status"));
        ImageButton cb = (ImageButton) v.findViewById(R.id.comment_button);
        View cv = v.findViewById(R.id.guest_notes);
        String du = (String) m.get("due");
        NumberFormat nf = NumberFormat.getInstance();
        float due = 0;
        try {
            Number n = nf.parse(du);
            due = n.floatValue();
        } catch (ParseException e) {
        }

        View sh = v.findViewById(R.id.sale_hold);

        if( ck ==1 )
            check.setChecked(true);
        else
            check.setChecked(false);

        if( ((String)m.get("comments")).length() > 0 ){
            if( !( (Boolean) (m.get("comment_visible")) ) ) {
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

        if( due > 0 ) {
            sh.setVisibility(View.VISIBLE);
        } else {
            sh.setVisibility(View.GONE);
        }

    }

}
