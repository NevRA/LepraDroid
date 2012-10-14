package com.home.lepradroid;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.UpdateBadgeCounterTask;
import com.home.lepradroid.utils.Badge;
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
                clickIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                clickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));
                
                PendingIntent pendIntent = PendingIntent.getActivity(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Badge badge = new Badge();
                try
                {
                    if(SettingsWorker.Instance().IsLogoned())
                        badge = UpdateBadgeCounterTask.GetItemsCount();
                }
                catch (Exception e)
                {
                    // TODO: handle exception
                }
                
                Integer prevCounter = SettingsWorker.Instance().loadUnreadCounter();
                Integer newCounter = Utils.updateWidget(remoteViews, badge);
                
                remoteViews.setOnClickPendingIntent(R.id.widget, pendIntent);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
                
                if(newCounter == 0)
                    Utils.clearNotification(this.getApplicationContext());
                else
                {
                    if(Utils.isNotifyOnUnreadOnlyOnce(this.getApplicationContext()))
                    {
                        if(prevCounter == 0 && newCounter > 0)
                            Utils.pushNotification(this.getApplicationContext());
                    }
                    else
                    {
                        if(prevCounter < newCounter)
                            Utils.pushNotification(this.getApplicationContext());
                    } 
                }
            }
            catch (Throwable t)
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