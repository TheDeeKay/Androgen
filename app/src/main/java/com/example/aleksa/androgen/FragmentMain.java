package com.example.aleksa.androgen;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.aleksa.androgen.data.PolenContract;
import com.example.aleksa.androgen.data.PolenContract.PlantEntry;

public class FragmentMain extends Fragment {

    public static final String PLANT_ID = "plant_id";

    // Holds the current selected location
    private String mLocation;

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

            // Get a URI that matches the plant with our ID
            Uri plantIdUri = PlantEntry.buildPlantUri(this.getArguments().getInt(PLANT_ID));

            // Cursor containing the query results for plant table, should be a single plant with our ID
            Cursor plant = getContext().getContentResolver().query(
                    PlantEntry.CONTENT_URI,
                    null,
                    PlantEntry.TABLE_NAME + "." + PlantEntry._ID + " = ?",
                    new String[]{String.valueOf(getArguments().get(PLANT_ID))},
                    null);

            // If there is a plant with such ID, set the text views to display its info
            if (plant.moveToFirst()) {



                String plantName = plant.getString(plant.getColumnIndex(PlantEntry.COLUMN_NAME));
                holder.plantName.setText(plant.getString(plant.getColumnIndex(PlantEntry.COLUMN_NAME)));

                // Get a URI that matches the pollen entry with this plant ID, for this location and date
                Uri pollenUri = PolenContract.PolenEntry.buildPolenLocationWithDateAndPlant(
                        mLocation,
                        System.currentTimeMillis(),
                        plantName
                        );

                // A cursor containing the entry for the given date, plant and location
                Cursor pollen = getContext().getContentResolver().query(
                        pollenUri,
                        null,
                        null,
                        null,
                        null);

                if (pollen.moveToFirst())
                    holder.statusText.setText(pollen.getInt(
                            pollen.getColumnIndex(PolenContract.PolenEntry.COLUMN_CONCENTRATION)
                    ));

                // TODO set the flavor text properly
            }

        }

        return rootView;
    }


}
