package com.activityrez.fulfillment.views;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.CustomText;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.core.ArezApi;
import com.activityrez.fulfillment.core.ViewModel;
import com.activityrez.fulfillment.events.SearchEvent;
import com.activityrez.fulfillment.models.Ticket;
import com.google.inject.Inject;

import org.json.JSONObject;

import java.util.Observable;

/**
 * Created by alex on 10/16/13.
 */
public class SearchResultView extends ViewModel {
    @Inject ArezApi api;
    private boolean lastChecked;
    private boolean isOpen;


    public SearchResultView(View v, Ticket m){
        super(v,m);

        CustomText text = (CustomText) v.findViewById(R.id.guestName);
        text.setText((String) m.get("name"));
        CustomText type = (CustomText) v.findViewById(R.id.guest_title);
        type.setText((String) m.get("guest_type"));
        CustomText act = (CustomText) v.findViewById(R.id.activity_name);
        act.setText((String) m.get("activity"));
        CustomText vouch = (CustomText) v.findViewById(R.id.voucher_id);
        vouch.setText(m.get("voucher").toString());
        CustomText notes = (CustomText) v.findViewById(R.id.guest_notes);
        notes.setText((String)m.get("notes"));

        v.findViewById(R.id.sale_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject evt = new JSONObject();
                try {
                    evt.put("sale_id",getModel().get("sale"));
                    ARContainer.bus.post(new SearchEvent(evt));
                } catch(Exception e){}
            }
        });

        final ImageButton cb = (ImageButton)v.findViewById(R.id.comment_button);
        if(((String)m.get("notes")).length() > 0){
            cb.setImageResource(R.drawable.comment_a);
            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isOpen){
                        isOpen = false;
                        cb.setImageResource(R.drawable.comment_a);
                        getView().findViewById(R.id.guest_notes).setVisibility(View.GONE);
                    } else {
                        isOpen = true;
                        cb.setImageResource(R.drawable.comment_s);
                        getView().findViewById(R.id.guest_notes).setVisibility(View.VISIBLE);
                    }
                }
            });

        } else {
            cb.setImageResource(R.drawable.comment_d);
        }

        final CheckBox check = (CheckBox) v.findViewById(R.id.checkbox1);
        check.setChecked(((Boolean) m.get("checkedIn")).booleanValue());

        if((Integer)m.get("id") != 0){
            lastChecked = (Boolean)m.get("checkedIn");
        }

        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                getModel().set("checkedIn",check.isChecked());
            }
        });
    }

    @Override
    public void update(Observable observable, Object data) {
        View v = getView();
        Ticket m = (Ticket)getModel();
        boolean checked = ((Boolean) m.get("checkedIn")).booleanValue();

        CustomText text = (CustomText) v.findViewById(R.id.guestName);
        text.setText((String) m.get("name"));
        CustomText type = (CustomText) v.findViewById(R.id.guest_title);
        type.setText((String) m.get("guest_type"));
        CustomText act = (CustomText) v.findViewById(R.id.activity_name);
        act.setText((String) m.get("activity"));
        CustomText vouch = (CustomText) v.findViewById(R.id.voucher_id);
        vouch.setText(m.get("voucher").toString());
        CustomText notes = (CustomText) v.findViewById(R.id.guest_notes);
        notes.setText((String)m.get("notes"));

        if(isOpen){
            ((ImageButton)v.findViewById(R.id.comment_button)).setImageResource(R.drawable.comment_s);
        } else if(((String)m.get("notes")).length() > 0){
            ((ImageButton)v.findViewById(R.id.comment_button)).setImageResource(R.drawable.comment_a);
        } else {
            ((ImageButton)v.findViewById(R.id.comment_button)).setImageResource(R.drawable.comment_d);
        }

        CheckBox check = (CheckBox) v.findViewById(R.id.checkbox1);
        if(check.isChecked() != checked){
            check.setChecked(checked);
        }

        if(checked != lastChecked){
            Log.i("ticket stuff","we've toggled the checkin! CALL SAVE");
            lastChecked = checked;
            JSONObject params = new JSONObject();
            try {
                params.put("id",m.get("id"));
                params.put("status",checked);
                api.request("ticket/status/test",params,null,null);
            } catch(Exception e){}
        }
    }
}
