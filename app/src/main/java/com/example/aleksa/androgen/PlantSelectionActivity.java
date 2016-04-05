package com.example.aleksa.androgen;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.example.aleksa.androgen.adapter.SelectionCursorAdapter;
import com.example.aleksa.androgen.data.PolenContract;

public class PlantSelectionActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>{

    private SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plants_selection);

        mAdapter = new SelectionCursorAdapter(this,
                R.layout.plants_selection_list_item, null,
                new String[]{PolenContract.PlantEntry.COLUMN_NAME},
                new int[]{R.id.item_plant_selection}, 0);

        ((ListView)findViewById(R.id.list_plants_selection)).setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri queryUri = PolenContract.PlantEntry.CONTENT_URI;

        String[] columns = new String[]{
                PolenContract.PlantEntry.COLUMN_PLANT_ID + " AS " + BaseColumns._ID,
                PolenContract.PlantEntry.COLUMN_NAME
        };

        return new CursorLoader(this,
                queryUri,
                columns,
                null, null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}