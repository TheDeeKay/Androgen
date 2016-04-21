package com.example.aleksa.androgen.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.aleksa.androgen.R;
import com.example.aleksa.androgen.Utilities;

public class LocationListAdapter extends SimpleCursorAdapter{

    int defaultColor;

    public LocationListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    public LocationListAdapter(Context context, int layout, Cursor c, String[] from, int[] to,
                               int flags, ListView locationList){
        super(context, layout, c, from, to, flags);
        defaultColor = locationList.getSolidColor();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View returnView =  super.getView(position, convertView, parent);

        if (position == (Utilities.getPreferredLocation(mContext) - 1))
            returnView.setBackgroundColor(
                    ContextCompat.getColor(mContext, R.color.themeColor));
        else
            returnView.setBackgroundColor(defaultColor);

        return returnView;
    }
}
