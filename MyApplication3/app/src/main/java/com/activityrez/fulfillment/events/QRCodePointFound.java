package com.activityrez.fulfillment.events;

import com.google.zxing.ResultPoint;

/**
 * Created by alex on 10/30/13.
 */
public class QRCodePointFound {
    public ResultPoint point;
    public QRCodePointFound(ResultPoint point){ this.point = point; }
}
