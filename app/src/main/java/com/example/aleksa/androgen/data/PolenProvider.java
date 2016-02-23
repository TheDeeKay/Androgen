package com.example.aleksa.androgen.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.aleksa.androgen.data.PolenContract.LocationEntry;
import com.example.aleksa.androgen.data.PolenContract.PlantEntry;
import com.example.aleksa.androgen.data.PolenContract.PolenEntry;

public class PolenProvider extends ContentProvider {

    // A URI matcher for this provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    // Unique values for matched types of URIs
    private static final int POLEN = 100;
    private static final int POLEN_WITH_LOCATION = 101;
    private static final int POLEN_WITH_LOCATION_DATE = 102;
    private static final int POLEN_WITH_LOCATION_DATE_PLANT = 103;
    private static final int LOCATION = 200;
    private static final int PLANT = 300;

    // This is a query builder for the three tables joined together
    private static final SQLiteQueryBuilder sPolenByLocationAndPlant;
    // Initialization for the query builder
    static {
        sPolenByLocationAndPlant = new SQLiteQueryBuilder();
        // Join the three tables accordingly (on plant and location IDs)
        sPolenByLocationAndPlant.setTables(
                PolenEntry.TABLE_NAME + "INNER JOIN" + LocationEntry.TABLE_NAME
                        + " ON " + PolenEntry.TABLE_NAME + "." + PolenEntry.COLUMN_LOCATION_ID + " = "
                        + LocationEntry.TABLE_NAME + "." + LocationEntry._ID
                        + " INNER JOIN" + PlantEntry.TABLE_NAME
                        + " ON " + PolenEntry.TABLE_NAME + "." + PolenEntry.COLUMN_PLANT_ID + " = "
                        + PlantEntry.TABLE_NAME + "." + PlantEntry._ID
        );
    }

    // AND keyword for SQL selection parameters
    private static final String AND = " AND ";

    // String for selection of entries by location
    // polen.location_id = ?
    private static final String sLocationSelection =
            PolenEntry.TABLE_NAME + "." +
                    PolenEntry.COLUMN_LOCATION_ID + " = ? ";

    // String for selection of entries by location and date
    // polen.location_id = ? AND polen.date = ?
    private static final String sLocationDateSelection =
            sLocationSelection + AND +
                    PolenEntry.TABLE_NAME + "." + PolenEntry.COLUMN_DATE + " = ? ";

    // String for selection of entries by location, date, and plant
    // polen.location_id = ? AND polen.date = ? AND polen.plant_id = ?
    private static final String sLocationDatePlantSelection =
            sLocationDateSelection + AND +
                    PolenEntry.TABLE_NAME + "." + PolenEntry.COLUMN_PLANT_ID + " = ? ";

    // A database defined in the contract and helper
    private PolenDbHelper mOpenHelper;

