package com.example.yash.mymaps;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class GeoFenceTransitionIntentService extends IntentService {

    protected static final String TAG="gfservice";
    String trigger;
    Location location;
    Double lat,lon;
    Context context;
    DBhelper db ;
    Handler mHandler;
    BluetoothAdapter blueADP;
    AudioManager audioManager;
    AlarmManager alarmManager;
    WifiManager onwifi,offwifi;
    SettingsValue svDB;
    GeofencingEvent geofencingEvent;
    int geofencetransition;
    List<Geofence> trigeringgeofences;


    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public GeoFenceTransitionIntentService(){
        super(TAG);
    }


    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    public void onCreate(){
        super.onCreate();

        try {
            mHandler=new Handler();
            svDB= new SettingsValue();
            db = new DBhelper(this);
            context = getApplicationContext();
            audioManager = (AudioManager) getSystemService(context.AUDIO_SERVICE);
            onwifi = (WifiManager) this.getSystemService(context.WIFI_SERVICE);
            offwifi= (WifiManager) this.getSystemService(context.WIFI_SERVICE);
            alarmManager=(AlarmManager) getSystemService(context.ALARM_SERVICE);
            blueADP = BluetoothAdapter.getDefaultAdapter();
        }catch(Exception e){
            Toast.makeText(context,""+e,Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(context,"GeoTest",Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
             geofencingEvent = GeofencingEvent.fromIntent(intent);
        }
        catch(Exception e){
           final String n;
            n=""+e;
           mHandler.post(new Runnable() {
               @Override
               public void run() {
                 Toast.makeText(GeoFenceTransitionIntentService.this,n,Toast.LENGTH_SHORT).show();
               }
           });
        }
        if(geofencingEvent.hasError()){
            String errormessage=GeoFenceErrorMessages.getErrorString(this,geofencingEvent.getErrorCode());
            Toast.makeText(context,errormessage,Toast.LENGTH_SHORT).show();
            return;
        }
        try {
             geofencetransition = geofencingEvent.getGeofenceTransition();
        }
        catch(Exception e){
            final String n;
            n=""+e;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(GeoFenceTransitionIntentService.this,n,Toast.LENGTH_SHORT).show();
                }
            });
        }
        if(geofencetransition== Geofence.GEOFENCE_TRANSITION_ENTER ){


            try {
                trigeringgeofences= geofencingEvent.getTriggeringGeofences();
                trigger=trigeringgeofences.get(0).getRequestId();
                /*location = geofencingEvent.getTriggeringLocation();
                lat = location.getLatitude();
                lon = location.getLongitude();*/
            }
            catch(Exception e){
                final String n;
                n=""+e;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GeoFenceTransitionIntentService.this,n,Toast.LENGTH_SHORT).show();
                    }
                });
            }


            String geofencetransitiondetails=getGeofenceTransitionDetails(
                this,
                geofencetransition, trigeringgeofences
            );

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(GeoFenceTransitionIntentService.this,"intent Service Working!!"+trigger,Toast.LENGTH_SHORT).show();
                }
            });


            try {
                svDB = db.searchDB(trigger);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GeoFenceTransitionIntentService.this,"Function "+svDB.getBrightness(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
            catch(Exception e){
                final String n;
                n=""+e+" from searchdb";
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GeoFenceTransitionIntentService.this,n,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,svDB.getBrightness());

           if(svDB.getWifi()==1){
                        onwifi.setWifiEnabled(true);
            }
            else{
                offwifi.setWifiEnabled(false);
            }
        if(svDB.getBluetooth()==1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                blueADP.enable();
            }

        }
        if(svDB.getAlarm()==1){
            Intent i=new Intent(GeoFenceTransitionIntentService.this,MyAlarm.class);
            PendingIntent pi=PendingIntent.getBroadcast(this.getApplicationContext(),1,i,0);
            alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+(1*1000),pi);
        }

        if(svDB.getVibrate()==1){
                 audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }
        if(svDB.getSilent()==1){
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

            }
        if(svDB.getNormal()==1){

                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

            }

            //sendNotification(geofencetransitiondetails);
            Log.i(TAG, geofencetransitiondetails);
        } else {
            // Log the error.
            Log.e(TAG, "Invalid Geofence Transition");
        }
    }



    private String getGeofenceTransitionDetails(Context context,int geofencetransition,List<Geofence> trigeringgeofences){

        String  geofenceTransitionString=getTransitionString(geofencetransition);
        ArrayList trigeringGeofencesIDslist= new ArrayList();

        for(Geofence geofence: trigeringgeofences)
        {
            trigeringGeofencesIDslist.add(geofence.getRequestId());

        }
        String trigeringgeofencesIDsString= TextUtils.join(",",trigeringGeofencesIDslist);
        return geofenceTransitionString+": "+trigeringgeofencesIDsString;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Entering Geofence";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Exiting Gofence";
            default:
                return "Unknown Geofence Transition";
        }
    }



   /* private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        //Give App icon
        builder.setSmallIcon(R.drawable.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }   */

}


