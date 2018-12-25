package com.example.yash.mymaps;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.location.GeofenceStatusCodes;

/**
 * Created by YASH on 16-04-2017.
 */

public class GeoFenceErrorMessages {

    private GeoFenceErrorMessages(){}

    public static String getErrorString(Context context,int errorcode){
        Resources resources=context.getResources();
        switch(errorcode)
        {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence not available";
            case  GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "you have registered too many geofences";
            case  GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "you have provided too many pending intents to addgeofences ";
            default:
                return "Unknown Error: geofence service not available";
        }

    }
}
