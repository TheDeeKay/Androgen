package com.example.aleksa.androgen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import com.example.aleksa.androgen.asyncTask.FetchPolenTask;
import com.example.aleksa.androgen.data.PolenContract;

public class WidgetProvider extends AppWidgetProvider {

    public static final String FETCH_DATA_EXTRA = "fetchData";
    public static final String FETCH_COMPLETE_EXTRA = "fetchComplete";

    public static final int FETCH_AND_UPDATE = 0;
    public static final int UPDATE = 1;
    public static final int FETCH_STARTED = 3;

    private static boolean updateImageFlag = false;

    @Override
    public void onReceive(Context context, Intent intent) {


        if (intent.getExtras() != null) {

            // If the intent came from pressing the widget refresh button
            if (intent.getExtras().getInt(FETCH_DATA_EXTRA, UPDATE) == FETCH_AND_UPDATE) {

                // If FetchPolenTask isn't running yet, run it
                if (FetchPolenTask.getInstance(context).getStatus() != AsyncTask.Status.RUNNING) {
                    FetchPolenTask.getInstance(context).execute();

                    // otherwise, show a Toast stating that it's already running
                } else {
                    FetchPolenTask.alreadyRunningToast(context);
                }
                updateImageFlag = true;
            }

            // If the intent came from FetchPolenTask's onPostExecute
            else if (intent.hasExtra(FETCH_COMPLETE_EXTRA))
                updateImageFlag = false;

            else if (intent.getExtras().getInt(FETCH_DATA_EXTRA, UPDATE) == FETCH_STARTED)
                updateImageFlag = true;


            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            onUpdate(context, awm, awm.getAppWidgetIds(new ComponentName(context, WidgetProvider.class)));
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int length = appWidgetIds.length;

        if (!updateImageFlag) {
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

                RemoteViews views = new RemoteViews(
                        context.getPackageName(), R.layout.widget_layout);

                Intent intent = new Intent(context, WidgetProvider.class);
                intent.putExtra(FETCH_DATA_EXTRA, FETCH_AND_UPDATE);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                PendingIntent pending = PendingIntent.getBroadcast(
                        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                views.setOnClickPendingIntent(R.id.widget_refresh_button, pending);
                views.setImageViewResource(R.id.widget_refresh_button, R.drawable.widget_refresh_image);

                if (queryResults.moveToFirst()) {

                    int concentration = queryResults.getInt(
                            queryResults.getColumnIndex(
                                    PolenContract.PolenEntry.COLUMN_CONCENTRATION));

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
        else { // TODO execute this elsewhere (instead of calling onUpdate, do it in onReceive)
            for (int i = 0; i < length; i++) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                views.setImageViewResource(R.id.widget_refresh_button, R.drawable.widget_refreshing);
                appWidgetManager.updateAppWidget(appWidgetIds[i], views);
            }

            updateImageFlag = false;
        }
    }
}
