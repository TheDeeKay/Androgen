package com.example.aleksa.androgen.data;

import android.provider.BaseColumns;

/*
    Database contract for the polen database

    The database holds three tables: measurement entries, locations and plants
 */
public class PolenContract {


    /*
        Polen measurements table, contains: plant and location IDs, date, concentration and tendency
     */
    public static final class PolenEntry implements BaseColumns {

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

    }


    /*
        Locations table, containing basic location info
     */
    public static final class LocationEntry implements BaseColumns{

        // Table name
        public static final String TABLE_NAME = "location";

        // Location name
        public static final String COLUMN_NAME = "name";

        // Location's latitude and longitude
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";

    }


    /*
        Plants table, containing basic plants info
     */
    public static final class PlantEntry implements BaseColumns{

        // Table name
        public static final String TABLE_NAME = "plant";

        // Plant name in latin
        public static final String COLUMN_NAME = "name";

    }

}
