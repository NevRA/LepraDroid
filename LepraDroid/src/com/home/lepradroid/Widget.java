package com.home.lepradroid;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.home.lepradroid.serverworker.ServerWorker;

public class Widget extends AppWidgetProvider 
{    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds)
    {        
        Intent intent = new Intent(context, Main.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_view);
        views.setOnClickPendingIntent(R.id.widget, pendIntent);
        
        try
        {
            int counter = ServerWorker.Instance().getNewItemsCount();
            views.setViewVisibility(R.id.widget_counter, counter == 0 ? View.INVISIBLE : View.VISIBLE);
            views.setTextViewText(R.id.widget_counter, Integer.toString(counter));
        }
        catch (Exception e)
        {
            // TODO
        }
        
        appWidgetManager.updateAppWidget(appWidgetIds[0], views);
    }
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
    }
}
