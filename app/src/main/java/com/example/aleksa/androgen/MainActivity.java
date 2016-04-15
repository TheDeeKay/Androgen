package com.example.aleksa.androgen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.aleksa.androgen.adapter.SlidingAdapter;
import com.example.aleksa.androgen.asyncTask.FetchCsvTask;
import com.example.aleksa.androgen.asyncTask.FetchPolenTask;
import com.example.aleksa.androgen.data.PolenContract.LocationEntry;

public class MainActivity extends AppCompatActivity {

    private SlidingAdapter mAdapter;

    // Hold a reference to the sharedPref listener, otherwise it gets GCed
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO set a listener to this. Also, trigger under appropriate conditions
        // If it's the first launch, set a delay after this so it finishes the fetch first
        // Check if there's internet connection, and move this somewhere else so it doesn't cancel
        FetchPolenTask fetchPolenTask = new FetchPolenTask(this);
        fetchPolenTask.execute();

        FetchCsvTask fetchCsvTask = new FetchCsvTask(this);
        fetchCsvTask.execute();


        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.shared_pref_plants), Context.MODE_PRIVATE);

        // Add a listener to watch for location changes and notify the adapter on change
        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        // Notify change only if the changed value is the selected location ID
                        if (key == Utilities.LOCATION_SHAREDPREF_KEY)
                            mAdapter.notifyDataSetChanged();
                    }
                };

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_main);
        drawerLayout.setScrimColor(0xFFFFFF);

        // Get a cursor containing all of our locations
        Cursor locationCursor = getContentResolver().query(
                LocationEntry.CONTENT_URI,
                new String[]{
                        LocationEntry.COLUMN_LOCATION_ID + " AS " + BaseColumns._ID,
                        LocationEntry.COLUMN_NAME},
                null, null,
                LocationEntry.COLUMN_LOCATION_ID + " ASC"
        );

        locationCursor.moveToFirst();

        final ListView locationList = (ListView) findViewById(R.id.location_selection_list_view);

        // Create a simple cursor adapter with our locations cursor and set it to the drawer list
        SimpleCursorAdapter locationAdapter = new SimpleCursorAdapter(
                this,
                R.layout.location_selection_list_item,
                locationCursor,
                new String[]{LocationEntry.COLUMN_NAME},
                new int[]{R.id.item_location_selection},
                0
        );
        locationList.setAdapter(locationAdapter);

        final ImageButton locationButton = (ImageButton) findViewById(R.id.location_button);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    drawerLayout.openDrawer(locationList);
                    locationButton.setX(locationList.getWidth());
            }
        });

        // Create an adapter for our viewpager and attach it
        ViewPager pager = (ViewPager) findViewById(R.id.main_pager);
        mAdapter = new SlidingAdapter(getSupportFragmentManager(), this);
        pager.setAdapter(mAdapter);

        FloatingActionButton floatingAB = (FloatingActionButton) findViewById(R.id.main_fab);

        final Context context = this;

        floatingAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlantSelectionActivity.class);
                startActivity(intent);
            }
        });

        // Register the SharedPreferences listener
        sharedPref.registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected void onStop() {
        // Unregister the SharedPreferences listener
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.shared_pref_plants), Context.MODE_PRIVATE);

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);

        super.onStop();
    }
}
