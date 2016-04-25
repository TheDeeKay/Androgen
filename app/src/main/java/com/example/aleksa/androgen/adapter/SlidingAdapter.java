package com.example.aleksa.androgen.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.aleksa.androgen.FragmentMain;
import com.example.aleksa.androgen.Utilities;
import com.example.aleksa.androgen.data.PolenContract;


/*
Pager adapter class for paging the main screen with plants and their info
Pages FragmentMain
 */
public class SlidingAdapter extends FragmentStatePagerAdapter{

    // Member variable for storing the context in which the adapter is used
    public Context mContext;

    // A cursor from which we draw the data for fragments
    private Cursor mCursor;

    public SlidingAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        // Find what plant ID goes to the given position
        int plantId = Utilities.getPlantIdAtPositionSorted(position, mContext);

        if (mCursor != null && mCursor.moveToPosition(plantId)){

            String plantName = Utilities.getPlantName(plantId, mContext);
            int concentration = mCursor.getInt(
                    mCursor.getColumnIndex(PolenContract.PolenEntry.COLUMN_CONCENTRATION));
            long date = mCursor.getLong(
                    mCursor.getColumnIndex(PolenContract.PolenEntry.COLUMN_DATE));

            return FragmentMain.newInstance(plantName, concentration, date);
        }

        return FragmentMain.newInstance();
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

    public void swapCursor(Cursor cursor){
        mCursor = cursor;
        if (mContext != null)
            notifyDataSetChanged();
    }
}
