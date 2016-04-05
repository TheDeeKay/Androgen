package com.example.aleksa.androgen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.aleksa.androgen.adapter.SlidingAdapter;
import com.example.aleksa.androgen.asyncTask.FetchCsvTask;
import com.example.aleksa.androgen.asyncTask.FetchPolenTask;

public class MainActivity extends AppCompatActivity {

    private SlidingAdapter mAdapter;

    private ViewPager mPager;

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

        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        // Notify change only if the changed value is the selected location ID
                        if (key == Utilities.LOCATION_SHAREDPREF_KEY)
                            mAdapter.notifyDataSetChanged();
                    }
                };


        LocationTracker lt = new LocationTracker(this);
        lt.connect();

        // Create an adapter for our viewpager and attach it
        mPager = (ViewPager) findViewById(R.id.main_pager);
        mAdapter = new SlidingAdapter(getSupportFragmentManager(), this);
        mPager.setAdapter(mAdapter);

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
    protected void onStop() {
        // Unregister the SharedPreferences listener
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.shared_pref_plants), Context.MODE_PRIVATE);

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);

        super.onStop();
    }
}
