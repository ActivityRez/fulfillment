package com.activityrez.fulfillment.core;

import android.content.Context;

import com.activityrez.fulfillment.ARContainer;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

/**
 * Created by alex on 10/24/13.
 */
public class WrapModule extends AbstractModule {
    private final ARContainer context;

    @Inject
    public WrapModule(Context context){
        super();
        this.context = (ARContainer)context;
    }

    @Override
    protected void configure() {
        bind(AppProvider.class).toInstance( new AppProvider( context ) );
        bind( ARContainer.class ).toProvider( AppProvider.class );
    }
}
