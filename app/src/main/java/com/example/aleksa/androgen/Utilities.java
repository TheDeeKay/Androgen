package com.example.aleksa.androgen;

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
    public static boolean plantSelected(int plantID){
        //TODO check whether selected, once the DBs are in
        return true;
    }

    /*
    Returns the number of selected plants up until (including) the plant with the ID provided
     */
    public static int plantsSelectedCount(int plantID){
        //TODO implement actual method once DBs are in
        return 25;
    }

    /*
    Gets the total number of currently selected plants
     */
    public static int plantsSelectedCount(){
        //TODO fix to depend on the same with arguments
        return 5;
    }

    /*
    Returns the ID of the plant that has the given position among the selected ones
    Returns -1 if the plant ID is not found
     */
    public static int getPlantID(int position){

        // check if position is valid
        if (plantsSelectedCount() - 1 > position)
            return -1;

        int i;

        for (i = 0; (i < totalPlantsNumber) && (position > -1); i++)
            if (plantSelected(i)) --position;

        return i-1;
    }

}
