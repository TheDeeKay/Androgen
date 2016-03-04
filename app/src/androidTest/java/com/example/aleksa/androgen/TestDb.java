package com.example.aleksa.androgen;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

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

    }
}
