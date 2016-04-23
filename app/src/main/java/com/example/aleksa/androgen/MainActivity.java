package com.example.aleksa.androgen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aleksa.androgen.adapter.LocationListAdapter;
import com.example.aleksa.androgen.adapter.SlidingAdapter;
import com.example.aleksa.androgen.asyncTask.FetchCsvTask;
import com.example.aleksa.androgen.asyncTask.FetchPolenTask;
import com.example.aleksa.androgen.data.PolenContract;
import com.example.aleksa.androgen.data.PolenContract.LocationEntry;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "MainActivity";

    private SlidingAdapter mAdapter;
    private Context mContext;
    private LocationTracker mLocationTracker = null;
    private LocationListAdapter locationListAdapter;
    private Toast toast;

    // Hold a reference to the sharedPref listener, otherwise it gets GCed
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        // Fetch the data from .csv files and from the server, if possible and adequate
        if (savedInstanceState == null)
            fetchData();

        // Get a reference to the DrawerLayout and set its scrim color
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_main);
        drawerLayout.setScrimColor(0x00FFFFFF); // TODO move this to a color resource

        // The list of locations within the drawer
        final ListView locationList = (ListView) findViewById(R.id.location_selection_list_view);
        setLocationListAdapter(locationList);

        // A reference to the LinearLayout which holds the drawer's contents
        final LinearLayout drawerContent = (LinearLayout) findViewById(R.id.drawer_content);

        // Set the drawer drawer to be 65% of screen width
        DrawerLayout.LayoutParams params =
                (DrawerLayout.LayoutParams) drawerContent.getLayoutParams();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.65);
        drawerContent.setLayoutParams(params);

        // Add onClickListener to the location button so it opens the drawer
        ImageButton locationButton = (ImageButton) findViewById(R.id.location_button);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Add onClickListener to the "detect my location" TextView within the drawer
        TextView detectLocationView = (TextView) findViewById(R.id.detect_location_text);
        detectLocationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLocationTracker == null)
                    mLocationTracker = new LocationTracker(mContext);

                // Check if the mLocationTracker is already connected or connecting
                if (!mLocationTracker.isActive()){
                    mLocationTracker.connect();
                }
            }
        });

        // Create an adapter for our viewpager and attach it
        ViewPager pager = (ViewPager) findViewById(R.id.main_pager);
        mAdapter = new SlidingAdapter(getSupportFragmentManager(), this);
        pager.setAdapter(mAdapter);

        // Set onClickListener to the floating action button
        FloatingActionButton floatingAB = (FloatingActionButton) findViewById(R.id.main_fab);
        floatingAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlantSelectionActivity.class);
                startActivity(intent);
            }
        });

        // Set onClickListener to the refresh button
        ImageButton refreshButton = (ImageButton) findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);

        registerLocationPreferencesListener();
    }

    @Override
    protected void onResume() {
        // This is needed so that the slider reflects the changes made in selection activity
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected void onStop() {
        // Unregister the SharedPreferences listener
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.shared_pref_plants), Context.MODE_PRIVATE);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);

        if (!locationListAdapter.getCursor().isClosed())
            locationListAdapter.getCursor().close();

        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // Re-register the location sharedPref listener
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.shared_pref_plants), Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(mListener);
    }

    /*
                Attaches a SimpleCursorAdapter to the given location ListView

                Also attaches OnClickListener to all the items in the list to modify the selected location
                 */
    private void setLocationListAdapter(final ListView locationList){

        if (locationListAdapter == null) {

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

            // Create a simple cursor adapter with our locations cursor and set it to the drawer list
            locationListAdapter = new LocationListAdapter(
                    this,
                    R.layout.location_selection_list_item,
                    locationCursor,
                    new String[]{LocationEntry.COLUMN_NAME},
                    new int[]{R.id.item_location_selection},
                    0,
                    locationList);
        }
        locationList.setAdapter(locationListAdapter);

        locationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Utilities.setLocation((int) l, mContext);
                locationListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchData(){
        // TODO Move this somewhere else so it doesn't cancel

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            FetchPolenTask fetchPolenTask = FetchPolenTask.getInstance(mContext);

            if (!(fetchPolenTask.getStatus() == AsyncTask.Status.RUNNING)) {
                fetchPolenTask.execute();
                showFetchToast("Ažuriranje...");
            }
            else
                showFetchToast("Ažuriranje je već u toku.");

        }
        else
            showFetchToast("Nema internet konekcije, ažuriranje neuspelo.");

        if (Utilities.isFirstLaunch(this)) {
            FetchCsvTask fetchCsvTask = FetchCsvTask.getInstance(this);
            fetchCsvTask.execute();
        }
    }

    private void showFetchToast(String message){
        if (toast != null)
            toast.cancel();

        boolean condition = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
        if ((toast == null && condition) || !condition)
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        if ((toast != null && condition))
            toast.setText(message);
        toast.show();
    }

    private void registerLocationPreferencesListener(){

        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.shared_pref_plants), Context.MODE_PRIVATE);

        // Add a listener to watch for location changes and notify the adapter on change
        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                // Notify change only if the changed value is the selected location ID
                if (key.equals(Utilities.LOCATION_SHAREDPREF_KEY)) {
                    mAdapter.notifyDataSetChanged();
                    if (locationListAdapter != null)
                        locationListAdapter.notifyDataSetChanged();
                    getSupportLoaderManager().restartLoader(0, null, (MainActivity)mContext);
                }
            }
        };

        // Register the SharedPreferences listener
        sharedPref.registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri queryUri = PolenContract.PolenEntry.buildPolenLocationGroupBy(
            String.valueOf(Utilities.getPreferredLocation(mContext)));

        String sortOrder = PolenContract.PolenEntry.COLUMN_PLANT_ID + " ASC ";

        return new CursorLoader(mContext,
                queryUri,
                null,
                null, null,
                sortOrder);
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
