package com.example.aleksa.androgen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class FragmentMain extends Fragment {

    public static final String PLANT_NAME = "plant_name";
    public static final String CONCENTRATION = "concentration";
    public static final String ENTRY_DATE = "date";

    private static final int PLANT_INFO_LOADER = 1;
    private static final int POLEN_INFO_LOADER = 2;

    private ViewHolder mHolder;

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
    Creates a new FragmentMain displaying the entry with the given info
     */
    public static FragmentMain newInstance(String plantName, int concentration, long date) {

        Bundle args = new Bundle();
        args.putString(PLANT_NAME ,plantName);
        args.putInt(CONCENTRATION, concentration);
        args.putLong(ENTRY_DATE, date);

        FragmentMain fragment = new FragmentMain();
        fragment.setArguments(args);
        return fragment;
    }

    /*
    Creates a fragment with default layout
     */
    public static FragmentMain newInstance(){

        FragmentMain fragment = new FragmentMain();

        fragment.setArguments(null);

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

        mHolder = new ViewHolder();
        mHolder.plantName = (TextView) rootView.findViewById(R.id.plant_name);
        mHolder.flavorText = (TextView) rootView.findViewById(R.id.flavor_text);
        mHolder.statusText = (TextView) rootView.findViewById(R.id.status_text);
        mHolder.percentageText = (TextView) rootView.findViewById(R.id.percentage_text);

        Bundle arguments = this.getArguments();

        if (arguments != null){

            mHolder.plantName.setText(arguments.getString(PLANT_NAME));

            readResults(arguments.getInt(CONCENTRATION));

            if (!isRecentDate(arguments.getLong(ENTRY_DATE))) {
                // TODO warn that the data shown is not fresh
            }
        }

        return rootView;
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
    private void readResults(int concentration){

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

        mHolder.flavorText.setText(flavorText);
        mHolder.percentageText.setText(percentageText);
    }

    /*
    A method that deals with checking whether the latest date in the DB is recent enough

    Can change in the future if the updates to the online DB become more frequent
     */
    private boolean isRecentDate(long dataMillis){

        long midnightMillis = getMidnightMillis();

        return (dataMillis == midnightMillis);
    }
}
