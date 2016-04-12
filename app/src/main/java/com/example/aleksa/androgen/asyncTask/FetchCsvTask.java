package com.example.aleksa.androgen.asyncTask;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.aleksa.androgen.R;
import com.example.aleksa.androgen.data.PolenContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FetchCsvTask extends AsyncTask<Void, Void, Void> {

    private static final String LOG_TAG = FetchCsvTask.class.getSimpleName();
    private static final String CSV_PLANTS_PATH = "vrste polena.csv";
    private static final String CSV_LOCATIONS_PATH = "lokacije Stanica Polen.csv";
    private final Context mContext;

    public FetchCsvTask(Context context){
        mContext = context;
    }

    /*
    Parses a .csv file that is passed in as an InputStream
    Includes a contentUri where the rows should be inserted
     */
    private void parseCsv(InputStream is, Uri contentUri){

        // Buffered reader for the input stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        try {

            String line;

            // Parse the csv line by line (line represents a row of data)
            while ((line = reader.readLine()) != null) {

                String[] RowData = line.split(",");

                // Determine whether it's the plants or locations csv
                // This is important because of index numbers of columns
                if (contentUri == PolenContract.PlantEntry.CONTENT_URI)
                    insertPlantRow(RowData, contentUri);

                else if (contentUri == PolenContract.LocationEntry.CONTENT_URI)
                    insertLocationRow(RowData, contentUri);

            }

            // After inserting, fill the sharedpref for sorted plant indexes
            Cursor queryPlants = mContext.getContentResolver().query(
                    PolenContract.PlantEntry.CONTENT_URI,
                    null,
                    null, null,
                    PolenContract.PlantEntry.COLUMN_NAME + " ASC"
            );

            if (queryPlants.moveToFirst()){

                SharedPreferences sharedPref = mContext.getSharedPreferences(
                        mContext.getString(R.string.shared_pref_sorted_ids), Context.MODE_PRIVATE
                );
                SharedPreferences.Editor editor = sharedPref.edit();

                do {

                    int plantId = queryPlants.getInt(
                            queryPlants.getColumnIndex(PolenContract.PlantEntry.COLUMN_PLANT_ID));

                    int sortedIndex = queryPlants.getPosition();

                    editor.putInt(String.valueOf(sortedIndex), plantId);

                } while (queryPlants.moveToNext());

                editor.commit();
            }

            queryPlants.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error parsing .csv file.", e);
        }
    }

    /*
    Inserts a row of data into the plants table
     */
    private void insertPlantRow(String[] row, Uri uri){
        // A ContentValues object that will be used to put in the row
        ContentValues cv = new ContentValues();

        // Use the specific order in which the values appear in the .csv file
        int plant_id = Integer.parseInt(row[0])-1;
        String plant_name = row[2];
        int plant_allergenic_index = Integer.parseInt(row[4]);

        // Put the values into the ContentValues object
        cv.put(PolenContract.PlantEntry.COLUMN_PLANT_ID, plant_id);
        cv.put(PolenContract.PlantEntry.COLUMN_NAME, plant_name);
        cv.put(PolenContract.PlantEntry.COLUMN_PLANT_ALLERGENIC_INDEX, plant_allergenic_index);

        // Insert the ContentValues object in the correct table
        mContext.getContentResolver().insert(uri, cv);

    }

    /*
    Inserts a row of data into the locations table
     */
    private void insertLocationRow(String[] row, Uri uri){
        // A ContentValues object that will be used to put in the row
        ContentValues cv = new ContentValues();

        // For reasons beyond my comprehension, the split method
        // adds an invisible character to the first item in the first row
        // TODO this is a temporary workaround until I figure it out
        if (!Character.isDigit(row[0].charAt(0)))
            row[0] = row[0].substring(1);

        // Use the specific order in which the values appear in the .csv file
        int location_id = Integer.parseInt(row[0]);
        String location_name = row[1];
        double longitude = Double.parseDouble(row[2]);
        double latitude = Double.parseDouble(row[3]);

        // Put the values into the ContentValues object
        // using the specific order in which they appear in the .csv file
        cv.put(PolenContract.LocationEntry.COLUMN_LOCATION_ID, location_id);
        cv.put(PolenContract.LocationEntry.COLUMN_NAME, location_name);
        cv.put(PolenContract.LocationEntry.COLUMN_LONGITUDE, longitude);
        cv.put(PolenContract.LocationEntry.COLUMN_LATITUDE, latitude);

        // Insert the ContentValues object in the correct table
        mContext.getContentResolver().insert(uri, cv);
    }

    @Override
    protected Void doInBackground(Void... params) {

        // TODO re-download .csv in the future, currently unsafe because of bad data in them
        // Attempt to parse the .csv files containing the locations and plants data
        // First, get an AssetManager and an input stream for csv
        AssetManager assetManager = mContext.getAssets();
        InputStream csvInputStream = null;

        // Attempt to open the .csv files
        try {

            // Open and parse the plants .csv
            csvInputStream = assetManager.open(CSV_PLANTS_PATH);
            parseCsv(csvInputStream, PolenContract.PlantEntry.CONTENT_URI);

            // Open and parse the locations .csv
            csvInputStream = assetManager.open(CSV_LOCATIONS_PATH);
            parseCsv(csvInputStream, PolenContract.LocationEntry.CONTENT_URI);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error opening .csv file from the assets.", e);

        } finally {

            // Close the input stream and asset manager
            try {
                csvInputStream.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error closing the .csv input stream", e);
            }
        }
        return null;
    }
}
