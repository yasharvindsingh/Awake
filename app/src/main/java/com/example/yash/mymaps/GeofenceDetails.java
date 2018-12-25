package com.example.yash.mymaps;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by YASH on 18-04-2017.
 */

public class GeofenceDetails {
    public  double lat;
    public double lon;
    public  String g;
    public  int r;
    public int t;


    public  void setLat(double m) {
        lat = m;
    }

    public  double getlat(){
        return lat;
    }

    public void setLon(double n){  lon=n;}

    public double getLon(){ return lon; }

    public  void setKey(String s) {
        g = s;
    }

    public  String getKey(){
        return g;
    }

    public  void setradius(int radius) {
        r = radius;
    }

    public  int getradius(){
        return r;
    }

    public void setTime(int time){
        t=time;
    }

    public int getTime(){
        return t;
    }

}