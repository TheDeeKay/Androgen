package com.example.aleksa.androgen;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;

import com.example.aleksa.androgen.data.PolenContract.LocationEntry;

/*
A class that contains all utility methods and constants
(such as
 */
public class Utilities {

    // Key for stored value for location ID in the sharedPref and default location ID
    public static final String LOCATION_SHAREDPREF_KEY = "location_id";
    public static final int DEFAULT_LOCATION_ID = 1;

    public static final int SELECTED = 1;
    public static final int UNSELECTED = 0;

    // Contains the total number of the plants in the DB
    // Initialized to 25 as default, changed to correct number when the data is fetched
    private static int totalPlantsNumber = 25;

    /*
    Sets the total number of plants in the database
    This is called when the data fetch is executed
    // TODO call this in fetch
     */
    public static void setTotalPlantsNumber(int number){
        totalPlantsNumber = number;
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

        editor.commit();
    }

    /*
    Checks whether the plant is selected or not, given the plant ID
     */
    public static boolean plantSelected(int plantID, Context context){

        // Get a handle to the shared preferences containing info about plant selections
        // The values are stored as (String plantId, int selected)
        // selected values are 0 and 1 (1 for selected, 0 for unselected)
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_pref_plants), context.MODE_PRIVATE
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
                context.getString(R.string.shared_pref_plants), context.MODE_PRIVATE
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
        return plantsSelectedCount(totalPlantsNumber-1, context);
    }

    /*
    Returns the ID of the plant that has the given position among the selected ones
    Returns -1 if the plant ID is not found
     */
    public static int getPlantIdAtPosition(int position, Context context){

        // check if position is valid
        if (plantsSelectedCount(context) <= position)
            return -1;

        int i;

        for (i = 0; (i < totalPlantsNumber) && (position > -1); i++)
            if (plantSelected(i, context)) --position;

        return i-1;
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
    public static int getPlantIdFromSorted(int sortedIndex, Context context){
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

        return returnIndex;
    }

    /*
    // TODO set a listener for this in the MainActivity
    Set the selected location to the ID given as parameter
     */
    public static void setLocation(int locationId, Context context){

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_pref_plants), Context.MODE_PRIVATE);

        sharedPref.edit().putInt(LOCATION_SHAREDPREF_KEY, locationId).commit();
    }
}
