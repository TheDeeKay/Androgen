package com.example.aleksa.androgen.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.aleksa.androgen.R;
import com.example.aleksa.androgen.Utilities;
import com.example.aleksa.androgen.data.PolenContract.PlantEntry;

// TODO change the color of the strikethrough here somehow
public class SelectionCursorAdapter extends SimpleCursorAdapter{

    public SelectionCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View returnView = convertView;

        // If there is no old view to recycle, just inflate a new one
        if (returnView == null){
            LayoutInflater layoutInflater;
            layoutInflater = LayoutInflater.from(mContext);
            returnView = layoutInflater.inflate(R.layout.plants_selection_list_item, null);
        }

        if (mCursor.moveToPosition(position)){

            TextView textView = (TextView) returnView.findViewById(R.id.item_plant_selection);

            // Get plant name
            String plantName = mCursor.getString(
                    mCursor.getColumnIndex(PlantEntry.COLUMN_NAME));

            final int plantId = Utilities.getPlantIdFromSorted(position, mContext);

            textView.setText(plantName);

            // If it's not selected, we want it to be strikethrough
            if (!Utilities.plantSelected(plantId, mContext)){
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            returnView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TextView text = (TextView) v;

                    if (Utilities.plantSelected(plantId, mContext)){

                        text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        Utilities.setPlantSelected(plantId, Utilities.UNSELECTED, mContext);
                    }
                    else {

                        text.setPaintFlags(text.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        Utilities.setPlantSelected(plantId, Utilities.SELECTED, mContext);
                    }

                }
            });
        }

        return returnView;
    }
}
