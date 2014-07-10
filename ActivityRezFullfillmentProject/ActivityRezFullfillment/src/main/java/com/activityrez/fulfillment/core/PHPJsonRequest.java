package com.activityrez.fulfillment.core;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 10/21/13.
 */
public class PHPJsonRequest extends Request<JSONObject> {
    //private static final String PROTOCOL_CHARSET = "utf-8";
    //private static final String PROTOCOL_CONTENT_TYPE =
    //        String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    private int mMethod;
    private JSONObject mBody;
    private Response.Listener<JSONObject> mListener;

    public PHPJsonRequest(int method, String url, JSONObject jsonRequest,
                             Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method,url,errorListener);
        mMethod = method;
        mBody = jsonRequest;
        mListener = listener;
    }

    @Override
    public String getUrl() {
        if(mMethod == Method.POST)
            return super.getUrl();
        String body = "";
        try {
            body = new String(getBody());
        }catch(AuthFailureError e){
        }
        return super.getUrl() + (body.length()>0?"?"+body:"");
    }

    public byte[] getBody() throws AuthFailureError {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String,String> m = null;
        try {
            m = (Map<String,String>) _fromJson(mBody);
        } catch(Exception e){
            //Log.i("params","error parsing object");
        }
        return m;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject o) {
        mListener.onResponse(o);
    }

    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    private static Object _fromJson(Object json) throws JSONException {
        if(json == JSONObject.NULL)
            return null;

        if(json instanceof JSONObject){
            Map<String, Object> map = new HashMap();
            JSONObject obj = (JSONObject) json;
            Iterator keys = obj.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                map.put(key, _fromJson(obj.get(key)));
            }
            return map;
        }
        if(json instanceof JSONArray){
            List list = new ArrayList();
            JSONArray array = (JSONArray)json;
            for (int i = 0; i < array.length(); i++) {
                list.add(_fromJson(array.get(i)));
            }
            return list;
        }
        return json.toString();
    }

}
