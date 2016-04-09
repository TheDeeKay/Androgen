package com.example.aleksa.androgen;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ListView;

import com.example.aleksa.androgen.adapter.SelectionCursorAdapter;
import com.example.aleksa.androgen.data.PolenContract;

import io.codetail.animation.SupportAnimator;

import static io.codetail.animation.ViewAnimationUtils.createCircularReveal;

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

        final ListView list = ((ListView)findViewById(R.id.list_plants_selection));

        list.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null){

            list.setVisibility(View.INVISIBLE);

            ViewTreeObserver viewTreeObserver = list.getViewTreeObserver();

            if (viewTreeObserver.isAlive()){
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        revealAnimation(list);
                        list.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

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

    private void revealAnimation(final View view){

        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;

        int dx = Math.max(view.getWidth() - cx, cx);
        int dy = Math.max(view.getHeight() - cy, cy);
        float finalRadius = (float) Math.hypot(dx, dy);

        SupportAnimator animator = createCircularReveal(view, cx, cy, 0, finalRadius);

        animator.setDuration(500);

        view.setVisibility(View.VISIBLE);

        animator.start();
    }
}