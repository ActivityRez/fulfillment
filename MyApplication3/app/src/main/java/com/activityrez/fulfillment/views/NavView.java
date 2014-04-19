package com.activityrez.fulfillment.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.CustomButton;
import com.activityrez.fulfillment.CustomText;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.core.Model;
import com.activityrez.fulfillment.core.ViewModel;
import com.activityrez.fulfillment.events.AllIn;
import com.activityrez.fulfillment.events.NavStatus;
import com.activityrez.fulfillment.models.Login;
import com.activityrez.fulfillment.models.NavState;
import com.activityrez.fulfillment.models.Ticket;

import java.lang.reflect.Method;
import java.util.Observable;

/**
 * Created by alex on 10/31/13.
 */
public class NavView extends ViewModel {
    protected LoginView loginView;
    protected CustomButton topButton;
    protected CustomButton bottomButton;
    protected MatchedTicket ticket;

    public NavView(View v){
        this(v, new NavState(null));
    }
    public NavView(View v, Model m){
        super(v, m);

        loginView = new LoginView(v.findViewById(R.id.login),(Model) m.get("login"));
        RelativeLayout buttongroup = (RelativeLayout)v.findViewById(R.id.ButtonLayout);
        topButton = (CustomButton)buttongroup.findViewById(R.id.button);
        bottomButton = (CustomButton)buttongroup.findViewById(R.id.button2);

        if((Boolean)((Model)m.get("login")).get("show") == false){
            buttongroup.setVisibility(View.VISIBLE);
        } else {
            buttongroup.setVisibility(View.INVISIBLE);
        }

        ((Model)m.get("login")).addObserver(this);

        ((CustomButton)v.findViewById(R.id.allinbutton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ARContainer.bus.post(new AllIn());
            }
        });
    }

    public void update(NavState observable, Object data) {
        final NavState n = (NavState)getModel();
        final View v = getView();
        CustomText status = (CustomText)getView().findViewById(R.id.scan_status);

        if(n.get("state") == NavStatus.State.SEARCHING){
            bottomButton.setText("scan");
            bottomButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if(n.get("ticket") != null)
                        n.set("ticket",null);
                    ARContainer.bus.post(new NavStatus(NavStatus.State.SCANNING));
                }
            });

            status.setVisibility(View.GONE);

            Ticket t = (Ticket)n.get("ticket");
            if(t != null){
                topButton.setVisibility(View.GONE);
                v.findViewById(R.id.search_wrap).setVisibility(View.GONE);

                ((CustomText)v.findViewById(R.id.activityName)).setText((String)t.get("activity"));
                ((CustomText)v.findViewById(R.id.activityDate)).setText((String)t.get("date"));
                ((CustomText)v.findViewById(R.id.activityTime)).setText((String)t.get("time"));
                ((CustomText)v.findViewById(R.id.guestName)).setText((String)t.get("name"));

                v.findViewById(R.id.ticketLayout).setVisibility(View.VISIBLE);
            } else {
                topButton.setText("search");
                topButton.setVisibility(View.VISIBLE);

                v.findViewById(R.id.ticketLayout).setVisibility(View.GONE);

                v.findViewById(R.id.search_wrap).setVisibility(View.VISIBLE);
            }
        } else if(n.get("state") == NavStatus.State.SCANNING){
            v.findViewById(R.id.search_wrap).setVisibility(View.GONE);
            topButton.setVisibility(View.GONE);
            bottomButton.setText("search");
            bottomButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                ARContainer.bus.post(new NavStatus(NavStatus.State.SEARCHING));
                }
            });

            status.setVisibility(View.VISIBLE);

            if(observable.get("scan") != null){
                status.setText("loading");
            } else if((Boolean)observable.get("scanError")){
                status.setText("invalid");
            } else {
                status.setText("scanning");
            }
        } else {
            v.findViewById(R.id.search_wrap).setVisibility(View.GONE);
            topButton.setText("DEFAULT");
            bottomButton.setText("scan");
            status.setVisibility(View.GONE);
        }
    }
    public void update(Login observable, Object data) {
        RelativeLayout buttongroup = (RelativeLayout)getView().findViewById(R.id.ButtonLayout);
        final View login = getView().findViewById(R.id.login);

        if((Boolean)((Model)getModel().get("login")).get("show") == false){
            buttongroup.setVisibility(View.VISIBLE);

            ObjectAnimator sdown = ObjectAnimator.ofFloat(login,"translationY",login.getHeight());
            sdown.setDuration(300);

            sdown.addListener(new Animator.AnimatorListener() {
                public void onAnimationStart(Animator animation) {}
                public void onAnimationEnd(Animator animation) {
                    login.setVisibility(View.INVISIBLE);
                }
                public void onAnimationCancel(Animator animation) {}
                public void onAnimationRepeat(Animator animation) {}
            });
            sdown.start();
        } else {
            buttongroup.setVisibility(View.INVISIBLE);

            ObjectAnimator sdown = ObjectAnimator.ofFloat(login,"translationY",0);
            sdown.setDuration(800);

            sdown.addListener(new Animator.AnimatorListener() {
                public void onAnimationStart(Animator animation) {
                    login.setVisibility(View.VISIBLE);
                }
                public void onAnimationEnd(Animator animation) {}
                public void onAnimationCancel(Animator animation) {}
                public void onAnimationRepeat(Animator animation) {}
            });
            sdown.start();
        }
    }

    //we use reflection here to call updates based on type
    public void update(Observable observable, Object data){
        try{
            Method update = getClass().getMethod("update",observable.getClass(), Object.class);
            update.invoke(this, observable, data);
        } catch(Exception e) {}
    }
}
