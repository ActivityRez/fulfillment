package com.activityrez.fulfillment.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.activityrez.fulfillment.AuthModule;
import com.activityrez.fulfillment.CustomButton;
import com.activityrez.fulfillment.CustomText;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.core.Model;
import com.activityrez.fulfillment.core.ViewModel;
import com.activityrez.fulfillment.models.Login;

import android.widget.EditText;

import java.util.Observable;

import javax.inject.Inject;

public class LoginView extends ViewModel {
    @Inject AuthModule auth;

    public LoginView(View v, Model m){
        super(v, m);
        v.findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Login)getModel()).login();
            }
        });

        EditText u = (EditText)getView().findViewById(R.id.login_username);
        EditText p = (EditText)getView().findViewById(R.id.login_password);
        u.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getModel().set("username",s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
        p.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getModel().set("password",s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
        if((Boolean)m.get("show") == false){
            v.setVisibility(View.INVISIBLE);
        }
    }

    public void update(Observable observable, Object data){
        CustomText title = (CustomText)getView().findViewById(R.id.login_title);
        CustomButton v = (CustomButton)getView().findViewById(R.id.login_button);
        EditText p = (EditText)getView().findViewById(R.id.login_password);
        Resources r = getView().getResources();
        Login m = (Login)getModel();

        try {
            if(((String)m.get("password")).length() == 0 && p.getText().length() != 0){
                ((EditText)getView().findViewById(R.id.login_password)).setText("");
            }
            if(((Boolean)m.get("loading")).booleanValue()){
                v.setEnabled(false);
                v.setText(R.string.loading);
            } else {
                v.setEnabled(true);
                v.setText(R.string.login);
            }

            if(((String) m.get("error")).length() > 0){
                title.setTextColor(r.getColor(R.color.my_red));
                title.setText((String)getModel().get("error"));
            } else {
                title.setTextColor(r.getColor(R.color.my_white));
                title.setText("please login");
            }
        } catch(NullPointerException e){}
    }
}