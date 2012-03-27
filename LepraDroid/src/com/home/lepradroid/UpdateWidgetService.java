package com.home.lepradroid;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Utils;

public class UpdateWidgetService extends Service
{
    @Override
    public void onStart(Intent intent, int startId)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        int[] allWidgetIds = intent
                .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        for (int widgetId : allWidgetIds)
        {
            try
            {
                RemoteViews remoteViews = new RemoteViews(this
                        .getApplicationContext().getPackageName(),
                        R.layout.widget_view);
                
                Intent clickIntent = new Intent(getApplicationContext(), Main.class);
                clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));
                
                PendingIntent pendIntent = PendingIntent.getActivity(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                int counter = ServerWorker.Instance().getNewItemsCount();
                Utils.updateWidget(remoteViews, counter);
                remoteViews.setOnClickPendingIntent(R.id.widget, pendIntent);
                
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }
            catch (Exception e)
            {
                // TODO
            }            
        }
        stopSelf();

        super.onStart(intent, startId);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}