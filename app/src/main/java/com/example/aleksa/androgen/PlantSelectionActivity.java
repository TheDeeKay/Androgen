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

    private static final String TAG = "PlantSelectionActivity";

    private SelectionCursorAdapter mAdapter;

    private final static int ANIMATION_DURATION = 1000;
    private final static int ANIMATION_GROW = 0;
    private final static int ANIMATION_SHRINK = 1;

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
                        revealAnimation(rLayout, floatingAB);
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

    private void revealAnimation(View mainView, FloatingActionButton fAB){
        int[] center = new int[2];
        getFABLocation(fAB, center);

        int cx = center[0];
        int cy = center[1];

        int dx = Math.max(mainView.getWidth() - cx, cx);
        int dy = Math.max(mainView.getHeight() - cy, cy);

        final float initialRadius = (float) (fAB.getWidth() / 2.0);
        final float finalRadius = (float) Math.hypot(dx, dy);

        revealAnimation(mainView, fAB, ANIMATION_GROW, cx, cy,
                initialRadius, finalRadius, initialRadius);
    }

    private void revealAnimation(
            final View mainView, final FloatingActionButton fAB, final int direction,
            final int centerX, final int centerY,
            final float initialRadius, final float finalRadius, final float currentRadius){

        final float endRadius;
        float startRadius;
        if (direction == ANIMATION_GROW) {
            endRadius = finalRadius;
            startRadius = initialRadius;
        }
        else {
            endRadius = initialRadius;
            startRadius = finalRadius;
        }

        final SupportAnimator animator = createCircularReveal(
                mainView, centerX, centerY, currentRadius, endRadius);

        double progress = Math.abs((startRadius - currentRadius) / (finalRadius - initialRadius));
        final long duration = (long) ((1 - progress) * ANIMATION_DURATION);

        animator.setDuration(duration);

        final long startTime = System.currentTimeMillis();

        animator.addListener(new SupportAnimator.AnimatorListener() {

            boolean wasCancelled = false;

            @Override
            public void onAnimationStart() {

                if (direction == ANIMATION_GROW)
                    mainView.setVisibility(View.VISIBLE);

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
                if (!wasCancelled){

                    if (direction == ANIMATION_GROW)
                        fAB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                revealAnimation(
                                        mainView, fAB, ANIMATION_SHRINK,
                                        centerX, centerY, initialRadius, finalRadius, finalRadius
                                );
                            }
                        });

                    else {
                        mainView.setVisibility(View.INVISIBLE);
                        finish();
                    }
                }
            }

            @Override
            public void onAnimationCancel() {
                wasCancelled = true;

                int newDirection;

                if (direction == ANIMATION_GROW)
                    newDirection = ANIMATION_SHRINK;
                else
                    newDirection = ANIMATION_GROW;

                double progress = (double)(System.currentTimeMillis() - startTime) / duration;

                revealAnimation(mainView, fAB, newDirection,
                        centerX, centerY,
                        initialRadius, finalRadius,
                        (float) (progress * (endRadius - currentRadius) + currentRadius));

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