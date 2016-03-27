package com.example.aleksa.androgen;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


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
        // TODO the (plantID+1) is temporary, once the .csv fetch is fixed
        // this should be reverted back to just (plantID).
        // This is because currently the plant IDs start at 1, not 0

        // Find what plant ID goes to the given position
        int plantID = Utilities.getPlantIdAtPosition(position, mContext);

        return FragmentMain.newInstance(plantID);
    }

    @Override
    public int getCount() {
        return Utilities.plantsSelectedCount(mContext);
    }

}
