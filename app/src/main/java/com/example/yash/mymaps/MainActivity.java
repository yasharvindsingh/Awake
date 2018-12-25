package com.example.yash.mymaps;

import android.*;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Map;

import static com.example.yash.mymaps.GeofenceDetails.*;

public class MainActivity extends AppCompatActivity implements ResultCallback<Status>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    Button bt,bts;
    double lat,lon;
    Intent intent;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mlocationrequest;
    public ArrayList<GeofenceDetails> gd=new ArrayList<GeofenceDetails>();
    public static final int MY_FINE_LOCATION_PERMISSION=100;
    DBhelper helper=new DBhelper(this);
    SettingsValue svt;
    public static ArrayList<Geofence> mGeofenceList;

    GeofenceDetails gdd;
    GeofenceHelper geofenceHelper=new GeofenceHelper(this);
    public static PendingIntent mPendingIntent;
    MapsActivity m=new MapsActivity();
    ArrayList<String> ListDisplay = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt=(Button)findViewById(R.id.btaccess);
        mGeofenceList = new ArrayList<Geofence>();
        Toast.makeText(this,"mainonCreate",Toast.LENGTH_SHORT).show();

        populategeofence();

        ListAdapter displaylistadp = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,ListDisplay);
        ListView displaylistview = (ListView)findViewById(R.id.displaylist);
        displaylistview.setAdapter(displaylistadp);

        buildGoogleApiClient();



        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in=new Intent(MainActivity.this,MapsActivity.class);
                startActivity(in);
            }
        });

    }

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
            intent = new Intent(this, GeoFenceTransitionIntentService.class);
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
            Toast.makeText(this, "Inisde getGeofencePendingIntent", Toast.LENGTH_SHORT).show();
            return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mlocationrequest = LocationRequest.create();
        mlocationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationrequest.setInterval(10000);
        requestLocationUpdates();
    }

   public void requestLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},MY_FINE_LOCATION_PERMISSION);
            }
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mlocationrequest, this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case MY_FINE_LOCATION_PERMISSION:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                }
                else{
                    Toast.makeText(getApplicationContext(),"The permissions are required!!",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            Toast.makeText(this,"Start",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
          mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void  populategeofence(){

        int i=1;
        try {
            while ((gdd=geofenceHelper.searchGB(i))!=null) {

                mGeofenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(gdd.getKey())

                        // Set the circular region of this geofence.
                        .setCircularRegion(
                                gdd.getlat(),
                                gdd.getLon(),
                                gdd.getradius()
                        )

                        .setExpirationDuration(gdd.getTime()*86400000)


                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER )

                        // Create the geofence.
                        .build());
                ListDisplay.add(gdd.getKey());
                Toast.makeText(this, "Lat" + gdd.getlat()+ ": Lon" + gdd.getLon() + "added to geofence list", Toast.LENGTH_SHORT).show();
                svt=helper.searchDB(gdd.getKey());
                if(svt!=null) {
                    Toast.makeText(this, "Attributes : " + svt.getBrightness() + " " + svt.getSilent() + " " + svt.getWifi() + " " + svt.getBluetooth() + " "
                            + svt.getNormal() + " " + svt.getVibrate()+" "+svt.getAlarm(), Toast.LENGTH_SHORT).show();
                    i++;
                }
            }


        }catch(Exception e){
            Toast.makeText(this,"Geofence List Size: "+mGeofenceList.size(),Toast.LENGTH_SHORT).show();
            Toast.makeText(this,""+e,Toast.LENGTH_SHORT).show();
        }


    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    @Override
    public void onResult(@NonNull Status status) {
        try {
            if (status.isSuccess()) {
                Toast.makeText(
                        this,
                        "Geofences Added",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                // Get the status code for the error and log it using a user-friendly message.
                String errorMessage = GeoFenceErrorMessages.getErrorString(this,
                        status.getStatusCode());
                Toast.makeText(
                        this,
                        errorMessage,
                        Toast.LENGTH_SHORT
                ).show();

            }
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"errorin onresult",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public void onAddGeofences(View view){
        if(!mGoogleApiClient.isConnected()){
            Toast.makeText(this,"Location Services not connected",Toast.LENGTH_SHORT).show();
        }
               if(mPendingIntent!=null) {
                   LocationServices.GeofencingApi.removeGeofences(
                           mGoogleApiClient,
                           // This is the same pending intent that was used in addGeofences().
                           mPendingIntent
                   ).setResultCallback(this); // Result processed in onResult().
                   Toast.makeText(this,"Removing Geofences",Toast.LENGTH_SHORT).show();
               }
             try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    mPendingIntent=getGeofencePendingIntent()
            ).setResultCallback(this);
            // Result processed in onResult().
            Toast.makeText(this,"Intent fired",Toast.LENGTH_SHORT).show();
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Toast.makeText(this,""+securityException,Toast.LENGTH_SHORT).show();
        }
    }
}
