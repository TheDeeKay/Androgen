package com.example.aleksa.androgen;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.aleksa.androgen.adapter.SelectionCursorAdapter;
import com.example.aleksa.androgen.data.PolenContract;

import io.codetail.animation.SupportAnimator;

import static io.codetail.animation.ViewAnimationUtils.createCircularReveal;

public class PlantSelectionActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>{

    private SimpleCursorAdapter mAdapter;

    private final static int ANIMATION_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plants_selection);

        mAdapter = new SelectionCursorAdapter(this,
                R.layout.plants_selection_list_item, null,
                new String[]{PolenContract.PlantEntry.COLUMN_NAME},
                new int[]{R.id.item_plant_selection}, 0);

        ListView list = ((ListView) findViewById(R.id.list_plants_selection));

        list.setAdapter(mAdapter);

        // Remove borders between TextViews
        list.setDivider(null);

        getSupportLoaderManager().initLoader(0, null, this);

        // Remove the enter and exit animations from this activity, since they mess with reveal
        this.overridePendingTransition(0, 0);

        final RelativeLayout rLayout =
                (RelativeLayout) findViewById(R.id.selection_relative_layout);

        final FloatingActionButton floatingAB = (FloatingActionButton) findViewById(R.id.selection_fab);


        if (savedInstanceState == null) {

            rLayout.setVisibility(View.INVISIBLE);

            ViewTreeObserver viewTreeObserver = rLayout.getViewTreeObserver();

            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        revealAnimation(rLayout, floatingAB, ANIMATION_DURATION);
                        rLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
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


    // TODO there's still a bug in the animator
    // if it's started and then clicked 2 times before it finishes
    private void revealAnimation(final View mainView, final FloatingActionButton fAB, int time){

        int[] center = new int[2];

        getFABLocation(fAB, center);

        int cx = center[0];
        int cy = center[1];

        int dx = Math.max(mainView.getWidth() - cx, cx);
        int dy = Math.max(mainView.getHeight() - cy, cy);

        float initialRadius = (float) (fAB.getWidth() / 2.0);

        float finalRadius = (float) Math.hypot(dx, dy);

        initialRadius = initialRadius +
                (finalRadius - initialRadius) * (ANIMATION_DURATION -time) / ANIMATION_DURATION;

        final SupportAnimator animator =
                createCircularReveal(mainView, cx, cy, initialRadius, finalRadius);

        animator.setDuration(time);

        final long startTime = System.currentTimeMillis();

        animator.addListener(new SupportAnimator.AnimatorListener() {

            boolean wasCancelled = false;

            @Override
            public void onAnimationStart() {
                if (Build.VERSION.SDK_INT >= 21)
                    fAB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            animator.cancel();
                        }
                    });
                else
                    fAB.setOnClickListener(null);
            }

            @Override
            public void onAnimationEnd() {
                if (!wasCancelled)
                    fAB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideAnimation(mainView, fAB, ANIMATION_DURATION);
                        }
                    });
            }

            @Override
            public void onAnimationCancel() {
                wasCancelled = true;
                hideAnimation(
                        mainView, fAB, (int) (System.currentTimeMillis() - startTime));
            }

            @Override
            public void onAnimationRepeat() {

            }
        });

        mainView.setVisibility(View.VISIBLE);

        animator.start();
    }

    private void hideAnimation(final View mainView, final FloatingActionButton fAB, int time){

        int[] center = new int[2];
        getFABLocation(fAB, center);

        int cx = center[0];
        int cy = center[1];

        int dx = Math.max(mainView.getWidth() - cx, cx);
        int dy = Math.max(mainView.getHeight() - cy, cy);

        float initialRadius = (float) Math.hypot(dx, dy);

        float finalRadius = (float) (fAB.getWidth() / 2.0);

        initialRadius = initialRadius +
                (finalRadius - initialRadius) * (ANIMATION_DURATION -time) / ANIMATION_DURATION;

        final SupportAnimator animator =
                createCircularReveal(mainView, cx, cy, initialRadius, finalRadius);

        animator.setDuration(time);

        final long startTime = System.currentTimeMillis();

        animator.addListener(new SupportAnimator.AnimatorListener() {

            boolean wasCancelled = false;

            @Override
            public void onAnimationStart() {
                if (Build.VERSION.SDK_INT >= 21)
                    fAB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            animator.cancel();
                        }
                    });
                else
                    fAB.setOnClickListener(null);
            }

            @Override
            public void onAnimationEnd() {
                if (!wasCancelled) {
                    mainView.setVisibility(View.INVISIBLE);
                    finish();
                }
            }

            @Override
            public void onAnimationCancel() {
                wasCancelled = true;
                revealAnimation(
                        mainView, fAB, (int) (System.currentTimeMillis() - startTime));
            }

            @Override
            public void onAnimationRepeat() {

            }
        });

        animator.start();
    }

    private void getFABLocation(FloatingActionButton fAB, int[] results){

        int[] beginning = new int[2];

        fAB.getLocationOnScreen(beginning);

        results[0] = beginning[0] + (fAB.getWidth() / 2);

        results[1] = beginning[1] + (fAB.getHeight() / 2);
    }
}