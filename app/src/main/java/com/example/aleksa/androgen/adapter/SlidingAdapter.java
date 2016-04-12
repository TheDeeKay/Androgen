package com.example.aleksa.androgen.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.aleksa.androgen.FragmentMain;
import com.example.aleksa.androgen.Utilities;


/*
Pager adapter class for paging the main screen with plants and their info
Pages FragmentMain
 */
public class SlidingAdapter extends FragmentStatePagerAdapter{

    // Member variable for storing the context in which the adapter is used
    Context mContext;

    public SlidingAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        // Find what plant ID goes to the given position
        int plantID = Utilities.getPlantIdAtPosition(position, mContext);

        return FragmentMain.newInstance(plantID);
    }

    @Override
    public int getCount() {
        return Utilities.plantsSelectedCount(mContext);
    }

    @Override
    public int getItemPosition(Object object) {
        // TODO optimize this
        return POSITION_NONE;
    }
}
