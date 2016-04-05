package com.example.aleksa.androgen;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.aleksa.androgen.adapter.SlidingAdapter;
import com.example.aleksa.androgen.asyncTask.FetchCsvTask;
import com.example.aleksa.androgen.asyncTask.FetchPolenTask;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FetchPolenTask fetchPolenTask = new FetchPolenTask(this);
        fetchPolenTask.execute();

        FetchCsvTask fetchCsvTask = new FetchCsvTask(this);
        fetchCsvTask.execute();

        LocationTracker lt = new LocationTracker(this);
        lt.connect();

        // Create an adapter for our viewpager and attach it
        ViewPager pager = (ViewPager) findViewById(R.id.main_pager);
        SlidingAdapter adapter = new SlidingAdapter(getSupportFragmentManager(), this);
        pager.setAdapter(adapter);
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
}
