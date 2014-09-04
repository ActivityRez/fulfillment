package com.activityrez.fulfillment.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.activityrez.fulfillment.ARContainer;
import com.activityrez.fulfillment.AuthModule;
import com.activityrez.fulfillment.CustomButton;
import com.activityrez.fulfillment.CustomText;
import com.activityrez.fulfillment.R;
import com.activityrez.fulfillment.core.ArezApi;
import com.activityrez.fulfillment.core.Model;
import com.activityrez.fulfillment.core.ViewModel;
import com.activityrez.fulfillment.events.AllIn;
import com.activityrez.fulfillment.events.NavStatus;
import com.activityrez.fulfillment.events.SearchEvent;
import com.activityrez.fulfillment.models.Company;
import com.activityrez.fulfillment.models.DateRange;
import com.activityrez.fulfillment.models.Login;
import com.activityrez.fulfillment.models.NavState;
import com.activityrez.fulfillment.models.SoldActivity;
import com.activityrez.fulfillment.models.Ticket;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Observable;

/**
 * Created by alex on 10/31/13.
 */
public class NavView extends ViewModel {
    @Inject AuthModule auth;
    @Inject ArezApi api;

    protected LoginView loginView;
    protected SearchEntry searchView;
    protected ImageView logo;
    protected CustomButton topButton;
    protected CustomButton bottomButton;
    private ArrayAdapter<SoldActivity> a;
    private ArrayAdapter<DateRange> d;

    private static final long limitedTime = 1000;
    private static long clickTime;

