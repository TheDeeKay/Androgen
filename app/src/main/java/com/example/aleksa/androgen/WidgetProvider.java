package com.example.aleksa.androgen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import com.example.aleksa.androgen.data.PolenContract;

public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

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
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);
        PendingIntent pending = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.widget_refresh_button, pending);

        if (queryResults.moveToFirst()) {

            int concentration = queryResults.getInt(
                    queryResults.getColumnIndex(PolenContract.PolenEntry.COLUMN_CONCENTRATION));

            String text;

            switch (concentration){
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

                default: text = "Nema gi";
            }

            views.setTextViewText(R.id.widget_status_text, text);
        }

        queryResults.close();

        appWidgetManager.updateAppWidget(appWidgetIds[0], views);
    }
}
