package com.example.aleksa.androgen.asyncTask;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.aleksa.androgen.R;
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
        final String PLANT_ID = "ID_VRSTE";

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
                plantId = Integer.parseInt(polenEntry.getString(PLANT_ID)) - 1;
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
            else {
                Log.e(LOG_TAG, "The CVVector is empty.");
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


    @Override
    protected Void doInBackground(Void... params) {

        /*
        TODO try to optimize this by fetching only the needed amount of data
        Determine the amount of data needed using the current date and last known fetch date
         */

        // Declare those two outside try so they can be closed in finally
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Attempt to fetch the JSON data, parse, and insert it
        // TODO this is hardcoded value here, get a better way of dealing with it
        for (int locationId = 1; locationId <= 18; locationId++) {
        try{

            // Strings that serve for building the query
            final String POLEN_BASE_URL = mContext.getString(R.string.data_sepa_gov);
            final String RESOURCE_PARAM = "resource_id";
            final String SORT_PARAM = "sort";
            final String FILTER_LOCATION_PARAM = "filters[ID_LOKACIJE]";

            // Resource IDs for different tables
            final String RESOURCE_POLEN = mContext.getString(R.string.polen_json_id);
            // Sort order, in this case sorted by date descending
            final String SORT_BY_DATE = "(DATUM desc)";
            // Construct the Uri for querying the remote database
            Uri queryPolenUri = Uri.parse(POLEN_BASE_URL).buildUpon()
                    .appendQueryParameter(RESOURCE_PARAM, RESOURCE_POLEN)
                    .appendQueryParameter(SORT_PARAM, SORT_BY_DATE)
                    .appendQueryParameter(FILTER_LOCATION_PARAM, String.valueOf(locationId))
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
            if (inputStream == null) {
                Log.e(LOG_TAG, "The input stream from remote database is empty.");
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line + "\n");

            // If buffer is empty, there's nothing to parse
            if (buffer.length() == 0) {
                Log.e(LOG_TAG, "The buffer is empty.");
                return null;
            }

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
        }}

        return null;
    }
}
