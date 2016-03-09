package com.example.aleksa.androgen;

import android.content.Context;
import android.content.SharedPreferences;

/*
A class that contains all utility methods and constants
(such as
 */
public class Utilities {

    //contains the total number of the plants in the DB
    //TODO init this appropriately
    private static int totalPlantsNumber = 25;

    /*
    Checks whether the plant is selected or not, given the plant ID
     */
    public static boolean plantSelected(int plantID, Context context){

        // Get a handle to the shared preferences containing info about plant selections
        // The values are stored as (String plantId, int selected)
        // selected values are 0 and 1 (0 for selected, 1 for unselected)
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_pref_plants), context.MODE_PRIVATE
        );

        return (sharedPref.getInt(Integer.toString(plantID), 0) == 0);
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
            if (sharedPref.getInt(Integer.toString(i), 0) == 0) count++ ;
        }

        return count;
    }

    /*
    Gets the total number of currently selected plants
     */
    public static int plantsSelectedCount(Context context){
        return plantsSelectedCount(totalPlantsNumber, context);
    }

    /*
    Returns the ID of the plant that has the given position among the selected ones
    Returns -1 if the plant ID is not found
     */
    public static int getPlantIdAtPosition(int position, Context context){

        // check if position is valid
        if (plantsSelectedCount(context) - 1 > position)
            return -1;

        int i;

        // Get shared preferences for the selected plants
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_pref_plants), Context.MODE_PRIVATE);

        for (i = 0; (i < totalPlantsNumber) && (position > -1); i++)
            if (plantSelected(i, context)) --position;

        return i-1;
    }

}
