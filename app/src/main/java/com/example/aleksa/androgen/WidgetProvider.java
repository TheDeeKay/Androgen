package com.example.aleksa.androgen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import com.example.aleksa.androgen.asyncTask.FetchPolenTask;
import com.example.aleksa.androgen.data.PolenContract;

public class WidgetProvider extends AppWidgetProvider {

    private static final String FETCH_DATA_EXTRA = "fetchData";

    public static final String FETCH_COMPLETE_EXTRA = "fetchComplete";

    private static final int FETCH_AND_UPDATE = 0;
    private static final int UPDATE = 1;

    private static final int STANDARD_REFRESH_IMAGE = 100;
    private static final int CURRENTLY_REFRESHING_IMAGE = 101;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getExtras() != null &&
                intent.getExtras().getInt(FETCH_DATA_EXTRA, UPDATE) == FETCH_AND_UPDATE){

            if (FetchPolenTask.getInstance(context).getStatus() != AsyncTask.Status.RUNNING) {

                FetchPolenTask.getInstance(context).execute();

                setRefreshButtonImage(context, CURRENTLY_REFRESHING_IMAGE);
            } else {
                FetchPolenTask.alreadyRunningToast(context);
            }

        }
        else {

            if (intent.getExtras() != null && intent.hasExtra(FETCH_COMPLETE_EXTRA)){

                setRefreshButtonImage(context, STANDARD_REFRESH_IMAGE);
            }

            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int length = appWidgetIds.length;

        for (int i = 0; i < length; i++) {

            // TODO determine the widget plant
            Uri queryUri = PolenContract.PolenEntry.buildPolenLocationPlant(
                    String.valueOf(Utilities.getPreferredLocation(context)), String.valueOf(0));

            Cursor queryResults = context.getContentResolver().query(
                    queryUri,
                    null,
                    null, null,
                    null
            );

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            Intent intent = new Intent(context, WidgetProvider.class);
            intent.putExtra(FETCH_DATA_EXTRA, FETCH_AND_UPDATE);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            PendingIntent pending = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.widget_refresh_button, pending);

            if (queryResults.moveToFirst()) {

                int concentration = queryResults.getInt(
                        queryResults.getColumnIndex(PolenContract.PolenEntry.COLUMN_CONCENTRATION));

                String text;

                switch (concentration) {
                    case 0: {
                        text = "Havarija";
                        break;
                    }
                    case 1: {
                        text = "Bleh";
                        break;
                    }
                    case 2: {
                        text = "Bogme...";
                        break;
                    }
                    case 3: {
                        text = "Haos";
                        break;
                    }

                    default:
                        text = "Nema gi";
                }

                views.setTextViewText(R.id.widget_status_text, text);
            }

            queryResults.close();

            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }
    }

    private void setRefreshButtonImage(Context context, int image){

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        switch (image) {


            case STANDARD_REFRESH_IMAGE: {

                remoteViews.setImageViewResource(
                        R.id.widget_refresh_button,R.drawable.widget_refresh_image);
                break;
            }


            case CURRENTLY_REFRESHING_IMAGE: {
                remoteViews.setImageViewResource(
                        R.id.widget_refresh_button, R.drawable.widget_refreshing);
            }
        }
    }
}
