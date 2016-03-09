package com.example.aleksa.androgen;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.aleksa.androgen.data.PolenContract.LocationEntry;
import com.example.aleksa.androgen.data.PolenContract.PlantEntry;
import com.example.aleksa.androgen.data.PolenContract.PolenEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


public class FetchPolenTask extends AsyncTask<Void, Void, Void>{

    private static final String LOG_TAG = FetchPolenTask.class.getSimpleName();
    private static final String CSV_PLANTS_PATH = "vrste polena.csv";
    private static final String CSV_LOCATIONS_PATH = "lokacije Stanica Polen.csv";
    private final Context mContext;

    public FetchPolenTask(Context context){
        mContext = context;
    }

    /*
    Takes a pollen data JSON, parses it and inserts it into the table
     */
    private void getDataFromJSON(String polenJsonStr)
        throws JSONException{

        // This is needed so we can "traverse" the JSON
        final String RESULT = "result";
        final String RECORDS = "records";

        // Location info
        final String LOCATION_ID = "ID_LOKACIJE";

        // Plant info
        final String PLANT_ID = "ID_BILJKE";

        // Entry info
        final String DATE = "DATUM";
        final String CONCENTRATION = "KONCENTRACIJA";
        final String TENDENCY = "TENDENCIJA";

        // Attempt to parse the JSON
        try{
            JSONObject polenJson = new JSONObject(polenJsonStr);
            JSONObject polenArray = polenJson.getJSONObject(RESULT);
            JSONArray results = polenArray.getJSONArray(RECORDS);

            // The number of entries
            int length = results.length();

            // This will be used to store parsed entries and later bulk insert them into DB
            Vector<ContentValues> cVVector = new Vector<ContentValues>(length);

            // Parse the JSON and make entries, store them in the ContentValues vector
            for (int i = 0; i < length; i++) {

                // Values retrieved from the Json used for entries in the DB
                int plantId;
                int locationId;
                long date;
                int concentration;
                int tendency;

                // Get JSON object representing one pollen measurement entry
                // Has specific plant, location, and date
                JSONObject polenEntry = results.getJSONObject(i);

                // Extract the needed information from the JSON object using keys defined earlier
                plantId = polenEntry.getInt(PLANT_ID);
                locationId = polenEntry.getInt(LOCATION_ID);
                concentration = polenEntry.getInt(CONCENTRATION);
                tendency = polenEntry.getInt(TENDENCY);

                // We need a specific method that converts date in form of string to milliseconds
                date = dateStringToMillis(polenEntry.getString(DATE));


                // Create a new content values object and put the info in it
                ContentValues cv = new ContentValues();

                cv.put(PolenEntry.COLUMN_PLANT_ID, plantId);
                cv.put(PolenEntry.COLUMN_LOCATION_ID, locationId);
                cv.put(PolenEntry.COLUMN_CONCENTRATION, concentration);
                cv.put(PolenEntry.COLUMN_TENDENCY, tendency);
                cv.put(PolenEntry.COLUMN_DATE, date);

                // Add the contentvalues to the vector
                cVVector.add(cv);
            }

            int inserted = 0;

            // Bulk insert the contents
            if (cVVector.size() > 0){
                ContentValues[] contents = new ContentValues[cVVector.size()];
                cVVector.toArray(contents);
                inserted = mContext.getContentResolver().bulkInsert(PolenEntry.CONTENT_URI, contents);
            }

            Log.d(LOG_TAG, "Successfully fetched data and inserted " + inserted + " entries.");

        }catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private long dateStringToMillis(String string) {

        // Expected time format from the server
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

        try {

            // Try to parse the date using the given format, return time in millis
            Date date = sdf.parse(string);
            return date.getTime();

        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing date", e);
        }

        return -1;
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
                if (contentUri == PlantEntry.CONTENT_URI)
                    insertPlantRow(RowData, contentUri);

                else if (contentUri == LocationEntry.CONTENT_URI)
                    insertLocationRow(RowData, contentUri);

            }
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
        int plant_id = Integer.parseInt(row[0]);
        String plant_name = row[2];
        int plant_allergenic_index = Integer.parseInt(row[4]);

        // Put the values into the ContentValues object
        cv.put(PlantEntry.COLUMN_PLANT_ID, plant_id);
        cv.put(PlantEntry.COLUMN_NAME, plant_name);
        cv.put(PlantEntry.COLUMN_PLANT_ALLERGENIC_INDEX, plant_allergenic_index);

        // Insert the ContentValues object in the correct table
        mContext.getContentResolver().insert(uri, cv);

    }

