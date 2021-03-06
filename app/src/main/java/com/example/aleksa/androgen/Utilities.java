package com.example.aleksa.androgen;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;

import com.example.aleksa.androgen.data.PolenContract;
import com.example.aleksa.androgen.data.PolenContract.LocationEntry;

/*
A class that contains all utility methods and constants
(such as
 */
public class Utilities {

    // Key for stored value for location ID in the sharedPref and default location ID
    public static final String LOCATION_SHAREDPREF_KEY = "location_id";
    public static final int DEFAULT_LOCATION_ID = 1;

    private static final String PLANTS_NUMBER_SHAREDPREF_KEY = "plants_number";
    private static final String FIRST_LAUNCH_SHAREDPREF_KEY = "first_launch";

    private static final int FIRST_LAUNCH = 0;
    private static final int SUBSEQUENT_LAUNCH = 1;

    public static final int SELECTED = 1;
    public static final int UNSELECTED = 0;

    /*
    Gets the total number of plants in the database
    This is changed when the .csv data fetch is executed
     */
    public static int getTotalPlantsNumber(Context context){

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_pref_plants), Context.MODE_PRIVATE);

        return sharedPref.getInt(PLANTS_NUMBER_SHAREDPREF_KEY, 25);
    }

    /*
    Sets the total number of plants
     */
    public static void setTotalPlantsNumber(int totalPlantsNumber, SharedPreferences sharedPref){

        sharedPref.edit().putInt(PLANTS_NUMBER_SHAREDPREF_KEY, totalPlantsNumber).apply();
    }

    /*
    Changes the selected status of a plant with the given ID
    The selected status is changed to 0 or 1, given by the selected parameter
     */
    public static void setPlantSelected(int plantId, int selected, Context context){

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_pref_plants), Context.MODE_PRIVATE
        );

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(String.valueOf(plantId), selected);
        editor.apply();
    }

    /*
    Checks whether the plant is selected or not, given the plant ID
     */
    public static boolean isPlantSelected(int plantID, Context context){

        // Get a handle to the shared preferences containing info about plant selections
        // The values are stored as (String plantId, int selected)
        // selected values are 0 and 1 (1 for selected, 0 for unselected)
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_pref_plants), Context.MODE_PRIVATE
        );

        return (sharedPref.getInt(Integer.toString(plantID), SELECTED) == SELECTED);
    }

    /*
    Returns the number of selected plants up until (including) the plant with the ID provided
     */
    public static int plantsSelectedCount(int plantID, Context context){

        // Get a handle to the shared preferences containing info about plant selections
        // The values are stored as (String plantId, int selected)
        // selected values are 0 and 1 (0 for selected, 1 for unselected)
        // It also holds the information for the selected location
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_pref_plants), Context.MODE_PRIVATE
        );

        // Count the selected plants
        int count = 0;

        // Iterate through sharedPref and check every plantId for selection
        for (int i = 0; i <= plantID; i++){
            if (sharedPref.getInt(Integer.toString(i), SELECTED) == SELECTED) count++ ;
        }

        return count;
    }

    /*
    Gets the total number of currently selected plants
     */
    public static int plantsSelectedCount(Context context){
        return plantsSelectedCount(getTotalPlantsNumber(context)-1, context);
    }

    /*
    Returns the ID of the plant that has the given position among the selected ones, sorted by name
    Returns -1 if the plant ID is not found
     */
    public static int getPlantIdAtPositionSorted(int position, Context context){

        // check if position is valid
        if (plantsSelectedCount(context) <= position)
            return -1;

        int i;

        for (i = 0; (i < getTotalPlantsNumber(context)) && (position > -1); i++)
            if (isPlantSelected(getPlantIdFromSortedIndex(i, context), context)) {
                --position;
            }

        return getPlantIdFromSortedIndex(i-1, context);
    }

    // Returns the ID of currently selected location, or the default one
    public static int getPreferredLocation(Context context){
        SharedPreferences sharedPref =
                context.getSharedPreferences(context.getString(R.string.shared_pref_plants),
                        Context.MODE_PRIVATE);

        return sharedPref.getInt(LOCATION_SHAREDPREF_KEY, DEFAULT_LOCATION_ID);
    }

    /*
    The plants in the database have IDs assigned to them based on Latin name
    We are displaying plants ordered by Serbian name

    Thus it is useful to know which sorted index corresponds to which plantId
     */
    public static int getPlantIdFromSortedIndex(int sortedIndex, Context context){
        int plantId;

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_pref_sorted_ids), Context.MODE_PRIVATE
        );

        plantId = sharedPref.getInt(String.valueOf(sortedIndex), sortedIndex);

        return plantId;
    }

    /*
    Takes longitude and latitude of a location and returns the nearest location ID from our DB
     */
    public static int findNearestLocation(double longitude, double latitude, Context context,
                                          float[] distanceToNearest) {

        Uri queryUri = LocationEntry.CONTENT_URI;

        Cursor locations = context.getContentResolver().query(
                queryUri,
                null,
                null, null,
                null
        );

        // Set to -1, which is return code of this method for "query failed"
        int returnIndex = -1;

        distanceToNearest[0] = Float.POSITIVE_INFINITY;

        if (locations.moveToFirst()){

            int idColumnIndex = locations.getColumnIndex(LocationEntry.COLUMN_LOCATION_ID);
            int longitudeColumnIndex = locations.getColumnIndex(LocationEntry.COLUMN_LONGITUDE);
            int latitudeColumnIndex = locations.getColumnIndex(LocationEntry.COLUMN_LATITUDE);

            do{

                double currLongitude = locations.getDouble(longitudeColumnIndex);
                double currLatitude = locations.getDouble(latitudeColumnIndex);

                float[] distance = new float[1];

                Location.distanceBetween(
                        currLatitude, currLongitude,
                        latitude, longitude,
                        distance);

                if (distance[0] < distanceToNearest[0]) {

                    distanceToNearest[0] = distance[0];
                    returnIndex = locations.getInt(idColumnIndex);
                }

            } while(locations.moveToNext());
        }

        locations.close();

        return returnIndex;
    }

    public static String getPlantName(int plantId, Context context){

        Uri queryUri = PolenContract.PlantEntry.buildPlantUri(plantId);

        Cursor queryCursor = context.getContentResolver().query(
                queryUri,
                null,
                null, null,
                null
        );

        if (queryCursor.moveToFirst()){

            String name = queryCursor.getString(
                    queryCursor.getColumnIndex(PolenContract.PlantEntry.COLUMN_NAME));

            queryCursor.close();

            return name;
        }

        queryCursor.close();

        return null;
    }

    /*
    Set the selected location to the ID given as parameter
     */
    public static void setLocation(int locationId, Context context){

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_pref_plants), Context.MODE_PRIVATE);

        sharedPref.edit().putInt(LOCATION_SHAREDPREF_KEY, locationId).apply();
    }


    /*
    Determines if this is the first launch, using a field in shared preferences
    If it's the first launch, it sets the field to indicate that the app has been launched already
     */
    public static boolean isFirstLaunch(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_pref_plants), Context.MODE_PRIVATE);

        if (sharedPref.getInt(FIRST_LAUNCH_SHAREDPREF_KEY, FIRST_LAUNCH) == FIRST_LAUNCH) {
            sharedPref.edit().putInt(FIRST_LAUNCH_SHAREDPREF_KEY, SUBSEQUENT_LAUNCH).apply();
            return true;
        }
        else
            return false;
    }
}
