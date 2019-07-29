package com.rtg.navigationwtd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String TAG = "DatabaseHelper";
    public static final String DATABASE = "FAVORITES";
    public static final String TABLE_NAME = "LOCATIONS";
    public static final int DATABASE_VERSION = 1;
    public static final String KEY_ID = "_id";
    public static final String LABEL = "Label";
    public static final String ADDRESS = "Address";
    public static final String COORDINATES = "Coords";

    //create table MY_DATABASE (ID integer primary key, label,address,coordinates);
    private static final String SCRIPT_CREATE_DATABASE =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + LABEL + " TEXT, "
                    + ADDRESS + " TEXT, "
                    + COORDINATES + " TEXT);";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = SCRIPT_CREATE_DATABASE;
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String label, String address, String coords) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LABEL, label);
        contentValues.put(ADDRESS, address);
        contentValues.put(COORDINATES, coords);

        Log.d(TAG, "addData: Adding " + label + " to " + TABLE_NAME);
        Log.d(TAG, "addData: Adding " + address + " to " + TABLE_NAME);
        Log.d(TAG, "addData: Adding " + coords + " to " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public Cursor getItemID(String label){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + KEY_ID + " FROM " + TABLE_NAME +
                " WHERE " + LABEL + " = '" + label + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public void updateLabel(String newLabel, int id, String oldLabel){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + LABEL +
                " = '" + newLabel + "' WHERE " + KEY_ID + " = '" + id + "'" +
                " AND " + LABEL + " = '" + oldLabel + "'";
        Log.d(TAG, "updateLabel: query: " + query);
        Log.d(TAG, "updateLabel: Setting name to " + newLabel);
        db.execSQL(query);
    }

    public void updateAddress(String newAddress, int id, String oldAddress){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + ADDRESS +
                " = '" + newAddress + "' WHERE " + KEY_ID + " = '" + id + "'" +
                " AND " + ADDRESS + " = '" + oldAddress + "'";
        Log.d(TAG, "updateAddress: query: " + query);
        Log.d(TAG, "updateAddress: Setting name to " + newAddress);
        db.execSQL(query);
    }

    public void updateCoords(String newCoords, int id, String oldCoords){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + COORDINATES +
                " = '" + newCoords + "' WHERE " + KEY_ID + " = '" + id + "'" +
                " AND " + COORDINATES + " = '" + oldCoords + "'";
        Log.d(TAG, "updateCoords: query: " + query);
        Log.d(TAG, "updateCoords: Setting name to " + newCoords);
        db.execSQL(query);
    }

    public void deleteLocation(int id, String label, String address, String coords){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE "
                + KEY_ID + " = '" + id + "'" +
                " AND " + LABEL + " = '" + label + "'" +
                " AND " + ADDRESS + " = '" + address + "'" +
                " AND " + COORDINATES + " = '" + coords + "'";
        Log.d(TAG, "deleteLocation: query: " + query);
        Log.d(TAG, "deleteLocation: Deleting " + label + ", " + address + ", " + coords + " from database.");
        db.execSQL(query);
    }

//    public String getCoordsFromLabel()

}