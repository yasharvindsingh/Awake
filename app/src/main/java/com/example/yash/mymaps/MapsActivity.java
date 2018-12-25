package com.example.yash.mymaps;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.support.annotation.IdRes;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.example.yash.mymaps.GeofenceDetails.*;


public class MapsActivity extends FragmentActivity implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnMapReadyCallback {

    private GoogleMap mMap;
    Geocoder geocoder;
    double lat,latchang;
    double lon,lonchang;
    public static final String TAG= "com.example.yash.mymaps";
    int radius,time=1;
    String name;
    String tim;
    EditText nameid,radiusid,timeid;
    LatLng mlatlng;
    Marker marker = null;
    //Location mlastlocation;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mlocationrequest;
   ArrayList <GeofenceDetails> gd=new ArrayList<GeofenceDetails>();
    public static final int MY_FINE_LOCATION_PERMISSION=100;

    SeekBar seekbar;
    ToggleButton blueonoff,wifionoff,alarmonoff;
    BluetoothAdapter  blueadp;
    RadioGroup rg;
    int checked;
    DBhelper helper=new DBhelper(this);
    GeofenceHelper ghelper=new GeofenceHelper(this);
    SettingsValue sv= new SettingsValue();
   // SettingsValue svt=new SettingsValue();
    static String  filename="mysharedprefs";
   public  ArrayList<Geofence> mGeofenceList;
    int progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        mGeofenceList = new ArrayList<Geofence>();
         nameid=(EditText)findViewById(R.id.nameid) ;
         radiusid=(EditText)findViewById(R.id.radiusid) ;
         timeid=(EditText)findViewById(R.id.timeid) ;

        Toast.makeText(this,"on Create",Toast.LENGTH_SHORT).show();


        seekbar= (SeekBar)findViewById(R.id.seekBarBright);
        seekbar.setMax(255);
        blueonoff= (ToggleButton)findViewById(R.id.bluetooth);
        wifionoff= (ToggleButton)findViewById(R.id.wifi);
        alarmonoff= (ToggleButton)findViewById(R.id.alarm);
        rg = (RadioGroup) findViewById(R.id.radio_g);
        blueadp= BluetoothAdapter.getDefaultAdapter();

        int curr_brightness=0;


