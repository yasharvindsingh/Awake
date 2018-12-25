package com.example.yash.mymaps;


public class SettingsValue {

    private double latitude, longitude;
    private int brightness, wifi, airplane , silent, vibrate , normal, bluetooth;
    private String name;

    public void setLatitude(double lat){
        this.latitude=lat;
    }

    public double getLatitude(){
        return latitude;
    }

    public void setName(String n){this.name=n;}

    public String getName(){return name;}

    public void setLongitude(double longi){
        this.longitude=longi;
    }

    public double getLongitude(){
        return longitude;
    }


    public void setBrightness(int bright){
        this.brightness=bright;
    }

    public int getBrightness(){
        return brightness;
    }


    public void setWifi(int wifi){
        this.wifi=wifi;
    }

    public int getWifi(){
        return wifi;
    }


    public void setAlarm(int air){
        this.airplane=air;
    }

    public int getAlarm(){
        return airplane;
    }


    public void setSilent(int s){
        this.silent=s;
    }

    public int getSilent(){
        return silent;
    }


    public void setVibrate(int vib){
        this.vibrate=vib;
    }

    public int getVibrate(){
        return vibrate;
    }


    public void setNormal(int nor){
        this.normal=nor;
    }

    public int getNormal(){
        return normal;
    }


    public void setBluetooth(int b){
        this.bluetooth=b;
    }

    public int getBluetooth(){
        return bluetooth;
    }


}
