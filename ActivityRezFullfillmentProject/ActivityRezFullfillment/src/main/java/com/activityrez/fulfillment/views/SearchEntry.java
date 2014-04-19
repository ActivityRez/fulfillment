package com.activityrez.fulfillment.views;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.core.Model;
import com.activityrez.fulfillment.core.ViewModel;
import com.activityrez.fulfillment.models.Login;

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

    }

}
