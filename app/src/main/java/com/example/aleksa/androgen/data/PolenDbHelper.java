package com.example.aleksa.androgen.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.aleksa.androgen.data.PolenContract.LocationEntry;
import com.example.aleksa.androgen.data.PolenContract.PlantEntry;
import com.example.aleksa.androgen.data.PolenContract.PolenEntry;

/*
    Database helper class for the Polen database
 */
public class PolenDbHelper extends SQLiteOpenHelper {

    // Must manually increase this each time database schema is changed
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "polen.db";

    public PolenDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // The CREATE statement for the locations table
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME +
                " (" + LocationEntry.COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY, " +
                LocationEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LONGITUDE + " REAL NOT NULL" +
                " );";

        // The CREATE statement for the plants table
        final String SQL_CREATE_PLANT_TABLE = "CREATE TABLE " + PlantEntry.TABLE_NAME +
                " (" + PlantEntry.COLUMN_PLANT_ID + " INTEGER PRIMARY KEY, " +
                PlantEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                PlantEntry.COLUMN_PLANT_ALLERGENIC_INDEX + " INTEGER NOT NULL" +
                " );";

        // The CREATE statement for the polen table
        //
        final String SQL_CREATE_POLEN_TABLE =
                "CREATE TABLE " + PolenEntry.TABLE_NAME + " (" +

                        // The ID to the measurement entry
                        // It is autoincremented so it is sorted accordingly
                        PolenEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        PolenEntry.COLUMN_PLANT_ID + " INTEGER NOT NULL, " +
                        PolenEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                        PolenEntry.COLUMN_LOCATION_ID + " INTEGER NOT NULL, " +
                        PolenEntry.COLUMN_CONCENTRATION + " INTEGER NOT NULL, " +
                        PolenEntry.COLUMN_TENDENCY + " INTEGER, " +

                        // Set up foreign keys from location and plant tables
                        // Replace rows that have the same parameters in those 3
                        " FOREIGN KEY (" + PolenEntry.COLUMN_LOCATION_ID +") REFERENCES " +
                        LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +

                        "FOREIGN KEY (" + PolenEntry.COLUMN_PLANT_ID +") REFERENCES " +
                        PlantEntry.TABLE_NAME + " (" + PlantEntry._ID + "), " +

                        // Assure that there is only one entry per plant per location per day
                        " UNIQUE (" + PolenEntry.COLUMN_DATE +", " +
                        PolenEntry.COLUMN_PLANT_ID + ", " +
                        PolenEntry.COLUMN_LOCATION_ID + ") ON CONFLICT REPLACE);";


        db.execSQL(SQL_CREATE_LOCATION_TABLE);
        db.execSQL(SQL_CREATE_PLANT_TABLE);
        db.execSQL(SQL_CREATE_POLEN_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Since this is only executed when the database version changes
        // just drop the existing tables and create new ones
        db.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PlantEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PolenEntry.TABLE_NAME);

        onCreate(db);
    }
}
