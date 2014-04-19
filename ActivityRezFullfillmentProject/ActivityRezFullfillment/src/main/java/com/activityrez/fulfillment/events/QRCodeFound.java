package com.activityrez.fulfillment.events;

/**
 * Created by alex on 10/30/13.
 */
public class QRCodeFound {
    public final String result;

    public QRCodeFound(String result){
        this.result = result;
    }
}