    public NavView(View v){
        this(v, new NavState(null));
    }
    public NavView(View v, final Model m){
        super(v, m);

        loginView = new LoginView(v.findViewById(R.id.login),(Model) m.get("login"));
//        final View login = getView().findViewById(R.id.login);

        RelativeLayout buttongroup = (RelativeLayout)v.findViewById(R.id.ButtonLayout);
        topButton = (CustomButton)buttongroup.findViewById(R.id.button);
        bottomButton = (CustomButton)buttongroup.findViewById(R.id.button2);

        if((Boolean)((Model)m.get("login")).get("show") == false){
            buttongroup.setVisibility(View.VISIBLE);
        } else {
            buttongroup.setVisibility(View.INVISIBLE);
        }

        ((Model)m.get("login")).addObserver(this);

        v.findViewById(R.id.imageView1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m.get("ticket") != null) m.set("ticket",null);

                InputMethodManager mgr = (InputMethodManager) ARContainer.context.getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                ((Login)m.get("login")).logout();
                ARContainer.bus.post(new NavStatus(NavStatus.State.LOGIN));
            }
        });

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
            bottomButton.setVisibility(View.VISIBLE);
            bottomButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    InputMethodManager mgr = (InputMethodManager) ARContainer.context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    if(n.get("ticket") != null) n.set("ticket",null);
                    ARContainer.bus.post(new NavStatus(NavStatus.State.SCANNING));
                }
            });

            status.setVisibility(View.GONE);

            Ticket t = (Ticket)n.get("ticket");
            if(t != null) {
                topButton.setVisibility(View.GONE);
                v.findViewById(R.id.search_wrap).setVisibility(View.GONE);

                ((CustomText) v.findViewById(R.id.activityName)).setText((String) t.get("activity_name"));
                ((CustomText) v.findViewById(R.id.activityDate)).setText((String) t.get("activity_date"));
                ((CustomText) v.findViewById(R.id.activityTime)).setText((String) t.get("activity_time"));
                ((CustomText) v.findViewById(R.id.activityTimeZone)).setText((String) t.get("activity_timezone_abbreviation"));
                if (((String) t.get("first_name")).length() > 0 || ((String) t.get("last_name")).length() > 0)
                    ((CustomText) v.findViewById(R.id.guestName)).setText(((String) t.get("first_name") + (String) " " + (String) t.get("last_name")).trim());
                else
                    ((CustomText) v.findViewById(R.id.guestName)).setText(((String) t.get("lead_first_name") + (String) " " + (String) t.get("lead_last_name")).trim());

                v.findViewById(R.id.ticketLayout).setVisibility(View.VISIBLE);
            } else {

                a = new ArrayAdapter(ARContainer.context, R.layout.spinner);
                try {
                    JSONObject j = new JSONObject("{\"id\":0,\"name\":(none)}");
                    SoldActivity act = new SoldActivity();
                    act.hydrate(j,true);
                    a.add(act);
                } catch(Exception e){}

                JSONObject params = new JSONObject();
                try {
                    params.put("owner", ( (Company)auth.getUser().get("company") ).get("id"));

                    api.request(Request.Method.GET, "activity/soldTitles", params, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject ret) {
                                    try {
                                        if (ret.getInt("status") < 1) {
                                            if( ret.has("total") && ret.getInt("total") == 0 ) {
                                                onActivityNotFound();;
                                            } else {
                                                onError();
                                            }
                                            return;
                                        }
                                        JSONArray list = (JSONArray) ret.get("results");
                                        if (list.length() == 0) {
                                            return;
                                        }

                                        for (int ni = 0; ni < list.length(); ni++) {
                                            SoldActivity act = new SoldActivity();
                                            act.hydrate(list.get(ni), true);
                                            a.add(act);
                                        }

                                    } catch (Exception e) {
                                        Log.e("ERRORS", e.toString());
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                }
                            }
                    );
                } catch (Exception e) {
                    Log.e("ERRORS", e.toString());
                }

                a.setDropDownViewResource( R.layout.spinner_dropdown );
                Spinner spinner1 = (Spinner) getView().findViewById(R.id.activity_search);
                spinner1.setPromptId(R.string.activity_prompt);
                spinner1.setAdapter(a);

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat td = new SimpleDateFormat("MM/dd/yyyy");
                String today = td.format(cal.getTime());

                d = new ArrayAdapter(ARContainer.context, R.layout.spinner, new DateRange[] {
                        new DateRange( 1, "Only Today ("+today+")" ),
                        new DateRange( 2, "Starting Today (From "+today+")" ),
                        new DateRange( 3, "All" ),
                });
                d.setDropDownViewResource( R.layout.spinner_dropdown );
                Spinner spinner2 = (Spinner) getView().findViewById(R.id.date_range_search);
                spinner2.setPromptId(R.string.date_range_prompt);
                spinner2.setAdapter(d);

                topButton.setText("search");
                topButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if( !oneClickEvent() ) return;

                        InputMethodManager mgr = (InputMethodManager) ARContainer.context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                        if (n.get("ticket") != null)
                            n.set("ticket", null);
                        ARContainer.bus.post(new SearchEvent((com.activityrez.fulfillment.models.SearchEntry)searchView.getModel()));
                    }
                });
                topButton.setVisibility(View.VISIBLE);

                v.findViewById(R.id.ticketLayout).setVisibility(View.GONE);
                v.findViewById(R.id.search_wrap).setVisibility(View.VISIBLE);

                searchView = new SearchEntry(v.findViewById(R.id.search_wrap),(Model) getModel().get("search"));
            }
        } else if(n.get("state") == NavStatus.State.SCANNING){
            v.findViewById(R.id.search_wrap).setVisibility(View.GONE);
            topButton.setVisibility(View.GONE);

            if(observable.get("scan") != null){
                status.setText("loading");
                bottomButton.setVisibility(View.GONE);
            } else if((Boolean)observable.get("scanError")){
                status.setText("invalid");
                if (n.get("ticket") != null)
                    n.set("ticket", null);
                bottomButton.setVisibility(View.VISIBLE);
            } else {
                status.setText("scanning");
                bottomButton.setVisibility(View.VISIBLE);
            }

            bottomButton.setText("search");

            if(observable.get("scan") == null){
                bottomButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        ARContainer.bus.post(new NavStatus(NavStatus.State.SEARCHING));
                    }
                });
            }

            status.setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.search_wrap).setVisibility(View.GONE);
            topButton.setText("DEFAULT");
            bottomButton.setText("scan");
            bottomButton.setVisibility(View.VISIBLE);
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

    public static boolean oneClickEvent() {
        long time = System.currentTimeMillis();
        if( time - clickTime < limitedTime ) {
            return false;
        }
        clickTime = time;
        return true;
    }

    private void onSuccess(){}
    private void onError(){
        View v = (View) getView().findViewById(R.id.search_error);
        ((CustomText) v.findViewById(R.id.error_message)).setText("There was an error communicating with the server. Please try again later.");
        getView().findViewById(R.id.search_error).setVisibility(View.VISIBLE);
    }
    private void onActivityNotFound(){
        View v = (View) getView().findViewById(R.id.search_error);
        ((CustomText) v.findViewById(R.id.error_message)).setText("Not sold activities found for the filter. You may not have them yet. Please try again later.");
        getView().findViewById(R.id.search_error).setVisibility(View.VISIBLE);
    }
}