    /*
    Inserts a row of data into the locations table
     */
    private void insertLocationRow(String[] row, Uri uri){
        // A ContentValues object that will be used to put in the row
        ContentValues cv = new ContentValues();

        // Use the specific order in which the values appear in the .csv file
        int location_id = Integer.parseInt(row[0]);
        String location_name = row[1];
        double longitude = Double.parseDouble(row[2]);
        double latitude = Double.parseDouble(row[3]);

        // Put the values into the ContentValues object
        // using the specific order in which they appear in the .csv file
        cv.put(LocationEntry.COLUMN_LOCATION_ID, location_id);
        cv.put(LocationEntry.COLUMN_NAME, location_name);
        cv.put(LocationEntry.COLUMN_LONGITUDE, longitude);
        cv.put(LocationEntry.COLUMN_LATITUDE, latitude);

        // Insert the ContentValues object in the correct table
        mContext.getContentResolver().insert(uri, cv);
    }

    @Override
    protected Void doInBackground(Void... params) {

        /*
        TODO try to optimize this by fetching only the needed amount of data
        Determine the amount of data needed using the current date and last known fetch date
         */

        // TODO re-download .csv in the future, currently unsafe because of bad data in them
        // Attempt to parse the .csv files containing the locations and plants data
        // First, get an AssetManager and an input stream for csv
        AssetManager assetManager = mContext.getAssets();
        InputStream csvInputStream = null;

        // Attempt to open the .csv files
        try {
            // Open and parse the plants .csv
            csvInputStream = assetManager.open(CSV_PLANTS_PATH);
            parseCsv(csvInputStream, PlantEntry.CONTENT_URI);

            // Open and parse the locations .csv
            csvInputStream = assetManager.open(CSV_LOCATIONS_PATH);
            parseCsv(csvInputStream, LocationEntry.CONTENT_URI);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error opening .csv file from the assets.", e);

        } finally {

            // Close the input stream and asset manager
            try {
                csvInputStream.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error closing the .csv input stream", e);
            }
            // Close the asset manager
            assetManager.close();
        }


        // Declare those two outside try so they can be closed in finally
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Attempt to fetch the JSON data, parse, and insert it
        try{

            // Strings that serve for building the query
            final String POLEN_BASE_URL = mContext.getString(R.string.data_sepa_gov);
            final String RESOURCE_PARAM = "resource_id";

            // Resource IDs for different tables
            final String RESOURCE_POLEN = mContext.getString(R.string.polen_json_id);

            // Construct the Uris for querying the tables
            Uri queryPolenUri = Uri.parse(POLEN_BASE_URL).buildUpon()
                    .appendQueryParameter(RESOURCE_PARAM, RESOURCE_POLEN)
                    .build();

            // URL for the query
            URL urlPolen = new URL(queryPolenUri.toString());

            // Open the connection
            urlConnection = (HttpURLConnection) urlPolen.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Get the input stream
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            // If there is nothing to do
            if (inputStream == null)
                return null;

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line + "\n");

            // If buffer is empty, there's nothing to parse
            if (buffer.length() == 0)
                return null;

            String polenJsonStr = buffer.toString();
            getDataFromJSON(polenJsonStr);


        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error extracting data from JSON", e);

        } finally {

            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
        }

        return null;
    }
}
