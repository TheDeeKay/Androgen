package com.example.aleksa.androgen.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/*
    Database contract for the polen database

    The database holds three tables: measurement entries, locations and plants
 */
public class PolenContract {


    // Content authority for the polen provider
    public static final String CONTENT_AUTHORITY = "com.example.aleksa.androgen";

    // The base URI for accessing content of the databases
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // The paths appended to the base content uri in order to access the desired table
    public static final String PATH_PLANT = "plant";
    public static final String PATH_POLEN = "polen";
    public static final String PATH_LOCATION = "location";

    public static final String GROUP_BY = "group";

    /*
    TODO
    Standardizes the date
    Takes in local time in milliseconds, returns UTC time in millis
     */
    public static long standardizeServerDate(long date) {
        return date;
    }

    /*
        Polen measurements table, contains: plant and location IDs, date, concentration and tendency
     */
    public static final class PolenEntry implements BaseColumns {

        // Content URI base for polen table
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POLEN).build();

        // Strings marking whether the action returns single or multiple entries
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POLEN;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POLEN;

        // Table name
        public static final String TABLE_NAME = "polen";

        // Date of the measurement, to be formatted in UTC time
        public static final String COLUMN_DATE = "date";

        // ID of the location where the measurement was taken
        public static final String COLUMN_LOCATION_ID = "location_id";

        // ID of the plant whose polen concentration was measured
        public static final String COLUMN_PLANT_ID = "plant_id";

        // Concentration of the plant's polen
        public static final String COLUMN_CONCENTRATION = "concentration";

        // Tendency of plant's polen concentration
        public static final String COLUMN_TENDENCY = "tendency";


        // Returns URI referencing polen entry with the given id
        public static Uri buildPolenUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Returns a URI referencing polen entry with the given location ID and plant ID
        public static Uri buildPolenLocationPlant(String location, String plant){
            return CONTENT_URI.buildUpon().
                    appendPath(location).appendPath(plant).build();
        }

        // Returns URI referencing polen entry with the given location, date, and plant
        public static Uri buildPolenLocationWithDateAndPlant(String location, long date, String plant){
            long standardDate = standardizeServerDate(date);
            return CONTENT_URI.buildUpon().appendPath(location).
                    appendPath(Long.toString(standardDate)).appendPath(plant).build();
        }

        // Returns a URI referencing polen entry with the given location ID
        public static Uri buildPolenLocation(String location){
            return CONTENT_URI.buildUpon().appendPath(location).build();
        }

        // Returns a URI referencing polen entries with the given location ID, grouped by plant ID
        public static Uri buildPolenLocationGroupBy(String location){
            return CONTENT_URI.buildUpon().appendPath(location).appendPath(GROUP_BY).build();
        }

    }


    /*
        Locations table, containing basic location info
     */
    public static final class LocationEntry implements BaseColumns{

        // Content URI base for locations table
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        // Strings marking whether the action returns single or multiple entries
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        // Table name
        public static final String TABLE_NAME = "location";

        // Location name
        public static final String COLUMN_NAME = "name";

        // Location id
        public static final String COLUMN_LOCATION_ID = "id";

        // Location's latitude and longitude
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";

        // Returns URI referencing location entry with the given id
        public static Uri buildLocationUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /*
        Plants table, containing basic plants info
     */
    public static final class PlantEntry implements BaseColumns{

        // Content URI base for plants table
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANT).build();

        // Strings marking whether the action returns single or multiple entries
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLANT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLANT;

        // Table name
        public static final String TABLE_NAME = "plant";

        // Plant name in Serbian
        public static final String COLUMN_NAME = "name";

        // Plant ID
        public static final String COLUMN_PLANT_ID = "id";

        // Plant allergenic potency
        public static final String COLUMN_PLANT_ALLERGENIC_INDEX = "allergen";

        // Returns URI referencing plant entry with the given id
        public static Uri buildPlantUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    // Takes a uri and extracts location info from it
    public static String getLocationFromUri(Uri uri){
        return uri.getPathSegments().get(1);
    }

    // Takes a uri and extracts date from it, then standardizes it
    public static long getDateFromUri(Uri uri){
        return standardizeServerDate(Long.parseLong(uri.getPathSegments().get(2)));
    }

    // Takes a uri and extracts plant id from it
    public static int getPlantFromUri(Uri uri){
        return Integer.parseInt(uri.getPathSegments().get(3));
    }

}
