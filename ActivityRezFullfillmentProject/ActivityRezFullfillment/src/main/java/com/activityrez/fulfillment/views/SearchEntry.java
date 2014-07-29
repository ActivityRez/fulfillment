package com.activityrez.fulfillment.views;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.core.Model;
import com.activityrez.fulfillment.core.ViewModel;
import com.activityrez.fulfillment.models.DateRange;
import com.activityrez.fulfillment.models.SoldActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by hiro on 3/12/14.
 */
public class SearchEntry extends ViewModel {

    public SearchEntry(View v, Model m){
        super(v, m);


        EditText na = (EditText)getView().findViewById(R.id.name_search);
        EditText ph = (EditText)getView().findViewById(R.id.phone_search);
        EditText em = (EditText)getView().findViewById(R.id.email_search);
        EditText cr = (EditText)getView().findViewById(R.id.credit_search);
        EditText sa = (EditText)getView().findViewById(R.id.sale_id_search);
        Spinner  ac = (Spinner)getView().findViewById(R.id.activity_search);
        Spinner  dr = (Spinner)getView().findViewById(R.id.date_range_search);

        na.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getModel().set("name",s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
        ph.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getModel().set("phone",s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
        em.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getModel().set("email",s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
        cr.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getModel().set("credit",s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
        sa.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getModel().set("sale_id", s.toString());
            }

            public void afterTextChanged(Editable s) {
            }
        });

       ac.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           public void onItemSelected(AdapterView parent, View viw, int arg2, long arg3) {
               Spinner spinner = (Spinner) parent;
               SoldActivity item = (SoldActivity) spinner.getSelectedItem();
               getModel().set("activity_id", item.get("id"));
           }

           public void onNothingSelected(AdapterView parent) {
           }
       });

        dr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View viw, int arg2, long arg3) {
                Spinner spinner = (Spinner) parent;
                DateRange item = (DateRange) spinner.getSelectedItem();

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat td = new SimpleDateFormat("yyyy-MM-dd");
                String today = td.format(cal.getTime());

                switch( ((Integer) item.get("id")) ) {
                    case 1: getModel().set("date_from", today + " 00:00:00");
                             getModel().set("date_to", today + " 23:59:59");
                             break;
                    case 2: getModel().set("date_from", today + " 00:00:00");
                             getModel().set("date_to", "");
                             break;
                    case 3:
                    default: getModel().set("date_from", "");
                              getModel().set("date_to", "");
                              break;
                }
            }

            public void onNothingSelected(AdapterView parent) {
            }
        });

    }

}
