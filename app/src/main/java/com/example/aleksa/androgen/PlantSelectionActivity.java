package com.example.aleksa.androgen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.support.annotation.ColorRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.aleksa.androgen.adapter.SelectionCursorAdapter;
import com.example.aleksa.androgen.data.PolenContract;

public class PlantSelectionActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>{

    private SelectionCursorAdapter mAdapter;
    private RelativeLayout rootLayout;
    private FrameLayout rootFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plants_selection);

        mAdapter = new SelectionCursorAdapter(this,
                R.layout.plants_selection_list_item, null,
                new String[]{PolenContract.PlantEntry.COLUMN_NAME},
                new int[]{R.id.item_plant_selection}, 0);
        rootLayout = (RelativeLayout) findViewById(R.id.root_layout);
        rootFrame = (FrameLayout) findViewById(R.id.root_frame);

        ((ListView)findViewById(R.id.list_plants_selection)).setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);

        rootLayout.post(new Runnable() {
            @Override
            public void run() {
                setupEnterAnimation();
            }
        });
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



    // NEW ANIMATION TESTING

    private void setupEnterAnimation() {
        animateRevealShow(rootLayout);
    }


    private void initViews() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(PlantSelectionActivity.this, android.R.anim.slide_in_left);
                animation.setDuration(500);
                rootFrame.startAnimation(animation);
                rootFrame.setVisibility(View.VISIBLE);
                System.out.println("CALLED INIT VIEWS!!");
            }
        });
    }

    private void animateRevealShow(final View viewRoot) {
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
            animateRevealShow(this, viewRoot, 0, R.color.colorAccent,
                cx, cy, new OnRevealAnimationListener() {
                    @Override
                    public void onRevealHide() {

                    }

                    @Override
                    public void onRevealShow() {
                        initViews();
                    }
                });
    }



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void animateRevealShow(final Context ctx, final View view, final int startRadius,
                                         @ColorRes final int color, int x, int y, final OnRevealAnimationListener listener) {
        float finalRadius = (float) Math.hypot(view.getWidth(), view.getHeight());
        Animator anim = ViewAnimationUtils.createCircularReveal(view, x, y, startRadius, finalRadius);
        anim.setDuration(ctx.getResources().getInteger(R.integer.animation_duration));
        anim.setInterpolator(new FastOutLinearInInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setBackgroundColor(ContextCompat.getColor(ctx, color));
                listener.onRevealShow();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(listener != null) {
                    //listener.onRevealShow();
                }
                view.setVisibility(View.VISIBLE);
            }
        });
        anim.start();
    }

}