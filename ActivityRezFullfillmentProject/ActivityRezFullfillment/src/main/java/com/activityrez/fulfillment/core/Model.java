package com.activityrez.fulfillment.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.activityrez.fulfillment.ARContainer;

import roboguice.RoboGuice;

/**
 * Created by alex on 10/16/13.
 */
public class Model extends Observable {
    public Model(){
        this(null);
    }
    public Model(HashMap<String,Object> data){
        RoboGuice.getInjector(ARContainer.context).injectMembers(this);
        ARContainer.bus.register(this);
        if(data != null)
            this.hydrate(data);
    }

    public Object get(String field){
        try {
            Field f = this.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(this);
        } catch(NoSuchFieldException e){
            //Log.e("model","field [" + field + "] does not exist");
            return null;
        } catch(IllegalAccessException e){
            //Log.e("model","field [" + field + "] cannot be accessed");
            return null;
        }
    }

    public void set(String field, Object val){
        try {
            Field f = this.getClass().getDeclaredField(field);
            f.setAccessible(true);

            if(f.get(this) == val) return;

            f.set(this,val);

            this.setChanged();
            this.notifyObservers();
        } catch(NoSuchFieldException e){
            //Log.e("model","field [" + field + "] does not exist");
        } catch(IllegalAccessException e){
            //Log.e("model","field [" + field + "] cannot be accessed");
        }
    }

    public void hydrate(HashMap<String,Object> data){
        this.hydrate(data,false);
    }
    public void hydrate(Object _data, boolean from_json){
        HashMap<String,Object> data;

        if(from_json){
            try {
                data = (HashMap<String,Object>) _fromJson(_data);
            }catch(Exception e){
                return;
            }
        } else {
            data = (HashMap<String,Object>) _data;
        }

        Class c = this.getClass();
        for(Field f : c.getDeclaredFields()){
            if(!data.containsKey(f.getName())) continue;
            f.setAccessible(true);
            if(!f.getType().isArray()){
                cleanSet(f, data.get(f.getName()));
            } else if(data.get(f.getName()) instanceof ArrayList){
                cleanArraySet(f, (ArrayList) data.get(f.getName()));
            }
        }
    }
    public Object out(){
        return this.out(false);
    }
    public Object out(boolean as_json){
        ArrayList<String> fs = new ArrayList<String>();
        for(Field f : this.getClass().getDeclaredFields()){
            fs.add(f.getName());
        }
        return this.out(fs,as_json);
    }
    public Object out(ArrayList fields, boolean as_json){
        if(fields == null)
            out(as_json);

        HashMap<String,Object> map = new HashMap<String, Object>();
        for(Field f : this.getClass().getDeclaredFields()){
            if(!fields.contains(f.getName())) continue;
            try {
                f.setAccessible(true);
                if(Model.class.isAssignableFrom(f.getType())){
                    Model _m = (Model)f.get(this);
                    if(_m != null)
                        map.put(f.getName(),_m.out());
                } else if(f.getType().isArray()){
                    Object[] z = (Object[])f.get(this);
                    Class subtype = f.getType().getComponentType();
                    List o = new ArrayList();
                    if(Model.class.isAssignableFrom(subtype)){
                        for(int ni = 0; ni < z.length; ni++)
                            o.add(((Model)z[ni]).out());
                    } else {
                        for(int ni = 0; ni < z.length; ni++)
                            o.add(z[ni]);
                    }
                    map.put(f.getName(),o);
                } else {
                    map.put(f.getName(),f.get(this));
                }
            }catch(IllegalAccessException e){
                //Log.e("model", "cant touch field [" + f.getName() + "]");
            }
        }
        if(!as_json)
            return map;
        return _toJson(map);
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
        return json;
    }
    private static Object _toJson(Object obj){
        if(obj instanceof Map) {
            JSONObject json = new JSONObject();
            Map map = (Map) obj;
            for (Object key : map.keySet()) {
                try {
                    json.put(key.toString(), _toJson(map.get(key)));
                } catch(Exception e){}
            }
            return json;
        } else if (obj instanceof Iterable) {
            JSONArray json = new JSONArray();
            for (Object value : ((Iterable)obj)) {
                json.put(value);
            }
            return json;
        } else {
            return obj;
        }
    }
    private void cleanSet(Field f, Object val){
        try {
            if(Model.class.isAssignableFrom(f.getType())){
                Class _c = f.getType();
                try {
                    Model _m = (Model)_c.getDeclaredConstructor().newInstance();
                    _m.hydrate(val,false);
                    f.set(this,_m);
                } catch(Exception e){
                }
            } else {
                String type = f.getType().toString();
                if(type.equals("int")){
                    f.set(this,Integer.valueOf(val.toString()));
                } else if(type.equals("class java.lang.String")){
                    if(val != null && val.toString().length() > 0)
                        f.set(this,val.toString());
                    else
                        f.set(this,"");
                } else if(type.equals("boolean")){
                    f.set(this,((Boolean)val).booleanValue());
                } else {
                    //Log.i("unaccounted for type",type);
                    f.set(this,val);
                }
            }
        } catch(IllegalAccessException e){ e.printStackTrace(); }
    }
    private void cleanArraySet(Field f, ArrayList val){
        Class type = f.getType().getComponentType();
        Object[] outr = (Object[])Array.newInstance(type,val.size());

        for(int ni = 0; ni < val.size(); ni++){
            try {
                if(Model.class.isAssignableFrom(type)){
                    Model _m = (Model)type.getDeclaredConstructor().newInstance();
                    _m.hydrate(val.get(ni),false);
                    outr[ni] = _m;
                } else {
                    outr[ni] = type.cast(val.get(ni));
                }
            }catch(Exception e){ e.printStackTrace(); }
        }
        try{ f.set(this,outr); }catch(Exception e){ e.printStackTrace(); }
    }
}
