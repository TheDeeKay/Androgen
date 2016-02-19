package com.example.aleksa.androgen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentMain extends Fragment {

    public static final String PLANT_ID = "plant_id";

    /*
    ViewHolder class for the views in the fragment
     */
    class ViewHolder {

        TextView plantName;
        TextView flavorText;
        TextView statusText;
        TextView percentageText;

    }

    // holds the ID of the plant displayed within the fragment
    public int displayedPlantID;

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
            //TODO fragment had arguments, instantiate appropriately
        }

        return rootView;
    }


}
