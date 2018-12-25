package com.example.yash.mymaps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;

/**
 * Created by YASH on 19-04-2017.
 */

public class GeofenceHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="geofence.db";
    private static final String TABLE_GEO="geofence";
    private static final String COLUMN_KEY="_key";
    private static final String COLUMN_NAME="name";
    private static final String COLUMN_LATITUDE="latitude";
    private static final String COLUMN_LONGITUDE="longitude";
    private static final String COLUMN_RADIUS="radius";
    private static final String COLUMN_TIME="time";
    SQLiteDatabase db;
    GeofenceDetails gd=new GeofenceDetails();
    private static final String TABLE_CREATE="CREATE TABLE IF NOT EXISTS geofence(_key INTEGER PRIMARY KEY AUTOINCREMENT ," +
            " name VARCHAR(50) ,latitude REAL ," +
            " longitude  REAL,radius INTEGER ,time INTEGER); ";


    public GeofenceHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        this.db=db;
    }

    public long insertGB(String name, double lat, double lon, int radius,int time) {
        db = this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(COLUMN_LATITUDE,lat);
        values.put(COLUMN_LONGITUDE,lon);
        values.put(COLUMN_NAME,name);
        values.put(COLUMN_RADIUS,radius);
        values.put(COLUMN_TIME,time);

       long v= db.insert(TABLE_GEO,null,values);
        db.close();
        return v;
    }

    public GeofenceDetails searchGB(int i) {
        db = this.getReadableDatabase();
        LinkedList list = new LinkedList();
        String query = "SELECT * FROM " + TABLE_GEO + " WHERE _key="+ i +";";

        Cursor c = db.rawQuery(query,null);
        if (c != null) {
            c.moveToFirst();
            gd.setKey(c.getString(c.getColumnIndex(COLUMN_NAME)));
            gd.setLat(c.getDouble(c.getColumnIndex(COLUMN_LATITUDE)));
            gd.setLon(c.getDouble(c.getColumnIndex(COLUMN_LONGITUDE)));
            gd.setradius(c.getInt(c.getColumnIndex(COLUMN_RADIUS)));
            gd.setTime(c.getInt(c.getColumnIndex(COLUMN_TIME)));

        }
        else{
            gd=null;
        }
        db.close();
        return gd;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query="DROP TABLE IF EXISTS "+TABLE_GEO;
        db.execSQL(query);
        this.onCreate(db);

    }
}
