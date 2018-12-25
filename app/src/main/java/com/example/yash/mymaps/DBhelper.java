package com.example.yash.mymaps;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;
import android.os.Handler;
import android.widget.Toast;

import java.util.LinkedList;

public class DBhelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="awake.db";
    private static final String TABLE_AWAKE="awake";
    private static final String COLUMN_LATITUDE="latitude";
    private static final String COLUMN_LONGITUDE="longitude";
    private static final String COLUMN_BRIGHTNESS="brightness";
    private static final String COLUMN_WIFI="wifi";
    private static final String COLUMN_NAMEID="nameid";
    private static final String COLUMN_SILENT="silent";
    private static final String COLUMN_VIBRATE="vibrate";
    private static final String COLUMN_NORMAL="normal";
    private static final String COLUMN_BLUETOOTH="bluetooth";
    private static final String COLUMN_ALARM="alarm";
    SQLiteDatabase db;
    SettingsValue svDB=new SettingsValue();
    Context con;

    long b;

    private static final String TABLE_CREATE="CREATE TABLE IF NOT EXISTS awake(latitude REAL, longitude REAL," +
            "brightness INTEGER, wifi INTEGER, silent INTEGER , vibrate INTEGER," +
            "normal INTEGER, bluetooth INTEGER, alarm INTEGER, nameid VARCHAR(50)); ";

    public DBhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        con=context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
     db.execSQL(TABLE_CREATE);
        this.db=db;

    }

    public long insertDB(SettingsValue sv){

        db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(COLUMN_NAMEID,sv.getName());
        values.put(COLUMN_LATITUDE,sv.getLatitude());
        values.put(COLUMN_LONGITUDE,sv.getLongitude());
        values.put(COLUMN_BRIGHTNESS,sv.getBrightness());
        values.put(COLUMN_WIFI,sv.getWifi());
        values.put(COLUMN_NORMAL,sv.getNormal());
        values.put(COLUMN_VIBRATE,sv.getVibrate());
        values.put(COLUMN_SILENT,sv.getSilent());
        values.put(COLUMN_BLUETOOTH,sv.getBluetooth());
        values.put(COLUMN_ALARM,sv.getAlarm());

        b=db.insert(TABLE_AWAKE, null, values);

        db.close();

        return b;
    }

    public SettingsValue searchDB(String id){
        Cursor c;
            db = this.getReadableDatabase();


        String query = "SELECT * FROM " + TABLE_AWAKE + " WHERE " + COLUMN_NAMEID + "='" + id +"';";
         try{
             c = db.rawQuery(query, null);

            if (c.moveToFirst()) {

                svDB.setBrightness(c.getInt(c.getColumnIndex(COLUMN_BRIGHTNESS)));
                svDB.setWifi(c.getInt(c.getColumnIndex(COLUMN_WIFI)));
                svDB.setSilent(c.getInt(c.getColumnIndex(COLUMN_SILENT)));
                svDB.setVibrate(c.getInt(c.getColumnIndex(COLUMN_VIBRATE)));
                svDB.setNormal(c.getInt(c.getColumnIndex(COLUMN_NORMAL)));
                svDB.setBluetooth(c.getInt(c.getColumnIndex(COLUMN_BLUETOOTH)));
                svDB.setAlarm(c.getInt(c.getColumnIndex(COLUMN_ALARM)));
            } else {
                svDB = null;
            }
            c.close();
             //db.close();
        }
        catch(Exception e){
            Toast.makeText(con, e.getLocalizedMessage()+"in rawquery", Toast.LENGTH_SHORT).show();

        }
            return svDB;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query="DROP TABLE IF EXISTS "+TABLE_AWAKE;
        db.execSQL(query);
        this.onCreate(db);

    }

}
