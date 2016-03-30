package com.example.aleksa.androgen;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aleksa.androgen.data.PolenContract.PlantEntry;
import com.example.aleksa.androgen.data.PolenContract.PolenEntry;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class FragmentMain extends Fragment {

    private static final String PLANT_ID = "plant_id";

    // ID of default location, used if there is no selected location in shared preferences
    private static final int DEFAULT_LOCATION_ID = 1;


    /*
    ViewHolder class for the views in the fragment
     */
    class ViewHolder {

        TextView plantName;
        TextView flavorText;
        TextView statusText;
        TextView percentageText;

    }


    public FragmentMain() {
        // Required empty public constructor
    }

    /*
    Creates a new FragmentMain displaying the plant with plantID
     */
    public static FragmentMain newInstance(int plantID) {

        Bundle args = new Bundle();
        args.putInt(PLANT_ID ,plantID);

        FragmentMain fragment = new FragmentMain();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);

        ViewHolder holder = new ViewHolder();
        holder.plantName = (TextView) rootView.findViewById(R.id.plant_name);
        holder.flavorText = (TextView) rootView.findViewById(R.id.flavor_text);
        holder.statusText = (TextView) rootView.findViewById(R.id.status_text);
        holder.percentageText = (TextView) rootView.findViewById(R.id.percentage_text);

        if (this.getArguments() != null) {
            // Set the view contents using the arguments given
            setViewContents(this.getArguments(), holder);
        }

        return rootView;
    }

    /*
    Called to set the view contents to appropriate values when instantiating the fragment
     */
    private void setViewContents(Bundle arguments, ViewHolder holder){

        // Get a URI that matches the plant with our ID
        Uri plantIdUri = PlantEntry.buildPlantUri(arguments.getInt(PLANT_ID));

        // Cursor containing the query results for plant table, should be a single plant with our ID
        Cursor plant = getContext().getContentResolver().query(
                plantIdUri,
                null,
                null,
                null,
                null);

        // If there is a plant with such ID, set the text views to display its info
        if (plant.moveToFirst()) {

            holder.plantName.setText(plant.getString(plant.getColumnIndex(PlantEntry.COLUMN_NAME)));

            int plantId = plant.getInt(plant.getColumnIndex(PlantEntry.COLUMN_PLANT_ID));

            // Finally, set the flavor and percentage text
            setFlavorAndPercentage(holder, plantId);
        }

        plant.close();

    }

    /*
     Used to determine and set the percentage and flavor texts
      */
    private void setFlavorAndPercentage(ViewHolder holder, int plantId){

        // Get today's midnight time
        long midnightTime = getMidnightMillis();

        // Get the location setting, or use default if it isn't available
        SharedPreferences sharedPref = getContext().getSharedPreferences(
                getContext().getString(R.string.shared_pref_plants),
                Context.MODE_PRIVATE
        );
        int locationId = sharedPref.getInt(Utilities.LOCATION_SHAREDPREF_KEY, Utilities.DEFAULT_LOCATION_ID);

        // Now, create a Uri for querying the database, contains locationId and plantId
        Uri queryUri = PolenEntry.buildPolenLocationPlant(String.valueOf(locationId), String.valueOf(plantId));

        // Query the database for this locationId and plantId
        Cursor queryResults = getContext().getContentResolver().query(
                queryUri,
                null,
                null,
                null,
                PolenEntry.COLUMN_DATE + " DESC"
        );

        // Check whether there's results from the query
        if (queryResults.moveToFirst()) {

            readResults(holder, queryResults);

            // Check whether the data is today's
            // If not, trigger some warning
            long queryDate =
                    queryResults.getLong(queryResults.getColumnIndex(PolenEntry.COLUMN_DATE));
            if (queryDate != midnightTime) {

                // TODO display some warning that the data is not fresh

                holder.percentageText.setText("Vreme nesto ne valja");

            }
        }
        else {
            // TODO remove later, just for testing purposes for now
            Toast.makeText(getContext(), "Ma daj bre", Toast.LENGTH_LONG).show();
        }

        queryResults.close();

    }

    /*
    Returns the last midnight's UTC time in milliseconds since era's beginning

    This is because we retrieve the time of day's beginning when querying the server
    so we have to match it when querying the database
     */
    private long getMidnightMillis(){

        // First, we need to get the current time in UTC format
        SimpleTimeZone utc = new SimpleTimeZone(0, TimeZone.getAvailableIDs(0)[0]);
        Calendar nowTime = new GregorianCalendar(utc);

        // Set HH:MM:SS.MS to 00:00:00.0, ie. midnight
        nowTime.set(Calendar.HOUR_OF_DAY, 0);
        nowTime.set(Calendar.MINUTE, 0);
        nowTime.set(Calendar.SECOND, 0);
        nowTime.set(Calendar.MILLISECOND, 0);

        return nowTime.getTimeInMillis();
    }


    /*
    TODO decide later how to actually formulate the text
    Reads the query result and correctly sets the flavor and percentage texts

    Auxiliary method for setFlavorAndPercentageText, so that changes to the process
    of reading the results and converting to text can be more easily changed
     */
    private void readResults(ViewHolder holder, Cursor queryResults){

        int concentration = queryResults.getInt(queryResults.getColumnIndex(PolenEntry.COLUMN_CONCENTRATION));

        String flavorText;
        String percentageText;

        switch (concentration) {

            case 0: {
                flavorText = "Suri bre, havarija";
                percentageText = "0%";
                break;
            }

            case 1: {
                flavorText = "Tu i tamo, bedak";
                percentageText = "33%";
                break;
            }

            case 2: {
                flavorText = "Samo se pazi, ja cu ti alergiju poslat";
                percentageText = "66%";
                break;
            }

            case 3: {
                flavorText = "Kataklizma, fort djeded";
                percentageText = "100%";
                break;
            }

            default: {
                flavorText = "Nesto ne valja";
                percentageText = "-9001%";
            }
        }

        holder.flavorText.setText(flavorText);
        holder.percentageText.setText(percentageText);
    }
}
