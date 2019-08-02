package com.rtg.navigationwtd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.sql.SQLInput;


public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String TAG = "DatabaseHelper";
    public static final String DATABASE = "FAVORITES";
    public static final String TABLE_NAME = "LOCATIONS";
    public static final int DATABASE_VERSION = 1;
    public static final String KEY_ID = "id";
    public static final String LABEL = "Label";
    public static final String ADDRESS = "Address";
    public static final String COORDINATES = "Coords";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 2);
    }

//    private static final String SCRIPT_CREATE_DATABASE =
//            "CREATE TABLE " + TABLE_NAME + " ("
//                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//                    + LABEL + " TEXT, "
//                    + ADDRESS + " TEXT, "
//                    + COORDINATES + " TEXT);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS LOCATIONS (id INTEGER PRIMARY KEY AUTOINCREMENT, Label TEXT, Address TEXT, Coords TEXT)";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.execute();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        SQLiteStatement stmt = db.compileStatement("DROP TABLE IF EXISTS " + TABLE_NAME);
        stmt.execute();
        onCreate(db);
    }

    public boolean addData(String label, String address, String coords) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "INSERT INTO LOCATIONS(Label,Address,Coords) VALUES(?,?,?)";
        SQLiteStatement statement = db.compileStatement(query);
        statement.bindString(1,label);
        statement.bindString(2,address);
        statement.bindString(3,coords);
        long rowId = statement.executeInsert();

        if (rowId == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM LOCATIONS";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public Cursor getItemID(String label){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT id FROM LOCATIONS WHERE Label = ?";
        Cursor data = db.rawQuery(query, new String[] {label});
        return data;
    }

    public void updateRow(int id, String columnName, String newData){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE LOCATIONS SET " + columnName + "=? WHERE id = ?";
        SQLiteStatement statement = db.compileStatement(query);
        statement.bindString(1,newData);
        statement.bindLong(2,id);
        int numberOfRowsAffected = statement.executeUpdateDelete();
    }

    public void deleteLocation(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            String sql = "DELETE FROM " + TABLE_NAME +
                    " WHERE " + KEY_ID + " = ?";
            SQLiteStatement statement = db.compileStatement(sql);

            statement.clearBindings();
            statement.bindLong(1, id);
            statement.executeUpdateDelete();

            db.setTransactionSuccessful();

        } catch (SQLException e) {
            Log.w("Exception:", e);
        } finally {
            db.endTransaction();
        }
    }
}