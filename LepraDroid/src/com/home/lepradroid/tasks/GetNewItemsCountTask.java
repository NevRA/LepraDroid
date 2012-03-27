package com.home.lepradroid.tasks;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import com.home.lepradroid.R;
import com.home.lepradroid.Widget;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Utils;

public class GetNewItemsCountTask extends BaseTask
{
    private Context context;
    public GetNewItemsCountTask(Context context)
    {
        this.context = context;
    }
    
    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            int counter = ServerWorker.Instance().getNewItemsCount();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_view);
            Utils.updateWidget(remoteViews, counter);
            ComponentName thisWidget = new ComponentName(context, Widget.class);
            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        }
        catch (Exception e)
        {
            setException(e);
        }
        
        return e;
    }
}