    // Builds a UriMatcher used later to determine what kind of action is performed
    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PolenContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PolenContract.PATH_POLEN, POLEN);
        matcher.addURI(authority, PolenContract.PATH_POLEN + "/*", POLEN_WITH_LOCATION);
        matcher.addURI(authority, PolenContract.PATH_POLEN + "/*/#", POLEN_WITH_LOCATION_DATE);
        matcher.addURI(authority, PolenContract.PATH_POLEN + "/*/#/*", POLEN_WITH_LOCATION_DATE_PLANT);
        matcher.addURI(authority, PolenContract.PATH_LOCATION, LOCATION);
        matcher.addURI(authority, PolenContract.PATH_PLANT, PLANT);

        return matcher;
    }

    private Cursor getPolenLocationDatePlant(Uri uri, String[] projection, String sortOrder) {
        // TODO fix these to be read from the uri
        String location = null;
        String plant = null;
        long date = 0;

        return sPolenByLocationAndPlant.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationDatePlantSelection,
                new String[]{location, Long.toString(date), plant},
                null, null,
                sortOrder);
    }

    private Cursor getPolenLocationDate(Uri uri, String[] projection, String sortOrder) {
        // TODO fix these to implement methods that read the values from the uri
        String location = null;
        long date = 0;

        return sPolenByLocationAndPlant.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationDateSelection,
                new String[]{location, Long.toString(date)},
                null, null,
                sortOrder
        );
    }

    private Cursor getPolenLocation(Uri uri, String[] projection, String sortOrder) {
        // TODO
        return null;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PolenDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)){

            // polen/*/#/*
            case POLEN_WITH_LOCATION_DATE_PLANT: {
                retCursor = getPolenLocationDatePlant(uri, projection, sortOrder);
                break;
            }

            // polen/*/#
            case POLEN_WITH_LOCATION_DATE: {
                retCursor = getPolenLocationDate(uri, projection, sortOrder);
                break;
            }

            // polen/*
            case POLEN_WITH_LOCATION: {
                retCursor = getPolenLocation(uri, projection, sortOrder);
                break;
            }

            // polen/
            case POLEN: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PolenEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder
                );
                break;
            }

            // location/
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder
                );
                break;
            }

            // plant/
            case PLANT: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PlantEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        // Get the type of the URI according to our matcher build before
        final int match = sUriMatcher.match(uri);

        // Act accordingly by the type of match
        switch (match) {
            case POLEN:
                return PolenEntry.CONTENT_TYPE;

            case POLEN_WITH_LOCATION:
                return PolenEntry.CONTENT_TYPE;

            case POLEN_WITH_LOCATION_DATE:
                return PolenEntry.CONTENT_TYPE;

            case POLEN_WITH_LOCATION_DATE_PLANT:
                return PolenEntry.CONTENT_ITEM_TYPE;

            case LOCATION:
                return LocationEntry.CONTENT_TYPE;

            case PLANT:
                return PlantEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch (match) {

            case POLEN: {
                long _id = db.insert(PolenEntry.TABLE_NAME, null, values);

                if (_id != -1)
                    returnUri = PolenEntry.buildPolenUri(_id);
                else
                    throw new SQLException("Error inserting row into " + uri);

                break;
            }
            case LOCATION: {
                long _id = db.insert(LocationEntry.TABLE_NAME, null, values);

                if (_id != -1)
                    returnUri = LocationEntry.buildLocationUri(_id);
                else
                    throw new SQLException("Error inserting row into " + uri);
                break;
            }
            case PLANT: {
                long _id = db.insert(PlantEntry.TABLE_NAME, null, values);

                if (_id != -1)
                    returnUri = PlantEntry.buildPlantUri(_id);
                else
                    throw new SQLException("Error inserting row into " + uri);

                break;
            }
            default:
                throw new SQLException("Error inserting row into " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // Code for deleting every row of data
        if (selection == null) selection = "1";

        switch (match){

            case POLEN: {

                rowsDeleted = db.delete(PolenEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case LOCATION: {
                rowsDeleted = db.delete(LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case PLANT: {
                rowsDeleted = db.delete(PlantEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match){

            case POLEN: {

                // normalize the date in values
                normalizeDate(values);

                rowsUpdated = db.update(PolenEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            case LOCATION: {

                rowsUpdated = db.update(LocationEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            case PLANT: {

                rowsUpdated = db.update(PlantEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: "+ uri);
        }

        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);


        switch (match) {

            case POLEN: {

                // open a transaction
                db.beginTransaction();
                int returnCount = 0;

                // attempt to transact all the values into the database and count them
                try {

                    for (ContentValues value: values) {
                        long _id = db.insert(PolenEntry.TABLE_NAME, null, value);
                        if (_id != -1)
                            returnCount++;
                    }

                    db.setTransactionSuccessful();

                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }

            default:
                return super.bulkInsert(uri, values);
        }
    }

    // Normalizes date values in the argument, using the date conversion given in the contract
    private void normalizeDate(ContentValues values) {

        if (values.containsKey(PolenEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(PolenEntry.COLUMN_DATE);
            values.put(PolenEntry.COLUMN_DATE, PolenContract.standardizeDate(dateValue));
        }
    }
}
