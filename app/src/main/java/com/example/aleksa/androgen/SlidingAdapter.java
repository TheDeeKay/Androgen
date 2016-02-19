package com.example.aleksa.androgen;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


/*
Pager adapter class for paging the main screen with plants and their info
Pages FragmentMain
 */
public class SlidingAdapter extends FragmentStatePagerAdapter{

    public SlidingAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // Find what plant ID goes to the given position
        int plantID = Utilities.getPlantID(position);

        return FragmentMain.newInstance(plantID);
    }

    @Override
    public int getCount() {
        return Utilities.plantsSelectedCount();
    }

}
