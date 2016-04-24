package com.example.aleksa.androgen;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.example.aleksa.androgen.asyncTask.FetchCsvTask;
import com.example.aleksa.androgen.asyncTask.FetchPolenTask;
import com.example.aleksa.androgen.data.PolenContract;
import com.example.aleksa.androgen.data.PolenDbHelper;

import java.util.HashSet;

public class TestDb extends AndroidTestCase{

    @Override
    protected void setUp() throws Exception {
        deleteDb();
    }

    // Deletes the pollen database
    private void deleteDb() {
        mContext.deleteDatabase(PolenDbHelper.DATABASE_NAME);
    }

    public void testCreateDb() throws Throwable {

        final HashSet<String> tableNames = new HashSet<>();
        tableNames.add(PolenContract.PolenEntry.TABLE_NAME);
        tableNames.add(PolenContract.PlantEntry.TABLE_NAME);
        tableNames.add(PolenContract.LocationEntry.TABLE_NAME);

        // Close the db, open it and assert it's open
        mContext.deleteDatabase(PolenDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new PolenDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Get the names of the tables that currently exist
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: no tables are open.", c.moveToFirst());

        // Remove any table names that currently exist from our tableNames hashSet
        do {
            tableNames.remove(c.getString(0));
        } while (c.moveToNext());

        // If the tableNames hashSet is not empty, it means that not all the tables are open
        assertTrue("Error: not all the tables are open.", tableNames.isEmpty());

        FetchPolenTask fetchPolenTask = FetchPolenTask.getInstance(getContext());
        fetchPolenTask.execute();

        FetchCsvTask fetchCsvTask = FetchCsvTask.getInstance(getContext());
        fetchCsvTask.execute();

        ContentValues cv = new ContentValues();
        cv.put(PolenContract.PolenEntry.COLUMN_CONCENTRATION, 1);
        cv.put(PolenContract.PolenEntry.COLUMN_DATE, 1);
        cv.put(PolenContract.PolenEntry.COLUMN_LOCATION_ID, 1);
        cv.put(PolenContract.PolenEntry.COLUMN_PLANT_ID, 1);
        cv.put(PolenContract.PolenEntry.COLUMN_TENDENCY, 0);
        getContext().getContentResolver().insert(PolenContract.PolenEntry.CONTENT_URI,
                cv);

        cv.clear();
        cv.put(PolenContract.LocationEntry.COLUMN_NAME, "Beograd");
        cv.put(PolenContract.LocationEntry.COLUMN_LOCATION_ID, 1);
        cv.put(PolenContract.LocationEntry.COLUMN_LATITUDE, 0);
        cv.put(PolenContract.LocationEntry.COLUMN_LONGITUDE, 0);
        getContext().getContentResolver().insert(
                PolenContract.LocationEntry.CONTENT_URI, cv
        );

        cv.clear();
        cv.put(PolenContract.PlantEntry.COLUMN_NAME, "Jova");
        cv.put(PolenContract.PlantEntry.COLUMN_PLANT_ALLERGENIC_INDEX, 1);
        cv.put(PolenContract.PlantEntry.COLUMN_PLANT_ID, 1);
        getContext().getContentResolver().insert(
                PolenContract.PlantEntry.CONTENT_URI, cv
        );

//        Uri queryUri = PolenContract.PolenEntry.CONTENT_URI;
        Uri queryUri = PolenContract.PolenEntry.buildPolenLocationPlant("1", "1");
        Cursor random = getContext().getContentResolver().query(
                queryUri,
                null,
                null, null,
                null
        );

        assertTrue("Database is empty", random.moveToFirst());
        assertEquals("Jova", random.getString(random.getColumnIndex(PolenContract.PlantEntry.COLUMN_NAME)));

        random.close();

    }
}