        try {
            curr_brightness= Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Brightness not found!", Toast.LENGTH_SHORT).show();
        }
        radiusid.addTextChangedListener(filterTextWatcher);
        seekbar.setProgress(curr_brightness);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress=progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sv.setBrightness(progress);
            }



        });

        blueonoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sv.setBluetooth(1);
                }
                else{
                    sv.setBluetooth(0);
                }
            }
        });

        wifionoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sv.setWifi(1);
                }
                else{
                    sv.setWifi(0);
                }
            }
        });


        alarmonoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sv.setAlarm(1);
                }
                else{
                    sv.setAlarm(0);
                }
            }
        });

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                checked = rg.indexOfChild(findViewById(checkedId));
                switch(checked){
                    case 0:
                        sv.setNormal(1);
                        sv.setSilent(0);
                        sv.setVibrate(0);
                        break;
                    case 1:
                        sv.setNormal(0);
                        sv.setSilent(0);
                        sv.setVibrate(1);
                        break;
                    case 2:
                        sv.setNormal(0);
                        sv.setSilent(1);
                        sv.setVibrate(0);
                        break;
                    default:
                        break;
                }

            }
        });



    }

    TextWatcher filterTextWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!radiusid.getText().toString().equals("")) {
                radius = Integer.parseInt(s.toString());

                mMap.clear();
                marker = mMap.addMarker(new MarkerOptions().position(mlatlng).title(mlatlng.latitude + ":" + mlatlng.longitude));
                mMap.addCircle(new CircleOptions().center(mlatlng).radius(radius)
                        .strokeColor(Color.BLUE).fillColor(Color.argb(64, 0, 0, 255)));
            }
            else{
                mMap.clear();
                marker = mMap.addMarker(new MarkerOptions().position(mlatlng).title(mlatlng.latitude + ":" + mlatlng.longitude));
            }
            lat=mlatlng.latitude;
            lon=mlatlng.longitude;
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };




    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void onSearch(View view) {
        //Handler mHandler = new Handler();
        if (marker != null) {
            marker.remove();
            mMap.clear();
        }
        EditText loc_input = (EditText) findViewById(R.id.loc);
        String loc_add = loc_input.getText().toString();
        List<Address> addressList = new ArrayList<Address>();
        geocoder = new Geocoder(this);
        Address address = null;

        Toast.makeText(this,"looking for"+loc_add,Toast.LENGTH_LONG).show();



        try {

            while(addressList.size()==0)
            {
                addressList = geocoder.getFromLocationName(loc_add, 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            address = addressList.get(0);

            lat = address.getLatitude();
            lon = address.getLongitude();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        mlatlng = new LatLng(address.getLatitude(), address.getLongitude());
        CameraPosition cp = new CameraPosition.Builder().target(mlatlng).zoom(14).build();
        marker = mMap.addMarker(new MarkerOptions().position(mlatlng).title(lat + ":" + lon));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        mMap.addCircle(new CircleOptions().center(mlatlng).radius(radius)
                .strokeColor(Color.BLUE).fillColor(Color.argb(64,0,0,255)));

    }


    public void onOk(View view) {

        sv.setLatitude(lat);
        sv.setLongitude(lon);
        name = nameid.getText().toString();

        Toast.makeText(this,name + " "+ sv.getBrightness()+" "+ sv.getWifi()+" "+sv.getSilent(),Toast.LENGTH_SHORT).show();
        long v=0;
        sv.setName(name);
       // sv.setName(name);
        //START FROM HERE ON 17 APril
        try {
            tim=timeid.getText().toString();
            if(tim!="" )
            time = Integer.parseInt(tim);
        }
        catch(Exception e){
            Log.e(TAG,e.getLocalizedMessage());
            Toast.makeText(this,""+e,Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this,"Event Created",Toast.LENGTH_SHORT).show();
        try {
            v= ghelper.insertGB(name, lat, lon, radius,time);
        }catch(Exception e ){
            Toast.makeText(this,"ghelper:"+e,Toast.LENGTH_SHORT).show();
        }
        try {
            long b=helper.insertDB(sv);
            Toast.makeText(this,""+b,Toast.LENGTH_SHORT).show();
        }catch(Exception e ){
            Toast.makeText(this,"dhelper:"+e,Toast.LENGTH_SHORT).show();
        }
        Intent i = new Intent(MapsActivity.this, MainActivity.class);
        i.putExtra("number",v);
        startActivity(i);

    }






    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Add a marker in Sydney and move the camera
        mlatlng = new LatLng(16.8459, 74.6013);
        CameraPosition cp = new CameraPosition.Builder().target(mlatlng).zoom(18).build();
        marker = mMap.addMarker(new MarkerOptions().position(mlatlng).title("WCE"));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));

        // Setting a click event handler for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                // Creating a marker
                lat = latLng.latitude;
                lon = latLng.longitude;
                mlatlng=new LatLng(lat,lon);

                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                // Clears the previously touched position
                mMap.clear();

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);
                mMap.addCircle(new CircleOptions().center(mlatlng).radius(radius)
                        .strokeColor(Color.BLUE).fillColor(Color.argb(64,0,0,255)));
            }
        });


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
       // Toast.makeText(this, "onConnected has been called", Toast.LENGTH_SHORT).show();
        mlocationrequest = LocationRequest.create();
        mlocationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationrequest.setInterval(10000);
        requestLocationUpdates();

        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }

    public void requestLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},MY_FINE_LOCATION_PERMISSION);
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

    public void onMyLoc(View view) {


        if(marker!=null)
        {
            marker.remove();
            mMap.clear();
        }
        lat=latchang;
        lon=lonchang;



        Toast.makeText(this,"My Location",Toast.LENGTH_SHORT).show();
        Toast.makeText(this,"latitude:"+lat+" "+"Longitude:"+lon,Toast.LENGTH_SHORT).show();
        mlatlng = new LatLng(lat, lon);
        CameraPosition cp = new CameraPosition.Builder().target(mlatlng).zoom(14).build();
        marker= mMap.addMarker(new MarkerOptions().position(mlatlng).title("My Location!!"));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        mMap.addCircle(new CircleOptions().center(mlatlng).radius(radius)
                .strokeColor(Color.BLUE).fillColor(Color.argb(64,0,0,255)));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
        Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            Toast.makeText(this,"Stop",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onLocationChanged(Location location) {

        latchang=location.getLatitude();
        lonchang=location.getLongitude();
    }

   /* @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }*/
}
